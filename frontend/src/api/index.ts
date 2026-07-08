import axios from 'axios'
import type { Result, RagResponse } from '@/types'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

export const chatApi = {
  simpleChat(message: string) {
    return api.get<Result<string>>('/chat/chat', {
      params: { message }
    })
  }
}

export const documentApi = {
  upload(file: File, knowledgeBaseId: string) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('knowledgeBaseId', knowledgeBaseId)
    return api.post<Result<void>>('/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  uploadBatch(files: File[], knowledgeBaseId: string) {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    formData.append('knowledgeBaseId', knowledgeBaseId)
    return api.post<Result<void>>('/documents/upload/batch', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const knowledgeBaseApi = {
  delete(id: string) {
    return api.delete<Result<void>>(`/knowledge-bases/${id}`)
  }
}

export const ragApi = {
  query(params: {
    question: string
    knowledgeBaseId: string
    topK?: number
    temperature?: number
  }) {
    return api.post<Result<RagResponse>>('/rag/query', params)
  },
  
  queryStream(params: {
    question: string
    knowledgeBaseId: string
    topK?: number
    temperature?: number
  }) {
    return fetch('/api/v1/rag/query/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params)
    })
  }
}
