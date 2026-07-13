<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Send, Loader2, Sparkles, Zap, Sliders } from 'lucide-vue-next'
import ChatBubble from '@/components/ChatBubble.vue'
import SourcePanel from '@/components/SourcePanel.vue'
import { useSSE } from '@/composables/useSSE'
import type { Message, ChatMessage, DocumentSource, TokenUsage } from '@/types'

const messages = ref<Message[]>([])
const input = ref('')
const knowledgeBaseId = ref('')
const chatContainer = ref<HTMLElement | null>(null)
const { isLoading, streamResponse } = useSSE()

// Advanced RAG 配置
const showAdvancedOptions = ref(false)
const enableQueryExpansion = ref(true)
const enableRerank = ref(false)
const topK = ref(5)
const conversationId = ref('')

// 多轮对话历史（最多保留最近 10 轮）
const conversationHistory = ref<ChatMessage[]>([])

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2)
}

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 构建请求体
function buildRequestPayload(question: string) {
  return {
    question,
    knowledgeBaseId: knowledgeBaseId.value,
    topK: topK.value,
    enableQueryExpansion: enableQueryExpansion.value,
    enableRerank: enableRerank.value,
    conversationId: conversationId.value || undefined,
    historyMessages: conversationHistory.value.length > 0
      ? conversationHistory.value.slice(-20) // 最多带 20 条历史消息
      : undefined
  }
}

async function sendMessage() {
  const question = input.value.trim()
  if (!question || isLoading.value) return

  if (!knowledgeBaseId.value) {
    alert('请输入知识库 ID')
    return
  }

  // 生成当前会话 ID（首次对话时生成）
  if (!conversationId.value) {
    conversationId.value = generateId()
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
      buildRequestPayload(question),
      (chunk: string) => {
        assistantMsg.content += chunk
        scrollToBottom()
      },
      () => {
        assistantMsg.timestamp = Date.now()
        // 保存到对话历史
        conversationHistory.value.push({ role: 'user', content: question })
        conversationHistory.value.push({ role: 'assistant', content: assistantMsg.content })
      },
      (event) => {
        if (event.type === 'sources') {
          assistantMsg.sources = event.data as DocumentSource[]
        } else if (event.type === 'usage') {
          assistantMsg.tokenUsage = event.data as TokenUsage
        }
      }
    )
  } catch {
    assistantMsg.content += '\n\n[请求失败，请确保后端服务已启动]'
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 重置对话
function resetConversation() {
  messages.value = []
  conversationHistory.value = []
  conversationId.value = ''
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 顶栏 -->
    <header class="h-16 glass-effect border-b border-white/5 flex items-center px-8 gap-4 relative z-10">
      <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500/20 to-accent/20 flex items-center justify-center">
        <Sparkles class="w-4 h-4 text-primary-400" />
      </div>
      <h1 class="text-white font-semibold text-base">智能问答</h1>
      <span class="text-xs text-white/30 bg-white/5 px-2 py-0.5 rounded-md">RAG</span>
      <div class="flex-1" />
      <input
        v-model="knowledgeBaseId"
        placeholder="输入知识库 ID"
        class="bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white
               placeholder-white/25 focus:outline-none focus:border-primary-500/40 w-56
               transition-all duration-300"
      />
      <button
        @click="showAdvancedOptions = !showAdvancedOptions"
        :class="['flex items-center gap-1.5 text-xs px-3 py-2 rounded-xl transition-all duration-300',
                 showAdvancedOptions ? 'bg-primary-500/20 text-primary-300 border border-primary-500/30' 
                 : 'text-white/40 hover:text-white/70 border border-white/10']"
      >
        <Sliders class="w-3.5 h-3.5" />
        高级选项
      </button>
      <button
        @click="resetConversation"
        class="text-xs text-white/40 hover:text-white/70 transition-colors duration-200 px-2"
        title="重置对话"
      >
        重置
      </button>
    </header>

    <!-- 高级选项面板 -->
    <div v-show="showAdvancedOptions" 
         class="glass-effect border-b border-white/5 px-8 py-4 flex flex-wrap items-center gap-6 animate-slide-down">
      <!-- 查询扩展 -->
      <label class="flex items-center gap-2 cursor-pointer">
        <input type="checkbox" v-model="enableQueryExpansion" class="w-4 h-4 rounded accent-primary-500" />
        <span class="text-sm text-white/70">查询扩展</span>
        <span class="text-xs text-white/30">(多查询变体)</span>
      </label>

      <!-- 重排序 -->
      <label class="flex items-center gap-2 cursor-pointer">
        <input type="checkbox" v-model="enableRerank" class="w-4 h-4 rounded accent-primary-500" />
        <span class="text-sm text-white/70">重排序</span>
        <span class="text-xs text-white/30">(需 Cohere API)</span>
      </label>

      <!-- TopK -->
      <div class="flex items-center gap-2">
        <span class="text-sm text-white/70">TopK:</span>
        <input type="range" v-model.number="topK" min="1" max="20" 
               class="w-24 h-1.5 accent-primary-500" />
        <span class="text-xs text-white/50 w-6 text-center">{{ topK }}</span>
      </div>

      <!-- 会话 ID -->
      <div class="flex items-center gap-2">
        <span class="text-sm text-white/50">会话:</span>
        <span class="text-xs text-white/30 font-mono">{{ conversationId || '自动生成' }}</span>
      </div>

      <!-- 历史轮数 -->
      <span class="text-xs text-white/30">
        历史: {{ conversationHistory.length / 2 }} 轮
      </span>
    </div>

    <!-- 消息列表 -->
    <div ref="chatContainer" class="flex-1 overflow-y-auto scrollbar-thin py-6">
      <!-- 空状态 -->
      <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full">
        <div class="w-20 h-20 rounded-3xl bg-gradient-to-br from-primary-500/10 to-accent/10 flex items-center justify-center mb-6 shadow-glow">
          <Sparkles class="w-10 h-10 text-primary-400/60" />
        </div>
        <p class="text-white/50 text-lg font-medium">开始 AI 对话</p>
        <p class="text-white/25 text-sm mt-2">输入知识库 ID，然后开始提问</p>
        <div class="flex gap-3 mt-8">
          <div class="glass-effect rounded-xl px-4 py-2.5 text-xs text-white/30">
            <Zap class="w-3 h-3 inline mr-1" />查询扩展
          </div>
          <div class="glass-effect rounded-xl px-4 py-2.5 text-xs text-white/30">
            展示引用来源
          </div>
          <div class="glass-effect rounded-xl px-4 py-2.5 text-xs text-white/30">
            多轮对话记忆
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