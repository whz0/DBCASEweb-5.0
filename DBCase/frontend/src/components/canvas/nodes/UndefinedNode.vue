<template>
  <v-group
    :config="{
      id: undefinedEl.id,
      x: undefinedEl.position.x,
      y: undefinedEl.position.y,
      draggable: true,
    }"
    @dragstart="erSchemaStore.saveHistory()"
    @dragmove="
      (e: KonvaEventObject<DragEvent>) =>
        emit('dragmove', { id: undefinedEl.id, x: e.target.x(), y: e.target.y() })
    "
    @mousedown="
      (e: KonvaEventObject<MouseEvent>) => {
        erSchemaStore.selectElement(undefinedEl.id)
        e.cancelBubble = true
      }
    "
    @touchstart="
      (e: KonvaEventObject<MouseEvent>) => {
        erSchemaStore.selectElement(undefinedEl.id)
        e.cancelBubble = true
      }
    "
  >
    <v-circle
      :config="{
        radius: 30,
        fill: '#7c3aed',
        stroke: isSelected ? 'blue' : '#5b21b6',
        strokeWidth: isSelected ? 4 : 2,
      }"
    />
    <v-text
      :config="{
        text: undefinedEl.name,
        fontSize: 14,
        fontFamily: 'arial',
        fill: 'white',
        width: 60,
        height: 60,
        align: 'center',
        verticalAlign: 'middle',
        offsetX: 30,
        offsetY: 30,
      }"
    />
  </v-group>
</template>

<script setup lang="ts">
import type { KonvaEventObject } from 'konva/lib/Node'
import { computed } from 'vue'

import { useErSchemaStore } from '@/stores/erSchemaStore'
import type { Undefined } from '@/types/er-diagram-elements'

const props = defineProps<{ undefinedEl: Undefined }>()
const emit = defineEmits(['dragmove'])
const erSchemaStore = useErSchemaStore()
const isSelected = computed(() => erSchemaStore.selectedElementId === props.undefinedEl.id)
</script>
