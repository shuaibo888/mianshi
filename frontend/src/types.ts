export type CandidateStatus =
  | 'APPLIED'
  | 'SCREEN_PASSED'
  | 'INTERVIEWING'
  | 'OFFER_PENDING'
  | 'HIRED'
  | 'REJECTED'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface Candidate {
  id: number
  name: string
  phone: string
  position: string
  workYears: number
  status: CandidateStatus
  statusLabel: string
  rating: number | null
  remark: string | null
  hasResume: boolean
  resumeOriginalName: string | null
  resumeSize: number | null
  createdAt: string
  updatedAt: string
}

export interface CandidatePayload {
  name: string
  phone: string
  position: string
  workYears: number
  status: CandidateStatus
  remark: string
}

export interface InterviewRecord {
  id: number
  candidateId: number
  interviewTime: string
  interviewer: string
  score: number | null
  content: string | null
  createdAt: string
  updatedAt: string
}

export interface StatusLog {
  id: number
  fromStatus: CandidateStatus | null
  fromStatusLabel: string
  toStatus: CandidateStatus
  toStatusLabel: string
  note: string | null
  createdAt: string
}

export interface CandidateDetail {
  candidate: Candidate
  interviews: InterviewRecord[]
  statusLogs: StatusLog[]
}

export interface DashboardStats {
  totalCandidates: number
  statusCounts: Record<CandidateStatus, number>
  averageRating: number
  hireRate: number
}

export interface OptionItem {
  value: CandidateStatus
  label: string
}
