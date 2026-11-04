import { defineStore } from 'pinia'
import { api } from '@/services/api'

export const usePantryStore = defineStore('pantry', {
  state: () => ({ items: [], loaded: false, loading: false }),
  getters: {
    fridge: (state) => state.items.filter((item) => item.location === 'FRIDGE'),
    cupboard: (state) => state.items.filter((item) => item.location === 'CUPBOARD')
  },
  actions: {
    async load(force = false) {
      if (this.loaded && !force) return
      this.loading = true
      try {
        this.items = await api.listPantry()
        this.loaded = true
      } finally {
        this.loading = false
      }
    },
    async add(item) {
      const created = await api.createPantryItem(item)
      this.items = [...this.items, created].sort((a, b) => a.name.localeCompare(b.name))
      return created
    },
    async update(id, item) {
      const updated = await api.updatePantryItem(id, item)
      this.items = this.items.map((current) => (current.id === id ? updated : current))
      return updated
    },
    async remove(id) {
      await api.deletePantryItem(id)
      this.items = this.items.filter((item) => item.id !== id)
    },
    reset() {
      this.items = []
      this.loaded = false
    }
  }
})
