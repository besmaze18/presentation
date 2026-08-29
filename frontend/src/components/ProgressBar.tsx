interface ProgressBarProps {
  value: number
  max: number
  color?: string
  label?: string
}

/** A linear progress meter that visually caps at 100% but flags an overshoot. */
export function ProgressBar({ value, max, color, label }: ProgressBarProps) {
  const safeMax = max > 0 ? max : 1
  const ratio = value / safeMax
  const percent = Math.min(100, Math.max(0, ratio * 100))
  const over = ratio > 1.0001

  return (
    <div
      className={`progress${over ? ' progress--over' : ''}`}
      role="progressbar"
      aria-valuenow={Math.round(value)}
      aria-valuemin={0}
      aria-valuemax={Math.round(safeMax)}
      aria-label={label}
    >
      <div
        className="progress__bar"
        style={{ width: `${percent}%`, background: over ? undefined : color }}
      />
    </div>
  )
}
