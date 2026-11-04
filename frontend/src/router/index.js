import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'

export const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true, title: 'Entrar' } },
  { path: '/', name: 'recipes', component: () => import('@/views/RecipesView.vue'), meta: { requiresAuth: true, title: 'Minhas receitas' } },
  { path: '/chef', name: 'chef', component: () => import('@/views/ChefChatView.vue'), meta: { requiresAuth: true, title: 'Chef Magic' } },
  { path: '/despensa', name: 'pantry', component: () => import('@/views/PantryView.vue'), meta: { requiresAuth: true, title: 'Geladeira & Armário' } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

/** Pure guard so it can be unit tested without a browser. */
export function resolveAccess(to, isAuthenticated) {
  if (to.meta?.requiresAuth && !isAuthenticated) {
    return { name: 'login', query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }
  if (to.meta?.guestOnly && isAuthenticated) {
    return { name: 'recipes' }
  }
  return true
}

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => resolveAccess(to, useAuthStore().isAuthenticated))
router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · MagicChefAI` : 'MagicChefAI'
})

export default router
