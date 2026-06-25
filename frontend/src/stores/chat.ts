import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'
import { authHeaders } from '@/api/client'
import type { ChatMessage } from '@/types'

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
  async function send(content: string, onDone?: () => void) {
    if (isStreaming.value) return

    addUserMessage(content)
    await nextTick()

    const placeholder = addAssistantPlaceholder()
    isStreaming.value = true

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ message: content }),
      })

      if (!response.ok) {
        const status = response.status
        placeholder.content =
          status === 401
            ? 'Session expired — please sign in again.'
            : 'Something went wrong on the server. Please try again.'
        return
      }

      const data = (await response.json()) as { reply: string }
      placeholder.content = data.reply

    } catch {
      placeholder.content = 'Could not reach the backend. Is Spring Boot running on port 8080?'
    } finally {
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
