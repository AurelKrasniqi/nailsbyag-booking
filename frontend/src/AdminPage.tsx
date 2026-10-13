import { useCallback, useEffect, useState } from 'react'
import { isLoggedIn, logout } from './api/admin'
import { fetchEmployees } from './api/availability'
import { ExceptionsEditor } from './components/admin/ExceptionsEditor'
import { LoginForm } from './components/admin/LoginForm'
import { OpeningHoursEditor } from './components/admin/OpeningHoursEditor'
import { UpcomingAppointments } from './components/admin/UpcomingAppointments'
import './AdminPage.css'

// Reached at /#/admin. Not linked from the public site.
export function AdminPage() {
  const [loggedIn, setLoggedIn] = useState(isLoggedIn)
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  // Stable identity: the editors list it as an effect dependency
  const handleUnauthorized = useCallback(() => setLoggedIn(false), [])

  useEffect(() => {
    if (!loggedIn) return
    fetchEmployees()
      .then((employees) => setEmployeeId(employees[0]?.id ?? null))
      .catch((e: Error) => setError(e.message))
  }, [loggedIn])

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

      <main className="admin-main">
        {!loggedIn && <LoginForm onLoggedIn={() => setLoggedIn(true)} />}
        {loggedIn && error && <p className="error">{error}</p>}
        {loggedIn && employeeId !== null && (
          <>
            <UpcomingAppointments onUnauthorized={handleUnauthorized} />
            <OpeningHoursEditor employeeId={employeeId} onUnauthorized={handleUnauthorized} />
            <ExceptionsEditor employeeId={employeeId} onUnauthorized={handleUnauthorized} />
          </>
        )}
      </main>
    </div>
  )
}
