<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { DialogId } from '@/stores/dialogStore'
import { useDomainStore } from '@/stores/domainStore'

const { t } = useI18n()
const { visible, closeModal } = useDiagramDialog(DialogId.AddDomain, DialogId.EditDomain)

const domainStore = useDomainStore()

const domainName = ref('')
const baseType = ref('INTEGER')

watch(visible, (isNowVisible) => {
  if (!isNowVisible) return
  domainName.value = ''
  baseType.value = 'INTEGER'
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

const saveDomain = async () => {
  if (!domainName.value.trim()) return
  await domainStore.add(domainName.value.trim(), baseType.value)
  closeModal()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :style="{ width: '25rem' }"
    :header="t('panels.createDomain')"
  >
    <div class="flex flex-col gap-3">
      <label for="domainName" class="font-semibold">{{ t('domain.name') }}</label>
      <InputText id="domainName" v-model="domainName" autofocus @keydown.enter="saveDomain" />

      <label for="baseType" class="font-semibold mt-2">{{ t('domain.baseType') }}</label>
      <Select
        id="baseType"
        v-model="baseType"
        :options="baseTypeOptions"
        optionLabel="label"
        optionValue="value"
      />

      <div v-if="domainStore.domains.length > 0" class="mt-4">
        <h3 class="font-bold mb-2">{{ t('domain.existingDomains') }}</h3>
        <ul class="max-h-40 overflow-y-auto">
          <li
            v-for="domain in domainStore.domains"
            :key="domain.id"
            class="flex justify-between items-center py-1 border-b border-black/10 dark:border-white/10 last:border-0"
          >
            <span>{{ domain.name }} ({{ domain.baseType }})</span>
            <Button
              v-if="domain.baseType !== domain.name"
              icon="bi bi-trash"
              severity="danger"
              text
              @click="domainStore.remove(domain.name)"
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
      <Button :label="t('common.add')" icon="bi bi-check-lg" @click="saveDomain" />
    </template>
  </Dialog>
</template>

<style scoped></style>
