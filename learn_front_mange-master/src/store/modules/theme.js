const STORAGE_KEY = 'admin-theme'

function readStored() {
  try {
    const v = localStorage.getItem(STORAGE_KEY)
    return v === 'dark' ? 'dark' : 'light'
  } catch (e) {
    return 'light'
  }
}

function applyDom(mode) {
  if (typeof document === 'undefined') return
  document.documentElement.setAttribute('data-theme', mode)
}

export default {
  namespaced: true,
  state: {
    mode: readStored()
  },
  mutations: {
    setMode(state, mode) {
      if (mode !== 'light' && mode !== 'dark') return
      state.mode = mode
      try {
        localStorage.setItem(STORAGE_KEY, mode)
      } catch (e) {}
      applyDom(mode)
    }
  }
}
