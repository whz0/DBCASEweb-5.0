
<template>
  <v-group :config="{
    x: relationship.position.x,
    y: relationship.position.y,
    draggable: true
  }" @dragmove="handleDragMove">
    <v-line :config="{
      points: calculateDiamondPoints(relationship.name),
      fill: '#FF3F20',
      stroke: '#c9280e',
      strokeWidth: 2,
      closed: true,
      offsetX: calculateDiamondOffsetX(relationship.name),
      offsetY: 25
    }" />
    <v-line v-if="relationship.isWeak" :config="{
      points: calculateWeakDiamondPoints(relationship.name),
      fill: '#FF3F20',
      stroke: '#c9280e',
      strokeWidth: 2,
      closed: true,
      offsetX: calculateWeakDiamondOffsetX(relationship.name),
      offsetY: 25
    }" />
    <v-text :config="{
      text: relationship.name,
      fontSize: 16,
      fontFamily: 'arial',
      fill: 'black',
      width: calculateTextWidth(relationship.name),
      height: 50,
      align: 'center',
      verticalAlign: 'middle',
      offsetX: calculateTextOffsetX(relationship.name),
      offsetY: 25
    }" />
  </v-group>
</template>

<script setup lang="ts">
import type { Relationship } from '@/types/er-diagram-elements'

const props = defineProps<{
  relationship: Relationship
}>()

const emit = defineEmits(['dragmove']) // Changed to dragmove

const handleDragMove = (event: any) => { // Changed to handleDragMove
  emit('dragmove', { id: props.relationship.id, x: event.target.x(), y: event.target.y() })
}

const calculateTextWidth = (name: string) => {
  const baseWidth = 100;
  const charWidth = 11; // Approximation
  return name.length < 8 ? baseWidth : name.length * charWidth;
};

const calculateTextOffsetX = (name: string) => {
  return calculateTextWidth(name) / 2;
};

const calculateDiamondPoints = (name: string) => {
  const width = calculateTextWidth(name);
  const height = 50;
  return [width / 2, 0, width, height / 2, width / 2, height, 0, height / 2];
};

const calculateDiamondOffsetX = (name: string) => {
  return calculateTextWidth(name) / 2;
};

const calculateWeakDiamondPoints = (name: string) => {
  const width = calculateTextWidth(name) + 10; // Slightly larger for weak
  const height = 60; // Slightly larger for weak
  return [width / 2, 0, width, height / 2, width / 2, height, 0, height / 2];
};

const calculateWeakDiamondOffsetX = (name: string) => {
  return (calculateTextWidth(name) + 10) / 2;
};
</script>
