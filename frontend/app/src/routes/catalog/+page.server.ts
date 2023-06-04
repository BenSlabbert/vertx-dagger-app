import type { PageServerLoad } from './$types';
import loggerFactory from '$lib/logger';
import { factory } from '$lib/api/catalog';
const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ fetch, locals }) => {
	const catalogApi = factory(fetch);

	const items = await catalogApi.items({ token: locals.user.token, from: 0, to: 10 });

	if (items instanceof Error) {
		logger.error('failed to get items');
	} else {
		return items;
	}
};
