<script lang="ts">
	import { page } from '$app/stores';
	import routes from './routes';
</script>

<nav>
	{#if !$page.data.user}
		<p>not logged in</p>
		{#if $page.route.id === routes.login}
			<a href={routes.register}>register</a>
		{:else}
			<a href={routes.login}>login</a>
		{/if}
	{:else}
		<p>logged in, hello: {$page.data.user.name}!</p>
		<ul>
			<li>
				<a href={routes.home}>Home</a>
			</li>
			<li>
				<a href={routes.about}>About</a>
			</li>
			<li>
				<form action="/logout" method="POST">
					<button type="submit">Log out</button>
				</form>
			</li>
		</ul>
	{/if}
</nav>
