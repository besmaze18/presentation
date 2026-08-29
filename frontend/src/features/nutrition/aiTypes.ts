import type { FoodItem, Macros } from './types'

export interface AiAnalysis {
  id: string
  kind: 'TEXT' | 'IMAGE' | 'ESTIMATE'
  status: 'PENDING_REVIEW' | 'CONFIRMED' | 'DISCARDED' | 'FAILED'
  provider: string
  model: string | null
  suggestedName: string | null
  items: FoodItem[]
  totals: Macros
  confidence: number | null
  assumptions: string[]
  notes: string | null
  imageUrl: string | null
  latencyMillis: number | null
  confirmedFoodEntryId: string | null
  createdAt: string
}

export interface AiStatus {
  available: boolean
  provider: string
}
