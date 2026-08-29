import { useMutation, useQuery } from '@tanstack/react-query'
import { api, apiRequest } from '@/lib/api'
import { useInvalidateNutrition } from './api'
import type { AiAnalysis, AiStatus } from './aiTypes'
import type { CreateFoodEntryPayload, FoodEntry } from './types'

export function useAiStatus() {
  return useQuery({
    queryKey: ['ai', 'status'],
    queryFn: () => api.get<AiStatus>('/api/ai/food-analysis/status'),
    staleTime: 5 * 60_000,
  })
}

export function useAnalyzeText() {
  return useMutation({
    mutationFn: (description: string) =>
      api.post<AiAnalysis>('/api/ai/food-analysis/text', { description }),
  })
}

export function useAnalyzeImage() {
  return useMutation({
    mutationFn: ({ file, hint }: { file: File; hint?: string }) => {
      const form = new FormData()
      form.append('image', file)
      const query = hint && hint.trim() ? `?hint=${encodeURIComponent(hint.trim())}` : ''
      return apiRequest<AiAnalysis>(`/api/ai/food-analysis/image${query}`, {
        method: 'POST',
        body: form,
      })
    },
  })
}

/** Saves the values the user accepted on the review screen - not the raw prediction. */
export function useConfirmAnalysis() {
  const invalidate = useInvalidateNutrition()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CreateFoodEntryPayload }) =>
      api.post<FoodEntry>(`/api/ai/food-analysis/${id}/confirm`, payload),
    onSuccess: invalidate,
  })
}

export function useDiscardAnalysis() {
  return useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/ai/food-analysis/${id}`),
  })
}
