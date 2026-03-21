<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'

const { t } = useI18n()
const { diagramStore, dialogStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddDomain,
  DialogId.EditDomain,
)

const domainName = ref('')
const baseType = ref('INTEGER')
const values = ref('')

const currentDomain = computed(() => {
  if (!isEditMode.value) return null
  const id = diagramStore.selectedElementId
  return diagramStore.domains.find((d) => d.id === id) || null
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    if (isEditMode.value && currentDomain.value) {
      domainName.value = currentDomain.value.name
      baseType.value = currentDomain.value.baseType
      values.value = currentDomain.value.values?.join(', ') || ''
    } else {
      domainName.value = ''
      baseType.value = 'INTEGER'
      values.value = ''
    }
  }
})

const baseTypeOptions = [
  { label: 'BIT', value: 'BIT' },
  { label: 'BLOB', value: 'BLOB' },
  { label: 'CHAR', value: 'CHAR' },
  { label: 'DATE', value: 'DATE' },
  { label: 'DATETIME', value: 'DATETIME' },
  { label: 'DECIMAL', value: 'DECIMAL' },
  { label: 'FLOAT', value: 'FLOAT' },
  { label: 'GEOMETRY', value: 'GEOMETRY' },
  { label: 'INTEGER', value: 'INTEGER' },
  { label: 'TEXT', value: 'TEXT' },
  { label: 'TIME', value: 'TIME' },
  { label: 'VARCHAR', value: 'VARCHAR' },
]

const saveDomain = () => {
  if (!domainName.value.trim()) return

  diagramStore.saveDomain(
    {
      name: domainName.value.trim(),
      baseType: baseType.value,
      values: values.value.trim() ? values.value.split(',').map((v) => v.trim()) : undefined,
    },
    isEditMode.value,
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
    :style="{ width: '25rem' }"
    :header="isEditMode ? t('domain.editDomain') : t('panels.createDomain')"
  >
    <div class="flex flex-col gap-3">
      <label for="domainName" class="font-semibold">{{ t('domain.name') }}</label>
      <InputText id="domainName" v-model="domainName" autofocus @keyup.enter="saveDomain" />

      <label for="baseType" class="font-semibold mt-2">{{ t('domain.baseType') }}</label>
      <Select
        id="baseType"
        v-model="baseType"
        :options="baseTypeOptions"
        optionLabel="label"
        optionValue="value"
      />

      <label for="values" class="font-semibold mt-2">{{ t('domain.allowedValues') }}</label>
      <InputText id="values" v-model="values" :placeholder="t('domain.enterValues')" />

      <div v-if="diagramStore.domains.length > 0 && !isEditMode" class="mt-4">
        <h3 class="font-bold mb-2">{{ t('domain.existingDomains') }}</h3>
        <ul class="max-h-40 overflow-y-auto border rounded p-2">
          <li
            v-for="domain in diagramStore.domains"
            :key="domain.id"
            class="flex justify-between items-center py-1 border-b last:border-0"
          >
            <span>{{ domain.name }} ({{ domain.baseType }})</span>
            <div class="flex gap-1">
              <Button
                icon="bi bi-pencil"
                severity="secondary"
                text
                @click="
                  () => {
                    diagramStore.selectElement(domain.id)
                    dialogStore.open(DialogId.EditDomain)
                  }
                "
              />
              <Button
                icon="bi bi-trash"
                severity="danger"
                text
                @click="diagramStore.deleteElement(domain.id)"
              />
            </div>
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
      <Button
        :label="isEditMode ? t('common.confirm') : t('common.add')"
        icon="bi bi-check-lg"
        @click="saveDomain"
      />
    </template>
  </Dialog>
</template>

<style scoped></style>
