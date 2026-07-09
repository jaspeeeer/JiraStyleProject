/**
 * Date-to-percent math shared by the roadmap and timeline views. All dates are ISO
 * `YYYY-MM-DD` strings interpreted in UTC, so positioning is timezone-independent.
 */

const DAY_MS = 24 * 60 * 60 * 1000;

const MONTH_LABELS = [
  "Jan", "Feb", "Mar", "Apr", "May", "Jun",
  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
];

function toUtcMs(date: string): number {
  const [year, month, day] = date.split("-").map(Number);
  return Date.UTC(year, month - 1, day);
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

export interface Span {
  /** Percent offset from the left edge (0–100). */
  left: number;
  /** Percent width (≥ 1 so single-day bars stay visible). */
  width: number;
}

/** Position of a [start, end] bar inside the [rangeStart, rangeEnd] axis, in percent. */
export function spanPercent(
  start: string,
  end: string,
  rangeStart: string,
  rangeEnd: string,
): Span {
  const total = Math.max(toUtcMs(rangeEnd) - toUtcMs(rangeStart) + DAY_MS, DAY_MS);
  const left = clamp(((toUtcMs(start) - toUtcMs(rangeStart)) / total) * 100, 0, 100);
  // The end date is inclusive (a one-day bar still has a day of width).
  const rawWidth = ((toUtcMs(end) - toUtcMs(start) + DAY_MS) / total) * 100;
  const width = clamp(rawWidth, 1, 100 - left);
  return { left, width: Math.max(width, 1) };
}

export interface MonthTick {
  label: string;
  /** Percent offset from the left edge (0–100). */
  left: number;
}

/** Month boundaries between rangeStart and rangeEnd, as axis tick positions. */
export function monthTicks(rangeStart: string, rangeEnd: string): MonthTick[] {
  const startMs = toUtcMs(rangeStart);
  const endMs = toUtcMs(rangeEnd);
  const total = Math.max(endMs - startMs + DAY_MS, DAY_MS);

  const cursor = new Date(startMs);
  cursor.setUTCDate(1);

  const ticks: MonthTick[] = [];
  while (cursor.getTime() <= endMs) {
    const left = clamp(((cursor.getTime() - startMs) / total) * 100, 0, 100);
    ticks.push({
      label: `${MONTH_LABELS[cursor.getUTCMonth()]} ${cursor.getUTCFullYear()}`,
      left,
    });
    cursor.setUTCMonth(cursor.getUTCMonth() + 1);
  }

  // Thin the ticks when the range spans many months so labels don't overlap.
  const step = Math.ceil(ticks.length / 12);
  return step > 1 ? ticks.filter((_, index) => index % step === 0) : ticks;
}
