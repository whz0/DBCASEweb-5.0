
<template>
  <v-group :config="{
    x: relationship.position.x,
    y: relationship.position.y,
    draggable: true
  }">
    <v-line :config="{
      points: [50, 0, 100, 25, 50, 50, 0, 25],
      fill: 'white',
      stroke: 'black',
      strokeWidth: 2,
      closed: true
    }" />
    <v-text :config="{
      text: relationship.name,
      fontSize: 16,
      fontFamily: 'Calibri',
      fill: 'black',
      width: 100,
      padding: 10,
      align: 'center',
      verticalAlign: 'middle'
    }" />
  </v-group>
</template>

<script setup lang="ts">
import type { Relationship } from '@/types/er-diagram-elements'

defineProps<{
  relationship: Relationship
}>()
</script>
