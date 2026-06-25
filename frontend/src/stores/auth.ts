import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '@/api/client'
import type { AuthResponse } from '@/types'

// defineStore(id, setup) — the "setup store" syntax mirrors Vue's
// Composition API. ref() = state, computed() = getter, function = action.
// Pinia automatically makes this reactive and devtools-inspectable.
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('sora_token'))
  const username = ref<string | null>(localStorage.getItem('sora_username'))
  const error = ref<string | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  function persist(t: string, u: string) {
    token.value = t
    username.value = u
    localStorage.setItem('sora_token', t)
    localStorage.setItem('sora_username', u)
  }

  function clear() {
    token.value = null
    username.value = null
    localStorage.removeItem('sora_token')
    localStorage.removeItem('sora_username')
  }

  async function login(u: string, password: string) {
    error.value = null
    loading.value = true
    try {
      const { data } = await apiClient.post<AuthResponse>('/api/auth/login', {
        username: u,
        password,
      })
      persist(data.token, u)
    } catch {
      error.value = 'Invalid username or password.'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  async function register(u: string, password: string) {
    error.value = null
    loading.value = true
    try {
      const { data } = await apiClient.post<AuthResponse>('/api/auth/register', {
        username: u,
        password,
      })
      persist(data.token, u)
    } catch (e: unknown) {
      const msg =
        (e as { response?: { data?: string } })?.response?.data === 'Username already taken'
          ? 'That username is taken — try another.'
          : 'Could not create account. Try again.'
      error.value = msg
      throw msg
    } finally {
      loading.value = false
    }
  }

  function logout() {
    clear()
  }

  return { token, username, isLoggedIn, error, loading, login, register, logout }
})
