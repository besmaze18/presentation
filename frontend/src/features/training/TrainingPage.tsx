import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Card } from '@/components/Card'
import { EmptyState } from '@/components/EmptyState'
import { ErrorMessage } from '@/components/ErrorMessage'
import { FullPageSpinner } from '@/components/FullPageSpinner'
import { formatDateLabel, formatDuration, formatKcal, formatNumber, formatTime } from '@/lib/format'
import { useTrainingSession, useTrainingSessions } from './api'
import { TrainingSessionSheet } from './TrainingSessionSheet'
import { CATEGORY_LABELS, type TrainingSession } from './types'

export default function TrainingPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)

  const sessions = useTrainingSessions()
  // The list endpoint returns summaries; the full record (with exercises) is fetched on demand.
  const editing = useTrainingSession(editingId)

  useEffect(() => {
    if (searchParams.get('add') === 'session') {
      setCreating(true)
      searchParams.delete('add')
      setSearchParams(searchParams, { replace: true })
    }
  }, [searchParams, setSearchParams])

  const grouped = useMemo(() => {
    const byDate = new Map<string, TrainingSession[]>()
    for (const session of sessions.data ?? []) {
      const list = byDate.get(session.sessionDate) ?? []
      list.push(session)
      byDate.set(session.sessionDate, list)
    }
    return [...byDate.entries()]
  }, [sessions.data])

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>Training</h1>
          <p className="page-header__subtitle">Manual sessions and imported workouts together</p>
        </div>
        <button type="button" className="btn" onClick={() => setCreating(true)}>
          Add session
        </button>
      </header>

      <ErrorMessage error={sessions.error} />

      {!sessions.data ? (
        <FullPageSpinner label="Loading your training…" />
      ) : grouped.length === 0 ? (
        <Card>
          <EmptyState
            icon="🏋"
            title="No training yet"
            description="Add a session by hand, or connect WHOOP in Settings to import your workouts."
          />
        </Card>
      ) : (
        grouped.map(([date, daySessions]) => (
          <Card key={date} flush>
            <p className="meal-group__title">{formatDateLabel(date)}</p>
            <div className="list">
              {daySessions.map((session) => (
                <button
                  key={session.id}
                  type="button"
                  className="list__item"
                  onClick={() => setEditingId(session.id)}
                >
                  <span>
                    <span className="list__primary">{session.title}</span>
                    <span className="list__secondary" style={{ display: 'block' }}>
                      {formatTime(session.startedAt)} · {CATEGORY_LABELS[session.category]} ·{' '}
                      {formatDuration(session.durationMinutes)}
                      {session.imported && ' · WHOOP'}
                      {session.perceivedExertion != null && ` · RPE ${session.perceivedExertion}`}
                    </span>
                  </span>
                  <span className="list__meta">
                    {session.strain != null && (
                      <span className="list__primary">{formatNumber(session.strain, 1)}</span>
                    )}
                    {session.caloriesKcal != null && (
                      <span className="list__secondary" style={{ display: 'block' }}>
                        {formatKcal(session.caloriesKcal)}
                      </span>
                    )}
                  </span>
                </button>
              ))}
            </div>
          </Card>
        ))
      )}

      {creating && <TrainingSessionSheet onClose={() => setCreating(false)} />}
      {editingId && editing.data && (
        <TrainingSessionSheet session={editing.data} onClose={() => setEditingId(null)} />
      )}
    </div>
  )
}
