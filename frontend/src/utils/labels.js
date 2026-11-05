export const CATEGORIES = {
  GRAIN: { label: 'Grãos', icon: 'wheat' },
  MEAT: { label: 'Carnes', icon: 'drumstick' },
  VEGETABLE: { label: 'Legumes', icon: 'carrot' },
  DAIRY: { label: 'Laticínios', icon: 'milk' },
  BAKERY: { label: 'Padaria', icon: 'bread' },
  PROTEIN: { label: 'Proteínas', icon: 'egg' },
  GREENS: { label: 'Verduras', icon: 'leaf' },
  FRUIT: { label: 'Frutas', icon: 'apple' },
  PASTA: { label: 'Massas', icon: 'noodles' },
  SWEETS: { label: 'Doces', icon: 'candy' },
  BEVERAGE: { label: 'Bebidas', icon: 'cup' },
  SPICE: { label: 'Temperos', icon: 'garlic' },
  CONDIMENT: { label: 'Condimentos', icon: 'jar' },
  FROZEN: { label: 'Congelados', icon: 'snowflake' },
  OTHER: { label: 'Outros', icon: 'utensils' }
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
  FRIDGE: { label: 'Minha geladeira', icon: 'fridge' },
  CUPBOARD: { label: 'Meu armário', icon: 'cupboard' }
}

export const DIFFICULTIES = {
  FACIL: { label: 'Fácil', tone: 'mint' },
  MEDIO: { label: 'Médio', tone: 'amber' },
  DIFICIL: { label: 'Difícil', tone: 'rose' }
}

export const categoryOf = (key) => CATEGORIES[key] ?? CATEGORIES.OTHER
export const unitOf = (key) => UNITS[key] ?? UNITS.UNIT
export const difficultyOf = (key) => DIFFICULTIES[key] ?? null
