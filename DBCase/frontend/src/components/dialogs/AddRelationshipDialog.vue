<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { targetParticipantId } from '@/composables/useTargetParticipant'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import type { Relationship } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { erSchemaStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddRelationship,
  DialogId.EditRelationship,
)
const dialogStore = useDialogStore()

const name = ref('')

const currentRelationship = computed<Relationship | null>(() => {
  if (!isEditMode.value) return null
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.relationships.find((r: Relationship) => r.id === id) || null
})

const participantsWithNames = computed(() => {
  if (!currentRelationship.value) return []
  return currentRelationship.value.participants.map((p) => ({
    ...p,
    name: erSchemaStore.entities.find((e) => e.id === p.entityId)?.name ?? 'Unknown',
  }))
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    name.value = isEditMode.value && currentRelationship.value ? currentRelationship.value.name : ''
  }
})

const save = () => {
  if (!name.value.trim()) return
  erSchemaStore.saveRelationship({ name: name.value.trim() }, isEditMode.value)
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
    :header="isEditMode ? t('relationship.editRelationship') : t('relationship.addRelationship')"
  >
    <form @submit.prevent="save">
      <div class="flex flex-col gap-3">
        <label for="name" class="font-semibold">{{ t('common.name') }}</label>
        <InputText
          id="name"
          v-model="name"
          :placeholder="t('relationship.enterRelationshipName')"
          autofocus
        />

        <template v-if="isEditMode">
          <div class="flex items-center justify-between mt-2">
            <span class="font-semibold">{{ t('relationship.currentParticipants') }}</span>
            <Button
              :label="t('entity.addEntity')"
              icon="bi bi-plus"
              size="small"
              severity="secondary"
              @click="dialogStore.open(DialogId.AddEntityToRelationship)"
            />
          </div>
          <ul v-if="participantsWithNames.length > 0">
            <li
              v-for="p in participantsWithNames"
              :key="p.entityId"
              class="flex justify-between items-center py-1 border-b border-black/10 dark:border-white/10 last:border-0"
            >
              <span class="text-sm">
                <span class="font-semibold">{{ p.name }}</span>
                ({{ p.cardinalityMin }},{{ p.cardinalityMax }})
                <span v-if="p.role" class="italic text-gray-400"> — {{ p.role }}</span>
              </span>
              <div class="flex">
                <Button
                  icon="bi bi-pencil"
                  severity="secondary"
                  text
                  size="small"
                  @click="
                    () => {
                      erSchemaStore.selectElement(currentRelationship!.id)
                      targetParticipantId = p.entityId
                      dialogStore.open(DialogId.EditCardinality)
                    }
                  "
                />
                <Button
                  icon="bi bi-trash"
                  severity="danger"
                  text
                  size="small"
                  @click="
                    erSchemaStore.removeParticipantFromRelationship(
                      currentRelationship!.id,
                      p.entityId,
                    )
                  "
                />
              </div>
            </li>
          </ul>
          <span v-else class="text-sm text-gray-400">{{ t('relationship.noParticipants') }}</span>
        </template>
      </div>
    </form>

    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="closeModal"
      />
      <Button
        :label="isEditMode ? t('common.confirm') : t('relationship.addRelationship')"
        icon="bi bi-check-lg"
        @click="save"
      />
    </template>
  </Dialog>
</template>
