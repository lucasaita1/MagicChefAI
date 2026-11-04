import { defineStore } from 'pinia'
import { api } from '@/services/api'

const STORAGE_KEY = 'mc_session'

function readSession() {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY)) ?? null
  } catch {
    return null
  }
}

/**
 * The JWT lives in sessionStorage (cleared when the tab closes). Even if a script stole it,
 * the API rejects it without the HttpOnly fingerprint cookie that JavaScript cannot read.
 */
export const useAuthStore = defineStore('auth', {
  state: () => {
    const session = readSession()
    return { token: session?.token ?? null, user: session?.user ?? null, expiresAt: session?.expiresAt ?? null }
  },
  getters: {
    isAuthenticated: (state) => Boolean(state.token) && (!state.expiresAt || new Date(state.expiresAt) > new Date()),
    firstName: (state) => state.user?.name?.split(' ')[0] ?? ''
  },
  actions: {
    async login(credentials) {
      this.persist(await api.login(credentials))
    },
    async register(payload) {
      this.persist(await api.register(payload))
    },
    async logout() {
      try {
        if (this.token) await api.logout()
      } finally {
        this.clear()
      }
    },
    persist({ token, user, expiresAt }) {
      this.token = token
      this.user = user
      this.expiresAt = expiresAt
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ token, user, expiresAt }))
    },
    clear() {
      this.token = null
      this.user = null
      this.expiresAt = null
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }
})
