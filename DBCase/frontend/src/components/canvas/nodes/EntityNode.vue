
<template>
  <v-group :config="{
    x: entity.position.x,
    y: entity.position.y,
    draggable: true
  }" @dragmove="handleDragMove">
    <v-rect v-if="entity.isWeak" :config="{
      width: entity.name.length < 8 ? 110 : (entity.name.length * 11) + 10,
      height: 50,
      fill: '#ffcc45',
      stroke: '#bf9523',
      strokeWidth: 2,
      cornerRadius: 8,
      offsetX: (entity.name.length < 8 ? 110 : (entity.name.length * 11) + 10) / 2,
      offsetY: 25
    }" />
    <v-rect :config="{
      width: entity.name.length < 8 ? 100 : entity.name.length * 11,
      height: 40,
      fill: '#ffcc45',
      stroke: '#bf9523',
      strokeWidth: 2,
      cornerRadius: 8,
      offsetX: (entity.name.length < 8 ? 100 : entity.name.length * 11) / 2,
      offsetY: 20
    }" />
    <v-text :config="{
      text: entity.name,
      fontSize: 16,
      fontFamily: 'arial',
      fill: 'black',
      width: entity.name.length < 8 ? 100 : entity.name.length * 11,
      height: 40,
      align: 'center',
      verticalAlign: 'middle',
      offsetX: (entity.name.length < 8 ? 100 : entity.name.length * 11) / 2,
      offsetY: 20
    }" />
  </v-group>
</template>

<script setup lang="ts">
import type { Entity } from '@/types/er-diagram-elements'

const props = defineProps<{
  entity: Entity
}>()

const emit = defineEmits(['dragmove']) // Changed to dragmove

const handleDragMove = (event: any) => { // Changed to handleDragMove
  emit('dragmove', { id: props.entity.id, x: event.target.x(), y: event.target.y() })
}
</script>
