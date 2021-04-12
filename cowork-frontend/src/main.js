import Vue from 'vue'
import App from '@/App.vue'
import router from '@/router'
import axios from '@/api'
import store from '@/store'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import { format } from 'date-fns'

Vue.use(ElementUI)
Vue.prototype.$axios = axios
Vue.config.productionTip = false

Vue.filter('dateFormat', (value, formatStr = 'MM-dd HH:mm') => {
  return format(value, formatStr)
})

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
