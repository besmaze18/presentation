import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/features/nutrition/__tests__/testUtils'
import { TrainingSessionSheet } from '../TrainingSessionSheet'
import type { TrainingSession } from '../types'

const importedSession: TrainingSession = {
  id: 'w1',
  title: 'Weightlifting',
  category: 'STRENGTH',
  sportLabel: 'Weightlifting',
  startedAt: '2026-08-29T17:00:00Z',
  endedAt: '2026-08-29T18:05:00Z',
  sessionDate: '2026-08-29',
  durationMinutes: 65,
  perceivedExertion: null,
  caloriesKcal: 520,
  strain: 12.4,
  averageHeartRate: 123,
  maxHeartRate: 168,
  distanceMeters: null,
  notes: null,
  source: 'WHOOP',
  imported: true,
  exercises: [],
}

function mockFetch() {
  const fetchMock = vi.fn().mockResolvedValue(
    new Response(JSON.stringify({ id: 'w1' }), {
      status: 200,
      headers: { 'content-type': 'application/json' },
    }),
  )
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('TrainingSessionSheet', () => {
  beforeEach(() => mockFetch())

  it('creates a manual session with the fields the user filled in', async () => {
    const user = userEvent.setup()
    const fetchMock = mockFetch()
    renderWithProviders(<TrainingSessionSheet onClose={vi.fn()} />)

    await user.type(screen.getByLabelText('Title'), 'Lower body')
    await user.clear(screen.getByLabelText('Duration (minutes)'))
    await user.type(screen.getByLabelText('Duration (minutes)'), '75')
    await user.type(screen.getByLabelText('Perceived exertion'), '8')
    await user.click(screen.getByRole('button', { name: 'Add session' }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalled())
    const [url, init] = fetchMock.mock.calls.at(-1) as [string, RequestInit]
    expect(url).toBe('/api/training/sessions')
    expect(init.method).toBe('POST')
    const body = JSON.parse(init.body as string)
    expect(body).toMatchObject({
      title: 'Lower body',
      category: 'STRENGTH',
      durationMinutes: 75,
      perceivedExertion: 8,
    })
  })

  it('restricts an imported session to annotation and says why', async () => {
    const user = userEvent.setup()
    const fetchMock = mockFetch()
    renderWithProviders(<TrainingSessionSheet session={importedSession} onClose={vi.fn()} />)

    expect(screen.getByText(/came from WHOOP/)).toBeInTheDocument()
    // Device-measured fields are not offered for editing at all.
    expect(screen.queryByLabelText('Duration (minutes)')).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Started')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument()

    await user.type(screen.getByLabelText('Perceived exertion'), '7')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalled())
    const [url, init] = fetchMock.mock.calls.at(-1) as [string, RequestInit]
    expect(url).toBe('/api/training/sessions/w1')
    expect(init.method).toBe('PATCH')
    expect(JSON.parse(init.body as string)).toMatchObject({ perceivedExertion: 7 })
  })
})
