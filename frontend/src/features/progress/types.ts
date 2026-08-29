export interface AnalyticsBucket {
  date: string
  label: string
  calories: number | null
  proteinG: number | null
  carbsG: number | null
  fatG: number | null
  fiberG: number | null
  foodEntryCount: number
  weightKg: number | null
  workoutCount: number
  trainingMinutes: number
  recoveryScore: number | null
  strain: number | null
  sleepDurationMillis: number | null
}

export interface AnalyticsTotals {
  averageCalories: number | null
  averageProteinG: number | null
  averageCarbsG: number | null
  averageFatG: number | null
  averageFiberG: number | null
  totalWorkouts: number
  totalTrainingMinutes: number
  averageRecovery: number | null
  averageStrain: number | null
  averageSleepHours: number | null
  weightChangeKg: number | null
  daysLogged: number
}

export interface AnalyticsSeries {
  from: string
  to: string
  granularity: 'DAY' | 'WEEK' | 'MONTH'
  buckets: AnalyticsBucket[]
  totals: AnalyticsTotals
}

export interface BodyMeasurement {
  id: string
  recordedAt: string
  weightKg: number
  bodyFatPercentage: number | null
  source: 'MANUAL' | 'WHOOP' | 'FUTURE_INTEGRATION'
  note: string | null
}

export interface WeightTrend {
  latestKg: number | null
  latestRecordedAt: string | null
  average7dKg: number | null
  average30dKg: number | null
  change7dKg: number | null
  change30dKg: number | null
  targetKg: number | null
  toTargetKg: number | null
}
