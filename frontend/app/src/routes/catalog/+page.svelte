<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import routes, { catalogEdit, catalogDelete } from '$lib/routes';
	import type { ItemsResponse } from '$lib/api/catalog';
	export let data: ItemsResponse;
	let loading = false;

	let suggestions: string[] = [];

	function getPreviousPageUrl(url: URL) {
		let pageParam = Number(url.searchParams.get('page')) | 0;

		if (pageParam <= 0) {
			console.log('pageParam is zero, previous redirects to current url');
			return url.href;
		}

		pageParam--;
		url.searchParams.set('page', pageParam);

		return url.href;
	}

	function getNextPageUrl(url: URL) {
		let pageParam = Number(url.searchParams.get('page')) | 0;
		console.log('next pageParam', pageParam);

		if (data.items.length < 10) {
			console.log('last page, next redirects to current url');
			return url.href;
		}

		pageParam++;
		url.searchParams.set('page', pageParam);

		return url.href;
	}

	async function onKeyDown(e) {
		loading = true;
		const resp = await fetch(`/api/catalog?s=${e.key}`);
		const data = await resp.json();
		loading = false;
		suggestions = data.suggestions;
	}
</script>

<form method="GET">
	<div class="grid">
		<input
			list="suggestions"
			type="text"
			placeholder="name"
			name="s"
			value={$page.url.searchParams?.get('s') ?? ''}
			on:keydown={onKeyDown}
		/>

		<datalist id="suggestions">
			{#each suggestions as sug}
				<option value={sug} />
			{/each}
		</datalist>

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
	</div>

	<div class="grid">
		<button type="submit" aria-busy={loading} class:secondary={loading}> Submit </button>
		<a href={routes.catalogCreate}>create new</a>
	</div>

	<div class="grid">
		<a href={getPreviousPageUrl(new URL($page.url))}>prev</a>
		<p>page: {$page.url.searchParams?.get('page') ?? 0}</p>
		<a href={getNextPageUrl(new URL($page.url))}>next</a>
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
