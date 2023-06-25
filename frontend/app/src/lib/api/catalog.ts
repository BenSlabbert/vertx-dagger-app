type ItemsRequest = {
	token: string;
	page: number;
	size: number;
};

type Item = {
	id: string;
	name: string;
	priceInCents: number;
};

type ItemsResponse = {
	items: Item[];
};

type GetOneItemRequest = {
	token: string;
	id: string;
};

type GetOneItemResponse = {
	item: Item;
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

type EditRequest = {
	token: string;
	id: string;
	name: string;
	priceInCents: number;
};

type EditResponse = {};

type DeleteRequest = {
	token: string;
	id: string;
};

type DeleteResponse = {};

type SearchRequest = {
	token: string;
	searchTerm: string | null;
	priceFrom: number | null;
	priceTo: number | null;
	page: number;
	size: number;
};

type SearchResponse = {
	items: Item[];
};

interface CatalogApi {
	getItems(request: ItemsRequest): Promise<ItemsResponse | Error>;
	getOneItem(request: GetOneItemRequest): Promise<GetOneItemResponse | Error>;
	create(request: CreateRequest): Promise<CreateResponse | Error>;
	edit(request: EditRequest): Promise<EditResponse | Error>;
	delete(request: DeleteRequest): Promise<DeleteResponse | Error>;
	search(request: SearchRequest): Promise<SearchResponse | Error>;
}

export type {
	CatalogApi,
	ItemsRequest,
	ItemsResponse,
	CreateRequest,
	CreateResponse,
	GetOneItemRequest,
	GetOneItemResponse,
	EditRequest,
	EditResponse,
	DeleteRequest,
	DeleteResponse,
	Item
};

class CatalogApiImpl implements CatalogApi {
	fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>;

	async getItems(request: ItemsRequest): Promise<ItemsResponse | Error> {
		try {
			const resp = await this.fetch(
				`http://localhost:8081/api/items?page=${request.page}&size=${request.size}`,
				{
					method: 'GET',
					headers: {
						Authorization: 'Bearer ' + request.token
					}
				}
			);

			const json = await resp.json();

			var items: Item[] = json.items.map((j: any) => {
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

	async getOneItem(request: GetOneItemRequest): Promise<GetOneItemResponse | Error> {
		try {
			const resp = await this.fetch(`http://localhost:8081/api/${request.id}`, {
				method: 'GET',
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			const json = await resp.json();

			return {
				item: {
					id: json.id,
					name: json.name,
					priceInCents: json.priceInCents
				}
			};
		} catch (e) {
			return this.handleError(e);
		}
	}

	async create(request: CreateRequest): Promise<Error | CreateResponse> {
		try {
			const resp = await this.fetch('http://localhost:8081/api/create', {
				method: 'POST',
				body: JSON.stringify({
					name: request.name,
					priceInCents: Number(request.priceInCents)
				}),
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			const json = await resp.json();

			return {
				id: json.id,
				name: json.name,
				priceInCents: json.priceInCents
			};
		} catch (e) {
			return this.handleError(e);
		}
	}

	async edit(request: EditRequest): Promise<EditResponse | Error> {
		try {
			await this.fetch(`http://localhost:8081/api/edit/${request.id}`, {
				method: 'POST',
				body: JSON.stringify({
					name: request.name,
					priceInCents: Number(request.priceInCents)
				}),
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			return {};
		} catch (e) {
			return this.handleError(e);
		}
	}

	async delete(request: DeleteRequest): Promise<DeleteResponse | Error> {
		try {
			await this.fetch(`http://localhost:8081/api/${request.id}`, {
				method: 'DELETE',
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			return {};
		} catch (e) {
			return this.handleError(e);
		}
	}

	async search(request: SearchRequest): Promise<SearchResponse | Error> {
		try {
			let query = `page=${request.page}&size=${request.size}`;

			if (request.searchTerm) {
				query += `&s=${request.searchTerm}`;
			}

			if (request.priceFrom && request.priceTo) {
				query += `&priceFrom=${request.priceFrom}&priceTo=${request.priceTo}`;
			}

			const resp = await this.fetch(`http://localhost:8081/api/search?${query}`, {
				method: 'GET',
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			const json = await resp.json();

			var items: Item[] = json.items.map((j: any) => {
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
