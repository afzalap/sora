<script setup lang="ts">
import { watch } from 'vue'
import { useBookingsStore } from '@/stores/bookings'
import BookingCard from './BookingCard.vue'

const props = defineProps<{ open: boolean }>()
defineEmits<{ close: [] }>()

const store = useBookingsStore()

// Refresh bookings whenever the drawer opens.
watch(() => props.open, (isOpen) => {
  if (isOpen) store.fetch()
})
</script>

<template>
  <div :class="['drawer', open && 'drawer--open']">

    <!-- Header -->
    <div class="drawer-header">
      <span class="drawer-title">My bookings</span>
      <span class="drawer-count mono">{{ store.confirmedCount }} confirmed</span>
      <button class="close-btn" @click="$emit('close')">✕</button>
    </div>

    <!-- Sub-info -->
    <div class="drawer-info">
      Confirmed &amp; cancelled trips only.
      Flights selected but not confirmed stay in
      <span class="accent-text">Needs a decision</span>
      in the sidebar.
    </div>

    <!-- Bookings list -->
    <div class="drawer-body">
      <div v-if="store.loading" class="empty-state">Loading…</div>

      <div v-else-if="store.bookings.length === 0" class="empty-state">
        No bookings yet. Ask Sora to search and book a flight.
      </div>

      <template v-else>
        <BookingCard
          v-for="booking in store.bookings"
          :key="booking.id"
          :booking="booking"
        />
      </template>
    </div>

  </div>
</template>

<style scoped>
.drawer {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 380px;
  max-width: 88vw;
  background: var(--surface);
  border-left: 1px solid var(--line);
  box-shadow: -14px 0 40px -24px rgba(0, 0, 0, 0.4);
  z-index: 9;
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.32s cubic-bezier(0.4, 0, 0.2, 1);
}

.drawer--open {
  transform: translateX(0);
}

.drawer-header {
  height: 60px;
  flex: none;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--line);
  gap: 10px;
}

.drawer-title {
  font-size: 15px;
  font-weight: 600;
}

.drawer-count {
  font-size: 12px;
  color: var(--ink-3);
  margin-left: auto;
}

.close-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: var(--surface-2);
  border-radius: 8px;
  color: var(--ink-2);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.close-btn:hover {
  background: var(--line);
}

.drawer-info {
  padding: 12px 20px;
  flex: none;
  border-bottom: 1px solid var(--line);
  font-size: 12px;
  color: var(--ink-3);
  line-height: 1.5;
}

.accent-text {
  color: var(--accent-soft-ink);
  font-weight: 500;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  font-size: 13.5px;
  color: var(--ink-3);
  text-align: center;
  padding: 32px 16px;
  line-height: 1.6;
}
</style>
