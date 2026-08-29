import { useState, type FormEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { isoToLocalInput, localInputToIso } from '@/lib/format'
import { MacroInputs } from './MacroInputs'
import { EMPTY_MACROS, MEAL_TYPES, MEAL_TYPE_LABELS, type CreateFoodEntryPayload, type FoodEntry, type MealType, type Macros } from './types'

interface ManualEntryFormProps {
  initial?: FoodEntry
  defaultMealType?: MealType
  defaultConsumedAt?: string
  submitLabel: string
  pending: boolean
  error: unknown
  onSubmit: (payload: CreateFoodEntryPayload) => void
  onDelete?: () => void
}

/** The plain macro form, reused for creating a new entry and for editing an existing one. */
export function ManualEntryForm({
  initial,
  defaultMealType = 'LUNCH',
  defaultConsumedAt,
  submitLabel,
  pending,
  error,
  onSubmit,
  onDelete,
}: ManualEntryFormProps) {
  const [name, setName] = useState(initial?.name ?? '')
  const [mealType, setMealType] = useState<MealType>(initial?.mealType ?? defaultMealType)
  const [consumedAt, setConsumedAt] = useState(
    isoToLocalInput(initial?.consumedAt ?? defaultConsumedAt ?? new Date().toISOString()),
  )
  const [quantity, setQuantity] = useState(initial?.quantity != null ? String(initial.quantity) : '')
  const [unit, setUnit] = useState(initial?.unit ?? '')
  const [notes, setNotes] = useState(initial?.notes ?? '')
  const [macros, setMacros] = useState<Macros>(initial?.macros ?? EMPTY_MACROS)

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!name.trim()) return
    onSubmit({
      name: name.trim(),
      mealType,
      consumedAt: localInputToIso(consumedAt),
      quantity: quantity === '' ? null : Number(quantity),
      unit: unit.trim() === '' ? null : unit.trim(),
      macros,
      notes: notes.trim() === '' ? null : notes.trim(),
    })
  }

  return (
    <form className="stack" onSubmit={handleSubmit} noValidate>
      <ErrorMessage error={error} />

      <Field label="Food or meal">
        {(props) => (
          <input
            {...props}
            className="input"
            required
            placeholder="Chicken, rice and olive oil"
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        )}
      </Field>

      <div className="grid grid-2">
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
        <Field label="When">
          {(props) => (
            <input
              {...props}
              className="input"
              type="datetime-local"
              value={consumedAt}
              onChange={(event) => setConsumedAt(event.target.value)}
            />
          )}
        </Field>
      </div>

      <div className="grid grid-2">
        <Field label="Quantity" hint="Optional">
          {(props) => (
            <input
              {...props}
              className="input"
              type="number"
              min={0}
              step="0.1"
              inputMode="decimal"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
            />
          )}
        </Field>
        <Field label="Unit" hint="g, ml, plate…">
          {(props) => (
            <input
              {...props}
              className="input"
              value={unit}
              onChange={(event) => setUnit(event.target.value)}
            />
          )}
        </Field>
      </div>

      <MacroInputs value={macros} onChange={setMacros} />

      <Field label="Notes" hint="Optional">
        {(props) => (
          <textarea
            {...props}
            className="textarea"
            value={notes}
            onChange={(event) => setNotes(event.target.value)}
          />
        )}
      </Field>

      <div className="row">
        <button className="btn" type="submit" disabled={pending || !name.trim()}>
          {pending ? 'Saving…' : submitLabel}
        </button>
        {onDelete && (
          <button type="button" className="btn btn--secondary" onClick={onDelete} disabled={pending}>
            Delete
          </button>
        )}
      </div>
    </form>
  )
}
