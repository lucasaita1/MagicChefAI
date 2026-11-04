<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePantryStore } from '@/stores/pantry'
import BaseModal from '@/components/BaseModal.vue'
import PantryItemForm from '@/components/PantryItemForm.vue'
import RobotChef from '@/components/RobotChef.vue'
import { LOCATIONS, categoryOf } from '@/utils/labels'
import { daysUntil, formatQuantity } from '@/utils/format'

const pantry = usePantryStore()
const router = useRouter()

const modal = ref(null)
const selected = ref([])
const error = ref('')

const columns = computed(() => [
  { key: 'FRIDGE', ...LOCATIONS.FRIDGE, items: pantry.fridge },
  { key: 'CUPBOARD', ...LOCATIONS.CUPBOARD, items: pantry.cupboard }
])

onMounted(async () => {
  try {
    await pantry.load(true)
  } catch (e) {
    error.value = e.message
  }
})

function expiry(item) {
  const days = daysUntil(item.expiresAt)
  if (days === null) return null
  if (days < 0) return { label: 'Vencido', tone: 'rose' }
  if (days === 0) return { label: 'Vence hoje', tone: 'rose' }
  if (days <= 3) return { label: `Vence em ${days}d`, tone: 'amber' }
  return null
}

function toggle(id) {
  selected.value = selected.value.includes(id) ? selected.value.filter((value) => value !== id) : [...selected.value, id]
}

function openCreate(location) {
  modal.value = { location, item: null }
}

function openEdit(item) {
  modal.value = { location: item.location, item }
}

function submitForm(payload) {
  return modal.value.item ? pantry.update(modal.value.item.id, payload) : pantry.add(payload)
}

async function remove(item) {
  if (!window.confirm(`Remover ${item.name}?`)) return
  try {
    await pantry.remove(item.id)
    selected.value = selected.value.filter((id) => id !== item.id)
  } catch (e) {
    error.value = e.message
  }
}

function cook() {
  router.push({ name: 'chef', query: { items: selected.value.join(',') } })
}
</script>

<template>
  <main class="container page">
    <header class="head">
      <div>
        <span class="eyebrow">Sua cozinha</span>
        <h1>Geladeira <span class="gradient-text">&</span> Armário</h1>
        <p>Cadastre o que você tem em casa e leve direto para o Chef Magic. Toque nos itens para selecionar.</p>
      </div>
      <RobotChef mood="idle" :size="110" class="head-bot" />
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="columns">
      <section v-for="column in columns" :key="column.key" class="column glass" :class="column.key.toLowerCase()">
        <header class="column-head">
          <h2><span class="emoji">{{ column.emoji }}</span> {{ column.label }}</h2>
          <span class="count">{{ column.items.length }}</span>
          <button class="btn add" @click="openCreate(column.key)">＋ Adicionar</button>
        </header>

        <TransitionGroup v-if="column.items.length" name="fade-up" tag="ul" class="items">
          <li
            v-for="item in column.items"
            :key="item.id"
            class="item"
            :class="{ on: selected.includes(item.id) }"
            tabindex="0"
            :aria-pressed="selected.includes(item.id)"
            @click="toggle(item.id)"
            @keydown.enter="toggle(item.id)"
          >
            <span class="cat">{{ categoryOf(item.category).emoji }}</span>
            <div class="info">
              <strong>{{ item.name }}</strong>
              <small>{{ formatQuantity(item.quantity, item.unit) }} · {{ categoryOf(item.category).label }}</small>
            </div>
            <span v-if="expiry(item)" class="chip" :class="expiry(item).tone">{{ expiry(item).label }}</span>
            <div class="item-actions">
              <button class="btn btn-ghost btn-icon" :aria-label="`Editar ${item.name}`" @click.stop="openEdit(item)">✎</button>
              <button class="btn btn-ghost btn-icon btn-danger" :aria-label="`Remover ${item.name}`" @click.stop="remove(item)">🗑</button>
            </div>
          </li>
        </TransitionGroup>

        <div v-else class="empty">
          <p>{{ column.key === 'FRIDGE' ? 'Geladeira vazia... que tal cadastrar ovos, leite ou legumes?' : 'Armário vazio... arroz, massas e temperos moram aqui.' }}</p>
        </div>
      </section>
    </div>

    <Transition name="fade-up">
      <div v-if="selected.length" class="cook-bar glass">
        <span><strong>{{ selected.length }}</strong> {{ selected.length > 1 ? 'itens selecionados' : 'item selecionado' }}</span>
        <button class="btn btn-ghost" @click="selected = []">Limpar</button>
        <button class="btn btn-primary" @click="cook">👨‍🍳 Cozinhar com o Chef</button>
      </div>
    </Transition>

    <BaseModal v-if="modal" :title="modal.item ? 'Editar ingrediente' : 'Novo ingrediente'" @close="modal = null">
      <PantryItemForm :item="modal.item" :default-location="modal.location" :submit="submitForm" @done="modal = null" />
    </BaseModal>
  </main>
</template>

<style scoped>
.page {
  display: grid;
  gap: 24px;
  padding: 28px 0 120px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.head h1 {
  margin: 6px 0 8px;
  font-size: clamp(26px, 3.4vw, 36px);
  font-weight: 800;
}
.head p {
  margin: 0;
  color: var(--text-soft);
}
.columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  align-items: start;
}
.column {
  padding: 20px;
  border-top: 3px solid transparent;
}
.column.fridge {
  border-top-color: rgba(60, 242, 208, 0.6);
}
.column.cupboard {
  border-top-color: rgba(255, 179, 71, 0.6);
}
.column-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.column-head h2 {
  font-size: 18px;
}
.count {
  padding: 2px 9px;
  border-radius: 99px;
  background: var(--surface-strong);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-soft);
}
.add {
  margin-left: auto;
  min-height: 38px;
  font-size: 13.5px;
}
.items {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 10px 10px 12px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: rgba(10, 8, 26, 0.35);
  cursor: pointer;
  transition: all 0.2s var(--ease-out);
}
.item:hover {
  border-color: var(--border-strong);
}
.item.on {
  border-color: rgba(255, 179, 71, 0.6);
  background: rgba(255, 179, 71, 0.1);
}
.cat {
  display: grid;
  place-items: center;
  flex: none;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--surface-strong);
  font-size: 20px;
}
.info {
  display: grid;
  min-width: 0;
  flex: 1;
}
.info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.info small {
  color: var(--text-dim);
}
.item-actions {
  display: flex;
  opacity: 0.6;
  transition: opacity 0.2s;
}
.item:hover .item-actions {
  opacity: 1;
}
.empty p {
  margin: 0;
  padding: 18px;
  border: 1px dashed var(--border-strong);
  border-radius: 14px;
  color: var(--text-dim);
  text-align: center;
}
.error {
  color: var(--danger);
}
.cook-bar {
  position: fixed;
  left: 50%;
  bottom: 24px;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 10px 10px 20px;
  transform: translateX(-50%);
  border-radius: 18px;
  background: rgba(22, 18, 48, 0.9);
}
@media (max-width: 820px) {
  .columns {
    grid-template-columns: 1fr;
  }
  .head-bot {
    display: none;
  }
  .cook-bar {
    width: calc(100% - 32px);
    justify-content: space-between;
  }
}
</style>
