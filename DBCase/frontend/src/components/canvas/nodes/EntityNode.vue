
<template>
  <v-group :config="{
    x: entity.position.x,
    y: entity.position.y,
    draggable: true
  }">
    <v-rect :config="{
      width: 100,
      height: 50,
      fill: 'white',
      stroke: 'black',
      strokeWidth: 2
    }" />
    <v-text :config="{
      text: entity.name,
      fontSize: 16,
      fontFamily: 'Calibri',
      fill: 'black',
      width: 100,
      padding: 10,
      align: 'center'
    }" />
  </v-group>
</template>

<script setup lang="ts">
import type { Entity } from '@/types/er-diagram-elements'

defineProps<{
  entity: Entity
}>()
</script>
