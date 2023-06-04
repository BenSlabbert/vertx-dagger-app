<script lang="ts">
	import routes from '$lib/routes';
	// todo show created items
	import type { ItemsResponse } from '$lib/api/catalog';
	export let data: ItemsResponse;
</script>

<div>
	<a href={routes.catalogCreate}>create new</a>
</div>

<div>
	<table>
		<thead>
			<tr>
				<th>ID</th>
				<th>Name</th>
				<th>Price In Cents</th>
			</tr>
		</thead>
		<tbody>
			{#each data.items as item}
				<tr>
					<td>{item.id}</td>
					<td>{item.name}</td>
					<td>{item.priceInCents}</td>
				</tr>
			{/each}
		</tbody><tfoot>
			<tr>
				<td>foot 1</td>
				<td>foot 2</td>
				<td>foot 3</td>
			</tr>
		</tfoot>
	</table>
</div>
