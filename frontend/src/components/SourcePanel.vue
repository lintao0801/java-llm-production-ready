<script setup lang="ts">
import { ref } from 'vue'
import type { DocumentSource } from '@/types'
import { FileText, ChevronDown, ChevronUp, Link2 } from 'lucide-vue-next'

defineProps<{
  sources: DocumentSource[]
}>()

const expanded = ref(false)
</script>

<template>
  <div v-if="sources.length > 0" class="mx-6 mb-3 animate-fade-in">
    <button
        @click="expanded = !expanded"
        class="flex items-center gap-2 text-xs text-white/40 hover:text-primary-400 transition-colors duration-200 group"
    >
      <div class="w-6 h-6 rounded-md bg-primary-500/10 flex items-center justify-center group-hover:bg-primary-500/20 transition-colors">
        <Link2 class="w-3 h-3" />
      </div>
      <span>引用来源 ({{ sources.length }})</span>
      <ChevronDown v-if="!expanded" class="w-3 h-3 transition-transform" />
      <ChevronUp v-else class="w-3 h-3 transition-transform" />
    </button>

    <div v-if="expanded" class="mt-3 space-y-2 animate-fade-in">
      <div
          v-for="(source, index) in sources"
          :key="index"
          class="glass-effect rounded-xl p-4 hover:border-primary-500/20 transition-all duration-300"
      >
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2">
            <FileText class="w-3.5 h-3.5 text-primary-400" />
            <span class="text-xs text-primary-300 font-medium">{{ source.fileName }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <div class="h-1.5 w-16 bg-white/5 rounded-full overflow-hidden">
              <div
                  class="h-full bg-gradient-to-r from-primary-500 to-accent rounded-full"
                  :style="{ width: `${source.similarityScore * 100}%` }"
              />
            </div>
            <span class="text-[11px] text-white/30 font-mono">
              {{ (source.similarityScore * 100).toFixed(1) }}%
            </span>
          </div>
        </div>
        <p class="text-xs text-white/50 leading-relaxed line-clamp-3">
          {{ source.content }}
        </p>
      </div>
    </div>
  </div>
</template>
