<script setup lang="ts">
import { computed } from 'vue'
import { useBookingsStore } from '@/stores/bookings'
import { features } from '@/config/features'
import type { BookingResponse } from '@/types'

const props = defineProps<{ booking: BookingResponse }>()

const store = useBookingsStore()

const confirmed = computed(() => props.booking.status === 'CONFIRMED')
const isConfirming = computed(() => store.cancelConfirmId === props.booking.id)
const opacity = computed(() => (confirmed.value ? '1' : '0.62'))

// Format "2026-07-15T11:30:00" → "Jul 15, 2026"
function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}
function fmtTime(iso: string) {
  return new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
}

const dateline = computed(() => {
  const dep = fmtDate(props.booking.departureTime)
  const depT = fmtTime(props.booking.departureTime)
  const arrT = fmtTime(props.booking.arrivalTime)
  return `${dep} · ${depT}–${arrT}`
})

const price = computed(() => {
  const symbol = props.booking.currency === 'JPY' ? '¥' : props.booking.currency + ' '
  return `${symbol}${Math.round(props.booking.price).toLocaleString()}`
})
</script>

<template>
  <div class="card" :style="{ opacity }">

    <!-- Main info -->
    <div class="card-body">
      <div class="card-top">
        <div class="airline-chip">{{ booking.carrier }}</div>
        <div class="airline-name">{{ booking.carrier }} · {{ booking.flightNumber }}</div>
        <div
          class="status-badge"
          :class="confirmed ? 'badge--good' : 'badge--warn'"
        >
          {{ confirmed ? 'Confirmed' : 'Cancelled' }}
        </div>
      </div>

      <div class="route">
        <span class="iata">{{ booking.origin }}</span>
        <span class="arrow">→</span>
        <span class="iata">{{ booking.destination }}</span>
        <span class="ref mono">{{ booking.bookingReference }}</span>
      </div>

      <div class="dateline">{{ dateline }}</div>
      <div class="price mono">{{ price }}</div>
    </div>

    <!-- Cancel confirmation step -->
    <div v-if="isConfirming" class="cancel-confirm">
      <p class="cancel-warn">Cancel this booking? This can't be undone.</p>
      <div class="cancel-actions">
        <button class="btn-warn" @click="store.confirmCancel(booking.id)">Yes, cancel</button>
        <button class="btn-ghost" @click="store.abortCancel()">Keep it</button>
      </div>
    </div>

    <!-- Footer actions -->
    <div v-if="!isConfirming" class="card-footer">
      <!-- Ask Sora (Phase 2 muted) -->
      <button
        :class="['ask-btn', !features.askSora && 'feature-muted']"
        :disabled="!features.askSora"
        :title="features.askSora ? 'Ask Sora about this booking' : 'Coming soon'"
      >
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M2 3.5h12v8H6l-3 2.5v-2.5H2z"/>
        </svg>
        Ask Sora
        <span v-if="!features.askSora" class="soon-mini">Soon</span>
      </button>

      <!-- Cancel (only for confirmed bookings) -->
      <button
        v-if="confirmed"
        class="cancel-btn"
        @click="store.startCancel(booking.id)"
      >
        Cancel booking
      </button>
    </div>

  </div>
</template>

<style scoped>
.card {
  border: 1px solid var(--line-2);
  border-radius: 13px;
  background: var(--surface);
  overflow: hidden;
  transition: opacity 0.2s;
}

.card-body {
  padding: 15px 16px;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 13px;
}

.airline-chip {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  background: var(--accent);
  color: var(--accent-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'IBM Plex Mono', monospace;
  font-weight: 600;
  font-size: 11px;
}

.airline-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--ink);
}

.status-badge {
  margin-left: auto;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 20px;
}

.badge--good {
  background: var(--good-soft);
  color: var(--good-ink);
}

.badge--warn {
  background: var(--warn-soft);
  color: var(--warn-ink);
}

.route {
  display: flex;
  align-items: baseline;
  gap: 9px;
  font-family: 'IBM Plex Mono', monospace;
  font-weight: 600;
  color: var(--ink);
}

.iata {
  font-size: 18px;
}

.arrow {
  color: var(--ink-3);
}

.ref {
  margin-left: auto;
  font-size: 12px;
  font-weight: 500;
  color: var(--ink-3);
}

.dateline {
  font-size: 12px;
  color: var(--ink-2);
  margin-top: 7px;
}

.price {
  font-size: 13px;
  color: var(--ink-2);
  margin-top: 4px;
}

/* Cancel confirmation */
.cancel-confirm {
  border-top: 1px solid var(--line);
  padding: 12px 16px;
  background: var(--warn-soft);
}

.cancel-warn {
  font-size: 12.5px;
  color: var(--warn-ink);
  font-weight: 500;
  margin: 0 0 10px;
}

.cancel-actions {
  display: flex;
  gap: 8px;
}

.btn-warn {
  flex: 1;
  border: none;
  background: var(--warn);
  color: #fff;
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 12.5px;
  font-weight: 600;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-ghost {
  flex: 1;
  border: 1px solid var(--line-2);
  background: var(--surface);
  color: var(--ink-2);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 12.5px;
  font-weight: 500;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
}

/* Footer actions */
.card-footer {
  border-top: 1px solid var(--line);
  padding: 9px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ask-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 1px solid var(--line-2);
  background: var(--surface);
  color: var(--ink-2);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 12.5px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.ask-btn:not(.feature-muted):hover {
  border-color: var(--accent);
  color: var(--accent-soft-ink);
}

.soon-mini {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--accent-soft-ink);
  background: var(--accent-soft);
  padding: 2px 6px;
  border-radius: 4px;
}

.cancel-btn {
  margin-left: auto;
  border: 1px solid var(--line-2);
  background: var(--surface);
  color: var(--ink-2);
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 12.5px;
  font-weight: 500;
  padding: 6px 13px;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.cancel-btn:hover {
  border-color: var(--warn);
  color: var(--warn-ink);
}
</style>
