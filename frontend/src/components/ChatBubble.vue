<script setup lang="ts">
import type { Message } from '@/types'
import { User, Bot } from 'lucide-vue-next'

defineProps<{
  message: Message
}>()
</script>

<template>
  <div
    class="flex gap-4 px-6 py-3 animate-fade-in"
    :class="message.role === 'user' ? 'flex-row-reverse' : 'flex-row'"
  >
    <!-- 头像 -->
    <div
      class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 transition-all duration-300"
      :class="message.role === 'user'
        ? 'bg-gradient-to-br from-primary-500 to-accent shadow-glow'
        : 'glass-effect'"
    >
      <User v-if="message.role === 'user'" class="w-5 h-5 text-white" />
      <Bot v-else class="w-5 h-5 text-primary-400" />
    </div>

    <!-- 消息内容 -->
    <div class="max-w-[70%] flex flex-col" :class="message.role === 'user' ? 'items-end' : 'items-start'">
      <div
        class="rounded-2xl px-5 py-3.5 relative overflow-hidden"
        :class="message.role === 'user'
          ? 'bg-gradient-to-br from-primary-500/20 to-accent/10 border border-primary-500/20'
          : 'glass-effect'"
      >
        <p class="text-sm leading-relaxed whitespace-pre-wrap text-white/90">{{ message.content }}</p>
      </div>
      <span class="text-[11px] text-white/25 mt-1.5 px-2">
        {{ new Date(message.timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }}
      </span>
    </div>
  </div>
</template>
