import { useRef, useState, type ChangeEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { useAnalyzeImage } from './aiApi'
import type { AiAnalysis } from './aiTypes'

const MAX_BYTES = 10 * 1024 * 1024

export function AiPhotoEntry({ onAnalyzed }: { onAnalyzed: (analysis: AiAnalysis) => void }) {
  const [file, setFile] = useState<File | null>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [hint, setHint] = useState('')
  const [localError, setLocalError] = useState<string | null>(null)
  const cameraInputRef = useRef<HTMLInputElement>(null)
  const libraryInputRef = useRef<HTMLInputElement>(null)
  const analyzeImage = useAnalyzeImage()

  function choose(event: ChangeEvent<HTMLInputElement>) {
    const selected = event.target.files?.[0] ?? null
    setLocalError(null)
    if (!selected) return
    if (selected.size > MAX_BYTES) {
      setLocalError('That image is larger than 10 MB. Try a smaller photo.')
      return
    }
    setFile(selected)
    setPreviewUrl((current) => {
      if (current) URL.revokeObjectURL(current)
      return URL.createObjectURL(selected)
    })
  }

  return (
    <div className="stack">
      <ErrorMessage error={analyzeImage.error} />
      {localError && (
        <div className="alert alert--error" role="alert">
          {localError}
        </div>
      )}

      {previewUrl && (
        <img
          src={previewUrl}
          alt="Selected meal"
          style={{ width: '100%', borderRadius: 'var(--radius)', maxHeight: 260, objectFit: 'cover' }}
        />
      )}

      {/*
        Two inputs rather than one: `capture` opens the camera directly on a phone, which is the
        common case, while the second lets anyone pick an existing photo. Desktop browsers ignore
        `capture` and show a file picker, so both paths always work.
      */}
      <input
        ref={cameraInputRef}
        className="visually-hidden"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        capture="environment"
        onChange={choose}
        aria-hidden="true"
        tabIndex={-1}
      />
      <input
        ref={libraryInputRef}
        className="visually-hidden"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        onChange={choose}
        aria-hidden="true"
        tabIndex={-1}
      />

      <div className="row">
        <button type="button" className="btn" onClick={() => cameraInputRef.current?.click()}>
          📷 Take a photo
        </button>
        <button
          type="button"
          className="btn btn--secondary"
          onClick={() => libraryInputRef.current?.click()}
        >
          Choose a photo
        </button>
      </div>

      <Field label="Anything to add?" hint="Optional — e.g. 'the rice bowl is about 300 g'">
        {(props) => (
          <input
            {...props}
            className="input"
            value={hint}
            onChange={(event) => setHint(event.target.value)}
          />
        )}
      </Field>

      <button
        type="button"
        className="btn"
        disabled={!file || analyzeImage.isPending}
        onClick={() => file && analyzeImage.mutate({ file, hint }, { onSuccess: onAnalyzed })}
      >
        {analyzeImage.isPending ? 'Analysing photo…' : 'Analyse photo'}
      </button>

      <p className="text-xs text-subtle">
        The photo is stored with your account and the estimate is shown for review before anything
        is saved.
      </p>
    </div>
  )
}
