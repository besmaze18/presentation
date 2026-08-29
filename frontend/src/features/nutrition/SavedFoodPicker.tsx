import { useState } from 'react'
import { EmptyState } from '@/components/EmptyState'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { formatKcal, formatNumber } from '@/lib/format'
import { useLogSavedFood, useSavedFoods } from './api'
import { MEAL_TYPES, MEAL_TYPE_LABELS, type MealType, type SavedFood } from './types'

interface SavedFoodPickerProps {
  defaultMealType: MealType
  consumedAt: string
  onLogged: () => void
}

export function SavedFoodPicker({ defaultMealType, consumedAt, onLogged }: SavedFoodPickerProps) {
  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<SavedFood | null>(null)
  const [quantity, setQuantity] = useState('')
  const [mealType, setMealType] = useState<MealType>(defaultMealType)
  const savedFoods = useSavedFoods(query)
  const logSavedFood = useLogSavedFood()

  function choose(food: SavedFood) {
    setSelected(food)
    setQuantity(String(food.servingQuantity))
    setMealType(food.defaultMealType ?? defaultMealType)
  }

  if (selected) {
    const factor = selected.servingQuantity > 0 ? Number(quantity || 0) / selected.servingQuantity : 0
    return (
      <div className="stack">
        <ErrorMessage error={logSavedFood.error} />
        <div className="row-between">
          <div>
            <p className="list__primary">{selected.name}</p>
            <p className="list__secondary">
              {formatNumber(selected.servingQuantity, 0)} {selected.servingUnit} ·{' '}
              {formatKcal(selected.macros.calories)}
            </p>
          </div>
          <button type="button" className="btn btn--ghost btn--sm" onClick={() => setSelected(null)}>
            Change
          </button>
        </div>

        <div className="grid grid-2">
          <Field label={`Quantity (${selected.servingUnit})`}>
            {(props) => (
              <input
                {...props}
                className="input"
                type="number"
                min={0}
                step="0.1"
                inputMode="decimal"
                autoFocus
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
              />
            )}
          </Field>
          <Field label="Meal">
            {(props) => (
              <select
                {...props}
                className="select"
                value={mealType}
                onChange={(event) => setMealType(event.target.value as MealType)}
              >
                {MEAL_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {MEAL_TYPE_LABELS[type]}
                  </option>
                ))}
              </select>
            )}
          </Field>
        </div>

        <p className="text-sm text-muted">
          Logs {formatKcal(selected.macros.calories * factor)} ·{' '}
          {formatNumber(selected.macros.proteinG * factor, 1)} g protein ·{' '}
          {formatNumber(selected.macros.carbsG * factor, 1)} g carbs ·{' '}
          {formatNumber(selected.macros.fatG * factor, 1)} g fat
        </p>

        <button
          type="button"
          className="btn"
          disabled={logSavedFood.isPending || Number(quantity) <= 0}
          onClick={() =>
            logSavedFood.mutate(
              { id: selected.id, quantity: Number(quantity), mealType, consumedAt },
              { onSuccess: onLogged },
            )
          }
        >
          {logSavedFood.isPending ? 'Logging…' : 'Log it'}
        </button>
      </div>
    )
  }

  return (
    <div className="stack">
      <Field label="Search your foods">
        {(props) => (
          <input
            {...props}
            className="input"
            type="search"
            placeholder="Chicken, oats, shake…"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        )}
      </Field>

      <ErrorMessage error={savedFoods.error} />

      {savedFoods.data && savedFoods.data.length === 0 ? (
        <EmptyState
          icon="🔖"
          title="Nothing saved yet"
          description="Save a food from any logged entry and it will show up here for one-tap logging."
        />
      ) : (
        <div className="card card--flush">
          <div className="list">
            {savedFoods.data?.map((food) => (
              <button
                key={food.id}
                type="button"
                className="list__item"
                onClick={() => choose(food)}
              >
                <span>
                  <span className="list__primary">{food.name}</span>
                  <span className="list__secondary" style={{ display: 'block' }}>
                    {food.brand ? `${food.brand} · ` : ''}
                    {formatNumber(food.servingQuantity, 0)} {food.servingUnit}
                  </span>
                </span>
                <span className="list__meta">
                  <span className="list__primary">{formatKcal(food.macros.calories)}</span>
                  <span className="list__secondary" style={{ display: 'block' }}>
                    {formatNumber(food.macros.proteinG, 0)} g P
                  </span>
                </span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
