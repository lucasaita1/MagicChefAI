import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api, ApiError, configureApi, request } from '@/services/api'

function respond(status, body) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => (body === undefined ? Promise.reject(new Error('no body')) : Promise.resolve(body))
  })
}

describe('api service', () => {
  let onUnauthorized

  beforeEach(() => {
    onUnauthorized = vi.fn()
    configureApi({ getToken: () => 'jwt-123', onUnauthorized })
    globalThis.fetch = vi.fn()
  })

  afterEach(() => vi.restoreAllMocks())

  it('sends bearer token, JSON body and credentials for the fingerprint cookie', async () => {
    fetch.mockReturnValue(respond(201, { id: 1 }))

    const result = await api.generateRecipe({ message: 'ovos', pantryItemIds: [1] })

    expect(result).toEqual({ id: 1 })
    const [url, options] = fetch.mock.calls[0]
    expect(url).toBe('/api/recipes/generate')
    expect(options.method).toBe('POST')
    expect(options.credentials).toBe('include')
    expect(options.headers.Authorization).toBe('Bearer jwt-123')
    expect(JSON.parse(options.body)).toEqual({ message: 'ovos', pantryItemIds: [1] })
  })

  it('returns null for 204 responses', async () => {
    fetch.mockReturnValue(respond(204))
    await expect(api.deleteRecipe(3)).resolves.toBeNull()
  })

  it('maps Problem Details into ApiError with field errors', async () => {
    fetch.mockReturnValue(respond(400, { detail: 'Dados inválidos', errors: { name: 'obrigatório' } }))

    const error = await api.createPantryItem({}).catch((e) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(400)
    expect(error.message).toBe('Dados inválidos')
    expect(error.fieldErrors).toEqual({ name: 'obrigatório' })
  })

  it('triggers the unauthorized handler on 401 from protected routes', async () => {
    fetch.mockReturnValue(respond(401, { detail: 'Sessão inválida' }))

    await expect(api.listRecipes()).rejects.toThrow('Sessão inválida')
    expect(onUnauthorized).toHaveBeenCalledOnce()
  })

  it('does not redirect on 401 from login (wrong password)', async () => {
    fetch.mockReturnValue(respond(401, { detail: 'E-mail ou senha inválidos.' }))

    await expect(api.login({ email: 'a', password: 'b' })).rejects.toThrow('E-mail ou senha inválidos.')
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('uses fallback messages when the body is missing', async () => {
    fetch.mockReturnValue(respond(429))
    await expect(request('/x')).rejects.toThrow('Muitas requisições')

    fetch.mockReturnValue(respond(503))
    await expect(request('/x')).rejects.toThrow('Algo deu errado')
  })

  it('reports network failures', async () => {
    fetch.mockRejectedValue(new TypeError('Failed to fetch'))
    const error = await api.me().catch((e) => e)
    expect(error.status).toBe(0)
    expect(error.message).toMatch(/conexão/)
  })

  it('omits the Authorization header without a token', async () => {
    configureApi({ getToken: () => null })
    fetch.mockReturnValue(respond(200, []))

    await api.listPantry()

    expect(fetch.mock.calls[0][1].headers.Authorization).toBeUndefined()
  })

  it('exposes the error code and the verification endpoints', async () => {
    fetch.mockReturnValue(respond(403, { detail: 'Confirme', code: 'EMAIL_NOT_VERIFIED' }))
    const error = await api.generateRecipe({}).catch((e) => e)
    expect(error.code).toBe('EMAIL_NOT_VERIFIED')

    fetch.mockReturnValue(respond(200, { id: 1 }))
    await api.verifyEmail('abc')
    await api.resendVerification()
    expect(JSON.parse(fetch.mock.calls[1][1].body)).toEqual({ token: 'abc' })
    expect(fetch.mock.calls.slice(1).map(([url]) => url)).toEqual(['/api/auth/verify-email', '/api/auth/resend-verification'])
  })

  it('exposes every endpoint', async () => {
    fetch.mockReturnValue(respond(200, {}))
    await api.register({})
    await api.logout()
    await api.updatePantryItem(1, {})
    await api.deletePantryItem(1)
    await api.toggleFavorite(1)
    expect(fetch.mock.calls.map(([url, options]) => `${options.method} ${url}`)).toEqual([
      'POST /api/auth/register',
      'POST /api/auth/logout',
      'PUT /api/pantry/1',
      'DELETE /api/pantry/1',
      'PATCH /api/recipes/1/favorite'
    ])
  })
})
