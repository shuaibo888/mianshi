import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import CandidateListView from '@/views/CandidateListView.vue'
import CandidateDetailView from '@/views/CandidateDetailView.vue'
import DashboardView from '@/views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
    { path: '/', name: 'candidates', component: CandidateListView },
    { path: '/candidates/:id', name: 'candidate-detail', component: CandidateDetailView },
    { path: '/dashboard', name: 'dashboard', component: DashboardView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) {
    return '/login'
  }
  if (to.path === '/login' && auth.isLoggedIn) {
    return '/'
  }
  return true
})

export default router
