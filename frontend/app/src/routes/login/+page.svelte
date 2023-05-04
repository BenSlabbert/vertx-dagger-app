<script lang="ts">
	import { enhance, type SubmitFunction } from '$app/forms';
	import type { ActionData } from './$types';

	export let form: ActionData;
	let loading = false;

	// for some reason this is using window.fetch and trigger the warning on the console
	// need to investigate why
	const processLogin: SubmitFunction = (input) => {
		loading = true;
		// do something before the form submits
		console.log('input', input);
		console.log('loading', loading);

		return async ({ update, result }) => {
			console.log('result.status', result.status);
			// do something after the form submits
			loading = false;
			console.log('loading', loading);
			await update();
		};
	};
</script>

{#if form?.errors?.server}
	<div>
		<p>{form?.errors?.server}</p>
	</div>
{/if}

<form method="POST" use:enhance={processLogin}>
	<input type="text" name="user" value={form?.data?.user ?? ''} />
	{#if form?.errors?.user}
		<p class="error">Name is required</p>
	{/if}

	<input type="password" name="password" value={form?.data?.password ?? ''} />
	{#if form?.errors?.password}
		<p class="error">Password is required</p>
	{/if}

	<button aria-busy={loading} class:secondary={loading} type="submit">
		{#if !loading}Login{/if}
	</button>
</form>

<style>
	.error {
		color: tomato;
	}
</style>
