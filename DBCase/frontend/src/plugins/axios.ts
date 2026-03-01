import axios from 'axios';

const http = axios.create({
  baseURL: 'http://127.0.0.1:8080/api'
});

export async function login(
  data: Object,
  onError: (message: string, severity: 'error' | 'warn' | 'info') => void
) {
  http
    .post('/user/login', data)
    .then(() => {
      window.location.replace('/')
    })
    .catch((error) => {
      let message;
      let severity: 'error' | 'warn' | 'info' = 'error';

      if (error.response) {
        message = error.response.data?.message || 'Error de autenticación';
        severity = error.response.status >= 500 ? 'error' : 'warn';
      } else if (error.request) {
        message = 'No se pudo contactar con el servidor';
        severity = 'error';
      } else {
        message = error.message;
      }

      onError(message, severity);
    })
}
