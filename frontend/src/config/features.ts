// Feature flags — one source of truth for which phase is live.
//
// Phase 1 (current): login, single chat + streaming, my bookings.
// Phase 2: conversation history + sidebar, ongoing/resumable bookings, RAG.
// Phase 3: reasoning, reschedule, expanded RAG.
//
// To unmute a feature, flip its flag to true. No redesign needed —
// the UI renders live vs muted based solely on these values.

export const features = {
  // Phase 1 — live
  auth: true,
  chat: true,
  bookings: true,

  // Phase 2 — muted
  conversationHistory: false,
  resumableBookings: false,
  policyRag: false,
  askSora: false,

  // Phase 3 — muted
  leaveTimeReasoning: false,
  reschedule: false,
} as const

export type FeatureKey = keyof typeof features
