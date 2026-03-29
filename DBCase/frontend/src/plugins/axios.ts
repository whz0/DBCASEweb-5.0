import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080/api',
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
  },
}

export default http
