import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { Dashboard } from './types'

export function useDashboard(date: string) {
  return useQuery({
    queryKey: ['dashboard', date],
    queryFn: () => api.get<Dashboard>('/api/dashboard', { date }),
    // The Today view changes as the user logs, so keep it fresher than the default.
    staleTime: 10_000,
  })
}
