<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/diagramStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const newName = ref('')

const visible = computed(() => dialogStore.isOpen(DialogId.Rename))

const currentElement = computed(() => {
  const id = diagramStore.selectedElementId
  if (!id) return null
  return diagramStore.entities.find(e => e.id === id) ||
         diagramStore.relationships.find(r => r.id === id) ||
         diagramStore.attributes.find(a => a.id === id)
})

watch(visible, (isNowVisible) => {
  if (isNowVisible && currentElement.value) {
    newName.value = currentElement.value.name
  }
})

const closeModal = () => {
  dialogStore.close(DialogId.Rename)
  newName.value = ''
}

const confirmRename = () => {
  const id = diagramStore.selectedElementId
  if (id && newName.value.trim()) {
    diagramStore.renameElement(id, newName.value.trim())
  }
  closeModal()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :draggable="false"
    :style="{ width: '25rem' }"
    :header="t('common.rename')"
  >
    <div class="flex flex-col gap-3">
      <label for="newName" class="font-semibold">{{ t('common.rename') }}</label>
      <InputText id="newName" v-model="newName" autofocus @keyup.enter="confirmRename" />
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button :label="t('common.confirm')" icon="bi bi-check-lg" @click="confirmRename" />
    </template>
  </Dialog>
</template>
