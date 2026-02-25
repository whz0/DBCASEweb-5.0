import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Entity, Relationship, Attribute, Domain } from '@/types/er-diagram-elements'

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

  function addRelationship(relationship: Relationship) {
    relationships.value.push(relationship)
  }

  function addAttribute(attribute: Attribute) {
    attributes.value.push(attribute)
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
    addRelationship,
    addAttribute,
    addDomain,
    // ... expose other actions and state
  }
})
