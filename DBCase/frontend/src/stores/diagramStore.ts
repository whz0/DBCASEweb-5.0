import { jsPDF } from 'jspdf'
import type { Stage } from 'konva/lib/Stage'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import type {
  Attribute,
  DiagramElement,
  Domain,
  Entity,
  Position,
  Relationship,
  Snapshot,
} from '@/types/er-diagram-elements'

export const useDiagramStore = defineStore('diagram', () => {
  const entities = ref<Entity[]>([])
  const relationships = ref<Relationship[]>([])
  const attributes = ref<Attribute[]>([])
  const domains = ref<Domain[]>([])
  const selectedElementId = ref<string | null>(null)
  const lastClickPosition = ref<Position>({ x: 100, y: 100 })
  const stageRef = ref<Stage | null>(null)

  const past = ref<Snapshot[]>([])
  const future = ref<Snapshot[]>([])

  function getCurrentSnapshot(): Snapshot {
    return JSON.parse(
      JSON.stringify({
        entities: entities.value,
        relationships: relationships.value,
        attributes: attributes.value,
        domains: domains.value,
      }),
    )
  }

  function exportToPDF(filename: string = 'diagram') {
    if (!stageRef.value) return

    const stage = stageRef.value
    const dataURL = stage.toDataURL({ pixelRatio: 2 })
    const pdf = new jsPDF({
      orientation: stage.width() > stage.height() ? 'landscape' : 'portrait',
      unit: 'px',
      format: [stage.width(), stage.height()],
    })

    pdf.addImage(dataURL, 'PNG', 0, 0, stage.width(), stage.height())
    pdf.save(`${filename}.pdf`)
  }

  function saveHistory() {
    past.value.push(getCurrentSnapshot())
    future.value = []

    if (past.value.length > 50) {
      past.value.shift()
    }
  }

  function applySnapshot(snapshot: Snapshot) {
    entities.value = snapshot.entities || []
    relationships.value = snapshot.relationships || []
    attributes.value = snapshot.attributes || []
    domains.value = snapshot.domains || []
    selectedElementId.value = null
  }

  function undo() {
    if (past.value.length === 0) return

    future.value.push(getCurrentSnapshot())
    applySnapshot(past.value.pop()!)
  }

  function redo() {
    if (future.value.length === 0) return

    past.value.push(getCurrentSnapshot())
    applySnapshot(future.value.pop()!)
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

  function addEntity(entity: Entity) {
    saveHistory()
    entities.value.push(entity)
  }

  function updateEntityPosition(id: string, position: Position) {
    updateElementPosition(id, entities.value, position)
  }

  function addRelationship(relationship: Relationship) {
    saveHistory()
    relationships.value.push(relationship)
  }

  function updateRelationshipPosition(id: string, position: Position) {
    updateElementPosition(id, relationships.value, position)
  }

  function updateAttributePosition(id: string, position: Position) {
    updateElementPosition(id, attributes.value, position)
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

  function loadSnapshot(newState: Snapshot) {
    saveHistory()
    applySnapshot(newState)
  }

  function syncWeakEntityRelationship(
    entity: Entity,
    isWeak: boolean,
    strongEntity: Entity | null,
    relationName: string,
  ) {
    const existingIdRel = relationships.value.find(
      (r) => r.type === 'Weak' && r.participants.some((p) => p.entityId === entity.id),
    )

    if (isWeak && strongEntity) {
      if (existingIdRel) {
        existingIdRel.name = relationName.trim() || 'Identifying'
        existingIdRel.participants = [
          { entityId: entity.id, cardinalityMin: '1', cardinalityMax: '1' },
          { entityId: strongEntity.id, cardinalityMin: '1', cardinalityMax: 'N' },
        ]
      } else {
        addRelationship({
          id: crypto.randomUUID(),
          name: relationName.trim() || 'Identifying',
          position: { x: entity.position.x + 150, y: entity.position.y },
          type: 'Weak' as const,
          participants: [
            { entityId: entity.id, cardinalityMin: '1', cardinalityMax: '1' },
            { entityId: strongEntity.id, cardinalityMin: '1', cardinalityMax: 'N' },
          ],
          attributes: [],
        })
      }
    } else if (!isWeak && existingIdRel) {
      deleteElement(existingIdRel.id)
    }
  }

  function saveEntity(
    data: { name: string; isWeak: boolean },
    isEdit: boolean,
    strongEntity: Entity | null,
    relationName: string,
  ) {
    let entity: Entity
    if (isEdit && selectedElementId.value) {
      const existing = entities.value.find((e) => e.id === selectedElementId.value)
      if (!existing) return
      saveHistory()
      entity = { ...existing, ...data }
      const index = entities.value.findIndex((e) => e.id === entity.id)
      entities.value[index] = entity
    } else {
      saveHistory()
      entity = {
        id: crypto.randomUUID(),
        name: data.name,
        position: { ...lastClickPosition.value },
        isWeak: data.isWeak,
        attributes: [],
        primaryKeys: [],
      }
      entities.value.push(entity)
    }

    syncWeakEntityRelationship(entity, data.isWeak, strongEntity, relationName)
  }

  function saveAttribute(
    data: {
      name: string
      parentId: string
      isKey: boolean
      isMultivalued: boolean
      isComposite: boolean
      isNotNull: boolean
      isUnique: boolean
      size: number
      domainId?: string
    },
    isEdit: boolean,
  ) {
    if (isEdit && selectedElementId.value) {
      const existing = attributes.value.find((a) => a.id === selectedElementId.value)
      if (!existing) return
      saveHistory()
      const index = attributes.value.findIndex((a) => a.id === existing.id)
      attributes.value[index] = { ...existing, ...data }
    } else {
      saveHistory()
      attributes.value.push({
        id: crypto.randomUUID(),
        position: { ...lastClickPosition.value },
        ...data,
      })
    }
  }

  function saveRelationship(data: { name: string }, isEdit: boolean) {
    if (isEdit && selectedElementId.value) {
      const rel = relationships.value.find((r) => r.id === selectedElementId.value)
      if (rel) {
        saveHistory()
        rel.name = data.name.trim()
      }
    } else {
      saveHistory()
      addRelationship({
        id: crypto.randomUUID(),
        name: data.name.trim(),
        position: { ...lastClickPosition.value },
        type: 'Normal',
        participants: [],
        attributes: [],
      })
    }
  }

  function saveIsARelationship(
    data: {
      parent: Entity
      children: Entity[]
    },
    isEdit: boolean,
  ) {
    if (isEdit && selectedElementId.value) {
      const rel = relationships.value.find((r) => r.id === selectedElementId.value)
      if (rel && rel.type === 'IsA') {
        saveHistory()
        rel.participants = [
          { entityId: data.parent.id, cardinalityMin: '', cardinalityMax: '', role: 'Parent' },
          ...data.children.map((c) => ({
            entityId: c.id,
            cardinalityMin: '',
            cardinalityMax: '',
            role: 'Child',
          })),
        ]
      }
    } else {
      addRelationship({
        id: crypto.randomUUID(),
        name: 'IsA',
        position: { ...lastClickPosition.value },
        type: 'IsA' as const,
        participants: [
          { entityId: data.parent.id, cardinalityMin: '', cardinalityMax: '', role: 'Parent' },
          ...data.children.map((c) => ({
            entityId: c.id,
            cardinalityMin: '',
            cardinalityMax: '',
            role: 'Child',
          })),
        ],
        attributes: [],
      })
    }
  }

  function saveDomain(
    data: { name: string; baseType: string; values?: string[] },
    isEdit: boolean,
    domainId?: string,
  ) {
    const targetId = isEdit ? domainId || selectedElementId.value : null
    if (isEdit && targetId) {
      const index = domains.value.findIndex((d) => d.id === targetId)
      if (index !== -1) {
        saveHistory()
        domains.value[index] = { ...domains.value[index], ...data } as Domain
      }
    } else {
      saveHistory()
      domains.value.push({
        id: crypto.randomUUID(),
        ...data,
      })
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
    getCurrentSnapshot,
    loadSnapshot,
    saveEntity,
    saveAttribute,
    saveRelationship,
    saveIsARelationship,
    saveDomain,
    addEntity,
    addRelationship,
    selectElement,
    deleteElement,
    removeParticipantFromRelationship,
    updateEntityPosition,
    updateRelationshipPosition,
    addParticipantToRelationship,
    updateAttributePosition,
    setLastClickPosition,
    stageRef,
    exportToPDF,
  }
})
