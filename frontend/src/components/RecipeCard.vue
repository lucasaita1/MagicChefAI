<script setup>
import AppIcon from '@/components/AppIcon.vue'
import { computed } from 'vue'
import { difficultyOf } from '@/utils/labels'
import { formatDate, formatMinutes } from '@/utils/format'

const props = defineProps({ recipe: { type: Object, required: true } })
const emit = defineEmits(['open', 'favorite'])

const difficulty = computed(() => difficultyOf(props.recipe.difficulty))
</script>

<template>
  <article class="card glass" tabindex="0" @click="emit('open', recipe)" @keydown.enter="emit('open', recipe)">
    <div class="top">
      <span class="date">{{ formatDate(recipe.createdAt) }}</span>
      <button
        class="fav"
        :class="{ on: recipe.favorite }"
        :aria-label="recipe.favorite ? 'Remover dos favoritos' : 'Adicionar aos favoritos'"
        @click.stop="emit('favorite', recipe)"
      >
        <AppIcon name="heart" :size="17" :filled="recipe.favorite" />
      </button>
    </div>
    <h3>{{ recipe.title }}</h3>
    <p>{{ recipe.summary }}</p>
    <div class="meta">
      <span class="chip"><AppIcon name="clock" :size="14" /> {{ formatMinutes(recipe.prepTimeMinutes) }}</span>
      <span v-if="recipe.servings" class="chip"><AppIcon name="utensils" :size="14" /> {{ recipe.servings }} {{ recipe.servings > 1 ? 'porções' : 'porção' }}</span>
      <span v-if="difficulty" class="chip" :class="difficulty.tone">{{ difficulty.label }}</span>
    </div>
  </article>
</template>

<style scoped>
.card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  border-radius: var(--radius);
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.3s var(--ease-out), border-color 0.3s, box-shadow 0.3s;
}
.card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 3px;
  background: var(--grad-warm);
  opacity: 0;
  transition: opacity 0.3s;
}
.card:hover {
  transform: translateY(-4px);
  border-color: var(--border-strong);
  box-shadow: 0 24px 50px -24px rgba(255, 94, 126, 0.45);
}
.card:hover::before {
  opacity: 1;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.date {
  font-size: 12px;
  color: var(--text-dim);
}
.fav {
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 50%;
  background: var(--surface);
  color: var(--text-soft);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: transform 0.25s var(--ease-spring), color 0.2s, background 0.2s;
}
.fav:hover {
  transform: scale(1.15);
}
.fav.on {
  color: var(--coral);
  background: rgba(255, 94, 126, 0.14);
}
h3 {
  font-size: 18px;
}
p {
  margin: 0;
  color: var(--text-soft);
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: auto;
  padding-top: 6px;
}
</style>
