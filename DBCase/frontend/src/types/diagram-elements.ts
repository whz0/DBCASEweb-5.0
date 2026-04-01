export interface Node {

  name: String,
  color?: String,
}

export interface Entity extends Node {

  weak?: boolean,
}

export interface Attribute extends Node {

  pk?: boolean,
  fk?: boolean,
  compose?: boolean,
  notNull?: boolean,
  unique?: boolean,
  multivalue?: boolean,
  size?: number,
}

export interface Relationship extends Node {

}

export interface Edge {

  source: String,
  target: String,
  label?: String,
}

export interface Diagram {

  nodes: Node[],
  edges: Edge[],
}
