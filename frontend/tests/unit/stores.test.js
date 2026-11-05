import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setupPinia, sampleItem, sampleRecipe } from '../helpers'

vi.mock('@/services/api', () => ({
  api: {
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    me: vi.fn(),
    verifyEmail: vi.fn(),
    resendVerification: vi.fn(),
    listPantry: vi.fn(),
    createPantryItem: vi.fn(),
    updatePantryItem: vi.fn(),
    deletePantryItem: vi.fn(),
    listRecipes: vi.fn(),
    generateRecipe: vi.fn(),
    toggleFavorite: vi.fn(),
    deleteRecipe: vi.fn()
  }
}))

const { api } = await import('@/services/api')
const { useAuthStore } = await import('@/stores/auth')
const { usePantryStore } = await import('@/stores/pantry')
const { useRecipesStore } = await import('@/stores/recipes')

const SESSION = { token: 'jwt', user: { id: 1, name: 'Lucas Aita', email: 'l@mail.com', emailVerified: true }, expiresAt: '2999-01-01T00:00:00Z' }

beforeEach(() => {
  vi.clearAllMocks()
  sessionStorage.clear()
  setupPinia()
})

describe('auth store', () => {
  it('logs in and persists the session in sessionStorage', async () => {
    api.login.mockResolvedValue(SESSION)
    const auth = useAuthStore()

    await auth.login({ email: 'l@mail.com', password: 'x' })

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.firstName).toBe('Lucas')
    expect(JSON.parse(sessionStorage.getItem('mc_session')).token).toBe('jwt')
  })

  it('registers and restores a previous session', async () => {
    api.register.mockResolvedValue(SESSION)
    await useAuthStore().register({})

    setupPinia()
    expect(useAuthStore().token).toBe('jwt')
  })

  it('treats expired sessions as logged out and survives corrupted storage', () => {
    sessionStorage.setItem('mc_session', JSON.stringify({ ...SESSION, expiresAt: '2000-01-01T00:00:00Z' }))
    expect(useAuthStore().isAuthenticated).toBe(false)

    sessionStorage.setItem('mc_session', '{broken')
    setupPinia()
    expect(useAuthStore().token).toBeNull()
  })

  it('always clears local state on logout, even if the API fails', async () => {
    api.login.mockResolvedValue(SESSION)
    api.logout.mockRejectedValue(new Error('offline'))
    const auth = useAuthStore()
    await auth.login({})

    await expect(auth.logout()).rejects.toThrow('offline')

    expect(auth.token).toBeNull()
    expect(sessionStorage.getItem('mc_session')).toBeNull()
  })

  it('skips the API call on logout without a token', async () => {
    await useAuthStore().logout()
    expect(api.logout).not.toHaveBeenCalled()
  })
})

describe('pantry store', () => {
  it('loads once, splits by location and supports CRUD', async () => {
    api.listPantry.mockResolvedValue([sampleItem(), sampleItem({ id: 11, name: 'Arroz', location: 'CUPBOARD' })])
    const pantry = usePantryStore()

    await pantry.load()
    await pantry.load()
    expect(api.listPantry).toHaveBeenCalledOnce()
    expect(pantry.fridge).toHaveLength(1)
    expect(pantry.cupboard[0].name).toBe('Arroz')

    api.createPantryItem.mockResolvedValue(sampleItem({ id: 12, name: 'Alho' }))
    await pantry.add({})
    expect(pantry.items.map((item) => item.name)).toEqual(['Alho', 'Arroz', 'Leite'])

    api.updatePantryItem.mockResolvedValue(sampleItem({ name: 'Leite integral' }))
    await pantry.update(10, {})
    expect(pantry.items.find((item) => item.id === 10).name).toBe('Leite integral')

    api.deletePantryItem.mockResolvedValue(null)
    await pantry.remove(10)
    expect(pantry.items).toHaveLength(2)

    pantry.reset()
    expect(pantry.loaded).toBe(false)
  })
})

describe('recipes store', () => {
  it('generates, favorites and removes recipes', async () => {
    api.listRecipes.mockResolvedValue([sampleRecipe()])
    const recipes = useRecipesStore()
    await recipes.load()

    api.generateRecipe.mockResolvedValue(sampleRecipe({ id: 2, title: 'Bolo' }))
    const created = await recipes.generate('farinha', [1])
    expect(api.generateRecipe).toHaveBeenCalledWith({ message: 'farinha', pantryItemIds: [1] })
    expect(created.title).toBe('Bolo')
    expect(recipes.items[0].id).toBe(2)

    api.toggleFavorite.mockResolvedValue(sampleRecipe({ favorite: true }))
    await recipes.toggleFavorite(1)
    expect(recipes.favorites).toHaveLength(1)

    api.deleteRecipe.mockResolvedValue(null)
    await recipes.remove(1)
    expect(recipes.items).toHaveLength(1)

    recipes.reset()
    expect(recipes.items).toEqual([])
  })

  it('resets the loading flag when the request fails', async () => {
    api.listRecipes.mockRejectedValue(new Error('boom'))
    const recipes = useRecipesStore()

    await expect(recipes.load()).rejects.toThrow('boom')
    expect(recipes.loading).toBe(false)
  })
})

describe('auth store e-mail verification', () => {
  it('refreshes the user and confirms the e-mail', async () => {
    api.login.mockResolvedValue({ ...SESSION, user: { ...SESSION.user, emailVerified: false } })
    const auth = useAuthStore()
    await auth.login({})
    expect(auth.isVerified).toBe(false)

    api.verifyEmail.mockResolvedValue({ ...SESSION.user, emailVerified: true })
    await auth.verifyEmail('tok')
    expect(api.verifyEmail).toHaveBeenCalledWith('tok')
    expect(auth.isVerified).toBe(true)
    expect(JSON.parse(sessionStorage.getItem('mc_session')).user.emailVerified).toBe(true)

    api.me.mockResolvedValue({ ...SESSION.user, name: 'Lucas Novo' })
    await auth.refreshUser()
    expect(auth.firstName).toBe('Lucas')
    expect(auth.user.name).toBe('Lucas Novo')

    api.resendVerification.mockResolvedValue(null)
    await auth.resendVerification()
    expect(api.resendVerification).toHaveBeenCalled()
  })

  it('does not touch another logged user when verifying a foreign link, nor refresh without a token', async () => {
    const auth = useAuthStore()
    await auth.refreshUser()
    expect(api.me).not.toHaveBeenCalled()

    api.verifyEmail.mockResolvedValue({ id: 99, emailVerified: true })
    await auth.verifyEmail('tok')
    expect(auth.user).toBeNull()
  })
})
