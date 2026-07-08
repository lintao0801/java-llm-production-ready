<script setup lang="ts">
import { ref } from 'vue'
import { Database, Trash2, Plus, FolderOpen, Upload } from 'lucide-vue-next'
import FileUpload from '@/components/FileUpload.vue'
import { knowledgeBaseApi } from '@/api'
import { useKnowledgeStore } from '@/stores/knowledge'

const store = useKnowledgeStore()
const selectedKbId = ref<string>('')
const showUpload = ref(false)
const showDeleteConfirm = ref(false)
const deleteTargetId = ref('')

function selectKnowledgeBase(id: string) {
  selectedKbId.value = id
  showUpload.value = true
}

function confirmDelete(id: string) {
  deleteTargetId.value = id
  showDeleteConfirm.value = true
}

async function handleDelete() {
  try {
    await knowledgeBaseApi.delete(deleteTargetId.value)
    store.removeKnowledgeBase(deleteTargetId.value)
  } catch {
    alert('删除失败')
  }
  showDeleteConfirm.value = false
}

function onUploaded() {
  showUpload.value = false
  selectedKbId.value = ''
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 顶部栏 -->
    <header class="h-16 glass-effect border-b border-white/5 flex items-center px-8 gap-4 relative z-10">
      <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500/20 to-accent/20 flex items-center justify-center">
        <Database class="w-4 h-4 text-primary-400" />
      </div>
      <h1 class="text-white font-semibold text-base">知识库管理</h1>
    </header>

    <div class="flex-1 overflow-y-auto scrollbar-thin p-8">
      <!-- 操作区 -->
      <div class="glass-effect rounded-2xl p-6 mb-8">
        <div class="flex items-center gap-3 mb-4">
          <Upload class="w-5 h-5 text-primary-400" />
          <h3 class="text-white font-medium">上传文档</h3>
        </div>
        <div class="flex gap-4">
          <input
            v-model="selectedKbId"
            placeholder="请输入知识库 ID"
            class="flex-1 bg-white/5 border border-white/10 rounded-xl px-5 py-3 text-sm text-white
                   placeholder-white/20 focus:outline-none focus:border-primary-500/40 transition-all duration-300"
          />
          <button
            @click="showUpload = !showUpload"
            :disabled="!selectedKbId"
            class="px-6 py-3 rounded-xl bg-gradient-to-r from-primary-500 to-accent text-white font-medium text-sm
                   hover:shadow-glow-lg transition-all duration-300
                   disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <Plus class="w-4 h-4" />
            上传文档
          </button>
        </div>

        <!-- 上传面板 -->
        <div v-if="showUpload && selectedKbId" class="mt-6 pt-6 border-t border-white/5 animate-fade-in">
          <FileUpload :knowledge-base-id="selectedKbId" @uploaded="onUploaded" />
        </div>
      </div>

      <!-- 知识库列表 -->
      <div>
        <h3 class="text-sm text-white/40 mb-4 font-medium uppercase tracking-wider">知识库列表</h3>
        <div
          v-if="store.knowledgeBases.length === 0"
          class="flex flex-col items-center justify-center py-20"
        >
          <div class="w-20 h-20 rounded-3xl bg-white/5 flex items-center justify-center mb-6">
            <FolderOpen class="w-10 h-10 text-white/15" />
          </div>
          <p class="text-white/30 text-base">暂无知识库</p>
          <p class="text-white/15 text-sm mt-2">输入知识库 ID 并上传文档来创建</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="kb in store.knowledgeBases"
            :key="kb.id"
            class="glass-effect rounded-2xl p-6 hover:border-primary-500/20 transition-all duration-300 group cursor-pointer"
            @click="selectKnowledgeBase(kb.id)"
          >
            <div class="flex items-start justify-between mb-4">
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-primary-500/10 to-accent/10 flex items-center justify-center">
                <Database class="w-5 h-5 text-primary-400/60" />
              </div>
              <button
                @click.stop="confirmDelete(kb.id)"
                class="text-white/10 hover:text-red-400 transition-all duration-300 opacity-0 group-hover:opacity-100"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
            <h4 class="text-white font-medium mb-1">{{ kb.name }}</h4>
            <p v-if="kb.description" class="text-xs text-white/30 mb-3">{{ kb.description }}</p>
            <div class="text-[11px] text-white/20 font-mono">ID: {{ kb.id }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div
      v-if="showDeleteConfirm"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 animate-fade-in"
      @click.self="showDeleteConfirm = false"
    >
      <div class="glass-effect rounded-2xl p-8 w-[400px] shadow-glow-lg">
        <h3 class="text-white font-semibold text-lg mb-2">确认删除</h3>
        <p class="text-sm text-white/40 mb-8">确定要删除这个知识库吗？此操作不可撤销。</p>
        <div class="flex justify-end gap-3">
          <button
            @click="showDeleteConfirm = false"
            class="px-5 py-2.5 rounded-xl text-sm text-white/50 hover:text-white glass-effect transition-colors"
          >
            取消
          </button>
          <button
            @click="handleDelete"
            class="px-5 py-2.5 rounded-xl bg-red-500/20 text-red-400 text-sm font-medium
                   hover:bg-red-500/30 transition-colors border border-red-500/20"
          >
            确认删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
