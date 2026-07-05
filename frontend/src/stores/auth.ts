import { defineStore } from 'pinia'
import { loginApi, meApi } from '@/api/auth'

interface AuthState {
  token: string
  username: string
  displayName: string
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('mini_ats_token') || '',
    username: localStorage.getItem('mini_ats_username') || '',
    displayName: localStorage.getItem('mini_ats_display_name') || '',
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    async login(username: string, password: string) {
      const result = await loginApi(username, password)
      this.token = result.token
      this.username = result.username
      this.displayName = result.displayName
      localStorage.setItem('mini_ats_token', result.token)
      localStorage.setItem('mini_ats_username', result.username)
      localStorage.setItem('mini_ats_display_name', result.displayName)
    },
    async refreshMe() {
      if (!this.token) return
      const result = await meApi()
      this.username = result.username
      this.displayName = result.displayName
    },
    logout() {
      this.token = ''
      this.username = ''
      this.displayName = ''
      localStorage.removeItem('mini_ats_token')
      localStorage.removeItem('mini_ats_username')
      localStorage.removeItem('mini_ats_display_name')
    },
  },
})
