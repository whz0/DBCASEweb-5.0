<script setup lang="ts">
import { MySQL, PostgreSQL } from 'dt-sql-parser'
import type { editor as MonacoEditor } from 'monaco-editor'
import { Range } from 'monaco-editor'
import CodeEditor from 'monaco-editor-vue3'
import { useToast } from 'primevue'
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import AccessDatabaseDialog from '@/components/dialogs/AccessDatabaseDialog.vue'
import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import { DiagramType, useDiagramStore } from '@/stores/diagramStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const { t } = useI18n()
const toast = useToast()
const panelStore = useGeneratePanelStore()
const { transform } = useDiagramStore()
const diagramStore = useDiagramStore()
const erSchemaStore = useErSchemaStore()

const parsers = {
  mysql: new MySQL(),
  postgresql: new PostgreSQL(),
}

const selectLanguage = ref()
const languages = ref([
  { language: 'MySQL', value: 'mysql' },
  { language: 'PostgreSQL', value: 'postgresql' },
])

const code = ref()
const toastMessage = (message: string, severity: 'error' | 'warn' | 'info' | 'success') =>
  toast.add({ severity, detail: message, life: 3000 })

let editorInstance: MonacoEditor.IStandaloneCodeEditor | null = null
let decorationsCollection: MonacoEditor.IEditorDecorationsCollection | null = null

const highlightQuestionLines = () => {
  if (!editorInstance || !code.value) return

  const newDecorations = code.value
    .split('\n')
    .reduce((acc: MonacoEditor.IModelDeltaDecoration[], line: string, i: number) => {
      if (line.includes('?'))
        acc.push({
          range: new Range(i + 1, 1, i + 1, 1),
          options: { isWholeLine: true, className: 'highlight-question-line' },
        })
      return acc
    }, [])

  if (decorationsCollection) {
    decorationsCollection.set(newDecorations)
  } else {
    decorationsCollection = editorInstance.createDecorationsCollection(newDecorations)
  }
}

const handleEditorDidMount = (editor: MonacoEditor.IStandaloneCodeEditor) => {
  editorInstance = editor
  highlightQuestionLines()
}

watch(
  () => diagramStore.dbResult,
  (val) => {
    if (val) {
      code.value = val
      highlightQuestionLines()
    }
  },
)

const editorOptions = {
  fontSize: 14,
  minimap: { enabled: false },
  automaticLayout: true,
}

const validate = (): string[] => {
  const parser = parsers[selectLanguage.value as keyof typeof parsers]
  if (!parser) return []
  return parser.validate(code.value).map((e) => e.message)
}

const showTransform = ref(false)
const showDeploy = ref(false)

const handleTransform = async (value: DiagramType) => {
  showTransform.value = false
  const validationErrors = validate()
  if (Array.isArray(validationErrors) && validationErrors.length > 0) return

  const diagram = { sql: code.value }
  const data = await transform(diagram, DiagramType.db, value, toastMessage)

  if (value === DiagramType.er) {
    if (data) erSchemaStore.loadSnapshot(data)
    if (!panelStore.isOpen(PanelId.ERScheme)) panelStore.open(PanelId.ERScheme)
  } else if (value == DiagramType.logical) {
    if (!panelStore.isOpen(PanelId.LogicalScheme)) panelStore.open(PanelId.LogicalScheme)
  }
}
</script>

<template>
  <div class="grid grid-cols-3 my-4">
    <div class="card flex justify-center">
      <Select
        v-model="selectLanguage"
        :options="languages"
        optionLabel="language"
        optionValue="value"
        placeholder="Select a Language"
        class="w-full md:w-56"
      />
    </div>
    <div class="text-3xl">
      <h1>{{ t('panels.physical') }}</h1>
    </div>
    <div>
      <Button
        severity="secondary"
        class="bi bi-arrow-left-right"
        @click="showTransform = true"
        v-tooltip.bottom="t('schema.transform')"
        text
      />
      <Button
        severity="secondary"
        class="bi bi-play-circle"
        @click="showDeploy = true"
        v-tooltip.bottom="'Deploy to DB'"
        text
      />
      <Button
        severity="secondary"
        class="bi bi-x-lg"
        @click="panelStore.close(PanelId.BDScheme)"
        v-tooltip.bottom="t('panels.close')"
        text
      />
    </div>
  </div>

  <div class="h-full my-4">
    <CodeEditor
      v-model:value="code"
      language="sql"
      theme="vs-white"
      :options="editorOptions"
      @editorDidMount="handleEditorDidMount"
    />
  </div>

  <TransformDiagramDialog
    v-model:visible="showTransform"
    source-type="physical"
    @transform="handleTransform"
  />

  <AccessDatabaseDialog v-model:visible="showDeploy" :database-type="selectLanguage" :code="code" />
</template>

<style scoped></style>
