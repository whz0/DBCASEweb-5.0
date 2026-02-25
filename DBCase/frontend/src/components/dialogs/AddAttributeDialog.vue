<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/diagramStore'
import type { Entity } from '@/types/er-diagram-elements'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const attributeName = ref('')
const selectedEntity = ref<Entity | null>(null)

const entities = computed(() => diagramStore.entities)

const visible = computed(() => dialogStore.isOpen(DialogId.AddAttribute))
const closeModal = () => {
  dialogStore.close(DialogId.AddAttribute)
  attributeName.value = ''
  selectedEntity.value = null
}

const addAttribute = () => {
  if (selectedEntity.value) {
    const newAttribute = {
      id: crypto.randomUUID(),
      name: attributeName.value,
      position: { x: selectedEntity.value.position.x - 100, y: selectedEntity.value.position.y - 50 },
      isKey: false,
      isMultivalued: false,
      isDerived: false,
      parentId: selectedEntity.value.id
    }
    diagramStore.addAttribute(newAttribute)
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
    :header="t('attribute.addAttribute')"
  >
    <div class="flex flex-col gap-3">
      <label for="attributeName" class="font-semibold">{{ t('attribute.attributeName') }}</label>
      <InputText
        id="attributeName"
        v-model="attributeName"
        :placeholder="t('attribute.enterAttributeName')"
        autofocus
      />

      <label for="entity" class="font-semibold mt-2">{{ t('attribute.parentEntity') }}</label>
      <Select
        id="entity"
        v-model="selectedEntity"
        :options="entities"
        optionLabel="name"
        :placeholder="t('attribute.selectParentEntity')"
      />
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button :label="t('common.add')" icon="bi bi-check-lg" @click="addAttribute" />
    </template>
  </Dialog>
</template>

<style scoped></style>
