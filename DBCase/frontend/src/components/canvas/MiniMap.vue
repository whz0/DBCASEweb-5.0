<script setup lang="ts">
import type { KonvaEventObject } from 'konva/lib/Node'
import { computed, onMounted, onUnmounted, ref, watchEffect } from 'vue'

import { useErSchemaStore } from '@/stores/erSchemaStore'
import type { Entity, Relationship } from '@/types/er-diagram-elements'

const props = defineProps<{
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  mainStage: any | null
}>()

const store = useErSchemaStore()
const miniMapWidth = 200
const miniMapHeight = 150
const padding = 10

const viewportRect = ref({ x: 0, y: 0, width: 0, height: 0 })
const scale = ref(0.1)
const offset = ref({ x: 0, y: 0 })

const updateMiniMap = () => {
  if (!props.mainStage) return

  const stage = props.mainStage
  const stageWidth = stage.width()
  const stageHeight = stage.height()
  const stageX = stage.x()
  const stageY = stage.y()
  const stageScale = stage.scaleX()

  let minX = -stageX / stageScale
  let minY = -stageY / stageScale
  let maxX = (-stageX + stageWidth) / stageScale
  let maxY = (-stageY + stageHeight) / stageScale

  store.entities.forEach((e) => {
    minX = Math.min(minX, e.position.x - 100)
    minY = Math.min(minY, e.position.y - 50)
    maxX = Math.max(maxX, e.position.x + 100)
    maxY = Math.max(maxY, e.position.y + 50)
  })

  store.relationships.forEach((r) => {
    minX = Math.min(minX, r.position.x - 100)
    minY = Math.min(minY, r.position.y - 50)
    maxX = Math.max(maxX, r.position.x + 100)
    maxY = Math.max(maxY, r.position.y + 50)
  })

  const contentWidth = maxX - minX
  const contentHeight = maxY - minY

  const availableWidth = miniMapWidth - 2 * padding
  const availableHeight = miniMapHeight - 2 * padding

  scale.value = Math.min(availableWidth / contentWidth, availableHeight / contentHeight)

  offset.value = {
    x: padding - minX * scale.value + (availableWidth - contentWidth * scale.value) / 2,
    y: padding - minY * scale.value + (availableHeight - contentHeight * scale.value) / 2,
  }

  viewportRect.value = {
    x: (-stageX / stageScale) * scale.value + offset.value.x,
    y: (-stageY / stageScale) * scale.value + offset.value.y,
    width: (stageWidth / stageScale) * scale.value,
    height: (stageHeight / stageScale) * scale.value,
  }
}

watchEffect(() => {
  updateMiniMap()
})

const handleMouseDown = (e: KonvaEventObject<MouseEvent>) => {
  if (!props.mainStage) return
  const stage = props.mainStage
  const miniStage = e.target.getStage()
  if (!miniStage) return
  const pos = miniStage.getPointerPosition()
  if (!pos) return

  const stageScale = stage.scaleX()

  // Center viewport on click
  const targetContentX = (pos.x - offset.value.x) / scale.value
  const targetContentY = (pos.y - offset.value.y) / scale.value

  stage.x(-targetContentX * stageScale + stage.width() / 2)
  stage.y(-targetContentY * stageScale + stage.height() / 2)
  stage.batchDraw()
}

onMounted(() => {
  if (props.mainStage) {
    props.mainStage.on('dragmove zoom xChange yChange scaleXChange', updateMiniMap)
  }
})

onUnmounted(() => {
  if (props.mainStage) {
    props.mainStage.off('dragmove zoom xChange yChange scaleXChange', updateMiniMap)
  }
})

// Simplified shapes for minimap
const miniEntities = computed(() =>
  store.entities.map((e: Entity) => ({
    x: e.position.x * scale.value + offset.value.x,
    y: e.position.y * scale.value + offset.value.y,
    width: (e.name.length < 8 ? 100 : e.name.length * 11) * scale.value,
    height: 40 * scale.value,
  })),
)

const miniRelationships = computed(() =>
  store.relationships.map((r: Relationship) => ({
    x: r.position.x * scale.value + offset.value.x,
    y: r.position.y * scale.value + offset.value.y,
    width: (r.name.length < 8 ? 80 : r.name.length * 10) * scale.value,
    height: 40 * scale.value,
  })),
)
</script>

<template>
  <div
    class="absolute bottom-4 right-4 border border-surface-200 dark:border-surface-700 bg-surface-0/80 dark:bg-surface-900/80 backdrop-blur-sm rounded-lg overflow-hidden shadow-lg select-none"
    :style="{ width: miniMapWidth + 'px', height: miniMapHeight + 'px' }"
  >
    <v-stage :config="{ width: miniMapWidth, height: miniMapHeight }" @mousedown="handleMouseDown">
      <v-layer>
        <!-- Background elements -->
        <v-rect
          v-for="(entity, index) in miniEntities"
          :key="'me-' + index"
          :config="{
            x: entity.x - entity.width / 2,
            y: entity.y - entity.height / 2,
            width: entity.width,
            height: entity.height,
            fill: '#ffcc45',
            cornerRadius: 2 * scale,
            opacity: 0.6,
          }"
        />
        <v-rect
          v-for="(rel, index) in miniRelationships"
          :key="'mr-' + index"
          :config="{
            x: rel.x - rel.width / 2,
            y: rel.y - rel.height / 2,
            width: rel.width,
            height: rel.height,
            fill: '#7ecbff',
            rotation: 45,
            offsetX: rel.width / 2,
            offsetY: rel.height / 2,
            opacity: 0.6,
          }"
        />

        <v-rect
          :config="{
            ...viewportRect,
            stroke: '#3b82f6',
            strokeWidth: 2,
            fill: '#3b82f622',
          }"
        />
      </v-layer>
    </v-stage>
  </div>
</template>
