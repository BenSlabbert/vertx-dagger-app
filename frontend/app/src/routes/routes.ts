import { base } from '$app/paths';

const routes = {
	home: base ? base : '/',
	login: `${base}/login`,
	about: `${base}/about`
};

export default routes;
