<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Send, Loader2, Sparkles } from 'lucide-vue-next'
import ChatBubble from '@/components/ChatBubble.vue'
import SourcePanel from '@/components/SourcePanel.vue'
import { useSSE } from '@/composables/useSSE'
import type { Message } from '@/types'

const messages = ref<Message[]>([])
const input = ref('')
const knowledgeBaseId = ref('')
const chatContainer = ref<HTMLElement | null>(null)
const { isLoading, streamResponse } = useSSE()

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2)
}

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

async function sendMessage() {
  const question = input.value.trim()
  if (!question || isLoading.value) return

  if (!knowledgeBaseId.value) {
    alert('请先输入知识库 ID')
    return
  }

  messages.value.push({
    id: generateId(),
    role: 'user',
    content: question,
    timestamp: Date.now()
  })
  input.value = ''
  await scrollToBottom()

  const assistantMsg: Message = {
    id: generateId(),
    role: 'assistant',
    content: '',
    timestamp: Date.now()
  }
  messages.value.push(assistantMsg)
  await scrollToBottom()

  try {
    await streamResponse(
      '/api/v1/rag/query/stream',
      { question, knowledgeBaseId: knowledgeBaseId.value },
      (chunk: string) => {
        assistantMsg.content += chunk
        scrollToBottom()
      },
      () => {
        assistantMsg.timestamp = Date.now()
      }
    )
  } catch {
    assistantMsg.content += '\n\n[请求失败，请检查后端服务是否正常运行]'
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 顶部栏 -->
    <header class="h-16 glass-effect border-b border-white/5 flex items-center px-8 gap-4 relative z-10">
      <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500/20 to-accent/20 flex items-center justify-center">
        <Sparkles class="w-4 h-4 text-primary-400" />
      </div>
      <h1 class="text-white font-semibold text-base">智能问答</h1>
      <span class="text-xs text-white/30 bg-white/5 px-2 py-0.5 rounded-md">RAG</span>
      <div class="flex-1" />
      <div class="relative">
        <input
          v-model="knowledgeBaseId"
          placeholder="输入知识库 ID"
          class="bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white
                 placeholder-white/25 focus:outline-none focus:border-primary-500/40 w-72
                 transition-all duration-300 focus:shadow-glow"
        />
      </div>
    </header>

    <!-- 消息列表 -->
    <div ref="chatContainer" class="flex-1 overflow-y-auto scrollbar-thin py-6">
      <!-- 空状态 -->
      <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full">
        <div class="w-20 h-20 rounded-3xl bg-gradient-to-br from-primary-500/10 to-accent/10 flex items-center justify-center mb-6 shadow-glow">
          <Sparkles class="w-10 h-10 text-primary-400/60" />
        </div>
        <p class="text-white/50 text-lg font-medium">开始与 AI 对话</p>
        <p class="text-white/25 text-sm mt-2">输入知识库 ID，然后提出你的问题</p>
        <div class="flex gap-3 mt-8">
          <div class="glass-effect rounded-xl px-4 py-2.5 text-xs text-white/30">
            支持流式输出
          </div>
          <div class="glass-effect rounded-xl px-4 py-2.5 text-xs text-white/30">
            展示引用来源
          </div>
          <div class="glass-effect rounded-xl px-4 py-2.5 text-xs text-white/30">
            多知识库检索
          </div>
        </div>
      </div>

      <template v-for="msg in messages" :key="msg.id">
        <ChatBubble :message="msg" />
        <SourcePanel
          v-if="msg.role === 'assistant' && msg.sources && msg.sources.length > 0"
          :sources="msg.sources"
        />
      </template>

      <!-- 加载指示 -->
      <div v-if="isLoading && messages.length > 0 && messages[messages.length - 1].content === ''" class="flex gap-4 px-6 py-3 animate-fade-in">
        <div class="w-10 h-10 rounded-xl glass-effect flex items-center justify-center">
          <Loader2 class="w-5 h-5 text-primary-400 animate-spin" />
        </div>
        <div class="glass-effect rounded-2xl px-5 py-4">
          <div class="flex gap-1.5">
            <span class="w-2 h-2 bg-primary-400/60 rounded-full animate-bounce" style="animation-delay: 0ms" />
            <span class="w-2 h-2 bg-accent/60 rounded-full animate-bounce" style="animation-delay: 150ms" />
            <span class="w-2 h-2 bg-primary-400/60 rounded-full animate-bounce" style="animation-delay: 300ms" />
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="glass-effect border-t border-white/5 p-5 relative z-10">
      <div class="flex items-end gap-4 max-w-4xl mx-auto">
        <div class="flex-1 relative">
          <textarea
            v-model="input"
            @keydown="handleKeydown"
            placeholder="输入你的问题... (Enter 发送, Shift+Enter 换行)"
            rows="1"
            class="w-full bg-white/5 border border-white/10 rounded-2xl px-5 py-3.5 text-sm text-white
                   placeholder-white/20 focus:outline-none focus:border-primary-500/40 resize-none
                   transition-all duration-300 focus:shadow-glow"
          />
        </div>
        <button
          @click="sendMessage"
          :disabled="isLoading || !input.trim()"
          class="w-12 h-12 rounded-2xl bg-gradient-to-r from-primary-500 to-accent text-white flex items-center justify-center
                 hover:shadow-glow-lg transition-all duration-300
                 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          <Send class="w-5 h-5" />
        </button>
      </div>
    </div>
  </div>
</template>
