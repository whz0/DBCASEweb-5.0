
<template>
  <v-group :config="{
    x: attribute.position.x,
    y: attribute.position.y,
    draggable: true
  }" @dragmove="handleDragMove">
    <v-ellipse :config="{
      radiusX: effectiveRadiusX,
      radiusY: effectiveRadiusY,
      fill: '#22bdb1',
      stroke: '#078980',
      strokeWidth: 2
    }" />
    <v-text :config="{
      text: attribute.name,
      fontSize: 16,
      fontFamily: 'arial',
      fill: 'black',
      x: -effectiveRadiusX,
      y: -effectiveRadiusY,
      width: effectiveRadiusX * 2,
      height: effectiveRadiusY * 2,
      align: 'center',
      verticalAlign: 'middle',
    }" />
  </v-group>
</template>

<script setup lang="ts">
import type { Attribute } from '@/types/er-diagram-elements'
import { computed } from 'vue'

const props = defineProps<{
  attribute: Attribute
}>()

const emit = defineEmits(['dragmove'])

const handleDragMove = (event: any) => {
  emit('dragmove', { id: props.attribute.id, x: event.target.x(), y: event.target.y() })
}

const effectiveRadiusX = computed(() => props.attribute.name.length < 8 ? 50 : props.attribute.name.length * 5.5)
const effectiveRadiusY = 25
</script>
