<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/erSchemaStore.ts'
import type { Entity } from '@/types/er-diagram-elements'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const selectedEntity = ref<Entity | null>(null)
const cardinalityMin = ref('0')
const cardinalityMax = ref('n')
const role = ref('')

const entities = computed(() => {
  const selectedRelationshipId = diagramStore.selectedElementId
  if (!selectedRelationshipId) return []
  const relationship = diagramStore.relationships.find((r) => r.id === selectedRelationshipId)
  if (!relationship) return []

  // Filter out entities that already participate
  return diagramStore.entities.filter(
    (e) => !relationship.participants.some((p) => p.entityId === e.id),
  )
})

const visible = computed(() => dialogStore.isOpen(DialogId.AddEntityToRelationship))

const currentRelationship = computed(() => {
  const id = diagramStore.selectedElementId
  return diagramStore.relationships.find((r) => r.id === id) || null
})

const participantsWithNames = computed(() => {
  if (!currentRelationship.value) return []
  return currentRelationship.value.participants.map((p) => ({
    ...p,
    name: diagramStore.entities.find((e) => e.id === p.entityId)?.name || 'Unknown',
  }))
})

const closeModal = () => {
  dialogStore.close(DialogId.AddEntityToRelationship)
  selectedEntity.value = null
  cardinalityMin.value = '0'
  cardinalityMax.value = 'n'
  role.value = ''
}

const addParticipant = () => {
  const selectedRelationshipId = diagramStore.selectedElementId
  if (selectedRelationshipId && selectedEntity.value) {
    diagramStore.addParticipantToRelationship(selectedRelationshipId, {
      entityId: selectedEntity.value.id,
      cardinalityMin: cardinalityMin.value,
      cardinalityMax: cardinalityMax.value,
      role: role.value,
    })
  }
  closeModal()
}

interface CardinalityOption {
  label: string
  min: string
  max: string
}

const cardinalityOptions: CardinalityOption[] = [
  { label: '0,1', min: '0', max: '1' },
  { label: '1,1', min: '1', max: '1' },
  { label: '0,n', min: '0', max: 'n' },
  { label: '1,n', min: '1', max: 'n' },
  { label: t('relationship.custom'), min: '', max: '' },
]

const selectedCardinalityOption = ref(cardinalityOptions[2])

const onCardinalityChange = (e: { value: CardinalityOption }) => {
  if (e.value.label !== 'Custom') {
    cardinalityMin.value = e.value.min
    cardinalityMax.value = e.value.max
  }
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
    :header="t('relationship.addRelationship')"
  >
    <div class="flex flex-col gap-3">
      <div v-if="currentRelationship" class="mb-2">
        <span class="font-bold text-lg"
          >{{ t('relationship.addRelationship') }}: {{ currentRelationship.name }}</span
        >
      </div>

      <label for="entity" class="font-semibold">{{ t('relationship.selectEntity') }}</label>
      <Select
        id="entity"
        v-model="selectedEntity"
        :options="entities"
        optionLabel="name"
        :placeholder="t('relationship.selectEntity')"
      />

      <label class="font-semibold mt-2">{{ t('relationship.cardinality') }}</label>
      <Select
        v-model="selectedCardinalityOption"
        :options="cardinalityOptions"
        optionLabel="label"
        @change="onCardinalityChange"
      />

      <div v-if="selectedCardinalityOption?.label === t('relationship.custom')" class="flex gap-2">
        <div class="flex flex-col flex-1">
          <label for="min" class="text-sm">{{ t('relationship.min') }}</label>
          <InputText id="min" v-model="cardinalityMin" />
        </div>
        <div class="flex flex-col flex-1">
          <label for="max" class="text-sm">{{ t('relationship.max') }}</label>
          <InputText id="max" v-model="cardinalityMax" />
        </div>
      </div>

      <label for="role" class="font-semibold mt-2">{{ t('relationship.role') }}</label>
      <InputText id="role" v-model="role" :placeholder="t('relationship.enterRole')" />

      <div v-if="participantsWithNames.length > 0" class="mt-4">
        <h3 class="font-bold mb-2">{{ t('relationship.currentParticipants') }}</h3>
        <ul class="max-h-40 overflow-y-auto border rounded p-2">
          <li
            v-for="p in participantsWithNames"
            :key="p.entityId"
            class="flex justify-between items-center py-1 border-b last:border-0"
          >
            <span class="text-sm">
              <span class="font-semibold">{{ p.name }}</span>
              ({{ p.cardinalityMin }},{{ p.cardinalityMax }})
              <span v-if="p.role" class="italic text-gray-500">- {{ p.role }}</span>
            </span>
            <Button
              icon="bi bi-trash"
              severity="danger"
              text
              @click="
                diagramStore.removeParticipantFromRelationship(currentRelationship!.id, p.entityId)
              "
            />
          </li>
        </ul>
      </div>
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button :label="t('common.add')" icon="bi bi-check-lg" @click="addParticipant" />
    </template>
  </Dialog>
</template>
