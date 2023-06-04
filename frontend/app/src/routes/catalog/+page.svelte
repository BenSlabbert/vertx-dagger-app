<script lang="ts">
	import routes, { catalogEdit, catalogDelete } from '$lib/routes';
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
