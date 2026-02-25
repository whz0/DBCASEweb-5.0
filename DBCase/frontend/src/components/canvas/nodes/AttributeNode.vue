
<template>
  <v-group :config="{
    x: attribute.position.x,
    y: attribute.position.y,
    draggable: true
  }">
    <v-ellipse :config="{
      width: 100,
      height: 50,
      fill: 'white',
      stroke: 'black',
      strokeWidth: 2
    }" />
    <v-text :config="{
      text: attribute.name,
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
import type { Attribute } from '@/types/er-diagram-elements'

defineProps<{
  attribute: Attribute
}>()
</script>
