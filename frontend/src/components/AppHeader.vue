<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePantryStore } from '@/stores/pantry'
import { useRecipesStore } from '@/stores/recipes'
import RobotChef from './RobotChef.vue'
import AppIcon from './AppIcon.vue'

const NAV = [
  { name: 'recipes', label: 'Minhas receitas', icon: 'book' },
  { name: 'pantry', label: 'Geladeira & Armário', icon: 'fridge' }
]

const auth = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)
const menu = ref(null)
const leaving = ref(false)

function closeOnOutside(event) {
  if (menu.value && !menu.value.contains(event.target)) menuOpen.value = false
}
onMounted(() => document.addEventListener('click', closeOnOutside))
onBeforeUnmount(() => document.removeEventListener('click', closeOnOutside))

async function logout() {
  leaving.value = true
  try {
    await auth.logout()
  } finally {
    usePantryStore().reset()
    useRecipesStore().reset()
    leaving.value = false
    menuOpen.value = false
    router.push({ name: 'login' })
  }
}
</script>

<template>
  <header class="header">
    <div class="container bar">
      <RouterLink :to="{ name: 'recipes' }" class="brand" aria-label="MagicChefAI, início">
        <span class="brand-mark"><RobotChef :size="30" mood="idle" /></span>
        <span class="brand-name">Magic<span class="gradient-text">Chef</span><span class="ai">AI</span></span>
      </RouterLink>

      <nav class="nav" aria-label="Principal">
        <RouterLink v-for="item in NAV" :key="item.name" :to="{ name: item.name }" class="nav-link">
          <AppIcon :name="item.icon" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="actions">
        <RouterLink :to="{ name: 'chef' }" class="btn btn-primary cta">
          <AppIcon name="sparkles" />
          <span>Nova receita</span>
        </RouterLink>

        <div ref="menu" class="user">
          <button class="user-btn" :aria-expanded="menuOpen" aria-haspopup="menu" @click="menuOpen = !menuOpen">
            <span class="avatar">{{ auth.firstName.charAt(0).toUpperCase() || '?' }}</span>
            <span class="user-name">{{ auth.firstName }}</span>
            <AppIcon name="chevron-down" :size="16" class="chev" :class="{ open: menuOpen }" />
          </button>
          <Transition name="fade-up">
            <div v-if="menuOpen" class="dropdown glass" role="menu">
              <div class="who">
                <strong>{{ auth.user?.name }}</strong>
                <small>{{ auth.user?.email }}</small>
              </div>
              <button class="item danger" role="menuitem" :disabled="leaving" @click="logout">
                <span v-if="leaving" class="spinner" />
                <AppIcon v-else name="logout" :size="16" />
                Sair da conta
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </header>

  <!-- Mobile tab bar -->
  <nav class="tabbar" aria-label="Navegação móvel">
    <RouterLink :to="{ name: 'recipes' }" class="tab"><AppIcon name="book" :size="20" /><span>Receitas</span></RouterLink>
    <RouterLink :to="{ name: 'chef' }" class="tab tab-main"><span class="tab-orb"><AppIcon name="sparkles" :size="22" /></span><span>Chef</span></RouterLink>
    <RouterLink :to="{ name: 'pantry' }" class="tab"><AppIcon name="fridge" :size="20" /><span>Despensa</span></RouterLink>
  </nav>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 40;
  background: linear-gradient(180deg, rgba(12, 10, 29, 0.85), rgba(12, 10, 29, 0.6));
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border-bottom: 1px solid var(--border);
}
.header::after {
  content: '';
  position: absolute;
  inset: auto 0 -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 140, 110, 0.5), rgba(139, 108, 255, 0.4), transparent);
}
.bar {
  display: flex;
  align-items: center;
  gap: 24px;
  height: 68px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 179, 71, 0.16), rgba(255, 94, 126, 0.16));
  border: 1px solid rgba(255, 140, 110, 0.3);
}
.brand-mark :deep(svg) {
  margin-top: -4px;
}
.brand-name {
  font: 700 19px var(--font-display);
  letter-spacing: -0.03em;
}
.ai {
  margin-left: 3px;
  padding: 1px 6px;
  border-radius: 6px;
  background: var(--surface-strong);
  border: 1px solid var(--border);
  font-size: 12px;
  vertical-align: 3px;
  color: var(--text-soft);
}

.nav {
  display: flex;
  gap: 4px;
  padding: 4px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid var(--border);
}
.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  color: var(--text-dim);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s, color 0.2s;
}
.nav-link:hover {
  color: var(--text);
}
.nav-link.router-link-exact-active {
  color: var(--text);
  background: var(--surface-strong);
  box-shadow: inset 0 0 0 1px var(--border-strong), 0 6px 18px -10px rgba(0, 0, 0, 0.6);
}
.nav-link.router-link-exact-active :deep(svg) {
  color: var(--saffron);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
.cta {
  min-height: 40px;
  padding: 0 16px;
  font-size: 14px;
}

.user {
  position: relative;
}
.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 10px 0 4px;
  border: 1px solid var(--border);
  border-radius: 99px;
  background: var(--surface);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.user-btn:hover {
  border-color: var(--border-strong);
  background: var(--surface-strong);
}
.avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--grad-cool);
  color: #0c0a1d;
  font-weight: 700;
  font-size: 14px;
}
.user-name {
  font-size: 14px;
  font-weight: 600;
}
.chev {
  color: var(--text-dim);
  transition: transform 0.25s var(--ease-out);
}
.chev.open {
  transform: rotate(180deg);
}
.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  width: 240px;
  padding: 8px;
  border-radius: 16px;
  background: rgba(22, 18, 48, 0.96);
}
.who {
  display: grid;
  padding: 10px 12px 12px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 6px;
}
.who small {
  color: var(--text-dim);
  overflow: hidden;
  text-overflow: ellipsis;
}
.item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: 10px;
  background: none;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
}
.item.danger:hover {
  background: rgba(255, 107, 129, 0.1);
  color: var(--danger);
}

.tabbar {
  display: none;
}

@media (max-width: 1000px) {
  .nav-link span {
    display: none;
  }
  .nav-link {
    padding: 0 11px;
  }
}

@media (max-width: 760px) {
  .nav,
  .cta,
  .user-name,
  .chev {
    display: none;
  }
  .user-btn {
    padding: 0 4px;
  }
  .tabbar {
    position: fixed;
    left: 12px;
    right: 12px;
    bottom: 12px;
    z-index: 45;
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    align-items: end;
    padding: 8px 6px calc(8px + env(safe-area-inset-bottom));
    border-radius: 20px;
    background: rgba(22, 18, 48, 0.88);
    border: 1px solid var(--border);
    backdrop-filter: blur(18px);
    -webkit-backdrop-filter: blur(18px);
    box-shadow: var(--shadow);
  }
  .tab {
    display: grid;
    justify-items: center;
    gap: 3px;
    color: var(--text-dim);
    font-size: 11.5px;
    font-weight: 600;
    text-decoration: none;
  }
  .tab.router-link-exact-active {
    color: var(--text);
  }
  .tab-orb {
    display: grid;
    place-items: center;
    width: 48px;
    height: 48px;
    margin-top: -26px;
    border-radius: 50%;
    background: var(--grad-warm);
    color: #1a0f1f;
    box-shadow: var(--glow-warm);
  }
}
</style>
