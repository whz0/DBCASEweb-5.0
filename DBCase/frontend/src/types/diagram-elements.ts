export interface Node {
  name: string
  color?: string
}

export interface Entity extends Node {
  weak?: boolean
}

export interface Attribute extends Node {
  pk?: boolean
  fk?: boolean
  compose?: boolean
  notNull?: boolean
  unique?: boolean
  multivalue?: boolean
  size?: number
}

export interface Relationship extends Node {
  cardinality: number
  level: number
}

export interface Edge {
  source: string
  target: string
  label?: string
}

export interface Diagram {
  nodes: Node[]
  edges: Edge[]
}
