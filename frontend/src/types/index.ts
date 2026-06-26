// ── Backend response shapes ─────────────────────────────────────────────────

export interface FlightOption {
  carrier: string
  flightNumber: string
  origin: string
  destination: string
  departureTime: string   // ISO datetime e.g. "2026-07-03T08:00:00"
  arrivalTime: string
  durationMinutes: number
  stops: number
  price: number
  currency: string
}

export interface BookingResponse {
  id: number
  bookingReference: string
  carrier: string
  flightNumber: string
  origin: string
  destination: string
  departureTime: string  // ISO datetime, e.g. "2026-07-15T11:30:00"
  arrivalTime: string
  price: number
  currency: string
  status: 'CONFIRMED' | 'CANCELLED'
  createdAt: string
}

// ── Chat message shapes ─────────────────────────────────────────────────────

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  flights?: FlightOption[]
  // Only meaningful for assistant messages:
  streaming: boolean   // still receiving tokens from the SSE stream
  toolActive: boolean  // show "Thinking..." indicator
}

// ── Auth ────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  token: string
}
