import { type Actions, fail, redirect } from '@sveltejs/kit';
import routes from '$lib/routes';
import type { PageServerLoad } from './$types';
import { zfd } from 'zod-form-data';
import loggerFactory from '$lib/logger';
import { factory } from '$lib/api/catalog';

const logger = loggerFactory(import.meta.url);

export const load: PageServerLoad = async ({ locals, fetch, params }) => {
	const catalogApi = factory(fetch);

	const item = await catalogApi.getOneItem({ token: locals.user.token, id: params.id });

	if (item instanceof Error) {
		logger.error('failed to get items');
	} else {
		return item;
	}
};

export const actions: Actions = {
	default: async ({ request, locals, fetch }) => {
		// get the form data
		const formData = await request.formData();

		// define the validation schema
		const createSchema = zfd.formData({
			id: zfd.text(),
			name: zfd.text(),
			priceInCents: zfd.numeric()
		});

		// parse the validation schema
		const result = createSchema.safeParse(formData);

		// in case of an error return the data and errors
		if (!result.success) {
			const data = {
				data: Object.fromEntries(formData),
				errors: result.error.flatten().fieldErrors
			};
			return fail(400, data);
		}

		const catalogApi = factory(fetch);

		const id = formData.get('id') as string;

		// call the catalog service
		const resp = await catalogApi.edit({
			id: id,
			token: locals.user.token,
			name: formData.get('name') as string,
			priceInCents: formData.get('priceInCents') as unknown as number
		});

		if (resp instanceof Error) {
			// failed
			logger.error(`failed to reach catalog api: ${resp}`);
			// todo display error to user
			return fail(503, {
				errors: {
					server: resp.message
				}
			});
		}

		// redirect the user
		redirect(303, routes.catalog);
	}
};
