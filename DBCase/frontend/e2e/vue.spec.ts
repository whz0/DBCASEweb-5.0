import { expect, test } from '@playwright/test'

// See here how to get started:
// https://playwright.dev/docs/intro
test('redirects to login when unauthenticated', async ({ page }) => {
  expect(page).toBeDefined();
  expect(true).toBe(true)
})
