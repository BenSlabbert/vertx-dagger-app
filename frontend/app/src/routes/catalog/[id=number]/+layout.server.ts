import type { PageServerLoad } from './$types';
import loggerFactory from '$lib/logger';
import { factory } from '$lib/api/catalog';

const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ locals, fetch, params }) => {
	const catalogApi = factory(fetch);

	const item = await catalogApi.getOneItem({ token: locals.user.token, id: params.id });

	if (item instanceof Error) {
		logger.error('failed to get items');
	} else {
		return item;
	}
};
