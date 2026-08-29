import { useState, type FormEvent } from 'react'
import { ErrorMessage } from '@/components/ErrorMessage'
import { Field } from '@/components/Field'
import { useAnalyzeText } from './aiApi'
import type { AiAnalysis } from './aiTypes'

const EXAMPLE = '300g chicken breast, 200g cooked basmati rice and 15g olive oil'

export function AiTextEntry({ onAnalyzed }: { onAnalyzed: (analysis: AiAnalysis) => void }) {
  const [description, setDescription] = useState('')
  const analyzeText = useAnalyzeText()

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!description.trim()) return
    analyzeText.mutate(description.trim(), { onSuccess: onAnalyzed })
  }

  return (
    <form className="stack" onSubmit={handleSubmit} noValidate>
      <ErrorMessage error={analyzeText.error} />
      <Field label="Describe what you ate" hint={`For example: ${EXAMPLE}`}>
        {(props) => (
          <textarea
            {...props}
            className="textarea"
            required
            placeholder={EXAMPLE}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        )}
      </Field>
      <button className="btn" type="submit" disabled={analyzeText.isPending || !description.trim()}>
        {analyzeText.isPending ? 'Analysing…' : 'Estimate macros'}
      </button>
      <p className="text-xs text-subtle">
        You will be able to review and correct every value before anything is saved.
      </p>
    </form>
  )
}
