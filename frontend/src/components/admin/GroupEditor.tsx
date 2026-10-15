import { useEffect, useState } from 'react'
import {
  createGroup,
  createService,
  deleteGroup,
  deleteGroupImage,
  updateGroup,
  updateService,
  uploadGroupImage,
  UnauthorizedError,
} from '../../api/admin'
import type { SalonService } from '../../api/salonServices'
import type { ServiceGroup } from '../../api/serviceGroups'
import { ServiceCard } from '../ServiceCard'
import { ImagePicker } from './ImagePicker'
import { TreatmentRow, type TreatmentRowData } from './TreatmentRow'

interface Props {
  // The box being edited; null = a new box that does not exist yet
  group: ServiceGroup | null
  // All treatments in the box (also hidden ones)
  treatments: SalonService[]
  reload: () => Promise<void>
  onCreated: (id: number) => void
  onBack: () => void
  onUnauthorized: () => void
  onDirtyChange: (dirty: boolean) => void
}

const NO_SELECTION = new Set<number>()

// Edit one box: title, small text above the title, photo (with the part of it that is shown), and the treatments in it.
// A live preview shows the box exactly as it looks on the website.
export function GroupEditor({ group, treatments, reload, onCreated, onBack, onUnauthorized, onDirtyChange }: Props) {
  const [title, setTitle] = useState(group?.title ?? '')
  const [eyebrow, setEyebrow] = useState(group?.eyebrow ?? '')
  const [focus, setFocus] = useState(group?.imageFocus ?? 40)
  // A photo picked but not saved yet (already shrunk), with the temporary address used to show it
  const [pending, setPending] = useState<{ blob: Blob; url: string } | null>(null)
  const [removeUploaded, setRemoveUploaded] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const titleOk = title.trim().length > 0 && title.length <= 100
  const textChanged =
    group === null ||
    title.trim() !== group.title ||
    eyebrow.trim() !== (group.eyebrow ?? '') ||
    focus !== group.imageFocus
  const imageChanged = pending !== null || removeUploaded
  const dirty = group === null ? title.trim() !== '' || eyebrow.trim() !== '' || imageChanged : textChanged || imageChanged

  useEffect(() => {
    onDirtyChange(dirty)
  }, [dirty, onDirtyChange])
  useEffect(() => () => onDirtyChange(false), [onDirtyChange])

  // What the box shows now: the picked photo, else (after "remove") the one that ships with the website, else the saved one
  const previewSrc = pending?.url ?? (removeUploaded ? (group?.defaultImageUrl ?? null) : (group?.imageUrl ?? null))
  const hasUploaded = group?.hasUploadedImage === true && !removeUploaded
  const removeLabel = pending
    ? 'Angre valgt bilde'
    : hasUploaded
      ? group?.defaultImageUrl
        ? 'Bruk standardbildet'
        : 'Fjern bildet'
      : null

  // The temporary address of a picked photo must be released when it is replaced, so it is done in one place
  function replacePending(blob: Blob | null) {
    setPending((old) => {
      if (old) URL.revokeObjectURL(old.url)
      return blob ? { blob, url: URL.createObjectURL(blob) } : null
    })
  }

  async function guard(action: () => Promise<void>) {
    setError(null)
    try {
      await action()
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    }
  }

  async function save() {
    setSaving(true)
    await guard(async () => {
      const fields = { title: title.trim(), eyebrow: eyebrow.trim() || null, imageFocus: focus }
      if (group === null) {
        const created = await createGroup(fields)
        if (pending) await uploadGroupImage(created.id, pending.blob)
        await reload()
        onCreated(created.id)
        return
      }
      if (textChanged) await updateGroup(group.id, fields)
      if (pending) await uploadGroupImage(group.id, pending.blob)
      else if (removeUploaded) await deleteGroupImage(group.id)
      replacePending(null)
      setRemoveUploaded(false)
      await reload()
    })
    setSaving(false)
  }

  function discard() {
    setTitle(group?.title ?? '')
    setEyebrow(group?.eyebrow ?? '')
    setFocus(group?.imageFocus ?? 40)
    replacePending(null)
    setRemoveUploaded(false)
    setError(null)
  }

  function back() {
    if (dirty && !window.confirm('Du har endringer som ikke er lagret. Vil du gå tilbake likevel?')) return
    onBack()
  }

  async function saveTreatment(row: TreatmentRowData) {
    const original = treatments.find((t) => t.id === row.id)!
    await updateService({
      ...original,
      name: row.name,
      priceNok: row.priceNok,
      durationMinutes: row.durationMinutes,
      active: row.active,
    })
    await reload()
  }

  async function removeBox() {
    if (!group || !window.confirm(`Slette boksen «${group.title}»?`)) return
    await guard(async () => {
      await deleteGroup(group.id)
      await reload()
      onBack()
    })
  }

  return (
    <>
      <button type="button" className="link-button back-link" onClick={back}>
        ← Alle bokser
      </button>

      <h3 className="editor-heading">{group ? `Boks: ${group.title}` : 'Ny boks'}</h3>

      <div className="editor-field">
        <div className="editor-label-row">
          <label htmlFor="box-title">Tittel</label>
          <span className="char-count">{title.length}/100</span>
        </div>
        <input id="box-title" type="text" value={title} maxLength={100} onChange={(e) => setTitle(e.target.value)} />
      </div>

      <div className="editor-field">
        <div className="editor-label-row">
          <label htmlFor="box-eyebrow">Liten tekst over tittelen (valgfritt)</label>
        </div>
        <input
          id="box-eyebrow"
          type="text"
          value={eyebrow}
          maxLength={100}
          onChange={(e) => setEyebrow(e.target.value)}
        />
        <p className="field-hint">F.eks. «Manikyr» over tittelen «Nytt sett».</p>
      </div>

      <div className="editor-field">
        <div className="editor-label-row">
          <span className="editor-label">Bilde</span>
        </div>
        <ImagePicker
          id="box-image"
          src={previewSrc}
          removeLabel={removeLabel}
          onPick={(blob) => {
            replacePending(blob)
            setRemoveUploaded(false)
          }}
          onRemove={() => {
            if (pending) replacePending(null)
            else setRemoveUploaded(true)
          }}
          onError={setError}
        />
        {previewSrc && (
          <div className="focus-control">
            <label htmlFor="box-focus">Hvilken del av bildet vises</label>
            <div className="focus-slider">
              <span>Toppen</span>
              <input
                id="box-focus"
                type="range"
                min={0}
                max={100}
                value={focus}
                onChange={(e) => setFocus(Number(e.target.value))}
              />
              <span>Bunnen</span>
            </div>
            <p className="field-hint">Boksen viser bare en stripe av bildet. Dra for å velge hvilken del som vises.</p>
          </div>
        )}
      </div>

      <div className="editor-field">
        <div className="editor-label-row">
          <span className="editor-label">Forhåndsvisning</span>
        </div>
        <div className="preview-frame">
          <ul className="cards cards-preview">
            <ServiceCard
              card={{
                groupId: group?.id ?? 0,
                eyebrow: eyebrow.trim() || null,
                title: title.trim() || 'Tittel',
                imageUrl: previewSrc,
                imageFocus: focus,
                options: treatments.filter((t) => t.active),
              }}
              selectedIds={NO_SELECTION}
              onToggle={() => {}}
            />
          </ul>
        </div>
        {group === null && (
          <p className="field-hint">Behandlinger legger du til etter at boksen er opprettet.</p>
        )}
      </div>

      <div className="save-bar">
        <span className={error ? 'error' : 'admin-muted'}>
          {error ??
            (!dirty
              ? 'Ingen endringer.'
              : !titleOk
                ? 'Boksen må ha en tittel.'
                : 'Du har endringer som ikke er lagret.')}
        </span>
        <span className="save-bar-buttons">
          <button type="button" className="link-button" disabled={!dirty || saving} onClick={discard}>
            Forkast
          </button>
          <button
            type="button"
            className="button admin-button"
            disabled={!dirty || !titleOk || saving}
            onClick={save}
          >
            {saving ? 'Lagrer…' : group ? 'Lagre boks' : 'Opprett boks'}
          </button>
        </span>
      </div>

      {group && (
        <div className="editor-field treatments-section">
          <h3 className="editor-heading">Behandlinger i boksen</h3>
          {treatments.length === 0 ? (
            <p className="admin-muted">Ingen behandlinger ennå. Legg til den første under.</p>
          ) : (
            <ul className="admin-list price-list">
              {treatments.map((t) => (
                <TreatmentRow
                  key={`${t.id}-${t.name}-${t.priceNok}-${t.durationMinutes}-${t.active}`}
                  row={t}
                  editableName
                  onSave={async (row) => {
                    try {
                      await saveTreatment(row)
                    } catch (e) {
                      if (e instanceof UnauthorizedError) onUnauthorized()
                      throw e
                    }
                  }}
                />
              ))}
            </ul>
          )}

          <NewTreatmentForm
            groupId={group.id}
            onAdded={reload}
            onUnauthorized={onUnauthorized}
          />

          {treatments.length === 0 && (
            <button type="button" className="link-button danger-link" onClick={removeBox}>
              Slett denne boksen
            </button>
          )}
        </div>
      )}
    </>
  )
}

function NewTreatmentForm({
  groupId,
  onAdded,
  onUnauthorized,
}: {
  groupId: number
  onAdded: () => Promise<void>
  onUnauthorized: () => void
}) {
  const [name, setName] = useState('')
  const [price, setPrice] = useState(0)
  const [minutes, setMinutes] = useState(60)
  const [note, setNote] = useState('')
  const [onRequest, setOnRequest] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function add() {
    setSaving(true)
    setError(null)
    try {
      await createService({
        name: name.trim(),
        description: null,
        durationMinutes: minutes,
        priceNok: onRequest ? 0 : price,
        category: null,
        subcategory: null,
        active: true,
        note: note.trim() || null,
        priceOnRequest: onRequest,
        groupId,
      })
      setName('')
      setPrice(0)
      setMinutes(60)
      setNote('')
      setOnRequest(false)
      await onAdded()
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="new-treatment">
      <h4>Legg til behandling</h4>
      <div className="new-treatment-fields">
        <input
          type="text"
          placeholder="Navn, f.eks. Ansiktsrens"
          aria-label="Navn på ny behandling"
          value={name}
          maxLength={100}
          onChange={(e) => setName(e.target.value)}
        />
        {!onRequest && (
          <label className="unit-input">
            <input
              type="number"
              min={0}
              aria-label="Pris"
              value={price}
              onChange={(e) => setPrice(Number(e.target.value))}
            />
            <span>kr</span>
          </label>
        )}
        <label className="unit-input">
          <input
            type="number"
            min={1}
            step={5}
            aria-label="Varighet"
            value={minutes}
            onChange={(e) => setMinutes(Number(e.target.value))}
          />
          <span>min</span>
        </label>
      </div>
      <input
        type="text"
        className="new-treatment-note"
        placeholder="Liten tekst under behandlingen (valgfritt), f.eks. Tillegg kan forekomme"
        aria-label="Liten tekst"
        value={note}
        maxLength={200}
        onChange={(e) => setNote(e.target.value)}
      />
      <div className="new-treatment-footer">
        <label className="check-field">
          <input type="checkbox" checked={onRequest} onChange={(e) => setOnRequest(e.target.checked)} /> Pris på
          forespørsel (kan ikke bookes på nettet)
        </label>
        <button
          type="button"
          className="button admin-button"
          disabled={saving || name.trim() === '' || minutes < 1}
          onClick={add}
        >
          {saving ? 'Legger til…' : 'Legg til'}
        </button>
      </div>
      {error && <p className="error">{error}</p>}
    </div>
  )
}
