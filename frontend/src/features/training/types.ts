export const TRAINING_CATEGORIES = [
  'STRENGTH',
  'RUNNING',
  'CYCLING',
  'SWIMMING',
  'ROWING',
  'HIIT',
  'CLASS',
  'SPORT',
  'MOBILITY',
  'WALKING',
  'OTHER',
] as const

export type TrainingCategory = (typeof TRAINING_CATEGORIES)[number]

export const CATEGORY_LABELS: Record<TrainingCategory, string> = {
  STRENGTH: 'Strength',
  RUNNING: 'Running',
  CYCLING: 'Cycling',
  SWIMMING: 'Swimming',
  ROWING: 'Rowing',
  HIIT: 'HIIT',
  CLASS: 'Class',
  SPORT: 'Sport',
  MOBILITY: 'Mobility',
  WALKING: 'Walking',
  OTHER: 'Other',
}

export interface ExerciseSet {
  repetitions: number | null
  weightKg: number | null
  rpe: number | null
  distanceMeters: number | null
  durationSeconds: number | null
}

export interface Exercise {
  name: string
  notes: string | null
  sets: ExerciseSet[]
}

export interface TrainingSession {
  id: string
  title: string
  category: TrainingCategory
  sportLabel: string | null
  startedAt: string
  endedAt: string | null
  sessionDate: string
  durationMinutes: number
  perceivedExertion: number | null
  caloriesKcal: number | null
  strain: number | null
  averageHeartRate: number | null
  maxHeartRate: number | null
  distanceMeters: number | null
  notes: string | null
  source: 'MANUAL' | 'WHOOP'
  imported: boolean
  exercises: Exercise[]
}

export interface TrainingSessionPayload {
  title: string
  category: TrainingCategory
  sportLabel?: string | null
  startedAt: string
  durationMinutes: number
  perceivedExertion?: number | null
  caloriesKcal?: number | null
  distanceMeters?: number | null
  notes?: string | null
  exercises?: Exercise[]
}

export interface AnnotatePayload {
  title?: string
  category?: TrainingCategory
  perceivedExertion?: number | null
  notes?: string | null
}
