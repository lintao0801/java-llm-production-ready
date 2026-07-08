import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { KnowledgeBase } from '@/types'

export const useKnowledgeStore = defineStore('knowledge', () => {
  const knowledgeBases = ref<KnowledgeBase[]>([])
  const currentKnowledgeBase = ref<KnowledgeBase | null>(null)

  function setKnowledgeBases(list: KnowledgeBase[]) {
    knowledgeBases.value = list
  }

  function setCurrentKnowledgeBase(kb: KnowledgeBase | null) {
    currentKnowledgeBase.value = kb
  }

  function addKnowledgeBase(kb: KnowledgeBase) {
    knowledgeBases.value.push(kb)
  }

  function removeKnowledgeBase(id: string) {
    knowledgeBases.value = knowledgeBases.value.filter(kb => kb.id !== id)
    if (currentKnowledgeBase.value?.id === id) {
      currentKnowledgeBase.value = null
    }
  }

  return {
    knowledgeBases,
    currentKnowledgeBase,
    setKnowledgeBases,
    setCurrentKnowledgeBase,
    addKnowledgeBase,
    removeKnowledgeBase
  }
})
