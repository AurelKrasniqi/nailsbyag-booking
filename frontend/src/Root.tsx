import { useEffect, useState } from 'react'
import { AdminPage } from './AdminPage.tsx'
import App from './App.tsx'
import { PrivacyPage, TermsPage } from './LegalPages.tsx'
import { FakeVippsPage, PaymentReturnPage } from './PaymentPages.tsx'

// Minimal routing without a library, based on the part after # in the address:
//   /#/admin             admin dashboard (/#/admin/timer, /#/admin/kalender, /#/admin/inntekt are its pages)
//   /#/betaling/<token>  where the customer lands after paying
//   /#/fake-vipps/<ref>  development stand-in for the Vipps payment page
//   /#/vilkar            terms of sale, /#/personvern  privacy policy
//   anything else        the public site
export function Root() {
  const [hash, setHash] = useState(window.location.hash)

  useEffect(() => {
    const onChange = () => setHash(window.location.hash)
    window.addEventListener('hashchange', onChange)
    return () => window.removeEventListener('hashchange', onChange)
  }, [])

  if (hash.startsWith('#/admin')) return <AdminPage hash={hash} />
  if (hash.startsWith('#/vilkar')) return <TermsPage />
  if (hash.startsWith('#/personvern')) return <PrivacyPage />
  if (hash.startsWith('#/betaling/')) return <PaymentReturnPage token={hash.slice('#/betaling/'.length)} />
  if (hash.startsWith('#/fake-vipps/')) return <FakeVippsPage token={hash.slice('#/fake-vipps/'.length)} />
  return <App />
}
