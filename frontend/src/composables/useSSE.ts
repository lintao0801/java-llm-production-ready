import { ref } from 'vue'
import type { DocumentSource, TokenUsage } from '@/types'

export interface SSEEvent {
  type: 'message' | 'sources' | 'usage'
  data: any
}

export function useSSE() {
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  async function streamResponse(
    url: string,
    body: any,
    onChunk: (chunk: string) => void,
    onDone: () => void,
    onEvent?: (event: SSEEvent) => void
  ) {
    isLoading.value = true
    error.value = null

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('No reader available')
      }

      const decoder = new TextDecoder()
      let currentEvent = 'message'
      
      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          onDone()
          break
        }

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')
        
        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data === '[DONE]') {
              onDone()
              return
            }

            if (currentEvent === 'sources') {
              try {
                const sources: DocumentSource[] = JSON.parse(data)
                onEvent?.({ type: 'sources', data: sources })
              } catch {
                // ignore parse errors for sources
              }
            } else if (currentEvent === 'usage') {
              try {
                const usage: TokenUsage = JSON.parse(data)
                onEvent?.({ type: 'usage', data: usage })
              } catch {
                // ignore parse errors for usage
              }
            } else {
              // default: message event (text chunks)
              try {
                const parsed = JSON.parse(data)
                if (parsed.content) {
                  onChunk(parsed.content)
                }
              } catch {
                if (data && data !== '[DONE]') {
                  onChunk(data)
                }
              }
            }
          }
        }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    isLoading,
    error,
    streamResponse
  }
}