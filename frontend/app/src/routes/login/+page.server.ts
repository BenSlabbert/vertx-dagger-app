import type { PageServerLoad } from './$types';
import { fail, redirect, type Actions } from '@sveltejs/kit';
import { zfd } from 'zod-form-data';
import routes from '../routes';
import loggerFactory from '$lib/logger';
const logger = loggerFactory(import.meta.url);

async function sleep(ms: number) {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

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
	default: async ({ request, cookies, locals }) => {
		// redirect user if logged in
		if (locals.user) {
			logger.info('user logged in already, redirect');
			throw redirect(302, routes.home);
		}

		await sleep(250);

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

		const cookie = {
			name: 'cookie-name',
			role: 'cookie-role'
		};

		cookies.set('sessionId', JSON.stringify(cookie), {
			// send cookie for every page
			path: routes.home,
			// server side only cookie so you can't use `document.cookie`
			httpOnly: true,
			// only requests from same site can send cookies
			// https://developer.mozilla.org/en-US/docs/Glossary/CSRF
			sameSite: 'strict',
			// only sent over HTTPS in production
			secure: process.env.NODE_ENV === 'production',
			// set cookie to expire after a month
			maxAge: 60 * 60 * 24 * 30
		});

		// redirect the user
		throw redirect(303, routes.home);
	}
};
