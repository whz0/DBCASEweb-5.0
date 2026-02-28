<template>
  <div ref="container" class="h-full" style="width: 100%;">
    <v-stage :config="stageConfig">
      <v-layer>
        <!-- Render nodes first -->
        <EntityNode v-for="entity in store.entities" :key="entity.id" :entity="entity" @dragmove="handleEntityDragMove" />
        <RelationshipNode v-for="relationship in store.relationships" :key="relationship.id" :relationship="relationship" @dragmove="handleRelationshipDragMove" />
        <AttributeNode v-for="attribute in store.attributes" :key="attribute.id" :attribute="attribute" @dragmove="handleAttributeDragMove" />

        <!-- Then render connections on top -->
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
      </v-layer>
    </v-stage>
  </div>
</template>

<script setup lang="ts">
import { useDiagramStore } from '@/stores/diagramStore'
import EntityNode from './nodes/EntityNode.vue'
import RelationshipNode from './nodes/RelationshipNode.vue'
import AttributeNode from './nodes/AttributeNode.vue'
import { onMounted, onUnmounted, ref, reactive, computed } from 'vue'
import type { Attribute, Entity, Position } from '@/types/er-diagram-elements'

const store = useDiagramStore()

const container = ref<HTMLDivElement | null>(null)
const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: true
})

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
  // entity.position is the center of the node group
  const x = entity.position.x;
  const y = entity.position.y;
  return { x: x - width / 2, y: y - height / 2, width, height };
};

// Calculates Konva ellipse config for an attribute's bounding box
const calculateAttributeRenderProps = (attribute: Attribute): EllipseShape => {
  const rx = attribute.name.length < 8 ? 50 : attribute.name.length * 5.5;
  const ry = 25;
  // attribute.position is the center of the node group
  const cx = attribute.position.x;
  const cy = attribute.position.y;
  return { cx, cy, rx, ry };
};

// Robust function to find intersection of a line segment (p1->p2) with a rectangle
const getLineRectangleIntersection = (p1: Position, p2: Position, rect: RectShape): Position | null => {
  const { x: rectX, y: rectY, width: rectW, height: rectH } = rect;

  const dx = p2.x - p1.x;
  const dy = p2.y - p1.y;

  const t = { x: 0, y: 0 }; // t values for x and y intersections
  let tmin = -Infinity;
  let tmax = Infinity;

  // Intersect with X-planes (vertical sides)
  if (dx === 0) { // Line is vertical
    if (p1.x < rectX || p1.x > rectX + rectW) return null; // Line is outside rect
  } else {
    t.x = (rectX - p1.x) / dx;
    const tx2 = (rectX + rectW - p1.x) / dx;
    tmin = Math.max(tmin, Math.min(t.x, tx2));
    tmax = Math.min(tmax, Math.max(t.x, tx2));
  }

  // Intersect with Y-planes (horizontal sides)
  if (dy === 0) { // Line is horizontal
    if (p1.y < rectY || p1.y > rectY + rectH) return null; // Line is outside rect
  } else {
    t.y = (rectY - p1.y) / dy;
    const ty2 = (rectY + rectH - p1.y) / dy;
    tmin = Math.max(tmin, Math.min(t.y, ty2));
    tmax = Math.min(tmax, Math.max(t.y, ty2));
  }

  if (tmin > tmax || tmax < 0) {
    return null; // No intersection within the segment or rectangle
  }

  // We are looking for the point where the line enters the rectangle from p1
  if (tmin >= 0 && tmin <= 1) {
    return { x: p1.x + tmin * dx, y: p1.y + tmin * dy };
  }
  return null;
};

// Finds intersection of a line segment (p1->p2) with an ellipse
const getLineEllipseIntersection = (p1: Position, p2: Position, ellipse: EllipseShape): Position | null => {
  const { cx, cy, rx, ry } = ellipse;

  // Translate line so ellipse center is at origin
  const p1t = { x: p1.x - cx, y: p1.y - cy };
  const p2t = { x: p2.x - cx, y: p2.y - cy };

  const dx = p2t.x - p1t.x;
  const dy = p2t.y - p1t.y;

  // Normalize ellipse (transform to a circle)
  const scaleX = rx;
  const scaleY = ry;
  const p1n = { x: p1t.x / scaleX, y: p1t.y / scaleY };
  const p2n = { x: p2t.x / scaleX, y: p2t.y / scaleY };

  const dxn = p2n.x - p1n.x;
  const dyn = p2n.y - p1n.y;

  const a = dxn * dxn + dyn * dyn;
  const b = 2 * (p1n.x * dxn + p1n.y * dyn);
  const c = p1n.x * p1n.x + p1n.y * p1n.y - 1; // For unit circle radius 1

  const discriminant = b * b - 4 * a * c;

  if (discriminant < 0) {
    return null; // No real intersection
  }

  const t_values = [];
  const sqrt_discriminant = Math.sqrt(discriminant);
  const t1 = (-b + sqrt_discriminant) / (2 * a);
  const t2 = (-b - sqrt_discriminant) / (2 * a);

  // Consider intersections only if they lie on the segment (0 <= t <= 1)
  if (t1 >= 0 && t1 <= 1) t_values.push(t1);
  if (t2 >= 0 && t2 <= 1) t_values.push(t2);

  if (t_values.length === 0) {
    return null;
  }

  // Find the intersection point on the line segment that is closest to p1 (start of the segment)
  let closest_t = t_values[0];
  if (t_values.length > 1 && t_values[1] < t_values[0]) { // Take the smaller t value to get the first intersection point
    closest_t = t_values[1];
  }

  // Translate back and scale
  const intersectionPoint = {
    x: (p1n.x + closest_t * dxn) * scaleX + cx,
    y: (p1n.y + closest_t * dyn) * scaleY + cy,
  };

  return intersectionPoint;
};


const attributeConnections = computed(() => {
  const connections: { attributeId: string; parentId: string; startX: number; startY: number; endX: number; endY: number }[] = [];

  store.attributes.forEach((attribute: Attribute) => {
    const parentEntity = store.entities.find((entity: Entity) => entity.id === attribute.parentId);
    if (parentEntity) {
      const attributeShape = calculateAttributeRenderProps(attribute);
      const entityShape = calculateEntityRenderProps(parentEntity);

      // Define the conceptual line segment between the centers of the two objects
      const attributeCenter = { x: attributeShape.cx, y: attributeShape.cy };
      const entityCenter = { x: entityShape.x + entityShape.width / 2, y: entityShape.y + entityShape.height / 2 };

      // Find intersection point on the attribute (ellipse) boundary
      // Line segment for attribute starts from entityCenter and goes towards attributeCenter
      const startPoint = getLineEllipseIntersection(entityCenter, attributeCenter, attributeShape);
      // Find intersection point on the entity (rectangle) boundary
      // Line segment for entity starts from attributeCenter and goes towards entityCenter
      const endPoint = getLineRectangleIntersection(attributeCenter, entityCenter, entityShape);


      if (startPoint && endPoint) {
        connections.push({
          attributeId: attribute.id,
          parentId: parentEntity.id,
          startX: startPoint.x,
          startY: startPoint.y,
          endX: endPoint.x,
          endY: endPoint.y,
        });
      } else {
         // Fallback to center-to-center if intersection not found
         connections.push({
            attributeId: attribute.id,
            parentId: parentEntity.id,
            startX: attribute.position.x,
            startY: attribute.position.y,
            endX: parentEntity.position.x,
            endY: parentEntity.position.y,
         });
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
