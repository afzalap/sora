<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useBookingsStore } from '@/stores/bookings'

const chat = useChatStore()
const bookingsStore = useBookingsStore()
const draft = ref('')

function send() {
  const text = draft.value.trim()
  if (!text || chat.isStreaming) return
  draft.value = ''
  // After each message, refresh bookings — the AI may have just created or cancelled one.
  chat.send(text, () => bookingsStore.fetch())
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}
</script>

<template>
  <div class="composer-wrap">
    <div class="composer-inner">
      <div class="input-row" :class="{ 'input-row--streaming': chat.isStreaming }">
        <textarea
          v-model="draft"
          rows="1"
          class="input"
          :placeholder="chat.isStreaming ? 'Sora is thinking…' : 'Message Sora — e.g. cheapest flights Tokyo → Kumamoto'"
          :disabled="chat.isStreaming"
          @keydown="onKey"
          @input="($event.target as HTMLTextAreaElement).style.height = 'auto'; ($event.target as HTMLTextAreaElement).style.height = ($event.target as HTMLTextAreaElement).scrollHeight + 'px'"
        />
        <button
          class="send-btn"
          :class="{ 'send-btn--active': draft.trim() && !chat.isStreaming }"
          :disabled="!draft.trim() || chat.isStreaming"
          @click="send"
        >
          ↑
        </button>
      </div>
      <p class="disclaimer">
        Sora confirms before booking · demo data, no real payments
      </p>
    </div>
  </div>
</template>

<style scoped>
.composer-wrap {
  flex: none;
  padding: 14px 24px 18px;
  background: linear-gradient(to top, var(--bg) 70%, transparent);
}

.composer-inner {
  max-width: 680px;
  margin: 0 auto;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: var(--surface);
  border: 1px solid var(--line-2);
  border-radius: 14px;
  padding: 7px 7px 7px 16px;
  box-shadow: var(--shadow);
  transition: border-color 0.15s;
}
.input-row:focus-within {
  border-color: var(--accent);
}
.input-row--streaming {
  opacity: 0.7;
}

.input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: var(--ink);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 14.5px;
  resize: none;
  min-height: 34px;
  max-height: 160px;
  overflow-y: auto;
  line-height: 1.5;
  padding: 4px 0;
}

.send-btn {
  flex: none;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: var(--surface-2);
  color: var(--ink-3);
  cursor: default;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}

.send-btn--active {
  background: var(--accent);
  color: var(--accent-ink);
  cursor: pointer;
}

.disclaimer {
  text-align: center;
  font-size: 11.5px;
  color: var(--ink-3);
  margin: 9px 0 0;
}
</style>
