<script setup>
import { onBeforeUnmount, onMounted } from 'vue'

defineProps({
  title: { type: String, default: '' },
  wide: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])

function onKey(event) {
  if (event.key === 'Escape') emit('close')
}
onMounted(() => {
  document.addEventListener('keydown', onKey)
  document.body.style.overflow = 'hidden'
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div class="backdrop" @click.self="emit('close')">
      <div class="modal glass" :class="{ wide }" role="dialog" aria-modal="true" :aria-label="title">
        <header class="modal-head">
          <h3>{{ title }}</h3>
          <button class="btn btn-ghost btn-icon" aria-label="Fechar" @click="emit('close')">✕</button>
        </header>
        <div class="modal-body">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(6, 4, 16, 0.65);
  backdrop-filter: blur(6px);
  animation: fade 0.25s var(--ease-out);
}
.modal {
  width: min(520px, 100%);
  max-height: calc(100vh - 32px);
  display: flex;
  flex-direction: column;
  background: rgba(22, 18, 48, 0.92);
  animation: pop 0.35s var(--ease-spring);
}
.modal.wide {
  width: min(860px, 100%);
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 0;
}
.modal-head h3 {
  font-size: 18px;
}
.modal-body {
  padding: 18px 20px 22px;
  overflow-y: auto;
}
@keyframes fade {
  from { opacity: 0; }
}
@keyframes pop {
  from { opacity: 0; transform: translateY(16px) scale(0.97); }
}
</style>
