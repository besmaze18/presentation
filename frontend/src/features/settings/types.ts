export interface UserSettings {
  timeZone: string
  unitSystem: 'METRIC' | 'IMPERIAL'
  sex: 'MALE' | 'FEMALE' | 'UNSPECIFIED'
  birthDate: string | null
  heightCm: number | null
  activityLevel: 'SEDENTARY' | 'LIGHT' | 'MODERATE' | 'ACTIVE' | 'VERY_ACTIVE'
}

export interface NutritionGoal {
  id: string
  effectiveFrom: string
  calorieTarget: number
  proteinTargetG: number
  carbsTargetG: number
  fatTargetG: number
  fiberTargetG: number
  targetBodyWeightKg: number | null
}

export interface GoalPayload {
  effectiveFrom?: string
  calorieTarget: number
  proteinTargetG: number
  carbsTargetG: number
  fatTargetG: number
  fiberTargetG: number
  targetBodyWeightKg?: number | null
}
