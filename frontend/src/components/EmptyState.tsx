import type { ReactNode } from 'react'

export function EmptyState({
  icon = '·',
  title,
  description,
  action,
}: {
  icon?: string
  title: string
  description?: string
  action?: ReactNode
}) {
  return (
    <div className="empty-state">
      <div className="empty-state__icon" aria-hidden="true">
        {icon}
      </div>
      <p className="list__primary">{title}</p>
      {description && <p className="text-sm mt-3">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
