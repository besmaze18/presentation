import { screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/features/nutrition/__tests__/testUtils'
import DashboardPage from '../DashboardPage'
import type { Dashboard } from '../types'

const dashboard: Dashboard = {
  date: '2026-03-10',
  timeZone: 'Europe/Berlin',
  nutrition: {
    calories: { consumed: 1980, target: 2400, remaining: 420, percentOfTarget: 83 },
    protein: { consumed: 125, target: 160, remaining: 35, percentOfTarget: 78 },
    carbs: { consumed: 215, target: 260, remaining: 45, percentOfTarget: 83 },
    fat: { consumed: 60, target: 80, remaining: 20, percentOfTarget: 75 },
    fiber: { consumed: 20, target: 30, remaining: 10, percentOfTarget: 67 },
    entryCount: 3,
  },
  energy: {
    intakeKcal: 1980,
    wearableExpenditureKcal: 2810,
    estimatedBmrKcal: 1780,
    estimatedTdeeKcal: 2759,
    balanceKcal: -830,
    balanceBasis: 'WEARABLE_EXPENDITURE',
    weightKg: 82,
  },
  weight: {
    latestKg: 82,
    latestRecordedAt: '2026-03-10T06:00:00Z',
    average7dKg: 82,
    average30dKg: 82.6,
    change7dKg: -0.4,
    change30dKg: -0.8,
    targetKg: 78,
    toTargetKg: 4,
  },
  wearable: {
    date: '2026-03-10',
    recoveryScore: 74,
    restingHeartRate: 48,
    hrvMilli: 92,
    strain: 14.2,
    expenditureKcal: 2810,
    sleepDurationMillis: 7 * 3_600_000 + 20 * 60_000,
    sleepNeedMillis: 7 * 3_600_000 + 30 * 60_000,
    sleepPerformancePercentage: 97,
    workoutCount: 1,
  },
  wearableConnected: true,
  workoutsToday: [
    {
      id: 'w1',
      title: 'Lower body',
      category: 'STRENGTH',
      sportLabel: 'Weightlifting',
      startedAt: '2026-03-10T17:00:00Z',
      durationMinutes: 65,
      perceivedExertion: 7,
      caloriesKcal: 520,
      strain: 12.4,
      source: 'WHOOP',
      imported: true,
    },
  ],
  insights: [
    { code: 'CALORIES_REMAINING', text: '420 kcal remaining', tone: 'NEUTRAL' },
    { code: 'PROTEIN_REMAINING', text: '35 g protein remaining', tone: 'NEUTRAL' },
    { code: 'WEIGHT_DOWN', text: '7-day weight average decreased 0.4 kg', tone: 'NEUTRAL' },
    {
      code: 'RECOVERY_ABOVE_BASELINE',
      text: 'Recovery is 14 points above your 30-day average',
      tone: 'POSITIVE',
    },
  ],
}

function mockDashboard(payload: Dashboard) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue(
      new Response(JSON.stringify(payload), {
        status: 200,
        headers: { 'content-type': 'application/json' },
      }),
    ),
  )
}

describe('DashboardPage', () => {
  beforeEach(() => mockDashboard(dashboard))

  it('renders the headline calorie figures and macro tiles', async () => {
    renderWithProviders(<DashboardPage />)

    // 1,980 kcal appears twice: as consumed calories and as energy intake.
    expect(await screen.findAllByText('1,980 kcal')).toHaveLength(2)
    expect(screen.getByText('2,400 kcal')).toBeInTheDocument()
    expect(screen.getByText('420 kcal')).toBeInTheDocument()
    expect(screen.getByText('remaining')).toBeInTheDocument()
    expect(screen.getByText('125 g')).toBeInTheDocument()
    expect(screen.getByText('35 g left')).toBeInTheDocument()
    expect(screen.getByText('3 entries logged')).toBeInTheDocument()
  })

  it('shows the backend-calculated insights verbatim', async () => {
    renderWithProviders(<DashboardPage />)

    expect(await screen.findByText('420 kcal remaining')).toBeInTheDocument()
    expect(screen.getByText('7-day weight average decreased 0.4 kg')).toBeInTheDocument()
    expect(
      screen.getByText('Recovery is 14 points above your 30-day average'),
    ).toBeInTheDocument()
  })

  it('shows wearable metrics and today’s workouts', async () => {
    renderWithProviders(<DashboardPage />)

    expect(await screen.findByText('74%')).toBeInTheDocument()
    expect(screen.getByText('7h 20m')).toBeInTheDocument()
    expect(screen.getByText('14.2')).toBeInTheDocument()
    expect(screen.getByText('Lower body')).toBeInTheDocument()
    expect(screen.getByText(/Weightlifting · 65 min/)).toBeInTheDocument()
  })

  it('renders a dash, never "undefined", when wearable fields are absent from the payload', async () => {
    // The API omitted nulls at one point, so these fields arrived as undefined while the types
    // said `number | null`. Every `!== null` guard passed and the UI rendered "undefined%".
    const sparse = JSON.parse(JSON.stringify(dashboard))
    delete sparse.wearable.recoveryScore
    delete sparse.wearable.strain
    delete sparse.wearable.sleepPerformancePercentage
    delete sparse.wearable.expenditureKcal
    delete sparse.wearable.sleepDurationMillis
    delete sparse.energy.estimatedBmrKcal
    delete sparse.energy.estimatedTdeeKcal
    delete sparse.energy.wearableExpenditureKcal
    delete sparse.workoutsToday[0].strain
    delete sparse.workoutsToday[0].caloriesKcal
    delete sparse.weight.change7dKg
    mockDashboard(sparse)

    const { container } = renderWithProviders(<DashboardPage />)
    await screen.findByText('Calculated insights')

    expect(container.textContent).not.toMatch(/undefined/)
    expect(container.textContent).not.toMatch(/NaN/)
    expect(screen.getByText('Add height, birth date and weight')).toBeInTheDocument()
  })

  it('prompts to connect a wearable rather than showing blank metrics', async () => {
    mockDashboard({
      ...dashboard,
      wearableConnected: false,
      wearable: {
        date: '2026-03-10',
        recoveryScore: null,
        restingHeartRate: null,
        hrvMilli: null,
        strain: null,
        expenditureKcal: null,
        sleepDurationMillis: null,
        sleepNeedMillis: null,
        sleepPerformancePercentage: null,
        workoutCount: 0,
      },
    })
    renderWithProviders(<DashboardPage />)

    expect(await screen.findAllByText('Connect WHOOP in Settings')).toHaveLength(3)
  })
})
