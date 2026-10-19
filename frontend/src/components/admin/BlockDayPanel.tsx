import { useState } from 'react'
import type { ScheduleException } from '../../api/admin'
import { shortTime } from '../../dates'
import { formatDuration, formatMinutes, type Slot } from './blockTimeModel'

const LONG_DATE = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })

interface Props {
  date: string
  slots: Slot[]
  isPastDay: boolean
  wholeDayBlock: ScheduleException | undefined
  saving: boolean
  onBlockRange: (start: number, end: number) => void
  onBlockWholeDay: () => void
  onUnblock: (block: ScheduleException) => void
}

// The timeline of one day. Tap a start time and then an end time to pick a range, then confirm.
// Mount it with key={date} so the selection starts fresh for every day.
export function BlockDayPanel({ date, slots, isPastDay, wholeDayBlock, saving, onBlockRange, onBlockWholeDay, onUnblock }: Props) {
  // anchor = first tapped slot, end = last tapped slot, picking = still waiting for the end tap
  const [selection, setSelection] = useState<{ anchor: number; end: number; picking: boolean } | null>(null)
  const [hover, setHover] = useState<number | null>(null)
  const [hint, setHint] = useState<string | null>(null)

  const hasBookings = slots.some((s) => s.state === 'booked')
  const hasFreeSlots = slots.some((s) => s.state === 'free')

  function rangeBetween(a: number, b: number): [number, number] {
    return a <= b ? [a, b] : [b, a]
  }

  function isRangeFree(lo: number, hi: number): boolean {
    return slots.slice(lo, hi + 1).every((s) => s.state === 'free')
  }

  function tap(index: number) {
    setHint(null)
    if (selection?.picking) {
      const [lo, hi] = rangeBetween(selection.anchor, index)
      if (!isRangeFree(lo, hi)) {
        setHint('Det er en time eller blokkering midt i. Velg et tidsrom uten opptatte tider.')
        return
      }
      setSelection({ anchor: selection.anchor, end: index, picking: false })
    } else {
      setSelection({ anchor: index, end: index, picking: true })
    }
  }

  // While the second tap is pending, the range follows the mouse so you see what you are about to pick
  const previewEnd = selection?.picking && hover !== null && isRangeFree(...rangeBetween(selection.anchor, hover)) ? hover : selection?.end
  const [lo, hi] = selection && previewEnd !== undefined ? rangeBetween(selection.anchor, previewEnd) : [-1, -2]

  function confirm() {
    if (!selection) return
    const [from, to] = rangeBetween(selection.anchor, selection.end)
    onBlockRange(slots[from].start, slots[to].end)
    setSelection(null)
  }

  const selectedStart = lo >= 0 ? slots[lo].start : 0
  const selectedEnd = hi >= 0 ? slots[hi].end : 0

  return (
    <section className="admin-card block-panel" aria-label="Valgt dag">
      <header className="block-panel-head">
        <h2>{LONG_DATE.format(new Date(`${date}T00:00:00`))}</h2>
        {!isPastDay && !wholeDayBlock && (
          <button
            type="button"
            className="pill-button"
            disabled={saving || hasBookings}
            onClick={onBlockWholeDay}
            title={hasBookings ? 'Det finnes timer denne dagen' : undefined}
          >
            Blokker hele dagen
          </button>
        )}
      </header>

      {wholeDayBlock && (
        <div className="block-banner">
          <span>
            <strong>Hele dagen er blokkert.</strong> Ingen kan booke denne dagen.
          </span>
          <button type="button" className="pill-button" disabled={saving} onClick={() => onUnblock(wholeDayBlock)}>
            Åpne igjen
          </button>
        </div>
      )}

      {!wholeDayBlock && hasBookings && !isPastDay && (
        <p className="admin-hint block-hint">Hele dagen kan ikke blokkeres siden det finnes timer. Blokker ledige tidsrom under.</p>
      )}

      {slots.length === 0 && <p className="admin-muted">Ingen åpningstider denne dagen. Sett dem opp under Åpningstider.</p>}

      {!wholeDayBlock && slots.length > 0 && !isPastDay && hasFreeSlots && !selection && (
        <p className="admin-hint block-hint">Trykk på starttidspunktet og så sluttidspunktet for tiden du vil blokkere.</p>
      )}

      <ul className="slot-list" onMouseLeave={() => setHover(null)}>
        {slots.map((slot, i) => {
          const previous = slots[i - 1]
          const startsGroup =
            !previous ||
            previous.state !== slot.state ||
            previous.appointment !== slot.appointment ||
            previous.block !== slot.block
          const selected = i >= lo && i <= hi
          const clickable = slot.state === 'free' && !isPastDay

          let classes = `slot slot-${slot.state}`
          if (!startsGroup) classes += ' slot-continued'
          if (selected) classes += ' slot-selected'
          if (selected && i === lo) classes += ' slot-selected-first'
          if (selected && i === hi) classes += ' slot-selected-last'

          return (
            <li key={slot.start} className={classes}>
              <span className={slot.start % 60 === 0 ? 'slot-time' : 'slot-time slot-time-half'}>{formatMinutes(slot.start)}</span>
              {clickable ? (
                <button
                  type="button"
                  className="slot-body slot-button"
                  aria-pressed={selected}
                  aria-label={`${formatMinutes(slot.start)}–${formatMinutes(slot.end)}, ledig`}
                  onClick={() => tap(i)}
                  onMouseEnter={() => setHover(i)}
                >
                  {selected && i === lo && <span className="slot-label">Valgt</span>}
                </button>
              ) : (
                <div className="slot-body">
                  {startsGroup && slot.state === 'booked' && slot.appointment && (
                    <span className="slot-label">
                      <strong>{slot.appointment.customerName}</strong>
                      <span className="slot-sub">
                        {shortTime(slot.appointment.startTime.slice(11))}–{shortTime(slot.appointment.endTime.slice(11))} ·{' '}
                        {slot.appointment.serviceNames.join(' + ')}
                      </span>
                    </span>
                  )}
                  {startsGroup && slot.state === 'blocked' && slot.block && !wholeDayBlock && (
                    <>
                      <span className="slot-label">
                        <strong>Blokkert</strong>
                        <span className="slot-sub">
                          {slot.block.startTime && slot.block.endTime
                            ? `${shortTime(slot.block.startTime)}–${shortTime(slot.block.endTime)}`
                            : 'Hele dagen'}
                        </span>
                      </span>
                      <button type="button" className="link-button" disabled={saving} onClick={() => onUnblock(slot.block!)}>
                        Åpne igjen
                      </button>
                    </>
                  )}
                  {startsGroup && slot.state === 'closed' && <span className="slot-sub">Stengt</span>}
                </div>
              )}
            </li>
          )
        })}
      </ul>

      {selection && (
        <div className="block-confirm" role="status">
          <span>
            <strong>
              {formatMinutes(selectedStart)}–{formatMinutes(selectedEnd)}
            </strong>{' '}
            <span className="admin-muted">
              · {formatDuration(selectedEnd - selectedStart)}
              {selection.picking && ' · trykk på et senere tidspunkt for å utvide'}
            </span>
            {hint && <span className="error block-error">{hint}</span>}
          </span>
          <span className="block-confirm-buttons">
            <button type="button" className="link-button" onClick={() => setSelection(null)}>
              Avbryt
            </button>
            <button type="button" className="button admin-button" disabled={saving} onClick={confirm}>
              {saving ? 'Blokkerer…' : 'Blokker'}
            </button>
          </span>
        </div>
      )}
    </section>
  )
}
