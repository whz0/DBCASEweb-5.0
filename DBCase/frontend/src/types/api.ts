import type { Snapshot } from './er-diagram-elements'

export interface UserSettings {
  language: string
  theme: string
}

export interface CustomDomain {
  name: string
  base: string | null
}

export interface LogicalDiagramInput {
  relationship: string
  restriction: string
  lossRestriction: string
}

export interface SqlDiagramInput {
  sql: string
}

export interface DatabaseUrl {
  databaseType: string
  host: string
  port: number | null
  databaseName: string
}

export type ConceptualDiagramInput = Snapshot

export type AnyDiagramInput = ConceptualDiagramInput | LogicalDiagramInput | SqlDiagramInput
