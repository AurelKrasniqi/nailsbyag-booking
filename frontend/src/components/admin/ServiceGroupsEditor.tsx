import { useState } from 'react'
import type { SalonService } from '../../api/salonServices'
import type { ServiceGroup } from '../../api/serviceGroups'
import { GroupEditor } from './GroupEditor'

interface Props {
  groups: ServiceGroup[]
  // Every treatment, also hidden ones
  services: SalonService[]
  reload: () => Promise<void>
  onUnauthorized: () => void
  onDirtyChange: (dirty: boolean) => void
}

type View = { kind: 'list' } | { kind: 'edit'; id: number } | { kind: 'new' }

// The content of the "Rediger behandlinger" window: first the list of boxes, then the editor for the chosen box
// (or for a new one).
export function ServiceGroupsEditor({ groups, services, reload, onUnauthorized, onDirtyChange }: Props) {
  const [view, setView] = useState<View>({ kind: 'list' })

  if (view.kind !== 'list') {
    const group = view.kind === 'edit' ? (groups.find((g) => g.id === view.id) ?? null) : null
    return (
      <GroupEditor
        // A fresh editor for every box, so nothing from the previous one lingers
        key={view.kind === 'edit' ? view.id : 'new'}
        group={group}
        treatments={group ? services.filter((s) => s.groupId === group.id) : []}
        reload={reload}
        onCreated={(id) => setView({ kind: 'edit', id })}
        onBack={() => setView({ kind: 'list' })}
        onUnauthorized={onUnauthorized}
        onDirtyChange={onDirtyChange}
      />
    )
  }

  return (
    <>
      <div className="admin-card-head">
        <p className="admin-hint">
          Dette er boksene på forsiden. Velg en boks for å endre tittel, bilde og behandlinger, eller lag en ny.
        </p>
        <button type="button" className="button admin-button" onClick={() => setView({ kind: 'new' })}>
          + Ny boks
        </button>
      </div>

      <ul className="box-list">
        {groups.map((group) => {
          const own = services.filter((s) => s.groupId === group.id)
          const hidden = own.filter((s) => !s.active).length
          return (
            <li key={group.id} className="box-list-item">
              {group.imageUrl ? (
                <img className="box-thumb" src={group.imageUrl} alt="" />
              ) : (
                <div className="box-thumb box-thumb-empty">
                  <img src="/images/butterfly-gold.png" alt="" />
                </div>
              )}
              <div className="box-list-text">
                {group.eyebrow && <span className="eyebrow">{group.eyebrow}</span>}
                <strong>{group.title}</strong>
                <span className="admin-muted">
                  {own.length} {own.length === 1 ? 'behandling' : 'behandlinger'}
                  {hidden > 0 ? `, ${hidden} skjult` : ''}
                </span>
              </div>
              <button
                type="button"
                className="button admin-button"
                onClick={() => setView({ kind: 'edit', id: group.id })}
              >
                Rediger
              </button>
            </li>
          )
        })}
      </ul>
    </>
  )
}
