import type { Handle, HandleFetch } from '@sveltejs/kit';
import routes from '$lib/routes';
import loggerFactory from '$lib/logger';
import { Buffer } from 'buffer';
import { factory } from '$lib/api/iam';
import cookieUtils from '$lib/cookie_utils';
const logger = loggerFactory(import.meta.url);

type tokenPayload = {
	exp: number;
};

// todo: main problem here is that client cookies are not being cleared/updated
// when their session expires, client still produces old cookies
export const handle: Handle = async ({ event, resolve }) => {
	logger.info(`handle request event.route.id: ${event.route.id}`);

	// https://kit.svelte.dev/docs/hooks#server-hooks-handle
	// This function runs every time the SvelteKit server receives a request — whether that happens while the app is running, or during prerendering — and determines the response

	// handle client requests to the server
	let cookieJSON = await cookieUtils.get(event.cookies);

	// if the tokens are expired we redirect, however the cookies for the event may
	// still contain expired tokens
	if (cookieJSON) {
		logger.info('we have a cookie, check if tokens are expired');
		const appUser = JSON.parse(cookieJSON) as App.User;
		logger.info(`appUser ${appUser}`);
		if (isTokenExpired(appUser.token) && isTokenExpired(appUser.refreshToken)) {
			logger.info('all tokens expired, clear cookies');
			await cookieUtils.clear(event.cookies);
			cookieJSON = undefined;
		}
	}

	// should be redirected to login page
	if (!cookieJSON) {
		logger.info('no cookie');
		if (!event.route) {
			logger.info('no route');
			// no idea why this happens
			// might be a 404
			return resolve(event);
		}

		if (routes.login === event.route.id) {
			logger.info('login route, resolve event');
			return resolve(event);
		}

		if (routes.register === event.route.id) {
			logger.info('register route, resolve event');
			return resolve(event);
		}

		logger.info('no session, redirect to login page');

		// no session and not loggin in, redirect to login page
		return Response.redirect(`${import.meta.env.VITE_APP_HOST}${routes.login}`);
	}

	// if there is a session set the user.locals here
	// login handler will set the session on the cookie
	// we can set this from the cookie or get data from another service
	// maybe cookie must not have data, just an ID
	const appUser = JSON.parse(cookieJSON) as App.User;
	logger.info(`appUser ${appUser}`);

	if (isTokenExpired(appUser.token)) {
		logger.warn('session expired, check refreshToken');

		if (isTokenExpired(appUser.refreshToken)) {
			logger.error('refreshToken expired, redirect to login');
			// both tokens expired, redirect to login
			return Response.redirect(`${import.meta.env.VITE_APP_HOST}${routes.login}`);
		}

		// refresh the token
		const iamApi = factory(event.fetch);
		logger.info('refresh token');
		const resp = await iamApi.refresh({ username: appUser.name, token: appUser.refreshToken });
		logger.info(`resp ${resp}`);
		if (resp instanceof Error) {
			logger.error(`failed to refresh token ${resp}`);
			await cookieUtils.clear(event.cookies);
			return Response.redirect(`${import.meta.env.VITE_APP_HOST}${routes.login}`);
		}

		logger.info('user refreshed, update locals');
		// update locals
		appUser.token = resp.token;
		appUser.refreshToken = resp.refreshToken;

		logger.info(`appUser ${appUser}`);
		// update the cookie
		// same code as in login server route
		await cookieUtils.set(event.cookies, appUser);
	}

	event.locals.user = appUser;

	return resolve(event);
};

function isTokenExpired(token: string | null): boolean {
	if (!token) {
		return true;
	}

	const { exp } = getTokenPaylod(token);
	if (exp === 0) return true;

	const now = Math.floor(Date.now() / 1000);
	const diff = now - exp;
	logger.info(`cooking lifetime remaining: ${diff}`);
	// refresh if there is less than 5 seconds left
	return diff > -5;
}

function getTokenPaylod(token: string): tokenPayload {
	const zero = { exp: 0 };
	if (!token) {
		return zero;
	}

	const split = token.split('.');

	if (split.length !== 3) {
		return zero;
	}

	return JSON.parse(Buffer.from(split[1], 'base64').toString('utf-8'));
}

export const handleFetch: HandleFetch = ({ request, fetch }) => {
	// https://kit.svelte.dev/docs/hooks#server-hooks-handlefetch
	// This function allows you to modify (or replace) a fetch request that happens inside a load or action function that runs on the server (or during pre-rendering).

	// handle outgoing server requests

	// server is making an external request
	logger.info(`outgoing server request ${request.url}`);
	return fetch(request);
};
