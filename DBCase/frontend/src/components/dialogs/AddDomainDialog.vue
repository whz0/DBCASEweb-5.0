<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramStore } from '@/stores/diagramStore'
import { DialogId, useDialogStore } from '@/stores/dialogStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const diagramStore = useDiagramStore()

const domainName = ref('')
const baseType = ref('INTEGER')
const values = ref('')

const baseTypeOptions = [
  { label: 'INTEGER', value: 'INTEGER' },
  { label: 'VARCHAR', value: 'VARCHAR' },
  { label: 'TEXT', value: 'TEXT' },
  { label: 'FLOAT', value: 'FLOAT' },
  { label: 'BOOLEAN', value: 'BOOLEAN' },
  { label: 'DATE', value: 'DATE' },
]

const visible = computed(() => dialogStore.isOpen(DialogId.AddDomain))

const closeModal = () => {
  dialogStore.close(DialogId.AddDomain)
  domainName.value = ''
  baseType.value = 'INTEGER'
  values.value = ''
}

const addDomain = () => {
  if (domainName.value.trim()) {
    diagramStore.addDomain({
      id: crypto.randomUUID(),
      name: domainName.value.trim(),
      baseType: baseType.value,
      values: values.value.trim() ? values.value.split(',').map((v) => v.trim()) : undefined,
    })
    closeModal()
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
    :style="{ width: '25rem' }"
    :header="t('panels.createDomain')"
  >
    <div class="flex flex-col gap-3">
      <label for="domainName" class="font-semibold">{{ t('domain.name') }}</label>
      <InputText id="domainName" v-model="domainName" autofocus @keyup.enter="addDomain" />

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

      <div v-if="diagramStore.domains.length > 0" class="mt-4">
        <h3 class="font-bold mb-2">{{ t('domain.existingDomains') }}</h3>
        <ul class="max-h-40 overflow-y-auto border rounded p-2">
          <li
            v-for="domain in diagramStore.domains"
            :key="domain.id"
            class="flex justify-between items-center py-1 border-b last:border-0"
          >
            <span>{{ domain.name }} ({{ domain.baseType }})</span>
            <Button
              icon="bi bi-trash"
              severity="danger"
              text
              @click="diagramStore.deleteElement(domain.id)"
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
      <Button :label="t('common.add')" icon="bi bi-check-lg" @click="addDomain" />
    </template>
  </Dialog>
</template>
