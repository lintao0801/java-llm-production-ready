<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessageSquare, Database, MessageCircle, Sparkles } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const menuItems = [
  { path: '/chat', label: '智能问答', icon: MessageSquare, desc: 'RAG 知识库问答' },
  { path: '/knowledge', label: '知识库管理', icon: Database, desc: '管理文档和知识库' },
  { path: '/simple-chat', label: '简单对话', icon: MessageCircle, desc: '直接 AI 对话' }
]

const isActive = (path: string) => computed(() => route.path === path)
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-gradient-dark">
    <!-- 侧边导航 -->
    <aside class="w-72 glass-effect flex flex-col relative">
      <!-- 装饰性背景 -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-20 -left-20 w-40 h-40 bg-primary-500/10 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-20 -right-20 w-40 h-40 bg-accent/10 rounded-full blur-3xl"></div>
      </div>

      <!-- Logo -->
      <div class="h-20 flex items-center px-6 border-b border-white/5 relative z-10">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-primary flex items-center justify-center shadow-glow animate-glow">
            <Sparkles class="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 class="text-white font-bold text-lg">智能知识库</h1>
            <p class="text-xs text-white/40">AI Knowledge Base</p>
          </div>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 py-6 px-4 relative z-10">
        <div class="space-y-2">
          <button
              v-for="item in menuItems"
              :key="item.path"
              @click="router.push(item.path)"
              class="w-full flex items-start gap-4 px-4 py-4 rounded-xl text-left transition-all duration-300 group"
              :class="isActive(item.path).value
              ? 'bg-gradient-to-r from-primary-500/20 to-accent/20 shadow-glow border border-primary-500/30'
              : 'hover:bg-white/5 border border-transparent'"
          >
            <div
                class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 transition-all duration-300"
                :class="isActive(item.path).value
                ? 'bg-gradient-primary shadow-glow'
                : 'bg-white/5 group-hover:bg-white/10'"
            >
              <component :is="item.icon" class="w-5 h-5" :class="isActive(item.path).value ? 'text-white' : 'text-white/60'" />
            </div>
            <div class="flex-1 min-w-0">
              <div
                  class="font-medium text-sm mb-1 transition-colors"
                  :class="isActive(item.path).value ? 'text-white' : 'text-white/70 group-hover:text-white'"
              >
                {{ item.label }}
              </div>
              <div class="text-xs text-white/40 truncate">{{ item.desc }}</div>
            </div>
          </button>
        </div>
      </nav>

      <!-- 底部信息 -->
      <div class="p-6 border-t border-white/5 relative z-10">
        <div class="text-center">
          <div class="text-xs text-white/30 mb-1">RAG Knowledge Base</div>
          <div class="text-xs text-white/20">v1.0.0</div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 overflow-hidden relative">
      <!-- 装饰性背景 -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute top-0 right-0 w-96 h-96 bg-primary-500/5 rounded-full blur-3xl"></div>
        <div class="absolute bottom-0 left-0 w-96 h-96 bg-accent/5 rounded-full blur-3xl"></div>
      </div>
      <div class="relative z-10 h-full">
        <router-view />
      </div>
    </main>
  </div>
</template>
