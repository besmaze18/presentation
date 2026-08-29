import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { ChartCard, SeriesTable } from '../charts'
import type { AnalyticsBucket } from '../types'

const buckets: AnalyticsBucket[] = [
  {
    date: '2026-08-01',
    label: '2026-08-01',
    calories: 2100,
    proteinG: 155,
    carbsG: 220,
    fatG: 70,
    fiberG: 28,
    foodEntryCount: 3,
    weightKg: 83,
    workoutCount: 1,
    trainingMinutes: 65,
    recoveryScore: 68,
    strain: 12.4,
    sleepDurationMillis: 25_200_000,
  },
  {
    date: '2026-08-02',
    label: '2026-08-02',
    calories: null,
    proteinG: null,
    carbsG: null,
    fatG: null,
    fiberG: null,
    foodEntryCount: 0,
    weightKg: 82.9,
    workoutCount: 0,
    trainingMinutes: 0,
    recoveryScore: null,
    strain: null,
    sleepDurationMillis: null,
  },
]

describe('ChartCard', () => {
  it('offers a table view so the numbers are reachable without reading the plot', async () => {
    const user = userEvent.setup()
    render(
      <ChartCard
        title="Calories"
        empty={false}
        table={
          <SeriesTable
            buckets={buckets}
            columns={[{ key: 'calories', label: 'kcal' }]}
          />
        }
      >
        <div data-testid="plot" />
      </ChartCard>,
    )

    expect(screen.getByTestId('plot')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Table' }))

    expect(screen.queryByTestId('plot')).not.toBeInTheDocument()
    expect(screen.getByRole('table')).toBeInTheDocument()
    expect(screen.getByText('2100')).toBeInTheDocument()
  })

  it('says so plainly when a range has no data instead of drawing an empty plot', () => {
    render(
      <ChartCard title="Recovery" empty table={<div />}>
        <div data-testid="plot" />
      </ChartCard>,
    )

    expect(screen.getByText('No data in this range yet.')).toBeInTheDocument()
    expect(screen.queryByTestId('plot')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Table' })).not.toBeInTheDocument()
  })
})

describe('SeriesTable', () => {
  it('renders a dash for a missing value rather than a zero', () => {
    render(
      <SeriesTable
        buckets={buckets}
        columns={[
          {
            key: 'recoveryScore',
            label: 'Recovery',
            format: (value) => (value === null || value === undefined ? '—' : String(value)),
          },
        ]}
      />,
    )

    expect(screen.getByText('68')).toBeInTheDocument()
    expect(screen.getByText('—')).toBeInTheDocument()
  })
})
