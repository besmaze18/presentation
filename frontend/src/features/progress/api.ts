import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { AnalyticsSeries, BodyMeasurement, WeightTrend } from './types'

export type Granularity = 'DAY' | 'WEEK' | 'MONTH'

/** Aggregation happens in the database; the browser only ever receives bucketed rows. */
export function useAnalyticsSeries(from: string, to: string, granularity: Granularity) {
  return useQuery({
    queryKey: ['analytics', 'series', from, to, granularity],
    queryFn: () => api.get<AnalyticsSeries>('/api/analytics/series', { from, to, granularity }),
  })
}

export function useWeightTrend() {
  return useQuery({
    queryKey: ['analytics', 'weight-trend'],
    queryFn: () => api.get<WeightTrend>('/api/body-measurements/trend'),
  })
}

export function useBodyMeasurements() {
  return useQuery({
    queryKey: ['analytics', 'body-measurements'],
    queryFn: () => api.get<BodyMeasurement[]>('/api/body-measurements', { size: 30 }),
  })
}

export function useRecordWeight() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: { weightKg: number; recordedAt?: string; note?: string | null }) =>
      api.post<BodyMeasurement>('/api/body-measurements', payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['analytics'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}

export function useDeleteWeight() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/body-measurements/${id}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['analytics'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}
