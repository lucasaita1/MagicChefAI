<script setup>
import AppIcon from '@/components/AppIcon.vue'
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRecipesStore } from '@/stores/recipes'
import { usePantryStore } from '@/stores/pantry'
import RobotChef from '@/components/RobotChef.vue'
import RecipeCard from '@/components/RecipeCard.vue'
import RecipeDetail from '@/components/RecipeDetail.vue'
import BaseModal from '@/components/BaseModal.vue'

const auth = useAuthStore()
const recipes = useRecipesStore()
const pantry = usePantryStore()

const filter = ref('all')
const search = ref('')
const opened = ref(null)
const error = ref('')

const visible = computed(() => {
  const term = search.value.trim().toLowerCase()
  return (filter.value === 'favorites' ? recipes.favorites : recipes.sorted).filter(
    (recipe) => !term || recipe.title.toLowerCase().includes(term) || recipe.summary?.toLowerCase().includes(term)
  )
})

onMounted(async () => {
  try {
    await Promise.all([recipes.load(true), pantry.load()])
  } catch (e) {
    error.value = e.message
  }
})

async function toggleFavorite(recipe) {
  const updated = await recipes.toggleFavorite(recipe.id)
  if (opened.value?.id === recipe.id) opened.value = updated
}

async function remove(recipe) {
  if (!window.confirm(`Excluir "${recipe.title}"?`)) return
  await recipes.remove(recipe.id)
  opened.value = null
}
</script>

<template>
  <main class="container page">
    <section class="welcome glass">
      <div class="welcome-text">
        <span class="eyebrow">Sua cozinha inteligente</span>
        <h1>Olá, <span class="gradient-text">{{ auth.firstName }}</span>!</h1>
        <p>O que vamos cozinhar hoje? Conte seus ingredientes e o Chef Magic cria a receita.</p>
        <div class="stats">
          <div><strong>{{ recipes.items.length }}</strong><span>receitas</span></div>
          <div><strong>{{ recipes.favorites.length }}</strong><span>favoritas</span></div>
          <div><strong>{{ pantry.items.length }}</strong><span>na despensa</span></div>
        </div>
        <RouterLink :to="{ name: 'chef' }" class="btn btn-primary big"><AppIcon name="sparkles" /> Pedir outra receita</RouterLink>
      </div>
      <RobotChef mood="wave" :size="190" class="welcome-bot" />
    </section>

    <section class="toolbar">
      <h2>Minhas receitas</h2>
      <div class="controls">
        <div class="segmented" role="tablist">
          <button :class="{ active: filter === 'all' }" role="tab" :aria-selected="filter === 'all'" @click="filter = 'all'">Todas</button>
          <button :class="{ active: filter === 'favorites' }" role="tab" :aria-selected="filter === 'favorites'" @click="filter = 'favorites'"><AppIcon name="heart" :size="14" /> Favoritas</button>
        </div>
        <input v-model="search" class="input search" type="search" placeholder="Buscar receita..." aria-label="Buscar receita" />
      </div>
    </section>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="recipes.loading && !recipes.items.length" class="grid">
      <div v-for="n in 3" :key="n" class="skeleton glass" />
    </div>

    <TransitionGroup v-else-if="visible.length" name="fade-up" tag="div" class="grid">
      <RecipeCard v-for="recipe in visible" :key="recipe.id" :recipe="recipe" @open="opened = $event" @favorite="toggleFavorite" />
    </TransitionGroup>

    <div v-else class="empty glass">
      <RobotChef mood="thinking" :size="120" />
      <h3>{{ recipes.items.length ? 'Nada por aqui com esse filtro' : 'Seu livro de receitas está vazio' }}</h3>
      <p>{{ recipes.items.length ? 'Tente outra busca.' : 'Peça sua primeira receita ao Chef Magic!' }}</p>
      <RouterLink v-if="!recipes.items.length" :to="{ name: 'chef' }" class="btn btn-primary">Começar agora</RouterLink>
    </div>

    <BaseModal v-if="opened" :title="opened.title" wide @close="opened = null">
      <RecipeDetail :recipe="opened" />
      <div class="modal-actions">
        <button class="btn" @click="toggleFavorite(opened)"><AppIcon name="heart" :filled="opened.favorite" :size="16" /> {{ opened.favorite ? 'Favorita' : 'Favoritar' }}</button>
        <button class="btn btn-ghost btn-danger" @click="remove(opened)"><AppIcon name="trash" :size="16" /> Excluir</button>
      </div>
    </BaseModal>
  </main>
</template>

<style scoped>
.page {
  padding: 28px 0 60px;
  display: grid;
  gap: 28px;
}
.welcome {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 32px 36px;
  overflow: hidden;
}
.welcome::after {
  content: '';
  position: absolute;
  right: -80px;
  top: -80px;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 94, 126, 0.25), transparent 65%);
  pointer-events: none;
}
.welcome-text {
  display: grid;
  gap: 10px;
  max-width: 560px;
}
.welcome h1 {
  font-size: clamp(26px, 3.4vw, 36px);
  font-weight: 800;
}
.welcome p {
  margin: 0;
  color: var(--text-soft);
}
.stats {
  display: flex;
  gap: 28px;
  margin: 8px 0 6px;
}
.stats div {
  display: grid;
}
.stats strong {
  font: 700 24px var(--font-display);
}
.stats span {
  font-size: 13px;
  color: var(--text-dim);
}
.big {
  justify-self: start;
  min-height: 50px;
  padding: 0 24px;
}
.welcome-bot {
  position: relative;
  z-index: 1;
  flex: none;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.toolbar h2 {
  font-size: 22px;
}
.controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.segmented {
  display: flex;
  padding: 4px;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid var(--border);
}
.segmented button {
  height: 36px;
  padding: 0 14px;
  border: 0;
  border-radius: 9px;
  background: none;
  color: var(--text-dim);
  font-weight: 600;
  cursor: pointer;
}
.segmented button.active {
  background: var(--surface-strong);
  color: var(--text);
}
.search {
  width: 240px;
  min-height: 44px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.skeleton {
  height: 190px;
  border-radius: var(--radius);
  background: linear-gradient(100deg, var(--surface) 40%, var(--surface-strong) 50%, var(--surface) 60%);
  background-size: 200% 100%;
  animation: shimmer 1.4s linear infinite;
}
.empty {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 40px 20px;
  text-align: center;
}
.empty p {
  margin: 0 0 6px;
  color: var(--text-soft);
}
.error {
  color: var(--danger);
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

@keyframes shimmer {
  to { background-position: -200% 0; }
}

@media (max-width: 720px) {
  .welcome {
    padding: 24px;
  }
  .welcome-bot {
    display: none;
  }
  .search {
    width: 100%;
  }
  .controls {
    width: 100%;
  }
}
</style>
