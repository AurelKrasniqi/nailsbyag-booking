export type PaymentMethod = 'AT_SALON' | 'VIPPS'

// Mirrors AppointmentRequest.java
export interface BookingRequest {
  employeeId: number
  serviceIds: number[]
  // Optional extras (glitter, charms, ...): which extra and how many
  addOns?: { addOnId: number; quantity: number }[]
  // Local date-time without time zone, e.g. "2026-10-20T14:00:00"
  startTime: string
  customer: {
    name: string
    phone: string
    email?: string
  }
  // Optional note to AG, max 500 characters
  comment?: string
  paymentMethod: PaymentMethod
}

// The part of AppointmentResponse.java the customer-facing screens need
export interface Appointment {
  id: number
  customerName: string
  serviceNames: string[]
  // The extras on the appointment, with the total price of each line
  addOns: { name: string; quantity: number; priceNok: number }[]
  totalPriceNok: number
  startTime: string
  endTime: string
  publicToken: string
  paymentMethod: PaymentMethod
  paymentStatus: 'UNPAID' | 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED'
  status: 'PENDING_PAYMENT' | 'BOOKED' | 'CANCELLED' | 'COMPLETED'
}

// Mirrors BookingResult.java. paymentRedirectUrl is set when the customer chose to pay online:
// the browser must go there to complete the payment.
export interface BookingResult {
  appointment: Appointment
  paymentRedirectUrl: string | null
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function failure(response: Response): Promise<ApiError> {
  // Spring's error body has a "message"; for 409 and 502 it is our own readable text
  const body = (await response.json().catch(() => null)) as { message?: string } | null
  return new ApiError(response.status, body?.message ?? `Backend svarte med ${response.status}`)
}

export async function createAppointment(request: BookingRequest): Promise<BookingResult> {
  const response = await fetch('/api/appointments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!response.ok) throw await failure(response)
  return response.json()
}

// Asks the backend to check with the payment provider what happened, and returns the booking as it now stands
export async function checkPayment(token: string): Promise<Appointment> {
  const response = await fetch(`/api/bookings/${encodeURIComponent(token)}/check-payment`, { method: 'POST' })
  if (!response.ok) throw await failure(response)
  return response.json()
}

// Only exists in development, where the backend uses the fake payment provider
export async function fakeVippsDecide(token: string, decision: 'approve' | 'reject'): Promise<void> {
  const response = await fetch(`/api/dev/fake-vipps/${encodeURIComponent(token)}/${decision}`, { method: 'POST' })
  if (!response.ok) throw await failure(response)
}
