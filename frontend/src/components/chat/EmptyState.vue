<script setup lang="ts">
defineEmits<{ send: [text: string] }>()

const chips = [
  'Cheapest flights Tokyo → Osaka, 15-day round trip',
  "What's the baggage allowance on ANA domestic?",
  'Any flights from HND to OKA this weekend?',
  'Show my bookings',
]
</script>

<template>
  <div class="empty-wrap">
    <!-- Sora identity -->
    <div class="sora-mark">空</div>
    <h1 class="greeting">Hi, I'm Sora.</h1>
    <p class="intro">
      Tell me where you want to go — I'll search live flights and handle your bookings.
      I always confirm before I book anything.
    </p>

    <!-- Suggestion chips -->
    <div class="chips">
      <button
        v-for="chip in chips"
        :key="chip"
        class="chip"
        @click="$emit('send', chip)"
      >
        {{ chip }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.empty-wrap {
  max-width: 680px;
  margin: 0 auto;
  padding: 64px 24px 24px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.sora-mark {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--accent);
  color: var(--accent-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Hiragino Sans', 'Yu Gothic', sans-serif;
  font-weight: 700;
  font-size: 26px;
  margin-bottom: 22px;
}

.greeting {
  font-size: 30px;
  font-weight: 600;
  letter-spacing: -0.02em;
  margin: 0 0 10px;
}

.intro {
  font-size: 16px;
  color: var(--ink-2);
  line-height: 1.55;
  max-width: 480px;
  margin: 0 0 30px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  padding: 11px 15px;
  border: 1px solid var(--line-2);
  border-radius: 11px;
  background: var(--surface);
  color: var(--ink);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.15s, background 0.15s;
}
.chip:hover {
  border-color: var(--accent);
  background: var(--surface-2);
}
</style>
