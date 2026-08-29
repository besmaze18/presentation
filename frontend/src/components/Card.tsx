import type { ReactNode } from 'react'

interface CardProps {
  title?: string
  action?: ReactNode
  children: ReactNode
  flush?: boolean
  className?: string
}

export function Card({ title, action, children, flush = false, className = '' }: CardProps) {
  return (
    <section className={`card${flush ? ' card--flush' : ''} ${className}`.trim()}>
      {(title || action) && (
        <header className="card__header" style={flush ? { padding: '16px 16px 0' } : undefined}>
          {title ? <h2 className="card__title">{title}</h2> : <span />}
          {action}
        </header>
      )}
      {children}
    </section>
  )
}
