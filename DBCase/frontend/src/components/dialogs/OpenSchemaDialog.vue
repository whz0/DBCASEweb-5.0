<script setup lang="ts">
import type { FileUploadSelectEvent } from 'primevue/fileupload'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { DialogId, useDialogStore } from '@/stores/dialogStore'

const { t } = useI18n()
const dialogStore = useDialogStore()

const selectedFile = ref<File | null>(null)

const visible = computed(() => dialogStore.isOpen(DialogId.OpenSchema))
const closeModal = () => {
  dialogStore.close(DialogId.OpenSchema)
  selectedFile.value = null
}

const onFileSelect = (event: FileUploadSelectEvent) => {
  if (event.files && event.files.length > 0) {
    selectedFile.value = event.files[0]
  } else {
    selectedFile.value = null
  }
}

const openSchema = () => {
  if (selectedFile.value) {
    console.log('Opening file:', selectedFile.value.name)
    closeModal()
  }
}
</script>

<template>
  <Dialog
    :header="t('toolbar.openSchema')"
    :visible="visible"
    @update:visible="closeModal"
    :dismissable-mask="true"
    :draggable="false"
    modal
    :style="{ width: '30rem' }"
  >
    <div class="flex flex-col gap-3">
      <label for="fileUpload">{{ t('schema.selectFile') }}</label>
      <FileUpload
        mode="basic"
        name="schemaFile[]"
        accept=".dbw"
        :maxFileSize="1000000"
        @select="onFileSelect"
        :chooseLabel="t('common.chooseFile')"
        :cancelLabel="t('common.cancel')"
      />
      <div v-if="selectedFile">{{ t('schema.selectedFile') }}: {{ selectedFile.name }}</div>
    </div>
    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button
        :label="t('schema.open')"
        :disabled="!selectedFile"
        icon="bi bi-check-lg"
        @click="openSchema"
      />
    </template>
  </Dialog>
</template>
