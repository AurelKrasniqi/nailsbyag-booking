import { useCallback, useEffect, useState } from 'react'
import {
  fetchAllAddOns,
  fetchAllServices,
  reorderGroups,
  UnauthorizedError,
  updateService,
  type AdminAddOn,
} from '../../api/admin'
import type { SalonService } from '../../api/salonServices'
import { fetchServiceGroups, type ServiceGroup } from '../../api/serviceGroups'
import { groupServices } from '../../groupServices'
import { AdminDialog } from './AdminDialog'
import { ServiceGroupsEditor } from './ServiceGroupsEditor'
import { AddOnsEditor } from './AddOnsEditor'
import { TreatmentRow, type TreatmentRowData } from './TreatmentRow'

interface Props {
  onUnauthorized: () => void
}

// Prices and durations, one card per box on the website. The "Rediger behandlinger" button opens the editor for the
// boxes themselves (title, photo, new boxes and treatments).
export function PriceListEditor({ onUnauthorized }: Props) {
  const [groups, setGroups] = useState<ServiceGroup[]>([])
  const [services, setServices] = useState<SalonService[]>([])
  const [addOns, setAddOns] = useState<AdminAddOn[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingBoxes, setEditingBoxes] = useState(false)
  const [unsaved, setUnsaved] = useState(false)

  const fail = useCallback(
    (e: unknown) => {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    },
    [onUnauthorized],
  )

  const fetchAll = () => Promise.all([fetchServiceGroups(), fetchAllServices(), fetchAllAddOns()])

  function show([g, s, a]: [ServiceGroup[], SalonService[], AdminAddOn[]]) {
    setGroups(g)
    setServices(s)
    setAddOns(a)
  }

  useEffect(() => {
    Promise.all([fetchServiceGroups(), fetchAllServices(), fetchAllAddOns()])
      .then(([g, s, a]) => {
        setGroups(g)
        setServices(s)
        setAddOns(a)
      })
      .catch(fail)
      .finally(() => setLoading(false))
  }, [fail])

  // Fetches the lists again; used after every change made in the "Rediger behandlinger" window
  async function reload() {
    try {
      show(await fetchAll())
    } catch (e) {
      fail(e)
    }
  }

  // A failed save is shown on the row; an expired login sends the admin back to the login form
  async function guard(save: () => Promise<void>) {
    try {
      await save()
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      throw e
    }
  }

  async function saveService(row: TreatmentRowData) {
    const original = services.find((s) => s.id === row.id)!
    const saved = await updateService({
      ...original,
      priceNok: row.priceNok,
      durationMinutes: row.durationMinutes,
      active: row.active,
    })
    setServices((current) => current.map((s) => (s.id === saved.id ? saved : s)))
  }

  // Moves one box on the website up or down. The whole list of boxes is sent, in the new order.
  async function moveBox(index: number, direction: -1 | 1) {
    const target = index + direction
    if (target < 0 || target >= groups.length) return
    const ids = groups.map((g) => g.id)
    ;[ids[index], ids[target]] = [ids[target], ids[index]]
    try {
      setGroups(await reorderGroups(ids))
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    }
  }

  function closeBoxEditor() {
    setEditingBoxes(false)
    setUnsaved(false)
  }

  if (loading) return <p className="status">Laster priser…</p>
  if (error) return <p className="error">{error}</p>

  const cards = groupServices(groups, services, true)

  return (
    <>
      <div className="admin-card-head price-page-head">
        <p className="admin-hint">
          Endre pris og varighet, skjul behandlinger du ikke tilbyr akkurat nå, og flytt rundt på boksene på nettsiden.
          Vil du endre tittel eller bilde, eller lage en ny boks eller behandling, bruk «Rediger behandlinger».
        </p>
        <button type="button" className="button admin-button" onClick={() => setEditingBoxes(true)}>
          Rediger behandlinger
        </button>
      </div>

      {cards.map((card, index) => (
        <section key={card.groupId} className="admin-card">
          <div className="card-heading">
            <h2>{card.eyebrow ? `${card.eyebrow} · ${card.title}` : card.title}</h2>
            <span className="move-buttons">
              <button
                type="button"
                className="link-button"
                disabled={index === 0}
                aria-label={`Flytt ${card.title} opp`}
                onClick={() => moveBox(index, -1)}
              >
                ↑ Opp
              </button>
              <button
                type="button"
                className="link-button"
                disabled={index === cards.length - 1}
                aria-label={`Flytt ${card.title} ned`}
                onClick={() => moveBox(index, 1)}
              >
                ↓ Ned
              </button>
            </span>
          </div>
          {card.options.length === 0 ? (
            <p className="admin-muted">Ingen behandlinger i denne boksen ennå.</p>
          ) : (
            <ul className="admin-list price-list">
              {card.options.map((s) => (
                // The key includes the saved values, so a row starts fresh from the server's answer after saving
                <TreatmentRow
                  key={`${s.id}-${s.priceNok}-${s.durationMinutes}-${s.active}`}
                  row={s}
                  onSave={(row) => guard(() => saveService(row))}
                />
              ))}
            </ul>
          )}
        </section>
      ))}

      <AddOnsEditor groups={groups} addOns={addOns} setAddOns={setAddOns} onUnauthorized={onUnauthorized} />

      {editingBoxes && (
        <AdminDialog title="Rediger behandlinger" hasUnsavedChanges={unsaved} onClose={closeBoxEditor}>
          <ServiceGroupsEditor
            groups={groups}
            services={services}
            reload={reload}
            onUnauthorized={onUnauthorized}
            onDirtyChange={setUnsaved}
          />
        </AdminDialog>
      )}
    </>
  )
}
