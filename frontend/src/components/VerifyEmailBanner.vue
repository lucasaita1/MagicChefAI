<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppIcon from './AppIcon.vue'

const auth = useAuthStore()
const status = ref('idle')
const message = ref('')

async function resend() {
  status.value = 'sending'
  try {
    await auth.resendVerification()
    status.value = 'sent'
    message.value = 'Novo link enviado! Confira sua caixa de entrada e o spam.'
  } catch (e) {
    status.value = 'error'
    message.value = e.message
  }
}

async function check() {
  try {
    await auth.refreshUser()
    if (!auth.isVerified) {
      status.value = 'error'
      message.value = 'Ainda não identificamos a confirmação. Clique no link do e-mail.'
    }
  } catch (e) {
    status.value = 'error'
    message.value = e.message
  }
}
</script>

<template>
  <div class="banner" role="status">
    <div class="container inner">
      <span class="icon"><AppIcon name="mail" :size="18" /></span>
      <p>
        <strong>Confirme seu e-mail para liberar o Chef Magic.</strong>
        <span v-if="!message"> Enviamos um link para {{ auth.user?.email }}.</span>
        <span v-else :class="status"> {{ message }}</span>
      </p>
      <div class="actions">
        <button class="btn btn-ghost small" :disabled="status === 'sending'" @click="check">Já confirmei</button>
        <button class="btn small" :disabled="status === 'sending'" @click="resend">
          <span v-if="status === 'sending'" class="spinner" />
          Reenviar e-mail
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.banner {
  position: relative;
  z-index: 39;
  background: linear-gradient(90deg, rgba(255, 179, 71, 0.14), rgba(255, 94, 126, 0.12));
  border-bottom: 1px solid rgba(255, 179, 71, 0.25);
}
.inner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
}
.icon {
  display: grid;
  place-items: center;
  flex: none;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 179, 71, 0.18);
  color: var(--saffron);
}
p {
  margin: 0;
  flex: 1;
  font-size: 14px;
  color: var(--text-soft);
}
strong {
  color: var(--text);
}
.sent {
  color: var(--mint);
}
.error {
  color: #ffc2cc;
}
.actions {
  display: flex;
  gap: 8px;
}
.small {
  min-height: 36px;
  padding: 0 14px;
  font-size: 13px;
}
@media (max-width: 760px) {
  .inner {
    flex-wrap: wrap;
  }
  .actions {
    width: 100%;
  }
  .actions .btn {
    flex: 1;
  }
}
</style>
