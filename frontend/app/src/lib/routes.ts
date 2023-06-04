import { base } from '$app/paths';

const routes = {
	home: base ? base : '/',
	login: `${base}/login`,
	register: `${base}/register`,
	about: `${base}/about`,
	catalog: `${base}/catalog`,
	catalogCreate: `${base}/catalog/create`
};

export default routes;
