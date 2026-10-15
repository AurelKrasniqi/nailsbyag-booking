import { useEffect, useState } from 'react'
import { DEFAULT_ABOUT_IMAGE, fetchAbout, type About } from '../../api/about'
import { AboutSection } from '../AboutSection'
import { AboutEditor } from './AboutEditor'
import { AdminDialog } from './AdminDialog'

// The "Om oss" section of the admin page: shows the box as it looks on the website, with a button to edit it.
export function AboutSettings({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [about, setAbout] = useState<About | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState(false)
  const [unsaved, setUnsaved] = useState(false)
  const [justSaved, setJustSaved] = useState(false)

  useEffect(() => {
    fetchAbout()
      .then(setAbout)
      .catch((e: Error) => setError(e.message))
  }, [])

  function closeEditor() {
    setEditing(false)
    setUnsaved(false)
  }

  return (
    <section className="admin-card">
      <div className="admin-card-head">
        <p className="admin-hint">Dette er boksen «Om oss» på forsiden, slik den ser ut nå.</p>
        <button
          type="button"
          className="button admin-button"
          disabled={!about}
          onClick={() => {
            setJustSaved(false)
            setEditing(true)
          }}
        >
          Rediger
        </button>
      </div>

      {error && <p className="error">Kunne ikke hente teksten: {error}</p>}
      {justSaved && <p className="admin-success">Lagret. Endringene vises nå på nettsiden.</p>}

      {about && (
        <div className="preview-frame">
          <AboutSection heading={about.heading} body={about.body} imageSrc={about.imageUrl ?? DEFAULT_ABOUT_IMAGE} />
        </div>
      )}

      {editing && about && (
        <AdminDialog title="Rediger «Om oss»" hasUnsavedChanges={unsaved} onClose={closeEditor}>
          <AboutEditor
            about={about}
            onUnauthorized={onUnauthorized}
            onDirtyChange={setUnsaved}
            onSaved={(saved) => {
              setAbout(saved)
              setJustSaved(true)
              closeEditor()
            }}
          />
        </AdminDialog>
      )}
    </section>
  )
}
