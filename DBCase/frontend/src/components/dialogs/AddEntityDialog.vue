<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import type { Entity, Relationship, RelationshipParticipant } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { erSchemaStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddEntity,
  DialogId.EditEntity,
)

const entityName = ref('')
const isWeakEntity = ref(false)
const relationName = ref('')
const selectedStrongEntity = ref<Entity | null>(null)

const currentEntity = computed(() => {
  if (!isEditMode.value) return null
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.entities.find((e: Entity) => e.id === id) || null
})

const strongEntities = computed(() =>
  erSchemaStore.entities.filter((e: Entity) => e.id !== currentEntity.value?.id),
)

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentEntity.value) {
      entityName.value = currentEntity.value.name
      isWeakEntity.value = !!currentEntity.value.isWeak

      if (isWeakEntity.value) {
        const idRel = erSchemaStore.relationships.find(
          (r: Relationship) =>
            r.type === 'Weak' &&
            r.participants.some(
              (p: RelationshipParticipant) => p.entityId === currentEntity.value?.id,
            ),
        )
        if (idRel) {
          relationName.value = idRel.name
          const strongParticipant = idRel.participants.find(
            (p: RelationshipParticipant) => p.entityId !== currentEntity.value?.id,
          )
          if (strongParticipant) {
            selectedStrongEntity.value =
              erSchemaStore.entities.find((e: Entity) => e.id === strongParticipant.entityId) ||
              null
          }
        }
      }
    } else {
      entityName.value = ''
      isWeakEntity.value = false
      relationName.value = ''
      selectedStrongEntity.value = null
    }
  }
})

const saveEntity = () => {
  if (!entityName.value.trim()) return

  erSchemaStore.saveEntity(
    { name: entityName.value.trim(), isWeak: isWeakEntity.value },
    isEditMode.value,
    selectedStrongEntity.value,
    relationName.value,
  )

  closeModal()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :style="{ width: '30rem' }"
    :header="isEditMode ? t('entity.editEntity') : t('entity.addEntity')"
  >
    <form @submit.prevent="saveEntity">
      <div class="flex flex-col gap-3">
        <InputText
          id="entityName"
          v-model="entityName"
          :placeholder="t('entity.enterEntityName')"
          autofocus
        />

        <div class="flex items-center gap-2 mt-2">
          <Checkbox v-model="isWeakEntity" inputId="isWeak" :binary="true" />
          <label for="isWeak">{{ t('entity.isWeakEntity') }}</label>
        </div>

        <template v-if="isWeakEntity">
          <label for="relationName" class="font-semibold mt-2">{{
            t('entity.weakRelationshipName')
          }}</label>
          <InputText
            id="relationName"
            v-model="relationName"
            :placeholder="t('entity.enterRelationName')"
          />

          <label for="strongEntity" class="font-semibold mt-2">{{
            t('entity.strongEntity')
          }}</label>
          <Select
            id="strongEntity"
            v-model="selectedStrongEntity"
            :options="strongEntities"
            optionLabel="name"
            :placeholder="t('entity.selectStrongEntity')"
          />
        </template>
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
        :label="isEditMode ? t('common.confirm') : t('entity.addEntity')"
        icon="bi bi-check-lg"
        @click="saveEntity"
      />
    </template>
  </Dialog>
</template>

<style scoped></style>
