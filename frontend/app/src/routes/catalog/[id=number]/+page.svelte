<script lang="ts">
  import {enhance, type SubmitFunction} from '$app/forms';
  import type {ActionData} from './$types';
  import type {GetOneItemResponse} from '$lib/api/catalog';

  export let data: GetOneItemResponse;
	export let form: ActionData;
	let loading = false;

	const process: SubmitFunction = (input) => {
		loading = true;
		// do something before the form submits
		console.log('input', input);
		console.log('loading', loading);

		return async ({ update }) => {
			// do something after the form submits
			loading = false;
			await update();
		};
	};
</script>

{#if form?.errors?.server}
	<div>
		<p>{form?.errors?.server}</p>
	</div>
{/if}

<div>edit item: {data.item.id}</div>

<form method="POST" use:enhance={process}>
	<input type="text" name="id" hidden value={data.item.id} />

	<input type="text" name="name" value={data.item.name} />
	{#if form?.errors?.name}
		<p class="error">Name is required</p>
	{/if}

	<input type="number" name="priceInCents" value={data.item.priceInCents} />
	{#if form?.errors?.priceInCents}
		<p class="error">priceInCents is required</p>
	{/if}

	<button aria-busy={loading} class:secondary={loading} type="submit">
		{#if !loading}Submit{/if}
	</button>
</form>

<style>
	.error {
		color: tomato;
	}
</style>
