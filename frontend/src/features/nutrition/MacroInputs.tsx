import { Field } from '@/components/Field'
import type { Macros } from './types'

interface MacroInputsProps {
  value: Macros
  onChange: (macros: Macros) => void
  disabled?: boolean
}

const FIELDS: { key: keyof Macros; label: string }[] = [
  { key: 'calories', label: 'Calories (kcal)' },
  { key: 'proteinG', label: 'Protein (g)' },
  { key: 'carbsG', label: 'Carbs (g)' },
  { key: 'fatG', label: 'Fat (g)' },
  { key: 'fiberG', label: 'Fiber (g)' },
]

export function MacroInputs({ value, onChange, disabled = false }: MacroInputsProps) {
  return (
    <div className="grid grid-2">
      {FIELDS.map((field) => (
        <Field key={field.key} label={field.label}>
          {(props) => (
            <input
              {...props}
              className="input"
              type="number"
              min={0}
              step="0.1"
              inputMode="decimal"
              disabled={disabled}
              value={Number.isFinite(value[field.key]) ? String(value[field.key]) : ''}
              onChange={(event) =>
                onChange({ ...value, [field.key]: Number(event.target.value) || 0 })
              }
            />
          )}
        </Field>
      ))}
    </div>
  )
}
