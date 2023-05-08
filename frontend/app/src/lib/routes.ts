import { base } from '$app/paths';

const routes = {
	home: base ? base : '/',
	login: `${base}/login`,
	register: `${base}/register`,
	about: `${base}/about`
};

export default routes;
