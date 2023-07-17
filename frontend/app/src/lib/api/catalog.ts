enum Direction {
  FORWARD = 'FORWARD',
  BACKWARD= 'BACKWARD',
}

type ItemsRequest = {
	token: string;
	lastId: number;
	direction: Direction;
	size: number;
};

type Item = {
	id: string;
	sequence: number;
	name: string;
	priceInCents: number;
};

type PageInfo = {
	total: number;
};

type ItemsResponse = {
	items: Item[];
	page: PageInfo;
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
	lastId: number;
	direction: Direction;
	size: number;
};

type SearchResponse = {
	items: Item[];
	page: PageInfo;
};

type SuggestRequest = {
	token: string;
	searchTerm: string;
};

type SuggestResponse = {
	suggestions: string[];
};

interface CatalogApi {
	getItems(request: ItemsRequest): Promise<ItemsResponse | Error>;
	getOneItem(request: GetOneItemRequest): Promise<GetOneItemResponse | Error>;
	create(request: CreateRequest): Promise<CreateResponse | Error>;
	edit(request: EditRequest): Promise<EditResponse | Error>;
	delete(request: DeleteRequest): Promise<DeleteResponse | Error>;
	search(request: SearchRequest): Promise<SearchResponse | Error>;
	suggest(request: SuggestRequest): Promise<SuggestResponse | Error>;
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
	Item,
};

class CatalogApiImpl implements CatalogApi {
	fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>;

	async getItems(request: ItemsRequest): Promise<ItemsResponse | Error> {
		try {
			const resp = await this.fetch(
				`http://localhost:8081/api/items?lastId=${request.lastId}&size=${request.size}&direction=${request.direction}`,
				{
					method: 'GET',
					headers: {
						Authorization: 'Bearer ' + request.token
					}
				}
			);

			const json = await resp.json();

			const items: Item[] = json.items.map((j: any) => {
				return {
					id: j.id,
					sequence: j.sequence,
					name: j.name,
					priceInCents: j.priceInCents
				};
			});

			return {
				items,
				page: {
					total: json.total
				}
			};
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
					sequence: json.sequence,
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
			let query = `lastId=${request.lastId}&size=${request.size}&direction=${request.direction}`;

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

			const items: Item[] = json.items.map((j: any) => {
				return {
					id: j.id,
					sequence: j.sequence,
					name: j.name,
					priceInCents: j.priceInCents
				};
			});

			return {
				items,
				page: {
					total: json.total
				}
			};
		} catch (e) {
			return this.handleError(e);
		}
	}

	async suggest(request: SuggestRequest): Promise<SuggestResponse | Error> {
		try {
			const resp = await this.fetch(`http://localhost:8081/api/suggest?s=${request.searchTerm}`, {
				method: 'GET',
				headers: {
					Authorization: 'Bearer ' + request.token
				}
			});

			return resp.json();
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

export {
  Direction
};

export function factory(
	fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>
) {
	return new CatalogApiImpl(fetch);
}
