<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const { t } = useI18n()
const dialogStore = useDialogStore()
const filename = ref('')

const visible = computed(() => dialogStore.isOpen(DialogId.SaveSchema))
const closeModal = () => {
  dialogStore.close(DialogId.SaveSchema)
  filename.value = ''
}

const saveSchema = () => {
  if (filename.value.trim()) {
    console.log(filename.value)
    closeModal()
  }
}
</script>

<template>
  <Dialog
    :header="t('toolbar.saveSchema')"
    :visible="visible"
    @update:visible="closeModal"
    :dismissable-mask="true"
    :draggable="false"
    modal
    :style="{ width: '30rem' }"
  >
    <form @submit.prevent="saveSchema">
      <div class="flex flex-col gap-3">
        <label for="filename">{{ t('schema.schemaName') }}</label>
        <InputText
          id="filename"
          v-model="filename"
          :placeholder="t('schema.enterSchemaName')"
          autofocus
        />
      </div>
    </form>
    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button
        :label="t('schema.save')"
        :disabled="!filename.trim()"
        icon="bi bi-check-lg"
        @click="saveSchema"
      />
    </template>
  </Dialog>
</template>
