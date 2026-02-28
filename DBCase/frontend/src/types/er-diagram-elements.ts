export interface Position {
  x: number;
  y: number;
}

export interface DiagramElement {
  id: string;
  name: string;
  position: Position;
}

export interface Entity extends DiagramElement {
  isWeak?: boolean;
  attributes: string[];
  primaryKeys: string[];
}

export interface Attribute extends DiagramElement {
  parentId: string;
  isComposite?: boolean;
  isMultivalued?: boolean;
  isNotNull?: boolean;
  isUnique?: boolean;
  domainId?: string;
  components?: string[];
}

export interface Relationship extends DiagramElement {
  type: 'Normal' | 'IsA' | 'Weak';
  participants: { entityId: string; cardinalityMin: string; cardinalityMax: string; role?: string }[];
  attributes: string[];
}

export interface Domain {
  id: string;
  name: string;
  baseType: string;
  values?: string[];
}
