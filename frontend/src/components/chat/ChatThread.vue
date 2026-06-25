<script setup lang="ts">
import { ref, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useBookingsStore } from '@/stores/bookings'
import EmptyState from './EmptyState.vue'
import UserMessage from './UserMessage.vue'
import AssistantMessage from './AssistantMessage.vue'

const chat = useChatStore()
const bookingsStore = useBookingsStore()
const threadEl = ref<HTMLElement | null>(null)

// Auto-scroll to bottom whenever new content is added.
// scrollTrigger is incremented in the store on every token or new message.
watch(
  () => chat.scrollTrigger,
  () => {
    if (threadEl.value) {
      threadEl.value.scrollTop = threadEl.value.scrollHeight
    }
  },
)

function sendChip(text: string) {
  chat.send(text, () => bookingsStore.fetch())
}
</script>

<template>
  <div ref="threadEl" class="thread">
    <!-- Empty state: suggestion chips -->
    <EmptyState
      v-if="chat.messages.length === 0"
      @send="sendChip"
    />

    <!-- Message list -->
    <div v-else class="messages">
      <template v-for="msg in chat.messages" :key="msg.id">
        <UserMessage v-if="msg.role === 'user'" :content="msg.content" />
        <AssistantMessage v-else :message="msg" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.thread {
  flex: 1;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.messages {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}
</style>
