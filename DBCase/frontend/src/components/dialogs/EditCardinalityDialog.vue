<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { targetParticipantId } from '@/composables/useTargetParticipant'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const erSchemaStore = useErSchemaStore()

const visible = computed(() => dialogStore.isOpen(DialogId.EditCardinality))

const currentRelationship = computed(() => {
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.relationships.find((r) => r.id === id) ?? null
})

const participants = computed(() =>
  (currentRelationship.value?.participants ?? []).map((p) => ({
    ...p,
    name: erSchemaStore.entities.find((e) => e.id === p.entityId)?.name ?? 'Unknown',
  })),
)

const selectedEntityId = ref<string | null>(null)
const cardinality = ref<'1' | 'N'>('N')
const participation = ref<'parcial' | 'total'>('parcial')
const useMinMax = ref(false)
const minVal = ref('0')
const maxVal = ref('n')
const role = ref('')

function loadParticipant(entityId: string | null) {
  selectedEntityId.value = entityId
  if (!entityId || !currentRelationship.value) return
  const p = currentRelationship.value.participants.find((p) => p.entityId === entityId)
  if (!p) return
  role.value = p.role ?? ''
  const min = p.cardinalityMin
  const max = p.cardinalityMax.toLowerCase()
  const isStandard = (min === '0' || min === '1') && (max === '1' || max === 'n')
  if (isStandard) {
    useMinMax.value = false
    cardinality.value = max === '1' ? '1' : 'N'
    participation.value = min === '1' ? 'total' : 'parcial'
  } else {
    useMinMax.value = true
    minVal.value = min
    maxVal.value = p.cardinalityMax
  }
}

watch(visible, (v) => {
  if (v) loadParticipant(targetParticipantId.value ?? participants.value[0]?.entityId ?? null)
})
watch(selectedEntityId, loadParticipant)

const computedMin = computed(() =>
  useMinMax.value ? minVal.value : participation.value === 'total' ? '1' : '0',
)
const computedMax = computed(() =>
  useMinMax.value ? maxVal.value : cardinality.value === '1' ? '1' : 'n',
)

const closeModal = () => dialogStore.close(DialogId.EditCardinality)

const save = () => {
  if (!currentRelationship.value || !selectedEntityId.value) return
  erSchemaStore.updateParticipant(currentRelationship.value.id, selectedEntityId.value, {
    cardinalityMin: computedMin.value,
    cardinalityMax: computedMax.value,
    role: role.value,
  })
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
    :style="{ width: '26rem' }"
    :header="
      t('relationship.editParticipant') +
      (currentRelationship ? ': ' + currentRelationship.name : '')
    "
  >
    <div class="flex flex-col gap-3">
      <label class="font-semibold">{{ t('relationship.entity') }}</label>
      <Select
        :modelValue="selectedEntityId"
        @update:modelValue="selectedEntityId = $event"
        :options="participants"
        optionLabel="name"
        optionValue="entityId"
      />

      <label class="font-semibold mt-1">{{ t('relationship.cardinality') }}</label>
      <div class="flex flex-col gap-1">
        <div class="flex items-center gap-2">
          <RadioButton v-model="cardinality" inputId="ecard1" value="1" :disabled="useMinMax" />
          <label for="ecard1">1</label>
        </div>
        <div class="flex items-center gap-2">
          <RadioButton v-model="cardinality" inputId="ecardN" value="N" :disabled="useMinMax" />
          <label for="ecardN">N</label>
        </div>
      </div>

      <label class="font-semibold mt-1">{{ t('relationship.participation') }}</label>
      <div class="flex flex-col gap-1">
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="participation"
            inputId="eparcial"
            value="parcial"
            :disabled="useMinMax"
          />
          <label for="eparcial">{{ t('relationship.partial') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="participation"
            inputId="etotal"
            value="total"
            :disabled="useMinMax"
          />
          <label for="etotal">{{ t('relationship.total') }}</label>
        </div>
      </div>

      <label class="font-semibold mt-1">{{ t('relationship.minMaxParticipation') }}</label>
      <div class="flex items-center gap-2 flex-wrap">
        <Checkbox v-model="useMinMax" :binary="true" inputId="eminmax" />
        <label for="eminmax" class="flex items-center gap-2">
          Min
          <InputText v-model="minVal" class="w-16" :disabled="!useMinMax" @keydown.enter="save" />
          Max
          <InputText v-model="maxVal" class="w-16" :disabled="!useMinMax" @keydown.enter="save" />
        </label>
      </div>

      <label class="font-semibold mt-1">{{ t('relationship.role') }}</label>
      <InputText v-model="role" :placeholder="t('relationship.enterRole')" @keydown.enter="save" />
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button :label="t('common.confirm')" icon="bi bi-check-lg" @click="save" />
    </template>
  </Dialog>
</template>
