import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { flush, makeRouter, setupPinia } from '../helpers'

vi.mock('@/services/api', () => ({
  api: {
    me: vi.fn(),
    verifyEmail: vi.fn(),
    resendVerification: vi.fn(),
    listPantry: vi.fn(),
    generateRecipe: vi.fn()
  }
}))

const { api } = await import('@/services/api')
const { useAuthStore } = await import('@/stores/auth')
const VerifyEmailView = (await import('@/views/VerifyEmailView.vue')).default
const VerifyEmailBanner = (await import('@/components/VerifyEmailBanner.vue')).default
const ChefChatView = (await import('@/views/ChefChatView.vue')).default
const App = (await import('@/App.vue')).default

const USER = { id: 1, name: 'Lucas', email: 'l@mail.com', emailVerified: false }

async function mountWith(component, path, { logged = true } = {}) {
  const pinia = setupPinia()
  if (logged) useAuthStore().persist({ token: 'jwt', user: { ...USER }, expiresAt: '2999-01-01T00:00:00Z' })
  const router = makeRouter()
  router.addRoute({ path: '/verificar-email', name: 'verify-email', component: VerifyEmailView })
  router.addRoute({ path: '/privada', name: 'private', component: { template: '<div />' }, meta: { requiresAuth: true } })
  await router.push(path)
  await router.isReady()
  const wrapper = mount(component, { global: { plugins: [pinia, router] } })
  await flush()
  return wrapper
}

beforeEach(() => {
  vi.clearAllMocks()
  sessionStorage.clear()
  Element.prototype.scrollTo = vi.fn()
  api.listPantry.mockResolvedValue([])
})

describe('VerifyEmailView', () => {
  it('confirms the token and unlocks the chef', async () => {
    api.verifyEmail.mockResolvedValue({ ...USER, emailVerified: true })
    const wrapper = await mountWith(VerifyEmailView, '/verificar-email?token=abc')

    expect(api.verifyEmail).toHaveBeenCalledWith('abc')
    expect(wrapper.text()).toContain('confirmado')
    expect(wrapper.text()).toContain('Pedir minha primeira receita')
    expect(useAuthStore().isVerified).toBe(true)
  })

  it('shows API errors and handles a missing token', async () => {
    api.verifyEmail.mockRejectedValue(new Error('Link de confirmação inválido ou expirado.'))
    const invalid = await mountWith(VerifyEmailView, '/verificar-email?token=old', { logged: false })
    expect(invalid.text()).toContain('inválido ou expirado')
    expect(invalid.text()).toContain('Ir para o login')

    const missing = await mountWith(VerifyEmailView, '/verificar-email')
    expect(missing.text()).toContain('incompleto')
    expect(missing.text()).toContain('pedir um novo e-mail')
  })

  it('offers login after confirming when logged out', async () => {
    api.verifyEmail.mockResolvedValue({ ...USER, emailVerified: true })
    const wrapper = await mountWith(VerifyEmailView, '/verificar-email?token=abc', { logged: false })
    expect(wrapper.text()).toContain('Entrar na cozinha')
  })
})

describe('VerifyEmailBanner', () => {
  it('resends the e-mail and reports provider errors', async () => {
    api.resendVerification.mockResolvedValueOnce(null).mockRejectedValueOnce(new Error('Aguarde um minuto'))
    const wrapper = await mountWith(VerifyEmailBanner, '/')
    expect(wrapper.text()).toContain('l@mail.com')

    await wrapper.findAll('button')[1].trigger('click')
    await flush()
    expect(wrapper.text()).toContain('Novo link enviado')

    await wrapper.findAll('button')[1].trigger('click')
    await flush()
    expect(wrapper.text()).toContain('Aguarde um minuto')
  })

  it('checks again when the user says the e-mail was confirmed', async () => {
    api.me.mockResolvedValueOnce({ ...USER }).mockRejectedValueOnce(new Error('offline'))
    const wrapper = await mountWith(VerifyEmailBanner, '/')

    await wrapper.findAll('button')[0].trigger('click')
    await flush()
    expect(wrapper.text()).toContain('Ainda não identificamos')

    await wrapper.findAll('button')[0].trigger('click')
    await flush()
    expect(wrapper.text()).toContain('offline')
  })
})

describe('locked chef chat', () => {
  it('explains the lock and hides the composer until the e-mail is confirmed', async () => {
    const wrapper = await mountWith(ChefChatView, '/chef')

    expect(wrapper.text()).toContain('confirme seu e-mail')
    expect(wrapper.find('.locked').exists()).toBe(true)
    expect(wrapper.find('textarea').exists()).toBe(false)
    expect(wrapper.find('.suggestions').exists()).toBe(false)
  })
})

describe('App shell', () => {
  it('shows the banner on private pages and re-checks the user when the tab regains focus', async () => {
    api.me.mockResolvedValue({ ...USER })
    const wrapper = await mountWith(App, '/privada')

    expect(api.me).toHaveBeenCalledTimes(1)
    expect(wrapper.find('.banner').exists()).toBe(true)

    document.dispatchEvent(new Event('visibilitychange'))
    await flush()
    expect(api.me).toHaveBeenCalledTimes(2)
    wrapper.unmount()
  })
})
