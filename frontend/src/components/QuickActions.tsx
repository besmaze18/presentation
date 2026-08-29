import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

/**
 * Food logging usually happens on a phone, one-handed, right after eating. These four actions are
 * therefore always one tap away from anywhere in the app. Each one deep-links into the owning
 * screen with an `add` query parameter, so the screen keeps sole ownership of its own form.
 */
const ACTIONS = [
  { label: 'Take food photo', icon: '📷', to: '/nutrition?add=photo' },
  { label: 'Add food', icon: '🍽', to: '/nutrition?add=manual' },
  { label: 'Add weight', icon: '⚖', to: '/progress?add=weight' },
  { label: 'Add training', icon: '🏋', to: '/training?add=session' },
]

export function QuickActions() {
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const onPointerDown = (event: MouseEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) setOpen(false)
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('mousedown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  return (
    <div className="quick-actions" ref={containerRef}>
      <button
        type="button"
        className="quick-actions__fab"
        aria-expanded={open}
        aria-label={open ? 'Close quick actions' : 'Open quick actions'}
        onClick={() => setOpen((value) => !value)}
      >
        {open ? '×' : '+'}
      </button>
      {open && (
        <div className="quick-actions__menu">
          {ACTIONS.map((action) => (
            <button
              key={action.to}
              type="button"
              className="quick-actions__action"
              onClick={() => {
                setOpen(false)
                navigate(action.to)
              }}
            >
              <span aria-hidden="true">{action.icon}</span>
              {action.label}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
