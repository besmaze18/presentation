import { useMemo, useState, type FormEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { formatKcal, isoToLocalInput, localInputToIso } from '@/lib/format'
import { useConfirmAnalysis, useDiscardAnalysis } from './aiApi'
import type { AiAnalysis } from './aiTypes'
import { MEAL_TYPES, MEAL_TYPE_LABELS, type FoodItem, type MealType } from './types'

interface AiReviewFormProps {
  analysis: AiAnalysis
  defaultMealType: MealType
  consumedAt: string
  onConfirmed: () => void
  onDiscarded: () => void
}

function sumItems(items: FoodItem[]) {
  return items.reduce(
    (total, item) => ({
      calories: total.calories + item.macros.calories,
      proteinG: total.proteinG + item.macros.proteinG,
      carbsG: total.carbsG + item.macros.carbsG,
      fatG: total.fatG + item.macros.fatG,
      fiberG: total.fiberG + item.macros.fiberG,
    }),
    { calories: 0, proteinG: 0, carbsG: 0, fatG: 0, fiberG: 0 },
  )
}

function confidenceLabel(confidence: number | null): { text: string; className: string } {
  if (confidence === null) return { text: 'Confidence unknown', className: 'chip' }
  const percent = Math.round(confidence * 100)
  if (percent >= 75) return { text: `${percent}% confident`, className: 'chip chip--positive' }
  if (percent >= 50) return { text: `${percent}% confident`, className: 'chip chip--warning' }
  return { text: `${percent}% confident — check carefully`, className: 'chip chip--danger' }
}

/**
 * The mandatory review step. The estimate arrives as an editable list; every number can be changed
 * or an item removed entirely, and only what is on screen when "Save" is pressed gets logged.
 */
export function AiReviewForm({
  analysis,
  defaultMealType,
  consumedAt,
  onConfirmed,
  onDiscarded,
}: AiReviewFormProps) {
  const [name, setName] = useState(analysis.suggestedName ?? '')
  const [mealType, setMealType] = useState<MealType>(defaultMealType)
  const [when, setWhen] = useState(isoToLocalInput(consumedAt))
  const [items, setItems] = useState<FoodItem[]>(analysis.items)

  const confirmAnalysis = useConfirmAnalysis()
  const discardAnalysis = useDiscardAnalysis()

  const totals = useMemo(() => sumItems(items), [items])
  const confidence = confidenceLabel(analysis.confidence)

  function updateItem(index: number, field: keyof FoodItem['macros'], value: number) {
    setItems((current) =>
      current.map((item, position) =>
        position === index ? { ...item, macros: { ...item.macros, [field]: value } } : item,
      ),
    )
  }

  function updateItemName(index: number, value: string) {
    setItems((current) =>
      current.map((item, position) => (position === index ? { ...item, name: value } : item)),
    )
  }

  function removeItem(index: number) {
    setItems((current) => current.filter((_, position) => position !== index))
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!name.trim() || items.length === 0) return
    confirmAnalysis.mutate(
      {
        id: analysis.id,
        payload: {
          name: name.trim(),
          mealType,
          consumedAt: localInputToIso(when),
          items,
        },
      },
      { onSuccess: onConfirmed },
    )
  }

  return (
    <form className="stack" onSubmit={handleSubmit} noValidate>
      <ErrorMessage error={confirmAnalysis.error ?? discardAnalysis.error} />

      <div className="alert alert--info">
        This is an estimate. Check the items and portions below — nothing is logged until you save.
      </div>

      {analysis.imageUrl && (
        <img
          src={analysis.imageUrl}
          alt="The meal you photographed"
          style={{ width: '100%', borderRadius: 'var(--radius)', maxHeight: 240, objectFit: 'cover' }}
        />
      )}

      <div className="row">
        <span className={confidence.className}>{confidence.text}</span>
        {analysis.model && <span className="chip">{analysis.model}</span>}
      </div>

      {analysis.assumptions.length > 0 && (
        <div className="card">
          <p className="card__title">Assumptions made</p>
          <ul className="text-sm text-muted" style={{ margin: 0, paddingLeft: '1.1rem' }}>
            {analysis.assumptions.map((assumption) => (
              <li key={assumption}>{assumption}</li>
            ))}
          </ul>
        </div>
      )}

      {analysis.notes && <p className="text-sm text-muted">{analysis.notes}</p>}

      <Field label="Meal name">
        {(props) => (
          <input
            {...props}
            className="input"
            required
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
              value={when}
              onChange={(event) => setWhen(event.target.value)}
            />
          )}
        </Field>
      </div>

      <div className="stack-sm">
        <p className="card__title">Detected items</p>
        {items.map((item, index) => (
          <div key={`${item.name}-${index}`} className="card stack-sm">
            <div className="row-between">
              <input
                className="input"
                aria-label={`Item ${index + 1} name`}
                value={item.name}
                onChange={(event) => updateItemName(index, event.target.value)}
              />
              <button
                type="button"
                className="btn btn--ghost btn--sm"
                onClick={() => removeItem(index)}
                aria-label={`Remove ${item.name}`}
              >
                Remove
              </button>
            </div>
            <p className="text-xs text-subtle">
              {item.quantity != null ? `${item.quantity} ${item.unit ?? ''}` : 'Portion not estimated'}
            </p>
            <div className="grid grid-2">
              <Field label={`Calories — ${item.name}`}>
                {(props) => (
                  <input
                    {...props}
                    className="input"
                    type="number"
                    min={0}
                    step="1"
                    inputMode="decimal"
                    value={item.macros.calories}
                    onChange={(event) => updateItem(index, 'calories', Number(event.target.value) || 0)}
                  />
                )}
              </Field>
              <Field label={`Protein — ${item.name}`}>
                {(props) => (
                  <input
                    {...props}
                    className="input"
                    type="number"
                    min={0}
                    step="0.1"
                    inputMode="decimal"
                    value={item.macros.proteinG}
                    onChange={(event) => updateItem(index, 'proteinG', Number(event.target.value) || 0)}
                  />
                )}
              </Field>
              <Field label={`Carbs — ${item.name}`}>
                {(props) => (
                  <input
                    {...props}
                    className="input"
                    type="number"
                    min={0}
                    step="0.1"
                    inputMode="decimal"
                    value={item.macros.carbsG}
                    onChange={(event) => updateItem(index, 'carbsG', Number(event.target.value) || 0)}
                  />
                )}
              </Field>
              <Field label={`Fat — ${item.name}`}>
                {(props) => (
                  <input
                    {...props}
                    className="input"
                    type="number"
                    min={0}
                    step="0.1"
                    inputMode="decimal"
                    value={item.macros.fatG}
                    onChange={(event) => updateItem(index, 'fatG', Number(event.target.value) || 0)}
                  />
                )}
              </Field>
            </div>
          </div>
        ))}
      </div>

      <div className="card row-between">
        <span className="stat__label">Total</span>
        <span className="stat__value">{formatKcal(totals.calories)}</span>
      </div>

      <div className="row">
        <button
          className="btn"
          type="submit"
          disabled={confirmAnalysis.isPending || items.length === 0 || !name.trim()}
        >
          {confirmAnalysis.isPending ? 'Saving…' : 'Save these values'}
        </button>
        <button
          type="button"
          className="btn btn--secondary"
          disabled={discardAnalysis.isPending}
          onClick={() => discardAnalysis.mutate(analysis.id, { onSuccess: onDiscarded })}
        >
          Discard
        </button>
      </div>
    </form>
  )
}
