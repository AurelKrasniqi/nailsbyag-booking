// Mirrors PaymentOptionsController.PaymentOptions: which ways of paying the booking page may offer.
// vippsEnabled is false on the live site until real Vipps credentials exist.
export interface PaymentOptions {
  vippsEnabled: boolean
}

export async function fetchPaymentOptions(): Promise<PaymentOptions> {
  const response = await fetch('/api/payment-options')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
