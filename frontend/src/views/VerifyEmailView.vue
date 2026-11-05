<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import RobotChef from '@/components/RobotChef.vue'

const auth = useAuthStore()
const route = useRoute()
const state = ref('loading')
const error = ref('')

const mood = computed(() => ({ loading: 'thinking', success: 'happy', error: 'confused' })[state.value])

onMounted(async () => {
  const token = typeof route.query.token === 'string' ? route.query.token : ''
  if (!token) {
    state.value = 'error'
    error.value = 'Link de confirmação incompleto. Abra o link direto do e-mail.'
    return
  }
  try {
    await auth.verifyEmail(token)
    state.value = 'success'
  } catch (e) {
    state.value = 'error'
    error.value = e.message
  }
})
</script>

<template>
  <main class="verify">
    <section class="card glass">
      <RobotChef :mood="mood" :size="170" />
      <template v-if="state === 'loading'">
        <h1>Conferindo seu link...</h1>
        <p>O Chef Magic está validando a sua confirmação.</p>
      </template>
      <template v-else-if="state === 'success'">
        <span class="eyebrow">Tudo certo</span>
        <h1>E-mail <span class="gradient-text">confirmado!</span></h1>
        <p>As receitas com IA estão liberadas. Bora cozinhar?</p>
        <RouterLink v-if="auth.isAuthenticated" :to="{ name: 'chef' }" class="btn btn-primary">Pedir minha primeira receita</RouterLink>
        <RouterLink v-else :to="{ name: 'login' }" class="btn btn-primary">Entrar na cozinha</RouterLink>
      </template>
      <template v-else>
        <h1>Ops, link inválido</h1>
        <p class="error">{{ error }}</p>
        <RouterLink :to="auth.isAuthenticated ? { name: 'recipes' } : { name: 'login' }" class="btn">
          {{ auth.isAuthenticated ? 'Voltar e pedir um novo e-mail' : 'Ir para o login' }}
        </RouterLink>
      </template>
    </section>
  </main>
</template>

<style scoped>
.verify {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px 16px;
}
.card {
  width: min(460px, 100%);
  display: grid;
  justify-items: center;
  gap: 12px;
  padding: 36px 28px;
  text-align: center;
}
h1 {
  font-size: 28px;
  font-weight: 800;
}
p {
  margin: 0 0 8px;
  color: var(--text-soft);
}
.error {
  color: #ffc2cc;
}
</style>
