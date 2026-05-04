<template>
  <div
    ref="container"
    class="w-full h-full relative overflow-hidden"
    @contextmenu.stop.prevent="onContextMenu"
  >
    <ContextMenu ref="cm" :model="menuModel" />
    <v-stage
      v-if="!isDiagramEmpty"
      ref="stageRef"
      :config="stageConfig"
      @mousedown="handleStageMouseDown"
      @mousemove="handleStageMouseMove"
      @mouseup="handleStageMouseUp"
      @touchstart="handleStageMouseDown"
      @wheel="handleWheel"
    >
      <v-layer>
        <!-- Connections -->
        <template
          v-for="connection in relationshipConnections"
          :key="connection.relId + '-' + connection.entityId"
        >
          <!-- Main Connection Line (IsA parent uses arrow) -->
          <v-arrow
            v-if="connection.isParent"
            :config="{
              points: [connection.startX, connection.startY, connection.endX, connection.endY],
              stroke: strokeColor,
              fill: strokeColor,
              strokeWidth: 1,
              pointerLength: 8,
              pointerWidth: 8,
              zIndex: -1,
            }"
          />
          <v-line
            v-else
            :config="{
              points: [
                connection.startX,
                connection.startY,
                connection.lineEndX,
                connection.lineEndY,
              ],
              stroke: strokeColor,
              strokeWidth: 1,
              zIndex: -1,
            }"
          />

          <!-- Arrowhead at entity end for classic-arrow mode when cardMax === '1' -->
          <v-line
            v-if="
              !connection.isParent &&
              cardinalityMode === 'classic-arrow' &&
              connection.cardMax === '1'
            "
            :config="{
              points: calculateArrowheadPoints(
                connection.startX,
                connection.startY,
                connection.endX,
                connection.endY,
                10,
                8,
                2,
              ),
              closed: true,
              fill: strokeColor,
              stroke: strokeColor,
              strokeWidth: 1,
              zIndex: -1,
            }"
          />

          <!-- Double line for Total Participation — only in classic modes -->
          <v-line
            v-if="connection.isTotal && cardinalityMode !== 'minmax'"
            :config="{
              points: calculateParallelPoints(
                connection.startX,
                connection.startY,
                connection.lineEndX,
                connection.lineEndY,
                4,
              ),
              stroke: strokeColor,
              strokeWidth: 1,
              zIndex: -1,
            }"
          />

          <!-- Label: (min,max) in minmax mode; max only in classic-number; nothing in classic-arrow -->
          <v-text
            v-if="connection.labelText"
            :config="{
              text: connection.labelText,
              x: (connection.startX + connection.endX) / 2,
              y: (connection.startY + connection.endY) / 2 - 20,
              fontSize: 14,
              fill: strokeColor,
              align: 'center',
            }"
          />
        </template>

        <template
          v-for="connection in attributeConnections"
          :key="connection.attributeId + '-' + connection.parentId"
        >
          <v-line
            :config="{
              points: [connection.startX, connection.startY, connection.endX, connection.endY],
              stroke: strokeColor,
              strokeWidth: 1,
              lineCap: 'round',
              lineJoin: 'round',
              tension: 0,
              zIndex: -1,
            }"
          />
        </template>

        <!-- Nodes -->
        <EntityNode
          v-for="entity in erSchemaStore.entities"
          :key="entity.id"
          :entity="entity"
          @dragmove="handleEntityDragMove"
        />
        <RelationshipNode
          v-for="relationship in erSchemaStore.relationships"
          :key="relationship.id"
          :relationship="relationship"
          @dragmove="handleRelationshipDragMove"
        />
        <AttributeNode
          v-for="attribute in erSchemaStore.attributes"
          :key="attribute.id"
          :attribute="attribute"
          @dragmove="handleAttributeDragMove"
        />
        <UndefinedNode
          v-for="undefinedEl in erSchemaStore.undefineds"
          :key="undefinedEl.id"
          :undefined-el="undefinedEl"
          @dragmove="handleUndefinedDragMove"
        />
        <!-- Rubber-band selection rect -->
        <v-rect
          v-if="rubberBand.visible"
          :config="{
            x: Math.min(rubberBand.x1, rubberBand.x2),
            y: Math.min(rubberBand.y1, rubberBand.y2),
            width: Math.abs(rubberBand.x2 - rubberBand.x1),
            height: Math.abs(rubberBand.y2 - rubberBand.y1),
            fill: 'rgba(99,102,241,0.15)',
            stroke: '#6366f1',
            strokeWidth: 1,
            dash: [4, 3],
          }"
        />
      </v-layer>
    </v-stage>
    <ContentUnavailableView
      v-else
      icon="bi bi-pencil-square"
      :title="t('schema.emptyDiagramTitle')"
      :message="t('schema.emptyDiagramMessage')"
      :actions="emptyDiagramActions"
    />
    <MiniMap v-if="mainStage && !isDiagramEmpty" :main-stage="mainStage" />
    <!-- Selection mode toggle -->
    <div class="absolute top-2 right-2 z-10">
      <Button
        :icon="selectionMode ? 'bi bi-cursor' : 'bi bi-arrows-move'"
        :severity="selectionMode ? 'primary' : 'secondary'"
        v-tooltip.left="selectionMode ? t('canvas.selectionMode') : t('canvas.dragMode')"
        text
        @click="toggleSelectionMode"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { KonvaEventObject, Node as KonvaNode } from 'konva/lib/Node'
import type { Shape as KonvaShape } from 'konva/lib/Shape'
import type { Stage } from 'konva/lib/Stage'
import ContextMenu from 'primevue/contextmenu'
import type { MenuItem } from 'primevue/menuitem'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCardinalityMode } from '@/composables/useCardinalityMode'
import { useTheme } from '@/composables/useTheme'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import type {
  Attribute,
  Entity,
  Position,
  Relationship,
  RelationshipParticipant,
} from '@/types/er-diagram-elements'
import {
  calculateArrowheadPoints,
  calculateParallelPoints,
  type EllipseShape,
  getLineDiamondIntersection,
  getLineEllipseIntersection,
  getLineRectangleIntersection,
  getLineTriangleIntersection,
  type RectShape,
} from '@/utils/geometry'

import ContentUnavailableView from '../ContentUnavailableView.vue'
import MiniMap from './MiniMap.vue'
import AttributeNode from './nodes/AttributeNode.vue'
import EntityNode from './nodes/EntityNode.vue'
import RelationshipNode from './nodes/RelationshipNode.vue'
import UndefinedNode from './nodes/UndefinedNode.vue'

const erSchemaStore = useErSchemaStore()
const dialogStore = useDialogStore()
const { t } = useI18n()
const { actualTheme } = useTheme()
const { cardinalityMode } = useCardinalityMode()
const strokeColor = computed(() => (actualTheme.value === 'dark' ? '#e5e7eb' : 'black'))

const isDiagramEmpty = computed(() => {
  return (
    erSchemaStore.entities.length === 0 &&
    erSchemaStore.relationships.length === 0 &&
    erSchemaStore.attributes.length === 0 &&
    erSchemaStore.undefineds.length === 0
  )
})

const emptyDiagramActions = computed(() => [
  {
    label: t('entity.addEntity'),
    icon: 'bi bi-plus-square',
    onClick: () => dialogStore.open(DialogId.AddEntity),
  },
  {
    label: t('relationship.addRelationship'),
    icon: 'bi bi-diamond',
    severity: 'secondary' as const,
    onClick: () => dialogStore.open(DialogId.AddRelationship),
  },
])

const container = ref<HTMLDivElement | null>(null)
interface KonvaStageComponent {
  getStage: () => Stage
}
const stageRef = ref<KonvaStageComponent | null>(null)
const mainStage = ref<Stage | null>(null)
const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: true,
})

const selectionMode = ref(false)

const toggleSelectionMode = () => {
  selectionMode.value = !selectionMode.value
  stageConfig.draggable = !selectionMode.value
  if (!selectionMode.value) erSchemaStore.selectElement(null)
}

const cm = ref()
const menuModel = ref<MenuItem[]>([])

const handleWheel = (e: KonvaEventObject<WheelEvent>) => {
  e.evt.preventDefault()
  const stage = e.target.getStage()
  if (!stage) return

  const oldScale = stage.scaleX()
  const pointer = stage.getPointerPosition()

  if (pointer) {
    const mousePointTo = {
      x: (pointer.x - stage.x()) / oldScale,
      y: (pointer.y - stage.y()) / oldScale,
    }

    const scaleBy = 1.05
    const newScale = e.evt.deltaY < 0 ? oldScale * scaleBy : oldScale / scaleBy

    stage.scale({ x: newScale, y: newScale })

    const newPos = {
      x: pointer.x - mousePointTo.x * newScale,
      y: pointer.y - mousePointTo.y * newScale,
    }
    stage.position(newPos)
    stage.batchDraw()
  }
}

const rubberBand = reactive({ visible: false, x1: 0, y1: 0, x2: 0, y2: 0 })

const getStagePos = (e: KonvaEventObject<MouseEvent>) => {
  const stage = e.target.getStage()!
  const p = stage.getPointerPosition()!
  const t = stage.getAbsoluteTransform().copy().invert()
  return t.point(p)
}

const handleStageMouseDown = (e: KonvaEventObject<MouseEvent>) => {
  if (e.evt.button !== 0) return
  if (e.target !== e.target.getStage()) return
  if (!selectionMode.value) {
    erSchemaStore.selectElement(null)
    return
  }
  if (!e.evt.shiftKey) erSchemaStore.selectElement(null)
  const pos = getStagePos(e)
  rubberBand.x1 = pos.x
  rubberBand.y1 = pos.y
  rubberBand.x2 = pos.x
  rubberBand.y2 = pos.y
  rubberBand.visible = true
}

const handleStageMouseMove = (e: KonvaEventObject<MouseEvent>) => {
  if (!rubberBand.visible) return
  const pos = getStagePos(e)
  rubberBand.x2 = pos.x
  rubberBand.y2 = pos.y
}

const handleStageMouseUp = () => {
  if (!rubberBand.visible) return
  rubberBand.visible = false

  const rx1 = Math.min(rubberBand.x1, rubberBand.x2)
  const rx2 = Math.max(rubberBand.x1, rubberBand.x2)
  const ry1 = Math.min(rubberBand.y1, rubberBand.y2)
  const ry2 = Math.max(rubberBand.y1, rubberBand.y2)

  // Only select if the rect has meaningful size
  if (rx2 - rx1 < 4 && ry2 - ry1 < 4) return

  const inside = (x: number, y: number) => x >= rx1 && x <= rx2 && y >= ry1 && y <= ry2

  const ids: string[] = []
  erSchemaStore.entities.forEach((el) => {
    if (inside(el.position.x, el.position.y)) ids.push(el.id)
  })
  erSchemaStore.relationships.forEach((el) => {
    if (inside(el.position.x, el.position.y)) ids.push(el.id)
  })
  erSchemaStore.attributes.forEach((el) => {
    if (inside(el.position.x, el.position.y)) ids.push(el.id)
  })
  erSchemaStore.undefineds.forEach((el) => {
    if (inside(el.position.x, el.position.y)) ids.push(el.id)
  })

  if (ids.length > 0) erSchemaStore.selectElements(ids)
}

const getContextMenuItems = () => {
  const selectedId = erSchemaStore.selectedElementId
  if (!selectedId) {
    return [
      {
        label: t('entity.addEntity'),
        icon: 'bi bi-square',
        command: () => dialogStore.open(DialogId.AddEntity),
      },
      {
        label: t('relationship.addRelationship'),
        icon: 'bi bi-diamond',
        command: () => dialogStore.open(DialogId.AddRelationship),
      },
      {
        label: t('panels.insertIsARelationship'),
        icon: 'bi bi-diagram-2',
        command: () => dialogStore.open(DialogId.AddIsARelationship),
      },
      {
        label: t('panels.createDomain'),
        icon: 'bi bi-collection',
        command: () => dialogStore.open(DialogId.AddDomain),
      },
    ]
  }

  const isEntity = erSchemaStore.entities.some((e) => e.id === selectedId)
  const isRelationship = erSchemaStore.relationships.some((r) => r.id === selectedId)
  const isAttribute = erSchemaStore.attributes.some((a) => a.id === selectedId)
  const isUndefined = erSchemaStore.undefineds.some((u) => u.id === selectedId)

  if (isEntity) {
    return [
      {
        label: t('attribute.addAttribute'),
        icon: 'bi bi-circle',
        command: () => dialogStore.open(DialogId.AddAttribute),
      },
      {
        label: t('panels.insertIsARelationship'),
        icon: 'bi bi-diagram-2',
        command: () => dialogStore.open(DialogId.AddIsARelationship),
      },
      {
        label: t('entity.editEntity'),
        icon: 'bi bi-pencil-square',
        command: () => dialogStore.open(DialogId.EditEntity),
      },
      { separator: true },
      {
        label: t('common.delete'),
        icon: 'bi bi-trash',
        class: 'text-red-500',
        command: () => erSchemaStore.deleteElement(selectedId),
      },
    ]
  }

  if (isRelationship) {
    const rel = erSchemaStore.relationships.find((r) => r.id === selectedId)
    const editDialogId =
      rel?.type === 'IsA' ? DialogId.EditIsARelationship : DialogId.EditRelationship
    return [
      {
        label: t('attribute.addAttribute'),
        icon: 'bi bi-circle',
        command: () => dialogStore.open(DialogId.AddAttribute),
      },
      {
        label: t('entity.addEntity'),
        icon: 'bi bi-plus-circle',
        command: () => dialogStore.open(DialogId.AddEntityToRelationship),
      },
      {
        label: t('relationship.editRelationship'),
        icon: 'bi bi-pencil-square',
        command: () => dialogStore.open(editDialogId),
      },
      { separator: true },
      {
        label: t('common.delete'),
        icon: 'bi bi-trash',
        class: 'text-red-500',
        command: () => erSchemaStore.deleteElement(selectedId),
      },
    ]
  }

  if (isAttribute) {
    return [
      {
        label: t('attribute.addAttribute'),
        icon: 'bi bi-plus-circle',
        command: () => dialogStore.open(DialogId.AddAttribute),
      },
      {
        label: t('attribute.editAttribute'),
        icon: 'bi bi-pencil-square',
        command: () => dialogStore.open(DialogId.EditAttribute),
      },
      { separator: true },
      {
        label: t('common.delete'),
        icon: 'bi bi-trash',
        class: 'text-red-500',
        command: () => erSchemaStore.deleteElement(selectedId),
      },
    ]
  }

  if (isUndefined) {
    return [
      {
        label: t('entity.convertToEntity', 'Convertir a Entidad'),
        icon: 'bi bi-square',
        command: () => erSchemaStore.convertUndefinedToEntity(selectedId),
      },
      {
        label: t('relationship.convertToRelationship', 'Convertir a Relación'),
        icon: 'bi bi-diamond',
        command: () => erSchemaStore.convertUndefinedToRelationship(selectedId),
      },
      { separator: true },
      {
        label: t('common.delete'),
        icon: 'bi bi-trash',
        class: 'text-red-500',
        command: () => erSchemaStore.deleteElement(selectedId),
      },
    ]
  }

  return []
}

const onContextMenu = (event: MouseEvent) => {
  if (!stageRef.value) return

  if (cm.value) cm.value.hide()

  const stage = stageRef.value.getStage()
  const pointerPosition = stage.getPointerPosition()

  if (pointerPosition) {
    const transform = stage.getAbsoluteTransform().copy().invert()
    const pos = transform.point(pointerPosition)
    erSchemaStore.setLastClickPosition({ x: pos.x, y: pos.y })

    const intersectedShape = stage.getIntersection(pointerPosition) as KonvaShape | null

    if (intersectedShape) {
      let node: KonvaNode | null = intersectedShape
      while (node && !node.id() && node.getParent()) {
        node = node.getParent()
      }
      if (node && node.id()) {
        erSchemaStore.selectElement(node.id())
      }
    } else {
      erSchemaStore.selectElement(null)
    }
  }

  menuModel.value = getContextMenuItems()

  nextTick(() => {
    cm.value.show(event)
  })
}

const calculateEntityRenderProps = (entity: Entity): RectShape => {
  const width = entity.name.length < 8 ? 100 : entity.name.length * 11
  const height = 40
  const x = entity.position.x
  const y = entity.position.y
  return { x: x - width / 2, y: y - height / 2, width, height }
}

const calculateAttributeRenderProps = (attribute: Attribute): EllipseShape => {
  const rx = attribute.name.length < 8 ? 50 : attribute.name.length * 5.5
  const ry = 25
  const cx = attribute.position.x
  const cy = attribute.position.y
  return { cx, cy, rx, ry }
}

const calculateRelationshipRenderProps = (
  relationship: Relationship,
): { cx: number; cy: number; width: number; height: number; type: string } => {
  const width = relationship.name.length < 8 ? 100 : relationship.name.length * 11
  const height = 50
  return {
    cx: relationship.position.x,
    cy: relationship.position.y,
    width,
    height,
    type: relationship.type,
  }
}

interface RelationshipConnection {
  relId: string
  entityId: string
  startX: number
  startY: number
  endX: number
  endY: number
  lineEndX: number
  lineEndY: number
  cardMax: string
  labelText: string
  isTotal: boolean
  isParent: boolean
}

const relationshipConnections = computed(() => {
  const connections: RelationshipConnection[] = []
  erSchemaStore.relationships.forEach((rel: Relationship) => {
    rel.participants.forEach((participant: RelationshipParticipant) => {
      const entity = erSchemaStore.entities.find((e) => e.id === participant.entityId)
      if (entity) {
        const relShape = calculateRelationshipRenderProps(rel)
        const entityShape = calculateEntityRenderProps(entity)
        const relCenter = { x: relShape.cx, y: relShape.cy }
        const entityCenter = {
          x: entityShape.x + entityShape.width / 2,
          y: entityShape.y + entityShape.height / 2,
        }

        let startPoint: Position | null = null
        if (rel.type === 'IsA') {
          const isParent = participant.role === 'Parent'
          const { cx, cy, size } = { cx: relShape.cx, cy: relShape.cy, size: 50 }
          if (isParent) {
            startPoint = { x: cx, y: cy - size / 2 }
          } else {
            startPoint = getLineTriangleIntersection(entityCenter, relCenter, { cx, cy, size })
          }
        } else {
          startPoint = getLineDiamondIntersection(entityCenter, relCenter, relShape)
        }

        const endPoint = getLineRectangleIntersection(relCenter, entityCenter, entityShape)
        if (startPoint && endPoint) {
          const minCard = participant.cardinalityMin
          const maxCard = participant.cardinalityMax
          const isIsA = rel.type === 'IsA'

          let labelText = ''
          if (!isIsA) {
            if (cardinalityMode.value === 'minmax') {
              labelText = `(${minCard},${maxCard})`
            } else if (cardinalityMode.value === 'classic-number') {
              labelText = maxCard
            }
            // classic-arrow: no label
          }

          connections.push({
            relId: rel.id,
            entityId: entity.id,
            startX: startPoint.x,
            startY: startPoint.y,
            endX: endPoint.x,
            endY: endPoint.y,
            lineEndX: (() => {
              if (!isIsA && cardinalityMode.value === 'classic-arrow' && maxCard === '1') {
                const dx = endPoint.x - startPoint.x
                const dy = endPoint.y - startPoint.y
                const d = Math.sqrt(dx * dx + dy * dy)
                return d === 0 ? endPoint.x : endPoint.x - (dx / d) * 10
              }
              return endPoint.x
            })(),
            lineEndY: (() => {
              if (!isIsA && cardinalityMode.value === 'classic-arrow' && maxCard === '1') {
                const dx = endPoint.x - startPoint.x
                const dy = endPoint.y - startPoint.y
                const d = Math.sqrt(dx * dx + dy * dy)
                return d === 0 ? endPoint.y : endPoint.y - (dy / d) * 10
              }
              return endPoint.y
            })(),
            cardMax: maxCard,
            labelText,
            isTotal: !isIsA && minCard !== '' && parseInt(minCard) >= 1,
            isParent: isIsA && participant.role === 'Parent',
          })
        }
      }
    })
  })
  return connections
})

interface AttributeConnection {
  attributeId: string
  parentId: string
  startX: number
  startY: number
  endX: number
  endY: number
}

const attributeConnections = computed(() => {
  const connections: AttributeConnection[] = []
  erSchemaStore.attributes.forEach((attribute: Attribute) => {
    const parentEntity = erSchemaStore.entities.find((e: Entity) => e.id === attribute.parentId)
    const parentRelationship = erSchemaStore.relationships.find(
      (r: Relationship) => r.id === attribute.parentId,
    )
    const parentAttr = erSchemaStore.attributes.find((a: Attribute) => a.id === attribute.parentId)
    const parentUndefined = erSchemaStore.undefineds.find((u) => u.id === attribute.parentId)

    const parent = parentEntity || parentRelationship || parentAttr || parentUndefined
    if (!parent) return

    const attributeShape = calculateAttributeRenderProps(attribute)
    const attributeCenter = { x: attributeShape.cx, y: attributeShape.cy }

    let parentCenter: Position
    let endPoint: Position | null = null

    if (parentRelationship) {
      const relShape = calculateRelationshipRenderProps(parentRelationship)
      parentCenter = { x: relShape.cx, y: relShape.cy }
      endPoint = getLineDiamondIntersection(attributeCenter, parentCenter, relShape)
    } else if (parentEntity) {
      const entityShape = calculateEntityRenderProps(parentEntity)
      parentCenter = {
        x: entityShape.x + entityShape.width / 2,
        y: entityShape.y + entityShape.height / 2,
      }
      endPoint = getLineRectangleIntersection(attributeCenter, parentCenter, entityShape)
    } else if (parentUndefined) {
      const undefinedShape: EllipseShape = {
        cx: parentUndefined.position.x,
        cy: parentUndefined.position.y,
        rx: 30,
        ry: 30,
      }
      parentCenter = { x: undefinedShape.cx, y: undefinedShape.cy }
      endPoint = getLineEllipseIntersection(attributeCenter, parentCenter, undefinedShape)
    } else {
      const parentAttrShape = calculateAttributeRenderProps(parentAttr as Attribute)
      parentCenter = { x: parentAttrShape.cx, y: parentAttrShape.cy }
      endPoint = getLineEllipseIntersection(attributeCenter, parentCenter, parentAttrShape)
    }

    const startPoint = getLineEllipseIntersection(parentCenter!, attributeCenter, attributeShape)

    if (startPoint && endPoint) {
      connections.push({
        attributeId: attribute.id,
        parentId: attribute.parentId,
        startX: startPoint.x,
        startY: startPoint.y,
        endX: endPoint.x,
        endY: endPoint.y,
      })
    }
  })
  return connections
})

const handleEntityDragMove = (event: { id: string; x: number; y: number }) => {
  erSchemaStore.updateEntityPosition(event.id, { x: event.x, y: event.y })
}
const handleAttributeDragMove = (event: { id: string; x: number; y: number }) => {
  erSchemaStore.updateAttributePosition(event.id, { x: event.x, y: event.y })
}
const handleRelationshipDragMove = (event: { id: string; x: number; y: number }) => {
  erSchemaStore.updateRelationshipPosition(event.id, { x: event.x, y: event.y })
}
const handleUndefinedDragMove = (event: { id: string; x: number; y: number }) => {
  const u = erSchemaStore.undefineds.find((u) => u.id === event.id)
  if (u) u.position = { x: event.x, y: event.y }
}

function handleKeydown(e: KeyboardEvent) {
  if (['INPUT', 'TEXTAREA'].includes((e.target as HTMLElement).tagName)) return
  if (e.key === 'Backspace' || e.key === 'Delete') {
    erSchemaStore.deleteSelected()
  }
}

onMounted(() => {
  if (stageRef.value) {
    mainStage.value = stageRef.value.getStage()
    erSchemaStore.stageRef = mainStage.value
  }
  window.addEventListener('keydown', handleKeydown)
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

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>
