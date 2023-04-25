import { expect, test } from '@playwright/test';

test('index page has expected h1', async ({ page }, testInfo) => {
	await page.goto('/');
	await expect(page.getByRole('heading', { name: 'Welcome to SvelteKit' })).toBeVisible();
	await page.screenshot({ path: testInfo.outputDir + '/index.png' });
});
