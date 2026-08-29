import { useMemo, useState } from 'react'
import { Sheet } from '@/components/Sheet'
import { SegmentedControl } from '@/components/SegmentedControl'
import { AiPhotoEntry } from './AiPhotoEntry'
import { AiReviewForm } from './AiReviewForm'
import { AiTextEntry } from './AiTextEntry'
import { useAiStatus } from './aiApi'
import type { AiAnalysis } from './aiTypes'
import { useCreateFoodEntry } from './api'
import { ManualEntryForm } from './ManualEntryForm'
import { SavedFoodPicker } from './SavedFoodPicker'
import type { MealType } from './types'

export type AddFoodMode = 'manual' | 'saved' | 'describe' | 'photo'

const ALL_MODES: { value: AddFoodMode; label: string; requiresAi?: boolean }[] = [
  { value: 'photo', label: 'Photo', requiresAi: true },
  { value: 'describe', label: 'Describe', requiresAi: true },
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
  const [analysis, setAnalysis] = useState<AiAnalysis | null>(null)
  const createEntry = useCreateFoodEntry()
  const aiStatus = useAiStatus()

  // AI tabs are hidden entirely when no provider is configured, rather than offered and failing.
  const aiAvailable = aiStatus.data?.available ?? false
  const modes = useMemo(
    () => ALL_MODES.filter((option) => !option.requiresAi || aiAvailable),
    [aiAvailable],
  )

  // An AI proposal always takes over the sheet: review is not a step the user can skip past.
  if (analysis) {
    return (
      <Sheet title="Review the estimate" onClose={onClose}>
        <AiReviewForm
          analysis={analysis}
          defaultMealType={defaultMealType}
          consumedAt={consumedAt}
          onConfirmed={onClose}
          onDiscarded={() => setAnalysis(null)}
        />
      </Sheet>
    )
  }

  const effectiveMode: AddFoodMode = modes.some((option) => option.value === mode) ? mode : 'manual'

  return (
    <Sheet title="Add food" onClose={onClose}>
      <div className="stack">
        <SegmentedControl
          ariaLabel="How to add food"
          options={modes.map(({ value, label }) => ({ value, label }))}
          value={effectiveMode}
          onChange={setMode}
        />

        {effectiveMode === 'photo' && <AiPhotoEntry onAnalyzed={setAnalysis} />}

        {effectiveMode === 'describe' && <AiTextEntry onAnalyzed={setAnalysis} />}

        {effectiveMode === 'manual' && (
          <ManualEntryForm
            submitLabel="Log it"
            defaultMealType={defaultMealType}
            defaultConsumedAt={consumedAt}
            pending={createEntry.isPending}
            error={createEntry.error}
            onSubmit={(payload) => createEntry.mutate(payload, { onSuccess: onClose })}
          />
        )}

        {effectiveMode === 'saved' && (
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
