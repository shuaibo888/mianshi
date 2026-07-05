import { http, request } from './http'
import type {
  Candidate,
  CandidateDetail,
  CandidatePayload,
  CandidateStatus,
  InterviewRecord,
  OptionItem,
  PageResponse,
  StatusLog,
} from '@/types'

export interface CandidateQuery {
  keyword?: string
  status?: CandidateStatus | ''
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export function listCandidates(params: CandidateQuery) {
  return request<PageResponse<Candidate>>(http.get('/candidates', { params }))
}

export function createCandidate(payload: CandidatePayload) {
  return request<Candidate>(http.post('/candidates', payload))
}

export function updateCandidate(id: number, payload: CandidatePayload) {
  return request<Candidate>(http.put(`/candidates/${id}`, payload))
}

export function uploadCandidateResume(id: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request<Candidate>(http.post(`/candidates/${id}/resume`, formData))
}

export async function fetchCandidateResume(id: number) {
  const response = await http.get<Blob>(`/candidates/${id}/resume`, { responseType: 'blob' })
  return response.data
}

export function deleteCandidate(id: number) {
  return request<void>(http.delete(`/candidates/${id}`))
}

export function getCandidate(id: number) {
  return request<CandidateDetail>(http.get(`/candidates/${id}`))
}

export function changeCandidateStatus(id: number, status: CandidateStatus, note: string) {
  return request<Candidate>(http.post(`/candidates/${id}/status`, { status, note }))
}

export function listStatusLogs(id: number) {
  return request<StatusLog[]>(http.get(`/candidates/${id}/status-logs`))
}

export function createInterview(
  candidateId: number,
  payload: { interviewTime: string; interviewer: string; score: number | null; content: string },
) {
  return request<InterviewRecord>(http.post(`/candidates/${candidateId}/interviews`, payload))
}

export function updateInterview(
  id: number,
  payload: { interviewTime: string; interviewer: string; score: number | null; content: string },
) {
  return request<InterviewRecord>(http.put(`/interviews/${id}`, payload))
}

export function deleteInterview(id: number) {
  return request<void>(http.delete(`/interviews/${id}`))
}

export function getOptions() {
  return request<{ statuses: OptionItem[] }>(http.get('/options'))
}
