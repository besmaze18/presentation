import { useState, type FormEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { Sheet } from '@/components/Sheet'
import { isoToLocalInput, localInputToIso } from '@/lib/format'
import { useRecordWeight } from './api'

export function AddWeightSheet({ onClose }: { onClose: () => void }) {
  const [weightKg, setWeightKg] = useState('')
  const [recordedAt, setRecordedAt] = useState(isoToLocalInput(new Date().toISOString()))
  const [note, setNote] = useState('')
  const recordWeight = useRecordWeight()

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const value = Number(weightKg)
    if (!Number.isFinite(value) || value <= 0) return
    recordWeight.mutate(
      {
        weightKg: value,
        recordedAt: localInputToIso(recordedAt),
        note: note.trim() === '' ? null : note.trim(),
      },
      { onSuccess: onClose },
    )
  }

  return (
    <Sheet title="Record weight" onClose={onClose}>
      <form className="stack" onSubmit={handleSubmit} noValidate>
        <ErrorMessage error={recordWeight.error} />
        <Field label="Weight (kg)">
          {(props) => (
            <input
              {...props}
              className="input"
              type="number"
              min={20}
              max={400}
              step="0.1"
              inputMode="decimal"
              required
              autoFocus
              value={weightKg}
              onChange={(event) => setWeightKg(event.target.value)}
            />
          )}
        </Field>
        <Field label="When">
          {(props) => (
            <input
              {...props}
              className="input"
              type="datetime-local"
              value={recordedAt}
              onChange={(event) => setRecordedAt(event.target.value)}
            />
          )}
        </Field>
        <Field label="Note" hint="Optional">
          {(props) => (
            <input
              {...props}
              className="input"
              value={note}
              onChange={(event) => setNote(event.target.value)}
            />
          )}
        </Field>
        <button className="btn" type="submit" disabled={recordWeight.isPending || weightKg === ''}>
          {recordWeight.isPending ? 'Saving…' : 'Save weight'}
        </button>
      </form>
    </Sheet>
  )
}
