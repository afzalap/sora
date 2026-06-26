import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'
import { authHeaders } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import type { ChatMessage, FlightOption } from '@/types'

let seq = 0
const uid = () => String(++seq)

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const isStreaming = ref(false)
  const scrollTrigger = ref(0)

  function addUserMessage(content: string) {
    messages.value.push({ id: uid(), role: 'user', content, streaming: false, toolActive: false })
    scrollTrigger.value++
  }

  function addAssistantPlaceholder(): ChatMessage {
    const msg: ChatMessage = {
      id: uid(),
      role: 'assistant',
      content: '',
      streaming: true,
      toolActive: true,
    }
    messages.value.push(msg)
    return msg
  }

  function finaliseMessage(msg: ChatMessage) {
    msg.streaming = false
    msg.toolActive = false
    scrollTrigger.value++
  }

  // POST /api/chat — non-streaming.
  //
  // Why not /api/chat/stream?  Spring AI's tool-calling loop (search flights →
  // re-prompt → generate reply) happens server-side.  The SSE stream stays
  // open but emits nothing visible while the tool round-trip is in flight,
  // leaving the frontend stuck in "Thinking…" indefinitely.  The non-streaming
  // endpoint waits for the full agentic loop, then returns the final reply in
  // one shot — reliable for Phase 1.  Phase 2 will wire typed SSE events
  // (flight-cards, booking-success) when the backend emits them explicitly.
  // Update a message safely through the reactive array so Vue's proxy
  // detects the change and re-renders. Mutating the original object reference
  // (returned by addAssistantPlaceholder) bypasses the proxy and can silently
  // fail to trigger an update.
  function setMessageContent(id: string, content: string, flights?: FlightOption[]) {
    const msg = messages.value.find((m) => m.id === id)
    if (msg) {
      msg.content = content
      if (flights) msg.flights = flights
    }
  }

  async function send(content: string, onDone?: () => void) {
    if (isStreaming.value) return

    addUserMessage(content)
    await nextTick()

    const placeholder = addAssistantPlaceholder()
    isStreaming.value = true

    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), 90_000)

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ message: content }),
        signal: controller.signal,
      })

      if (!response.ok) {
        if (response.status === 401) {
          // Token expired — logout() clears the Pinia token ref so
          // App.vue's v-if flips and the login view appears immediately.
          useAuthStore().logout()
          return
        }
        setMessageContent(placeholder.id, `Server error ${response.status}. Is Spring Boot running on port 8080?`)
        return
      }

      const data = (await response.json()) as { reply: string; flights?: FlightOption[] }
      setMessageContent(placeholder.id, data.reply, data.flights)

    } catch (e) {
      if (e instanceof Error && e.name === 'AbortError') {
        setMessageContent(placeholder.id, 'No response after 90 s. Is the AI backend running?')
      } else {
        setMessageContent(placeholder.id, 'Could not reach the backend. Is Spring Boot running on port 8080?')
      }
    } finally {
      clearTimeout(timeout)
      finaliseMessage(placeholder)
      isStreaming.value = false
      onDone?.()
    }
  }

  function clear() {
    messages.value = []
    isStreaming.value = false
    scrollTrigger.value = 0
  }

  return { messages, isStreaming, scrollTrigger, send, clear }
})
