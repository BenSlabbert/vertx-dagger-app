import type {PageServerLoad} from './$types';
import loggerFactory from '$lib/logger';
import {factory} from '$lib/api/catalog';

const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ locals, fetch, params }) => {
	const catalogApi = factory(fetch);

	const deleted = await catalogApi.delete({ token: locals.user.token, id: params.id });

	if (deleted instanceof Error) {
		logger.error(`failed to delete item ${params.id}`);
		return {
			success: false
		};
	} else {
		return {
			success: true
		};
	}
};
