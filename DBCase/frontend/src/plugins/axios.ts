import axios from 'axios'

import { DiagramType } from '@/stores/diagramStore'
import type { AnyDiagramInput, UserSettings } from '@/types/api'

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
    generate: (diagram: AnyDiagramInput, type: DiagramType, transformTo: DiagramType) =>
      http.post('/diagram/generate', {
        type,
        diagram: { ...diagram, type },
        transformTo,
      }),
  },
  database: {
    execute: (payload: {
      dbType: string
      host: string
      port: number
      dbName: string
      username: string
      password: string
      sql: string
    }) => http.post('/database/execute', payload),
  },
}
