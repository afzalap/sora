<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import type { ChatMessage } from '@/types'
import FlightCard from './FlightCard.vue'

const props = defineProps<{ message: ChatMessage }>()

const renderedContent = computed(() => marked.parse(props.message.content ?? '') as string)
</script>

<template>
  <div class="assistant-row fade-up">
    <!-- Avatar -->
    <div class="avatar">S</div>

    <!-- Content -->
    <div class="content">

      <!-- Thinking indicator: shown while streaming but no content yet -->
      <div v-if="message.toolActive" class="tool-status">
        <span class="pulse-dot" />
        Thinking…
      </div>

      <!-- Rendered markdown: v-html is safe here because content comes from our own AI backend -->
      <div v-if="message.content" class="text-block markdown" v-html="renderedContent" />

      <!-- Flight cards: rendered when the AI returns flight options -->
      <div v-if="message.flights?.length" class="flight-cards">
        <FlightCard
          v-for="(flight, i) in message.flights"
          :key="flight.flightNumber + i"
          :flight="flight"
          :index="i"
        />
      </div>

    </div>
  </div>
</template>

<style scoped>
.assistant-row {
  display: flex;
  gap: 13px;
}

.avatar {
  flex: none;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--accent);
  color: var(--accent-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'IBM Plex Mono', monospace;
  font-weight: 700;
  font-size: 13px;
  margin-top: 1px;
}

.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 3px;
}

.tool-status {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  align-self: flex-start;
  padding: 7px 13px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: var(--surface-2);
  font-size: 12.5px;
  color: var(--ink-2);
  font-family: 'IBM Plex Mono', monospace;
}

.pulse-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  animation: pulse-dot 1s ease-in-out infinite;
}

.text-block {
  font-size: 14.5px;
  line-height: 1.6;
  color: var(--ink);
  word-break: break-word;
}

/* Markdown styles scoped to assistant messages */
.markdown :deep(p)        { margin: 0 0 8px; }
.markdown :deep(p:last-child) { margin-bottom: 0; }
.markdown :deep(strong)   { font-weight: 600; color: var(--ink); }
.markdown :deep(ul),
.markdown :deep(ol)       { margin: 6px 0 8px 18px; padding: 0; }
.markdown :deep(li)       { margin-bottom: 3px; }
.markdown :deep(code)     { font-family: 'IBM Plex Mono', monospace; font-size: 12.5px;
                             background: var(--surface-2); padding: 1px 5px; border-radius: 4px; }
.markdown :deep(pre)      { background: var(--surface-2); padding: 10px 14px;
                             border-radius: 8px; overflow-x: auto; margin: 8px 0; }
.markdown :deep(pre code) { background: none; padding: 0; }

/* Blinking text cursor shown during streaming */
.cursor {
  display: inline-block;
  width: 7px;
  height: 15px;
  background: var(--accent);
  margin-left: 2px;
  vertical-align: -2px;
  animation: blink 1.1s steps(1) infinite;
}

.flight-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
}
</style>
