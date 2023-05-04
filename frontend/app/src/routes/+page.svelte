<script lang="ts">
	import { page } from '$app/stores';
	import { browser } from '$app/environment';
	import routes from './routes';

	function redirectToLogin() {
		if (browser) {
			window.location.href = routes.login;
		}
	}
</script>

{#if $page.data.user}
	<h1>name: {$page.data.user.name}</h1>
	<h1>role: {$page.data.user.role}</h1>
{:else}
	{redirectToLogin()}
{/if}
