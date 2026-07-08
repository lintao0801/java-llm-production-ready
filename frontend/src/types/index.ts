export interface DocumentSource {
  documentId: string
  fileName: string
  content: string
  similarityScore: number
  pageNumber?: number
  metadata?: Record<string, any>
}

export interface TokenUsage {
  inputTokens: number
  outputTokens: number
  totalTokens: number
}

export interface RagResponse {
  answer: string
  sources: DocumentSource[]
  tokenUsage: TokenUsage
  latencyMs: number
  model: string
  conversationId: string
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  sources?: DocumentSource[]
  timestamp: number
}

export interface KnowledgeBase {
  id: string
  name: string
  description?: string
  embeddingModel?: string
  documentCount?: number
}

export interface Result<T> {
  code: number
  message: string
  data: T
}
