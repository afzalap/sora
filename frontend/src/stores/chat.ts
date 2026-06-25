import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'
import { authHeaders } from '@/api/client'
import type { ChatMessage } from '@/types'

// Unique ID helper — just a counter, no library needed.
let seq = 0
const uid = () => String(++seq)

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const isStreaming = ref(false)
  // Exposed so ChatThread can auto-scroll when new content arrives.
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

  function appendToken(msg: ChatMessage, token: string) {
    // Once first real content arrives, stop the "Thinking…" state.
    if (msg.toolActive && token.trim()) msg.toolActive = false
    msg.content += token
    scrollTrigger.value++
  }

  function finaliseMessage(msg: ChatMessage) {
    msg.streaming = false
    msg.toolActive = false
    scrollTrigger.value++
  }

  // Parses the Server-Sent Events stream from POST /api/chat/stream.
  // Spring WebFlux emits each token as:  "data: <token>\n\n"
  // We use fetch + ReadableStream because EventSource only supports GET
  // and can't send custom headers (our JWT Authorization header).
  async function send(content: string, onDone?: () => void) {
    if (isStreaming.value) return

    addUserMessage(content)
    await nextTick()

    const placeholder = addAssistantPlaceholder()
    isStreaming.value = true

    try {
      const response = await fetch('/api/chat/stream', {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ message: content }),
      })

      if (!response.ok || !response.body) {
        placeholder.content = 'Sorry, something went wrong. Please try again.'
        finaliseMessage(placeholder)
        return
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // SSE events are separated by "\n\n".
        const parts = buffer.split('\n\n')
        buffer = parts.pop() ?? ''

        for (const part of parts) {
          for (const line of part.split('\n')) {
            if (line.startsWith('data: ')) {
              const token = line.slice(6)
              if (token && token !== '[DONE]') {
                appendToken(placeholder, token)
              }
            }
          }
        }

        await nextTick()
      }
    } catch {
      placeholder.content = 'Connection error. Is the backend running?'
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
