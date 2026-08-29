export interface WhoopStatus {
  configured: boolean
  connected: boolean
  status:
    | 'NOT_CONFIGURED'
    | 'NOT_CONNECTED'
    | 'CONNECTED'
    | 'REAUTHORISATION_REQUIRED'
    | 'REVOKED'
  whoopFirstName: string | null
  whoopLastName: string | null
  connectedAt: string | null
  lastSyncAt: string | null
  lastSyncError: string | null
  cycleCount: number
  recoveryCount: number
  sleepCount: number
  workoutCount: number
}

export interface WhoopSyncResult {
  syncedAt: string
  windowStart: string
  windowEnd: string
  cyclesImported: number
  cyclesUpdated: number
  recoveriesImported: number
  recoveriesUpdated: number
  sleepsImported: number
  sleepsUpdated: number
  workoutsImported: number
  workoutsUpdated: number
  trainingSessionsCreated: number
  bodyMeasurementsImported: number
  initialSync: boolean
}
