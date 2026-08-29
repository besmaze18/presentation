import { useState, type FormEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { Sheet } from '@/components/Sheet'
import { isoToLocalInput, localInputToIso } from '@/lib/format'
import {
  useAnnotateTrainingSession,
  useCreateTrainingSession,
  useDeleteTrainingSession,
  useUpdateTrainingSession,
} from './api'
import { ExerciseEditor } from './ExerciseEditor'
import {
  CATEGORY_LABELS,
  TRAINING_CATEGORIES,
  type Exercise,
  type TrainingCategory,
  type TrainingSession,
} from './types'

interface TrainingSessionSheetProps {
  session?: TrainingSession
  onClose: () => void
}

export function TrainingSessionSheet({ session, onClose }: TrainingSessionSheetProps) {
  const isImported = session?.imported ?? false

  const [title, setTitle] = useState(session?.title ?? '')
  const [category, setCategory] = useState<TrainingCategory>(session?.category ?? 'STRENGTH')
  const [startedAt, setStartedAt] = useState(
    isoToLocalInput(session?.startedAt ?? new Date().toISOString()),
  )
  const [durationMinutes, setDurationMinutes] = useState(String(session?.durationMinutes ?? 60))
  const [perceivedExertion, setPerceivedExertion] = useState(
    session?.perceivedExertion != null ? String(session.perceivedExertion) : '',
  )
  const [caloriesKcal, setCaloriesKcal] = useState(
    session?.caloriesKcal != null ? String(session.caloriesKcal) : '',
  )
  const [notes, setNotes] = useState(session?.notes ?? '')
  const [exercises, setExercises] = useState<Exercise[]>(session?.exercises ?? [])

  const createSession = useCreateTrainingSession()
  const updateSession = useUpdateTrainingSession()
  const annotateSession = useAnnotateTrainingSession()
  const deleteSession = useDeleteTrainingSession()

  const pending =
    createSession.isPending ||
    updateSession.isPending ||
    annotateSession.isPending ||
    deleteSession.isPending
  const error =
    createSession.error ?? updateSession.error ?? annotateSession.error ?? deleteSession.error

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!title.trim()) return

    if (session && isImported) {
      // Only the user's own annotations are editable on an imported session.
      annotateSession.mutate(
        {
          id: session.id,
          payload: {
            title: title.trim(),
            category,
            perceivedExertion: perceivedExertion === '' ? null : Number(perceivedExertion),
            notes: notes.trim() === '' ? null : notes.trim(),
          },
        },
        { onSuccess: onClose },
      )
      return
    }

    const payload = {
      title: title.trim(),
      category,
      startedAt: localInputToIso(startedAt),
      durationMinutes: Number(durationMinutes) || 1,
      perceivedExertion: perceivedExertion === '' ? null : Number(perceivedExertion),
      caloriesKcal: caloriesKcal === '' ? null : Number(caloriesKcal),
      notes: notes.trim() === '' ? null : notes.trim(),
      exercises: exercises.filter((exercise) => exercise.name.trim() !== ''),
    }

    if (session) {
      updateSession.mutate({ id: session.id, payload }, { onSuccess: onClose })
    } else {
      createSession.mutate(payload, { onSuccess: onClose })
    }
  }

  return (
    <Sheet title={session ? 'Edit session' : 'Add training'} onClose={onClose}>
      <form className="stack" onSubmit={handleSubmit} noValidate>
        <ErrorMessage error={error} />

        {isImported && (
          <div className="alert alert--info">
            This session came from WHOOP. Duration, strain and heart rate stay as the device
            recorded them; your title, category, exertion rating and notes are yours to set.
          </div>
        )}

        <Field label="Title">
          {(props) => (
            <input
              {...props}
              className="input"
              required
              placeholder="Lower body"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
            />
          )}
        </Field>

        <div className="grid grid-2">
          <Field label="Category">
            {(props) => (
              <select
                {...props}
                className="select"
                value={category}
                onChange={(event) => setCategory(event.target.value as TrainingCategory)}
              >
                {TRAINING_CATEGORIES.map((value) => (
                  <option key={value} value={value}>
                    {CATEGORY_LABELS[value]}
                  </option>
                ))}
              </select>
            )}
          </Field>

          <Field label="Perceived exertion" hint="1-10, optional">
            {(props) => (
              <input
                {...props}
                className="input"
                type="number"
                min={1}
                max={10}
                inputMode="numeric"
                value={perceivedExertion}
                onChange={(event) => setPerceivedExertion(event.target.value)}
              />
            )}
          </Field>
        </div>

        {!isImported && (
          <div className="grid grid-2">
            <Field label="Started">
              {(props) => (
                <input
                  {...props}
                  className="input"
                  type="datetime-local"
                  value={startedAt}
                  onChange={(event) => setStartedAt(event.target.value)}
                />
              )}
            </Field>
            <Field label="Duration (minutes)">
              {(props) => (
                <input
                  {...props}
                  className="input"
                  type="number"
                  min={1}
                  max={1440}
                  inputMode="numeric"
                  required
                  value={durationMinutes}
                  onChange={(event) => setDurationMinutes(event.target.value)}
                />
              )}
            </Field>
          </div>
        )}

        {!isImported && (
          <Field label="Calories (kcal)" hint="Optional — only if you actually know it">
            {(props) => (
              <input
                {...props}
                className="input"
                type="number"
                min={0}
                inputMode="numeric"
                value={caloriesKcal}
                onChange={(event) => setCaloriesKcal(event.target.value)}
              />
            )}
          </Field>
        )}

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

        {!isImported && <ExerciseEditor exercises={exercises} onChange={setExercises} />}

        <div className="row">
          <button className="btn" type="submit" disabled={pending || !title.trim()}>
            {pending ? 'Saving…' : session ? 'Save changes' : 'Add session'}
          </button>
          {session && !isImported && (
            <button
              type="button"
              className="btn btn--secondary"
              disabled={pending}
              onClick={() => deleteSession.mutate(session.id, { onSuccess: onClose })}
            >
              Delete
            </button>
          )}
        </div>
      </form>
    </Sheet>
  )
}
