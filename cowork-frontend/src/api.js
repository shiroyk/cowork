import axios from 'axios'
import router from '@/router'
import store from '@/store'

if (process.env.NODE_ENV == 'development') {
  axios.defaults.baseURL = 'http://localhost:8079/';
} else if (process.env.NODE_ENV == 'production') {
  axios.defaults.baseURL = 'http://localhost/api/';
}

axios.defaults.timeout = 5000
axios.defaults.retry = 2
axios.defaults.retryDelay = 100

axios.interceptors.request.use(
  config => {
    const token = store.state.accessToken;

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config;
  },
  err => {
    return Promise.reject(err);
  }
);

axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response.status == 401) {
      if (router.currentRoute.path != '/' && router.currentRoute.path != '/login') {
        store.commit("removeAccessToken")
        if (store.state.refreshToken) {
          axios
            .post('/oauth/token', new URLSearchParams({
              grant_type: 'refresh_token',
              client_id: 'cowork',
              client_secret: 'cowork',
              refresh_token: store.state.refreshToken
            }), {
              auth: {
                username: 'cowork',
                password: 'cowork',
              },
            })
            .then(res => {
              if (res.data.code === 200) {
                store.commit('updateAccessToken', res.data.data.access_token)
                store.commit('updateRefreshToken', res.data.data.refresh_token)
              }
            })
            .catch(err => console.log(err))
        } else {
          setTimeout(() => {
            router.push('/', () => { })
          }, 1000)
        }
      }
    }
    return Promise.reject(error)
  })

export default axios
