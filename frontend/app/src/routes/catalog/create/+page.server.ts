import { fail, redirect, type Actions } from '@sveltejs/kit';
import { zfd } from 'zod-form-data';
import routes from '$lib/routes';
import loggerFactory from '$lib/logger';
import { factory } from '$lib/api/catalog';
const logger = loggerFactory(import.meta.url);

export const actions: Actions = {
	default: async ({ request, locals, fetch }) => {
		// get the form data
		const formData = await request.formData();

		// define the validation schema
		const createSchema = zfd.formData({
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

		// call the catalog service
		const resp = await catalogApi.create({
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
