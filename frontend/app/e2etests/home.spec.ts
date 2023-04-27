import { expect, test } from '@playwright/test';

// https://playwright.dev/docs/codegen#recording-a-test
test('login form', async ({ page }, testInfo) => {
	await page.goto('/login/');
	await expect(page.getByText('not logged in')).toBeVisible();
	await expect(page.getByText('this is the footer')).toBeVisible();
	await expect(page.locator('input[name="user"]')).toBeVisible();
	await expect(page.locator('input[name="password"]')).toBeVisible();
	await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();

	await page.screenshot({ path: testInfo.outputDir + '/index.png' });
});
