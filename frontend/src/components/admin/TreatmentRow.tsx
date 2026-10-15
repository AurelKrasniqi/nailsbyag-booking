import { useState } from 'react'

// One editable line in the price list. Hidden treatments disappear from the website but stay in the database,
// so old appointments that used them keep their history (deleting them would break those).
export interface TreatmentRowData {
  id: number
  name: string
  priceNok: number
  durationMinutes: number
  active: boolean
  priceOnRequest: boolean
}

interface Props {
  row: TreatmentRowData
  // The name can be changed too (in the box editor); in the price list it is just a label
  editableName?: boolean
  onSave: (row: TreatmentRowData) => Promise<void>
}

export function TreatmentRow({ row, editableName = false, onSave }: Props) {
  const [draft, setDraft] = useState(row)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const nameOk = draft.name.trim().length > 0
  const changed =
    draft.name !== row.name ||
    draft.priceNok !== row.priceNok ||
    draft.durationMinutes !== row.durationMinutes ||
    draft.active !== row.active

  async function save() {
    setSaving(true)
    setError(null)
    try {
      await onSave({ ...draft, name: draft.name.trim() })
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <li className={`price-row ${draft.active ? '' : 'price-row-hidden'}`}>
      {editableName ? (
        <input
          type="text"
          className="price-name-input"
          aria-label="Navn på behandlingen"
          value={draft.name}
          maxLength={100}
          onChange={(e) => setDraft({ ...draft, name: e.target.value })}
        />
      ) : (
        <span className="price-name">{row.name}</span>
      )}
      {row.priceOnRequest ? (
        <span className="admin-muted">Pris på forespørsel</span>
      ) : (
        <label className="unit-input">
          <input
            type="number"
            min={0}
            aria-label={`Pris for ${row.name}`}
            value={draft.priceNok}
            onChange={(e) => setDraft({ ...draft, priceNok: Number(e.target.value) })}
          />
          <span>kr</span>
        </label>
      )}
      <label className="unit-input">
        <input
          type="number"
          min={1}
          step={5}
          aria-label={`Varighet for ${row.name}`}
          value={draft.durationMinutes}
          onChange={(e) => setDraft({ ...draft, durationMinutes: Number(e.target.value) })}
        />
        <span>min</span>
      </label>
      <label className="check-field">
        <input
          type="checkbox"
          checked={draft.active}
          onChange={(e) => setDraft({ ...draft, active: e.target.checked })}
        />{' '}
        Vises på nettsiden
      </label>
      <button type="button" className="button admin-button" disabled={!changed || !nameOk || saving} onClick={save}>
        {saving ? 'Lagrer…' : 'Lagre'}
      </button>
      {error && <p className="error price-error">{error}</p>}
    </li>
  )
}
