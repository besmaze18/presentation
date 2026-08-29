import { ApiError } from '@/lib/apiError'

export function ErrorMessage({ error }: { error: unknown }) {
  if (!error) return null
  const message =
    error instanceof ApiError
      ? error.displayMessage
      : error instanceof Error
        ? error.message
        : 'Something went wrong.'
  return (
    <div className="alert alert--error" role="alert">
      {message}
    </div>
  )
}
