import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'

export function setupPinia() {
  const pinia = createPinia()
  setActivePinia(pinia)
  return pinia
}

export function makeRouter() {
  const Stub = { template: '<div />' }
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'recipes', component: Stub },
      { path: '/login', name: 'login', component: Stub },
      { path: '/chef', name: 'chef', component: Stub },
      { path: '/despensa', name: 'pantry', component: Stub }
    ]
  })
}

export const flush = () => new Promise((resolve) => setTimeout(resolve, 0))

export const sampleRecipe = (overrides = {}) => ({
  id: 1,
  title: 'Omelete do Chef',
  summary: 'Fofinha e rápida',
  chefComment: 'Bip bop!',
  difficulty: 'FACIL',
  prepTimeMinutes: 10,
  servings: 2,
  ingredients: ['2 ovos', 'sal'],
  steps: ['Bata', 'Frite'],
  tips: ['Fogo baixo'],
  userRequest: 'ovos',
  favorite: false,
  createdAt: '2026-11-03T12:00:00Z',
  ...overrides
})

export const sampleItem = (overrides = {}) => ({
  id: 10,
  name: 'Leite',
  category: 'DAIRY',
  location: 'FRIDGE',
  quantity: 1,
  unit: 'LITER',
  expiresAt: null,
  ...overrides
})
