// Mirrors AppointmentRequest.java
export interface BookingRequest {
  employeeId: number
  serviceIds: number[]
  // Local date-time without time zone, e.g. "2026-10-20T14:00:00"
  startTime: string
  customer: {
    name: string
    phone: string
    email?: string
  }
}

// The part of AppointmentResponse.java the confirmation screen needs
export interface Appointment {
  id: number
  serviceNames: string[]
  totalPriceNok: number
  startTime: string
  endTime: string
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

export async function createAppointment(request: BookingRequest): Promise<Appointment> {
  const response = await fetch('/api/appointments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!response.ok) {
    // Spring's error body has a "message"; for 409 it is our own readable text
    const body = (await response.json().catch(() => null)) as { message?: string } | null
    throw new ApiError(response.status, body?.message ?? `Backend svarte med ${response.status}`)
  }
  return response.json()
}
