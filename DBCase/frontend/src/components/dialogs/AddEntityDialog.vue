<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'

const { t } = useI18n()
const dialogStore = useDialogStore()

const entityName = ref('')
const isWeakEntity = ref(false)
const relationName = ref('')
const selectedStrongEntity = ref(null)

const strongEntities = computed(() => [
  { label: t('entity.strongEntityA'), value: 'entityA' },
  { label: t('entity.strongEntityB'), value: 'entityB' },
])

const visible = computed(() => dialogStore.isOpen(DialogId.AddEntity))
const closeModal = () => dialogStore.close(DialogId.AddEntity)
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :draggable="false"
    :style="{ width: '30rem' }"
    :header="t('entity.addEntity')"
  >
    <div class="flex flex-col gap-3">
      <label for="entityName" class="font-semibold">{{ t('entity.entityName') }}</label>
      <InputText
        id="entityName"
        v-model="entityName"
        :placeholder="t('entity.enterEntityName')"
        autofocus
      />

      <div class="flex items-center gap-2 mt-2">
        <Checkbox v-model="isWeakEntity" inputId="isWeak" :binary="true" />
        <label for="isWeak">{{ t('entity.isWeakEntity') }}</label>
      </div>

      <template v-if="isWeakEntity">
        <label for="relationName" class="font-semibold mt-2">{{
          t('entity.weakRelationshipName')
        }}</label>
        <InputText
          id="relationName"
          v-model="relationName"
          :placeholder="t('entity.enterRelationName')"
        />

        <label for="strongEntity" class="font-semibold mt-2">{{ t('entity.strongEntity') }}</label>
        <Dropdown
          id="strongEntity"
          v-model="selectedStrongEntity"
          :options="strongEntities"
          optionLabel="label"
          :placeholder="t('entity.selectStrongEntity')"
        />
      </template>
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button :label="t('entity.addEntity')" icon="bi bi-check-lg" />
    </template>
  </Dialog>
</template>

<style scoped></style>
