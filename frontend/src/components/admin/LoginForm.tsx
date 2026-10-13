import { useState, type FormEvent } from 'react'
import { login } from '../../api/admin'

export function LoginForm({ onLoggedIn }: { onLoggedIn: () => void }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      if (await login(username.trim(), password)) onLoggedIn()
      else setError('Feil brukernavn eller passord.')
    } catch {
      setError('Kunne ikke kontakte serveren. Prøv igjen.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="admin-card login-form" onSubmit={submit}>
      <h1>Logg inn</h1>
      <label className="form-field">
        Brukernavn
        <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
      </label>
      <label className="form-field">
        Passord
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
          required
        />
      </label>
      {error && <p className="error">{error}</p>}
      <button type="submit" className="button admin-button" disabled={submitting}>
        {submitting ? 'Logger inn…' : 'Logg inn'}
      </button>
    </form>
  )
}
