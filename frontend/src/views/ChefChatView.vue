<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePantryStore } from '@/stores/pantry'
import { useRecipesStore } from '@/stores/recipes'
import RobotChef from '@/components/RobotChef.vue'
import RecipeDetail from '@/components/RecipeDetail.vue'
import PantryPicker from '@/components/PantryPicker.vue'
import { categoryOf } from '@/utils/labels'

const MAX_LENGTH = 500
const COOKING_STEPS = [
  'Lendo seus ingredientes...',
  'Picando a cebola...',
  'Aquecendo a frigideira...',
  'Provando o tempero...',
  'Empratando com carinho...'
]
const SUGGESTIONS = ['🥚 Ovos, queijo e tomate', '🍝 Macarrão, alho e azeite', '🥗 Algo leve com frango e salada']
const DEFAULT_MESSAGE = 'Quero uma receita com os itens que selecionei da minha despensa.'

const auth = useAuthStore()
const pantry = usePantryStore()
const recipes = useRecipesStore()
const route = useRoute()

const messages = ref([])
const input = ref('')
const selectedIds = ref([])
const pickerOpen = ref(false)
const loading = ref(false)
const mood = ref('wave')
const cookingStep = ref(0)
const thread = ref(null)
let nextId = 1
let cookingTimer = null

const selectedItems = computed(() => pantry.items.filter((item) => selectedIds.value.includes(item.id)))
const locked = computed(() => !auth.isVerified)
const canSend = computed(() => !locked.value && !loading.value && (input.value.trim().length > 0 || selectedIds.value.length > 0))

function push(message) {
  messages.value.push({ id: nextId++, ...message })
  nextTick(() => thread.value?.scrollTo({ top: thread.value.scrollHeight, behavior: 'smooth' }))
}

function chefSays(text, extra = {}) {
  push({ from: 'chef', type: 'text', text, ...extra })
}

onMounted(async () => {
  chefSays(
    locked.value
      ? `Olá, ${auth.firstName || 'chef'}! 👨‍🍳 Antes de cozinhar, confirme seu e-mail pelo link que enviei para ${auth.user?.email}. Assim que confirmar, é só voltar aqui!`
      : `Olá, ${auth.firstName || 'chef'}! 👨‍🍳 Eu sou o Chef Magic. Quais ingredientes você tem aí? Pode digitar ou carregar da sua geladeira e armário.`
  )
  setTimeout(() => {
    if (mood.value === 'wave') mood.value = 'idle'
  }, 2500)
  try {
    await pantry.load()
    const preselected = String(route.query.items ?? '')
      .split(',')
      .map(Number)
      .filter((id) => pantry.items.some((item) => item.id === id))
    if (preselected.length) selectedIds.value = preselected
  } catch {
    // The chat still works by typing ingredients
  }
})

onBeforeUnmount(() => clearInterval(cookingTimer))

function useSuggestion(text) {
  input.value = text.replace(/^\S+\s/, '')
}

function removeSelected(id) {
  selectedIds.value = selectedIds.value.filter((value) => value !== id)
}

async function send() {
  if (!canSend.value) return
  const message = input.value.trim() || DEFAULT_MESSAGE
  const ids = [...selectedIds.value]
  push({ from: 'user', type: 'text', text: message, items: selectedItems.value.map((item) => item.name) })
  input.value = ''
  selectedIds.value = []
  pickerOpen.value = false

  loading.value = true
  mood.value = 'thinking'
  cookingStep.value = 0
  cookingTimer = setInterval(() => {
    cookingStep.value = (cookingStep.value + 1) % COOKING_STEPS.length
  }, 1600)

  try {
    const recipe = await recipes.generate(message, ids)
    mood.value = 'happy'
    chefSays(recipe.chefComment || 'Prontinho! Olha só o que eu preparei:')
    push({ from: 'chef', type: 'recipe', recipe })
    chefSays('Salvei essa receita em “Minhas receitas”. Quer outra? É só me mandar mais ingredientes! 🍳')
  } catch (e) {
    mood.value = 'confused'
    selectedIds.value = ids
    chefSays(e.message, { error: true })
  } finally {
    clearInterval(cookingTimer)
    loading.value = false
  }
}

function onKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    send()
  }
}
</script>

<template>
  <main class="chat-page container">
    <aside class="side glass">
      <RobotChef :mood="mood" :size="170" />
      <h2>Chef Magic</h2>
      <p class="status" :class="{ busy: loading }">
        <span class="dot" />
        {{ loading ? COOKING_STEPS[cookingStep] : 'Online e com fome de criar' }}
      </p>
      <p class="hint">Eu só falo de culinária! Envie ingredientes, restrições (vegetariano, sem lactose...) ou o tipo de prato que deseja.</p>
    </aside>

    <section class="chat glass">
      <div ref="thread" class="thread" aria-live="polite">
        <TransitionGroup name="fade-up">
          <div v-for="message in messages" :key="message.id" class="msg" :class="[message.from, { wide: message.type === 'recipe' }]">
            <div v-if="message.from === 'chef'" class="avatar"><RobotChef :size="30" mood="idle" /></div>
            <div class="bubble" :class="{ error: message.error, recipe: message.type === 'recipe' }">
              <RecipeDetail v-if="message.type === 'recipe'" :recipe="message.recipe" compact />
              <template v-else>
                <p>{{ message.text }}</p>
                <div v-if="message.items?.length" class="mini-chips">
                  <span v-for="name in message.items" :key="name" class="chip">{{ name }}</span>
                </div>
              </template>
            </div>
          </div>
        </TransitionGroup>

        <div v-if="loading" class="msg chef">
          <div class="avatar"><RobotChef :size="30" mood="thinking" /></div>
          <div class="bubble typing" aria-label="O Chef está cozinhando">
            <span /><span /><span />
            <em>{{ COOKING_STEPS[cookingStep] }}</em>
          </div>
        </div>
      </div>

      <div v-if="messages.length <= 1 && !loading && !locked" class="suggestions">
        <button v-for="suggestion in SUGGESTIONS" :key="suggestion" class="chip suggestion" @click="useSuggestion(suggestion)">
          {{ suggestion }}
        </button>
      </div>

      <Transition name="fade-up">
        <div v-if="pickerOpen" class="picker-panel">
          <PantryPicker v-model:selected="selectedIds" :items="pantry.items" />
        </div>
      </Transition>

      <div v-if="locked" class="locked">
        <span>🔒</span>
        <p>O Chef está esperando a confirmação do seu e-mail. Use o aviso no topo da página para reenviar o link.</p>
      </div>

      <footer v-else class="composer">
        <div v-if="selectedItems.length" class="selected">
          <span v-for="item in selectedItems" :key="item.id" class="chip amber">
            {{ categoryOf(item.category).emoji }} {{ item.name }}
            <button class="x" :aria-label="`Remover ${item.name}`" @click="removeSelected(item.id)">×</button>
          </span>
        </div>
        <div class="input-row">
          <button
            class="btn btn-icon pantry-btn"
            :class="{ on: pickerOpen }"
            title="Carregar da geladeira e armário"
            aria-label="Carregar da geladeira e armário"
            @click="pickerOpen = !pickerOpen"
          >
            🧊
          </button>
          <textarea
            v-model="input"
            class="input message"
            rows="1"
            :maxlength="MAX_LENGTH"
            placeholder="Ex.: tenho frango, batata e alho. Quero algo para o jantar..."
            aria-label="Mensagem para o Chef"
            :disabled="loading"
            @keydown="onKeydown"
          />
          <button class="btn btn-primary send" :disabled="!canSend" aria-label="Enviar" @click="send">
            <span v-if="loading" class="spinner" />
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2 11 13" /><path d="m22 2-7 20-4-9-9-4z" /></svg>
          </button>
        </div>
        <small class="counter">{{ input.length }}/{{ MAX_LENGTH }}</small>
      </footer>
    </section>
  </main>
</template>

<style scoped>
.chat-page {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  padding: 24px 0;
  height: calc(100vh - 68px);
  min-height: 560px;
}
.side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 28px 22px;
  text-align: center;
}
.side h2 {
  font-size: 22px;
}
.status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 6px 12px;
  border-radius: 99px;
  background: rgba(60, 242, 208, 0.08);
  color: var(--mint);
  font-size: 13px;
  font-weight: 500;
}
.status.busy {
  background: rgba(255, 179, 71, 0.1);
  color: var(--saffron);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 10px currentColor;
  animation: blink 1.6s infinite;
}
.hint {
  margin: auto 0 0;
  font-size: 13px;
  color: var(--text-dim);
}

.chat {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.thread {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px;
  overflow-y: auto;
}
.msg {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 78%;
}
.msg.wide {
  max-width: 100%;
}
.msg.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.avatar {
  flex: none;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--surface-strong);
  border: 1px solid var(--border);
}
.bubble {
  padding: 12px 16px;
  border-radius: 18px 18px 18px 6px;
  background: var(--surface-strong);
  border: 1px solid var(--border);
}
.bubble p {
  margin: 0;
  white-space: pre-wrap;
}
.user .bubble {
  border-radius: 18px 18px 6px 18px;
  background: linear-gradient(135deg, rgba(255, 179, 71, 0.22), rgba(255, 94, 126, 0.22));
  border-color: rgba(255, 140, 110, 0.35);
}
.bubble.error {
  background: rgba(255, 107, 129, 0.1);
  border-color: rgba(255, 107, 129, 0.35);
  color: #ffd0d8;
}
.bubble.recipe {
  flex: 1;
  padding: 22px;
  background: rgba(16, 13, 38, 0.75);
  border-color: rgba(255, 179, 71, 0.25);
  box-shadow: 0 20px 50px -30px rgba(255, 94, 126, 0.6);
}
.mini-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.typing {
  display: flex;
  align-items: center;
  gap: 5px;
}
.typing span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--saffron);
  animation: typing 1s infinite;
}
.typing span:nth-child(2) { animation-delay: 0.15s; }
.typing span:nth-child(3) { animation-delay: 0.3s; }
.typing em {
  margin-left: 8px;
  font-size: 13px;
  color: var(--text-soft);
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 24px 12px;
}
.suggestion {
  cursor: pointer;
  transition: all 0.2s;
}
.suggestion:hover {
  color: var(--text);
  border-color: var(--border-strong);
  transform: translateY(-1px);
}
.locked {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 16px 16px;
  padding: 14px 16px;
  border-radius: var(--radius);
  background: rgba(255, 179, 71, 0.08);
  border: 1px dashed rgba(255, 179, 71, 0.4);
  color: var(--text-soft);
  font-size: 14px;
}
.locked p {
  margin: 0;
}
.picker-panel {
  max-height: 240px;
  overflow-y: auto;
  margin: 0 16px 10px;
  padding: 16px;
  border-radius: var(--radius);
  background: rgba(10, 8, 26, 0.6);
  border: 1px solid var(--border);
}
.composer {
  padding: 14px 16px 10px;
  border-top: 1px solid var(--border);
  background: rgba(12, 10, 29, 0.4);
}
.selected {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.x {
  border: 0;
  background: none;
  color: inherit;
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
}
.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}
.pantry-btn {
  width: 48px;
  min-height: 48px;
  font-size: 18px;
}
.pantry-btn.on {
  border-color: rgba(60, 242, 208, 0.5);
  background: rgba(60, 242, 208, 0.1);
}
.message {
  flex: 1;
  min-height: 48px;
  max-height: 140px;
  padding: 13px 14px;
  resize: none;
  field-sizing: content;
}
.send {
  width: 52px;
  min-height: 48px;
  padding: 0;
}
.counter {
  display: block;
  margin-top: 6px;
  text-align: right;
  font-size: 11.5px;
  color: var(--text-dim);
}

@keyframes typing {
  0%, 100% { transform: translateY(0); opacity: 0.5; }
  50% { transform: translateY(-5px); opacity: 1; }
}
@keyframes blink {
  50% { opacity: 0.35; }
}

@media (max-width: 900px) {
  .chat-page {
    grid-template-columns: 1fr;
    height: calc(100dvh - 68px - 92px);
    padding: 12px 0;
  }
  .side {
    display: none;
  }
  .msg {
    max-width: 92%;
  }
  .thread {
    padding: 16px;
  }
  .bubble.recipe {
    padding: 16px;
  }
}
</style>
