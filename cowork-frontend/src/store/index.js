import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

function setObject(key, item) {
  localStorage.setItem(key, JSON.stringify(item))
}

function getObject(key) {
  let val = localStorage.getItem(key)
  return val ? JSON.parse(val) : null
}

export default new Vuex.Store({
  state: {
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    userInfo: getObject('userInfo'),
    currentGroup: null
  },
  mutations: {
    updateAccessToken(state, newToken) {
      localStorage.setItem('accessToken', newToken);
      state.accessToken = newToken;
    },
    removeAccessToken(state) {
      localStorage.removeItem('accessToken');
      state.accessToken = null;
    },
    updateRefreshToken(state, newToken) {
      localStorage.setItem('refreshToken', newToken);
      state.refreshToken = newToken;
    },
    removeRefreshToken(state) {
      localStorage.removeItem('refreshToken');
      state.refreshToken = null;
    },
    updateUserInfo(state, info) {
      setObject('userInfo', info)
      state.userInfo = info
    },
    removeUserInfo(state) {
      localStorage.removeItem('userInfo');
      state.userInfo = null;
    },
    setCurrentGroup(state, gid) {
      state.currentGroup = gid
    }
  },
  actions: {
  },
  modules: {
  }
})