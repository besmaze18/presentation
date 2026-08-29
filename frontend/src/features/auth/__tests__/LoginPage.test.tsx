import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext, type AuthContextValue } from '../AuthContext'
import LoginPage from '../LoginPage'

function renderLogin(overrides: Partial<AuthContextValue> = {}) {
  const value: AuthContextValue = {
    user: null,
    status: 'anonymous',
    login: vi.fn().mockResolvedValue(undefined),
    register: vi.fn(),
    logout: vi.fn(),
    refreshUser: vi.fn(),
    ...overrides,
  }
  render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthContext.Provider value={value}>
        <LoginPage />
      </AuthContext.Provider>
    </MemoryRouter>,
  )
  return value
}

describe('LoginPage', () => {
  it('submits trimmed credentials', async () => {
    const user = userEvent.setup()
    const value = renderLogin()

    await user.type(screen.getByLabelText('Email'), '  athlete@example.test  ')
    await user.type(screen.getByLabelText('Password'), 'correct-horse-battery')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    await waitFor(() =>
      expect(value.login).toHaveBeenCalledWith({
        email: 'athlete@example.test',
        password: 'correct-horse-battery',
      }),
    )
  })

  it('shows the server error when sign-in fails', async () => {
    const user = userEvent.setup()
    renderLogin({ login: vi.fn().mockRejectedValue(new Error('Invalid email or password')) })

    await user.type(screen.getByLabelText('Email'), 'athlete@example.test')
    await user.type(screen.getByLabelText('Password'), 'wrong')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password')
  })
})
