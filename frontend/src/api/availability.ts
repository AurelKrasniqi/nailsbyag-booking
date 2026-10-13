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

// addOns: "extra id:quantity" pairs, e.g. ["1:4", "6:1"]. Extras make the treatment longer, so they change the free times.
export async function fetchAvailability(
  employeeId: number,
  serviceIds: number[],
  addOns: string[],
  from: string,
  to: string,
): Promise<DayAvailability[]> {
  const params = new URLSearchParams({
    employeeId: String(employeeId),
    serviceIds: serviceIds.join(','),
    from,
    to,
  })
  if (addOns.length > 0) {
    params.set('addOns', addOns.join(','))
  }
  const response = await fetch(`/api/availability?${params}`)
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
