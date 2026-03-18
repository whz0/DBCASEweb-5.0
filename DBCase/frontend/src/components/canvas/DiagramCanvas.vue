<template>
  <div 
    ref="container" 
    class="h-full relative" 
    style="width: 100%;" 
    @contextmenu.stop.prevent="onContextMenu"
  >
    <ContextMenu ref="cm" :model="menuModel" />
    <v-stage 
      ref="stageRef"
      :config="stageConfig" 
      @mousedown="handleStageMouseDown" 
      @touchstart="handleStageMouseDown"
    >
      <v-layer>
        <!-- Connections -->
        <template v-for="connection in relationshipConnections" :key="connection.relId + '-' + connection.entityId">
          <!-- Main Connection Line -->
          <v-line :config="{
            points: [connection.startX, connection.startY, connection.endX, connection.endY],
            stroke: 'black',
            strokeWidth: 1,
            zIndex: -1
          }" />
          
          <!-- Double line for Total Participation (min >= 1) -->
          <v-line v-if="connection.isTotal" :config="{
            points: calculateParallelPoints(connection.startX, connection.startY, connection.endX, connection.endY, 4),
            stroke: 'black',
            strokeWidth: 1,
            zIndex: -1
          }" />

          <v-text :config="{
            text: connection.card,
            x: (connection.startX + connection.endX) / 2,
            y: (connection.startY + connection.endY) / 2 - 20,
            fontSize: 14,
            fill: 'black',
            align: 'center'
          }" />
        </template>
        
        <template v-for="connection in attributeConnections" :key="connection.attributeId + '-' + connection.parentId">
          <v-line :config="{
            points: [connection.startX, connection.startY, connection.endX, connection.endY],
            stroke: 'black',
            strokeWidth: 1,
            lineCap: 'round',
            lineJoin: 'round',
            tension: 0,
            zIndex: -1
          }" />
        </template>
        
        <!-- Nodes -->
        <EntityNode v-for="entity in store.entities" :key="entity.id" :entity="entity" @dragmove="handleEntityDragMove" />
        <RelationshipNode v-for="relationship in store.relationships" :key="relationship.id" :relationship="relationship" @dragmove="handleRelationshipDragMove" />
        <AttributeNode v-for="attribute in store.attributes" :key="attribute.id" :attribute="attribute" @dragmove="handleAttributeDragMove" />
      </v-layer>
    </v-stage>
  </div>
</template>

<script setup lang="ts">
import { useDiagramStore } from '@/stores/diagramStore'
import { useDialogStore, DialogId } from '@/stores/dialogStore'
import EntityNode from './nodes/EntityNode.vue'
import RelationshipNode from './nodes/RelationshipNode.vue'
import AttributeNode from './nodes/AttributeNode.vue'
import { onMounted, onUnmounted, ref, reactive, computed, nextTick } from 'vue'
import type { Attribute, Entity, Position, Relationship } from '@/types/er-diagram-elements'
import ContextMenu from 'primevue/contextmenu'
import { useI18n } from 'vue-i18n'

const store = useDiagramStore()
const dialogStore = useDialogStore()
const { t } = useI18n()

const container = ref<HTMLDivElement | null>(null)
const stageRef = ref<any>(null)
const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: true
})

const cm = ref();
const menuModel = ref<any[]>([]);

const handleStageMouseDown = (e: any) => {
  // Only clear selection on left click on the stage background
  if (e.evt.button === 0 && e.target === e.target.getStage()) {
    store.selectElement(null);
  }
}

const getContextMenuItems = () => {
  const selectedId = store.selectedElementId;
  if (!selectedId) {
    return [
      { label: t('entity.addEntity'), icon: 'bi bi-square', command: () => dialogStore.open(DialogId.AddEntity) },
      { label: t('relationship.addRelationship'), icon: 'bi bi-diamond', command: () => dialogStore.open(DialogId.AddRelationship) },
      { label: t('panels.insertIsARelationship'), icon: 'bi bi-diagram-2', command: () => dialogStore.open(DialogId.AddIsARelationship) },
    ]
  }

  const isEntity = store.entities.some(e => e.id === selectedId);
  const isRelationship = store.relationships.some(r => r.id === selectedId);
  const isAttribute = store.attributes.some(a => a.id === selectedId);

  if (isEntity) {
    return [
      { label: t('attribute.addAttribute'), icon: 'bi bi-circle', command: () => dialogStore.open(DialogId.AddAttribute) },
      { label: t('panels.insertIsARelationship'), icon: 'bi bi-diagram-2', command: () => dialogStore.open(DialogId.AddIsARelationship) },
      { label: t('common.rename'), icon: 'bi bi-pencil', command: () => dialogStore.open(DialogId.Rename) },
      { separator: true },
      { label: t('common.delete'), icon: 'bi bi-trash', command: () => store.deleteElement(selectedId) }
    ]
  }

  if (isRelationship) {
    return [
      { label: t('entity.addEntity'), icon: 'bi bi-plus-circle', command: () => dialogStore.open(DialogId.AddEntityToRelationship) },
      { label: t('common.rename'), icon: 'bi bi-pencil', command: () => dialogStore.open(DialogId.Rename) },
      { label: t('common.delete'), icon: 'bi bi-trash', command: () => store.deleteElement(selectedId) }
    ]
  }

  if (isAttribute) {
    return [
      { label: t('attribute.addAttribute'), icon: 'bi bi-plus-circle', command: () => dialogStore.open(DialogId.AddAttribute) },
      { label: t('common.rename'), icon: 'bi bi-pencil', command: () => dialogStore.open(DialogId.Rename) },
      { label: t('common.delete'), icon: 'bi bi-trash', command: () => store.deleteElement(selectedId) }
    ]
  }

  return [];
}

const onContextMenu = (event: MouseEvent) => {
  if (!stageRef.value) return;
  
  // Hide current menu if any to prevent duplication/flicker
  if (cm.value) cm.value.hide();

  const stage = stageRef.value.getStage();
  const pointerPosition = stage.getPointerPosition();
  
  if (pointerPosition) {
    const intersectedShape = stage.getIntersection(pointerPosition);
    
    if (intersectedShape && intersectedShape !== stage) {
      let node = intersectedShape;
      while (node && !node.attrs.id && node.parent) {
        node = node.parent;
      }
      if (node && node.attrs.id) {
        store.selectElement(node.attrs.id);
      }
    } else {
      store.selectElement(null);
    }
  }

  // Set the menu items based on the NEW selection state immediately
  menuModel.value = getContextMenuItems();

  nextTick(() => {
    cm.value.show(event);
  });
}

// Helper functions for geometry
interface RectShape {
  x: number; // Top-left x
  y: number; // Top-left y
  width: number;
  height: number;
}

interface EllipseShape {
  cx: number; // Center x
  cy: number; // Center y
  rx: number; // Radius x
  ry: number; // Radius y
}

// Calculates Konva rectangle config for an entity's bounding box
const calculateEntityRenderProps = (entity: Entity): RectShape => {
  const width = entity.name.length < 8 ? 100 : entity.name.length * 11;
  const height = 40;
  const x = entity.position.x;
  const y = entity.position.y;
  return { x: x - width / 2, y: y - height / 2, width, height };
};

// Calculates Konva ellipse config for an attribute's bounding box
const calculateAttributeRenderProps = (attribute: Attribute): EllipseShape => {
  const rx = attribute.name.length < 8 ? 50 : attribute.name.length * 5.5;
  const ry = 25;
  const cx = attribute.position.x;
  const cy = attribute.position.y;
  return { cx, cy, rx, ry };
};

// Robust function to find intersection of a line segment (p1->p2) with a rectangle
const getLineRectangleIntersection = (p1: Position, p2: Position, rect: RectShape): Position | null => {
  const { x: rectX, y: rectY, width: rectW, height: rectH } = rect;
  const dx = p2.x - p1.x;
  const dy = p2.y - p1.y;

  const t = { x: 0, y: 0 };
  let tmin = -Infinity;
  let tmax = Infinity;

  if (dx === 0) {
    if (p1.x < rectX || p1.x > rectX + rectW) return null;
  } else {
    t.x = (rectX - p1.x) / dx;
    const tx2 = (rectX + rectW - p1.x) / dx;
    tmin = Math.max(tmin, Math.min(t.x, tx2));
    tmax = Math.min(tmax, Math.max(t.x, tx2));
  }

  if (dy === 0) {
    if (p1.y < rectY || p1.y > rectY + rectH) return null;
  } else {
    t.y = (rectY - p1.y) / dy;
    const ty2 = (rectY + rectH - p1.y) / dy;
    tmin = Math.max(tmin, Math.min(t.y, ty2));
    tmax = Math.min(tmax, Math.max(t.y, ty2));
  }

  if (tmin > tmax || tmax < 0) return null;
  if (tmin >= 0 && tmin <= 1) return { x: p1.x + tmin * dx, y: p1.y + tmin * dy };
  return null;
};

// Finds intersection of a line segment (p1->p2) with an ellipse
const getLineEllipseIntersection = (p1: Position, p2: Position, ellipse: EllipseShape): Position | null => {
  const { cx, cy, rx, ry } = ellipse;
  const p1t = { x: p1.x - cx, y: p1.y - cy };
  const p2t = { x: p2.x - cx, y: p2.y - cy };
  const dx = p2t.x - p1t.x;
  const dy = p2t.y - p1t.y;
  const scaleX = rx;
  const scaleY = ry;
  const p1n = { x: p1t.x / scaleX, y: p1t.y / scaleY };
  const p2n = { x: p2t.x / scaleX, y: p2t.y / scaleY };
  const dxn = p2n.x - p1n.x;
  const dyn = p2n.y - p1n.y;

  const a = dxn * dxn + dyn * dyn;
  const b = 2 * (p1n.x * dxn + p1n.y * dyn);
  const c = p1n.x * p1n.x + p1n.y * p1n.y - 1;
  const discriminant = b * b - 4 * a * c;

  if (discriminant < 0) return null;

  const t_values = [];
  const sqrt_discriminant = Math.sqrt(discriminant);
  const t1 = (-b + sqrt_discriminant) / (2 * a);
  const t2 = (-b - sqrt_discriminant) / (2 * a);
  if (t1 >= 0 && t1 <= 1) t_values.push(t1);
  if (t2 >= 0 && t2 <= 1) t_values.push(t2);
  if (t_values.length === 0) return null;

  let closest_t = t_values[0] as number;
  if (t_values.length > 1 && (t_values[1] as number) < (t_values[0] as number)) {
    closest_t = t_values[1] as number;
  }

  return {
    x: (p1n.x + closest_t * dxn) * scaleX + cx,
    y: (p1n.y + closest_t * dyn) * scaleY + cy,
  };
};

const getLineDiamondIntersection = (p1: Position, p2: Position, diamond: { cx: number, cy: number, width: number, height: number }): Position | null => {
  const { cx, cy, width, height } = diamond;
  const halfW = width / 2;
  const halfH = height / 2;
  const v = [
    { x: cx, y: cy - halfH }, { x: cx + halfW, y: cy },
    { x: cx, y: cy + halfH }, { x: cx - halfW, y: cy }
  ];
  const sides = [[v[0], v[1]], [v[1], v[2]], [v[2], v[3]], [v[3], v[0]]];
  for (const side of sides) {
    const intersect = getLineSegmentIntersection(p1, p2, side[0] as Position, side[1] as Position);
    if (intersect) return intersect;
  }
  return null;
};

const getLineSegmentIntersection = (p1: Position, p2: Position, p3: Position, p4: Position): Position | null => {
  const det = (p2.x - p1.x) * (p4.y - p3.y) - (p2.y - p1.y) * (p4.x - p3.x);
  if (det === 0) return null;
  const ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / det;
  const ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / det;
  if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
    return { x: p1.x + ua * (p2.x - p1.x), y: p1.y + ua * (p2.y - p1.y) };
  }
  return null;
};

const getLineTriangleIntersection = (p1: Position, p2: Position, triangle: { cx: number, cy: number, size: number }): Position | null => {
  const { cx, cy, size } = triangle;
  const half = size / 2;
  const v = [
    { x: cx, y: cy - half }, // Top
    { x: cx + half, y: cy + half }, // Bottom Right
    { x: cx - half, y: cy + half }  // Bottom Left
  ];
  const sides = [[v[0], v[1]], [v[1], v[2]], [v[2], v[0]]];
  for (const side of sides) {
    const intersect = getLineSegmentIntersection(p1, p2, side[0] as Position, side[1] as Position);
    if (intersect) return intersect;
  }
  return null;
};

const calculateRelationshipRenderProps = (relationship: Relationship): { cx: number, cy: number, width: number, height: number, type: string } => {
  const width = relationship.name.length < 8 ? 100 : relationship.name.length * 11;
  const height = 50;
  return { cx: relationship.position.x, cy: relationship.position.y, width, height, type: relationship.type };
};

const calculateParallelPoints = (x1: number, y1: number, x2: number, y2: number, offset: number) => {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const length = Math.sqrt(dx * dx + dy * dy);
  if (length === 0) return [x1, y1, x2, y2];
  
  const nx = -dy / length;
  const ny = dx / length;
  
  return [
    x1 + nx * offset,
    y1 + ny * offset,
    x2 + nx * offset,
    y2 + ny * offset
  ];
};

const relationshipConnections = computed(() => {
  const connections: { relId: string; entityId: string; startX: number; startY: number; endX: number; endY: number; card: string; isTotal: boolean }[] = [];
  store.relationships.forEach((rel: Relationship) => {
    rel.participants.forEach((participant: any) => {
      const entity = store.entities.find(e => e.id === participant.entityId);
      if (entity) {
        const relShape = calculateRelationshipRenderProps(rel);
        const entityShape = calculateEntityRenderProps(entity);
        const relCenter = { x: relShape.cx, y: relShape.cy };
        const entityCenter = { x: entityShape.x + entityShape.width / 2, y: entityShape.y + entityShape.height / 2 };

        let startPoint: Position | null = null;
        if (rel.type === 'IsA') {
          const isParent = participant.role === 'Parent'
          const { cx, cy, size } = { cx: relShape.cx, cy: relShape.cy, size: 50 }
          if (isParent) {
            startPoint = { x: cx, y: cy - size / 2 }
          } else {
            // Connect to bottom edge
            // A bit more complex, but for now let's just use the bottom center or triangle intersection
            startPoint = getLineTriangleIntersection(entityCenter, relCenter, { cx, cy, size });
          }
        } else {
          startPoint = getLineDiamondIntersection(entityCenter, relCenter, relShape);
        }

        const endPoint = getLineRectangleIntersection(relCenter, entityCenter, entityShape);
        if (startPoint && endPoint) {
          const minCard = participant.cardinalityMin;
          connections.push({ 
            relId: rel.id, 
            entityId: entity.id, 
            startX: startPoint.x, 
            startY: startPoint.y, 
            endX: endPoint.x, 
            endY: endPoint.y, 
            card: rel.type === 'IsA' ? '' : `(${participant.cardinalityMin},${participant.cardinalityMax})`,
            isTotal: minCard !== '' && parseInt(minCard) >= 1
          });
        }
      }
    });
  });
  return connections;
});

const attributeConnections = computed(() => {
  const connections: { attributeId: string; parentId: string; startX: number; startY: number; endX: number; endY: number }[] = [];
  store.attributes.forEach((attribute: Attribute) => {
    // Try to find parent entity or parent attribute
    const parentEntity = store.entities.find((entity: Entity) => entity.id === attribute.parentId);
    const parentAttr = store.attributes.find((attr: Attribute) => attr.id === attribute.parentId);
    
    if (parentEntity || parentAttr) {
      const attributeShape = calculateAttributeRenderProps(attribute);
      const parentShape = parentEntity 
        ? calculateEntityRenderProps(parentEntity) 
        : calculateAttributeRenderProps(parentAttr!);

      const attributeCenter = { x: attributeShape.cx, y: attributeShape.cy };
      const parentCenter = 'x' in parentShape 
        ? { x: (parentShape as RectShape).x + (parentShape as RectShape).width / 2, y: (parentShape as RectShape).y + (parentShape as RectShape).height / 2 }
        : { x: (parentShape as EllipseShape).cx, y: (parentShape as EllipseShape).cy };

      const startPoint = getLineEllipseIntersection(parentCenter, attributeCenter, attributeShape);
      let endPoint: Position | null = null;
      
      if ('x' in parentShape) {
        endPoint = getLineRectangleIntersection(attributeCenter, parentCenter, parentShape as RectShape);
      } else {
        endPoint = getLineEllipseIntersection(attributeCenter, parentCenter, parentShape as EllipseShape);
      }

      if (startPoint && endPoint) {
        connections.push({ attributeId: attribute.id, parentId: attribute.parentId, startX: startPoint.x, startY: startPoint.y, endX: endPoint.x, endY: endPoint.y });
      } else {
        connections.push({ attributeId: attribute.id, parentId: attribute.parentId, startX: attribute.position.x, startY: attribute.position.y, endX: parentEntity?.position.x || parentAttr?.position.x || 0, endY: parentEntity?.position.y || parentAttr?.position.y || 0 });
      }
    }
  });
  return connections;
});

const handleEntityDragMove = (event: { id: string; x: number; y: number }) => {
  store.updateEntityPosition(event.id, { x: event.x, y: event.y });
};
const handleAttributeDragMove = (event: { id: string; x: number; y: number }) => {
  store.updateAttributePosition(event.id, { x: event.x, y: event.y });
};
const handleRelationshipDragMove = (event: { id: string; x: number; y: number }) => {
  store.updateRelationshipPosition(event.id, { x: event.x, y: event.y });
};

onMounted(() => {
  if (container.value) {
    const updateStageSize = () => {
      if (container.value) {
        stageConfig.width = container.value.offsetWidth
        stageConfig.height = container.value.offsetHeight
      }
    }
    const resizeObserver = new ResizeObserver(() => { updateStageSize() })
    resizeObserver.observe(container.value)
    onUnmounted(() => { resizeObserver.disconnect() })
    updateStageSize()
  }
})
</script>
