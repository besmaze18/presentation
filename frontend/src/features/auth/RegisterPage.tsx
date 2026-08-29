import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { useAuth } from './useAuth'

const MIN_PASSWORD_LENGTH = 10

export default function RegisterPage() {
  const { register, status } = useAuth()
  const navigate = useNavigate()
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  if (status === 'authenticated') {
    return <Navigate to="/" replace />
  }

  const passwordTooShort = password.length > 0 && password.length < MIN_PASSWORD_LENGTH

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (passwordTooShort) return
    setError(null)
    setSubmitting(true)
    try {
      await register({ email: email.trim(), password, displayName: displayName.trim() })
      navigate('/', { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-card__brand">
          <span aria-hidden="true">📈</span> FitTrack
        </div>
        <p className="auth-card__subtitle">Create your account.</p>
        <form className="stack" onSubmit={onSubmit} noValidate>
          <ErrorMessage error={error} />
          <Field label="Name">
            {(props) => (
              <input
                {...props}
                className="input"
                name="displayName"
                autoComplete="name"
                required
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
              />
            )}
          </Field>
          <Field label="Email">
            {(props) => (
              <input
                {...props}
                className="input"
                type="email"
                name="email"
                autoComplete="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
              />
            )}
          </Field>
          <Field
            label="Password"
            hint={`At least ${MIN_PASSWORD_LENGTH} characters.`}
            error={passwordTooShort ? `Use at least ${MIN_PASSWORD_LENGTH} characters.` : undefined}
          >
            {(props) => (
              <input
                {...props}
                className="input"
                type="password"
                name="password"
                autoComplete="new-password"
                required
                minLength={MIN_PASSWORD_LENGTH}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            )}
          </Field>
          <button className="btn btn--block" type="submit" disabled={submitting || passwordTooShort}>
            {submitting ? 'Creating account…' : 'Create account'}
          </button>
        </form>
        <p className="text-sm text-muted mt-4">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
