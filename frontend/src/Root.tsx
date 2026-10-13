import { useEffect, useState } from 'react'
import { AdminPage } from './AdminPage.tsx'
import App from './App.tsx'

// Minimal routing without a library: the address /#/admin shows the admin page, anything else the public site
export function Root() {
  const [hash, setHash] = useState(window.location.hash)

  useEffect(() => {
    const onChange = () => setHash(window.location.hash)
    window.addEventListener('hashchange', onChange)
    return () => window.removeEventListener('hashchange', onChange)
  }, [])

  return hash.startsWith('#/admin') ? <AdminPage /> : <App />
}
