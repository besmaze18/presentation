import { formatKcal } from '@/lib/format'

interface CalorieRingProps {
  consumed: number
  target: number
  size?: number
}

/** The headline "how much is left today" figure, drawn as an SVG ring. */
export function CalorieRing({ consumed, target, size = 168 }: CalorieRingProps) {
  const stroke = 14
  const radius = (size - stroke) / 2
  const circumference = 2 * Math.PI * radius
  const safeTarget = target > 0 ? target : 1
  const ratio = Math.min(1, Math.max(0, consumed / safeTarget))
  const offset = circumference * (1 - ratio)
  const remaining = target - consumed
  const over = remaining < 0

  return (
    <div className="ring-wrap" style={{ width: size, height: size }}>
      <svg className="ring" width={size} height={size} aria-hidden="true">
        <circle
          className="ring__track"
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={stroke}
        />
        <circle
          className="ring__value"
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={over ? { stroke: 'var(--warning)' } : undefined}
        />
      </svg>
      <div className="ring-wrap__inner">
        <span className="stat__value" style={{ fontSize: '1.75rem' }}>
          {formatKcal(Math.abs(remaining))}
        </span>
        <span className="stat__hint">{over ? 'over target' : 'remaining'}</span>
      </div>
      <span className="visually-hidden">
        {formatKcal(consumed)} consumed of {formatKcal(target)} target
      </span>
    </div>
  )
}
