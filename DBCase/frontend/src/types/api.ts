import type { Snapshot } from './er-diagram-elements'

export interface UserSettings {
  language: string
  theme: string
}

export interface LogicalDiagramInput {
  relationship: string
  restriction: string
  lossRestriction: string
}

export interface SqlDiagramInput {
  sql: string
}

export type ConceptualDiagramInput = Snapshot

export type AnyDiagramInput = ConceptualDiagramInput | LogicalDiagramInput | SqlDiagramInput
