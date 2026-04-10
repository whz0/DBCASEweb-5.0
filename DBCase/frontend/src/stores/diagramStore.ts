import { defineStore } from 'pinia'

import { api } from '@/plugins/axios.ts'
import type { AnyDiagramInput } from '@/types/api'

export enum DiagramType {
  er = 'er',
  logical = 'logical',
  db = 'db',
}

export const useDiagramStore = defineStore('d', () => {
  async function save(
    diagram: AnyDiagramInput,
    type: DiagramType,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return api.diagram
      .generate(diagram, type, DiagramType.logical)
      .then((data) => {
        toast('Se ha guardado correctamente el diagrama' + data, 'success')
      })
      .catch((e) => {
        toast(e, 'error')
      })
  }

  function generate(
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    toast('hola', 'info')
  }

  function reset() {}

  return {
    save,
    generate,
    reset,
  }
})
