import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type {
  CreateFoodEntryPayload,
  DailyNutrition,
  FoodEntry,
  SavedFood,
  SavedFoodPayload,
} from './types'

export const nutritionKeys = {
  day: (date: string) => ['nutrition', 'day', date] as const,
  savedFoods: (query: string) => ['nutrition', 'saved-foods', query] as const,
}

export function useDailyNutrition(date: string) {
  return useQuery({
    queryKey: nutritionKeys.day(date),
    queryFn: () => api.get<DailyNutrition>(`/api/nutrition/days/${date}`),
  })
}

/** Invalidates every view whose numbers derive from logged food. */
export function useInvalidateNutrition() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: ['nutrition'] })
    void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    void queryClient.invalidateQueries({ queryKey: ['analytics'] })
  }
}

export function useCreateFoodEntry() {
  const invalidate = useInvalidateNutrition()
  return useMutation({
    mutationFn: (payload: CreateFoodEntryPayload) =>
      api.post<FoodEntry>('/api/nutrition/entries', payload),
    onSuccess: invalidate,
  })
}

export function useUpdateFoodEntry() {
  const invalidate = useInvalidateNutrition()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CreateFoodEntryPayload }) =>
      api.put<FoodEntry>(`/api/nutrition/entries/${id}`, payload),
    onSuccess: invalidate,
  })
}

export function useDeleteFoodEntry() {
  const invalidate = useInvalidateNutrition()
  return useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/nutrition/entries/${id}`),
    onSuccess: invalidate,
  })
}

export function useSavedFoods(query: string) {
  return useQuery({
    queryKey: nutritionKeys.savedFoods(query),
    queryFn: () => api.get<SavedFood[]>('/api/foods', { query, limit: 25 }),
  })
}

export function useCreateSavedFood() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: SavedFoodPayload) => api.post<SavedFood>('/api/foods', payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['nutrition', 'saved-foods'] }),
  })
}

export function useDeleteSavedFood() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/foods/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['nutrition', 'saved-foods'] }),
  })
}

export function useLogSavedFood() {
  const invalidate = useInvalidateNutrition()
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      id,
      quantity,
      mealType,
      consumedAt,
    }: {
      id: string
      quantity: number
      mealType?: string
      consumedAt?: string
    }) => api.post<FoodEntry>(`/api/foods/${id}/log`, { quantity, mealType, consumedAt }),
    onSuccess: () => {
      invalidate()
      void queryClient.invalidateQueries({ queryKey: ['nutrition', 'saved-foods'] })
    },
  })
}
