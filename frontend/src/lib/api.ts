import { ApiError, type ApiErrorBody } from './apiError'

/**
 * The access token lives only in memory. The refresh token is an HttpOnly cookie the browser
 * attaches automatically to /api/auth/refresh, so no long-lived credential is reachable from JS.
 */
let accessToken: string | null = null
let onSessionLost: (() => void) | null = null
let refreshInFlight: Promise<string | null> | null = null

export function setAccessToken(token: string | null): void {
  accessToken = token
}

export function getAccessToken(): string | null {
  return accessToken
}

export function setSessionLostHandler(handler: (() => void) | null): void {
  onSessionLost = handler
}

export interface RequestOptions {
  method?: string
  body?: unknown
  signal?: AbortSignal
  /** Skips the automatic refresh-and-retry, used by the auth endpoints themselves. */
  skipAuthRetry?: boolean
  query?: Record<string, string | number | boolean | null | undefined>
}

function buildUrl(path: string, query?: RequestOptions['query']): string {
  if (!query) return path
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, String(value))
    }
  }
  const qs = params.toString()
  return qs ? `${path}?${qs}` : path
}

async function parseBody(response: Response): Promise<unknown> {
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null
  }
  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  const text = await response.text()
  return text.length > 0 ? text : null
}

/**
 * Turns a network failure into the same ApiError shape as an HTTP error, so every caller has one
 * thing to handle. Status 0 means "the request never reached the server" - the common case for an
 * installed PWA that has gone offline.
 */
class OfflineError extends Error {}

async function rawRequest(path: string, options: RequestOptions): Promise<Response> {
  const headers = new Headers()
  const isFormData = options.body instanceof FormData
  if (options.body !== undefined && !isFormData) {
    headers.set('Content-Type', 'application/json')
  }
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }
  try {
    return await fetch(buildUrl(path, options.query), {
      method: options.method ?? 'GET',
      headers,
      credentials: 'include',
      signal: options.signal,
      body:
        options.body === undefined
          ? undefined
          : isFormData
            ? (options.body as FormData)
            : JSON.stringify(options.body),
    })
  } catch (error) {
    // An aborted request is the caller's own doing; anything else is a transport failure.
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw error
    }
    throw new OfflineError('offline')
  }
}

/** Refreshes the access token, collapsing concurrent callers onto one in-flight request. */
export function refreshAccessToken(): Promise<string | null> {
  if (!refreshInFlight) {
    refreshInFlight = fetch('/api/auth/refresh', { method: 'POST', credentials: 'include' })
      .then(async (response) => {
        if (!response.ok) return null
        const body = (await response.json()) as { accessToken: string }
        accessToken = body.accessToken
        return accessToken
      })
      .catch(() => null)
      .finally(() => {
        refreshInFlight = null
      })
  }
  return refreshInFlight
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  let response: Response
  try {
    response = await rawRequest(path, options)
  } catch (error) {
    if (error instanceof OfflineError) {
      throw new ApiError(0, { code: 'OFFLINE' })
    }
    throw error
  }

  if (response.status === 401 && !options.skipAuthRetry) {
    const refreshed = await refreshAccessToken()
    if (refreshed) {
      try {
        response = await rawRequest(path, options)
      } catch (error) {
        if (error instanceof OfflineError) {
          throw new ApiError(0, { code: 'OFFLINE' })
        }
        throw error
      }
    } else {
      accessToken = null
      onSessionLost?.()
    }
  }

  if (!response.ok) {
    const body = (await parseBody(response).catch(() => null)) as ApiErrorBody | null
    throw new ApiError(response.status, typeof body === 'object' ? body : null)
  }

  return (await parseBody(response)) as T
}

export const api = {
  get: <T,>(path: string, query?: RequestOptions['query']) => apiRequest<T>(path, { query }),
  post: <T,>(path: string, body?: unknown, options: RequestOptions = {}) =>
    apiRequest<T>(path, { ...options, method: 'POST', body }),
  put: <T,>(path: string, body?: unknown) => apiRequest<T>(path, { method: 'PUT', body }),
  patch: <T,>(path: string, body?: unknown) => apiRequest<T>(path, { method: 'PATCH', body }),
  delete: <T,>(path: string) => apiRequest<T>(path, { method: 'DELETE' }),
}
