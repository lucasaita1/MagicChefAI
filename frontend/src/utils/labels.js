export const CATEGORIES = {
  GRAIN: { label: 'Grãos', emoji: '🌾' },
  MEAT: { label: 'Carnes', emoji: '🥩' },
  VEGETABLE: { label: 'Legumes', emoji: '🥕' },
  DAIRY: { label: 'Laticínios', emoji: '🧀' },
  BAKERY: { label: 'Padaria', emoji: '🥖' },
  PROTEIN: { label: 'Proteínas', emoji: '🥚' },
  GREENS: { label: 'Verduras', emoji: '🥬' },
  FRUIT: { label: 'Frutas', emoji: '🍋' },
  PASTA: { label: 'Massas', emoji: '🍝' },
  SWEETS: { label: 'Doces', emoji: '🍫' },
  BEVERAGE: { label: 'Bebidas', emoji: '🧃' },
  SPICE: { label: 'Temperos', emoji: '🧄' },
  CONDIMENT: { label: 'Condimentos', emoji: '🫙' },
  FROZEN: { label: 'Congelados', emoji: '🧊' },
  OTHER: { label: 'Outros', emoji: '🍽️' }
}

export const UNITS = {
  UNIT: { label: 'unidade(s)', short: 'un' },
  GRAM: { label: 'gramas', short: 'g' },
  KILOGRAM: { label: 'quilos', short: 'kg' },
  MILLILITER: { label: 'mililitros', short: 'ml' },
  LITER: { label: 'litros', short: 'L' },
  PACKAGE: { label: 'pacote(s)', short: 'pct' }
}

export const LOCATIONS = {
  FRIDGE: { label: 'Minha geladeira', emoji: '🧊' },
  CUPBOARD: { label: 'Meu armário', emoji: '🗄️' }
}

export const DIFFICULTIES = {
  FACIL: { label: 'Fácil', tone: 'mint' },
  MEDIO: { label: 'Médio', tone: 'amber' },
  DIFICIL: { label: 'Difícil', tone: 'rose' }
}

export const categoryOf = (key) => CATEGORIES[key] ?? CATEGORIES.OTHER
export const unitOf = (key) => UNITS[key] ?? UNITS.UNIT
export const difficultyOf = (key) => DIFFICULTIES[key] ?? null
