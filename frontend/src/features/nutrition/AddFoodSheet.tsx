import { useState } from 'react'
import { Sheet } from '@/components/Sheet'
import { SegmentedControl } from '@/components/SegmentedControl'
import { useCreateFoodEntry } from './api'
import { ManualEntryForm } from './ManualEntryForm'
import { SavedFoodPicker } from './SavedFoodPicker'
import type { MealType } from './types'

export type AddFoodMode = 'manual' | 'saved'

const MODE_OPTIONS: { value: AddFoodMode; label: string }[] = [
  { value: 'manual', label: 'Manual' },
  { value: 'saved', label: 'Saved' },
]

interface AddFoodSheetProps {
  initialMode: AddFoodMode
  defaultMealType: MealType
  /** The instant to log against - the selected day at the current time of day. */
  consumedAt: string
  onClose: () => void
}

export function AddFoodSheet({
  initialMode,
  defaultMealType,
  consumedAt,
  onClose,
}: AddFoodSheetProps) {
  const [mode, setMode] = useState<AddFoodMode>(initialMode)
  const createEntry = useCreateFoodEntry()

  return (
    <Sheet title="Add food" onClose={onClose}>
      <div className="stack">
        <SegmentedControl
          ariaLabel="How to add food"
          options={MODE_OPTIONS}
          value={mode}
          onChange={setMode}
        />

        {mode === 'manual' && (
          <ManualEntryForm
            submitLabel="Log it"
            defaultMealType={defaultMealType}
            defaultConsumedAt={consumedAt}
            pending={createEntry.isPending}
            error={createEntry.error}
            onSubmit={(payload) => createEntry.mutate(payload, { onSuccess: onClose })}
          />
        )}

        {mode === 'saved' && (
          <SavedFoodPicker
            defaultMealType={defaultMealType}
            consumedAt={consumedAt}
            onLogged={onClose}
          />
        )}
      </div>
    </Sheet>
  )
}
