<template>
  <v-group
    :config="{
      id: entity.id,
      x: entity.position.x,
      y: entity.position.y,
      draggable: true,
    }"
    @dragstart="handleDragStart"
    @dragmove="handleDragMove"
    @mousedown="handleSelect"
    @touchstart="handleSelect"
  >
    <v-rect
      v-if="entity.isWeak"
      :config="{
        width: entity.name.length < 8 ? 110 : entity.name.length * 11 + 10,
        height: 50,
        fill: '#ffcc45',
        stroke: isSelected ? 'blue' : '#bf9523',
        strokeWidth: isSelected ? 4 : 2,
        cornerRadius: 8,
        offsetX: (entity.name.length < 8 ? 110 : entity.name.length * 11 + 10) / 2,
        offsetY: 25,
      }"
    />
    <v-rect
      :config="{
        width: entity.name.length < 8 ? 100 : entity.name.length * 11,
        height: 40,
        fill: '#ffcc45',
        stroke: isSelected ? 'blue' : '#bf9523',
        strokeWidth: isSelected ? 4 : 2,
        cornerRadius: entity.isWeak ? 4 : 8,
        offsetX: (entity.name.length < 8 ? 100 : entity.name.length * 11) / 2,
        offsetY: 20,
      }"
    />
    <v-text
      :config="{
        text: entity.name,
        fontSize: 16,
        fontFamily: 'arial',
        fill: 'black',
        width: entity.name.length < 8 ? 100 : entity.name.length * 11,
        height: 40,
        align: 'center',
        verticalAlign: 'middle',
        offsetX: (entity.name.length < 8 ? 100 : entity.name.length * 11) / 2,
        offsetY: 20,
      }"
    />
  </v-group>
</template>

<script setup lang="ts">
import type { KonvaEventObject } from 'konva/lib/Node'
import { computed } from 'vue'

import { useErSchemaStore } from '@/stores/erSchemaStore'
import type { Entity } from '@/types/er-diagram-elements'

const props = defineProps<{
  entity: Entity
}>()

const erSchemaStore = useErSchemaStore()

const isSelected = computed(() => erSchemaStore.isSelected(props.entity.id))

const emit = defineEmits(['dragmove'])

let prevX = 0
let prevY = 0

const handleDragMove = (event: KonvaEventObject<DragEvent>) => {
  const x = event.target.x()
  const y = event.target.y()
  erSchemaStore.moveSelected(props.entity.id, x - prevX, y - prevY)
  prevX = x
  prevY = y
  emit('dragmove', { id: props.entity.id, x, y })
}

const handleDragStart = (event: KonvaEventObject<DragEvent>) => {
  prevX = event.target.x()
  prevY = event.target.y()
  erSchemaStore.saveHistory()
}

const handleSelect = (e: KonvaEventObject<MouseEvent>) => {
  if (e.evt.shiftKey) {
    erSchemaStore.selectElement(props.entity.id, true)
  } else if (!erSchemaStore.isSelected(props.entity.id)) {
    erSchemaStore.selectElement(props.entity.id)
  }
  e.cancelBubble = true
}
</script>
