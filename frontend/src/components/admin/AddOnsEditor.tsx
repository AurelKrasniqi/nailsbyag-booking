import { useState } from 'react'
import {
  createAddOn,
  deleteAddOn,
  UnauthorizedError,
  updateAddOn,
  type AddOnInput,
  type AdminAddOn,
} from '../../api/admin'
import type { ServiceGroup } from '../../api/serviceGroups'

interface Props {
  groups: ServiceGroup[]
  addOns: AdminAddOn[]
  setAddOns: React.Dispatch<React.SetStateAction<AdminAddOn[]>>
  onUnauthorized: () => void
}

const EMPTY: AddOnInput = {
  name: '',
  unitLabel: null,
  priceNok: 0,
  durationMinutes: 5,
  maxQuantity: 1,
  groupIds: [],
  active: true,
}

// The "Tillegg" (extras) on the price list: change them, add new ones, or remove them.
// Which boxes an extra can be added to is chosen per extra.
export function AddOnsEditor({ groups, addOns, setAddOns, onUnauthorized }: Props) {
  async function guard<T>(action: () => Promise<T>): Promise<T> {
    try {
      return await action()
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      throw e
    }
  }

  return (
    <section className="admin-card">
      <h2>Tillegg</h2>
      <p className="admin-hint">
        Ekstra ting kunden kan legge til på en behandling. Pris og tid gjelder per enhet (f.eks. per negl). Velg hvilke
        bokser tillegget kan velges i.
      </p>
      <ul className="admin-list price-list">
        {addOns.map((a) => (
          <AddOnRow
            // Starts fresh from the server's answer after saving
            key={`${a.id}-${a.name}-${a.unitLabel}-${a.priceNok}-${a.durationMinutes}-${a.maxQuantity}-${a.active}-${a.groupIds.join(',')}`}
            groups={groups}
            value={a}
            onSave={(input) =>
              guard(async () => {
                const saved = await updateAddOn(a.id, input)
                setAddOns((current) => current.map((x) => (x.id === saved.id ? saved : x)))
              })
            }
            onDelete={() =>
              guard(async () => {
                await deleteAddOn(a.id)
                setAddOns((current) => current.filter((x) => x.id !== a.id))
              })
            }
          />
        ))}
      </ul>

      <h3 className="editor-heading">Legg til tillegg</h3>
      <ul className="admin-list price-list">
        <AddOnRow
          groups={groups}
          value={EMPTY}
          isNew
          onSave={(input) =>
            guard(async () => {
              const created = await createAddOn(input)
              setAddOns((current) => [...current, created])
            })
          }
        />
      </ul>
    </section>
  )
}

interface RowProps {
  groups: ServiceGroup[]
  value: AddOnInput
  isNew?: boolean
  onSave: (input: AddOnInput) => Promise<void>
  onDelete?: () => Promise<void>
}

function AddOnRow({ groups, value, isNew = false, onSave, onDelete }: RowProps) {
  const [draft, setDraft] = useState(value)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const valid = draft.name.trim() !== '' && draft.groupIds.length > 0 && draft.durationMinutes >= 1 && draft.maxQuantity >= 1
  const changed = isNew || JSON.stringify(draft) !== JSON.stringify(value)

  async function run(action: () => Promise<void>) {
    setSaving(true)
    setError(null)
    try {
      await action()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  async function save() {
    await run(async () => {
      await onSave({ ...draft, name: draft.name.trim(), unitLabel: draft.unitLabel?.trim() || null })
      if (isNew) setDraft(EMPTY)
    })
  }

  async function remove() {
    if (!onDelete || !window.confirm(`Slette tillegget «${value.name}»?`)) return
    await run(onDelete)
  }

  function toggleGroup(id: number) {
    const has = draft.groupIds.includes(id)
    setDraft({ ...draft, groupIds: has ? draft.groupIds.filter((g) => g !== id) : [...draft.groupIds, id].sort((a, b) => a - b) })
  }

  return (
    <li className={`price-row addon-row ${draft.active ? '' : 'price-row-hidden'}`}>
      <input
        type="text"
        className="price-name-input"
        aria-label="Navn på tillegget"
        placeholder="Navn, f.eks. Glitter/Chrome"
        value={draft.name}
        maxLength={100}
        onChange={(e) => setDraft({ ...draft, name: e.target.value })}
      />
      <label className="unit-input">
        <input
          type="number"
          min={0}
          aria-label="Pris"
          value={draft.priceNok}
          onChange={(e) => setDraft({ ...draft, priceNok: Number(e.target.value) })}
        />
        <span>kr</span>
      </label>
      <label className="unit-input">
        <input
          type="number"
          min={1}
          aria-label="Varighet"
          value={draft.durationMinutes}
          onChange={(e) => setDraft({ ...draft, durationMinutes: Number(e.target.value) })}
        />
        <span>min</span>
      </label>
      <label className="unit-input">
        <input
          type="text"
          aria-label="Pris gjelder per (valgfritt)"
          placeholder="pr. negl"
          value={draft.unitLabel ?? ''}
          maxLength={30}
          onChange={(e) => setDraft({ ...draft, unitLabel: e.target.value })}
        />
      </label>
      <label className="unit-input">
        <input
          type="number"
          min={1}
          max={50}
          aria-label="Høyeste antall kunden kan velge"
          value={draft.maxQuantity}
          onChange={(e) => setDraft({ ...draft, maxQuantity: Number(e.target.value) })}
        />
        <span>maks</span>
      </label>

      <div className="addon-groups">
        <span className="admin-muted">Kan velges i:</span>
        {groups.map((g) => (
          <label key={g.id} className="check-field">
            <input type="checkbox" checked={draft.groupIds.includes(g.id)} onChange={() => toggleGroup(g.id)} />{' '}
            {g.eyebrow ? `${g.eyebrow} · ${g.title}` : g.title}
          </label>
        ))}
      </div>

      <label className="check-field">
        <input type="checkbox" checked={draft.active} onChange={(e) => setDraft({ ...draft, active: e.target.checked })} />{' '}
        Vises på nettsiden
      </label>
      <span className="addon-actions">
        {onDelete && (
          <button type="button" className="link-button danger-link" disabled={saving} onClick={remove}>
            Slett
          </button>
        )}
        <button type="button" className="button admin-button" disabled={!changed || !valid || saving} onClick={save}>
          {saving ? 'Lagrer…' : isNew ? 'Legg til' : 'Lagre'}
        </button>
      </span>
      {!valid && changed && (
        <p className="admin-muted price-error">Trenger navn og minst én boks tillegget kan velges i.</p>
      )}
      {error && <p className="error price-error">{error}</p>}
    </li>
  )
}
