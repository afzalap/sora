<script setup lang="ts">
import type { ChatMessage } from '@/types'

defineProps<{ message: ChatMessage }>()
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

      <!-- Text: shown as content arrives (streaming) or once done -->
      <div v-if="message.content" class="text-block">
        {{ message.content }}<span
          v-if="message.streaming && !message.toolActive"
          class="cursor"
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
  white-space: pre-wrap;
  word-break: break-word;
}

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
</style>
