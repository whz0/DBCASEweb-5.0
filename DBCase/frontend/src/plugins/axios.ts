import axios from 'axios'

import { DiagramType } from '@/stores/diagramStore'
import type { AnyDiagramInput, CustomDomain, DatabaseUrl, UserSettings } from '@/types/api'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
})

export const api = {
  user: {
    login: (credentials: object) => http.post('/user/login', credentials),
    register: (credentials: object) => http.post('/user/register', credentials),
    logout: () => http.post('/user/logout'),
    getMe: () => http.get('/user/me'),
    saveChart: (chartJson: string) =>
      http.post('/user/chart', chartJson, {
        headers: { 'Content-Type': 'text/plain' },
      }),
    saveSettings: (settings: UserSettings) => http.post('/user/settings', settings),
  },
  diagram: {
    generate: (diagram: AnyDiagramInput, type: DiagramType, transformTo: DiagramType) => {
      let normalizedDiagram = diagram
      if (type === DiagramType.er) {
        const snapshot = diagram as import('@/types/er-diagram-elements').Snapshot
        normalizedDiagram = {
          ...snapshot,
          relationships: snapshot.relationships.map((r) => {
            if (r.type === 'Aggregation' && !r.aggregationName) {
              return { ...r, aggregationName: r.name }
            }
            return r
          }),
        }
      }
      return http.post('/diagram/generate', {
        type,
        diagram: { ...normalizedDiagram, type },
        transformTo,
      })
    },
  },
  domain: {
    getAll: () => http.get<CustomDomain[]>('/domain/data-types'),
    add: (name: string, base: string) => http.post('/domain/add', { name, base }),
    delete: (name: string) =>
      http.delete('/domain/delete', {
        data: name,
        headers: { 'Content-Type': 'text/plain' },
      }),
  },
  database: {
    test: (databaseUrl: DatabaseUrl, username: string, password: string) =>
      http.post('/database/test', {
        databaseUrl,
        username,
        password,
      }),
    execute: (databaseUrl: DatabaseUrl, username: string, password: string, sql: string) =>
      http.post('/database/execute', {
        databaseUrl,
        username,
        password,
        sql,
      }),
  },
}
