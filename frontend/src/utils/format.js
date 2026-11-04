import { unitOf } from './labels'

export function formatQuantity(quantity, unit) {
  const value = Number(quantity)
  const pretty = Number.isInteger(value) ? value : value.toLocaleString('pt-BR', { maximumFractionDigits: 2 })
  return `${pretty} ${unitOf(unit).short}`
}

export function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
}

export function formatMinutes(minutes) {
  if (!minutes) return '—'
  if (minutes < 60) return `${minutes} min`
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return rest ? `${hours}h ${rest}min` : `${hours}h`
}

/** Days until a yyyy-mm-dd date (negative when expired), or null. */
export function daysUntil(isoDate, today = new Date()) {
  if (!isoDate) return null
  const [year, month, day] = isoDate.split('-').map(Number)
  const target = Date.UTC(year, month - 1, day)
  const start = Date.UTC(today.getFullYear(), today.getMonth(), today.getDate())
  return Math.round((target - start) / 86_400_000)
}
