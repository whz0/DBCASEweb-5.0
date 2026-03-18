<template>
  <v-group
    :config="{
      id: relationship.id,
      x: relationship.position.x,
      y: relationship.position.y,
      draggable: true,
    }"
    @dragmove="handleDragMove"
    @mousedown="handleSelect"
    @touchstart="handleSelect"
  >
    <v-line
      v-if="relationship.type === 'Weak'"
      :config="{
        points: calculateWeakDiamondPoints(relationship.name),
        fill: 'transparent',
        stroke: isSelected ? 'blue' : '#c9280e',
        strokeWidth: 1,
        closed: true,
        offsetX: calculateWeakDiamondOffsetX(relationship.name),
        offsetY: 25,
      }"
    />
    <v-line
      :config="{
        points: calculateDiamondPoints(relationship.name),
        fill: '#FF3F20',
        stroke: isSelected ? 'blue' : '#c9280e',
        strokeWidth: isSelected ? 4 : 2,
        closed: true,
        offsetX: calculateDiamondOffsetX(relationship.name),
        offsetY: 25,
      }"
    />
    <v-line
      v-if="relationship.type === 'IsA'"
      :config="{
        points: calculateTrianglePoints(),
        fill: '#FF3F20',
        stroke: isSelected ? 'blue' : '#c9280e',
        strokeWidth: isSelected ? 4 : 2,
        closed: true,
        offsetX: 25,
        offsetY: 25,
      }"
    />
    <v-text
      :config="{
        text: relationship.type === 'IsA' ? '' : relationship.name,
        fontSize: 16,
        fontFamily: 'arial',
        fill: 'black',
        width: calculateTextWidth(relationship.name),
        height: 50,
        align: 'center',
        verticalAlign: 'middle',
        offsetX: calculateTextOffsetX(relationship.name),
        offsetY: 25,
      }"
    />
  </v-group>
</template>

<script setup lang="ts">
import type { KonvaEventObject } from 'konva/lib/Node'
import { computed } from 'vue'

import { useDiagramStore } from '@/stores/diagramStore'
import type { Relationship } from '@/types/er-diagram-elements'

const props = defineProps<{
  relationship: Relationship
}>()

const store = useDiagramStore()

const isSelected = computed(() => store.selectedElementId === props.relationship.id)

const emit = defineEmits(['dragmove'])

const handleDragMove = (event: KonvaEventObject<DragEvent>) => {
  emit('dragmove', { id: props.relationship.id, x: event.target.x(), y: event.target.y() })
}

const handleSelect = (e: KonvaEventObject<MouseEvent>) => {
  store.selectElement(props.relationship.id)
  e.cancelBubble = true
}

const calculateTextWidth = (name: string) => {
  const baseWidth = 100
  const charWidth = 11 // Approximation
  return name.length < 8 ? baseWidth : name.length * charWidth
}

const calculateTextOffsetX = (name: string) => {
  return calculateTextWidth(name) / 2
}

const calculateDiamondPoints = (name: string) => {
  const width = calculateTextWidth(name)
  const height = 50
  return [width / 2, 0, width, height / 2, width / 2, height, 0, height / 2]
}

const calculateDiamondOffsetX = (name: string) => {
  return calculateTextWidth(name) / 2
}

const calculateWeakDiamondPoints = (name: string) => {
  const width = calculateTextWidth(name) + 10 // Slightly larger for weak
  const height = 60 // Slightly larger for weak
  return [width / 2, 0, width, height / 2, width / 2, height, 0, height / 2]
}

const calculateWeakDiamondOffsetX = (name: string) => {
  return (calculateTextWidth(name) + 10) / 2
}

const calculateTrianglePoints = () => {
  const size = 50
  return [size / 2, 0, size, size, 0, size]
}
</script>
