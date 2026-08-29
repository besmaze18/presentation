import { useEffect, useState, type FormEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { useGoals, useUpsertGoal } from './api'

const FIELDS = [
  { key: 'calorieTarget', label: 'Calories (kcal)', step: '10' },
  { key: 'proteinTargetG', label: 'Protein (g)', step: '1' },
  { key: 'carbsTargetG', label: 'Carbs (g)', step: '1' },
  { key: 'fatTargetG', label: 'Fat (g)', step: '1' },
  { key: 'fiberTargetG', label: 'Fiber (g)', step: '1' },
  { key: 'targetBodyWeightKg', label: 'Target body weight (kg)', step: '0.1' },
] as const

type FieldKey = (typeof FIELDS)[number]['key']

export function GoalsForm() {
  const goals = useGoals()
  const upsertGoal = useUpsertGoal()
  const [values, setValues] = useState<Record<FieldKey, string>>({
    calorieTarget: '',
    proteinTargetG: '',
    carbsTargetG: '',
    fatTargetG: '',
    fiberTargetG: '',
    targetBodyWeightKg: '',
  })
  const [saved, setSaved] = useState(false)

  const current = goals.data?.[0]

  useEffect(() => {
    if (!current) return
    setValues({
      calorieTarget: String(current.calorieTarget),
      proteinTargetG: String(current.proteinTargetG),
      carbsTargetG: String(current.carbsTargetG),
      fatTargetG: String(current.fatTargetG),
      fiberTargetG: String(current.fiberTargetG),
      targetBodyWeightKg: current.targetBodyWeightKg == null ? '' : String(current.targetBodyWeightKg),
    })
  }, [current])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSaved(false)
    upsertGoal.mutate(
      {
        calorieTarget: Number(values.calorieTarget),
        proteinTargetG: Number(values.proteinTargetG),
        carbsTargetG: Number(values.carbsTargetG),
        fatTargetG: Number(values.fatTargetG),
        fiberTargetG: Number(values.fiberTargetG),
        targetBodyWeightKg:
          values.targetBodyWeightKg === '' ? null : Number(values.targetBodyWeightKg),
      },
      { onSuccess: () => setSaved(true) },
    )
  }

  return (
    <form className="stack" onSubmit={handleSubmit} noValidate>
      <ErrorMessage error={goals.error ?? upsertGoal.error} />
      <div className="grid grid-2">
        {FIELDS.map((field) => (
          <Field key={field.key} label={field.label}>
            {(props) => (
              <input
                {...props}
                className="input"
                type="number"
                min={0}
                step={field.step}
                inputMode="decimal"
                value={values[field.key]}
                onChange={(event) =>
                  setValues((current) => ({ ...current, [field.key]: event.target.value }))
                }
              />
            )}
          </Field>
        ))}
      </div>
      <p className="text-xs text-subtle">
        Targets take effect from today and are stored as a dated series, so past days keep the
        targets they were scored against.
      </p>
      <div className="row">
        <button className="btn" type="submit" disabled={upsertGoal.isPending}>
          {upsertGoal.isPending ? 'Saving…' : 'Save targets'}
        </button>
        {saved && <span className="chip chip--positive">Saved</span>}
      </div>
    </form>
  )
}
