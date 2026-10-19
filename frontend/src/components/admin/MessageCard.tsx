import { useId, useRef } from 'react'
import { PLACEHOLDER_LABELS, renderSample, smsInfo, unknownPlaceholders, type SampleBooking } from './messageTemplate'

// One SMS. Must match MessageTemplatesRequest.MAX_LENGTH in the backend.
export const MAX_LENGTH = 160

interface Props {
  title: string
  description: string
  value: string
  defaultValue: string
  placeholders: string[]
  sample: SampleBooking
  onChange: (value: string) => void
}

// One editable message: a text box, buttons that insert {placeholders}, and a preview as the customer sees it.
export function MessageCard({ title, description, value, defaultValue, placeholders, sample, onChange }: Props) {
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const id = useId()

  const preview = renderSample(value, sample)
  const sms = smsInfo(preview)
  const unknown = unknownPlaceholders(value, placeholders)
  const tooLong = value.length > MAX_LENGTH

  // Puts the placeholder where the cursor is (or replaces the selected text), and keeps the cursor after it
  function insert(name: string) {
    const field = textareaRef.current
    const token = `{${name}}`
    if (!field) {
      onChange(value + token)
      return
    }
    const start = field.selectionStart
    const end = field.selectionEnd
    onChange(value.slice(0, start) + token + value.slice(end))
    requestAnimationFrame(() => {
      field.focus()
      field.setSelectionRange(start + token.length, start + token.length)
    })
  }

  return (
    <section className="message-card">
      <div className="editor-label-row">
        <label htmlFor={id} className="message-title">
          {title}
        </label>
        <span className={`char-count ${tooLong ? 'over' : ''}`}>
          {value.length}/{MAX_LENGTH}
        </span>
      </div>
      <p className="admin-hint message-description">{description}</p>

      <textarea
        id={id}
        ref={textareaRef}
        className="message-textarea"
        rows={4}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />

      <div className="placeholder-row">
        <span className="placeholder-caption">Sett inn:</span>
        {placeholders.map((name) => (
          <button key={name} type="button" className="placeholder-chip" title={PLACEHOLDER_LABELS[name]} onClick={() => insert(name)}>
            {PLACEHOLDER_LABELS[name] ?? name}
          </button>
        ))}
      </div>

      {unknown.length > 0 && (
        <p className="error message-warning">
          Ukjent plassholder: {unknown.map((n) => `{${n}}`).join(', ')}. Bruk knappene over, så blir det riktig.
        </p>
      )}

      <div className="message-preview-wrap">
        <p className="message-preview-label">Slik ser den ut for kunden</p>
        <div className="sms-bubble" aria-live="polite">
          {preview.trim() === '' ? <span className="admin-muted">Skriv en tekst over…</span> : preview}
        </div>
        <p className="field-hint">
          Eksempel med «{sample.navn}». Ferdig tekst: {sms.length} tegn = {sms.parts} {sms.parts === 1 ? 'SMS' : 'SMS-deler'}.
          {sms.unicode && ' Emoji eller spesialtegn gjør at hver SMS-del bare rommer 70 tegn.'}
          {sms.parts > 1 && ' Navn og behandlinger er lengre for noen kunder, så hold deg gjerne litt under grensen.'}
        </p>
      </div>

      <button type="button" className="link-button" disabled={value === defaultValue} onClick={() => onChange(defaultValue)}>
        Tilbakestill til standard
      </button>
    </section>
  )
}
