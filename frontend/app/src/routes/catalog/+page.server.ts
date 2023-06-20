import type { PageServerLoad } from './$types';
import loggerFactory from '$lib/logger';
import { factory } from '$lib/api/catalog';
const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ fetch, locals, url }) => {
	const searchTerm = url.searchParams.get('s') as string;
	const priceFrom = url.searchParams.get('priceFrom') as any as number;
	const priceTo = url.searchParams.get('priceTo') as any as number;
	const page = url.searchParams.get('page') as any as number;

	const catalogApi = factory(fetch);

	if ((!searchTerm || searchTerm === '') && (!priceFrom || !priceTo)) {
		const items = await catalogApi.getItems({
			token: locals.user.token,
			from: page * 10,
			to: page * 10 + 10
		});

		if (items instanceof Error) {
			logger.error('failed to get items');
			return {};
		}

		return items;
	}

	const items = await catalogApi.search({
		token: locals.user.token,
		searchTerm,
		priceFrom,
		priceTo,
		from: page * 10,
		to: page * 10 + 10
	});

	if (items instanceof Error) {
		logger.error('failed to get items');
		return {};
	}

	return items;
};
