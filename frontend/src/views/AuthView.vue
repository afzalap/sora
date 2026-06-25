<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const tab = ref<'in' | 'up'>('in')
const username = ref('')
const password = ref('')

const title = { in: 'Welcome back', up: 'Create your account' }
const subtitle = {
  in: 'Sign in to chat with Sora and manage your trips.',
  up: 'A few seconds and you\'re booking by chat.',
}
const cta = { in: 'Sign in', up: 'Create account' }

async function submit() {
  if (!username.value || !password.value) return
  try {
    if (tab.value === 'in') {
      await auth.login(username.value, password.value)
    } else {
      await auth.register(username.value, password.value)
    }
  } catch {
    // error shown via auth.error
  }
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Enter') submit()
}
</script>

<template>
  <div class="auth-bg">
    <div class="auth-card-wrap">

      <!-- Logo -->
      <div class="auth-logo">
        <div class="logo-mark">S</div>
        <span class="logo-name">Sora</span>
      </div>

      <!-- Card -->
      <div class="auth-card">
        <div class="auth-heading">{{ title[tab] }}</div>
        <div class="auth-sub">{{ subtitle[tab] }}</div>

        <!-- Tab switcher -->
        <div class="tab-bar">
          <button :class="['tab-btn', tab === 'in' && 'tab-btn--active']" @click="tab = 'in'">
            Sign in
          </button>
          <button :class="['tab-btn', tab === 'up' && 'tab-btn--active']" @click="tab = 'up'">
            Create account
          </button>
        </div>

        <!-- Fields -->
        <label class="field-label">Username</label>
        <input
          v-model="username"
          type="text"
          class="field-input"
          placeholder="e.g. alice"
          autocomplete="username"
          @keydown="onKey"
        />

        <label class="field-label">Password</label>
        <input
          v-model="password"
          type="password"
          class="field-input"
          placeholder="••••••••"
          autocomplete="current-password"
          @keydown="onKey"
        />

        <!-- Error -->
        <div v-if="auth.error" class="auth-error">{{ auth.error }}</div>

        <!-- Submit -->
        <button class="auth-submit" :disabled="auth.loading" @click="submit">
          <span v-if="auth.loading">…</span>
          <span v-else>{{ cta[tab] }}</span>
        </button>

        <div class="auth-footer">
          Demo only · no real account, payment, or ticketing.
          <br />Try: <code>alice</code> / <code>password</code>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-bg {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(120% 90% at 80% -10%, var(--accent-soft), transparent 60%);
}

.auth-card-wrap {
  width: 100%;
  max-width: 400px;
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 11px;
  justify-content: center;
  margin-bottom: 30px;
}

.logo-mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--accent);
  color: var(--accent-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'IBM Plex Mono', monospace;
  font-weight: 700;
  font-size: 16px;
}

.logo-name {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.auth-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: var(--shadow);
  padding: 30px 28px;
}

.auth-heading {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.auth-sub {
  font-size: 13.5px;
  color: var(--ink-2);
  margin-bottom: 22px;
}

.tab-bar {
  display: flex;
  background: var(--surface-2);
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 3px;
  margin-bottom: 20px;
}

.tab-btn {
  flex: 1;
  border: none;
  border-radius: 8px;
  padding: 9px;
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  background: transparent;
  color: var(--ink-3);
  transition: background 0.15s, color 0.15s;
}

.tab-btn--active {
  background: var(--surface);
  color: var(--ink);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.field-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--ink-2);
  margin-bottom: 6px;
}

.field-input {
  display: block;
  width: 100%;
  height: 42px;
  padding: 0 13px;
  border: 1px solid var(--line-2);
  border-radius: 10px;
  background: var(--surface);
  color: var(--ink);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 14px;
  margin-bottom: 14px;
  outline: none;
  transition: border-color 0.15s;
}

.field-input:focus {
  border-color: var(--accent);
}

.auth-error {
  font-size: 13px;
  color: var(--warn-ink);
  background: var(--warn-soft);
  border: 1px solid var(--warn);
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 14px;
}

.auth-submit {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 11px;
  background: var(--accent);
  color: var(--accent-ink);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s;
}

.auth-submit:disabled {
  opacity: 0.6;
  cursor: default;
}

.auth-footer {
  text-align: center;
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 16px;
  line-height: 1.6;
}

.auth-footer code {
  font-family: 'IBM Plex Mono', monospace;
  color: var(--ink-2);
}
</style>
