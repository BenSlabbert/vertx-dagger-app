<script lang="ts">
  import {enhance, type SubmitFunction} from '$app/forms';
  import type {ActionData} from './$types';

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

<div>create item</div>

<form method="POST" use:enhance={process}>
	<input type="text" name="name" value={form?.data?.name ?? ''} />
	{#if form?.errors?.name}
		<p class="error">Name is required</p>
	{/if}

	<input type="number" name="priceInCents" value={form?.data?.priceInCents ?? ''} />
	{#if form?.errors?.priceInCents}
		<p class="error">priceInCents is required</p>
	{/if}

	<button aria-busy={loading} class:secondary={loading} type="submit">
		{#if !loading}Create{/if}
	</button>
</form>

<style>
	.error {
		color: tomato;
	}
</style>
