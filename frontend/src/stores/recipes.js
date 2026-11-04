import { defineStore } from 'pinia'
import { api } from '@/services/api'

export const useRecipesStore = defineStore('recipes', {
  state: () => ({ items: [], loaded: false, loading: false }),
  getters: {
    favorites: (state) => state.items.filter((recipe) => recipe.favorite)
  },
  actions: {
    async load(force = false) {
      if (this.loaded && !force) return
      this.loading = true
      try {
        this.items = await api.listRecipes()
        this.loaded = true
      } finally {
        this.loading = false
      }
    },
    async generate(message, pantryItemIds = []) {
      const recipe = await api.generateRecipe({ message, pantryItemIds })
      this.items = [recipe, ...this.items]
      return recipe
    },
    async toggleFavorite(id) {
      const updated = await api.toggleFavorite(id)
      this.items = this.items.map((recipe) => (recipe.id === id ? updated : recipe))
      return updated
    },
    async remove(id) {
      await api.deleteRecipe(id)
      this.items = this.items.filter((recipe) => recipe.id !== id)
    },
    reset() {
      this.items = []
      this.loaded = false
    }
  }
})
