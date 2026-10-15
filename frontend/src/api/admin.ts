// Everything the admin page sends to the backend. Admin endpoints need HTTP Basic credentials on every request.
// The credentials live in sessionStorage: they survive a page refresh but disappear when the tab is closed.
import type { About } from './about'
import type { AddOn } from './addOns'
import type { SalonService } from './salonServices'
import type { ServiceGroup } from './serviceGroups'

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

// Mirrors AddOnResponse.java, including whether the extra is switched on
export type AdminAddOn = AddOn & { active: boolean }

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
      ...(init.headers as Record<string, string> | undefined),
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

// Price list. The admin sees inactive treatments and extras too (the public lists hide them).
export async function fetchAllServices(): Promise<SalonService[]> {
  return (await adminFetch('/api/services')).json()
}

// The backend replaces the whole treatment on PUT, so everything is sent, not just the changed fields
export async function updateService(service: SalonService): Promise<SalonService> {
  const { id, ...body } = service
  const response = await adminFetch(`/api/services/${id}`, { method: 'PUT', body: JSON.stringify(body) })
  return response.json()
}

export async function saveAboutText(heading: string, body: string): Promise<About> {
  const response = await adminFetch('/api/about', { method: 'PUT', body: JSON.stringify({ heading, body }) })
  return response.json()
}

// The picture itself is the request body (already shrunk by shrinkImage)
export async function uploadAboutImage(image: Blob): Promise<About> {
  const response = await adminFetch('/api/about/image', {
    method: 'PUT',
    headers: { 'Content-Type': image.type || 'image/jpeg' },
    body: image,
  })
  return response.json()
}

// Back to the default photo
export async function deleteAboutImage(): Promise<About> {
  return (await adminFetch('/api/about/image', { method: 'DELETE' })).json()
}

export async function createService(service: Omit<SalonService, 'id'>): Promise<SalonService> {
  const response = await adminFetch('/api/services', { method: 'POST', body: JSON.stringify(service) })
  return response.json()
}

// The boxes on the website (title, photo, order). A box is deleted only when it holds no treatments.
export interface GroupFields {
  title: string
  eyebrow: string | null
  imageFocus: number
}

export async function createGroup(fields: GroupFields): Promise<ServiceGroup> {
  return (await adminFetch('/api/service-groups', { method: 'POST', body: JSON.stringify(fields) })).json()
}

export async function updateGroup(id: number, fields: GroupFields): Promise<ServiceGroup> {
  return (await adminFetch(`/api/service-groups/${id}`, { method: 'PUT', body: JSON.stringify(fields) })).json()
}

export async function deleteGroup(id: number): Promise<void> {
  await adminFetch(`/api/service-groups/${id}`, { method: 'DELETE' })
}

// ids = every box, in the order they should have on the website
export async function reorderGroups(ids: number[]): Promise<ServiceGroup[]> {
  const response = await adminFetch('/api/service-groups/order', { method: 'PUT', body: JSON.stringify({ ids }) })
  return response.json()
}

// The picture itself is the request body (already shrunk by shrinkImage)
export async function uploadGroupImage(id: number, image: Blob): Promise<ServiceGroup> {
  const response = await adminFetch(`/api/service-groups/${id}/image`, {
    method: 'PUT',
    headers: { 'Content-Type': image.type || 'image/jpeg' },
    body: image,
  })
  return response.json()
}

export async function deleteGroupImage(id: number): Promise<ServiceGroup> {
  return (await adminFetch(`/api/service-groups/${id}/image`, { method: 'DELETE' })).json()
}

export async function fetchAllAddOns(): Promise<AdminAddOn[]> {
  return (await adminFetch('/api/add-ons/all')).json()
}

export async function updateAddOn(addOn: AdminAddOn): Promise<AdminAddOn> {
  const body = { priceNok: addOn.priceNok, durationMinutes: addOn.durationMinutes, active: addOn.active }
  const response = await adminFetch(`/api/add-ons/${addOn.id}`, { method: 'PUT', body: JSON.stringify(body) })
  return response.json()
}
