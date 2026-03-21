import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import type {
  Attribute,
  DiagramElement,
  Domain,
  Entity,
  Position,
  Relationship,
} from '@/types/er-diagram-elements'

export const useDiagramStore = defineStore('diagram', () => {
  const entities = ref<Entity[]>([])
  const relationships = ref<Relationship[]>([])
  const attributes = ref<Attribute[]>([])
  const domains = ref<Domain[]>([])
  const selectedElementId = ref<string | null>(null)
  const lastClickPosition = ref<Position>({ x: 100, y: 100 })

  const past = ref<string[]>([])
  const future = ref<string[]>([])

  function saveHistory() {
    const snapshot = JSON.stringify({
      entities: entities.value,
      relationships: relationships.value,
      attributes: attributes.value,
      domains: domains.value,
    })
    past.value.push(snapshot)
    future.value = []

    if (past.value.length > 50) {
      past.value.shift()
    }
  }

  function undo() {
    if (past.value.length === 0) return

    const currentSnapshot = JSON.stringify({
      entities: entities.value,
      relationships: relationships.value,
      attributes: attributes.value,
      domains: domains.value,
    })
    future.value.push(currentSnapshot)

    const previousSnapshot = JSON.parse(past.value.pop()!)
    entities.value = previousSnapshot.entities
    relationships.value = previousSnapshot.relationships
    attributes.value = previousSnapshot.attributes
    domains.value = previousSnapshot.domains
    selectedElementId.value = null
  }

  function redo() {
    if (future.value.length === 0) return

    const currentSnapshot = JSON.stringify({
      entities: entities.value,
      relationships: relationships.value,
      attributes: attributes.value,
      domains: domains.value,
    })
    past.value.push(currentSnapshot)

    const nextSnapshot = JSON.parse(future.value.pop()!)
    entities.value = nextSnapshot.entities
    relationships.value = nextSnapshot.relationships
    attributes.value = nextSnapshot.attributes
    domains.value = nextSnapshot.domains
    selectedElementId.value = null
  }

  function setLastClickPosition(position: Position) {
    lastClickPosition.value = position
  }

  function find<T extends DiagramElement>(id: string, values: T[]): T | undefined {
    return values.find((e) => e.id === id)
  }

  function move(elementToMove: DiagramElement | undefined, position: Position) {
    if (elementToMove) {
      elementToMove.position = position
    }
  }

  function updateElementPosition<T extends DiagramElement>(
    id: string,
    elements: T[],
    position: Position,
  ) {
    const element = find(id, elements)
    move(element, position)
  }

  function addElement<T>(element: T, elements: T[]) {
    saveHistory()
    elements.push(element)
  }

  function addEntity(entity: Entity) {
    addElement(entity, entities.value)
  }

  function updateEntity(updatedEntity: Entity) {
    saveHistory()
    const index = entities.value.findIndex((e) => e.id === updatedEntity.id)
    if (index !== -1) {
      entities.value[index] = updatedEntity
    }
  }

  function updateEntityPosition(id: string, position: Position) {
    updateElementPosition(id, entities.value, position)
  }

  function addRelationship(relationship: Relationship) {
    addElement(relationship, relationships.value)
  }

  function updateRelationshipPosition(id: string, position: Position) {
    updateElementPosition(id, relationships.value, position)
  }

  function addAttribute(attribute: Attribute) {
    addElement(attribute, attributes.value)
  }

  function updateAttributePosition(id: string, position: Position) {
    updateElementPosition(id, attributes.value, position)
  }

  function addDomain(domain: Domain) {
    addElement(domain, domains.value)
  }

  function addParticipantToRelationship(
    relationshipId: string,
    participant: {
      entityId: string
      cardinalityMin: string
      cardinalityMax: string
      role?: string
    },
  ) {
    saveHistory()
    const relationship = relationships.value.find((r) => r.id === relationshipId)
    if (relationship) {
      if (!relationship.participants.some((p) => p.entityId === participant.entityId)) {
        relationship.participants.push(participant)
      }
    }
  }

  function removeParticipantFromRelationship(relationshipId: string, entityId: string) {
    saveHistory()
    const relationship = relationships.value.find((r) => r.id === relationshipId)
    if (relationship) {
      relationship.participants = relationship.participants.filter((p) => p.entityId !== entityId)
    }
  }

  function selectElement(id: string | null) {
    selectedElementId.value = id
  }

  function deleteEntity(entityIndex: number, entityId: string) {
    attributes.value = attributes.value.filter((a) => a.parentId !== entityId)
    relationships.value.forEach((r) => {
      r.participants = r.participants.filter((p) => p.entityId !== entityId)
    })
    entities.value.splice(entityIndex, 1)
  }

  function deleteElement(id: string) {
    saveHistory()
    const entityIndex = entities.value.findIndex((e) => e.id === id)
    if (entityIndex !== -1) {
      deleteEntity(entityIndex, id)
    } else {
      relationships.value = relationships.value.filter((r) => r.id !== id)
      attributes.value = attributes.value.filter((a) => a.id !== id)
      domains.value = domains.value.filter((d) => d.id !== id)
    }

    if (selectedElementId.value === id) {
      selectedElementId.value = null
    }
  }

  function renameElement(id: string, newName: string) {
    saveHistory()
    const entity = entities.value.find((e) => e.id === id)
    if (entity) {
      entity.name = newName
      return
    }
    const relationship = relationships.value.find((r) => r.id === id)
    if (relationship) {
      relationship.name = newName
      return
    }
    const attribute = attributes.value.find((a) => a.id === id)
    if (attribute) {
      attribute.name = newName
      return
    }
  }

  return {
    entities,
    relationships,
    attributes,
    domains,
    selectedElementId,
    lastClickPosition,
    canUndo: computed(() => past.value.length > 0),
    canRedo: computed(() => future.value.length > 0),
    undo,
    redo,
    saveHistory,
    selectElement,
    deleteElement,
    renameElement,
    addEntity,
    updateEntity,
    removeParticipantFromRelationship,
    updateEntityPosition,
    addRelationship,
    updateRelationshipPosition,
    addParticipantToRelationship,
    addAttribute,
    updateAttributePosition,
    addDomain,
    setLastClickPosition,
  }
})
