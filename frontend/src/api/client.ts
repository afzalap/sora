import axios from 'axios'

// Axios instance — base URL is empty because Vite proxies /api/* to
// http://localhost:8080 in development (see vite.config.ts).
// In production, set VITE_API_BASE_URL to the server origin.
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor: attach the JWT from localStorage on every request.
// This is "token-per-request" auth — stateless, which matches our Spring
// Security setup where there is no server-side session.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('sora_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// For the streaming endpoint we use the raw fetch API (Axios doesn't support
// ReadableStream), so we also export a helper that attaches the JWT header.
export function authHeaders(): HeadersInit {
  const token = localStorage.getItem('sora_token')
  return token
    ? { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }
    : { 'Content-Type': 'application/json' }
}
