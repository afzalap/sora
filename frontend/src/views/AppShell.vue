<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useBookingsStore } from '@/stores/bookings'
import { useChatStore } from '@/stores/chat'
import TheHeader from '@/components/TheHeader.vue'
import ConversationSidebar from '@/components/sidebar/ConversationSidebar.vue'
import ChatThread from '@/components/chat/ChatThread.vue'
import ChatComposer from '@/components/chat/ChatComposer.vue'
import BookingsDrawer from '@/components/bookings/BookingsDrawer.vue'

defineProps<{ theme: 'light' | 'dark' }>()
defineEmits<{ toggleTheme: [] }>()

const bookingsStore = useBookingsStore()
const chatStore = useChatStore()

const sidebarOpen = ref(false)
const bookingsOpen = ref(false)

function openBookings() {
  bookingsOpen.value = true
  sidebarOpen.value = false
}

function newChat() {
  chatStore.clear()
  sidebarOpen.value = false
}

// Refresh bookings whenever the drawer opens, and once on mount.
onMounted(() => bookingsStore.fetch())
</script>

<template>
  <div class="shell">
    <TheHeader
      :theme="theme"
      :confirmed-count="bookingsStore.confirmedCount"
      @toggle-sidebar="sidebarOpen = !sidebarOpen"
      @new-chat="newChat"
      @open-bookings="openBookings"
      @toggle-theme="$emit('toggleTheme')"
    />

    <div class="shell-body">

      <!-- ── Main chat column ─────────────────────────────── -->
      <main class="chat-col">
        <ChatThread />
        <ChatComposer />
      </main>

      <!-- ── Left conversations sidebar (slide-in) ──────── -->
      <div
        v-if="sidebarOpen"
        class="overlay"
        @click="sidebarOpen = false"
      />
      <ConversationSidebar
        :open="sidebarOpen"
        @close="sidebarOpen = false"
        @new-chat="newChat"
      />

      <!-- ── Right bookings drawer (slide-in) ───────────── -->
      <div
        v-if="bookingsOpen"
        class="overlay"
        @click="bookingsOpen = false"
      />
      <BookingsDrawer
        :open="bookingsOpen"
        @close="bookingsOpen = false"
      />

    </div>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.shell-body {
  flex: 1;
  position: relative;
  overflow: hidden;
  display: flex;
  min-height: 0;
}

.chat-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.28);
  z-index: 8;
}
</style>
