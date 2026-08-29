import { createContext } from 'react'
import type { LoginPayload, RegisterPayload, User } from './types'

export interface AuthContextValue {
  user: User | null
  status: 'loading' | 'authenticated' | 'anonymous'
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

/**
 * Kept in its own module so the provider file exports only components, which is what lets
 * React Fast Refresh preserve state while editing it.
 */
export const AuthContext = createContext<AuthContextValue | null>(null)
