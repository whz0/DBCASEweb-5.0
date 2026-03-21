<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import type { Entity } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { diagramStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddIsARelationship,
  DialogId.EditRelationship,
)

const selectedParent = ref<Entity | null>(null)
const selectedChildren = ref<Entity[]>([])

const entities = computed(() => diagramStore.entities)

const currentRelationship = computed(() => {
  if (!isEditMode.value) return null
  const id = diagramStore.selectedElementId
  return diagramStore.relationships.find((r) => r.id === id) || null
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentRelationship.value) {
      const rel = currentRelationship.value
      const parentPart = rel.participants.find((p) => p.role === 'Parent')
      const childParts = rel.participants.filter((p) => p.role === 'Child')

      if (parentPart) {
        selectedParent.value =
          diagramStore.entities.find((e) => e.id === parentPart.entityId) || null
      }
      selectedChildren.value = childParts
        .map((p) => diagramStore.entities.find((e) => e.id === p.entityId)!)
        .filter(Boolean)
    } else {
      const selectedId = diagramStore.selectedElementId
      if (selectedId) {
        const entity = diagramStore.entities.find((e) => e.id === selectedId)
        if (entity) {
          selectedParent.value = entity
        }
      } else {
        selectedParent.value = null
      }
      selectedChildren.value = []
    }
  }
})

const saveIsA = () => {
  if (selectedParent.value && selectedChildren.value.length > 0) {
    diagramStore.saveIsARelationship(
      {
        parent: selectedParent.value,
        children: selectedChildren.value,
      },
      isEditMode.value,
    )
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
    :style="{ width: '30rem' }"
    :header="isEditMode ? t('relationship.editRelationship') : t('panels.insertIsARelationship')"
  >
    <div class="flex flex-col gap-3">
      <label for="parent" class="font-semibold"
        >{{ t('attribute.parent') }} ({{ t('isa.parent') }})</label
      >
      <Select
        id="parent"
        v-model="selectedParent"
        :options="entities"
        optionLabel="name"
        :placeholder="t('entity.selectStrongEntity')"
      />

      <label for="children" class="font-semibold mt-2">{{ t('isa.childEntities') }}</label>
      <MultiSelect
        id="children"
        v-model="selectedChildren"
        :options="entities.filter((e) => e.id !== selectedParent?.id)"
        optionLabel="name"
        :placeholder="t('isa.selectChildren')"
        display="chip"
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
        :label="isEditMode ? t('common.confirm') : t('common.add')"
        icon="bi bi-check-lg"
        @click="saveIsA"
      />
    </template>
  </Dialog>
</template>

<style scoped></style>
