<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import routes, { catalogEdit, catalogDelete } from '$lib/routes';
	import type { ItemsResponse } from '$lib/api/catalog';
	export let data: ItemsResponse;

	async function onClickNextPage(event: SubmitEvent) {
		if (event instanceof PointerEvent) {
			return;
		}

		const formEl = event.target as HTMLFormElement;
		const url = new URL(formEl.action);
		let keys = [];
		for (const key of url.searchParams.keys()) {
			keys.push(key);
		}
		// clear search params
		keys.forEach((k) => url.searchParams.delete(k));

		const submitter = event.submitter as HTMLElement;
		const submitterName = submitter.getAttribute('name');
		const formData = new FormData(formEl);
		let currentPage = Number(formData.get('page') ?? 0) as number;

		// todo: terrible, fix
		outer: if (submitterName === 'previous') {
			if (currentPage > 0) {
				currentPage--;
			}
		} else if (submitterName === 'next') {
			if (data.items.length < 10) {
				// do nothing
				break outer;
			}

			if (currentPage >= 0) {
				currentPage++;
			} else if (currentPage < 0) {
				currentPage = 0;
			}
		} else {
			// default start search on page 0
			currentPage = 0;
		}

		for (const key of formData.keys()) {
			if (key === 'page') {
				url.searchParams.set(key, String(currentPage));
				continue;
			}

			url.searchParams.set(key, String(formData.get(key)));
		}

		goto(url.pathname + '?' + url.searchParams.toString());
	}
</script>

<div>
	<a href={routes.catalogCreate}>create new</a>
</div>

<form method="GET" on:submit|preventDefault={onClickNextPage}>
	<div class="grid">
		<input type="text" placeholder="name" name="s" value={$page.url.searchParams?.get('s') ?? ''} />

		<input
			type="number"
			name="priceFrom"
			placeholder="price from"
			value={$page.url.searchParams?.get('priceFrom') ?? null}
		/>

		<input
			type="number"
			name="priceTo"
			placeholder="price to"
			value={$page.url.searchParams?.get('priceTo') ?? null}
		/>

		<input
			type="text"
			hidden
			name="page"
			readonly
			value={$page.url.searchParams?.get('page') ?? 0}
		/>

		<button type="submit"> Submit </button>
	</div>

	<div class="grid">
		<button type="submit" name="previous" class="contrast outline">previous</button>

		<input type="text" name="page" readonly value={$page.url.searchParams?.get('page') ?? 0} />

		<button type="submit" name="next" class="contrast outline">next</button>
	</div>
</form>

<div>
	<table role="grid">
		<thead>
			<tr>
				<th>ID</th>
				<th>Name</th>
				<th>Price In Cents</th>
				<th>Action</th>
			</tr>
		</thead>
		<tbody>
			{#each data.items as item}
				<tr>
					<td>{item.id}</td>
					<td>{item.name}</td>
					<td>{item.priceInCents}</td>
					<td>
						<a href={catalogEdit(item.id)}>edit</a>
						<a href={catalogDelete(item.id)}>delete</a>
					</td>
				</tr>
			{/each}
		</tbody>
		<tfoot>
			<tr>
				<td>foot 1</td>
				<td>foot 2</td>
				<td>foot 3</td>
				<td>foot 4</td>
			</tr>
		</tfoot>
	</table>
</div>
