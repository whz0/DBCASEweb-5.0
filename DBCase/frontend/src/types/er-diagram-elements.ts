export interface Position {
  x: number
  y: number
}

export interface DiagramElement {
  id: string
  name: string
  position: Position
}

export interface RelationshipParticipant {
  entityId: string
  cardinalityMin: string
  cardinalityMax: string
  role?: string
}

export interface Entity extends DiagramElement {
  isWeak?: boolean
  attributes: string[]
  primaryKeys: string[]
}

export interface Attribute extends DiagramElement {
  parentId: string
  isKey?: boolean
  isComposite?: boolean
  isMultivalued?: boolean
  isNotNull?: boolean
  isUnique?: boolean
  isDerived?: boolean
  domain?: string
  size?: number
  components?: string[]
}

export interface Relationship extends DiagramElement {
  type: 'Normal' | 'IsA' | 'Weak' | 'Aggregation'
  participants: RelationshipParticipant[]
  attributes: string[]
  aggregationName?: string
}

export interface SnapshotDomain {
  id: string
  name: string
  baseType: string
  values?: string[]
}

export interface Undefined extends DiagramElement {
  attributes: string[]
}

export interface Snapshot {
  entities: Entity[]
  relationships: Relationship[]
  attributes: Attribute[]
  domains?: SnapshotDomain[]
  undefineds?: Undefined[]
}
