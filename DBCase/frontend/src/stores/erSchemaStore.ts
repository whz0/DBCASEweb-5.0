import { jsPDF } from 'jspdf'
import type { Stage } from 'konva/lib/Stage'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { api } from '@/plugins/axios'
import { useAuthStore } from '@/stores/authStore'
import { useDomainStore } from '@/stores/domainStore'
import type {
  Attribute,
  DiagramElement,
  Entity,
  Position,
  Relationship,
  Snapshot,
  Undefined,
} from '@/types/er-diagram-elements'

export const useErSchemaStore = defineStore('erSchema', () => {
  const entities = ref<Entity[]>([])
  const relationships = ref<Relationship[]>([])
  const attributes = ref<Attribute[]>([])
  const undefineds = ref<Undefined[]>([])
  const selectedElementIds = ref<Set<string>>(new Set())
  // Keep a single-id alias for context menu compatibility
  const selectedElementId = computed(() => {
    const ids = [...selectedElementIds.value]
    return ids.length === 1 ? ids[0] : ids.length > 1 ? ids[0] : null
  })
  const lastClickPosition = ref<Position>({ x: 100, y: 100 })
  const stageRef = ref<Stage | null>(null)

  const past = ref<Snapshot[]>([])
  const future = ref<Snapshot[]>([])

  function getCurrentSnapshot(): Snapshot {
    const domainStore = useDomainStore()
    return JSON.parse(
      JSON.stringify({
        entities: entities.value,
        relationships: relationships.value,
        attributes: attributes.value,
        domains: domainStore.domains,
        undefineds: undefineds.value,
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
    undefineds.value = snapshot.undefineds || []
    selectedElementIds.value = new Set()
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

  function updateParticipant(
    relationshipId: string,
    entityId: string,
    data: { cardinalityMin: string; cardinalityMax: string; role: string },
  ) {
    saveHistory()
    const relationship = relationships.value.find((r) => r.id === relationshipId)
    if (!relationship) return
    const participant = relationship.participants.find((p) => p.entityId === entityId)
    if (participant) {
      participant.cardinalityMin = data.cardinalityMin
      participant.cardinalityMax = data.cardinalityMax
      participant.role = data.role
    }
  }

  function isSelected(id: string) {
    return selectedElementIds.value.has(id)
  }

  function selectElement(id: string | null, addToSelection = false) {
    if (id === null) {
      selectedElementIds.value = new Set()
    } else if (addToSelection) {
      const next = new Set(selectedElementIds.value)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      selectedElementIds.value = next
    } else {
      selectedElementIds.value = new Set([id])
    }
  }

  function selectElements(ids: string[]) {
    selectedElementIds.value = new Set(ids)
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
      undefineds.value = undefineds.value.filter((u) => u.id !== id)
    }

    const next = new Set(selectedElementIds.value)
    next.delete(id)
    selectedElementIds.value = next
  }

  function deleteSelected() {
    const ids = [...selectedElementIds.value]
    if (ids.length === 0) return
    saveHistory()
    for (const id of ids) {
      const entityIndex = entities.value.findIndex((e) => e.id === id)
      if (entityIndex !== -1) {
        deleteEntity(entityIndex, id)
      } else {
        relationships.value = relationships.value.filter((r) => r.id !== id)
        attributes.value = attributes.value.filter((a) => a.id !== id)
        undefineds.value = undefineds.value.filter((u) => u.id !== id)
      }
    }
    selectedElementIds.value = new Set()
  }

  function moveSelected(id: string, dx: number, dy: number) {
    for (const selId of selectedElementIds.value) {
      if (selId === id) continue
      const entity = entities.value.find((e) => e.id === selId)
      if (entity) {
        entity.position.x += dx
        entity.position.y += dy
        continue
      }
      const rel = relationships.value.find((r) => r.id === selId)
      if (rel) {
        rel.position.x += dx
        rel.position.y += dy
        continue
      }
      const attr = attributes.value.find((a) => a.id === selId)
      if (attr) {
        attr.position.x += dx
        attr.position.y += dy
        continue
      }
      const undef = undefineds.value.find((u) => u.id === selId)
      if (undef) {
        undef.position.x += dx
        undef.position.y += dy
      }
    }
  }

  function loadSnapshot(newState: Snapshot) {
    saveHistory()
    applySnapshot(newState)
  }

  async function saveToProfile() {
    const snapshot = getCurrentSnapshot()
    const json = JSON.stringify(snapshot)
    try {
      const { data } = await api.user.saveChart(json)
      const authStore = useAuthStore()
      authStore.setChart(data.chart)
      return true
    } catch (error) {
      console.error('Error saving diagram to profile:', error)
      return false
    }
  }

  function loadFromProfile() {
    const authStore = useAuthStore()
    if (authStore.user.chart) {
      try {
        const snapshot = JSON.parse(authStore.user.chart)
        loadSnapshot(snapshot)
        return true
      } catch (error) {
        console.error('Error parsing saved diagram:', error)
      }
    }
    return false
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
      domain?: string
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
      const newId = crypto.randomUUID()
      attributes.value.push({
        id: newId,
        position: { ...lastClickPosition.value },
        ...data,
      })
      const parentEntity = entities.value.find((e) => e.id === data.parentId)
      if (parentEntity) {
        parentEntity.attributes.push(newId)
        if (data.isKey) parentEntity.primaryKeys.push(newId)
      } else {
        const parentRel = relationships.value.find((r) => r.id === data.parentId)
        if (parentRel) parentRel.attributes.push(newId)
      }
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

  function convertUndefinedToEntity(id: string) {
    const u = undefineds.value.find((u) => u.id === id)
    if (!u) return
    saveHistory()
    entities.value.push({
      id: u.id,
      name: u.name,
      position: u.position,
      attributes: u.attributes,
      primaryKeys: [],
    })
    undefineds.value = undefineds.value.filter((u) => u.id !== id)
  }

  function convertUndefinedToRelationship(id: string) {
    const u = undefineds.value.find((u) => u.id === id)
    if (!u) return
    saveHistory()
    relationships.value.push({
      id: u.id,
      name: u.name,
      position: u.position,
      type: 'Normal',
      participants: [],
      attributes: u.attributes,
    })
    undefineds.value = undefineds.value.filter((u) => u.id !== id)
  }

  function reset() {
    entities.value = []
    relationships.value = []
    attributes.value = []
    undefineds.value = []
    selectedElementIds.value = new Set()
    lastClickPosition.value = { x: 100, y: 100 }
    past.value = []
    future.value = []
  }

  function toggleAggregation(id: string) {
    const rel = relationships.value.find((r) => r.id === id)
    if (rel) {
      saveHistory()
      if (rel.type === 'Aggregation') {
        rel.type = 'Normal'
        rel.aggregationName = undefined
      } else {
        rel.type = 'Aggregation'
      }
    }
  }

  function renameAggregation(id: string, name: string) {
    const rel = relationships.value.find((r) => r.id === id)
    if (rel) {
      saveHistory()
      rel.aggregationName = name
    }
  }

  return {
    entities,
    relationships,
    attributes,
    undefineds,
    selectedElementId,
    selectedElementIds,
    lastClickPosition,
    canUndo: computed(() => past.value.length > 0),
    canRedo: computed(() => future.value.length > 0),
    undo,
    redo,
    saveHistory,
    getCurrentSnapshot,
    loadSnapshot,
    saveToProfile,
    loadFromProfile,
    saveEntity,
    saveAttribute,
    saveRelationship,
    saveIsARelationship,
    addEntity,
    addRelationship,
    isSelected,
    selectElement,
    selectElements,
    deleteElement,
    deleteSelected,
    moveSelected,
    removeParticipantFromRelationship,
    updateParticipant,
    updateEntityPosition,
    updateRelationshipPosition,
    addParticipantToRelationship,
    updateAttributePosition,
    setLastClickPosition,
    stageRef,
    exportToPDF,
    reset,
    convertUndefinedToEntity,
    convertUndefinedToRelationship,
    toggleAggregation,
    renameAggregation,
  }
})
