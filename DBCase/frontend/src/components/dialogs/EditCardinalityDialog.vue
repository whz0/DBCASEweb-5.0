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
  (currentRelationship.value?.participants ?? []).map((p) => {
    const entity = erSchemaStore.entities.find((e) => e.id === p.entityId)
    const rolePart = p.role ? `[${p.role}] ` : ''
    const aggRel = !entity
      ? erSchemaStore.relationships.find((r) => r.id === p.entityId && r.type === 'Aggregation')
      : undefined
    const name = entity
      ? `${rolePart}${entity.name}`
      : aggRel
        ? `${rolePart}[Agr] ${aggRel.aggregationName}`
        : 'Unknown'
    return { ...p, name, key: `${p.entityId}:${p.role ?? ''}` }
  }),
)

const selectedKey = ref<string | null>(null)
const selectedEntityId = computed(() => selectedKey.value?.split(':')[0] ?? null)
const selectedRole = computed(() => selectedKey.value?.split(':').slice(1).join(':') ?? '')

const isSelectedWeak = computed(
  () => erSchemaStore.entities.find((e) => e.id === selectedEntityId.value)?.isWeak ?? false,
)
const cardinality = ref<'1' | 'N'>('N')
const participation = ref<'parcial' | 'total'>('parcial')
const useMinMax = ref(false)
const minVal = ref('0')
const maxVal = ref('n')
const role = ref('')

function syncRadiosToFields() {
  minVal.value = participation.value === 'total' ? '1' : '0'
  maxVal.value = cardinality.value === '1' ? '1' : 'n'
}

function syncFieldsToRadios() {
  const min = minVal.value
  const max = maxVal.value.toLowerCase()
  cardinality.value = max === '1' ? '1' : 'N'
  participation.value = min === '1' ? 'total' : 'parcial'
}

function onCardinalityChange() {
  useMinMax.value = false
  syncRadiosToFields()
}

function onParticipationChange() {
  useMinMax.value = false
  syncRadiosToFields()
}

function onUseMinMaxChange(val: boolean) {
  useMinMax.value = val
  if (!val) syncFieldsToRadios()
}

function loadByKey(key: string | null) {
  selectedKey.value = key
  if (!key || !currentRelationship.value) return
  const [eId, ...roleParts] = key.split(':')
  const r = roleParts.join(':')
  const p = currentRelationship.value.participants.find(
    (p) => p.entityId === eId && (p.role ?? '') === r,
  )
  if (!p) return
  role.value = p.role ?? ''
  minVal.value = p.cardinalityMin
  maxVal.value = p.cardinalityMax
  const min = p.cardinalityMin
  const max = p.cardinalityMax.toLowerCase()
  const isWeak = erSchemaStore.entities.find((e) => e.id === eId)?.isWeak ?? false
  const isStandard = (min === '0' || min === '1') && (max === '1' || max === 'n')
  useMinMax.value = !isStandard
  cardinality.value = max === '1' ? '1' : 'N'
  participation.value = isWeak ? 'total' : min === '1' ? 'total' : 'parcial'
  if (isWeak) minVal.value = '1'
}

watch(visible, (v) => {
  if (v) {
    const targetId = targetParticipantId.value
    const first = participants.value[0]
    const initialKey = targetId
      ? (participants.value.find((p) => p.entityId === targetId)?.key ?? first?.key ?? null)
      : (first?.key ?? null)
    loadByKey(initialKey)
  }
})
watch(selectedKey, loadByKey)

const closeModal = () => dialogStore.close(DialogId.EditCardinality)

const save = () => {
  if (!currentRelationship.value || !selectedEntityId.value) return
  erSchemaStore.updateParticipant(
    currentRelationship.value.id,
    selectedEntityId.value,
    { cardinalityMin: minVal.value, cardinalityMax: maxVal.value, role: role.value },
    selectedRole.value,
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
    :style="{ width: '26rem' }"
    :header="
      t('relationship.editParticipant') +
      (currentRelationship ? ': ' + currentRelationship.name : '')
    "
  >
    <div class="flex flex-col gap-3">
      <label class="font-semibold">{{ t('relationship.entity') }}</label>
      <Select
        :modelValue="selectedKey"
        @update:modelValue="selectedKey = $event"
        :options="participants"
        optionLabel="name"
        optionValue="key"
      />

      <label class="font-semibold mt-1">{{ t('relationship.cardinality') }}</label>
      <div class="flex flex-col gap-1">
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="cardinality"
            inputId="ecard1"
            value="1"
            @update:modelValue="onCardinalityChange"
          />
          <label for="ecard1">1</label>
        </div>
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="cardinality"
            inputId="ecardN"
            value="N"
            @update:modelValue="onCardinalityChange"
          />
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
            :disabled="isSelectedWeak"
            @update:modelValue="onParticipationChange"
          />
          <label for="eparcial">{{ t('relationship.partial') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="participation"
            inputId="etotal"
            value="total"
            :disabled="isSelectedWeak"
            @update:modelValue="onParticipationChange"
          />
          <label for="etotal">{{ t('relationship.total') }}</label>
        </div>
      </div>

      <label class="font-semibold mt-1">{{ t('relationship.minMaxParticipation') }}</label>
      <div class="flex items-center gap-2 flex-wrap">
        <Checkbox
          :modelValue="useMinMax"
          :binary="true"
          inputId="eminmax"
          @update:modelValue="onUseMinMaxChange"
        />
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
