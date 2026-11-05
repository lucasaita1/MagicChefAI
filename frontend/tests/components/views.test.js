import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { flush, makeRouter, sampleItem, sampleRecipe, setupPinia } from '../helpers'

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
const LoginView = (await import('@/views/LoginView.vue')).default
const ChefChatView = (await import('@/views/ChefChatView.vue')).default
const RecipesView = (await import('@/views/RecipesView.vue')).default
const PantryView = (await import('@/views/PantryView.vue')).default
const AppHeader = (await import('@/components/AppHeader.vue')).default
const App = (await import('@/App.vue')).default

const SESSION = { token: 'jwt', user: { id: 1, name: 'Lucas Aita', email: 'l@mail.com', emailVerified: true }, expiresAt: '2999-01-01T00:00:00Z' }

async function mountView(component, path = '/') {
  const pinia = setupPinia()
  const router = makeRouter()
  await router.push(path)
  await router.isReady()
  const wrapper = mount(component, { global: { plugins: [pinia, router], stubs: { teleport: true } }, attachTo: document.body })
  await flush()
  return { wrapper, router }
}

function login() {
  useAuthStore().persist(SESSION)
}

beforeEach(() => {
  vi.clearAllMocks()
  sessionStorage.clear()
  Element.prototype.scrollTo = vi.fn()
  api.listPantry.mockResolvedValue([sampleItem(), sampleItem({ id: 11, name: 'Arroz', location: 'CUPBOARD', category: 'GRAIN' })])
  api.listRecipes.mockResolvedValue([])
})

describe('LoginView', () => {
  it('logs in and redirects to the requested page', async () => {
    api.login.mockResolvedValue(SESSION)
    const { wrapper, router } = await mountView(LoginView, '/login?redirect=/despensa')

    await wrapper.find('#email').setValue('l@mail.com')
    await wrapper.find('#password').setValue('segredo123')
    await wrapper.find('form').trigger('submit')
    await flush()

    expect(api.login).toHaveBeenCalledWith({ email: 'l@mail.com', password: 'segredo123' })
    expect(router.currentRoute.value.fullPath).toBe('/despensa')
  })

  it('registers a new account through the second tab', async () => {
    api.register.mockResolvedValue(SESSION)
    const { wrapper, router } = await mountView(LoginView, '/login')

    await wrapper.findAll('[role="tab"]')[1].trigger('click')
    await wrapper.find('#name').setValue('Lucas')
    await wrapper.find('#email').setValue('l@mail.com')
    await wrapper.find('#password').setValue('segredo123')
    await wrapper.find('[aria-label="Mostrar senha"]').trigger('click')
    expect(wrapper.find('#password').attributes('type')).toBe('text')
    await wrapper.find('form').trigger('submit')
    await flush()

    expect(api.register).toHaveBeenCalledWith({ name: 'Lucas', email: 'l@mail.com', password: 'segredo123' })
    expect(router.currentRoute.value.name).toBe('recipes')
  })

  it('makes the robot watch the typing and cover its eyes when the password is revealed', async () => {
    const { wrapper } = await mountView(LoginView, '/login')
    const robot = () => wrapper.find('.hero-bot')

    await wrapper.find('#email').trigger('focus')
    await wrapper.find('#email').setValue('lucas@mail.com')
    expect(robot().classes()).toContain('is-idle')
    expect(robot().find('.eyes').attributes('style')).toMatch(/translate\((?!0px, 0px)/)

    await wrapper.find('#email').trigger('blur')
    await wrapper.find('#password').setValue('segredo123')
    await wrapper.find('[aria-label="Mostrar senha"]').trigger('click')
    expect(robot().classes()).toContain('is-shy')

    await wrapper.find('[aria-label="Ocultar senha"]').trigger('click')
    expect(robot().classes()).not.toContain('is-shy')
  })

  it('shows API errors and the expired session notice', async () => {
    api.login.mockRejectedValue(Object.assign(new Error('E-mail ou senha inválidos.'), { fieldErrors: { email: 'E-mail inválido' } }))
    const { wrapper } = await mountView(LoginView, '/login?expired=1')
    expect(wrapper.text()).toContain('Sua sessão expirou')

    await wrapper.find('form').trigger('submit')
    await flush()

    expect(wrapper.find('[role="alert"]').text()).toBe('E-mail ou senha inválidos.')
    expect(wrapper.text()).toContain('E-mail inválido')
  })
})

describe('ChefChatView', () => {
  it('greets, sends ingredients with pantry items and renders the recipe', async () => {
    login()
    api.generateRecipe.mockResolvedValue(sampleRecipe({ chefComment: 'Bip bop, saiu do forno!' }))
    const { wrapper } = await mountView(ChefChatView, '/chef?items=10')

    expect(wrapper.text()).toContain('Olá, Lucas!')
    expect(wrapper.find('.selected').text()).toContain('Leite')

    await wrapper.find('textarea').setValue('Tenho ovos')
    await wrapper.find('.send').trigger('click')
    await flush()

    expect(api.generateRecipe).toHaveBeenCalledWith({ message: 'Tenho ovos', pantryItemIds: [10] })
    expect(wrapper.text()).toContain('Bip bop, saiu do forno!')
    expect(wrapper.find('.bubble.recipe').text()).toContain('Omelete do Chef')
  })

  it('shows refusals as chef error bubbles and restores the selection', async () => {
    login()
    api.generateRecipe.mockRejectedValue(new Error('O Chef Magic só fala de culinária!'))
    const { wrapper } = await mountView(ChefChatView, '/chef')

    await wrapper.find('.pantry-btn').trigger('click')
    await wrapper.findAll('.picker-panel .item')[0].trigger('click')
    await wrapper.find('textarea').trigger('keydown', { key: 'Enter' })
    await flush()

    expect(api.generateRecipe).toHaveBeenCalledWith({ message: expect.stringContaining('despensa'), pantryItemIds: [10] })
    expect(wrapper.find('.bubble.error').text()).toContain('só fala de culinária')
    expect(wrapper.find('.selected').text()).toContain('Leite')

    await wrapper.find('.selected .x').trigger('click')
    expect(wrapper.find('.selected').exists()).toBe(false)
  })

  it('fills the input from a suggestion and ignores empty sends', async () => {
    login()
    const { wrapper } = await mountView(ChefChatView, '/chef')

    await wrapper.find('.send').trigger('click')
    expect(api.generateRecipe).not.toHaveBeenCalled()

    await wrapper.find('.suggestion').trigger('click')
    expect(wrapper.find('textarea').element.value).toBe('Ovos, queijo e tomate')
  })
})

describe('RecipesView', () => {
  it('lists, filters, opens, favorites and deletes recipes', async () => {
    login()
    api.listRecipes.mockResolvedValue([sampleRecipe(), sampleRecipe({ id: 2, title: 'Bolo de cenoura', favorite: true })])
    api.toggleFavorite.mockResolvedValue(sampleRecipe({ favorite: true }))
    api.deleteRecipe.mockResolvedValue(null)
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const { wrapper } = await mountView(RecipesView)

    expect(wrapper.text()).toContain('Olá, Lucas!')
    expect(wrapper.findAll('.card')).toHaveLength(2)

    await wrapper.findAll('.segmented button')[1].trigger('click')
    expect(wrapper.findAll('.card')).toHaveLength(1)
    await wrapper.findAll('.segmented button')[0].trigger('click')

    await wrapper.find('.search').setValue('bolo')
    expect(wrapper.findAll('.card')).toHaveLength(1)
    await wrapper.find('.search').setValue('')

    await wrapper.findAll('.card')[0].trigger('click')
    expect(wrapper.find('.modal').text()).toContain('Modo de preparo')

    await wrapper.find('.modal-actions .btn').trigger('click')
    await flush()
    expect(api.toggleFavorite).toHaveBeenCalledWith(1)

    await wrapper.find('.modal-actions .btn-danger').trigger('click')
    await flush()
    expect(api.deleteRecipe).toHaveBeenCalledWith(1)
    expect(wrapper.find('.modal').exists()).toBe(false)
  })

  it('shows an empty state and load errors', async () => {
    login()
    api.listRecipes.mockRejectedValue(new Error('Servidor fora do ar'))
    const { wrapper } = await mountView(RecipesView)

    expect(wrapper.text()).toContain('Servidor fora do ar')
    expect(wrapper.text()).toContain('Seu livro de receitas está vazio')
  })
})

describe('PantryView', () => {
  it('shows both columns, adds, edits, removes and sends selection to the chef', async () => {
    login()
    api.createPantryItem.mockResolvedValue(sampleItem({ id: 12, name: 'Alho', location: 'CUPBOARD' }))
    api.updatePantryItem.mockResolvedValue(sampleItem({ name: 'Leite integral' }))
    api.deletePantryItem.mockResolvedValue(null)
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const { wrapper, router } = await mountView(PantryView, '/despensa')

    expect(wrapper.find('.column.fridge').text()).toContain('Leite')
    expect(wrapper.find('.column.cupboard').text()).toContain('Arroz')

    await wrapper.findAll('.add')[1].trigger('click')
    await wrapper.find('#item-name').setValue('Alho')
    await wrapper.find('.modal form').trigger('submit')
    await flush()
    expect(api.createPantryItem).toHaveBeenCalledWith(expect.objectContaining({ name: 'Alho', location: 'CUPBOARD' }))

    await wrapper.find('[aria-label="Editar Leite"]').trigger('click')
    await wrapper.find('.modal form').trigger('submit')
    await flush()
    expect(api.updatePantryItem).toHaveBeenCalledWith(10, expect.objectContaining({ name: 'Leite' }))

    await wrapper.find('.column.cupboard .item').trigger('click')
    expect(wrapper.find('.cook-bar').text()).toContain('1 item selecionado')
    await wrapper.find('.cook-bar .btn-primary').trigger('click')
    await flush()
    expect(router.currentRoute.value.fullPath).toMatch(/^\/chef\?items=1[12]$/)

    await wrapper.find('[aria-label="Remover Arroz"]').trigger('click')
    await flush()
    expect(api.deletePantryItem).toHaveBeenCalledWith(11)
  })

  it('flags items close to expiring', async () => {
    login()
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    const soon = [tomorrow.getFullYear(), tomorrow.getMonth() + 1, tomorrow.getDate()].map((n) => String(n).padStart(2, '0')).join('-')
    api.listPantry.mockResolvedValue([sampleItem({ expiresAt: soon }), sampleItem({ id: 2, name: 'Iogurte', expiresAt: '2000-01-01' })])
    const { wrapper } = await mountView(PantryView, '/despensa')

    expect(wrapper.text()).toMatch(/Vence em 1d/)
    expect(wrapper.text()).toContain('Vencido')
    expect(wrapper.find('.column.cupboard').text()).toContain('Armário vazio')
  })
})

describe('AppHeader and App shell', () => {
  it('shows the user menu and logs out clearing every store', async () => {
    login()
    api.logout.mockResolvedValue(null)
    const { wrapper, router } = await mountView(AppHeader)

    await wrapper.find('.user-btn').trigger('click')
    expect(wrapper.find('.dropdown').text()).toContain('l@mail.com')

    await wrapper.find('.dropdown .item').trigger('click')
    await flush()

    expect(api.logout).toHaveBeenCalled()
    expect(useAuthStore().token).toBeNull()
    expect(router.currentRoute.value.name).toBe('login')
  })

  it('closes the menu on outside click', async () => {
    login()
    const { wrapper } = await mountView(AppHeader)
    await wrapper.find('.user-btn').trigger('click')
    document.body.click()
    await flush()
    expect(wrapper.find('.dropdown').exists()).toBe(false)
  })

  it('renders the header only on private routes', async () => {
    const { wrapper } = await mountView(App, '/login')
    expect(wrapper.find('.header').exists()).toBe(false)
  })
})
