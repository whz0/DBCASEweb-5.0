import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  Entity,
  Relationship,
  Attribute,
  Domain,
  Position,
  DiagramElement,
} from '@/types/er-diagram-elements'

export const useDiagramStore = defineStore('diagram', () => {
  const entities = ref<Entity[]>([])
  const relationships = ref<Relationship[]>([])
  const attributes = ref<Attribute[]>([])
  const domains = ref<Domain[]>([])
  const selectedElementId = ref<string | null>(null)

  function find<T extends DiagramElement>(id: string, values: T[]) : T | undefined {
    return values.find(e => e.id === id);
  }

  function move(elementToMove: DiagramElement | undefined, position: Position) {
    if (elementToMove) {
      elementToMove.position = position
    }
  }

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
    const entity = find(id, entities.value)
    move(entity, position)
  }

  function addRelationship(relationship: Relationship) {
    relationships.value.push(relationship)
  }

  function updateRelationshipPosition(id: string, position: Position) {
    const relationship = find(id, relationships.value)
    move(relationship, position)
  }

  function addAttribute(attribute: Attribute) {
    attributes.value.push(attribute)
  }

  function updateAttributePosition(id: string, position: Position) {
    const attribute = find(id, attributes.value)
    move(attribute, position)
  }

  function addDomain(domain: Domain) {
    domains.value.push(domain)
  }

  function addParticipantToRelationship(relationshipId: string, participant: { entityId: string, cardinalityMin: string, cardinalityMax: string, role?: string }) {
    const relationship = relationships.value.find(r => r.id === relationshipId)
    if (relationship) {
      // Check if entity already participates
      if (!relationship.participants.some(p => p.entityId === participant.entityId)) {
        relationship.participants.push(participant)
      }
    }
  }

  function removeParticipantFromRelationship(relationshipId: string, entityId: string) {
    const relationship = relationships.value.find(r => r.id === relationshipId)
    if (relationship) {
      relationship.participants = relationship.participants.filter(p => p.entityId !== entityId)
    }
  }

  function selectElement(id: string | null) {
    selectedElementId.value = id
  }

  function deleteElement(id: string) {
    // If it's an entity, also delete its attributes and remove it from relationships
    const entityIndex = entities.value.findIndex(e => e.id === id)
    if (entityIndex !== -1) {
      attributes.value = attributes.value.filter(a => a.parentId !== id)
      relationships.value.forEach(r => {
        r.participants = r.participants.filter(p => p.entityId !== id)
      })
      entities.value.splice(entityIndex, 1)
    } else {
      relationships.value = relationships.value.filter(r => r.id !== id)
      attributes.value = attributes.value.filter(a => a.id !== id)
      domains.value = domains.value.filter(d => d.id !== id)
    }

    if (selectedElementId.value === id) {
      selectedElementId.value = null
    }
  }

  function renameElement(id: string, newName: string) {
    const entity = entities.value.find(e => e.id === id)
    if (entity) {
      entity.name = newName
      return
    }
    const relationship = relationships.value.find(r => r.id === id)
    if (relationship) {
      relationship.name = newName
      return
    }
    const attribute = attributes.value.find(a => a.id === id)
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
  }
})
