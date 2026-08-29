import { useEffect, useState, type FormEvent } from 'react'
import { Card } from '@/components/Card'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { FullPageSpinner } from '@/components/FullPageSpinner'
import { useAuth } from '@/features/auth/useAuth'
import { WhoopCard } from '@/features/integrations/WhoopCard'
import { browserTimeZone } from '@/lib/format'
import { useUpdateUserSettings, useUserSettings } from './api'
import { GoalsForm } from './GoalsForm'
import type { UserSettings } from './types'

const ACTIVITY_LEVELS: { value: UserSettings['activityLevel']; label: string }[] = [
  { value: 'SEDENTARY', label: 'Sedentary (little exercise)' },
  { value: 'LIGHT', label: 'Light (1-3 days a week)' },
  { value: 'MODERATE', label: 'Moderate (3-5 days a week)' },
  { value: 'ACTIVE', label: 'Active (6-7 days a week)' },
  { value: 'VERY_ACTIVE', label: 'Very active (physical job or twice daily)' },
]

export default function SettingsPage() {
  const { user, logout } = useAuth()
  const settings = useUserSettings()
  const updateSettings = useUpdateUserSettings()

  const [form, setForm] = useState<Partial<UserSettings>>({})
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (settings.data) setForm(settings.data)
  }, [settings.data])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSaved(false)
    updateSettings.mutate(
      {
        timeZone: form.timeZone,
        unitSystem: form.unitSystem,
        sex: form.sex,
        birthDate: form.birthDate || null,
        heightCm: form.heightCm ? Number(form.heightCm) : null,
        activityLevel: form.activityLevel,
      },
      { onSuccess: () => setSaved(true) },
    )
  }

  if (!settings.data) {
    return settings.error ? <ErrorMessage error={settings.error} /> : <FullPageSpinner />
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>Settings</h1>
          <p className="page-header__subtitle">{user?.email}</p>
        </div>
      </header>

      <Card title="Daily targets">
        <GoalsForm />
      </Card>

      <Card title="About you">
        <form className="stack" onSubmit={handleSubmit} noValidate>
          <ErrorMessage error={updateSettings.error} />
          <p className="text-sm text-muted">
            Height, birth date and sex are used only to estimate your BMR and TDEE. Leave them blank
            and the dashboard simply omits those estimates rather than guessing.
          </p>

          <div className="grid grid-2">
            <Field label="Sex">
              {(props) => (
                <select
                  {...props}
                  className="select"
                  value={form.sex ?? 'UNSPECIFIED'}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      sex: event.target.value as UserSettings['sex'],
                    }))
                  }
                >
                  <option value="UNSPECIFIED">Prefer not to say</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
              )}
            </Field>

            <Field label="Date of birth">
              {(props) => (
                <input
                  {...props}
                  className="input"
                  type="date"
                  value={form.birthDate ?? ''}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, birthDate: event.target.value }))
                  }
                />
              )}
            </Field>

            <Field label="Height (cm)">
              {(props) => (
                <input
                  {...props}
                  className="input"
                  type="number"
                  min={50}
                  max={260}
                  step="0.5"
                  inputMode="decimal"
                  value={form.heightCm ?? ''}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      heightCm: event.target.value === '' ? null : Number(event.target.value),
                    }))
                  }
                />
              )}
            </Field>

            <Field label="Units">
              {(props) => (
                <select
                  {...props}
                  className="select"
                  value={form.unitSystem ?? 'METRIC'}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      unitSystem: event.target.value as UserSettings['unitSystem'],
                    }))
                  }
                >
                  <option value="METRIC">Metric (kg, cm)</option>
                  <option value="IMPERIAL">Imperial (lb, in)</option>
                </select>
              )}
            </Field>
          </div>

          <Field
            label="Activity level"
            hint="Used to scale BMR into an estimated TDEE when no wearable data exists."
          >
            {(props) => (
              <select
                {...props}
                className="select"
                value={form.activityLevel ?? 'MODERATE'}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    activityLevel: event.target.value as UserSettings['activityLevel'],
                  }))
                }
              >
                {ACTIVITY_LEVELS.map((level) => (
                  <option key={level.value} value={level.value}>
                    {level.label}
                  </option>
                ))}
              </select>
            )}
          </Field>

          <Field label="Time zone" hint="Decides which calendar day an entry belongs to.">
            {(props) => (
              <div className="row">
                <input
                  {...props}
                  className="input"
                  style={{ flex: 1 }}
                  value={form.timeZone ?? ''}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, timeZone: event.target.value }))
                  }
                />
                <button
                  type="button"
                  className="btn btn--secondary btn--sm"
                  onClick={() =>
                    setForm((current) => ({ ...current, timeZone: browserTimeZone() }))
                  }
                >
                  Use device
                </button>
              </div>
            )}
          </Field>

          <div className="row">
            <button className="btn" type="submit" disabled={updateSettings.isPending}>
              {updateSettings.isPending ? 'Saving…' : 'Save'}
            </button>
            {saved && <span className="chip chip--positive">Saved</span>}
          </div>
        </form>
      </Card>

      <WhoopCard />

      <Card title="Account">
        <div className="stack-sm">
          <p className="text-sm text-muted">Signed in as {user?.email}</p>
          <div className="row">
            <button type="button" className="btn btn--secondary" onClick={() => void logout()}>
              Sign out
            </button>
          </div>
        </div>
      </Card>
    </div>
  )
}
