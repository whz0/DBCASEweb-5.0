<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import FileUpload from 'primevue/fileupload'

const { t } = useI18n()
const visible = ref(false)
const selectedFile = ref<File | null>(null)

const onFileSelect = (event: any) => {
  if (event.files && event.files.length > 0) {
    selectedFile.value = event.files[0]
  } else {
    selectedFile.value = null
  }
}

const openSchema = () => {
  if (selectedFile.value) {
    console.log('Opening file:', selectedFile.value.name)
    visible.value = false
    selectedFile.value = null
  }
}

defineExpose({
  visible,
})
</script>

<template>
  <Dialog
    :dismissable-mask="true"
    :header="t('toolbar.openSchema')"
    :draggable="false"
    v-model:visible="visible"
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
      <div v-if="selectedFile">
        {{ t('schema.selectedFile') }}: {{ selectedFile.name }}
      </div>
    </div>
    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="
          visible = false;
          selectedFile = null;
        "
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
