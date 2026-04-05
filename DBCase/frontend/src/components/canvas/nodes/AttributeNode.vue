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
        textDecoration: attribute.isKey ? 'underline' : '',
        fill: 'black',
        x: -effectiveRadiusX,
        y: -effectiveRadiusY,
        width: effectiveRadiusX * 2,
        height: effectiveRadiusY * 2,
        align: 'center',
        verticalAlign: 'middle',
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

const isSelected = computed(() => erSchemaStore.selectedElementId === props.attribute.id)

const emit = defineEmits(['dragmove'])

const handleDragMove = (event: KonvaEventObject<DragEvent>) => {
  emit('dragmove', { id: props.attribute.id, x: event.target.x(), y: event.target.y() })
}

const handleDragStart = () => {
  erSchemaStore.saveHistory()
}

const handleSelect = (e: KonvaEventObject<MouseEvent>) => {
  erSchemaStore.selectElement(props.attribute.id)
  e.cancelBubble = true
}

const effectiveRadiusX = computed(() =>
  props.attribute.name.length < 8 ? 50 : props.attribute.name.length * 5.5,
)
const effectiveRadiusY = 25
</script>
