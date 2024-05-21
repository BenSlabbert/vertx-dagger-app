import type {PageServerLoad} from './$types';
import loggerFactory from '$lib/logger';
import {Direction, factory} from '$lib/api/catalog';

const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ fetch, locals, url }) => {
	const searchTerm = url.searchParams.get('s') as string;
	const priceFrom = Number(url.searchParams.get('priceFrom')) | 0;
	const priceTo = Number(url.searchParams.get('priceTo')) | 0;
	const lastId = Number(url.searchParams.get('lastId')) | 0;
	const direction =
		Direction.BACKWARD.toString() === url.searchParams.get('direction')
			? Direction.BACKWARD
			: Direction.FORWARD;

	const catalogApi = factory(fetch);

	if ((!searchTerm || searchTerm === '') && (priceFrom === 0 || priceTo === 0)) {
		const items = await catalogApi.getItems({
			token: locals.user.token,
			lastId,
			direction,
			size: 10
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
		lastId,
		direction,
		size: 10
	});

	if (items instanceof Error) {
		logger.error('failed to get items');
		return {};
	}

	return items;
};
