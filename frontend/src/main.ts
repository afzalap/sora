import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './style.css'

// Pinia is Vue's official state-management library.
// createPinia() creates the store registry; we install it on the app
// so every component can call useXxxStore() without importing the registry each time.
const app = createApp(App)
app.use(createPinia())
app.mount('#app')
