export interface MacroProgress {
  consumed: number
  target: number | null
  remaining: number | null
  percentOfTarget: number | null
}

export interface EnergySummary {
  intakeKcal: number
  wearableExpenditureKcal: number | null
  estimatedBmrKcal: number | null
  estimatedTdeeKcal: number | null
  balanceKcal: number | null
  balanceBasis: 'WEARABLE_EXPENDITURE' | 'ESTIMATED_TDEE' | 'NONE'
  weightKg: number | null
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

export interface WearableDaySnapshot {
  date: string
  recoveryScore: number | null
  restingHeartRate: number | null
  hrvMilli: number | null
  strain: number | null
  expenditureKcal: number | null
  sleepDurationMillis: number | null
  sleepNeedMillis: number | null
  sleepPerformancePercentage: number | null
  workoutCount: number
}

export interface Insight {
  code: string
  text: string
  tone: 'NEUTRAL' | 'POSITIVE' | 'WARNING'
}

export interface WorkoutSummary {
  id: string
  title: string
  category: string
  sportLabel: string | null
  startedAt: string
  durationMinutes: number
  perceivedExertion: number | null
  caloriesKcal: number | null
  strain: number | null
  source: string
  imported: boolean
}

export interface Dashboard {
  date: string
  timeZone: string
  nutrition: {
    calories: MacroProgress
    protein: MacroProgress
    carbs: MacroProgress
    fat: MacroProgress
    fiber: MacroProgress
    entryCount: number
  }
  energy: EnergySummary
  weight: WeightTrend
  wearable: WearableDaySnapshot
  wearableConnected: boolean
  workoutsToday: WorkoutSummary[]
  insights: Insight[]
}
