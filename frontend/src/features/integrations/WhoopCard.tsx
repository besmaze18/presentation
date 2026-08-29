import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Card } from '@/components/Card'
import { ErrorMessage } from '@/components/ErrorMessage'
import { formatNumber } from '@/lib/format'
import { useConnectWhoop, useDisconnectWhoop, useSyncWhoop, useWhoopStatus } from './api'
import type { WhoopSyncResult } from './types'

const CALLBACK_MESSAGES: Record<string, { text: string; className: string }> = {
  connected: { text: 'WHOOP connected. Run a sync to import your history.', className: 'alert alert--info' },
  denied: { text: 'WHOOP authorisation was declined.', className: 'alert alert--warning' },
  error: { text: 'WHOOP could not be connected. Please try again.', className: 'alert alert--error' },
}

function describeSync(result: WhoopSyncResult): string {
  const imported =
    result.cyclesImported + result.recoveriesImported + result.sleepsImported + result.workoutsImported
  const updated =
    result.cyclesUpdated + result.recoveriesUpdated + result.sleepsUpdated + result.workoutsUpdated
  if (imported === 0 && updated === 0) {
    return 'Already up to date.'
  }
  const parts = []
  if (imported > 0) parts.push(`${imported} new`)
  if (updated > 0) parts.push(`${updated} updated`)
  if (result.trainingSessionsCreated > 0) {
    parts.push(`${result.trainingSessionsCreated} workout(s) added to training`)
  }
  return `Synced: ${parts.join(', ')}.`
}

export function WhoopCard() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [callbackOutcome, setCallbackOutcome] = useState<string | null>(null)

  const status = useWhoopStatus()
  const connect = useConnectWhoop()
  const sync = useSyncWhoop()
  const disconnect = useDisconnectWhoop()

  // The OAuth callback redirects back here with ?whoop=<outcome>; show it once, then clean the URL.
  useEffect(() => {
    const outcome = searchParams.get('whoop')
    if (!outcome) return
    setCallbackOutcome(outcome)
    searchParams.delete('whoop')
    setSearchParams(searchParams, { replace: true })
  }, [searchParams, setSearchParams])

  const data = status.data
  const message = callbackOutcome ? CALLBACK_MESSAGES[callbackOutcome] : null

  return (
    <Card title="WHOOP">
      <div className="stack">
        {message && <div className={message.className}>{message.text}</div>}
        <ErrorMessage error={status.error ?? connect.error ?? sync.error ?? disconnect.error} />

        {!data ? (
          <p className="text-sm text-muted">Checking connection…</p>
        ) : !data.configured ? (
          <p className="text-sm text-muted">
            WHOOP is not configured on this server. Set <code>WHOOP_CLIENT_ID</code> and{' '}
            <code>WHOOP_CLIENT_SECRET</code> to enable it.
          </p>
        ) : !data.connected ? (
          <>
            <div className="row-between">
              <span className="chip">
                {data.status === 'REAUTHORISATION_REQUIRED' ? 'Reconnect needed' : 'Not connected'}
              </span>
            </div>
            <p className="text-sm text-muted">
              Connect your WHOOP account to import recovery, sleep, strain and workouts. Your
              credentials stay with WHOOP; this app never sees them.
            </p>
            {data.lastSyncError && (
              <p className="text-sm" style={{ color: 'var(--danger)' }}>
                {data.lastSyncError}
              </p>
            )}
            <button
              type="button"
              className="btn"
              disabled={connect.isPending}
              onClick={() => connect.mutate()}
            >
              {connect.isPending ? 'Redirecting…' : 'Connect WHOOP'}
            </button>
          </>
        ) : (
          <>
            <div className="row-between">
              <div>
                <p className="list__primary">
                  {[data.whoopFirstName, data.whoopLastName].filter(Boolean).join(' ') ||
                    'WHOOP account'}
                </p>
                <p className="list__secondary">
                  {data.lastSyncAt
                    ? `Last synced ${new Date(data.lastSyncAt).toLocaleString()}`
                    : 'Not synced yet'}
                </p>
              </div>
              <span className="chip chip--positive">Connected</span>
            </div>

            <div className="grid grid-auto">
              <div className="stat">
                <span className="stat__label">Cycles</span>
                <span className="stat__value">{formatNumber(data.cycleCount)}</span>
              </div>
              <div className="stat">
                <span className="stat__label">Recoveries</span>
                <span className="stat__value">{formatNumber(data.recoveryCount)}</span>
              </div>
              <div className="stat">
                <span className="stat__label">Sleeps</span>
                <span className="stat__value">{formatNumber(data.sleepCount)}</span>
              </div>
              <div className="stat">
                <span className="stat__label">Workouts</span>
                <span className="stat__value">{formatNumber(data.workoutCount)}</span>
              </div>
            </div>

            {sync.data && <p className="text-sm text-muted">{describeSync(sync.data)}</p>}
            {data.lastSyncError && !sync.data && (
              <p className="text-sm" style={{ color: 'var(--danger)' }}>
                Last sync failed: {data.lastSyncError}
              </p>
            )}

            <div className="row">
              <button
                type="button"
                className="btn"
                disabled={sync.isPending}
                onClick={() => sync.mutate()}
              >
                {sync.isPending ? 'Syncing…' : 'Sync now'}
              </button>
              <button
                type="button"
                className="btn btn--secondary"
                disabled={disconnect.isPending}
                onClick={() => disconnect.mutate()}
              >
                Disconnect
              </button>
            </div>
            <p className="text-xs text-subtle">
              Data also syncs automatically in the background.
            </p>
          </>
        )}
      </div>
    </Card>
  )
}
