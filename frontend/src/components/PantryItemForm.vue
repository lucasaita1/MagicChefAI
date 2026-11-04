<script setup>
import { reactive, ref } from 'vue'
import { CATEGORIES, LOCATIONS, UNITS } from '@/utils/labels'

const props = defineProps({
  item: { type: Object, default: null },
  defaultLocation: { type: String, default: 'FRIDGE' },
  submit: { type: Function, required: true }
})
const emit = defineEmits(['done'])

const form = reactive({
  name: props.item?.name ?? '',
  category: props.item?.category ?? 'VEGETABLE',
  location: props.item?.location ?? props.defaultLocation,
  quantity: props.item?.quantity ?? 1,
  unit: props.item?.unit ?? 'UNIT',
  expiresAt: props.item?.expiresAt ?? ''
})
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

async function save() {
  saving.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    await props.submit({ ...form, quantity: Number(form.quantity), expiresAt: form.expiresAt || null })
    emit('done')
  } catch (e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <form class="form" novalidate @submit.prevent="save">
    <div class="field">
      <label for="item-name">Ingrediente</label>
      <input id="item-name" v-model.trim="form.name" class="input" placeholder="Ex.: Tomate italiano" maxlength="80" required />
      <span v-if="fieldErrors.name" class="field-error">{{ fieldErrors.name }}</span>
    </div>

    <div class="location" role="radiogroup" aria-label="Onde fica">
      <label v-for="(meta, key) in LOCATIONS" :key="key" class="loc" :class="{ active: form.location === key }">
        <input v-model="form.location" type="radio" name="location" :value="key" class="sr-only" />
        <span>{{ meta.emoji }}</span> {{ meta.label }}
      </label>
    </div>

    <div class="row">
      <div class="field">
        <label for="item-category">Categoria</label>
        <select id="item-category" v-model="form.category" class="input">
          <option v-for="(meta, key) in CATEGORIES" :key="key" :value="key">{{ meta.emoji }} {{ meta.label }}</option>
        </select>
      </div>
      <div class="field">
        <label for="item-expires">Validade (opcional)</label>
        <input id="item-expires" v-model="form.expiresAt" type="date" class="input" />
      </div>
    </div>

    <div class="row">
      <div class="field">
        <label for="item-quantity">Quantidade</label>
        <input id="item-quantity" v-model="form.quantity" type="number" min="0.01" step="0.01" class="input" required />
        <span v-if="fieldErrors.quantity" class="field-error">{{ fieldErrors.quantity }}</span>
      </div>
      <div class="field">
        <label for="item-unit">Unidade</label>
        <select id="item-unit" v-model="form.unit" class="input">
          <option v-for="(meta, key) in UNITS" :key="key" :value="key">{{ meta.label }}</option>
        </select>
      </div>
    </div>

    <p v-if="error" class="field-error" role="alert">{{ error }}</p>

    <button class="btn btn-primary" type="submit" :disabled="saving">
      <span v-if="saving" class="spinner" />
      {{ item ? 'Salvar alterações' : 'Guardar ingrediente' }}
    </button>
  </form>
</template>

<style scoped>
.form {
  display: grid;
  gap: 16px;
}
.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.location {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.loc {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 46px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text-soft);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.loc.active {
  color: var(--text);
  border-color: rgba(60, 242, 208, 0.5);
  background: rgba(60, 242, 208, 0.08);
  box-shadow: 0 0 0 4px rgba(60, 242, 208, 0.08);
}
@media (max-width: 480px) {
  .row {
    grid-template-columns: 1fr;
  }
}
</style>
