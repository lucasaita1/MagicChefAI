<script setup>
import { computed } from 'vue'

/**
 * Line icon set drawn on a 24px grid with a single stroke style, so every icon in the app
 * (UI actions and food categories) shares the same visual language. No emoji anywhere.
 */
const p = (d) => ({ tag: 'path', attrs: { d } })
const circle = (cx, cy, r) => ({ tag: 'circle', attrs: { cx, cy, r } })
const rect = (x, y, width, height, rx) => ({ tag: 'rect', attrs: { x, y, width, height, rx } })

const ICONS = {
  // UI
  book: [p('M4 19.5V5a2 2 0 0 1 2-2h13v15H6.5A2.5 2.5 0 0 0 4 20.5 2.5 2.5 0 0 0 6.5 23H19'), p('M9 7h6M9 11h4')],
  fridge: [rect(5, 2, 14, 20, 2.5), p('M5 10h14M9 5.5v2M9 13v3')],
  cupboard: [rect(3, 3, 18, 18, 2.5), p('M12 3v18M9.5 11v2M14.5 11v2')],
  sparkles: [p('M12 3l1.8 4.7L18.5 9.5l-4.7 1.8L12 16l-1.8-4.7L5.5 9.5l4.7-1.8z'), p('M19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z')],
  logout: [p('M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4'), p('m16 17 5-5-5-5M21 12H9')],
  'chevron-down': [p('m6 9 6 6 6-6')],
  mail: [rect(3, 5, 18, 14, 2.5), p('m3.5 6.5 8.5 6.5 8.5-6.5')],
  eye: [p('M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z'), circle(12, 12, 3)],
  'eye-off': [p('M9.9 4.2A9.6 9.6 0 0 1 12 4c6.5 0 10 8 10 8a17 17 0 0 1-2.2 3.2M6.6 6.6A17 17 0 0 0 2 12s3.5 7 10 7a9.7 9.7 0 0 0 5.4-1.6'), p('M9.9 9.9a3 3 0 0 0 4.2 4.2'), p('m2 2 20 20')],
  lock: [rect(4, 11, 16, 10, 2.5), p('M8 11V7a4 4 0 0 1 8 0v4')],
  plus: [p('M12 5v14M5 12h14')],
  edit: [p('M12 20h9'), p('M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z')],
  trash: [p('M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v6M14 11v6')],
  heart: [p('M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8z')],
  clock: [circle(12, 12, 9), p('M12 7v5l3 2')],
  utensils: [p('M5 2v8a2 2 0 0 0 4 0V2M7 12v10M18 2c-2 1-3 4-3 7h3v13')],
  basket: [p('M5 10h14l-1.5 9a2 2 0 0 1-2 1.7h-7a2 2 0 0 1-2-1.7z'), p('m9 10 3-6 3 6M3 10h18')],
  'chef-hat': [p('M6 13a4 4 0 0 1-.7-7.9A5 5 0 0 1 12 3a5 5 0 0 1 6.7 2.1A4 4 0 0 1 18 13v7H6z'), p('M6 17h12')],
  lightbulb: [p('M9 18h6M10 22h4'), p('M12 2a7 7 0 0 0-4 12.7c.6.5 1 1.3 1 2.3h6c0-1 .4-1.8 1-2.3A7 7 0 0 0 12 2z')],
  send: [p('M22 2 11 13'), p('m22 2-7 20-4-9-9-4z')],
  check: [p('m5 12 5 5 9-10')],
  x: [p('M18 6 6 18M6 6l12 12')],
  search: [circle(11, 11, 7), p('m20 20-4-4')],
  // Food categories
  wheat: [p('M12 22V9'), p('M12 9c-1.5-1.5-1.5-4 0-6 1.5 2 1.5 4.5 0 6z'), p('M12 14c-2 0-4-1.5-4-3.5 2 0 4 1.5 4 3.5zM12 14c2 0 4-1.5 4-3.5-2 0-4 1.5-4 3.5zM12 19c-2 0-4-1.5-4-3.5 2 0 4 1.5 4 3.5zM12 19c2 0 4-1.5 4-3.5-2 0-4 1.5-4 3.5z')],
  drumstick: [p('M14 3a6 6 0 0 1 6 6 6 6 0 0 1-6 6c-1 0-2-.2-2.8-.7L7.5 18a2 2 0 1 1-2.6 2.1A2 2 0 1 1 7 17.5l3.7-3.7A6 6 0 0 1 14 3z')],
  carrot: [p('M3 21s4-1 9-6l3.5-3.5a3.5 3.5 0 0 0-5-5L7 10c-5 5-4 11-4 11z'), p('M10 9l1.5 1.5M7.5 13 9 14.5'), p('M15.5 6.5S16 3 18 2c.5 2-1 4-1 4s2-1.5 4-1c-1 2-3.5 2.5-3.5 2.5')],
  milk: [p('M8 2h8M9 2v3L6 9v11a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V9l-3-4V2'), p('M6 13h12')],
  bread: [p('M5 11a4 4 0 0 1 1-7.8h12A4 4 0 0 1 19 11v8a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2z'), p('M9 9v3M15 9v3')],
  egg: [p('M12 22c4 0 7-3 7-7.5S15.5 2 12 2 5 10 5 14.5 8 22 12 22z')],
  leaf: [p('M11 20A7 7 0 0 1 4 13c0-6 6-10 16-10 0 10-4 17-9 17z'), p('M4 21c3-6 7-9 11-11')],
  apple: [p('M12 7.5c-2-1.2-6-1.2-7 2.8C3.8 14.8 6.5 21 9.5 21c1 0 1.5-.5 2.5-.5s1.5.5 2.5.5c3 0 5.7-6.2 4.5-10.7-1-4-5-4-7-2.8z'), p('M12 7.5c0-2 1-4 3-5')],
  noodles: [p('M3 11h18a9 9 0 0 1-18 0z'), p('M8 11c0-3 1-5 .5-8M12 11c0-3 1.5-5 1-8M16 11c0-2 1-4 1.5-6')],
  candy: [circle(12, 12, 4), p('M15 9l2-4 2 2 3 .5-2.5 3M9 15l-2 4-2-2-3-.5 2.5-3')],
  cup: [p('M5 8h12v7a5 5 0 0 1-5 5h-2a5 5 0 0 1-5-5z'), p('M17 10h1.5a2.5 2.5 0 0 1 0 5H17M9 2v3M13 2v3')],
  chili: [p('M17.5 6c-1.5.5-2.5 2-2.5 3.5C15 15 10.5 20 4 20.5c4.5-2.5 7-6.5 7.5-11 .3-2.5 3-4.5 6-3.5z'), p('M17.5 6c.5-1.5 2-3 3.5-3')],
  jar: [rect(6, 7, 12, 14, 2.5), p('M8 3h8v4H8zM6 12h12')],
  snowflake: [p('M12 2v20M3.3 7l17.4 10M3.3 17 20.7 7'), p('m9 4 3 2 3-2M9 20l3-2 3 2')],
  mushroom: [p('M3 12a9 7 0 0 1 18 0z'), p('M9.5 12v6a2.5 2.5 0 0 0 5 0v-6')],
  garlic: [p('M12 3c0 3-7 6-7 11a7 7 0 0 0 14 0c0-5-7-8-7-11z'), p('M12 9c-1.5 2-2 5-1.5 11M12 9c1.5 2 2 5 1.5 11')],
  cheese: [p('M3 12 15 5l6 5v9H3z'), p('M3 12h18'), circle(8, 15.5, 1.2), circle(15, 16, 1.6)]
}

const props = defineProps({
  name: { type: String, required: true },
  size: { type: Number, default: 18 },
  filled: { type: Boolean, default: false }
})

const elements = computed(() => ICONS[props.name] ?? ICONS.utensils)
</script>

<template>
  <svg
    class="app-icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    :fill="filled ? 'currentColor' : 'none'"
    stroke="currentColor"
    stroke-width="1.9"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <component :is="el.tag" v-for="(el, i) in elements" :key="i" v-bind="el.attrs" />
  </svg>
</template>

<style scoped>
.app-icon {
  flex: none;
  display: inline-block;
  vertical-align: middle;
}
</style>
