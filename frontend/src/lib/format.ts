/** Display formatting shared across screens, so numbers read consistently everywhere. */

export function formatNumber(value: number | null | undefined, decimals = 0): string {
  if (value === null || value === undefined || Number.isNaN(value)) return '—'
  return value.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })
}

export function formatGrams(value: number | null | undefined): string {
  if (value === null || value === undefined) return '—'
  return `${formatNumber(value, value % 1 === 0 ? 0 : 1)} g`
}

export function formatKcal(value: number | null | undefined): string {
  if (value === null || value === undefined) return '—'
  return `${formatNumber(Math.round(value))} kcal`
}

export function formatWeight(value: number | null | undefined, unit = 'kg'): string {
  if (value === null || value === undefined) return '—'
  return `${formatNumber(value, 1)} ${unit}`
}

export function formatSignedWeight(value: number | null | undefined, unit = 'kg'): string {
  if (value === null || value === undefined) return '—'
  const sign = value > 0 ? '+' : ''
  return `${sign}${formatNumber(value, 1)} ${unit}`
}

export function formatDuration(minutes: number | null | undefined): string {
  if (minutes === null || minutes === undefined) return '—'
  const total = Math.round(minutes)
  const hours = Math.floor(total / 60)
  const mins = total % 60
  return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
}

export function formatDurationFromMillis(millis: number | null | undefined): string {
  if (millis === null || millis === undefined) return '—'
  return formatDuration(millis / 60000)
}

const timeFormatter = new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit' })

export function formatTime(iso: string): string {
  return timeFormatter.format(new Date(iso))
}

export function formatDateLabel(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString(undefined, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  })
}

export function formatShortDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
  })
}

/** Today in the browser's own time zone, as an ISO date string. */
export function todayIso(): string {
  const now = new Date()
  const offsetMs = now.getTimezoneOffset() * 60_000
  return new Date(now.getTime() - offsetMs).toISOString().slice(0, 10)
}

export function addDays(isoDate: string, days: number): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  const date = new Date(Date.UTC(year, month - 1, day))
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}

export function browserTimeZone(): string {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'
  } catch {
    return 'UTC'
  }
}

/** Converts a datetime-local input value into an absolute ISO instant. */
export function localInputToIso(value: string): string {
  return new Date(value).toISOString()
}

/** Converts an ISO instant into the value format a datetime-local input expects. */
export function isoToLocalInput(iso: string): string {
  const date = new Date(iso)
  const offsetMs = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16)
}
