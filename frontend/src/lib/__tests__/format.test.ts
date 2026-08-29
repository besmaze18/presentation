import { describe, expect, it } from 'vitest'
import { addDays, formatDuration, formatGrams, formatKcal, formatSignedWeight } from '../format'

describe('formatters', () => {
  it('renders macros with at most one decimal', () => {
    expect(formatGrams(180)).toBe('180 g')
    expect(formatGrams(180.4)).toBe('180.4 g')
    expect(formatGrams(null)).toBe('—')
  })

  it('rounds calories to whole numbers', () => {
    expect(formatKcal(2410.6)).toBe('2,411 kcal')
    expect(formatKcal(undefined)).toBe('—')
  })

  it('shows the sign on weight deltas', () => {
    expect(formatSignedWeight(-0.42)).toBe('-0.4 kg')
    expect(formatSignedWeight(0.42)).toBe('+0.4 kg')
  })

  it('splits durations into hours and minutes', () => {
    expect(formatDuration(45)).toBe('45m')
    expect(formatDuration(95)).toBe('1h 35m')
  })

  it('walks ISO dates without drifting across DST boundaries', () => {
    expect(addDays('2026-03-28', 1)).toBe('2026-03-29')
    expect(addDays('2026-01-01', -1)).toBe('2025-12-31')
    expect(addDays('2026-02-28', 1)).toBe('2026-03-01')
  })
})
