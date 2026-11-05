<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** idle | wave | thinking | talking | happy | confused | shy (covers the eyes) */
  mood: { type: String, default: 'idle' },
  size: { type: Number, default: 220 },
  /** Where the robot looks, from -1 (left/up) to 1 (right/down). Used to follow what the user types. */
  lookX: { type: Number, default: 0 },
  lookY: { type: Number, default: 0 }
})

const clamp = (value) => Math.max(-1, Math.min(1, value))
const moodClass = computed(() => `is-${props.mood}`)
const happyEyes = computed(() => props.mood === 'happy' || props.mood === 'wave')
const closedEyes = computed(() => props.mood === 'shy')
const gaze = computed(() => ({ transform: `translate(${clamp(props.lookX) * 7}px, ${clamp(props.lookY) * 4}px)` }))
const headTurn = computed(() => ({ transform: `rotate(${clamp(props.lookX) * 4}deg)` }))
</script>

<template>
  <svg
    class="robot"
    :class="moodClass"
    :width="size"
    :height="size * 1.15"
    viewBox="0 0 200 230"
    role="img"
    :aria-label="`Chef Magic, o chef robô (${mood})`"
  >
    <defs>
      <linearGradient id="rc-shell" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#ffc56e" />
        <stop offset="0.55" stop-color="#ff8a6b" />
        <stop offset="1" stop-color="#ff5e7e" />
      </linearGradient>
      <linearGradient id="rc-apron" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#ffffff" />
        <stop offset="1" stop-color="#d9d4f5" />
      </linearGradient>
      <radialGradient id="rc-visor" cx="0.5" cy="0.35" r="0.8">
        <stop offset="0" stop-color="#2a2152" />
        <stop offset="1" stop-color="#120d29" />
      </radialGradient>
      <filter id="rc-glow" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur stdDeviation="2.4" result="blur" />
        <feMerge>
          <feMergeNode in="blur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
    </defs>

    <ellipse class="shadow" cx="100" cy="222" rx="46" ry="6" fill="rgba(0,0,0,.35)" />

    <g class="bot">
      <!-- Thinking bubbles -->
      <g class="thought">
        <circle cx="166" cy="62" r="4" />
        <circle cx="178" cy="46" r="6" />
        <circle cx="190" cy="26" r="8" />
      </g>
      <!-- Confused mark -->
      <text class="question" x="166" y="60">?</text>

      <!-- Arms -->
      <g class="arm arm-left">
        <rect x="40" y="160" width="26" height="14" rx="7" fill="url(#rc-shell)" />
        <circle cx="38" cy="167" r="9" fill="#f4f2ff" />
      </g>
      <g class="arm arm-right">
        <rect x="134" y="160" width="26" height="14" rx="7" fill="url(#rc-shell)" />
        <circle cx="162" cy="167" r="9" fill="#f4f2ff" />
        <!-- Whisk -->
        <g class="whisk">
          <line x1="162" y1="164" x2="172" y2="130" stroke="#c9c4e8" stroke-width="4" stroke-linecap="round" />
          <path d="M172 130 C160 112 166 98 176 100 C186 102 186 116 172 130 Z" fill="none" stroke="#e9e6ff" stroke-width="2.2" />
          <path d="M172 130 C168 114 172 104 176 104 C180 104 180 116 172 130 Z" fill="none" stroke="#e9e6ff" stroke-width="2" />
        </g>
      </g>

      <!-- Body -->
      <rect x="64" y="150" width="72" height="58" rx="22" fill="url(#rc-apron)" />
      <path d="M78 150 L122 150 L100 172 Z" fill="#ff5e7e" />
      <circle class="core" cx="100" cy="186" r="6" fill="#3cf2d0" filter="url(#rc-glow)" />
      <rect x="86" y="198" width="28" height="4" rx="2" fill="#c9c4e8" />

      <!-- Head -->
      <g class="head-turn" :style="headTurn">
      <g class="head">
        <circle cx="44" cy="112" r="9" fill="#ffd08a" />
        <circle class="ear-led" cx="44" cy="112" r="3.5" fill="#3cf2d0" />
        <circle cx="156" cy="112" r="9" fill="#ffd08a" />
        <circle class="ear-led" cx="156" cy="112" r="3.5" fill="#3cf2d0" />

        <rect x="46" y="66" width="108" height="88" rx="32" fill="url(#rc-shell)" />
        <path d="M62 78 Q100 62 138 78" stroke="rgba(255,255,255,.45)" stroke-width="4" fill="none" stroke-linecap="round" />

        <rect x="58" y="84" width="84" height="50" rx="24" fill="url(#rc-visor)" />
        <rect x="58" y="84" width="84" height="50" rx="24" fill="none" stroke="rgba(60,242,208,.25)" stroke-width="1.5" />

        <g class="eyes" :style="gaze" filter="url(#rc-glow)">
          <template v-if="closedEyes">
            <path d="M76 106 Q84 112 92 106" stroke="#3cf2d0" stroke-width="4" fill="none" stroke-linecap="round" />
            <path d="M108 106 Q116 112 124 106" stroke="#3cf2d0" stroke-width="4" fill="none" stroke-linecap="round" />
          </template>
          <template v-else-if="happyEyes">
            <path d="M76 110 Q84 100 92 110" stroke="#3cf2d0" stroke-width="4.5" fill="none" stroke-linecap="round" />
            <path d="M108 110 Q116 100 124 110" stroke="#3cf2d0" stroke-width="4.5" fill="none" stroke-linecap="round" />
          </template>
          <template v-else>
            <ellipse class="eye" cx="84" cy="106" rx="7" ry="9" fill="#3cf2d0" />
            <ellipse class="eye" cx="116" cy="106" rx="7" ry="9" fill="#3cf2d0" />
          </template>
        </g>

        <g class="mouth" :style="gaze" fill="#3cf2d0" filter="url(#rc-glow)">
          <rect class="bar b1" x="89" y="122" width="3" height="4" rx="1.5" />
          <rect class="bar b2" x="94" y="121" width="3" height="6" rx="1.5" />
          <rect class="bar b3" x="99" y="120" width="3" height="8" rx="1.5" />
          <rect class="bar b4" x="104" y="121" width="3" height="6" rx="1.5" />
          <rect class="bar b5" x="109" y="122" width="3" height="4" rx="1.5" />
        </g>

        <circle cx="64" cy="140" r="6" fill="#ff8fab" opacity=".55" />
        <circle cx="136" cy="140" r="6" fill="#ff8fab" opacity=".55" />

        <!-- Chef hat -->
        <g class="hat">
          <circle cx="72" cy="42" r="20" fill="#ffffff" />
          <circle cx="100" cy="30" r="26" fill="#ffffff" />
          <circle cx="128" cy="42" r="20" fill="#ffffff" />
          <rect x="62" y="40" width="76" height="26" rx="6" fill="#ffffff" />
          <rect x="60" y="56" width="80" height="14" rx="6" fill="#ece9fb" />
          <circle class="gem" cx="100" cy="63" r="3.5" fill="#ff5e7e" filter="url(#rc-glow)" />
        </g>
      </g>
      </g>

      <!-- Hands that cover the eyes when the password is revealed -->
      <g class="cover-hands">
        <g class="cover cover-left">
          <g class="mitt" transform="rotate(-14 86 110)">
            <rect x="70" y="96" width="32" height="28" rx="13" fill="#f4f2ff" stroke="#c9c4e8" stroke-width="1.5" />
            <path d="M78 97v9M86 96v10M94 97v9" stroke="#c9c4e8" stroke-width="1.6" stroke-linecap="round" />
          </g>
        </g>
        <g class="cover cover-right">
          <g class="mitt" transform="rotate(14 114 110)">
            <rect x="98" y="96" width="32" height="28" rx="13" fill="#f4f2ff" stroke="#c9c4e8" stroke-width="1.5" />
            <path d="M106 97v9M114 96v10M122 97v9" stroke="#c9c4e8" stroke-width="1.6" stroke-linecap="round" />
          </g>
        </g>
      </g>
    </g>
  </svg>
</template>

<style scoped>
.robot {
  display: block;
  overflow: visible;
}
.robot * {
  transform-box: fill-box;
}

.bot {
  animation: float 4.2s ease-in-out infinite;
}
.shadow {
  transform-origin: center;
  animation: shadow 4.2s ease-in-out infinite;
}
.hat {
  transform-origin: 50% 100%;
  animation: hat 4.2s ease-in-out infinite;
}
.eye {
  transform-origin: center;
  animation: blink 4.6s infinite;
}
.core,
.gem,
.ear-led {
  animation: pulse 2.4s ease-in-out infinite;
}
.arm-left {
  transform-origin: 100% 50%;
}
.arm-right {
  transform-origin: 0% 50%;
}
.whisk {
  transform-origin: 0% 100%;
  animation: whisk 3s ease-in-out infinite;
}
.thought,
.question {
  opacity: 0;
  fill: rgba(255, 255, 255, 0.85);
}
.question {
  font: 800 34px var(--font-display);
  fill: var(--amber);
}
.bar {
  transform-origin: center;
}
.eyes,
.mouth {
  transition: transform 0.25s var(--ease-out);
}
.head-turn {
  transform-origin: 50% 90%;
  transition: transform 0.35s var(--ease-out);
}
.robot .mitt {
  transform-box: view-box;
}
.cover {
  opacity: 0;
  transform-origin: center;
  transition: transform 0.45s var(--ease-spring), opacity 0.2s;
}
.cover-left {
  transform: translate(-48px, 58px) scale(0.6);
}
.cover-right {
  transform: translate(48px, 58px) scale(0.6);
}

/* Shy: the hands fly from the sides to cover the visor and the arms hide */
.is-shy .cover {
  opacity: 1;
  transform: translate(0, 0) scale(1);
}
.is-shy .arm {
  opacity: 0;
  transition: opacity 0.2s;
}
.is-shy .head-turn {
  transform: rotate(-6deg) !important;
}

/* Moods */
.is-wave .arm-right {
  animation: wave 1.1s ease-in-out infinite;
}
.is-thinking .eyes {
  animation: look-up 2s ease-in-out infinite;
}
.is-thinking .thought {
  opacity: 1;
}
.is-thinking .thought circle {
  animation: bubble 1.4s ease-in-out infinite;
}
.is-thinking .thought circle:nth-child(2) { animation-delay: 0.2s; }
.is-thinking .thought circle:nth-child(3) { animation-delay: 0.4s; }
.is-thinking .whisk {
  animation: stir 0.6s ease-in-out infinite;
}
.is-talking .bar {
  animation: talk 0.45s ease-in-out infinite;
}
.is-talking .b2, .is-talking .b4 { animation-delay: 0.12s; }
.is-talking .b3 { animation-delay: 0.24s; }
.is-happy .bot {
  animation: hop 0.9s var(--ease-spring) 2, float 4.2s ease-in-out 1.8s infinite;
}
.is-confused .head {
  transform-origin: 50% 100%;
  animation: tilt 2.4s ease-in-out infinite;
}
.is-confused .question {
  opacity: 1;
  animation: bubble 1.6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
@keyframes shadow {
  0%, 100% { transform: scaleX(1); opacity: 1; }
  50% { transform: scaleX(0.82); opacity: 0.7; }
}
@keyframes hat {
  0%, 100% { transform: rotate(-2deg); }
  50% { transform: rotate(2.5deg); }
}
@keyframes blink {
  0%, 92%, 100% { transform: scaleY(1); }
  95% { transform: scaleY(0.1); }
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.45; }
}
@keyframes whisk {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(-8deg); }
}
@keyframes stir {
  0%, 100% { transform: rotate(-14deg); }
  50% { transform: rotate(14deg); }
}
@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-38deg); }
  75% { transform: rotate(-18deg); }
}
@keyframes look-up {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(4px, -4px); }
}
@keyframes bubble {
  0%, 100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-4px); opacity: 1; }
}
@keyframes talk {
  0%, 100% { transform: scaleY(1); }
  50% { transform: scaleY(2.4); }
}
@keyframes hop {
  0%, 100% { transform: translateY(0); }
  40% { transform: translateY(-22px); }
}
@keyframes tilt {
  0%, 100% { transform: rotate(-6deg); }
  50% { transform: rotate(4deg); }
}
</style>
