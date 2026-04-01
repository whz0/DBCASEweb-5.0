import { ref } from 'vue'
import { defineStore } from "pinia";
import http from '@/plugins/axios.ts'
import type { Diagram } from "@/types/diagram-elements.ts";

export enum DiagramType {
  er = 'er',
  logical = 'logical',
  db = 'db',
}

export const useDiagramStore = defineStore(('d'), () => {

  const myDiagram = ref<Diagram | null>(null)

  async function save(diagram: any,
                      type: DiagramType,
                      toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success')
                        => void) {

    return http.post('diagram/generate', {
      type: type,
      diagram: diagram,
    })
      .then((data) => {
        myDiagram.value = data.data
        toast('Se ha guardado correctamente el diagrama', 'success')
      }).catch((e) => {
        toast(e, 'error')
      })
  }

  function generate(toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success')
    => void) {

    if(myDiagram.value == null) toast('No hay diagrama guardado para trasnformar',
      'error')
    else toast('Generado con éxito', 'success')
    return myDiagram
  }

  return  {
    save,
    generate
  }
})
