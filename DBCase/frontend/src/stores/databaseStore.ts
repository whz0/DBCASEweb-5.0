import { defineStore } from 'pinia'

import { api } from '@/plugins/axios.ts'
import type { DatabaseUrl } from '@/types/api.ts'

export enum DatabaseType {
  mysql = 'mysql',
  postgresql = 'postgresql',
  oracle = 'oracle',
}

export const useDatabaseStore = defineStore('database', () => {
  async function deploy(
    url: DatabaseUrl,
    username: string,
    password: string,
    code: string,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return api.database
      .execute(url, username, password, code)
      .then(() => {
        toast('Se ha generado la tablas correctamente', 'success')
      })
      .catch((e) => {
        toast(
          e?.response?.data?.message ?? e?.message ?? 'Error al transformar el diagrama',
          'error',
        )
      })
  }

  async function test(
    url: DatabaseUrl,
    username: string,
    password: string,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return api.database
      .test(url, username, password)
      .then(() => toast('Se ha realizado la conexión correctamente', 'success'))
      .catch(() => {
        toast('No se ha podido realizar la conexión', 'error')
      })
  }

  return {
    deploy,
    test,
  }
})
