<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import type { Entity } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { diagramStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddEntity,
  DialogId.EditEntity,
)

const entityName = ref('')
const isWeakEntity = ref(false)
const relationName = ref('')
const selectedStrongEntity = ref<Entity | null>(null)

const currentEntity = computed(() => {
  if (!isEditMode.value) return null
  const id = diagramStore.selectedElementId
  return diagramStore.entities.find((e) => e.id === id) || null
})

const strongEntities = computed(() =>
  diagramStore.entities.filter((e) => e.id !== currentEntity.value?.id),
)

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentEntity.value) {
      entityName.value = currentEntity.value.name
      isWeakEntity.value = !!currentEntity.value.isWeak

      if (isWeakEntity.value) {
        const idRel = diagramStore.relationships.find(
          (r) =>
            r.type === 'Weak' && r.participants.some((p) => p.entityId === currentEntity.value?.id),
        )
        if (idRel) {
          relationName.value = idRel.name
          const strongParticipant = idRel.participants.find(
            (p) => p.entityId !== currentEntity.value?.id,
          )
          if (strongParticipant) {
            selectedStrongEntity.value =
              diagramStore.entities.find((e) => e.id === strongParticipant.entityId) || null
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

  diagramStore.saveEntity(
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
    :draggable="false"
    :style="{ width: '30rem' }"
    :header="isEditMode ? t('entity.editEntity') : t('entity.addEntity')"
  >
    <div class="flex flex-col gap-3">
      <label for="entityName" class="font-semibold">{{ t('entity.entityName') }}</label>
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

        <label for="strongEntity" class="font-semibold mt-2">{{ t('entity.strongEntity') }}</label>
        <Select
          id="strongEntity"
          v-model="selectedStrongEntity"
          :options="strongEntities"
          optionLabel="name"
          :placeholder="t('entity.selectStrongEntity')"
        />
      </template>
    </div>

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
