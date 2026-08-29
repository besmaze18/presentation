import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { WhoopStatus, WhoopSyncResult } from './types'

export function useWhoopStatus() {
  return useQuery({
    queryKey: ['whoop', 'status'],
    queryFn: () => api.get<WhoopStatus>('/api/whoop/status'),
  })
}

/**
 * Starts the OAuth flow. The server owns the client id, secret and state; the browser only ever
 * receives a URL to visit.
 */
export function useConnectWhoop() {
  return useMutation({
    mutationFn: () => api.post<{ authorizationUrl: string }>('/api/whoop/authorize'),
    onSuccess: (response) => {
      window.location.assign(response.authorizationUrl)
    },
  })
}

export function useSyncWhoop() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => api.post<WhoopSyncResult>('/api/whoop/sync'),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['whoop'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      void queryClient.invalidateQueries({ queryKey: ['analytics'] })
      void queryClient.invalidateQueries({ queryKey: ['training'] })
    },
  })
}

export function useDisconnectWhoop() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => api.delete<void>('/api/whoop/connection'),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['whoop'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })
}
