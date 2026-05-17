<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const erSchemaStore = useErSchemaStore()

const visible = computed(() => dialogStore.isOpen(DialogId.AddEntityToRelationship))

const currentRelationship = computed(() => {
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.relationships.find((r) => r.id === id) ?? null
})

const availableEntities = computed(() => {
  const alreadyIn = currentRelationship.value?.participants.map((p) => p.entityId) ?? []
  const entities = erSchemaStore.entities
    .filter((e) => !alreadyIn.includes(e.id))
    .map((e) => ({ id: e.id, name: e.name }))
  const aggregations = erSchemaStore.relationships
    .filter((r) => r.type === 'Aggregation' && r.aggregationName && !alreadyIn.includes(r.id))
    .map((r) => ({ id: r.id, name: `[Agr] ${r.aggregationName}` }))
  return [...entities, ...aggregations]
})

const selectedEntity = ref<{ id: string; name: string } | null>(null)
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
  cardinality.value = maxVal.value.toLowerCase() === '1' ? '1' : 'N'
  participation.value = minVal.value === '1' ? 'total' : 'parcial'
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

const closeModal = () => {
  dialogStore.close(DialogId.AddEntityToRelationship)
  selectedEntity.value = null
  cardinality.value = 'N'
  participation.value = 'parcial'
  useMinMax.value = false
  minVal.value = '0'
  maxVal.value = 'n'
  role.value = ''
}

const save = () => {
  if (!currentRelationship.value || !selectedEntity.value) return
  erSchemaStore.addParticipantToRelationship(currentRelationship.value.id, {
    entityId: selectedEntity.value.id,
    cardinalityMin: minVal.value,
    cardinalityMax: maxVal.value,
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
    :header="t('entity.addEntity') + (currentRelationship ? ': ' + currentRelationship.name : '')"
  >
    <div class="flex flex-col gap-3">
      <label class="font-semibold">{{ t('relationship.entity') }}</label>
      <Select
        v-model="selectedEntity"
        :options="availableEntities"
        optionLabel="name"
        :placeholder="t('relationship.selectEntity')"
      />

      <label class="font-semibold mt-1">{{ t('relationship.cardinality') }}</label>
      <div class="flex flex-col gap-1">
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="cardinality"
            inputId="card1"
            value="1"
            @update:modelValue="onCardinalityChange"
          />
          <label for="card1">1</label>
        </div>
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="cardinality"
            inputId="cardN"
            value="N"
            @update:modelValue="onCardinalityChange"
          />
          <label for="cardN">N</label>
        </div>
      </div>

      <label class="font-semibold mt-1">{{ t('relationship.participation') }}</label>
      <div class="flex flex-col gap-1">
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="participation"
            inputId="parcial"
            value="parcial"
            @update:modelValue="onParticipationChange"
          />
          <label for="parcial">{{ t('relationship.partial') }}</label>
        </div>
        <div class="flex items-center gap-2">
          <RadioButton
            v-model="participation"
            inputId="total"
            value="total"
            @update:modelValue="onParticipationChange"
          />
          <label for="total">{{ t('relationship.total') }}</label>
        </div>
      </div>

      <label class="font-semibold mt-1">{{ t('relationship.minMaxParticipation') }}</label>
      <div class="flex items-center gap-2 flex-wrap">
        <Checkbox
          :modelValue="useMinMax"
          :binary="true"
          inputId="minmax"
          @update:modelValue="onUseMinMaxChange"
        />
        <label for="minmax" class="flex items-center gap-2">
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
      <Button
        :label="t('common.add')"
        icon="bi bi-check-lg"
        :disabled="!selectedEntity"
        @click="save"
      />
    </template>
  </Dialog>
</template>
