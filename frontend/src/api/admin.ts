// Everything the admin page sends to the backend. Admin endpoints need HTTP Basic credentials on every request.
// The credentials live in sessionStorage: they survive a page refresh but disappear when the tab is closed.
const STORAGE_KEY = 'adminAuth'

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'

// Mirrors OpeningHoursResponse.java (times come as "10:00:00")
export interface OpeningHours {
  id: number
  dayOfWeek: DayOfWeek
  startTime: string
  endTime: string
}

export type ExceptionType = 'BLOCKED' | 'EXTRA_OPEN'

// Mirrors ScheduleExceptionResponse.java. Both times null = the whole day.
export interface ScheduleException {
  id: number
  date: string
  startTime: string | null
  endTime: string | null
  type: ExceptionType
}

// Mirrors AppointmentResponse.java
export interface AdminAppointment {
  id: number
  customerName: string
  customerPhone: string
  serviceNames: string[]
  addOns: { name: string; quantity: number; priceNok: number }[]
  totalPriceNok: number
  startTime: string
  endTime: string
  comment: string | null
  paymentMethod: 'AT_SALON' | 'VIPPS'
  paymentStatus: 'UNPAID' | 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED'
  status: 'BOOKED' | 'CANCELLED' | 'COMPLETED'
}

export class UnauthorizedError extends Error {
  constructor() {
    super('Ikke innlogget')
  }
}

function readAuth(): string | null {
  try {
    return sessionStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

function writeAuth(value: string | null) {
  try {
    if (value === null) sessionStorage.removeItem(STORAGE_KEY)
    else sessionStorage.setItem(STORAGE_KEY, value)
  } catch {
    // Storage can be blocked (private mode); the login then only lasts until the page is refreshed
  }
}

// Kept in memory too, so login works even when sessionStorage is blocked
let memoryAuth: string | null = readAuth()

function encodeCredentials(username: string, password: string): string {
  const bytes = new TextEncoder().encode(`${username}:${password}`)
  return btoa(String.fromCharCode(...bytes))
}

export function isLoggedIn(): boolean {
  return memoryAuth !== null
}

export function logout() {
  memoryAuth = null
  writeAuth(null)
}

// After too many wrong passwords the backend locks the login for a while (1, then 10, then 30 minutes).
// The message already says how long to wait.
export class LoginLockedError extends Error {}

// Returns true when the backend accepts the credentials (GET /api/admin/me answers 200)
export async function login(username: string, password: string): Promise<boolean> {
  const encoded = encodeCredentials(username, password)
  const response = await fetch('/api/admin/me', { headers: { Authorization: `Basic ${encoded}` } })
  if (response.status === 429) {
    const body = (await response.json().catch(() => null)) as { message?: string } | null
    throw new LoginLockedError(body?.message ?? 'For mange feil forsøk. Prøv igjen senere.')
  }
  if (response.status === 401) return false
  if (!response.ok) throw new Error(`Backend svarte med ${response.status}`)
  memoryAuth = encoded
  writeAuth(encoded)
  return true
}

export async function adminFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const response = await fetch(path, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(memoryAuth ? { Authorization: `Basic ${memoryAuth}` } : {}),
    },
  })
  if (response.status === 401) {
    logout()
    throw new UnauthorizedError()
  }
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as { message?: string } | null
    throw new Error(body?.message ?? `Backend svarte med ${response.status}`)
  }
  return response
}

export async function fetchOpeningHours(employeeId: number): Promise<OpeningHours[]> {
  return (await adminFetch(`/api/employees/${employeeId}/opening-hours`)).json()
}

export async function saveOpeningHours(
  employeeId: number,
  hours: { dayOfWeek: DayOfWeek; startTime: string; endTime: string }[],
): Promise<OpeningHours[]> {
  const response = await adminFetch(`/api/employees/${employeeId}/opening-hours`, {
    method: 'PUT',
    body: JSON.stringify(hours),
  })
  return response.json()
}

export async function fetchExceptions(employeeId: number): Promise<ScheduleException[]> {
  return (await adminFetch(`/api/employees/${employeeId}/schedule-exceptions`)).json()
}

export async function createException(
  employeeId: number,
  exception: { date: string; startTime?: string; endTime?: string; type: ExceptionType },
): Promise<ScheduleException> {
  const response = await adminFetch(`/api/employees/${employeeId}/schedule-exceptions`, {
    method: 'POST',
    body: JSON.stringify(exception),
  })
  return response.json()
}

export async function deleteException(employeeId: number, exceptionId: number): Promise<void> {
  await adminFetch(`/api/employees/${employeeId}/schedule-exceptions/${exceptionId}`, { method: 'DELETE' })
}

export async function fetchAppointments(): Promise<AdminAppointment[]> {
  return (await adminFetch('/api/appointments')).json()
}

export async function cancelAppointment(id: number): Promise<void> {
  await adminFetch(`/api/appointments/${id}/cancel`, { method: 'PUT' })
}
