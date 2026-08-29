import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { GoalPayload, NutritionGoal, UserSettings } from './types'

export function useUserSettings() {
  return useQuery({
    queryKey: ['settings'],
    queryFn: () => api.get<UserSettings>('/api/users/me/settings'),
  })
}

export function useUpdateUserSettings() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: Partial<UserSettings>) =>
      api.put<UserSettings>('/api/users/me/settings', payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['settings'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}

export function useGoals() {
  return useQuery({
    queryKey: ['goals'],
    queryFn: () => api.get<NutritionGoal[]>('/api/users/me/goals', { limit: 10 }),
  })
}

export function useUpsertGoal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: GoalPayload) => api.put<NutritionGoal>('/api/users/me/goals', payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['goals'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}
