import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ManualEntryForm } from '../ManualEntryForm'
import { renderWithProviders } from './testUtils'

describe('ManualEntryForm', () => {
  it('submits trimmed values and the full macro breakdown', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    renderWithProviders(
      <ManualEntryForm
        submitLabel="Log it"
        pending={false}
        error={null}
        defaultMealType="DINNER"
        defaultConsumedAt="2026-03-10T18:30:00.000Z"
        onSubmit={onSubmit}
      />,
    )

    await user.type(screen.getByLabelText('Food or meal'), '  Chicken and rice  ')
    await user.clear(screen.getByLabelText('Calories (kcal)'))
    await user.type(screen.getByLabelText('Calories (kcal)'), '650')
    await user.clear(screen.getByLabelText('Protein (g)'))
    await user.type(screen.getByLabelText('Protein (g)'), '55')
    await user.type(screen.getByLabelText('Quantity'), '1')
    await user.type(screen.getByLabelText('Unit'), 'plate')
    await user.click(screen.getByRole('button', { name: 'Log it' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      name: 'Chicken and rice',
      mealType: 'DINNER',
      quantity: 1,
      unit: 'plate',
      macros: { calories: 650, proteinG: 55, carbsG: 0, fatG: 0, fiberG: 0 },
    })
  })

  it('keeps the submit button disabled until a name is entered', async () => {
    const user = userEvent.setup()
    renderWithProviders(
      <ManualEntryForm submitLabel="Log it" pending={false} error={null} onSubmit={vi.fn()} />,
    )

    const submit = screen.getByRole('button', { name: 'Log it' })
    expect(submit).toBeDisabled()

    await user.type(screen.getByLabelText('Food or meal'), 'Oats')
    expect(submit).toBeEnabled()
  })

  it('pre-fills from an existing entry and offers deletion', async () => {
    const onDelete = vi.fn()
    renderWithProviders(
      <ManualEntryForm
        submitLabel="Save changes"
        pending={false}
        error={null}
        onDelete={onDelete}
        onSubmit={vi.fn()}
        initial={{
          id: 'entry-1',
          name: 'Greek yoghurt',
          mealType: 'BREAKFAST',
          consumedAt: '2026-03-10T07:00:00.000Z',
          entryDate: '2026-03-10',
          quantity: 200,
          unit: 'g',
          macros: { calories: 180, proteinG: 18, carbsG: 12, fatG: 6, fiberG: 0 },
          source: 'MANUAL',
          aiAnalysisId: null,
          savedFoodId: null,
          notes: null,
          items: [],
          createdAt: '2026-03-10T07:00:00.000Z',
        }}
      />,
    )

    expect(screen.getByLabelText('Food or meal')).toHaveValue('Greek yoghurt')
    expect(screen.getByLabelText('Calories (kcal)')).toHaveValue(180)
    expect(screen.getByRole('button', { name: 'Delete' })).toBeInTheDocument()
    expect(onDelete).not.toHaveBeenCalled()
  })
})
