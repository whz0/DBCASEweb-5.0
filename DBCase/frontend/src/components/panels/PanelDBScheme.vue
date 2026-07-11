<script setup lang="ts">
import { MySQL, PostgreSQL } from 'dt-sql-parser'
import { editor as monacoEditor, Range } from 'monaco-editor'
import CodeEditor from 'monaco-editor-vue3'
import { useToast } from 'primevue'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import AccessDatabaseDialog from '@/components/dialogs/AccessDatabaseDialog.vue'
import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import { useTheme } from '@/composables/useTheme'
import { DiagramType, useDiagramStore } from '@/stores/diagramStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const { t } = useI18n()
const toast = useToast()
const { actualTheme } = useTheme()

const getCssVar = (name: string) =>
  getComputedStyle(document.documentElement).getPropertyValue(name).trim() || undefined

const defineMonacoThemes = () => {
  monacoEditor.defineTheme('aura-light', {
    base: 'vs',
    inherit: true,
    rules: [],
    colors: { 'editor.background': getCssVar('--p-surface-0') ?? '#ffffff' },
  })
  monacoEditor.defineTheme('aura-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [],
    colors: { 'editor.background': '#18181b' },
  })
}

const monacoTheme = computed(() => (actualTheme.value === 'dark' ? 'aura-dark' : 'aura-light'))

watch(monacoTheme, (theme) => {
  editorInstance?.updateOptions({ theme })
})
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

let editorInstance: monacoEditor.IStandaloneCodeEditor | null = null
let decorationsCollection: monacoEditor.IEditorDecorationsCollection | null = null

const highlightQuestionLines = () => {
  if (!editorInstance || !code.value) return

  const newDecorations = code.value
    .split('\n')
    .reduce((acc: monacoEditor.IModelDeltaDecoration[], line: string, i: number) => {
      if (line.includes('?'))
        acc.push({
          range: new Range(i + 1, 1, i + 1, 1),
          options: {
            isWholeLine: true,
            className: 'highlight-question-line',
            inlineClassName: 'highlight-question-line',
          },
        })
      return acc
    }, [])

  if (decorationsCollection) {
    decorationsCollection.set(newDecorations)
  } else {
    decorationsCollection = editorInstance.createDecorationsCollection(newDecorations)
  }
}

const handleEditorDidMount = (editor: monacoEditor.IStandaloneCodeEditor) => {
  editorInstance = editor
  defineMonacoThemes()
  editor.updateOptions({ theme: monacoTheme.value })

  editor.onDidChangeModelContent(() => {
    highlightQuestionLines()
  })

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
  if (!selectLanguage.value || !code.value || code.value.includes('?')) return []
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
  <div class="flex items-center gap-1 px-2 py-3 min-w-0">
    <div class="flex shrink-0 items-center min-w-0">
      <Select
        v-model="selectLanguage"
        :options="languages"
        optionLabel="language"
        optionValue="value"
        placeholder="Select a Language"
        class="w-40 min-w-0"
      />
    </div>
    <div class="flex-1 min-w-0 text-center">
      <h1 class="text-xl font-semibold truncate">{{ t('panels.physical') }}</h1>
    </div>
    <div class="flex shrink-0 items-center">
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
      :theme="monacoTheme"
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

<style>
.highlight-question-line {
  background-color: rgba(255, 200, 0, 0.15);
  border-left: 3px solid #ffc800;
}
</style>
