export function FullPageSpinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="full-page-spinner" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}
