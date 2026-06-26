<script setup lang="ts">
import type { FlightOption } from '@/types'
import { useChatStore } from '@/stores/chat'

const props = defineProps<{ flight: FlightOption; index: number }>()

const chat = useChatStore()

function fmt(iso: string) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function fmtDuration(mins: number) {
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return m ? `${h}h ${m}m` : `${h}h`
}

function select() {
  chat.send(
    `Book option ${props.index + 1}: ${props.flight.carrier} ${props.flight.flightNumber} ` +
    `${props.flight.origin}→${props.flight.destination} ` +
    `dep ${fmt(props.flight.departureTime)} ` +
    `${props.flight.price} ${props.flight.currency}`,
  )
}
</script>

<template>
  <div class="flight-card">
    <!-- Left: airline badge + flight number -->
    <div class="badge-col">
      <div class="carrier">{{ flight.carrier }}</div>
      <div class="flight-num">{{ flight.flightNumber }}</div>
      <div v-if="flight.stops === 0" class="stops direct">Direct</div>
      <div v-else class="stops">{{ flight.stops }} stop{{ flight.stops > 1 ? 's' : '' }}</div>
    </div>

    <!-- Tear line -->
    <div class="tear" />

    <!-- Middle: route + times -->
    <div class="route-col">
      <div class="times">
        <span class="time">{{ fmt(flight.departureTime) }}</span>
        <span class="arrow">→</span>
        <span class="time">{{ fmt(flight.arrivalTime) }}</span>
      </div>
      <div class="codes">
        <span>{{ flight.origin }}</span>
        <span class="dur">{{ fmtDuration(flight.durationMinutes) }}</span>
        <span>{{ flight.destination }}</span>
      </div>
    </div>

    <!-- Right: price + button -->
    <div class="price-col">
      <div class="price">{{ flight.currency }} {{ flight.price.toLocaleString() }}</div>
      <button class="select-btn" @click="select">Select</button>
    </div>
  </div>
</template>

<style scoped>
.flight-card {
  display: flex;
  align-items: center;
  gap: 0;
  background: var(--surface-2);
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
  font-size: 13px;
}

/* Badge column */
.badge-col {
  flex: none;
  width: 90px;
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 3px;
  background: var(--surface);
  border-right: none;
}

.carrier {
  font-weight: 700;
  font-size: 13.5px;
  color: var(--ink);
  font-family: 'IBM Plex Mono', monospace;
}

.flight-num {
  font-size: 11.5px;
  color: var(--ink-2);
  font-family: 'IBM Plex Mono', monospace;
}

.stops {
  font-size: 11px;
  color: var(--ink-2);
  margin-top: 4px;
}

.stops.direct {
  color: #34c759;
}

/* Dashed tear perforation */
.tear {
  flex: none;
  width: 1px;
  align-self: stretch;
  background: repeating-linear-gradient(
    to bottom,
    var(--line) 0,
    var(--line) 5px,
    transparent 5px,
    transparent 10px
  );
  margin: 0 1px;
}

/* Route column */
.route-col {
  flex: 1;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.times {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: var(--ink);
  font-family: 'IBM Plex Mono', monospace;
}

.arrow {
  color: var(--ink-2);
  font-weight: 400;
}

.codes {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: var(--ink-2);
  max-width: 200px;
}

.dur {
  color: var(--ink-3, var(--ink-2));
}

/* Price column */
.price-col {
  flex: none;
  padding: 14px 14px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  border-left: 1px dashed var(--line);
}

.price {
  font-weight: 700;
  font-size: 14px;
  color: var(--ink);
  white-space: nowrap;
  font-family: 'IBM Plex Mono', monospace;
}

.select-btn {
  padding: 6px 16px;
  border-radius: 7px;
  border: none;
  background: var(--accent);
  color: var(--accent-ink);
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s;
  white-space: nowrap;
}

.select-btn:hover {
  opacity: 0.85;
}

.select-btn:active {
  opacity: 0.7;
}
</style>
