<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import RobotChef from '@/components/RobotChef.vue'
import AppIcon from '@/components/AppIcon.vue'

const FLOATING = [
  { icon: 'chili', color: '#ff6b6b' },
  { icon: 'carrot', color: '#ffa94d' },
  { icon: 'garlic', color: '#e9e3ff' },
  { icon: 'egg', color: '#ffe8a3' },
  { icon: 'cheese', color: '#ffd43b' },
  { icon: 'leaf', color: '#69db7c' },
  { icon: 'apple', color: '#ff8787' },
  { icon: 'mushroom', color: '#d0bfff' },
  { icon: 'wheat', color: '#ffc078' },
  { icon: 'bread', color: '#e8b07a' }
]
/** How many characters it takes for the robot's eyes to sweep across a field. */
const GAZE_SPAN = 28

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const mode = ref('login')
const form = reactive({ name: '', email: '', password: '' })
const showPassword = ref(false)
const loading = ref(false)
const error = ref(route.query.expired ? 'Sua sessão expirou. Entre novamente.' : '')
const fieldErrors = ref({})
const shakeKey = ref(0)
const focused = ref(null)

const isRegister = computed(() => mode.value === 'register')
const passwordVisible = computed(() => showPassword.value && form.password.length > 0)
const robotMood = computed(() => {
  if (loading.value) return 'thinking'
  // Polite robot: covers its eyes while the password is visible
  if (passwordVisible.value) return 'shy'
  if (error.value) return 'confused'
  if (focused.value) return 'idle'
  return isRegister.value ? 'happy' : 'wave'
})

/** Eyes follow the field being typed and sweep along the text as it grows. */
const robotLook = computed(() => {
  if (!focused.value || passwordVisible.value) return { x: 0, y: 0 }
  const progress = Math.min(form[focused.value].length, GAZE_SPAN) / GAZE_SPAN
  const stacked = typeof window !== 'undefined' && window.matchMedia?.('(max-width: 900px)').matches
  if (stacked) return { x: -0.7 + progress * 1.4, y: 1 }
  const row = { name: 0.15, email: 0.5, password: 0.9 }[focused.value]
  return { x: 0.3 + progress * 0.7, y: row }
})

function switchMode(next) {
  mode.value = next
  error.value = ''
  fieldErrors.value = {}
}

async function submit() {
  loading.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    if (isRegister.value) {
      await auth.register({ name: form.name, email: form.email, password: form.password })
    } else {
      await auth.login({ email: form.email, password: form.password })
    }
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/') ? route.query.redirect : '/'
    router.push(redirect)
  } catch (e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
    shakeKey.value++
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login">
    <section class="hero" aria-hidden="true">
      <div class="orbit">
        <span v-for="(item, i) in FLOATING" :key="item.icon" class="food" :style="{ '--i': i, '--n': FLOATING.length }">
          <span class="food-inner" :style="{ '--c': item.color }"><AppIcon :name="item.icon" :size="22" /></span>
        </span>
      </div>
      <div class="halo" />
      <RobotChef :mood="robotMood" :look-x="robotLook.x" :look-y="robotLook.y" :size="250" class="hero-bot" />
    </section>

    <section class="panel">
      <div class="intro">
        <span class="eyebrow">IA generativa na sua cozinha</span>
        <h1>Seu chef particular, <span class="gradient-text">movido a IA.</span></h1>
        <p>Diga o que tem na geladeira e o <strong>Chef Magic</strong> cria uma receita sob medida em segundos.</p>
      </div>

      <div :key="shakeKey" class="card glass" :class="{ shake: shakeKey > 0 }">
        <div class="tabs" role="tablist">
          <button role="tab" :aria-selected="!isRegister" :class="{ active: !isRegister }" @click="switchMode('login')">Entrar</button>
          <button role="tab" :aria-selected="isRegister" :class="{ active: isRegister }" @click="switchMode('register')">Criar conta</button>
          <span class="tab-indicator" :class="{ right: isRegister }" />
        </div>

        <form novalidate @submit.prevent="submit">
          <Transition name="fade-up">
            <div v-if="isRegister" class="field">
              <label for="name">Como o Chef deve te chamar?</label>
              <input id="name" v-model.trim="form.name" class="input" autocomplete="name" placeholder="Seu nome" maxlength="80" required @focus="focused = 'name'" @blur="focused = null" />
              <span v-if="fieldErrors.name" class="field-error">{{ fieldErrors.name }}</span>
            </div>
          </Transition>

          <div class="field">
            <label for="email">E-mail</label>
            <input id="email" v-model.trim="form.email" class="input" type="email" autocomplete="email" placeholder="voce@email.com" required @focus="focused = 'email'" @blur="focused = null" />
            <span v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</span>
          </div>

          <div class="field">
            <label for="password">Senha</label>
            <div class="password">
              <input
                id="password"
                v-model="form.password"
                class="input"
                :type="showPassword ? 'text' : 'password'"
                :autocomplete="isRegister ? 'new-password' : 'current-password'"
                :placeholder="isRegister ? 'Mínimo 8 caracteres, letras e números' : '••••••••'"
                maxlength="72"
                required
                @focus="focused = 'password'"
                @blur="focused = null"
              />
              <button type="button" class="reveal" :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'" @click="showPassword = !showPassword">
                <AppIcon :name="showPassword ? 'eye-off' : 'eye'" :size="18" />
              </button>
            </div>
            <span v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</span>
          </div>

          <Transition name="fade-up">
            <p v-if="error" class="alert" role="alert">{{ error }}</p>
          </Transition>

          <button class="btn btn-primary submit" type="submit" :disabled="loading">
            <span v-if="loading" class="spinner" />
            <span>{{ loading ? 'Aquecendo as panelas...' : isRegister ? 'Criar minha conta' : 'Entrar na cozinha' }}</span>
          </button>
        </form>

        <p class="secure"><AppIcon name="lock" :size="13" /> Sessão protegida com JWT + cookie HttpOnly</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  align-items: center;
  gap: 40px;
  width: min(1180px, 100% - 32px);
  margin-inline: auto;
  padding: 40px 0;
}

.hero {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 520px;
}
.halo {
  position: absolute;
  width: 340px;
  height: 340px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 94, 126, 0.35), rgba(139, 108, 255, 0.15) 45%, transparent 70%);
  filter: blur(10px);
  animation: breathe 6s ease-in-out infinite;
}
.hero-bot {
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 30px 40px rgba(0, 0, 0, 0.45));
}
.orbit {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  animation: orbit 40s linear infinite;
}
.food {
  position: absolute;
  font-size: 34px;
  --angle: calc(360deg / var(--n) * var(--i));
  transform: rotate(var(--angle)) translateX(220px) rotate(calc(-1 * var(--angle)));
  filter: drop-shadow(0 8px 14px rgba(0, 0, 0, 0.4));
}
/* Counter-rotates so the icons stay upright while the ring spins */
.food-inner {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  color: var(--c);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 24px -6px var(--c), inset 0 0 12px -6px var(--c);
  backdrop-filter: blur(6px);
  animation: orbit 40s linear infinite reverse;
}

.panel {
  display: grid;
  gap: 28px;
  max-width: 460px;
}
.intro h1 {
  margin: 10px 0 12px;
  font-size: clamp(30px, 4vw, 44px);
  font-weight: 800;
}
.intro p {
  margin: 0;
  color: var(--text-soft);
  font-size: 16px;
}

.card {
  padding: 26px;
}
.card.shake {
  animation: shake 0.5s;
}
.tabs {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  padding: 4px;
  margin-bottom: 22px;
  border-radius: 14px;
  background: rgba(10, 8, 26, 0.55);
  border: 1px solid var(--border);
}
.tabs button {
  position: relative;
  z-index: 1;
  height: 40px;
  border: 0;
  background: none;
  color: var(--text-dim);
  font-weight: 600;
  cursor: pointer;
  transition: color 0.25s;
}
.tabs button.active {
  color: var(--text);
}
.tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: 40px;
  border-radius: 10px;
  background: var(--surface-strong);
  box-shadow: inset 0 0 0 1px var(--border-strong);
  transition: transform 0.35s var(--ease-spring);
}
.tab-indicator.right {
  transform: translateX(100%);
}

form {
  display: grid;
  gap: 16px;
}
.password {
  position: relative;
}
.password .input {
  padding-right: 48px;
}
.reveal {
  display: grid;
  place-items: center;
  color: var(--text-dim);
  transition: color 0.2s, background 0.2s;
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  background: none;
  cursor: pointer;
}
.alert {
  margin: 0;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 107, 129, 0.1);
  border: 1px solid rgba(255, 107, 129, 0.3);
  color: #ffc2cc;
  font-size: 14px;
}
.submit {
  min-height: 50px;
  margin-top: 4px;
  font-size: 15.5px;
}
.reveal:hover {
  color: var(--text);
  background: var(--surface);
}
.secure {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 16px 0 0;
  text-align: center;
  font-size: 12.5px;
  color: var(--text-dim);
}

@keyframes orbit {
  to { transform: rotate(360deg); }
}
@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.12); opacity: 1; }
}

@media (max-width: 900px) {
  .login {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 20px 0 40px;
  }
  .hero {
    min-height: 300px;
  }
  .hero-bot {
    width: 170px;
    height: 196px;
  }
  .food-inner {
    width: 36px;
    height: 36px;
  }
  .food {
    transform: rotate(var(--angle)) translateX(140px) rotate(calc(-1 * var(--angle)));
  }
  .halo {
    width: 220px;
    height: 220px;
  }
  .panel {
    max-width: none;
  }
  .intro {
    text-align: center;
  }
}
</style>
