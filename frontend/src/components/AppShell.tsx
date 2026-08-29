import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '@/features/auth/useAuth'
import { QuickActions } from './QuickActions'

const NAV_ITEMS = [
  { to: '/', label: 'Today', icon: '◎', end: true },
  { to: '/nutrition', label: 'Nutrition', icon: '🍽' },
  { to: '/training', label: 'Training', icon: '🏋' },
  { to: '/progress', label: 'Progress', icon: '📈' },
  { to: '/settings', label: 'Settings', icon: '⚙' },
]

export function AppShell() {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="app-header">
        <span className="app-header__brand">
          <span aria-hidden="true">📈</span> FitTrack
        </span>
        <div className="app-header__actions">
          <span className="text-sm text-muted">{user?.displayName}</span>
          <button type="button" className="btn btn--ghost btn--sm" onClick={() => void logout()}>
            Sign out
          </button>
        </div>
      </header>

      <main className="app-main">
        <Outlet />
      </main>

      <QuickActions />

      <nav className="bottom-nav" aria-label="Main navigation">
        {NAV_ITEMS.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end} className="bottom-nav__item">
            <span className="bottom-nav__icon" aria-hidden="true">
              {item.icon}
            </span>
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
