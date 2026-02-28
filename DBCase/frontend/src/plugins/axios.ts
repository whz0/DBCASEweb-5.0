import axios from 'axios';

const http = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export async function login(data: Object, onError: (message: string) => void) {
  http
    .post('/user/login', data)
    .then(() => {
      window.location.replace('/')
    })
    .catch((error) => {
      onError(error)
    })
}

