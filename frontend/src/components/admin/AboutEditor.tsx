import { useEffect, useState } from 'react'
import { deleteAboutImage, saveAboutText, UnauthorizedError, uploadAboutImage } from '../../api/admin'
import { DEFAULT_ABOUT_IMAGE, type About } from '../../api/about'
import { AboutSection } from '../AboutSection'
import { ImagePicker } from './ImagePicker'

const MAX_HEADING = 100
const MAX_BODY = 3000

interface Props {
  // What is on the website now
  about: About
  onUnauthorized: () => void
  // Lets the window around the editor ask "close without saving?"
  onDirtyChange: (dirty: boolean) => void
  onSaved: (saved: About) => void
}

// Edit the "Om oss" box: heading, text and photo, with a live preview of what the website will show.
// Nothing changes on the website until "Lagre endringer" is pressed.
export function AboutEditor({ about, onUnauthorized, onDirtyChange, onSaved }: Props) {
  const [heading, setHeading] = useState(about.heading)
  const [body, setBody] = useState(about.body)
  // A photo picked but not saved yet (already shrunk), with the temporary address used to show it
  const [pending, setPending] = useState<{ blob: Blob; url: string } | null>(null)
  const [useDefaultImage, setUseDefaultImage] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const headingOk = heading.trim().length > 0 && heading.length <= MAX_HEADING
  const bodyOk = body.trim().length > 0 && body.length <= MAX_BODY
  const textChanged = heading.trim() !== about.heading || body.trim() !== about.body
  const imageChanged = pending !== null || (useDefaultImage && about.imageUrl !== null)
  const dirty = textChanged || imageChanged

  useEffect(() => {
    onDirtyChange(dirty)
  }, [dirty, onDirtyChange])

  const currentImage = about.imageUrl ?? DEFAULT_ABOUT_IMAGE
  const previewImage = pending?.url ?? (useDefaultImage ? DEFAULT_ABOUT_IMAGE : currentImage)
  const showsDefaultImage = pending === null && (useDefaultImage || about.imageUrl === null)

  // The temporary address of a picked photo must be released when it is replaced, so it is done in one place
  function replacePending(blob: Blob | null) {
    setPending((old) => {
      if (old) URL.revokeObjectURL(old.url)
      return blob ? { blob, url: URL.createObjectURL(blob) } : null
    })
  }

  async function save() {
    setSaving(true)
    setError(null)
    try {
      let result = about
      if (textChanged) result = await saveAboutText(heading, body)
      if (pending) result = await uploadAboutImage(pending.blob)
      else if (useDefaultImage && about.imageUrl !== null) result = await deleteAboutImage()
      onSaved(result)
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  function discard() {
    setHeading(about.heading)
    setBody(about.body)
    replacePending(null)
    setUseDefaultImage(false)
    setError(null)
  }

  return (
    <>
      <div className="editor-field">
        <div className="editor-label-row">
          <label htmlFor="about-heading">Overskrift</label>
          <span className={`char-count ${heading.length > MAX_HEADING ? 'over' : ''}`}>
            {heading.length}/{MAX_HEADING}
          </span>
        </div>
        <input id="about-heading" type="text" value={heading} onChange={(e) => setHeading(e.target.value)} />
      </div>

      <div className="editor-field">
        <div className="editor-label-row">
          <label htmlFor="about-body">Tekst</label>
          <span className={`char-count ${body.length > MAX_BODY ? 'over' : ''}`}>
            {body.length}/{MAX_BODY}
          </span>
        </div>
        <textarea id="about-body" rows={9} value={body} onChange={(e) => setBody(e.target.value)} />
        <p className="field-hint">Legg inn en tom linje mellom avsnittene for å lage nytt avsnitt.</p>
      </div>

      <div className="editor-field">
        <div className="editor-label-row">
          <span className="editor-label">Bilde</span>
        </div>
        <ImagePicker
          id="about-image"
          src={previewImage}
          removeLabel={pending ? 'Angre valgt bilde' : showsDefaultImage ? null : 'Bruk standardbildet'}
          onPick={(blob) => {
            replacePending(blob)
            setUseDefaultImage(false)
          }}
          onRemove={() => {
            if (pending) replacePending(null)
            else setUseDefaultImage(true)
          }}
          onError={setError}
        />
      </div>

      <div className="editor-field">
        <div className="editor-label-row">
          <span className="editor-label">Forhåndsvisning</span>
        </div>
        <div className="preview-frame">
          <AboutSection
            heading={heading.trim() || 'Overskrift'}
            body={body.trim() || 'Teksten din vises her.'}
            imageSrc={previewImage}
          />
        </div>
        <p className="field-hint">
          Oppdateres mens du skriver. På mobil står bildet under teksten, og det beskjæres litt for å passe.
        </p>
      </div>

      <div className="save-bar">
        <span className={error ? 'error' : 'admin-muted'}>
          {error ??
            (!dirty
              ? 'Ingen endringer.'
              : !headingOk || !bodyOk
                ? 'Overskrift og tekst må fylles ut (og ikke bli for lange).'
                : 'Du har endringer som ikke er lagret.')}
        </span>
        <span className="save-bar-buttons">
          <button type="button" className="link-button" disabled={!dirty || saving} onClick={discard}>
            Forkast
          </button>
          <button
            type="button"
            className="button admin-button"
            disabled={!dirty || !headingOk || !bodyOk || saving}
            onClick={save}
          >
            {saving ? 'Lagrer…' : 'Lagre endringer'}
          </button>
        </span>
      </div>
    </>
  )
}
