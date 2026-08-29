import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Card } from '@/components/Card'
import { EmptyState } from '@/components/EmptyState'
import { ErrorMessage } from '@/components/ErrorMessage'
import { FullPageSpinner } from '@/components/FullPageSpinner'
import { addDays, formatDateLabel, formatGrams, formatKcal, formatTime, todayIso } from '@/lib/format'
import { AddFoodSheet, type AddFoodMode } from './AddFoodSheet'
import { useDailyNutrition } from './api'
import { DayTotals } from './DayTotals'
import { EditEntrySheet } from './EditEntrySheet'
import { MEAL_TYPES, MEAL_TYPE_LABELS, type FoodEntry, type MealType } from './types'

const SOURCE_LABELS: Record<FoodEntry['source'], string> = {
  MANUAL: 'Manual',
  SAVED_FOOD: 'Saved',
  AI_TEXT: 'AI · text',
  AI_IMAGE: 'AI · photo',
}

/** Suggests the meal a user is most likely logging, based on the time of day. */
function mealTypeForNow(): MealType {
  const hour = new Date().getHours()
  if (hour < 11) return 'BREAKFAST'
  if (hour < 15) return 'LUNCH'
  if (hour < 21) return 'DINNER'
  return 'SNACK'
}

export default function NutritionPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [date, setDate] = useState(todayIso())
  const [addMode, setAddMode] = useState<AddFoodMode | null>(null)
  const [editing, setEditing] = useState<FoodEntry | null>(null)

  const day = useDailyNutrition(date)

  // The quick-add menu deep-links here with ?add=…; consume it once and clean the URL.
  useEffect(() => {
    const requested = searchParams.get('add')
    if (!requested) return
    const known: Record<string, AddFoodMode> = { manual: 'manual', saved: 'saved', photo: 'manual' }
    setAddMode(known[requested] ?? 'manual')
    searchParams.delete('add')
    setSearchParams(searchParams, { replace: true })
  }, [searchParams, setSearchParams])

  const grouped = useMemo(() => {
    const entries = day.data?.entries ?? []
    return MEAL_TYPES.map((mealType) => ({
      mealType,
      entries: entries.filter((entry) => entry.mealType === mealType),
    })).filter((group) => group.entries.length > 0)
  }, [day.data])

  // Log against the selected day, keeping the current time of day.
  const consumedAt = useMemo(() => {
    const now = new Date()
    const [year, month, dayOfMonth] = date.split('-').map(Number)
    const target = new Date(year, month - 1, dayOfMonth, now.getHours(), now.getMinutes())
    return target.toISOString()
  }, [date])

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>Nutrition</h1>
          <p className="page-header__subtitle">{formatDateLabel(date)}</p>
        </div>
        <div className="row">
          <button
            type="button"
            className="btn btn--secondary btn--sm"
            onClick={() => setDate(addDays(date, -1))}
            aria-label="Previous day"
          >
            ‹
          </button>
          <input
            className="input"
            style={{ width: 'auto', minWidth: 150 }}
            type="date"
            value={date}
            max={todayIso()}
            onChange={(event) => setDate(event.target.value || todayIso())}
            aria-label="Date"
          />
          <button
            type="button"
            className="btn btn--secondary btn--sm"
            onClick={() => setDate(addDays(date, 1))}
            disabled={date >= todayIso()}
            aria-label="Next day"
          >
            ›
          </button>
        </div>
      </header>

      <ErrorMessage error={day.error} />

      {!day.data ? (
        <FullPageSpinner label="Loading your day…" />
      ) : (
        <>
          <DayTotals totals={day.data.totals} />

          <div className="row">
            <button type="button" className="btn" onClick={() => setAddMode('manual')}>
              Add food
            </button>
            <button
              type="button"
              className="btn btn--secondary"
              onClick={() => setAddMode('saved')}
            >
              From saved
            </button>
          </div>

          {grouped.length === 0 ? (
            <Card>
              <EmptyState
                icon="🍽"
                title="Nothing logged yet"
                description="Add a meal manually, reuse a saved food, or snap a photo."
              />
            </Card>
          ) : (
            grouped.map((group) => (
              <Card key={group.mealType} flush>
                <p className="meal-group__title">{MEAL_TYPE_LABELS[group.mealType]}</p>
                <div className="list">
                  {group.entries.map((entry) => (
                    <button
                      key={entry.id}
                      type="button"
                      className="list__item"
                      onClick={() => setEditing(entry)}
                    >
                      <span>
                        <span className="list__primary">{entry.name}</span>
                        <span className="list__secondary" style={{ display: 'block' }}>
                          {formatTime(entry.consumedAt)}
                          {entry.quantity != null && ` · ${entry.quantity}${entry.unit ? ` ${entry.unit}` : ''}`}
                          {entry.source !== 'MANUAL' && ` · ${SOURCE_LABELS[entry.source]}`}
                        </span>
                      </span>
                      <span className="list__meta">
                        <span className="list__primary">{formatKcal(entry.macros.calories)}</span>
                        <span className="list__secondary" style={{ display: 'block' }}>
                          {formatGrams(entry.macros.proteinG)} P
                        </span>
                      </span>
                    </button>
                  ))}
                </div>
              </Card>
            ))
          )}
        </>
      )}

      {addMode && (
        <AddFoodSheet
          initialMode={addMode}
          defaultMealType={mealTypeForNow()}
          consumedAt={consumedAt}
          onClose={() => setAddMode(null)}
        />
      )}

      {editing && <EditEntrySheet entry={editing} onClose={() => setEditing(null)} />}
    </div>
  )
}
