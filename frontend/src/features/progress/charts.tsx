import { useId, useState, type ReactNode } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { Card } from '@/components/Card'
import { formatNumber, formatShortDate } from '@/lib/format'
import type { AnalyticsBucket } from './types'

/**
 * Chart primitives shared by the progress screen.
 *
 * Colour rules followed here (see styles/global.css for the validated palette):
 * - Series colours are assigned in fixed slot order and never cycled.
 * - Colour never carries identity on its own: single-series charts are identified by
 *   their card title, the one multi-series chart has a legend, and every chart can be
 *   switched to a table view.
 * - One y-axis per chart. Two measures with different scales get two charts.
 */

const AXIS_STYLE = { fill: 'var(--text-subtle)', fontSize: 11 }
const GRID_STYLE = { stroke: 'var(--border)', strokeDasharray: '3 3' }
/** Wide enough for a four-digit calorie tick or an 83.2 kg tick without clipping. */
const Y_AXIS_WIDTH = 54

/**
 * Daily buckets arrive labelled with a full ISO date, which is far too long to repeat under
 * thirty bars. Week and month buckets already carry a short label, so those pass through.
 */
function tickLabel(label: string): string {
  return /^\d{4}-\d{2}-\d{2}$/.test(label) ? formatShortDate(label) : label
}

interface TooltipEntry {
  name?: string
  value?: number | string
  color?: string
  dataKey?: string | number
}

function ChartTooltip({
  active,
  payload,
  label,
  unit,
}: {
  active?: boolean
  payload?: TooltipEntry[]
  label?: string
  unit: string
}) {
  if (!active || !payload || payload.length === 0) return null
  return (
    <div
      style={{
        background: 'var(--bg-elevated)',
        border: '1px solid var(--border-strong)',
        borderRadius: 'var(--radius-sm)',
        padding: '8px 10px',
        fontSize: '0.8125rem',
        boxShadow: 'var(--shadow)',
      }}
    >
      <p style={{ color: 'var(--text-muted)', marginBottom: 4 }}>{label}</p>
      {payload.map((entry) => (
        <p key={String(entry.dataKey)} style={{ color: 'var(--text)' }}>
          <span
            aria-hidden="true"
            style={{
              display: 'inline-block',
              width: 8,
              height: 8,
              borderRadius: 2,
              background: entry.color,
              marginRight: 6,
            }}
          />
          {entry.name}: {typeof entry.value === 'number' ? entry.value.toLocaleString() : entry.value}{' '}
          {unit}
        </p>
      ))}
    </div>
  )
}

interface ChartCardProps {
  title: string
  /** Charts with a legend need extra height so the legend is not squeezed out of the plot. */
  tall?: boolean
  /** Rendered instead of the plot when the reader switches to the table view. */
  table: ReactNode
  children: ReactNode
  footnote?: string
  empty: boolean
}

/**
 * Wraps a plot with a table toggle. The table is not a fallback - it is how a reader who
 * cannot use the plot (screen reader, colour vision, print) gets the same numbers.
 */
export function ChartCard({ title, table, children, footnote, empty, tall = false }: ChartCardProps) {
  const [showTable, setShowTable] = useState(false)
  const regionId = useId()

  return (
    <Card
      title={title}
      action={
        !empty && (
          <button
            type="button"
            className="btn btn--ghost btn--sm"
            aria-pressed={showTable}
            aria-controls={regionId}
            onClick={() => setShowTable((current) => !current)}
          >
            {showTable ? 'Chart' : 'Table'}
          </button>
        )
      }
    >
      <div id={regionId}>
        {empty ? (
          <p className="text-sm text-muted">No data in this range yet.</p>
        ) : showTable ? (
          <div style={{ overflowX: 'auto' }}>{table}</div>
        ) : (
          <div className={tall ? 'chart-wrap chart-wrap--tall' : 'chart-wrap'}>{children}</div>
        )}
      </div>
      {footnote && !empty && <p className="text-xs text-subtle mt-3">{footnote}</p>}
    </Card>
  )
}

export function SeriesTable({
  buckets,
  columns,
}: {
  buckets: AnalyticsBucket[]
  columns: { key: keyof AnalyticsBucket; label: string; format?: (value: unknown) => string }[]
}) {
  return (
    <table className="text-sm tabular" style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr>
          <th style={{ textAlign: 'left', padding: '4px 8px', color: 'var(--text-muted)' }}>
            Period
          </th>
          {columns.map((column) => (
            <th
              key={String(column.key)}
              style={{ textAlign: 'right', padding: '4px 8px', color: 'var(--text-muted)' }}
            >
              {column.label}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {buckets.map((bucket) => (
          <tr key={bucket.date} style={{ borderTop: '1px solid var(--border)' }}>
            <td style={{ padding: '4px 8px' }}>{bucket.label}</td>
            {columns.map((column) => {
              const value = bucket[column.key]
              return (
                <td key={String(column.key)} style={{ textAlign: 'right', padding: '4px 8px' }}>
                  {column.format
                    ? column.format(value)
                    : value === null || value === undefined
                      ? '—'
                      : String(value)}
                </td>
              )
            })}
          </tr>
        ))}
      </tbody>
    </table>
  )
}

/** A single-series bar chart. Identity comes from the card title, so no legend is needed. */
export function SingleBarChart({
  buckets,
  dataKey,
  unit,
  name,
  target,
  allowDecimals = true,
  domain,
}: {
  buckets: AnalyticsBucket[]
  dataKey: keyof AnalyticsBucket
  unit: string
  name: string
  target?: number | null
  /** False for counts - half a workout is not a meaningful tick. */
  allowDecimals?: boolean
  domain?: [number | 'auto' | 'dataMin', number | 'auto' | 'dataMax']
}) {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={buckets} margin={{ top: 8, right: 8, left: -12, bottom: 0 }}>
        <CartesianGrid {...GRID_STYLE} vertical={false} />
        <XAxis
          dataKey="label"
          tick={AXIS_STYLE}
          tickFormatter={tickLabel}
          tickLine={false}
          axisLine={false}
          minTickGap={20}
        />
        <YAxis
          tick={AXIS_STYLE}
          tickLine={false}
          axisLine={false}
          width={Y_AXIS_WIDTH}
          allowDecimals={allowDecimals}
          domain={domain ?? [0, 'auto']}
        />
        <Tooltip content={<ChartTooltip unit={unit} />} cursor={{ fill: 'var(--surface-hover)' }} />
        {target != null && (
          <ReferenceLine
            y={target}
            stroke="var(--text-subtle)"
            strokeDasharray="4 4"
            label={{
              value: 'target',
              fill: 'var(--text-subtle)',
              fontSize: 11,
              // Inside the plot: 'right' pushes the label past the chart edge and clips it.
              position: 'insideTopRight',
            }}
          />
        )}
        <Bar
          dataKey={dataKey as string}
          name={name}
          fill="var(--series-1)"
          radius={[4, 4, 0, 0]}
          maxBarSize={28}
        />
      </BarChart>
    </ResponsiveContainer>
  )
}

/** A single-series line chart, for measures that read as a continuous trend. */
export function SingleLineChart({
  buckets,
  dataKey,
  unit,
  name,
  domain,
  decimals = 0,
}: {
  buckets: AnalyticsBucket[]
  dataKey: keyof AnalyticsBucket
  unit: string
  name: string
  domain?: [number | 'auto', number | 'auto']
  /** Tick precision. Weight needs one decimal; a recovery score needs none. */
  decimals?: number
}) {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={buckets} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
        <CartesianGrid {...GRID_STYLE} vertical={false} />
        <XAxis
          dataKey="label"
          tick={AXIS_STYLE}
          tickFormatter={tickLabel}
          tickLine={false}
          axisLine={false}
          minTickGap={20}
        />
        <YAxis
          tick={AXIS_STYLE}
          tickLine={false}
          axisLine={false}
          width={Y_AXIS_WIDTH}
          domain={domain ?? ['auto', 'auto']}
          tickFormatter={(value: number) => formatNumber(value, decimals)}
        />
        <Tooltip content={<ChartTooltip unit={unit} />} cursor={{ stroke: 'var(--border-strong)' }} />
        <Line
          type="monotone"
          dataKey={dataKey as string}
          name={name}
          stroke="var(--series-1)"
          strokeWidth={2}
          dot={{ r: 3, strokeWidth: 0, fill: 'var(--series-1)' }}
          activeDot={{ r: 5 }}
          connectNulls
        />
      </LineChart>
    </ResponsiveContainer>
  )
}

const MACRO_SERIES = [
  { key: 'proteinG', name: 'Protein', color: 'var(--series-1)' },
  { key: 'carbsG', name: 'Carbs', color: 'var(--series-2)' },
  { key: 'fatG', name: 'Fat', color: 'var(--series-3)' },
  { key: 'fiberG', name: 'Fiber', color: 'var(--series-4)' },
] as const

/**
 * The one multi-series chart. Grams share a single axis, so stacking is honest.
 *
 * Segments are separated by a 2px stroke in the surface colour, which both reads as a gap
 * and gives the palette the secondary encoding that makes neighbouring hues safe to tell
 * apart. A legend is always present, and the table view carries the same numbers.
 */
export function MacroStackChart({ buckets }: { buckets: AnalyticsBucket[] }) {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={buckets} margin={{ top: 8, right: 8, left: -12, bottom: 0 }}>
        <CartesianGrid {...GRID_STYLE} vertical={false} />
        <XAxis
          dataKey="label"
          tick={AXIS_STYLE}
          tickFormatter={tickLabel}
          tickLine={false}
          axisLine={false}
          minTickGap={20}
        />
        <YAxis tick={AXIS_STYLE} tickLine={false} axisLine={false} width={Y_AXIS_WIDTH} />
        <Tooltip content={<ChartTooltip unit="g" />} cursor={{ fill: 'var(--surface-hover)' }} />
        <Legend
          wrapperStyle={{ fontSize: '0.75rem', color: 'var(--text-muted)', paddingTop: 8 }}
          iconType="square"
          iconSize={9}
        />
        {MACRO_SERIES.map((series, index) => (
          <Bar
            key={series.key}
            dataKey={series.key}
            name={series.name}
            stackId="macros"
            fill={series.color}
            stroke="var(--chart-surface)"
            strokeWidth={2}
            maxBarSize={28}
            radius={index === MACRO_SERIES.length - 1 ? [4, 4, 0, 0] : undefined}
          />
        ))}
      </BarChart>
    </ResponsiveContainer>
  )
}
