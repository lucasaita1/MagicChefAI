<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/AppHeader.vue'
import VerifyEmailBanner from '@/components/VerifyEmailBanner.vue'

const route = useRoute()
const auth = useAuthStore()
const showHeader = computed(() => Boolean(route.meta?.requiresAuth))
const showBanner = computed(() => showHeader.value && auth.isAuthenticated && auth.user && !auth.isVerified)

/** Picks up a confirmation done in another tab (the e-mail link opens a new one). */
function syncVerification() {
  if (document.visibilityState === 'visible' && auth.isAuthenticated && !auth.isVerified) {
    auth.refreshUser().catch(() => {})
  }
}

onMounted(() => {
  syncVerification()
  document.addEventListener('visibilitychange', syncVerification)
})
onBeforeUnmount(() => document.removeEventListener('visibilitychange', syncVerification))
</script>

<template>
  <AppHeader v-if="showHeader" />
  <VerifyEmailBanner v-if="showBanner" />
  <RouterView v-slot="{ Component }">
    <Transition name="page" mode="out-in">
      <component :is="Component" />
    </Transition>
  </RouterView>
</template>
