<script lang="ts">
  import {page} from '$app/stores';
  import routes, {catalogDelete, catalogEdit} from '$lib/routes';
  import {Direction, type ItemsResponse} from '$lib/api/catalog';

  export let data: ItemsResponse;

	let loading = false;
	let suggestions: string[] = [];
	let search = $page.url.searchParams?.get('s') ?? '';
	let direction = $page.url.searchParams?.get('direction') ?? Direction.FORWARD.toString();
	let lastId = 0;

	$: updateableSuggestions = suggestions;
	$: {
		if (data && data.items) {
			if (data.items.length === 0) {
				lastId = 0;
			} else {
				lastId = data.items[data.items.length - 1].sequence;
			}
		}
	}

	function getPreviousPageUrl(url: URL) {
		// set direction backward
		url.searchParams.set('direction', Direction.BACKWARD.toString());

		const items = data?.items ?? [];

		if (!items || items.length === 0) {
			url.searchParams.set('lastId', String(0));
			return url.href;
		}

		url.searchParams.set('lastId', String(items[0].sequence));

		return url.href;
	}

	function getNextPageUrl(url: URL) {
		// set direction forward
		url.searchParams.set('direction', Direction.FORWARD.toString());

		const items = data?.items ?? [];

		if (!items || items.length === 0) {
			url.searchParams.set('lastId', String(0));
			return url.href;
		}

		url.searchParams.set('lastId', String(items[items.length - 1].sequence));

		return url.href;
	}

	async function onKeyUp(ignore) {
		loading = true;
		const resp = await fetch(`/api/catalog?s=${search}`);
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
			bind:value={search}
			on:keyup={onKeyUp}
		/>

		<datalist id="suggestions">
			{#each suggestions as sug, i}
				<option value={sug}>{sug}</option>
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
	</div>

	<div class="grid">
		<button type="submit" aria-busy={loading} class:secondary={loading}>Submit</button>
		<a href={routes.catalogCreate}>create new</a>
	</div>

	<div class="grid">
		<a href={getPreviousPageUrl(new URL($page.url))}>prev</a>
		<p>total: {data.page.total}</p>
		<a href={getNextPageUrl(new URL($page.url))}>next</a>
	</div>
</form>

<div>
	<table role="grid">
		<thead>
			<tr>
				<th>Name</th>
				<th>Price In Cents</th>
				<th>Action</th>
			</tr>
		</thead>
		<tbody>
			{#each data.items as item}
				<tr>
					<td>{item.name}</td>
					<td>{item.priceInCents}</td>
					<td>
						<a href={catalogEdit(item.id)}>edit</a>
						<!--todo: add confirm modal-->
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
			</tr>
		</tfoot>
	</table>
</div>
