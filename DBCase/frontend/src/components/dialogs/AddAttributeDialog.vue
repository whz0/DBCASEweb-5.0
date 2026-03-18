<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/diagramStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const attributeName = ref('')
const selectedParentId = ref<string | null>(null)
const isKey = ref(false)
const isMultivalued = ref(false)
const isDerived = ref(false)
const isComposite = ref(false)
const selectedDomainId = ref<string | null>(null)

const domains = computed(() => diagramStore.domains)

const parentOptions = computed(() => {
  return [
    ...diagramStore.entities.map(e => ({ id: e.id, name: `${e.name} (Entity)`, position: e.position })),
    ...diagramStore.relationships.map(r => ({ id: r.id, name: `${r.name} (Relationship)`, position: r.position })),
    ...diagramStore.attributes.map(a => ({ id: a.id, name: `${a.name} (Attribute)`, position: a.position }))
  ]
})

const visible = computed(() => dialogStore.isOpen(DialogId.AddAttribute))

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    const selectedId = diagramStore.selectedElementId
    if (selectedId) {
      const isEntity = diagramStore.entities.some(e => e.id === selectedId)
      const isRel = diagramStore.relationships.some(r => r.id === selectedId)
      const isAttr = diagramStore.attributes.some(a => a.id === selectedId)
      if (isEntity || isRel || isAttr) {
        selectedParentId.value = selectedId
      }
    }
  }
})

const closeModal = () => {
  dialogStore.close(DialogId.AddAttribute)
  attributeName.value = ''
  selectedParentId.value = null
  isKey.value = false
  isMultivalued.value = false
  isDerived.value = false
  isComposite.value = false
  selectedDomainId.value = null
}

const addAttribute = () => {
  if (selectedParentId.value) {
    const parent = parentOptions.value.find(p => p.id === selectedParentId.value)
    const newAttribute = {
      id: crypto.randomUUID(),
      name: attributeName.value,
      position: { 
        x: (parent?.position.x ?? 0) - 100, 
        y: (parent?.position.y ?? 0) - 50 
      },
      isKey: isKey.value,
      isMultivalued: isMultivalued.value,
      isDerived: isDerived.value,
      isComposite: isComposite.value,
      domainId: selectedDomainId.value || undefined,
      parentId: selectedParentId.value
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

      <label for="parent" class="font-semibold mt-2">{{ t('attribute.parent') }}</label>
      <Select
        id="parent"
        v-model="selectedParentId"
        :options="parentOptions"
        optionLabel="name"
        optionValue="id"
        :placeholder="t('attribute.selectParent')"
      />

      <label for="domain" class="font-semibold mt-2">{{ t('attribute.domain') }}</label>
      <Select
        id="domain"
        v-model="selectedDomainId"
        :options="domains"
        optionLabel="name"
        optionValue="id"
        :placeholder="t('attribute.selectDomain')"
        show-clear
      />

      <div class="flex flex-col gap-2 mt-2">
        <div class="flex items-center gap-2">
          <Checkbox v-model="isKey" :binary="true" inputId="isKey" />
          <label for="isKey">{{ t('attribute.primaryKey') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isMultivalued" :binary="true" inputId="isMultivalued" />
          <label for="isMultivalued">{{ t('attribute.multivalued') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isDerived" :binary="true" inputId="isDerived" />
          <label for="isDerived">{{ t('attribute.derived') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isComposite" :binary="true" inputId="isComposite" />
          <label for="isComposite">{{ t('attribute.composite') }}</label>
        </div>
      </div>
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
