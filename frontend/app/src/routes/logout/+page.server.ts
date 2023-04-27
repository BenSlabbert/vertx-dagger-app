import { redirect } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import routes from '../routes';
import loggerFactory from '$lib/logger';
const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async () => {
	logger.info('logout load');

	// we only use this endpoint for the api
	// and don't need to see the page
	throw redirect(302, routes.home);
};

export const actions: Actions = {
	default({ cookies }) {
		// eat the cookie
		cookies.set('sessionId', '', {
			path: routes.home,
			expires: new Date(0)
		});

		// redirect the user
		throw redirect(302, routes.login);
	}
};
