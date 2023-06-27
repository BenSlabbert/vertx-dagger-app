import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { factory } from '$lib/api/catalog';

export const GET = (async ({ url, fetch, locals }) => {
	const s = url.searchParams.get('s');

	if (!s) {
		return json({
			suggestions: []
		});
	}

	const catalogApi = factory(fetch);
	const suggestions = await catalogApi.suggest({ searchTerm: s, token: locals.user.token });
	return json(suggestions);
}) satisfies RequestHandler;
