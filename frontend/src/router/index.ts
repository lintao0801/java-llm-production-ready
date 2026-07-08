import { createRouter, createWebHistory } from 'vue-router'
import ChatPage from '@/pages/ChatPage.vue'
import KnowledgePage from '@/pages/KnowledgePage.vue'
import SimpleChatPage from '@/pages/SimpleChatPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/chat'
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatPage
    },
    {
      path: '/knowledge',
      name: 'knowledge',
      component: KnowledgePage
    },
    {
      path: '/simple-chat',
      name: 'simple-chat',
      component: SimpleChatPage
    }
  ]
})

export default router
