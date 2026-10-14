// Mirrors PhoneNumber.java on the backend, so the customer is told before the form is sent.
// The backend checks again: the browser is never trusted.
export function isValidPhone(input: string): boolean {
  let digits = input.replace(/[\s\-().]/g, '')
  if (digits.startsWith('00')) digits = '+' + digits.slice(2)
  if (/^\d{8}$/.test(digits)) digits = '+47' + digits
  else if (/^47\d{8}$/.test(digits)) digits = '+' + digits

  if (!/^\+\d{8,15}$/.test(digits)) return false
  // Norwegian mobile numbers start with 4 or 9; landlines cannot receive a text message
  if (digits.startsWith('+47') && !/^\+47[49]\d{7}$/.test(digits)) return false
  return true
}

export const PHONE_ERROR = 'Skriv et gyldig norsk mobilnummer, for eksempel 912 34 567. Vi sender bekreftelsen på SMS.'
