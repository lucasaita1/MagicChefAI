<script setup>
import { computed } from 'vue'
import { LOCATIONS, categoryOf } from '@/utils/labels'
import { formatQuantity } from '@/utils/format'

const props = defineProps({
  items: { type: Array, required: true },
  selected: { type: Array, required: true }
})
const emit = defineEmits(['update:selected'])

const groups = computed(() =>
  Object.entries(LOCATIONS)
    .map(([key, meta]) => ({ key, ...meta, items: props.items.filter((item) => item.location === key) }))
    .filter((group) => group.items.length)
)

function toggle(id) {
  const next = props.selected.includes(id) ? props.selected.filter((value) => value !== id) : [...props.selected, id]
  emit('update:selected', next)
}

function selectGroup(group) {
  const ids = group.items.map((item) => item.id)
  emit('update:selected', [...new Set([...props.selected, ...ids])])
}
</script>

<template>
  <div class="picker">
    <p v-if="!items.length" class="empty">
      Sua despensa está vazia. Cadastre ingredientes em <RouterLink :to="{ name: 'pantry' }">Geladeira & Armário</RouterLink>.
    </p>
    <section v-for="group in groups" :key="group.key" class="group">
      <header>
        <h4>{{ group.emoji }} {{ group.label }}</h4>
        <button type="button" class="link" @click="selectGroup(group)">Selecionar tudo</button>
      </header>
      <div class="items">
        <button
          v-for="item in group.items"
          :key="item.id"
          type="button"
          class="item"
          :class="{ on: selected.includes(item.id) }"
          :aria-pressed="selected.includes(item.id)"
          @click="toggle(item.id)"
        >
          <span>{{ categoryOf(item.category).emoji }}</span>
          <span class="name">{{ item.name }}</span>
          <small>{{ formatQuantity(item.quantity, item.unit) }}</small>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.picker {
  display: grid;
  gap: 16px;
}
.empty {
  margin: 0;
  color: var(--text-soft);
}
.group header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.group h4 {
  font-size: 14px;
}
.link {
  border: 0;
  background: none;
  color: var(--mint);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 99px;
  border: 1px solid var(--border);
  background: var(--surface);
  cursor: pointer;
  transition: all 0.2s var(--ease-out);
}
.item small {
  color: var(--text-dim);
}
.item.on {
  border-color: rgba(255, 179, 71, 0.6);
  background: rgba(255, 179, 71, 0.12);
  transform: translateY(-1px);
}
</style>
