import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import("@/views/Home.vue"),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import("@/views/Login.vue"),
  },
  {
    path: '/user',
    name: 'User',
    component: () => import("@/views/UserPage.vue"),
  },
  {
    path: '/document',
    component: () => import("@/views/Document.vue"),
    children: [
      {
        path: '',
        component: () => import("@/views/MyDoc.vue"),
      },
      {
        path: 'collection',
        component: () => import("@/views/MyCollection.vue"),
      },
      {
        path: 'group/:gid',
        component: () => import("@/views/GroupDoc.vue"),
        props: true
      },
      {
        path: 'trash',
        component: () => import("@/views/TrashDoc.vue"),
      },
    ]
  },
  {
    path: '/doc/:docId',
    name: 'Doc',
    component: () => import("@/views/DocEditor.vue"),
    props: true,
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
