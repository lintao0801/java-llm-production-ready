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

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface RagRequest {
  question: string
  knowledgeBaseId: string
  topK?: number
  temperature?: number
  stream?: boolean
  conversationId?: string
  historyMessages?: ChatMessage[]
  enableQueryExpansion?: boolean
  enableRerank?: boolean
  metadataFilters?: Record<string, string>
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  sources?: DocumentSource[]
  tokenUsage?: TokenUsage
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