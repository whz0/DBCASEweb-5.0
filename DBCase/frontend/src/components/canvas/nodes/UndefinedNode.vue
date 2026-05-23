<template>
  <v-group
    :config="{
      id: undefinedEl.id,
      x: undefinedEl.position.x,
      y: undefinedEl.position.y,
      draggable: true,
    }"
    @dragstart="handleDragStart"
    @dragmove="handleDragMove"
    @mousedown="handleSelect"
    @touchstart="handleSelect"
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
const isSelected = computed(() => erSchemaStore.isSelected(props.undefinedEl.id))

let prevX = 0
let prevY = 0

const handleDragStart = (event: KonvaEventObject<DragEvent>) => {
  prevX = event.target.x()
  prevY = event.target.y()
  erSchemaStore.saveHistory()
}

const handleDragMove = (e: KonvaEventObject<DragEvent>) => {
  const x = e.target.x()
  const y = e.target.y()
  erSchemaStore.moveSelected(props.undefinedEl.id, x - prevX, y - prevY)
  prevX = x
  prevY = y
  emit('dragmove', { id: props.undefinedEl.id, x, y })
}

const handleSelect = (e: KonvaEventObject<MouseEvent>) => {
  if (e.evt.ctrlKey) {
    erSchemaStore.selectElement(props.undefinedEl.id, true)
  } else if (!erSchemaStore.isSelected(props.undefinedEl.id)) {
    erSchemaStore.selectElement(props.undefinedEl.id)
  }
  e.cancelBubble = true
}
</script>
