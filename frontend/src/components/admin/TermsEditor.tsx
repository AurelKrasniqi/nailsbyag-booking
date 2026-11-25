import { useEffect, useState } from 'react'
import { saveTerms, UnauthorizedError } from '../../api/admin'
import { fetchTerms, type TermsSection } from '../../api/terms'

const MAX_HEADING = 100
const MAX_BODY = 5000
const MAX_SECTIONS = 30

// A section while it is being edited. The key keeps React's rows stable when sections are moved or removed.
interface Draft {
  key: number
  heading: string
  body: string
}

let nextKey = 1
const toDrafts = (sections: TermsSection[]): Draft[] =>
  sections.map((s) => ({ key: nextKey++, heading: s.heading ?? '', body: s.body }))
const toPayload = (drafts: Draft[]): TermsSection[] =>
  drafts.map((d) => ({ heading: d.heading.trim() || null, body: d.body.trim() }))

// The "Salgsvilkår" page: every block (heading + text) can be changed, moved, removed, and new ones added.
// Nothing changes on the website until "Lagre endringer" is pressed.
export function TermsEditor({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [saved, setSaved] = useState<TermsSection[] | null>(null)
  const [drafts, setDrafts] = useState<Draft[]>([])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [justSaved, setJustSaved] = useState(false)

  useEffect(() => {
    fetchTerms()
      .then((sections) => {
        setSaved(sections)
        setDrafts(toDrafts(sections))
      })
      .catch((e: Error) => setError(e.message))
  }, [])

  const valid =
    drafts.length > 0 &&
    drafts.every((d) => d.body.trim() !== '' && d.body.length <= MAX_BODY && d.heading.length <= MAX_HEADING)
  const dirty = saved !== null && JSON.stringify(toPayload(drafts)) !== JSON.stringify(saved)

  function update(key: number, change: Partial<Draft>) {
    setJustSaved(false)
    setDrafts((current) => current.map((d) => (d.key === key ? { ...d, ...change } : d)))
  }

  function move(index: number, by: -1 | 1) {
    setJustSaved(false)
    setDrafts((current) => {
      const target = index + by
      if (target < 0 || target >= current.length) return current
      const copy = [...current]
      ;[copy[index], copy[target]] = [copy[target], copy[index]]
      return copy
    })
  }

  function remove(key: number, heading: string) {
    if (!window.confirm(`Slette avsnittet${heading.trim() ? ` «${heading.trim()}»` : ''}?`)) return
    setJustSaved(false)
    setDrafts((current) => current.filter((d) => d.key !== key))
  }

  function add() {
    setJustSaved(false)
    setDrafts((current) => [...current, { key: nextKey++, heading: '', body: '' }])
  }

  async function save() {
    setSaving(true)
    setError(null)
    try {
      const result = await saveTerms(toPayload(drafts))
      setSaved(result)
      setDrafts(toDrafts(result))
      setJustSaved(true)
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  function discard() {
    if (saved) setDrafts(toDrafts(saved))
    setError(null)
  }

  return (
    <section className="admin-card">
      <p className="admin-hint">
        Dette er teksten på siden «Salgsvilkår». Hvert avsnitt har en overskrift (kan stå tom) og en tekst. Legg inn en
        tom linje mellom avsnitt i samme tekst. Du kan se resultatet på{' '}
        <a href="#/vilkar" target="_blank" rel="noreferrer">
          vilkårssiden
        </a>{' '}
        etter at du har lagret.
      </p>

      {saved === null && !error && <p className="admin-muted">Laster…</p>}
      {justSaved && <p className="admin-success">Lagret. Endringene vises nå på nettsiden.</p>}

      {saved !== null &&
        drafts.map((d, index) => (
          <div key={d.key} className="terms-block">
            <div className="editor-field">
              <div className="editor-label-row">
                <label htmlFor={`terms-heading-${d.key}`}>Overskrift (valgfri)</label>
                <span className={`char-count ${d.heading.length > MAX_HEADING ? 'over' : ''}`}>
                  {d.heading.length}/{MAX_HEADING}
                </span>
              </div>
              <input
                id={`terms-heading-${d.key}`}
                type="text"
                value={d.heading}
                onChange={(e) => update(d.key, { heading: e.target.value })}
              />
            </div>
            <div className="editor-field">
              <div className="editor-label-row">
                <label htmlFor={`terms-body-${d.key}`}>Tekst</label>
                <span className={`char-count ${d.body.length > MAX_BODY ? 'over' : ''}`}>
                  {d.body.length}/{MAX_BODY}
                </span>
              </div>
              <textarea
                id={`terms-body-${d.key}`}
                rows={5}
                value={d.body}
                onChange={(e) => update(d.key, { body: e.target.value })}
              />
            </div>
            <div className="terms-block-actions">
              <button type="button" className="link-button" disabled={index === 0} onClick={() => move(index, -1)}>
                ↑ Flytt opp
              </button>
              <button
                type="button"
                className="link-button"
                disabled={index === drafts.length - 1}
                onClick={() => move(index, 1)}
              >
                ↓ Flytt ned
              </button>
              <button type="button" className="link-button danger-link" onClick={() => remove(d.key, d.heading)}>
                Slett avsnitt
              </button>
            </div>
          </div>
        ))}

      {saved !== null && (
        <button
          type="button"
          className="button admin-button"
          disabled={drafts.length >= MAX_SECTIONS}
          onClick={add}
        >
          + Legg til avsnitt
        </button>
      )}

      <div className="save-bar">
        <span className={error ? 'error' : 'admin-muted'}>
          {error ??
            (!dirty
              ? 'Ingen endringer.'
              : !valid
                ? 'Hvert avsnitt må ha tekst (og ikke bli for langt).'
                : 'Du har endringer som ikke er lagret.')}
        </span>
        <span className="save-bar-buttons">
          <button type="button" className="link-button" disabled={!dirty || saving} onClick={discard}>
            Forkast
          </button>
          <button
            type="button"
            className="button admin-button"
            disabled={!dirty || !valid || saving}
            onClick={save}
          >
            {saving ? 'Lagrer…' : 'Lagre endringer'}
          </button>
        </span>
      </div>
    </section>
  )
}
