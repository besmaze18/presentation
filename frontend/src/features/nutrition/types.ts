export interface Macros {
  calories: number
  proteinG: number
  carbsG: number
  fatG: number
  fiberG: number
}

export const EMPTY_MACROS: Macros = {
  calories: 0,
  proteinG: 0,
  carbsG: 0,
  fatG: 0,
  fiberG: 0,
}

export const MEAL_TYPES = [
  'BREAKFAST',
  'LUNCH',
  'DINNER',
  'SNACK',
  'PRE_WORKOUT',
  'POST_WORKOUT',
  'OTHER',
] as const

export type MealType = (typeof MEAL_TYPES)[number]

export const MEAL_TYPE_LABELS: Record<MealType, string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
  SNACK: 'Snack',
  PRE_WORKOUT: 'Pre-workout',
  POST_WORKOUT: 'Post-workout',
  OTHER: 'Other',
}

export interface FoodItem {
  name: string
  quantity: number | null
  unit: string | null
  macros: Macros
}

export interface FoodEntry {
  id: string
  name: string
  mealType: MealType
  consumedAt: string
  entryDate: string
  quantity: number | null
  unit: string | null
  macros: Macros
  source: 'MANUAL' | 'SAVED_FOOD' | 'AI_TEXT' | 'AI_IMAGE'
  aiAnalysisId: string | null
  savedFoodId: string | null
  notes: string | null
  items: FoodItem[]
  createdAt: string
}

export interface DailyNutrition {
  date: string
  totals: Macros
  entryCount: number
  entries: FoodEntry[]
}

export interface SavedFood {
  id: string
  name: string
  brand: string | null
  servingQuantity: number
  servingUnit: string
  macros: Macros
  defaultMealType: MealType | null
  usageCount: number
  lastUsedAt: string | null
}

export interface SavedFoodPayload {
  name: string
  brand?: string | null
  servingQuantity: number
  servingUnit: string
  macros: Macros
  defaultMealType?: MealType | null
}

export interface CreateFoodEntryPayload {
  name: string
  mealType: MealType
  consumedAt?: string
  quantity?: number | null
  unit?: string | null
  macros?: Macros
  items?: FoodItem[]
  notes?: string | null
  aiAnalysisId?: string | null
}
