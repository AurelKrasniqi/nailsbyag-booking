import { useEffect, useRef, type ReactNode } from 'react'

interface Props {
  title: string
  onClose: () => void
  // Shown in a "close without saving?" question when the person has unsaved changes
  hasUnsavedChanges?: boolean
  children: ReactNode
}

// A window on top of the admin page, used for the "Rediger" buttons. Closes with the X, the Escape key or a click
// outside the window. The page behind it does not scroll while it is open.
export function AdminDialog({ title, onClose, hasUnsavedChanges = false, children }: Props) {
  const dialog = useRef<HTMLDivElement>(null)

  function requestClose() {
    if (!hasUnsavedChanges || window.confirm('Du har endringer som ikke er lagret. Vil du lukke likevel?')) {
      onClose()
    }
  }

  // The listener is re-attached when requestClose changes, so it always sees the latest "unsaved" state
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') requestClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  })

  useEffect(() => {
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    dialog.current?.focus()
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [])

  return (
    <div
      className="admin-dialog-backdrop"
      onMouseDown={(e) => {
        // Only a click that starts on the dark background (not a drag that ends there) closes the window
        if (e.target === e.currentTarget) requestClose()
      }}
    >
      <div className="admin-dialog" role="dialog" aria-modal="true" aria-label={title} tabIndex={-1} ref={dialog}>
        <header className="admin-dialog-header">
          <h2>{title}</h2>
          <button type="button" className="admin-dialog-close" aria-label="Lukk" onClick={requestClose}>
            ×
          </button>
        </header>
        <div className="admin-dialog-body">{children}</div>
      </div>
    </div>
  )
}
