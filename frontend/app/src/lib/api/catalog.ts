type ItemsRequest = {
	token: string;
	from: number;
	to: number;
};

type Items = {
	id: string;
	name: string;
	priceInCents: number;
};

type ItemsResponse = {
	items: Items[];
};

type CreateRequest = {
	token: string;
	name: string;
	priceInCents: number;
};

type CreateResponse = {
	id: string;
	name: string;
	priceInCents: number;
};

interface CatalogApi {
	items(request: ItemsRequest): Promise<ItemsResponse | Error>;
	create(request: CreateRequest): Promise<CreateResponse | Error>;
}

export type { CatalogApi, ItemsRequest, ItemsResponse, CreateRequest, CreateResponse, Items };

class CatalogApiImpl implements CatalogApi {
	fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>;

	async items(request: ItemsRequest): Promise<ItemsResponse | Error> {
		try {
			const resp = await this.fetch(
				`http://localhost:8081/api/items?from=${request.from}&to=${request.to}`,
				{
					method: 'GET',
					headers: {
						Authorization: 'Bearer ' + request.token
					}
				}
			);

			const json = await resp.json();

			var items: Items[] = json.items.map((j: any) => {
				return {
					id: j.id,
					name: j.name,
					priceInCents: j.priceInCents
				};
			});

			return { items };
		} catch (e) {
			return this.handleError(e);
		}
	}

	async create(request: CreateRequest): Promise<Error | CreateResponse> {
		try {
			const body = JSON.stringify({
				name: request.name,
				priceInCents: Number(request.priceInCents)
			});
			console.log('body: ', body);

			const resp = await this.fetch('http://localhost:8081/api/create', {
				method: 'POST',
				body: body,
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			const json = await resp.json();

			console.log('json: ', json);

			return {
				id: json.id,
				name: json.name,
				priceInCents: json.priceInCents
			};
		} catch (e) {
			return this.handleError(e);
		}
	}

	private handleError(e: any): Error {
		if (e instanceof Error) {
			return e;
		}
		return new Error('unknown error');
	}

	constructor(
		fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>
	) {
		this.fetch = fetch;
	}
}

export function factory(
	fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>
) {
	return new CatalogApiImpl(fetch);
}
