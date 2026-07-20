<script setup lang="ts">
import { useToast } from 'primevue'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import { useDomainStore } from '@/stores/domainStore'
import type { Attribute, Entity, Relationship } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { erSchemaStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddAttribute,
  DialogId.EditAttribute,
)
const toast = useToast()

const domainStore = useDomainStore()

const attributeName = ref('')
const selectedParentId = ref<string | null>(null)
const isKey = ref(false)
const isMultivalued = ref(false)
const isComposite = ref(false)
const isNotNull = ref(false)
const isUnique = ref(false)
const isDerived = ref(false)
const size = ref(20)
const selectedDomain = ref<string | null>(null)

const domains = computed(() => domainStore.domains)

const currentAttribute = computed(() => {
  if (!isEditMode.value) return null
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.attributes.find((a: Attribute) => a.id === id) || null
})

const parentOptions = computed(() => {
  return [
    ...erSchemaStore.entities.map((e: Entity) => ({
      id: e.id,
      name: `${e.name} (Entity)`,
      position: e.position,
    })),
    ...erSchemaStore.relationships.map((r: Relationship) => ({
      id: r.id,
      name: `${r.name} (Relationship)`,
      position: r.position,
    })),
    ...erSchemaStore.attributes.map((a: Attribute) => ({
      id: a.id,
      name: `${a.name} (Attribute)`,
      position: a.position,
    })),
  ].sort((a, b) => a.name.localeCompare(b.name))
})

const parentIsRelationship = computed(() =>
  erSchemaStore.relationships.some((r: Relationship) => r.id === selectedParentId.value),
)

watch(parentIsRelationship, (isRel) => {
  if (isRel) isKey.value = false
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentAttribute.value) {
      const attr = currentAttribute.value
      attributeName.value = attr.name
      selectedParentId.value = attr.parentId
      isKey.value = !!attr.isKey
      isMultivalued.value = !!attr.isMultivalued
      isComposite.value = !!attr.isComposite
      isNotNull.value = !!attr.isNotNull
      isUnique.value = !!attr.isUnique
      isDerived.value = !!attr.isDerived
      size.value = attr.size ?? 20
      selectedDomain.value = attr.domain || null
    } else {
      const selectedId = erSchemaStore.selectedElementId
      if (selectedId) {
        const isEntity = erSchemaStore.entities.some((e: Entity) => e.id === selectedId)
        const isRel = erSchemaStore.relationships.some((r: Relationship) => r.id === selectedId)
        const isAttr = erSchemaStore.attributes.some((a: Attribute) => a.id === selectedId)
        if (isEntity || isRel || isAttr) {
          selectedParentId.value = selectedId
        }
      } else {
        selectedParentId.value = null
      }
      attributeName.value = ''
      isKey.value = false
      isMultivalued.value = false
      isComposite.value = false
      isNotNull.value = true
      isUnique.value = false
      size.value = 20
      selectedDomain.value = null
    }
  }
})

const saveAttribute = () => {
  if (selectedParentId.value && attributeName.value.trim()) {
    const trimmed = attributeName.value.trim()
    const currentId = isEditMode.value ? erSchemaStore.selectedElementId : null
    const duplicate = erSchemaStore.attributes.some(
      (a: Attribute) =>
        a.name === trimmed && a.parentId === selectedParentId.value && a.id !== currentId,
    )
    if (duplicate) {
      toast.add({ severity: 'error', detail: t('attribute.nameAlreadyExists'), life: 3000 })
      return
    }
    erSchemaStore.saveAttribute(
      {
        name: trimmed,
        parentId: selectedParentId.value,
        isKey: isKey.value,
        isMultivalued: isMultivalued.value,
        isComposite: isComposite.value,
        isNotNull: isNotNull.value,
        isUnique: isUnique.value,
        isDerived: isDerived.value,
        size: size.value,
        domain: selectedDomain.value || undefined,
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
    :style="{ width: '30rem' }"
    :header="isEditMode ? t('attribute.editAttribute') : t('attribute.addAttribute')"
  >
    <div class="flex flex-col gap-3">
      <label for="attributeName" class="font-semibold">{{ t('attribute.attributeName') }}</label>
      <InputText
        id="attributeName"
        v-model="attributeName"
        :placeholder="t('attribute.enterAttributeName')"
        autofocus
        @keydown.enter="saveAttribute"
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
        v-model="selectedDomain"
        :options="domains"
        optionLabel="name"
        optionValue="name"
        :placeholder="t('attribute.selectDomain')"
        show-clear
      />

      <label for="size" class="font-semibold mt-2">{{ t('attribute.size') }}</label>
      <InputNumber id="size" v-model="size" />

      <div class="grid grid-cols-2 gap-2 mt-2">
        <div class="flex items-center gap-2">
          <Checkbox
            v-model="isKey"
            :binary="true"
            inputId="isKey"
            :disabled="parentIsRelationship"
          />
          <label for="isKey" :class="parentIsRelationship ? 'opacity-40' : ''">
            {{ t('attribute.primaryKey') }}
          </label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isMultivalued" :binary="true" inputId="isMultivalued" />
          <label for="isMultivalued">{{ t('attribute.multivalued') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isComposite" :binary="true" inputId="isComposite" />
          <label for="isComposite">{{ t('attribute.composite') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isNotNull" :binary="true" inputId="isNotNull" />
          <label for="isNotNull">{{ t('attribute.notNull') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isUnique" :binary="true" inputId="isUnique" />
          <label for="isUnique">{{ t('attribute.unique') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox v-model="isDerived" :binary="true" inputId="isDerived" />
          <label for="isDerived">{{ t('attribute.derived') }}</label>
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
      <Button
        :label="isEditMode ? t('common.confirm') : t('common.add')"
        icon="bi bi-check-lg"
        @click="saveAttribute"
      />
    </template>
  </Dialog>
</template>

<style scoped></style>
