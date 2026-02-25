
<template>
  <div ref="container" class="h-full" style="width: 100%;">
    <v-stage :config="stageConfig">
      <v-layer>
        <EntityNode v-for="entity in store.entities" :key="entity.id" :entity="entity" />
        <RelationshipNode v-for="relationship in store.relationships" :key="relationship.id" :relationship="relationship" />
        <AttributeNode v-for="attribute in store.attributes" :key="attribute.id" :attribute="attribute" />
      </v-layer>
    </v-stage>
  </div>
</template>

<script setup lang="ts">
import { useDiagramStore } from '@/stores/diagramStore'
import EntityNode from './nodes/EntityNode.vue'
import RelationshipNode from './nodes/RelationshipNode.vue'
import AttributeNode from './nodes/AttributeNode.vue'
import { onMounted, onUnmounted, ref, reactive } from 'vue'

const store = useDiagramStore()

const container = ref<HTMLDivElement | null>(null)
const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: true
})

onMounted(() => {
  if (container.value) {
    const updateStageSize = () => {
      if (container.value) {
        stageConfig.width = container.value.offsetWidth
        stageConfig.height = container.value.offsetHeight
      }
    }

    const resizeObserver = new ResizeObserver(() => {
      updateStageSize()
    })

    resizeObserver.observe(container.value)

    onUnmounted(() => {
      resizeObserver.disconnect()
    })
    
    updateStageSize()
  }
})
</script>
