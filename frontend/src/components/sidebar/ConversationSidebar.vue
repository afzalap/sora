<!-- Phase 2 feature: conversation history + sidebar.
     For Phase 1, the sidebar is present (so the layout is final) but
     muted — non-interactive, with a "Coming soon" placeholder.
     When features.conversationHistory flips to true, remove the muted
     wrapper and wire real conversation data from the store. -->

<script setup lang="ts">
import { features } from '@/config/features'

defineProps<{ open: boolean }>()
defineEmits<{ close: []; newChat: [] }>()
</script>

<template>
  <aside :class="['sidebar', open && 'sidebar--open']">

    <!-- New chat (Phase 1 live) -->
    <div class="sidebar-top">
      <button class="new-chat-btn" @click="$emit('newChat')">
        <svg width="16" height="16" viewBox="0 0 17 17" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
          <path d="M8.5 2.8v11.4M2.8 8.5h11.4"/>
        </svg>
        New chat
      </button>
    </div>

    <!-- History section (Phase 2 muted) -->
    <div :class="['sidebar-history', !features.conversationHistory && 'feature-muted']">
      <div class="history-label">
        <span class="dot" />
        Needs a decision
      </div>
      <div class="history-empty">No pending decisions.</div>

      <div class="history-label recent-label">Recent</div>
      <div class="history-empty">Your past conversations will appear here.</div>
    </div>

    <!-- "Soon" tag visible when muted -->
    <div v-if="!features.conversationHistory" class="soon-tag">
      Soon
    </div>

  </aside>
</template>

<style scoped>
.sidebar {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 272px;
  max-width: 84vw;
  background: var(--surface);
  border-right: 1px solid var(--line);
  box-shadow: 14px 0 40px -24px rgba(0, 0, 0, 0.4);
  z-index: 9;
  display: flex;
  flex-direction: column;
  transform: translateX(-100%);
  transition: transform 0.32s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar--open {
  transform: translateX(0);
}

.sidebar-top {
  padding: 14px 14px 10px;
  flex: none;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 11px 13px;
  border: 1px solid var(--line-2);
  border-radius: 11px;
  background: var(--surface);
  color: var(--ink);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.new-chat-btn:hover {
  border-color: var(--accent);
  background: var(--surface-2);
}

.sidebar-history {
  flex: 1;
  overflow-y: auto;
  padding: 8px 10px 16px;
  position: relative;
}

.history-label {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 12px 6px 7px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--accent-soft-ink);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.recent-label {
  color: var(--ink-3);
  margin-top: 8px;
}

.history-empty {
  padding: 6px 6px 4px;
  font-size: 12.5px;
  color: var(--ink-3);
  font-style: italic;
}

.soon-tag {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 3px 10px;
  border-radius: 20px;
  background: var(--accent-soft);
  color: var(--accent-soft-ink);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  pointer-events: none;
}
</style>
