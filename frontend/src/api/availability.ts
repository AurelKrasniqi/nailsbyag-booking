// Mirrors DayAvailabilityResponse.java. startTimes look like "10:00:00"; an empty list means closed or fully booked.
export interface DayAvailability {
  date: string
  startTimes: string[]
}

export interface Employee {
  id: number
  name: string
}

export async function fetchEmployees(): Promise<Employee[]> {
  const response = await fetch('/api/employees')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}

export async function fetchAvailability(
  employeeId: number,
  serviceIds: number[],
  from: string,
  to: string,
): Promise<DayAvailability[]> {
  const params = new URLSearchParams({
    employeeId: String(employeeId),
    serviceIds: serviceIds.join(','),
    from,
    to,
  })
  const response = await fetch(`/api/availability?${params}`)
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
