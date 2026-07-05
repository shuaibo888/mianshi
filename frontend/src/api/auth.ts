import { http, request } from './http'

export interface LoginResult {
  token: string
  username: string
  displayName: string
}

export function loginApi(username: string, password: string) {
  return request<LoginResult>(http.post('/auth/login', { username, password }))
}

export function meApi() {
  return request<{ username: string; displayName: string }>(http.get('/auth/me'))
}
