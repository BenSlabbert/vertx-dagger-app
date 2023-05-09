import type { Cookies } from '@sveltejs/kit';
import { COOKIE_ID } from '$lib/constants';
import routes from '$lib/routes';

function set(cookies: Cookies, appUser: App.User) {
	cookies.set(COOKIE_ID, JSON.stringify(appUser), {
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
}

function clear(cookies: Cookies) {
	cookies.delete(COOKIE_ID, { path: routes.home });
}

function get(cookies: Cookies): string | undefined {
	return cookies.get(COOKIE_ID);
}

export default { set, clear, get };
