import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '@/features/nutrition/__tests__/testUtils'
import { WhoopCard } from '../WhoopCard'
import type { WhoopStatus } from '../types'

const connected: WhoopStatus = {
  configured: true,
  connected: true,
  status: 'CONNECTED',
  whoopFirstName: 'Alex',
  whoopLastName: 'Rivera',
  connectedAt: '2026-03-01T08:00:00Z',
  lastSyncAt: '2026-03-10T09:00:00Z',
  lastSyncError: null,
  cycleCount: 90,
  recoveryCount: 88,
  sleepCount: 91,
  workoutCount: 42,
}

function mockStatus(status: WhoopStatus) {
  const fetchMock = vi.fn().mockImplementation((url: string) => {
    if (url === '/api/whoop/status') {
      return Promise.resolve(
        new Response(JSON.stringify(status), {
          status: 200,
          headers: { 'content-type': 'application/json' },
        }),
      )
    }
    if (url === '/api/whoop/authorize') {
      return Promise.resolve(
        new Response(JSON.stringify({ authorizationUrl: 'https://whoop.test/authorize?state=abc' }), {
          status: 200,
          headers: { 'content-type': 'application/json' },
        }),
      )
    }
    return Promise.resolve(new Response(null, { status: 204 }))
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('WhoopCard', () => {
  it('shows what has been imported when connected', async () => {
    mockStatus(connected)
    renderWithProviders(<WhoopCard />)

    expect(await screen.findByText('Alex Rivera')).toBeInTheDocument()
    expect(screen.getByText('Connected')).toBeInTheDocument()
    expect(screen.getByText('90')).toBeInTheDocument()
    expect(screen.getByText('42')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Sync now' })).toBeEnabled()
  })

  it('explains that the server is not configured rather than offering a broken button', async () => {
    mockStatus({ ...connected, configured: false, connected: false, status: 'NOT_CONFIGURED' })
    renderWithProviders(<WhoopCard />)

    expect(await screen.findByText(/WHOOP is not configured on this server/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Connect WHOOP' })).not.toBeInTheDocument()
  })

  it('sends the browser to the server-issued authorization URL', async () => {
    const user = userEvent.setup()
    mockStatus({ ...connected, connected: false, status: 'NOT_CONNECTED' })
    const assign = vi.fn()
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { ...window.location, assign },
    })

    renderWithProviders(<WhoopCard />)
    await user.click(await screen.findByRole('button', { name: 'Connect WHOOP' }))

    await waitFor(() =>
      expect(assign).toHaveBeenCalledWith('https://whoop.test/authorize?state=abc'),
    )
  })

  it('prompts to reconnect when the stored grant stopped working', async () => {
    mockStatus({
      ...connected,
      connected: false,
      status: 'REAUTHORISATION_REQUIRED',
      lastSyncError: 'WHOOP rejected the stored credentials',
    })
    renderWithProviders(<WhoopCard />)

    expect(await screen.findByText('Reconnect needed')).toBeInTheDocument()
    expect(screen.getByText('WHOOP rejected the stored credentials')).toBeInTheDocument()
  })

  it('reports the outcome of the OAuth callback redirect', async () => {
    mockStatus(connected)
    renderWithProviders(<WhoopCard />, { route: '/settings?whoop=connected' })

    expect(
      await screen.findByText('WHOOP connected. Run a sync to import your history.'),
    ).toBeInTheDocument()
  })
})
