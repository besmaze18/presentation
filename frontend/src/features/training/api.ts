import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { AnnotatePayload, TrainingSession, TrainingSessionPayload } from './types'

function useInvalidateTraining() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: ['training'] })
    void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    void queryClient.invalidateQueries({ queryKey: ['analytics'] })
  }
}

export function useTrainingSessions() {
  return useQuery({
    queryKey: ['training', 'sessions'],
    queryFn: () => api.get<TrainingSession[]>('/api/training/sessions', { size: 60 }),
  })
}

export function useTrainingSession(id: string | null) {
  return useQuery({
    queryKey: ['training', 'session', id],
    queryFn: () => api.get<TrainingSession>(`/api/training/sessions/${id}`),
    enabled: id !== null,
  })
}

export function useCreateTrainingSession() {
  const invalidate = useInvalidateTraining()
  return useMutation({
    mutationFn: (payload: TrainingSessionPayload) =>
      api.post<TrainingSession>('/api/training/sessions', payload),
    onSuccess: invalidate,
  })
}

export function useUpdateTrainingSession() {
  const invalidate = useInvalidateTraining()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: TrainingSessionPayload }) =>
      api.put<TrainingSession>(`/api/training/sessions/${id}`, payload),
    onSuccess: invalidate,
  })
}

/** Imported sessions accept only these fields; measured values stay as the device reported them. */
export function useAnnotateTrainingSession() {
  const invalidate = useInvalidateTraining()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AnnotatePayload }) =>
      api.patch<TrainingSession>(`/api/training/sessions/${id}`, payload),
    onSuccess: invalidate,
  })
}

export function useDeleteTrainingSession() {
  const invalidate = useInvalidateTraining()
  return useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/training/sessions/${id}`),
    onSuccess: invalidate,
  })
}
