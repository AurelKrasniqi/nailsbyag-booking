import { useEffect, useState } from 'react'
import { fetchMessageTemplates, saveMessageTemplates, UnauthorizedError, type MessageTemplates } from '../../api/admin'
import { MAX_LENGTH, MessageCard } from './MessageCard'
import { sampleBooking, unknownPlaceholders } from './messageTemplate'
import './MessageTemplates.css'

// The two text messages customers get: the confirmation right after booking, and the reminder the day before.
export function MessagesPage({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [saved, setSaved] = useState<MessageTemplates | null>(null)
  const [confirmation, setConfirmation] = useState('')
  const [reminder, setReminder] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [justSaved, setJustSaved] = useState(false)
  // Fixed for the lifetime of the page, so the preview does not change while Agneta types
  const [sample] = useState(sampleBooking)

  useEffect(() => {
    fetchMessageTemplates()
      .then((templates) => {
        setSaved(templates)
        setConfirmation(templates.confirmationText)
        setReminder(templates.reminderText)
      })
      .catch((e: unknown) => {
        if (e instanceof UnauthorizedError) onUnauthorized()
        else setError((e as Error).message)
      })
  }, [onUnauthorized])

  if (!saved) {
    return <section className="admin-card">{error ? <p className="error">{error}</p> : <p className="status">Laster…</p>}</section>
  }

  const dirty = confirmation !== saved.confirmationText || reminder !== saved.reminderText
  const valid = [confirmation, reminder].every(
    (text) => text.trim() !== '' && text.length <= MAX_LENGTH && unknownPlaceholders(text, saved.placeholders).length === 0,
  )

  async function save() {
    setSaving(true)
    setError(null)
    setJustSaved(false)
    try {
      const result = await saveMessageTemplates(confirmation, reminder)
      setSaved(result)
      setConfirmation(result.confirmationText)
      setReminder(result.reminderText)
      setJustSaved(true)
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  function discard() {
    if (!saved) return
    setConfirmation(saved.confirmationText)
    setReminder(saved.reminderText)
    setError(null)
  }

  return (
    <section className="admin-card">
      <p className="admin-hint">
        Disse tekstene sendes som SMS til kunden. Bruk knappene under tekstboksen for å sette inn kundens navn, dato og tid, så
        fylles de inn automatisk for hver kunde.
      </p>

      <MessageCard
        title="Bekreftelse"
        description="Sendes med en gang kunden har booket."
        value={confirmation}
        defaultValue={saved.defaultConfirmationText}
        placeholders={saved.placeholders}
        sample={sample}
        onChange={(v) => {
          setConfirmation(v)
          setJustSaved(false)
        }}
      />

      <MessageCard
        title="Påminnelse"
        description="Sendes dagen før timen (mellom kl. 10 og 20). Husk å skrive «i morgen» hvis du vil ha det med."
        value={reminder}
        defaultValue={saved.defaultReminderText}
        placeholders={saved.placeholders}
        sample={sample}
        onChange={(v) => {
          setReminder(v)
          setJustSaved(false)
        }}
      />

      <div className="save-bar">
        <span className={error ? 'error' : justSaved ? 'admin-success' : 'admin-muted'}>
          {error ??
            (justSaved
              ? 'Lagret. Nye bookinger får de nye tekstene.'
              : !dirty
                ? 'Ingen endringer.'
                : !valid
                  ? 'Tekstene må fylles ut, ikke være for lange, og bare bruke plassholderne fra knappene.'
                  : 'Du har endringer som ikke er lagret.')}
        </span>
        <span className="save-bar-buttons">
          <button type="button" className="link-button" disabled={!dirty || saving} onClick={discard}>
            Forkast
          </button>
          <button type="button" className="button admin-button" disabled={!dirty || !valid || saving} onClick={save}>
            {saving ? 'Lagrer…' : 'Lagre endringer'}
          </button>
        </span>
      </div>
    </section>
  )
}
