<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  visible: boolean
  sourceType: 'er' | 'logical' | 'physical'
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'transform', target: 'er' | 'logical' | 'physical'): void
}>()
</script>

<template>
  <Dialog
    :visible="props.visible"
    @update:visible="emit('update:visible', $event)"
    modal
    :dismissable-mask="true"
    :draggable="false"
    :header="t('schema.transform')"
    :style="{ width: '25rem' }"
  >
    <div class="flex flex-col gap-2">
      <Button
        v-if="props.sourceType !== 'er'"
        @click="emit('transform', 'er')"
        :label="t('schema.generateER')"
      />
      <Button
        v-if="props.sourceType !== 'logical'"
        @click="emit('transform', 'logical')"
        :label="t('schema.generateLogical')"
      />
      <Button
        v-if="props.sourceType !== 'physical'"
        @click="emit('transform', 'physical')"
        :label="t('schema.generatePhysical')"
      />
    </div>
  </Dialog>
</template>
