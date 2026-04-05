import type { Position } from '@/types/er-diagram-elements'

export interface RectShape {
  x: number // Top-left x
  y: number // Top-left y
  width: number
  height: number
}

export interface EllipseShape {
  cx: number // Center x
  cy: number // Center y
  rx: number // Radius x
  ry: number // Radius y
}

export const getLineRectangleIntersection = (
  p1: Position,
  p2: Position,
  rect: RectShape,
): Position | null => {
  const { x: rectX, y: rectY, width: rectW, height: rectH } = rect
  const dx = p2.x - p1.x
  const dy = p2.y - p1.y

  const t = { x: 0, y: 0 }
  let tmin = -Infinity
  let tmax = Infinity

  if (dx === 0) {
    if (p1.x < rectX || p1.x > rectX + rectW) return null
  } else {
    t.x = (rectX - p1.x) / dx
    const tx2 = (rectX + rectW - p1.x) / dx
    tmin = Math.max(tmin, Math.min(t.x, tx2))
    tmax = Math.min(tmax, Math.max(t.x, tx2))
  }

  if (dy === 0) {
    if (p1.y < rectY || p1.y > rectY + rectH) return null
  } else {
    t.y = (rectY - p1.y) / dy
    const ty2 = (rectY + rectH - p1.y) / dy
    tmin = Math.max(tmin, Math.min(t.y, ty2))
    tmax = Math.min(tmax, Math.max(t.y, ty2))
  }

  if (tmin > tmax || tmax < 0) return null
  if (tmin >= 0 && tmin <= 1) return { x: p1.x + tmin * dx, y: p1.y + tmin * dy }
  return null
}

export const getLineEllipseIntersection = (
  p1: Position,
  p2: Position,
  ellipse: EllipseShape,
): Position | null => {
  const { cx, cy, rx, ry } = ellipse
  const p1t = { x: p1.x - cx, y: p1.y - cy }
  const p2t = { x: p2.x - cx, y: p2.y - cy }
  const scaleX = rx
  const scaleY = ry
  const p1n = { x: p1t.x / scaleX, y: p1t.y / scaleY }
  const p2n = { x: p2t.x / scaleX, y: p2t.y / scaleY }
  const dxn = p2n.x - p1n.x
  const dyn = p2n.y - p1n.y

  const a = dxn * dxn + dyn * dyn
  const b = 2 * (p1n.x * dxn + p1n.y * dyn)
  const c = p1n.x * p1n.x + p1n.y * p1n.y - 1
  const discriminant = b * b - 4 * a * c

  if (discriminant < 0) return null

  const t_values = []
  const sqrt_discriminant = Math.sqrt(discriminant)
  const t1 = (-b + sqrt_discriminant) / (2 * a)
  const t2 = (-b - sqrt_discriminant) / (2 * a)
  if (t1 >= 0 && t1 <= 1) t_values.push(t1)
  if (t2 >= 0 && t2 <= 1) t_values.push(t2)
  if (t_values.length === 0) return null

  let closest_t = t_values[0] as number
  if (t_values.length > 1 && (t_values[1] as number) < (t_values[0] as number)) {
    closest_t = t_values[1] as number
  }

  return {
    x: (p1n.x + closest_t * dxn) * scaleX + cx,
    y: (p1n.y + closest_t * dyn) * scaleY + cy,
  }
}

export const getLineSegmentIntersection = (
  p1: Position,
  p2: Position,
  p3: Position,
  p4: Position,
): Position | null => {
  const det = (p2.x - p1.x) * (p4.y - p3.y) - (p2.y - p1.y) * (p4.x - p3.x)
  if (det === 0) return null
  const ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / det
  const ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / det
  if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
    return { x: p1.x + ua * (p2.x - p1.x), y: p1.y + ua * (p2.y - p1.y) }
  }
  return null
}

export const getLineDiamondIntersection = (
  p1: Position,
  p2: Position,
  diamond: { cx: number; cy: number; width: number; height: number },
): Position | null => {
  const { cx, cy, width, height } = diamond
  const halfW = width / 2
  const halfH = height / 2
  const v = [
    { x: cx, y: cy - halfH },
    { x: cx + halfW, y: cy },
    { x: cx, y: cy + halfH },
    { x: cx - halfW, y: cy },
  ]
  const sides = [
    [v[0], v[1]],
    [v[1], v[2]],
    [v[2], v[3]],
    [v[3], v[0]],
  ]
  for (const side of sides) {
    const intersect = getLineSegmentIntersection(p1, p2, side[0] as Position, side[1] as Position)
    if (intersect) return intersect
  }
  return null
}

export const getLineTriangleIntersection = (
  p1: Position,
  p2: Position,
  triangle: { cx: number; cy: number; size: number },
): Position | null => {
  const { cx, cy, size } = triangle
  const half = size / 2
  const v = [
    { x: cx - half, y: cy - half }, // Top Left
    { x: cx + half, y: cy - half }, // Top Right
    { x: cx, y: cy + half }, // Bottom Tip
  ]
  const sides = [
    [v[0], v[1]],
    [v[1], v[2]],
    [v[2], v[0]],
  ]
  for (const side of sides) {
    const intersect = getLineSegmentIntersection(p1, p2, side[0] as Position, side[1] as Position)
    if (intersect) return intersect
  }
  return null
}

export const calculateParallelPoints = (
  x1: number,
  y1: number,
  x2: number,
  y2: number,
  offset: number,
) => {
  const dx = x2 - x1
  const dy = y2 - y1
  const length = Math.sqrt(dx * dx + dy * dy)
  if (length === 0) return [x1, y1, x2, y2]

  const nx = -dy / length
  const ny = dx / length

  return [x1 + nx * offset, y1 + ny * offset, x2 + nx * offset, y2 + ny * offset]
}
