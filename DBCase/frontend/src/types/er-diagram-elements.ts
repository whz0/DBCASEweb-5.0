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
  domainId?: string
  size?: number
  components?: string[]
}

export interface Relationship extends DiagramElement {
  type: 'Normal' | 'IsA' | 'Weak'
  participants: RelationshipParticipant[]
  attributes: string[]
}

export interface Domain {
  id: string
  name: string
  baseType: string
  values?: string[]
}
