<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/diagramStore'
import type { Entity } from '@/types/er-diagram-elements'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const selectedParent = ref<Entity | null>(null)
const selectedChildren = ref<Entity[]>([])

const entities = computed(() => diagramStore.entities)
const visible = computed(() => dialogStore.isOpen(DialogId.AddIsARelationship))

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    const selectedId = diagramStore.selectedElementId
    if (selectedId) {
      const entity = diagramStore.entities.find(e => e.id === selectedId)
      if (entity) {
        selectedParent.value = entity
      }
    }
  }
})

const closeModal = () => {
  dialogStore.close(DialogId.AddIsARelationship)
  selectedParent.value = null
  selectedChildren.value = []
}

const addIsA = () => {
  if (selectedParent.value && selectedChildren.value.length > 0) {
    const newIsA = {
      id: crypto.randomUUID(),
      name: 'IsA',
      position: { ...diagramStore.lastClickPosition },
      type: 'IsA' as const,
      participants: [
        { entityId: selectedParent.value.id, cardinalityMin: '', cardinalityMax: '', role: 'Parent' },
        ...selectedChildren.value.map(c => ({
          entityId: c.id, cardinalityMin: '', cardinalityMax: '', role: 'Child'
        }))
      ],
      attributes: []
    }
    diagramStore.addRelationship(newIsA)
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
    :header="t('panels.insertIsARelationship')"
  >
    <div class="flex flex-col gap-3">
      <label for="parent" class="font-semibold">{{ t('attribute.parent') }} (Parent)</label>
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
        :options="entities.filter(e => e.id !== selectedParent?.id)"
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
      <Button :label="t('common.add')" icon="bi bi-check-lg" @click="addIsA" />
    </template>
  </Dialog>
</template>
