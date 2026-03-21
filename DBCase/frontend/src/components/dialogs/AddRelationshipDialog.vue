<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'

const { t } = useI18n()
const { diagramStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddRelationship,
  DialogId.EditRelationship,
)

const name = ref('')

const currentRelationship = computed(() => {
  if (!isEditMode.value) return null
  const id = diagramStore.selectedElementId
  return diagramStore.relationships.find((r) => r.id === id) || null
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentRelationship.value) {
      name.value = currentRelationship.value.name
    } else {
      name.value = ''
    }
  }
})

const saveRelationship = () => {
  if (!name.value.trim()) return

  diagramStore.saveRelationship({ name: name.value.trim() }, isEditMode.value)
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
    :style="{ width: '30rem' }"
    :header="isEditMode ? t('relationship.editRelationship') : t('relationship.addRelationship')"
  >
    <div class="flex flex-col gap-3">
      <label for="name" class="font-semibold">{{ t('common.name') }}</label>
      <InputText
        id="name"
        v-model="name"
        :placeholder="t('relationship.enterRelationshipName')"
        autofocus
        @keyup.enter="saveRelationship"
      />
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button
        :label="isEditMode ? t('common.confirm') : t('relationship.addRelationship')"
        icon="bi bi-check-lg"
        @click="saveRelationship"
      />
    </template>
  </Dialog>
</template>

<style scoped></style>
