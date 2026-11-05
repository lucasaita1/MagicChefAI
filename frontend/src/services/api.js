const BASE_URL = import.meta.env.VITE_API_URL ?? '/api'

export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}, code = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
    this.code = code
  }
}

let tokenProvider = () => null
let unauthorizedHandler = () => {}

/** The auth store plugs in how to read the token and what to do on 401. */
export function configureApi({ getToken, onUnauthorized }) {
  tokenProvider = getToken ?? tokenProvider
  unauthorizedHandler = onUnauthorized ?? unauthorizedHandler
}

const FALLBACK_MESSAGES = {
  0: 'Não foi possível falar com o servidor. Verifique sua conexão.',
  401: 'Sua sessão expirou. Faça login novamente.',
  429: 'Muitas requisições. Aguarde um instante.',
  500: 'Algo deu errado no servidor. Tente novamente.'
}

export async function request(path, { method = 'GET', body, skipAuthRedirect = false } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = tokenProvider()
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      // Sends the HttpOnly fingerprint cookie that binds the JWT to this browser
      credentials: 'include',
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch {
    throw new ApiError(0, FALLBACK_MESSAGES[0])
  }

  if (response.status === 204) return null

  const data = await response.json().catch(() => null)
  if (!response.ok) {
    if (response.status === 401 && !skipAuthRedirect) unauthorizedHandler()
    const message = data?.detail || FALLBACK_MESSAGES[response.status] || FALLBACK_MESSAGES[500]
    throw new ApiError(response.status, message, data?.errors ?? {}, data?.code ?? null)
  }
  return data
}

export const api = {
  register: (payload) => request('/auth/register', { method: 'POST', body: payload, skipAuthRedirect: true }),
  login: (payload) => request('/auth/login', { method: 'POST', body: payload, skipAuthRedirect: true }),
  logout: () => request('/auth/logout', { method: 'POST', skipAuthRedirect: true }),
  me: () => request('/auth/me'),
  verifyEmail: (token) => request('/auth/verify-email', { method: 'POST', body: { token }, skipAuthRedirect: true }),
  resendVerification: () => request('/auth/resend-verification', { method: 'POST' }),

  listPantry: () => request('/pantry'),
  createPantryItem: (item) => request('/pantry', { method: 'POST', body: item }),
  updatePantryItem: (id, item) => request(`/pantry/${id}`, { method: 'PUT', body: item }),
  deletePantryItem: (id) => request(`/pantry/${id}`, { method: 'DELETE' }),

  listRecipes: () => request('/recipes'),
  generateRecipe: (payload) => request('/recipes/generate', { method: 'POST', body: payload }),
  toggleFavorite: (id) => request(`/recipes/${id}/favorite`, { method: 'PATCH' }),
  deleteRecipe: (id) => request(`/recipes/${id}`, { method: 'DELETE' })
}
