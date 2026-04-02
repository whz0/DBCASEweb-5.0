<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import type { Attribute, Entity, Relationship } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { erSchemaStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddAttribute,
  DialogId.EditAttribute,
)

const attributeName = ref('')
const selectedParentId = ref<string | null>(null)
const isKey = ref(false)
const isMultivalued = ref(false)
const isComposite = ref(false)
const isNotNull = ref(false)
const isUnique = ref(false)
const size = ref(20)
const selectedDomainId = ref<string | null>(null)

const domains = computed(() => erSchemaStore.domains)

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
  ]
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
      size.value = attr.size ?? 20
      selectedDomainId.value = attr.domainId || null
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
      isNotNull.value = false
      isUnique.value = false
      size.value = 20
      selectedDomainId.value = null
    }
  }
})

const saveAttribute = () => {
  if (selectedParentId.value && attributeName.value.trim()) {
    erSchemaStore.saveAttribute(
      {
        name: attributeName.value.trim(),
        parentId: selectedParentId.value,
        isKey: isKey.value,
        isMultivalued: isMultivalued.value,
        isComposite: isComposite.value,
        isNotNull: isNotNull.value,
        isUnique: isUnique.value,
        size: size.value,
        domainId: selectedDomainId.value || undefined,
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
    :header="isEditMode ? t('attribute.editAttribute') : t('attribute.addAttribute')"
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

      <label for="size" class="font-semibold mt-2">{{ t('attribute.size') }}</label>
      <InputNumber id="size" v-model="size" />

      <div class="grid grid-cols-2 gap-2 mt-2">
        <div class="flex items-center gap-2">
          <Checkbox v-model="isKey" :binary="true" inputId="isKey" />
          <label for="isKey">{{ t('attribute.primaryKey') }}</label>
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
