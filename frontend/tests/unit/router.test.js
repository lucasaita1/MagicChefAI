import { describe, expect, it } from 'vitest'
import { resolveAccess, routes } from '@/router'

describe('router guard', () => {
  it('sends anonymous users to login keeping the destination', () => {
    expect(resolveAccess({ meta: { requiresAuth: true }, fullPath: '/chef' }, false))
      .toEqual({ name: 'login', query: { redirect: '/chef' } })
    expect(resolveAccess({ meta: { requiresAuth: true }, fullPath: '/' }, false))
      .toEqual({ name: 'login', query: {} })
  })

  it('keeps logged users away from the login page', () => {
    expect(resolveAccess({ meta: { guestOnly: true } }, true)).toEqual({ name: 'recipes' })
  })

  it('lets everything else through', () => {
    expect(resolveAccess({ meta: { requiresAuth: true } }, true)).toBe(true)
    expect(resolveAccess({ meta: { guestOnly: true } }, false)).toBe(true)
    expect(resolveAccess({}, false)).toBe(true)
  })

  it('protects every private page', () => {
    const privatePages = routes.filter((route) => route.meta?.requiresAuth).map((route) => route.name)
    expect(privatePages).toEqual(['recipes', 'chef', 'pantry'])
  })
})
