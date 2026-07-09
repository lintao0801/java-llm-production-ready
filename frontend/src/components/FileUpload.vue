<script setup lang="ts">
import { ref } from 'vue'
import { Upload, FileText, X, CheckCircle, AlertCircle } from 'lucide-vue-next'
import { documentApi } from '@/api'

const props = defineProps<{
  knowledgeBaseId: string
}>()

const emit = defineEmits<{
  (e: 'uploaded'): void
}>()

const files = ref<File[]>([])
const uploading = ref(false)
const uploadStatus = ref<Map<string, 'pending' | 'success' | 'error'>>(new Map())

function onFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files) {
    const newFiles = Array.from(input.files)
    files.value.push(...newFiles)
    newFiles.forEach(f => uploadStatus.value.set(f.name, 'pending'))
  }
}

function removeFile(index: number) {
  const file = files.value[index]
  files.value.splice(index, 1)
  uploadStatus.value.delete(file.name)
}

async function uploadFiles() {
  if (files.value.length === 0) return
  uploading.value = true
  try {
    if (files.value.length === 1) {
      await documentApi.upload(files.value[0], props.knowledgeBaseId)
      uploadStatus.value.set(files.value[0].name, 'success')
    } else {
      await documentApi.uploadBatch(files.value, props.knowledgeBaseId)
      files.value.forEach(f => uploadStatus.value.set(f.name, 'success'))
    }
    emit('uploaded')
  } catch {
    files.value.forEach(f => {
      if (uploadStatus.value.get(f.name) === 'pending') {
        uploadStatus.value.set(f.name, 'error')
      }
    })
  } finally {
    uploading.value = false
  }
}

function clearFiles() {
  files.value = []
  uploadStatus.value.clear()
}
</script>

<template>
  <div class="space-y-5">
    <!-- 拖拽上传区域 -->
    <label
      class="block border border-dashed border-white/10 rounded-2xl p-10 text-center cursor-pointer
             hover:border-primary-500/30 hover:bg-primary-500/5 transition-all duration-300 group"
    >
      <input
        type="file"
        multiple
        class="hidden"
        @change="onFileSelect"
        accept=".pdf,.doc,.docx,.txt,.md"
      />
      <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-primary-500/10 to-accent/10 flex items-center justify-center mx-auto mb-4 group-hover:shadow-glow transition-all duration-300">
        <Upload class="w-7 h-7 text-primary-400" />
      </div>
      <p class="text-sm text-white/60 group-hover:text-white/80 transition-colors">点击或拖拽文件到此处上传</p>
      <p class="text-xs text-white/30 mt-2">支持 PDF、Word、TXT、Markdown 格式</p>
    </label>

    <!-- 文件列表 -->
    <div v-if="files.length > 0" class="space-y-3">
      <div class="flex items-center justify-between">
        <span class="text-sm text-white/50">已选择 {{ files.length }} 个文件</span>
        <button
          @click="clearFiles"
          class="text-xs text-white/30 hover:text-white/60 transition-colors"
        >
          清空
        </button>
      </div>

      <div class="space-y-1.5 max-h-48 overflow-y-auto scrollbar-thin">
        <div
          v-for="(file, index) in files"
          :key="index"
          class="flex items-center gap-3 glass-effect rounded-xl px-4 py-2.5 animate-slide-in"
        >
          <FileText class="w-4 h-4 text-primary-400/60 flex-shrink-0" />
          <span class="text-sm text-white/60 flex-1 truncate">{{ file.name }}</span>
          <CheckCircle v-if="uploadStatus.get(file.name) === 'success'" class="w-4 h-4 text-primary-400" />
          <AlertCircle v-else-if="uploadStatus.get(file.name) === 'error'" class="w-4 h-4 text-red-400" />
          <button
            v-else
            @click="removeFile(index)"
            class="text-white/20 hover:text-white/60 transition-colors"
          >
            <X class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- 上传按钮 -->
      <button
        @click="uploadFiles"
        :disabled="uploading"
        class="w-full py-3 rounded-xl bg-gradient-to-r from-primary-500 to-accent text-white font-medium text-sm
               hover:shadow-glow-lg transition-all duration-300
               disabled:opacity-40 disabled:cursor-not-allowed"
      >
        {{ uploading ? '上传中...' : '开始上传' }}
      </button>
    </div>
  </div>
</template>
