import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AiReviewForm } from '../AiReviewForm'
import type { AiAnalysis } from '../aiTypes'
import { renderWithProviders } from './testUtils'

const analysis: AiAnalysis = {
  id: 'analysis-1',
  kind: 'IMAGE',
  status: 'PENDING_REVIEW',
  provider: 'stub',
  model: 'stub-model-1',
  suggestedName: 'Chicken, rice and olive oil',
  items: [
    {
      name: 'Chicken breast',
      quantity: 300,
      unit: 'g',
      macros: { calories: 495, proteinG: 93, carbsG: 0, fatG: 11, fiberG: 0 },
    },
    {
      name: 'Basmati rice, cooked',
      quantity: 200,
      unit: 'g',
      macros: { calories: 260, proteinG: 5.2, carbsG: 56, fatG: 0.6, fiberG: 0.8 },
    },
  ],
  totals: { calories: 755, proteinG: 98.2, carbsG: 56, fatG: 11.6, fiberG: 0.8 },
  confidence: 0.78,
  assumptions: ['Chicken was grilled without added fat'],
  notes: 'Portion sizes taken from the photo.',
  imageUrl: 'https://example.test/meal.png?expires=1&signature=abc',
  latencyMillis: 900,
  confirmedFoodEntryId: null,
  createdAt: '2026-03-10T19:00:00Z',
}

function mockFetch() {
  const fetchMock = vi.fn().mockResolvedValue(
    new Response(JSON.stringify({ id: 'entry-1' }), {
      status: 201,
      headers: { 'content-type': 'application/json' },
    }),
  )
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

function renderReview(onConfirmed = vi.fn(), onDiscarded = vi.fn()) {
  renderWithProviders(
    <AiReviewForm
      analysis={analysis}
      defaultMealType="DINNER"
      consumedAt="2026-03-10T19:00:00.000Z"
      onConfirmed={onConfirmed}
      onDiscarded={onDiscarded}
    />,
  )
  return { onConfirmed, onDiscarded }
}

describe('AiReviewForm', () => {
  beforeEach(() => {
    mockFetch()
  })

  it('presents the estimate as something to check, with confidence and assumptions', () => {
    renderReview()

    expect(
      screen.getByText(/This is an estimate\. Check the items and portions below/),
    ).toBeInTheDocument()
    expect(screen.getByText('78% confident')).toBeInTheDocument()
    expect(screen.getByText('Chicken was grilled without added fat')).toBeInTheDocument()
    expect(screen.getByText('Portion sizes taken from the photo.')).toBeInTheDocument()
    expect(screen.getByAltText('The meal you photographed')).toHaveAttribute(
      'src',
      analysis.imageUrl,
    )
  })

  it('flags a low-confidence estimate more loudly', () => {
    renderWithProviders(
      <AiReviewForm
        analysis={{ ...analysis, confidence: 0.31 }}
        defaultMealType="DINNER"
        consumedAt="2026-03-10T19:00:00.000Z"
        onConfirmed={vi.fn()}
        onDiscarded={vi.fn()}
      />,
    )

    expect(screen.getByText('31% confident — check carefully')).toBeInTheDocument()
  })

  it('recalculates the total as the user corrects an item', async () => {
    const user = userEvent.setup()
    renderReview()

    expect(screen.getByText('755 kcal')).toBeInTheDocument()

    const riceCalories = screen.getByLabelText('Calories — Basmati rice, cooked')
    await user.clear(riceCalories)
    await user.type(riceCalories, '195')

    expect(await screen.findByText('690 kcal')).toBeInTheDocument()
  })

  it('submits the corrected values, not the original prediction', async () => {
    const user = userEvent.setup()
    const fetchMock = mockFetch()
    const { onConfirmed } = renderReview()

    const riceCalories = screen.getByLabelText('Calories — Basmati rice, cooked')
    await user.clear(riceCalories)
    await user.type(riceCalories, '195')
    await user.click(screen.getByRole('button', { name: 'Save these values' }))

    await waitFor(() => expect(onConfirmed).toHaveBeenCalled())

    const [url, init] = fetchMock.mock.calls.at(-1) as [string, RequestInit]
    expect(url).toBe('/api/ai/food-analysis/analysis-1/confirm')
    const body = JSON.parse(init.body as string)
    expect(body.items).toHaveLength(2)
    expect(body.items[1].macros.calories).toBe(195)
    expect(body.mealType).toBe('DINNER')
  })

  it('lets the user drop an item the model got wrong', async () => {
    const user = userEvent.setup()
    renderReview()

    await user.click(screen.getByRole('button', { name: 'Remove Basmati rice, cooked' }))

    expect(screen.queryByLabelText('Calories — Basmati rice, cooked')).not.toBeInTheDocument()
    expect(screen.getByText('495 kcal')).toBeInTheDocument()
  })

  it('discards the estimate without logging anything', async () => {
    const user = userEvent.setup()
    const fetchMock = mockFetch()
    const { onDiscarded } = renderReview()

    await user.click(screen.getByRole('button', { name: 'Discard' }))

    await waitFor(() => expect(onDiscarded).toHaveBeenCalled())
    const [url, init] = fetchMock.mock.calls.at(-1) as [string, RequestInit]
    expect(url).toBe('/api/ai/food-analysis/analysis-1')
    expect(init.method).toBe('DELETE')
  })
})
