import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Card } from '@/components/Card'
import { ErrorMessage } from '@/components/ErrorMessage'
import { FullPageSpinner } from '@/components/FullPageSpinner'
import { SegmentedControl } from '@/components/SegmentedControl'
import { StatTile } from '@/components/StatTile'
import { useGoals } from '@/features/settings/api'
import {
  addDays,
  formatDuration,
  formatKcal,
  formatNumber,
  formatSignedWeight,
  formatWeight,
  todayIso,
} from '@/lib/format'
import { AddWeightSheet } from './AddWeightSheet'
import { useAnalyticsSeries, useWeightTrend, type Granularity } from './api'
import { ChartCard, MacroStackChart, SeriesTable, SingleBarChart, SingleLineChart } from './charts'
import type { AnalyticsBucket } from './types'

type RangeKey = 'DAY' | 'WEEK' | 'MONTH'

/**
 * The three views the product asks for. Each picks both a window and the bucket size that
 * makes that window readable - 30 daily bars, 12 weekly bars, 12 monthly bars.
 */
const RANGES: Record<RangeKey, { label: string; days: number; granularity: Granularity }> = {
  DAY: { label: 'Daily', days: 30, granularity: 'DAY' },
  WEEK: { label: 'Weekly', days: 90, granularity: 'WEEK' },
  MONTH: { label: 'Monthly', days: 365, granularity: 'MONTH' },
}

const RANGE_OPTIONS = (Object.keys(RANGES) as RangeKey[]).map((value) => ({
  value,
  label: RANGES[value].label,
}))

function hasValue(buckets: AnalyticsBucket[], key: keyof AnalyticsBucket): boolean {
  return buckets.some((bucket) => bucket[key] !== null && bucket[key] !== 0)
}

const number = (decimals: number) => (value: unknown) =>
  value === null || value === undefined ? '—' : formatNumber(Number(value), decimals)

export default function ProgressPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [range, setRange] = useState<RangeKey>('DAY')
  const [addingWeight, setAddingWeight] = useState(false)

  useEffect(() => {
    if (searchParams.get('add') === 'weight') {
      setAddingWeight(true)
      searchParams.delete('add')
      setSearchParams(searchParams, { replace: true })
    }
  }, [searchParams, setSearchParams])

  const { days, granularity } = RANGES[range]
  const to = todayIso()
  const from = addDays(to, -(days - 1))

  const series = useAnalyticsSeries(from, to, granularity)
  const weightTrend = useWeightTrend()
  const goals = useGoals()

  const buckets = useMemo(() => series.data?.buckets ?? [], [series.data])
  const totals = series.data?.totals
  const calorieTarget = goals.data?.[0]?.calorieTarget ?? null

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>Progress</h1>
          <p className="page-header__subtitle">
            {from} → {to}
          </p>
        </div>
        <div className="row">
          <SegmentedControl
            ariaLabel="Time range"
            options={RANGE_OPTIONS}
            value={range}
            onChange={setRange}
          />
          <button type="button" className="btn btn--sm" onClick={() => setAddingWeight(true)}>
            Add weight
          </button>
        </div>
      </header>

      <ErrorMessage error={series.error ?? weightTrend.error} />

      {!series.data ? (
        <FullPageSpinner label="Aggregating your history…" />
      ) : (
        <>
          <div className="grid grid-auto">
            <StatTile
              label="Weight now"
              value={formatWeight(weightTrend.data?.latestKg)}
              hint={
                weightTrend.data?.targetKg != null
                  ? `${formatWeight(weightTrend.data.toTargetKg)} to target`
                  : 'No target set'
              }
            />
            <StatTile
              label="7-day change"
              value={formatSignedWeight(weightTrend.data?.change7dKg)}
              hint="vs previous 7 days"
            />
            <StatTile
              label="30-day change"
              value={formatSignedWeight(weightTrend.data?.change30dKg)}
              hint="vs previous 30 days"
            />
            <StatTile
              label="Average calories"
              value={formatKcal(totals?.averageCalories)}
              hint={`${totals?.daysLogged ?? 0} days logged`}
            />
            <StatTile
              label="Workouts"
              value={formatNumber(totals?.totalWorkouts ?? 0)}
              hint={formatDuration(totals?.totalTrainingMinutes ?? 0)}
            />
            <StatTile
              label="Average recovery"
              value={
                totals?.averageRecovery != null ? `${formatNumber(totals.averageRecovery)}%` : '—'
              }
              hint={
                totals?.averageSleepHours != null
                  ? `${formatNumber(totals.averageSleepHours, 1)} h average sleep`
                  : 'No wearable data'
              }
            />
          </div>

          <ChartCard
            title="Calories"
            empty={!hasValue(buckets, 'calories')}
            footnote={
              granularity === 'DAY'
                ? undefined
                : 'Each bar is the average of the days in that period, so it stays comparable to a daily target.'
            }
            table={
              <SeriesTable
                buckets={buckets}
                columns={[
                  { key: 'calories', label: 'kcal', format: number(0) },
                  { key: 'foodEntryCount', label: 'Entries' },
                ]}
              />
            }
          >
            <SingleBarChart
              buckets={buckets}
              dataKey="calories"
              unit="kcal"
              name="Calories"
              target={calorieTarget}
            />
          </ChartCard>

          <ChartCard
            title="Macros (grams)"
            tall
            empty={!hasValue(buckets, 'proteinG')}
            table={
              <SeriesTable
                buckets={buckets}
                columns={[
                  { key: 'proteinG', label: 'Protein', format: number(1) },
                  { key: 'carbsG', label: 'Carbs', format: number(1) },
                  { key: 'fatG', label: 'Fat', format: number(1) },
                  { key: 'fiberG', label: 'Fiber', format: number(1) },
                ]}
              />
            }
          >
            <MacroStackChart buckets={buckets} />
          </ChartCard>

          <ChartCard
            title="Body weight"
            empty={!hasValue(buckets, 'weightKg')}
            footnote="Days without a reading carry the last known weight forward."
            table={
              <SeriesTable
                buckets={buckets}
                columns={[{ key: 'weightKg', label: 'kg', format: number(1) }]}
              />
            }
          >
            <SingleLineChart
              buckets={buckets}
              dataKey="weightKg"
              unit="kg"
              name="Weight"
              decimals={1}
            />
          </ChartCard>

          <ChartCard
            title="Workout frequency"
            empty={!hasValue(buckets, 'workoutCount')}
            table={
              <SeriesTable
                buckets={buckets}
                columns={[
                  { key: 'workoutCount', label: 'Workouts' },
                  { key: 'trainingMinutes', label: 'Minutes' },
                ]}
              />
            }
          >
            <SingleBarChart
              buckets={buckets}
              dataKey="workoutCount"
              unit="workouts"
              name="Workouts"
              allowDecimals={false}
              domain={[0, 'dataMax']}
            />
          </ChartCard>

          <ChartCard
            title="Recovery"
            empty={!hasValue(buckets, 'recoveryScore')}
            table={
              <SeriesTable
                buckets={buckets}
                columns={[{ key: 'recoveryScore', label: '%', format: number(0) }]}
              />
            }
          >
            <SingleLineChart
              buckets={buckets}
              dataKey="recoveryScore"
              unit="%"
              name="Recovery"
              domain={[0, 100]}
            />
          </ChartCard>

          <ChartCard
            title="Strain"
            empty={!hasValue(buckets, 'strain')}
            table={
              <SeriesTable
                buckets={buckets}
                columns={[{ key: 'strain', label: 'Strain', format: number(1) }]}
              />
            }
          >
            <SingleBarChart buckets={buckets} dataKey="strain" unit="strain" name="Strain" />
          </ChartCard>

          <ChartCard
            title="Sleep"
            empty={!hasValue(buckets, 'sleepDurationMillis')}
            table={
              <SeriesTable
                buckets={buckets}
                columns={[
                  {
                    key: 'sleepDurationMillis',
                    label: 'Hours',
                    format: (value) =>
                      value === null || value === undefined
                        ? '—'
                        : formatNumber(Number(value) / 3_600_000, 1),
                  },
                ]}
              />
            }
          >
            <SingleBarChart
              buckets={buckets.map((bucket) => ({
                ...bucket,
                sleepDurationMillis:
                  bucket.sleepDurationMillis === null
                    ? null
                    : Number((bucket.sleepDurationMillis / 3_600_000).toFixed(2)),
              }))}
              dataKey="sleepDurationMillis"
              unit="hours"
              name="Sleep"
            />
          </ChartCard>

          {buckets.length === 0 && (
            <Card>
              <p className="text-sm text-muted">Nothing recorded in this range yet.</p>
            </Card>
          )}
        </>
      )}

      {addingWeight && <AddWeightSheet onClose={() => setAddingWeight(false)} />}
    </div>
  )
}
