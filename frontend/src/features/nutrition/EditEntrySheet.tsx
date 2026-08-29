import { Sheet } from '@/components/Sheet'
import { useDeleteFoodEntry, useUpdateFoodEntry } from './api'
import { ManualEntryForm } from './ManualEntryForm'
import type { FoodEntry } from './types'

export function EditEntrySheet({ entry, onClose }: { entry: FoodEntry; onClose: () => void }) {
  const updateEntry = useUpdateFoodEntry()
  const deleteEntry = useDeleteFoodEntry()

  return (
    <Sheet title="Edit entry" onClose={onClose}>
      <div className="stack">
        {entry.items.length > 0 && (
          <p className="alert alert--info">
            This entry has {entry.items.length} components. Saving here replaces them with a single
            combined total.
          </p>
        )}
        <ManualEntryForm
          initial={entry}
          submitLabel="Save changes"
          pending={updateEntry.isPending || deleteEntry.isPending}
          error={updateEntry.error ?? deleteEntry.error}
          onSubmit={(payload) =>
            updateEntry.mutate({ id: entry.id, payload }, { onSuccess: onClose })
          }
          onDelete={() => deleteEntry.mutate(entry.id, { onSuccess: onClose })}
        />
      </div>
    </Sheet>
  )
}
