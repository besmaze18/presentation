import type { Insight } from './types'

const TONE_CLASS: Record<Insight['tone'], string> = {
  NEUTRAL: 'chip',
  POSITIVE: 'chip chip--positive',
  WARNING: 'chip chip--warning',
}

/**
 * Every figure shown here was calculated by the backend from stored data. Nothing on this list is
 * produced by a language model.
 */
export function InsightList({ insights }: { insights: Insight[] }) {
  if (insights.length === 0) {
    return <p className="text-sm text-muted">Log some food to see today's calculations.</p>
  }
  return (
    <ul className="row" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
      {insights.map((insight) => (
        <li key={insight.code} className={TONE_CLASS[insight.tone]}>
          {insight.text}
        </li>
      ))}
    </ul>
  )
}
