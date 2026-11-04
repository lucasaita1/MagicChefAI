import { describe, expect, it } from 'vitest'
import { daysUntil, formatDate, formatMinutes, formatQuantity } from '@/utils/format'
import { categoryOf, difficultyOf, unitOf } from '@/utils/labels'

describe('format utils', () => {
  it('formats quantities with short units', () => {
    expect(formatQuantity(2, 'KILOGRAM')).toBe('2 kg')
    expect(formatQuantity('1.5', 'LITER')).toBe('1,5 L')
    expect(formatQuantity(3, 'UNKNOWN')).toBe('3 un')
  })

  it('formats minutes in a friendly way', () => {
    expect(formatMinutes(null)).toBe('—')
    expect(formatMinutes(45)).toBe('45 min')
    expect(formatMinutes(60)).toBe('1h')
    expect(formatMinutes(95)).toBe('1h 35min')
  })

  it('formats dates in pt-BR and tolerates empty values', () => {
    expect(formatDate(null)).toBe('')
    expect(formatDate('2026-11-03T12:00:00Z')).toMatch(/2026/)
  })

  it('counts days until a date', () => {
    const today = new Date(2026, 10, 3)
    expect(daysUntil(null, today)).toBeNull()
    expect(daysUntil('2026-11-03', today)).toBe(0)
    expect(daysUntil('2026-11-06', today)).toBe(3)
    expect(daysUntil('2026-11-01', today)).toBe(-2)
  })
})

describe('labels', () => {
  it('falls back to safe defaults', () => {
    expect(categoryOf('MEAT').label).toBe('Carnes')
    expect(categoryOf('???').label).toBe('Outros')
    expect(unitOf('GRAM').short).toBe('g')
    expect(difficultyOf('MEDIO').label).toBe('Médio')
    expect(difficultyOf('X')).toBeNull()
  })
})
