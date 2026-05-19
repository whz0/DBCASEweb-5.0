<template>
  <v-group
    :config="{
      id: attribute.id,
      x: attribute.position.x,
      y: attribute.position.y,
      draggable: true,
    }"
    @dragstart="handleDragStart"
    @dragmove="handleDragMove"
    @mousedown="handleSelect"
    @touchstart="handleSelect"
  >
    <v-ellipse
      :config="{
        radiusX: effectiveRadiusX,
        radiusY: effectiveRadiusY,
        fill: '#22bdb1',
        stroke: isSelected ? 'blue' : '#078980',
        strokeWidth: isSelected ? 4 : 2,
        dash: attribute.isDerived ? [6, 4] : [],
        zIndex: 1,
      }"
    />
    <v-ellipse
      v-if="attribute.isMultivalued"
      :config="{
        radiusX: effectiveRadiusX - 4,
        radiusY: effectiveRadiusY - 4,
        fill: 'transparent',
        stroke: isSelected ? 'blue' : '#078980',
        strokeWidth: 1,
        zIndex: 1,
      }"
    />
    <v-text
      :config="{
        text: attribute.isKey || attribute.isNotNull ? attribute.name : attribute.name + '*',
        fontSize: 16,
        fontFamily: 'arial',
        textDecoration: attribute.isKey && !isWeakEntityKey ? 'underline' : '',
        fill: 'black',
        x: -effectiveRadiusX,
        y: -effectiveRadiusY,
        width: effectiveRadiusX * 2,
        height: effectiveRadiusY * 2,
        align: 'center',
        verticalAlign: 'middle',
      }"
    />
    <!-- Dashed underline for primary key of a weak entity -->
    <v-line
      v-if="isWeakEntityKey"
      :config="{
        points: [-textWidth / 2, 9, textWidth / 2, 9],
        stroke: 'black',
        strokeWidth: 1,
        dash: [4, 3],
      }"
    />
  </v-group>
</template>

<script setup lang="ts">
import type { KonvaEventObject } from 'konva/lib/Node'
import { computed } from 'vue'

import { useErSchemaStore } from '@/stores/erSchemaStore'
import type { Attribute } from '@/types/er-diagram-elements'

const props = defineProps<{
  attribute: Attribute
}>()

const erSchemaStore = useErSchemaStore()

const isSelected = computed(() => erSchemaStore.isSelected(props.attribute.id))

const isWeakEntityKey = computed(() => {
  if (!props.attribute.isKey) return false
  const parentEntity = erSchemaStore.entities.find((e) => e.id === props.attribute.parentId)
  return parentEntity?.isWeak === true
})

const emit = defineEmits(['dragmove'])

let prevX = 0
let prevY = 0

const handleDragMove = (event: KonvaEventObject<DragEvent>) => {
  const x = event.target.x()
  const y = event.target.y()
  erSchemaStore.moveSelected(props.attribute.id, x - prevX, y - prevY)
  prevX = x
  prevY = y
  emit('dragmove', { id: props.attribute.id, x, y })
}

const handleDragStart = (event: KonvaEventObject<DragEvent>) => {
  prevX = event.target.x()
  prevY = event.target.y()
  erSchemaStore.saveHistory()
}

const handleSelect = (e: KonvaEventObject<MouseEvent>) => {
  if (e.evt.shiftKey) {
    erSchemaStore.selectElement(props.attribute.id, true)
  } else if (!erSchemaStore.isSelected(props.attribute.id)) {
    erSchemaStore.selectElement(props.attribute.id)
  }
  e.cancelBubble = true
}

const effectiveRadiusX = computed(() =>
  props.attribute.name.length < 8 ? 50 : props.attribute.name.length * 5.5,
)
const effectiveRadiusY = 25

// Approximate text width to center the dashed underline
const textWidth = computed(() => {
  const charWidth = 8.5
  return props.attribute.name.length * charWidth
})
</script>
