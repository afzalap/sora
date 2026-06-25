<script setup lang="ts">
import { ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AuthView from '@/views/AuthView.vue'
import AppShell from '@/views/AppShell.vue'

const auth = useAuthStore()

// Theme is managed here so it applies to the root element.
const theme = ref<'light' | 'dark'>('dark')

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
}

// Keep <html> in sync so CSS vars in :root pick up the right values.
watch(
  theme,
  (t) => {
    document.documentElement.setAttribute('data-theme', t)
  },
  { immediate: true },
)
</script>

<template>
  <!-- data-theme on the root div drives all CSS variable swaps -->
  <div :data-theme="theme" class="app-root">
    <AuthView v-if="!auth.isLoggedIn" />
    <AppShell v-else :theme="theme" @toggle-theme="toggleTheme" />
  </div>
</template>

<style scoped>
.app-root {
  height: 100dvh;
  width: 100%;
  background: var(--bg);
  color: var(--ink);
  overflow: hidden;
}
</style>
