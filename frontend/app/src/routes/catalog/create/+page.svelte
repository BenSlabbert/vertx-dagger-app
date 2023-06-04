<script lang="ts">
	import { enhance, type SubmitFunction } from '$app/forms';
	import type { ActionData } from './$types';

	export let form: ActionData;
	let loading = false;

	const processLogin: SubmitFunction = (input) => {
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

<!-- todo add form to create item  -->
<div>create item</div>

<form method="POST" use:enhance={processLogin}>
	<input type="text" name="name" value={form?.data?.name ?? ''} />

	<input type="number" name="priceInCents" value={form?.data?.priceInCents ?? ''} />

	<button aria-busy={loading} class:secondary={loading} type="submit">
		{#if !loading}Create{/if}
	</button>
</form>
