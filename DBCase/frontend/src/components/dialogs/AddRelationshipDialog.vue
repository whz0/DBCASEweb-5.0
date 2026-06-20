<script setup lang="ts">
import { useToast } from 'primevue'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramDialog } from '@/composables/useDiagramDialog'
import { targetParticipantId } from '@/composables/useTargetParticipant'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import type { Entity, Relationship } from '@/types/er-diagram-elements'

const { t } = useI18n()
const { erSchemaStore, isEditMode, visible, closeModal } = useDiagramDialog(
  DialogId.AddRelationship,
  DialogId.EditRelationship,
)
const dialogStore = useDialogStore()
const toast = useToast()

const name = ref('')

const currentRelationship = computed<Relationship | null>(() => {
  if (!isEditMode.value) return null
  const id = erSchemaStore.selectedElementId
  return erSchemaStore.relationships.find((r: Relationship) => r.id === id) || null
})

const participantsWithNames = computed(() => {
  if (!currentRelationship.value) return []
  const entityCount: Record<string, number> = {}
  return currentRelationship.value.participants.map((p) => {
    const occurrenceIndex = entityCount[p.entityId] ?? 0
    entityCount[p.entityId] = occurrenceIndex + 1
    return {
      ...p,
      occurrenceIndex,
      name: (() => {
        const entity = erSchemaStore.entities.find((e) => e.id === p.entityId)
        if (entity) return entity.name
        const aggRel = erSchemaStore.relationships.find(
          (r) => r.id === p.entityId && r.type === 'Aggregation',
        )
        return aggRel ? `[Agr] ${aggRel.aggregationName}` : 'Unknown'
      })(),
    }
  })
})

watch(visible, (isNowVisible) => {
  if (isNowVisible) {
    name.value = isEditMode.value && currentRelationship.value ? currentRelationship.value.name : ''
  }
})

const save = () => {
  if (!name.value.trim()) return
  const trimmed = name.value.trim()
  const currentId = isEditMode.value ? erSchemaStore.selectedElementId : null
  const nameTaken =
    erSchemaStore.entities.some((e: Entity) => e.name === trimmed && e.id !== currentId) ||
    erSchemaStore.relationships.some((r: Relationship) => r.name === trimmed && r.id !== currentId)
  if (nameTaken) {
    toast.add({ severity: 'error', detail: t('entity.nameAlreadyExists'), life: 3000 })
    return
  }
  erSchemaStore.saveRelationship({ name: trimmed }, isEditMode.value)
  closeModal()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
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
                      p.occurrenceIndex,
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
