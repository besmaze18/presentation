import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, apiRequest, setAccessToken, setSessionLostHandler } from '@/lib/api'
import { browserTimeZone } from '@/lib/format'
import { AuthContext, type AuthContextValue } from './authContext'
import type { AuthResponse, LoginPayload, RegisterPayload, User } from './types'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [status, setStatus] = useState<AuthContextValue['status']>('loading')

  const applySession = useCallback((response: AuthResponse) => {
    setAccessToken(response.accessToken)
    setUser(response.user)
    setStatus('authenticated')
  }, [])

  const clearSession = useCallback(() => {
    setAccessToken(null)
    setUser(null)
    setStatus('anonymous')
  }, [])

  // On a cold load the access token is gone but the refresh cookie may still be valid,
  // so we attempt a silent refresh before deciding the visitor is anonymous.
  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const response = await apiRequest<AuthResponse>('/api/auth/refresh', {
          method: 'POST',
          skipAuthRetry: true,
        })
        if (!cancelled) applySession(response)
      } catch {
        if (!cancelled) clearSession()
      }
    })()
    return () => {
      cancelled = true
    }
  }, [applySession, clearSession])

  useEffect(() => {
    setSessionLostHandler(() => clearSession())
    return () => setSessionLostHandler(null)
  }, [clearSession])

  const login = useCallback(
    async (payload: LoginPayload) => {
      const response = await apiRequest<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: payload,
        skipAuthRetry: true,
      })
      applySession(response)
    },
    [applySession],
  )

  const register = useCallback(
    async (payload: RegisterPayload) => {
      const response = await apiRequest<AuthResponse>('/api/auth/register', {
        method: 'POST',
        body: { timeZone: browserTimeZone(), ...payload },
        skipAuthRetry: true,
      })
      applySession(response)
    },
    [applySession],
  )

  const logout = useCallback(async () => {
    try {
      await apiRequest<void>('/api/auth/logout', { method: 'POST', skipAuthRetry: true })
    } finally {
      clearSession()
    }
  }, [clearSession])

  const refreshUser = useCallback(async () => {
    const fresh = await api.get<User>('/api/users/me')
    setUser(fresh)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, status, login, register, logout, refreshUser }),
    [user, status, login, register, logout, refreshUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
