import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { configureApi } from './services/api'
import { useAuthStore } from './stores/auth'
import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const auth = useAuthStore()
configureApi({
  getToken: () => auth.token,
  onUnauthorized: () => {
    auth.clear()
    router.push({ name: 'login', query: { expired: '1' } })
  }
})

app.mount('#app')
