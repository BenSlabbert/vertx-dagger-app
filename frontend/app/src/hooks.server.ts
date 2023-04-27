import type { Handle, HandleFetch } from '@sveltejs/kit';
import routes from './routes/routes';
import loggerFactory from '$lib/logger';
const logger = loggerFactory(import.meta.url);

export const handle: Handle = async ({ event, resolve }) => {
	logger.info(`handle request ${event.route.id}`);

	// https://kit.svelte.dev/docs/hooks#server-hooks-handle
	// This function runs every time the SvelteKit server receives a request — whether that happens while the app is running, or during prerendering — and determines the response

	// handle client requests to the server

	const sessionId = event.cookies.get('sessionId');

	// should be redirected to login page
	if (!sessionId) {
		if (!event.route) {
			// no idea why this happens
			return await resolve(event);
		}

		if (routes.login === event.route.id) {
			logger.info('login route, resolve event');
			return await resolve(event);
		}

		// no session and not loggin in, redirect to login page
		return Response.redirect(`${import.meta.env.VITE_APP_HOST}${routes.login}`);
	}

	// if there is a session set the user.locals here
	// login handler will set the session on the cookie
	// we can set this from the cookie or get data from another service
	// maybe cookie must not have data, just an ID
	const cookie = event.cookies.get('sessionId');
	logger.info(`cookie ${cookie}`);
	const c = JSON.parse(cookie ? cookie : '{}');

	event.locals.user = {
		name: c.name,
		role: c.role
	};

	return await resolve(event);
};

export const handleFetch: HandleFetch = ({ request, fetch }) => {
	// https://kit.svelte.dev/docs/hooks#server-hooks-handlefetch
	// This function allows you to modify (or replace) a fetch request that happens inside a load or action function that runs on the server (or during pre-rendering).

	// handle outgoing server requests

	// server is making an external request
	logger.info(`outgoing server request ${request.url}`);
	return fetch(request);
};
