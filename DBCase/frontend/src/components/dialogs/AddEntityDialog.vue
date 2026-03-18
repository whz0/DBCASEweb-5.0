<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/diagramStore'
import type { Entity } from '@/types/er-diagram-elements'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const entityName = ref('')
const isWeakEntity = ref(false)
const relationName = ref('')
const selectedStrongEntity = ref<Entity | null>(null)

const strongEntities = computed(() => diagramStore.entities)

const visible = computed(() => dialogStore.isOpen(DialogId.AddEntity))
const closeModal = () => {
  dialogStore.close(DialogId.AddEntity)
  // Reset form
  entityName.value = ''
  isWeakEntity.value = false
  relationName.value = ''
  selectedStrongEntity.value = null
}

const addEntity = () => {
  const newEntity: Entity = {
    id: crypto.randomUUID(),
    name: entityName.value,
    position: { x: Math.random() * 400, y: Math.random() * 400 }, // Random position for now
    isWeak: isWeakEntity.value,
    attributes: [],
    primaryKeys: []
  }
  diagramStore.addEntity(newEntity)

  if (isWeakEntity.value && selectedStrongEntity.value) {
    const newRelationship = {
      id: crypto.randomUUID(),
      name: relationName.value,
      position: { x: newEntity.position.x + 150, y: newEntity.position.y },
      type: 'Weak' as const, 
      participants: [
        { entityId: newEntity.id, cardinalityMin: '1', cardinalityMax: '1' },
        { entityId: selectedStrongEntity.value.id, cardinalityMin: '1', cardinalityMax: 'N' }
      ],
      attributes: []
    }
    diagramStore.addRelationship(newRelationship)
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
    :header="t('entity.addEntity')"
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
      <Button :label="t('entity.addEntity')" icon="bi bi-check-lg" @click="addEntity" />
    </template>
  </Dialog>
</template>

<style scoped></style>
