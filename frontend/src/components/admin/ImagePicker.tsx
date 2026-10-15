import { useRef } from 'react'
import { shrinkImage } from '../../imageResize'

interface Props {
  id: string
  // The photo to show as a thumbnail; null = there is none yet
  src: string | null
  // Whether the remove link is shown, and what it says (e.g. "Bruk standardbildet" or "Fjern bildet")
  removeLabel: string | null
  hint?: string
  // The picked photo, already shrunk
  onPick: (image: Blob) => void
  onRemove: () => void
  onError: (message: string) => void
}

// Choose a photo from the computer or phone. Shared by the "Om oss" editor and the box editor.
export function ImagePicker({ id, src, removeLabel, hint, onPick, onRemove, onError }: Props) {
  const fileInput = useRef<HTMLInputElement>(null)

  async function pick(file: File | undefined) {
    if (!file) return
    try {
      onPick(await shrinkImage(file))
    } catch (e) {
      onError((e as Error).message)
    } finally {
      // Lets the same file be picked again after "Forkast"
      if (fileInput.current) fileInput.current.value = ''
    }
  }

  return (
    <div className="image-picker">
      {src ? (
        <img className="image-thumb" src={src} alt="" />
      ) : (
        <div className="image-thumb image-thumb-empty">
          <img src="/images/butterfly-gold.png" alt="" />
        </div>
      )}
      <div className="image-actions">
        <input
          ref={fileInput}
          id={id}
          className="visually-hidden"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          onChange={(e) => pick(e.target.files?.[0])}
        />
        <label htmlFor={id} className="button admin-button file-button">
          {src ? 'Velg nytt bilde' : 'Velg bilde'}
        </label>
        {removeLabel && (
          <button type="button" className="link-button" onClick={onRemove}>
            {removeLabel}
          </button>
        )}
        <p className="field-hint">
          {hint ?? 'JPG, PNG eller WebP. Bildet krympes automatisk, så store bilder fra mobilen går fint.'}
        </p>
      </div>
    </div>
  )
}
