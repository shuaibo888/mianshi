import { http, request } from './http'
import type { DashboardStats } from '@/types'

export function getDashboardStats() {
  return request<DashboardStats>(http.get('/dashboard/stats'))
}
