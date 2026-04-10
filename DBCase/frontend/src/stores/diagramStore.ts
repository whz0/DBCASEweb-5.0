import { defineStore } from 'pinia'
import { ref } from 'vue'

import { api } from '@/plugins/axios.ts'
import type { AnyDiagramInput } from '@/types/api'
import type { Snapshot } from '@/types/er-diagram-elements.ts'

export enum DiagramType {
  er = 'er',
  logical = 'logical',
  db = 'db',
}

export const useDiagramStore = defineStore('diagram', () => {
  const erResult = ref<Snapshot | null>(null)
  const logicalResult = ref<Map<string, string> | null>(null)
  const dbResult = ref<string | null>(null)

  async function save(
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    toast('Hola, no hay función guardar', 'info')
  }

  async function transform(
    diagram: AnyDiagramInput,
    type: DiagramType,
    transformTo: DiagramType,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return api.diagram
      .generate(diagram, type, transformTo)
      .then(({ data }) => {
        toast('Se ha generado exitosamente el diagrama', 'success')
        if (transformTo === DiagramType.er) erResult.value = data
        if (transformTo === DiagramType.logical) logicalResult.value = data
        if (transformTo === DiagramType.db) dbResult.value = data
        return data
      })
      .catch((e) => {
        toast(
          e?.response?.data?.message ?? e?.message ?? 'Error al transformar el diagrama',
          'error',
        )
        return null
      })
  }

  function reset() {
    logicalResult.value = null
    dbResult.value = null
  }

  return {
    erResult,
    logicalResult,
    dbResult,
    save,
    transform,
    reset,
  }
})
