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
    isVerified: (state) => Boolean(state.user?.emailVerified),
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
    /** Reloads the user, e.g. after the e-mail was confirmed in another tab. */
    async refreshUser() {
      if (!this.token) return
      this.setUser(await api.me())
    },
    async verifyEmail(token) {
      const verified = await api.verifyEmail(token)
      if (this.user?.id === verified.id) this.setUser(verified)
      return verified
    },
    resendVerification() {
      return api.resendVerification()
    },
    setUser(user) {
      this.user = user
      this.save()
    },
    persist({ token, user, expiresAt }) {
      this.token = token
      this.user = user
      this.expiresAt = expiresAt
      this.save()
    },
    save() {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ token: this.token, user: this.user, expiresAt: this.expiresAt }))
    },
    clear() {
      this.token = null
      this.user = null
      this.expiresAt = null
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }
})
