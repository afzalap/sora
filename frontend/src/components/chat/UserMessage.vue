<script setup lang="ts">
defineProps<{ content: string }>()
</script>

<template>
  <div class="user-row fade-up">
    <div class="bubble">{{ content }}</div>
  </div>
</template>

<style scoped>
.user-row {
  display: flex;
  justify-content: flex-end;
}

.bubble {
  max-width: 78%;
  background: var(--user-bubble);
  color: var(--ink);
  padding: 11px 15px;
  border-radius: 16px 16px 4px 16px;
  font-size: 14.5px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
