import { useCallback, useEffect, useState } from 'react'
import { isLoggedIn, logout } from './api/admin'
import { fetchEmployees } from './api/availability'
import { AboutSettings } from './components/admin/AboutSettings'
import { ExceptionsEditor } from './components/admin/ExceptionsEditor'
import { IncomePage } from './components/admin/IncomePage'
import { LoginForm } from './components/admin/LoginForm'
import { OpeningHoursEditor } from './components/admin/OpeningHoursEditor'
import { OverviewPage } from './components/admin/OverviewPage'
import { PriceListEditor } from './components/admin/PriceListEditor'
import { UpcomingAppointments } from './components/admin/UpcomingAppointments'
import './AdminPage.css'

// The dashboard has one page per topic. The page is the part of the address after #/admin
// (/#/admin = overview, /#/admin/timer, /#/admin/kalender, /#/admin/priser, /#/admin/om-oss, /#/admin/inntekt).
type PageKey = 'oversikt' | 'timer' | 'kalender' | 'priser' | 'om-oss' | 'inntekt'

const PAGES: { key: PageKey; label: string; href: string; title: string }[] = [
  { key: 'oversikt', label: 'Oversikt', href: '#/admin', title: 'Oversikt' },
  { key: 'timer', label: 'Timer', href: '#/admin/timer', title: 'Kommende timer' },
  { key: 'kalender', label: 'Kalender', href: '#/admin/kalender', title: 'Åpningstider og fridager' },
  { key: 'priser', label: 'Priser', href: '#/admin/priser', title: 'Priser og behandlinger' },
  { key: 'om-oss', label: 'Om oss', href: '#/admin/om-oss', title: 'Om oss' },
  { key: 'inntekt', label: 'Inntekt', href: '#/admin/inntekt', title: 'Inntekt' },
]

function pageFromHash(hash: string): PageKey {
  const part = hash.slice('#/admin'.length).replace(/^\//, '')
  return PAGES.find((p) => p.key === part)?.key ?? 'oversikt'
}

// Reached at /#/admin. Not linked from the public site.
export function AdminPage({ hash }: { hash: string }) {
  const [loggedIn, setLoggedIn] = useState(isLoggedIn)
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  // Stable identity: the pages list it as an effect dependency
  const handleUnauthorized = useCallback(() => setLoggedIn(false), [])

  useEffect(() => {
    if (!loggedIn) return
    fetchEmployees()
      .then((employees) => setEmployeeId(employees[0]?.id ?? null))
      .catch((e: Error) => setError(e.message))
  }, [loggedIn])

  const page = pageFromHash(hash)
  const current = PAGES.find((p) => p.key === page)!

  return (
    <div className="admin-page">
      <header className="admin-header">
        <strong>The Beauty Room · Admin</strong>
        <span>
          <a href="#/">Til nettsiden</a>
          {loggedIn && (
            <>
              {' · '}
              <button
                type="button"
                className="link-button"
                onClick={() => {
                  logout()
                  setLoggedIn(false)
                }}
              >
                Logg ut
              </button>
            </>
          )}
        </span>
      </header>

      {!loggedIn ? (
        <main className="admin-main">
          <LoginForm onLoggedIn={() => setLoggedIn(true)} />
        </main>
      ) : (
        <div className="admin-shell">
          <nav className="admin-nav" aria-label="Admin">
            {PAGES.map((p) => (
              <a
                key={p.key}
                href={p.href}
                className={p.key === page ? 'active' : ''}
                aria-current={p.key === page ? 'page' : undefined}
              >
                {p.label}
              </a>
            ))}
          </nav>

          <main className="admin-main">
            <h1 className="admin-page-title">{current.title}</h1>
            {error && <p className="error">{error}</p>}

            {page === 'oversikt' && <OverviewPage onUnauthorized={handleUnauthorized} />}
            {page === 'timer' && <UpcomingAppointments onUnauthorized={handleUnauthorized} />}
            {page === 'priser' && <PriceListEditor onUnauthorized={handleUnauthorized} />}
            {page === 'om-oss' && <AboutSettings onUnauthorized={handleUnauthorized} />}
            {page === 'inntekt' && <IncomePage onUnauthorized={handleUnauthorized} />}
            {page === 'kalender' && employeeId !== null && (
              <div className="admin-two-columns">
                <OpeningHoursEditor employeeId={employeeId} onUnauthorized={handleUnauthorized} />
                <ExceptionsEditor employeeId={employeeId} onUnauthorized={handleUnauthorized} />
              </div>
            )}
          </main>
        </div>
      )}
    </div>
  )
}
