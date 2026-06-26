<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

defineProps<{
  theme: 'light' | 'dark'
  confirmedCount: number
}>()

defineEmits<{
  toggleSidebar: []
  newChat: []
  openBookings: []
  toggleTheme: []
}>()

const auth = useAuthStore()
const userInitial = auth.username?.charAt(0).toUpperCase() ?? '?'
const themeLabel = (t: 'light' | 'dark') => (t === 'light' ? 'Dark' : 'Light')
</script>

<template>
  <header class="header">

    <!-- Left: sidebar toggle + new chat + logo -->
    <div class="header-left">
      <button class="icon-btn" title="Conversations" @click="$emit('toggleSidebar')">
        <svg width="17" height="17" viewBox="0 0 17 17" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round">
          <path d="M2 4h13M2 8.5h13M2 13h13"/>
        </svg>
      </button>
      <button class="icon-btn" title="New chat" @click="$emit('newChat')">
        <svg width="17" height="17" viewBox="0 0 17 17" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round">
          <path d="M8.5 2.8v11.4M2.8 8.5h11.4"/>
        </svg>
      </button>
      <button class="logo-btn" title="New chat" @click="$emit('newChat')">
        <div class="logo-mark">空</div>
        <div class="logo-text">
          <span class="logo-name">Sora</span>
          <span class="logo-sub">Flight assistant</span>
        </div>
      </button>
    </div>

    <!-- Right: bookings + theme + avatar -->
    <div class="header-right">
      <button class="bookings-btn" @click="$emit('openBookings')">
        My bookings
        <span class="badge">{{ confirmedCount }}</span>
      </button>
      <button class="text-btn" @click="$emit('toggleTheme')">{{ themeLabel(theme) }}</button>
      <div class="avatar">{{ userInitial }}</div>
    </div>
  </header>
</template>

<style scoped>
.header {
  height: 60px;
  flex: none;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  z-index: 12;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--line-2);
  border-radius: 9px;
  background: var(--surface);
  color: var(--ink-2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.15s, background 0.15s;
}
.icon-btn:hover {
  border-color: var(--accent);
  background: var(--surface-2);
}

.logo-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 8px;
  margin-left: 2px;
}
.logo-btn:hover {
  background: var(--surface-2);
}

.logo-mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--accent);
  color: var(--accent-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Hiragino Sans', 'Yu Gothic', sans-serif;
  font-weight: 700;
  font-size: 15px;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
  text-align: left;
}

.logo-name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--ink);
}

.logo-sub {
  font-size: 11px;
  color: var(--ink-3);
}

.bookings-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 13px;
  border: 1px solid var(--line-2);
  border-radius: 9px;
  background: var(--surface);
  color: var(--ink);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.bookings-btn:hover {
  border-color: var(--accent);
  background: var(--surface-2);
}

.badge {
  min-width: 19px;
  height: 19px;
  padding: 0 5px;
  border-radius: 6px;
  background: var(--accent-soft);
  color: var(--accent-soft-ink);
  font-family: 'IBM Plex Mono', monospace;
  font-size: 11px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.text-btn {
  height: 36px;
  padding: 0 13px;
  border: 1px solid var(--line-2);
  border-radius: 9px;
  background: var(--surface);
  color: var(--ink-2);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.15s;
}
.text-btn:hover {
  border-color: var(--accent);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--surface-2);
  border: 1px solid var(--line-2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-2);
}
</style>
