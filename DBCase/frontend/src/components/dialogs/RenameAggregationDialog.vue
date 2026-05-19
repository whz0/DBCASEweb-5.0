<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const erSchemaStore = useErSchemaStore()

const visible = computed(() => dialogStore.isOpen(DialogId.RenameAggregation))

const currentRel = computed(() => {
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.relationships.find((r) => r.id === id) ?? null
})

const name = ref('')

watch(visible, (v) => {
  if (v) name.value = currentRel.value?.aggregationName ?? ''
})

const closeModal = () => dialogStore.close(DialogId.RenameAggregation)

const save = () => {
  if (!name.value.trim() || !currentRel.value) return
  if (currentRel.value.type !== 'Aggregation') {
    erSchemaStore.toggleAggregation(currentRel.value.id)
  }
  erSchemaStore.renameAggregation(currentRel.value.id, name.value.trim())
  closeModal()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :style="{ width: '26rem' }"
    :header="t('relationship.aggregationName')"
  >
    <form @submit.prevent="save">
      <InputText
        v-model="name"
        :placeholder="t('relationship.enterAggregationName')"
        autofocus
        class="w-full"
      />
    </form>
    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button
        :label="t('common.confirm')"
        icon="bi bi-check-lg"
        :disabled="!name.trim()"
        @click="save"
      />
    </template>
  </Dialog>
</template>
