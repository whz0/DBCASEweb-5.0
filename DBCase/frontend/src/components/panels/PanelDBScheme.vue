<script setup lang="ts">
import { MySQL, PostgreSQL } from 'dt-sql-parser'
import CodeEditor from 'monaco-editor-vue3'
import { useToast } from 'primevue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import { DiagramType, useDiagramStore } from '@/stores/diagramStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const { t } = useI18n()
const toast = useToast()
const panelStore = useGeneratePanelStore()
const { save } = useDiagramStore()

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

const editorOptions = {
  fontSize: 14,
  minimap: { enabled: false },
  automaticLayout: true,
}

const validate = () => {
  const parser = parsers[selectLanguage.value as keyof typeof parsers]
  return parser.validate(code.value).map((e) => e.message)
}

const handleSave = () => {
  if (validate()) {
    save(code.value, DiagramType.db, (message, severity) =>
      toast.add({ severity: severity, detail: message, life: 3000 }),
    )
  }
}

const showTransform = ref(false)

const handleTransform = (target: 'er' | 'logical' | 'physical') => {
  showTransform.value = false
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
        class="bi bi-save"
        @click="handleSave"
        v-tooltip.bottom="t('common.save')"
        text
      />
      <Button
        severity="secondary"
        class="bi bi-arrow-left-right"
        @click="showTransform = true"
        v-tooltip.bottom="t('schema.transform')"
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
    <CodeEditor v-model:value="code" language="sql" theme="vs-white" :options="editorOptions" />
  </div>
  <TransformDiagramDialog
    v-model:visible="showTransform"
    source-type="physical"
    @transform="handleTransform"
  />
</template>

<style scoped></style>
