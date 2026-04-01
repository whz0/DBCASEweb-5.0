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
        cornerRadius: 8,
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

import { useDiagramStore } from '@/stores/erSchemaStore.ts'
import type { Entity } from '@/types/er-diagram-elements'

const props = defineProps<{
  entity: Entity
}>()

const store = useDiagramStore()

const isSelected = computed(() => store.selectedElementId === props.entity.id)

const emit = defineEmits(['dragmove'])

const handleDragMove = (event: KonvaEventObject<DragEvent>) => {
  emit('dragmove', { id: props.entity.id, x: event.target.x(), y: event.target.y() })
}

const handleDragStart = () => {
  store.saveHistory()
}

const handleSelect = (e: KonvaEventObject<MouseEvent>) => {
  store.selectElement(props.entity.id)
  e.cancelBubble = true
}
</script>
