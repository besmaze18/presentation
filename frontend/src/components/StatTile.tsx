import type { ReactNode } from 'react'
import { ProgressBar } from './ProgressBar'

interface StatTileProps {
  label: string
  value: ReactNode
  hint?: ReactNode
  progress?: { value: number; max: number; color?: string }
}

export function StatTile({ label, value, hint, progress }: StatTileProps) {
  return (
    <div className="stat">
      <span className="stat__label">{label}</span>
      <span className="stat__value">{value}</span>
      {progress && (
        <ProgressBar
          value={progress.value}
          max={progress.max}
          color={progress.color}
          label={label}
        />
      )}
      {hint && <span className="stat__hint">{hint}</span>}
    </div>
  )
}
