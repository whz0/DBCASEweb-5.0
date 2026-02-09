export interface Position {
  x: number;
  y: number;
}

export interface DiagramElement {
  id: string;
  name: string;
  position: Position;
  // Common properties
}

export interface Entity extends DiagramElement {
  // Entity-specific properties
  isWeak?: boolean;
  attributes: string[]; // Array of attribute IDs
  primaryKeys: string[]; // Array of attribute IDs that are primary keys
}

export interface Attribute extends DiagramElement {
  // Attribute-specific properties
  parentId: string; // ID of the entity/relationship/attribute it belongs to
  isComposite?: boolean;
  isMultivalued?: boolean;
  isNotNull?: boolean;
  isUnique?: boolean;
  domainId?: string; // ID of the domain if applicable
  components?: string[]; // If composite, array of child attribute IDs
}

export interface Relationship extends DiagramElement {
  // Relationship-specific properties
  type: 'Normal' | 'IsA' | 'Weak';
  participants: { entityId: string; cardinalityMin: string; cardinalityMax: string; role?: string }[];
  attributes: string[]; // Array of attribute IDs
}

export interface Domain {
  id: string;
  name: string;
  baseType: string; // e.g., 'VARCHAR', 'INT', 'DATE'
  values?: string[]; // If enumerated domain
}
