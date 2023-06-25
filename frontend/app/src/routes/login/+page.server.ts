import type { PageServerLoad } from './$types';
import { fail, redirect, type Actions } from '@sveltejs/kit';
import { zfd } from 'zod-form-data';
import routes from '$lib/routes';
import loggerFactory from '$lib/logger';
import { factory } from '$lib/api/iam';
import cookieUtils from '$lib/cookie_utils';
const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ locals }) => {
	// redirect user if logged in
	logger.info(`login load, locals.user ${locals.user}`);

	if (locals.user) {
		logger.info(`user is already logged in, redirect to ${routes.home}`);
		throw redirect(303, routes.home);
	}

	// if not logged in, just render the page
	logger.info('user not logged in, just render login page');
};

export const actions: Actions = {
	default: async ({ request, cookies, locals, fetch }) => {
		// redirect user if logged in
		if (locals.user) {
			logger.info('user logged in already, redirect');
			throw redirect(302, routes.home);
		}

		// get the form data
		const formData = await request.formData();

		// define the validation schema
		const loginSchema = zfd.formData({
			user: zfd.text(),
			password: zfd.text()
		});

		// parse the validation schema
		const result = loginSchema.safeParse(formData);

		// in case of an error return the data and errors
		if (!result.success) {
			const data = {
				data: Object.fromEntries(formData),
				errors: result.error.flatten().fieldErrors
			};
			return fail(400, data);
		}

		const iamApi = factory(fetch);

		// call the iam service
		const resp = await iamApi.login({
			username: formData.get('user') as string,
			password: formData.get('password') as string
		});

		if (resp instanceof Error) {
			// failed
			logger.error(`failed to reach iam api: ${resp}`);
			// todo display error to user
			return fail(503, {
				errors: {
					server: resp.message
				}
			});
		}

		const appUser = {
			name: formData.get('user'),
			role: 'cookie-role',
			token: resp.token,
			refreshToken: resp.refreshToken
		} as App.User;

		console.log('updated refresh token', resp.refreshToken);

		await cookieUtils.set(cookies, appUser);

		// redirect the user
		throw redirect(303, routes.home);
	}
};
