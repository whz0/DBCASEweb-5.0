<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import type { Entity, Relationship, RelationshipParticipant } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { erSchemaStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddIsARelationship,
  DialogId.EditRelationship,
)

const selectedParent = ref<Entity | null>(null)
const selectedChildren = ref<Entity[]>([])

const entities = computed(() => erSchemaStore.entities)

const currentRelationship = computed(() => {
  if (!isEditMode.value) return null
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.relationships.find((r: Relationship) => r.id === id) || null
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentRelationship.value) {
      const rel = currentRelationship.value
      const parentPart = rel.participants.find((p: RelationshipParticipant) => p.role === 'Parent')
      const childParts = rel.participants.filter((p: RelationshipParticipant) => p.role === 'Child')

      if (parentPart) {
        selectedParent.value =
          erSchemaStore.entities.find((e: Entity) => e.id === parentPart.entityId) || null
      }
      selectedChildren.value = childParts
        .map(
          (p: RelationshipParticipant) =>
            erSchemaStore.entities.find((e: Entity) => e.id === p.entityId)!,
        )
        .filter(Boolean)
    } else {
      const selectedId = erSchemaStore.selectedElementId
      if (selectedId) {
        const entity = erSchemaStore.entities.find((e: Entity) => e.id === selectedId)
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
    erSchemaStore.saveIsARelationship(
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
        :options="entities.filter((e: Entity) => e.id !== selectedParent?.id)"
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
