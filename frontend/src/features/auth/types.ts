export interface User {
  id: string
  email: string
  displayName: string
  role: string
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: User
}

export interface RegisterPayload {
  email: string
  password: string
  displayName: string
  timeZone?: string
}

export interface LoginPayload {
  email: string
  password: string
}
