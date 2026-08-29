import { Link } from 'react-router-dom'
import { Card } from '@/components/Card'
import { CalorieRing } from '@/components/CalorieRing'
import { ErrorMessage } from '@/components/ErrorMessage'
import { FullPageSpinner } from '@/components/FullPageSpinner'
import { StatTile } from '@/components/StatTile'
import {
  formatDateLabel,
  formatDurationFromMillis,
  formatGrams,
  formatKcal,
  formatNumber,
  formatSignedWeight,
  formatWeight,
  todayIso,
} from '@/lib/format'
import { useDashboard } from './api'
import { InsightList } from './InsightList'
import type { MacroProgress } from './types'

const MACRO_COLORS = {
  protein: 'var(--protein)',
  carbs: 'var(--carbs)',
  fat: 'var(--fat)',
  fiber: 'var(--fiber)',
}

function macroHint(progress: MacroProgress): string {
  if (progress.target === null) return 'No target set'
  if (progress.remaining === null) return `${formatGrams(progress.target)} target`
  return progress.remaining >= 0
    ? `${formatGrams(progress.remaining)} left`
    : `${formatGrams(Math.abs(progress.remaining))} over`
}

export default function DashboardPage() {
  const date = todayIso()
  const dashboard = useDashboard(date)

  if (dashboard.error) {
    return <ErrorMessage error={dashboard.error} />
  }
  if (!dashboard.data) {
    return <FullPageSpinner label="Building your day…" />
  }

  const { nutrition, energy, weight, wearable, wearableConnected, workoutsToday, insights } =
    dashboard.data

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>Today</h1>
          <p className="page-header__subtitle">{formatDateLabel(dashboard.data.date)}</p>
        </div>
      </header>

      <Card title="Calories">
        <div className="row" style={{ justifyContent: 'center', gap: 'var(--space-5)' }}>
          <CalorieRing
            consumed={nutrition.calories.consumed}
            target={nutrition.calories.target ?? 0}
          />
          <div className="stack-sm">
            <div>
              <p className="stat__label">Consumed</p>
              <p className="stat__value">{formatKcal(nutrition.calories.consumed)}</p>
            </div>
            <div>
              <p className="stat__label">Target</p>
              <p className="stat__value">{formatKcal(nutrition.calories.target)}</p>
            </div>
            <p className="stat__hint">
              {nutrition.entryCount} {nutrition.entryCount === 1 ? 'entry' : 'entries'} logged
            </p>
          </div>
        </div>
      </Card>

      <div className="grid grid-auto">
        <StatTile
          label="Protein"
          value={formatGrams(nutrition.protein.consumed)}
          hint={macroHint(nutrition.protein)}
          progress={{
            value: nutrition.protein.consumed,
            max: nutrition.protein.target ?? 0,
            color: MACRO_COLORS.protein,
          }}
        />
        <StatTile
          label="Carbs"
          value={formatGrams(nutrition.carbs.consumed)}
          hint={macroHint(nutrition.carbs)}
          progress={{
            value: nutrition.carbs.consumed,
            max: nutrition.carbs.target ?? 0,
            color: MACRO_COLORS.carbs,
          }}
        />
        <StatTile
          label="Fat"
          value={formatGrams(nutrition.fat.consumed)}
          hint={macroHint(nutrition.fat)}
          progress={{
            value: nutrition.fat.consumed,
            max: nutrition.fat.target ?? 0,
            color: MACRO_COLORS.fat,
          }}
        />
        <StatTile
          label="Fiber"
          value={formatGrams(nutrition.fiber.consumed)}
          hint={macroHint(nutrition.fiber)}
          progress={{
            value: nutrition.fiber.consumed,
            max: nutrition.fiber.target ?? 0,
            color: MACRO_COLORS.fiber,
          }}
        />
      </div>

      <Card title="Calculated insights">
        <InsightList insights={insights} />
      </Card>

      <div className="grid grid-auto">
        <StatTile
          label="Body weight"
          value={formatWeight(weight.latestKg)}
          hint={
            weight.change7dKg !== null
              ? `${formatSignedWeight(weight.change7dKg)} vs previous 7 days`
              : 'Record a weight to see the trend'
          }
        />
        <StatTile
          label="Recovery"
          value={wearable.recoveryScore !== null ? `${wearable.recoveryScore}%` : '—'}
          hint={wearableConnected ? 'From WHOOP' : 'Connect WHOOP in Settings'}
        />
        <StatTile
          label="Sleep"
          value={formatDurationFromMillis(wearable.sleepDurationMillis)}
          hint={
            wearable.sleepPerformancePercentage !== null
              ? `${wearable.sleepPerformancePercentage}% of need`
              : wearableConnected
                ? 'No sleep recorded'
                : 'Connect WHOOP in Settings'
          }
        />
        <StatTile
          label="Strain"
          value={wearable.strain !== null ? formatNumber(wearable.strain, 1) : '—'}
          hint={
            wearable.expenditureKcal !== null
              ? `${formatKcal(wearable.expenditureKcal)} expenditure`
              : wearableConnected
                ? 'No cycle data yet'
                : 'Connect WHOOP in Settings'
          }
        />
      </div>

      <Card title="Energy (estimates)">
        <div className="grid grid-auto">
          <StatTile label="Intake" value={formatKcal(energy.intakeKcal)} />
          <StatTile
            label="Wearable expenditure"
            value={formatKcal(energy.wearableExpenditureKcal)}
            hint={energy.wearableExpenditureKcal === null ? 'No wearable data' : 'As reported'}
          />
          <StatTile
            label="Estimated BMR"
            value={formatKcal(energy.estimatedBmrKcal)}
            hint={energy.estimatedBmrKcal === null ? 'Add height, birth date and weight' : 'Mifflin-St Jeor'}
          />
          <StatTile
            label="Estimated TDEE"
            value={formatKcal(energy.estimatedTdeeKcal)}
            hint={energy.estimatedTdeeKcal === null ? 'Needs BMR inputs' : 'BMR × activity level'}
          />
        </div>
        <p className="text-xs text-subtle mt-3">
          Balance and TDEE are estimates, not measurements. Reported wearable expenditure is stored
          separately and never treated as a definitive TDEE.
        </p>
      </Card>

      <Card
        title="Workouts today"
        action={
          <Link className="text-sm" to="/training">
            All training
          </Link>
        }
        flush
      >
        {workoutsToday.length === 0 ? (
          <p className="text-sm text-muted" style={{ padding: 'var(--space-4)' }}>
            No workouts recorded today.
          </p>
        ) : (
          <div className="list">
            {workoutsToday.map((workout) => (
              <div key={workout.id} className="list__item">
                <span>
                  <span className="list__primary">{workout.title}</span>
                  <span className="list__secondary" style={{ display: 'block' }}>
                    {workout.sportLabel ?? workout.category} · {workout.durationMinutes} min
                    {workout.imported && ' · imported'}
                  </span>
                </span>
                <span className="list__meta">
                  {workout.strain !== null && (
                    <span className="list__primary">{formatNumber(workout.strain, 1)}</span>
                  )}
                  {workout.caloriesKcal !== null && (
                    <span className="list__secondary" style={{ display: 'block' }}>
                      {formatKcal(workout.caloriesKcal)}
                    </span>
                  )}
                </span>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  )
}
