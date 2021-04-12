import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Login",
    component: () => import("../views/Login.vue"),
  },
  {
    path: "/dashboard",
    component: () => import("../views/Dashboard.vue"),
    children: [
      {
        path: '',
        component: () => import("../views/DashHome.vue"),
      },
      {
        path: 'user',
        component: () => import("../views/User.vue"),
      },
      {
        path: 'group',
        component: () => import("../views/Group.vue"),
      },
      {
        path: 'document',
        component: () => import("../views/Document.vue"),
      }
    ]
  },
  {
    path: "/about",
    name: "About",
    component: () => import("../views/About.vue"),
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
