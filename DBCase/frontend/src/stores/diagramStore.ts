import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Entity, Relationship, Attribute, Domain, Position } from '@/types/er-diagram-elements'

export const useDiagramStore = defineStore('diagram', () => {
  const entities = ref<Entity[]>([])
  const relationships = ref<Relationship[]>([])
  const attributes = ref<Attribute[]>([]) // Attributes can be independent or attached
  const domains = ref<Domain[]>([])
  const selectedElementId = ref<string | null>(null) // ID of the currently selected element

  // Actions
  function addEntity(entity: Entity) {
    entities.value.push(entity)
  }

  function updateEntity(updatedEntity: Entity) {
    const index = entities.value.findIndex(e => e.id === updatedEntity.id)
    if (index !== -1) {
      entities.value[index] = updatedEntity
    }
  }

  function updateEntityPosition(id: string, position: Position) {
    const entity = entities.value.find(e => e.id === id)
    if (entity) {
      entity.position = position
    }
  }

  function addRelationship(relationship: Relationship) {
    relationships.value.push(relationship)
  }

  function updateRelationshipPosition(id: string, position: Position) {
    const relationship = relationships.value.find(r => r.id === id)
    if (relationship) {
      relationship.position = position
    }
  }

  function addAttribute(attribute: Attribute) {
    attributes.value.push(attribute)
  }

  function updateAttributePosition(id: string, position: Position) {
    const attribute = attributes.value.find(a => a.id === id)
    if (attribute) {
      attribute.position = position
    }
  }

  function addDomain(domain: Domain) {
    domains.value.push(domain)
  }

  // ... other actions for relationships, attributes, domains, selection, etc.

  return {
    entities,
    relationships,
    attributes,
    domains,
    selectedElementId,
    addEntity,
    updateEntity,
    updateEntityPosition,
    addRelationship,
    updateRelationshipPosition,
    addAttribute,
    updateAttributePosition,
    addDomain,
    // ... expose other actions and state
  }
})
