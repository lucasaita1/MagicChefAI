<script setup>
import AppIcon from '@/components/AppIcon.vue'
import { computed, ref } from 'vue'
import { difficultyOf } from '@/utils/labels'
import { formatMinutes } from '@/utils/format'
import RobotChef from './RobotChef.vue'

const props = defineProps({
  recipe: { type: Object, required: true },
  compact: { type: Boolean, default: false }
})

const checked = ref(new Set())
const difficulty = computed(() => difficultyOf(props.recipe.difficulty))

function toggle(index) {
  const next = new Set(checked.value)
  next.has(index) ? next.delete(index) : next.add(index)
  checked.value = next
}
</script>

<template>
  <div class="recipe" :class="{ compact }">
    <header>
      <h2>{{ recipe.title }}</h2>
      <p v-if="recipe.summary" class="summary">{{ recipe.summary }}</p>
      <div class="meta">
        <span class="chip"><AppIcon name="clock" :size="14" /> {{ formatMinutes(recipe.prepTimeMinutes) }}</span>
        <span v-if="recipe.servings" class="chip"><AppIcon name="utensils" :size="14" /> {{ recipe.servings }} {{ recipe.servings > 1 ? 'porções' : 'porção' }}</span>
        <span v-if="difficulty" class="chip" :class="difficulty.tone">{{ difficulty.label }}</span>
      </div>
    </header>

    <blockquote v-if="recipe.chefComment" class="quote">
      <RobotChef :size="40" mood="happy" />
      <span>“{{ recipe.chefComment }}”</span>
    </blockquote>

    <div class="columns">
      <section>
        <h4><AppIcon name="basket" /> Ingredientes</h4>
        <ul class="ingredients">
          <li v-for="(item, index) in recipe.ingredients" :key="index" :class="{ done: checked.has(index) }" @click="toggle(index)">
            <span class="check" aria-hidden="true"><AppIcon v-if="checked.has(index)" name="check" :size="13" /></span>
            {{ item }}
          </li>
        </ul>
      </section>
      <section>
        <h4><AppIcon name="chef-hat" /> Modo de preparo</h4>
        <ol class="steps">
          <li v-for="(step, index) in recipe.steps" :key="index">
            <span class="num">{{ index + 1 }}</span>
            <p>{{ step }}</p>
          </li>
        </ol>
      </section>
    </div>

    <section v-if="recipe.tips?.length" class="tips">
      <h4><AppIcon name="lightbulb" /> Dicas do Chef</h4>
      <ul>
        <li v-for="(tip, index) in recipe.tips" :key="index">{{ tip }}</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.recipe {
  display: grid;
  gap: 20px;
}
h2 {
  font-size: 24px;
}
.compact h2 {
  font-size: 20px;
}
.summary {
  margin: 8px 0 12px;
  color: var(--text-soft);
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.quote {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  padding: 10px 16px 10px 10px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(255, 179, 71, 0.1), rgba(255, 94, 126, 0.08));
  border: 1px solid rgba(255, 179, 71, 0.2);
  font-style: italic;
  color: #ffe0b8;
}
.columns {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.3fr);
  gap: 24px;
}
.compact .columns {
  grid-template-columns: 1fr;
  gap: 18px;
}
h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 15px;
}
h4 :deep(svg) {
  color: var(--saffron);
}
ul,
ol {
  margin: 0;
  padding: 0;
  list-style: none;
}
.ingredients {
  display: grid;
  gap: 6px;
}
.ingredients li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--surface);
  cursor: pointer;
  transition: background 0.2s, opacity 0.2s;
}
.ingredients li:hover {
  background: var(--surface-strong);
}
.ingredients li.done {
  opacity: 0.5;
  text-decoration: line-through;
}
.check {
  display: grid;
  place-items: center;
  flex: none;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 1.5px solid var(--border-strong);
  color: #0c0a1d;
  font-size: 12px;
  font-weight: 800;
}
.done .check {
  background: var(--mint);
  border-color: var(--mint);
}
.steps {
  display: grid;
  gap: 12px;
}
.steps li {
  display: flex;
  gap: 12px;
}
.num {
  display: grid;
  place-items: center;
  flex: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--grad-warm);
  color: #1a0f1f;
  font: 700 13px var(--font-display);
}
.steps p {
  margin: 3px 0 0;
}
.tips ul {
  display: grid;
  gap: 6px;
}
.tips li {
  padding: 10px 14px;
  border-left: 3px solid var(--mint);
  border-radius: 0 10px 10px 0;
  background: rgba(60, 242, 208, 0.06);
  color: var(--text-soft);
}
@media (max-width: 720px) {
  .columns {
    grid-template-columns: 1fr;
  }
}
</style>
