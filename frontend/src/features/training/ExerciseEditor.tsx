import { Field } from '@/components/Field'
import type { Exercise } from './types'

interface ExerciseEditorProps {
  exercises: Exercise[]
  onChange: (exercises: Exercise[]) => void
}

const EMPTY_SET = {
  repetitions: null,
  weightKg: null,
  rpe: null,
  distanceMeters: null,
  durationSeconds: null,
}

/**
 * Optional exercise-level detail. Sessions work perfectly well without it; the structure exists so
 * strength tracking can grow here rather than needing a new domain later.
 */
export function ExerciseEditor({ exercises, onChange }: ExerciseEditorProps) {
  function updateExercise(index: number, patch: Partial<Exercise>) {
    onChange(exercises.map((exercise, i) => (i === index ? { ...exercise, ...patch } : exercise)))
  }

  function updateSet(exerciseIndex: number, setIndex: number, patch: Partial<Exercise['sets'][0]>) {
    onChange(
      exercises.map((exercise, i) =>
        i === exerciseIndex
          ? {
              ...exercise,
              sets: exercise.sets.map((set, s) => (s === setIndex ? { ...set, ...patch } : set)),
            }
          : exercise,
      ),
    )
  }

  return (
    <div className="stack-sm">
      <div className="row-between">
        <p className="card__title">Exercises (optional)</p>
        <button
          type="button"
          className="btn btn--ghost btn--sm"
          onClick={() => onChange([...exercises, { name: '', notes: null, sets: [{ ...EMPTY_SET }] }])}
        >
          + Exercise
        </button>
      </div>

      {exercises.map((exercise, exerciseIndex) => (
        <div key={exerciseIndex} className="card stack-sm">
          <div className="row-between">
            <input
              className="input"
              aria-label={`Exercise ${exerciseIndex + 1} name`}
              placeholder="Back squat"
              value={exercise.name}
              onChange={(event) => updateExercise(exerciseIndex, { name: event.target.value })}
            />
            <button
              type="button"
              className="btn btn--ghost btn--sm"
              aria-label={`Remove exercise ${exerciseIndex + 1}`}
              onClick={() => onChange(exercises.filter((_, i) => i !== exerciseIndex))}
            >
              Remove
            </button>
          </div>

          {exercise.sets.map((set, setIndex) => (
            <div key={setIndex} className="grid grid-2">
              <Field label={`Set ${setIndex + 1} reps`}>
                {(props) => (
                  <input
                    {...props}
                    className="input"
                    type="number"
                    min={0}
                    inputMode="numeric"
                    value={set.repetitions ?? ''}
                    onChange={(event) =>
                      updateSet(exerciseIndex, setIndex, {
                        repetitions: event.target.value === '' ? null : Number(event.target.value),
                      })
                    }
                  />
                )}
              </Field>
              <Field label={`Set ${setIndex + 1} weight (kg)`}>
                {(props) => (
                  <input
                    {...props}
                    className="input"
                    type="number"
                    min={0}
                    step="0.5"
                    inputMode="decimal"
                    value={set.weightKg ?? ''}
                    onChange={(event) =>
                      updateSet(exerciseIndex, setIndex, {
                        weightKg: event.target.value === '' ? null : Number(event.target.value),
                      })
                    }
                  />
                )}
              </Field>
            </div>
          ))}

          <button
            type="button"
            className="btn btn--ghost btn--sm"
            onClick={() =>
              updateExercise(exerciseIndex, { sets: [...exercise.sets, { ...EMPTY_SET }] })
            }
          >
            + Set
          </button>
        </div>
      ))}
    </div>
  )
}
