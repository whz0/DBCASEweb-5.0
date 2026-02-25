
<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useDiagramStore } from '@/stores/diagramStore'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const { t } = useI18n()
const dialogStore = useDialogStore()
const store = useDiagramStore()

const name = ref('')

const visible = computed(() => dialogStore.isOpen(DialogId.AddRelationship))
const closeModal = () => dialogStore.close(DialogId.AddRelationship)

const addRelationship = () => {
  store.addRelationship({
    id: crypto.randomUUID(),
    name: name.value,
    position: { x: 100, y: 100 },
    type: 'Normal',
    participants: [],
    attributes: []
  })
  name.value = ''
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
    :header="t('relationship.addRelationship')"
  >
    <div class="flex flex-col gap-3">
      <label for="name" class="font-semibold">{{ t('common.name') }}</label>
      <InputText
        id="name"
        v-model="name"
        :placeholder="t('relationship.enterRelationshipName')"
        autofocus
      />
    </div>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button :label="t('relationship.addRelationship')" icon="bi bi-check-lg" @click="addRelationship" />
    </template>
  </Dialog>
</template>

<style scoped></style>
