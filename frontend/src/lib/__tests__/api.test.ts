import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api, apiRequest, setAccessToken, setSessionLostHandler } from '../api'
import { ApiError } from '../apiError'

function jsonResponse(status: number, body: unknown): Response {
  return new Response(body === null ? null : JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}

describe('apiRequest', () => {
  beforeEach(() => {
    setAccessToken(null)
    setSessionLostHandler(null)
  })

  it('attaches the bearer token when one is set', async () => {
    setAccessToken('token-123')
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, { ok: true }))
    vi.stubGlobal('fetch', fetchMock)

    await api.get('/api/users/me')

    const headers = (fetchMock.mock.calls[0][1] as RequestInit).headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer token-123')
  })

  it('serialises query parameters and drops empty ones', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, []))
    vi.stubGlobal('fetch', fetchMock)

    await api.get('/api/nutrition/entries', { date: '2026-01-02', mealType: undefined, note: '' })

    expect(fetchMock.mock.calls[0][0]).toBe('/api/nutrition/entries?date=2026-01-02')
  })

  it('refreshes once on a 401 and replays the original request', async () => {
    setAccessToken('expired')
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(401, { code: 'UNAUTHORIZED' }))
      .mockResolvedValueOnce(jsonResponse(200, { accessToken: 'fresh-token' }))
      .mockResolvedValueOnce(jsonResponse(200, { id: 'user-1' }))
    vi.stubGlobal('fetch', fetchMock)

    const result = await api.get<{ id: string }>('/api/users/me')

    expect(result).toEqual({ id: 'user-1' })
    expect(fetchMock).toHaveBeenCalledTimes(3)
    expect(fetchMock.mock.calls[1][0]).toBe('/api/auth/refresh')
    const replayHeaders = (fetchMock.mock.calls[2][1] as RequestInit).headers as Headers
    expect(replayHeaders.get('Authorization')).toBe('Bearer fresh-token')
  })

  it('signals a lost session when the refresh also fails', async () => {
    setAccessToken('expired')
    const onLost = vi.fn()
    setSessionLostHandler(onLost)
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(401, { code: 'UNAUTHORIZED' }))
      .mockResolvedValueOnce(jsonResponse(401, { code: 'UNAUTHORIZED' }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(api.get('/api/users/me')).rejects.toBeInstanceOf(ApiError)
    expect(onLost).toHaveBeenCalledOnce()
  })

  it('does not attempt a refresh when skipAuthRetry is set', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(401, { code: 'UNAUTHORIZED' }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(
      apiRequest('/api/auth/refresh', { method: 'POST', skipAuthRetry: true }),
    ).rejects.toBeInstanceOf(ApiError)
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('surfaces field violations from a validation error', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      jsonResponse(400, {
        code: 'VALIDATION_FAILED',
        message: 'Request validation failed',
        errors: [{ field: 'calories', message: 'must be at least 0' }],
      }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(api.post('/api/nutrition/entries', {})).rejects.toMatchObject({
      status: 400,
      code: 'VALIDATION_FAILED',
      displayMessage: 'calories: must be at least 0',
    })
  })

  it('reports a transport failure as an offline error rather than a raw TypeError', async () => {
    const fetchMock = vi.fn().mockRejectedValue(new TypeError('Failed to fetch'))
    vi.stubGlobal('fetch', fetchMock)

    await expect(api.get('/api/dashboard')).rejects.toMatchObject({
      status: 0,
      code: 'OFFLINE',
      displayMessage: 'You appear to be offline. Your data is safe — try again once you reconnect.',
    })
  })

  it('lets a caller-initiated abort propagate untouched', async () => {
    const abort = new DOMException('aborted', 'AbortError')
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(abort))

    await expect(api.get('/api/dashboard')).rejects.toBe(abort)
  })

  it('returns null for a 204 response', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(api.delete('/api/nutrition/entries/abc')).resolves.toBeNull()
  })
})
