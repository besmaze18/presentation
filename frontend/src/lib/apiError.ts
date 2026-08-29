export interface FieldViolation {
  field: string
  message: string
}

export interface ApiErrorBody {
  timestamp?: string
  status?: number
  code?: string
  message?: string
  path?: string
  errors?: FieldViolation[]
}

/** Thrown for every non-2xx API response so callers get a consistent shape. */
export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: FieldViolation[]

  constructor(status: number, body: ApiErrorBody | null) {
    super(body?.message || defaultMessage(status))
    this.name = 'ApiError'
    this.status = status
    this.code = body?.code ?? 'UNKNOWN'
    this.fieldErrors = body?.errors ?? []
  }

  /** A single sentence suitable for showing in a form or toast. */
  get displayMessage(): string {
    if (this.fieldErrors.length > 0) {
      return this.fieldErrors.map((e) => `${e.field}: ${e.message}`).join('; ')
    }
    return this.message
  }
}

function defaultMessage(status: number): string {
  switch (status) {
    case 0:
      return 'You appear to be offline. Your data is safe — try again once you reconnect.'
    case 400:
      return 'The request was rejected as invalid.'
    case 401:
      return 'Your session has expired. Please sign in again.'
    case 403:
      return 'You are not allowed to do that.'
    case 404:
      return 'That record no longer exists.'
    case 409:
      return 'That conflicts with existing data.'
    case 413:
      return 'That file is too large.'
    case 502:
      return 'An external service is unavailable. Please try again.'
    default:
      return status >= 500 ? 'Something went wrong on the server.' : 'The request failed.'
  }
}
