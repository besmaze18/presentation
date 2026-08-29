import { StatTile } from '@/components/StatTile'
import { formatGrams, formatKcal } from '@/lib/format'
import type { Macros } from './types'

export function DayTotals({ totals }: { totals: Macros }) {
  return (
    <div className="grid grid-auto">
      <StatTile label="Calories" value={formatKcal(totals.calories)} />
      <StatTile label="Protein" value={formatGrams(totals.proteinG)} />
      <StatTile label="Carbs" value={formatGrams(totals.carbsG)} />
      <StatTile label="Fat" value={formatGrams(totals.fatG)} />
      <StatTile label="Fiber" value={formatGrams(totals.fiberG)} />
    </div>
  )
}
