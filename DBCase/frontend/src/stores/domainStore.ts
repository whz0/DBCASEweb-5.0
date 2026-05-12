import { defineStore } from 'pinia'
import { ref } from 'vue'

import { api } from '@/plugins/axios.ts'

interface Domain {
  id: string
  name: string
  baseType: string
  values?: string[]
}

export const useDomainStore = defineStore('domain', () => {
  const domains = ref<Domain[]>([])

  async function load() {
    const { data } = await api.domain.getAll()
    domains.value = data.map((d, i) => ({
      id: `idDomain${i}`,
      name: d.name,
      baseType: d.base ?? d.name,
    }))
  }

  async function add(name: string, base: string) {
    await api.domain.add(name, base)
    await load()
  }

  async function remove(name: string) {
    await api.domain.delete(name)
    domains.value = domains.value.filter((d) => d.name !== name)
  }

  function reset() {
    domains.value = []
  }

  return { domains, load, add, remove, reset }
})
