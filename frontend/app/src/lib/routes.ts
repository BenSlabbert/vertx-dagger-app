import { base } from '$app/paths';

const routes = {
	home: base ? base : '/',
	login: `${base}/login`,
	logout: `${base}/logout`,
	register: `${base}/register`,
	about: `${base}/about`,
	catalog: `${base}/catalog`,
	catalogCreate: `${base}/catalog/create`
};

export function catalogEdit(id: string): string {
	return `${routes.catalog}/${id}`;
}

export function catalogDelete(id: string): string {
	return `${routes.catalog}/${id}/delete`;
}

export default routes;
