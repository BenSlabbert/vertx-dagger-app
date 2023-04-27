<script lang="ts">
	import { page } from '$app/stores';
	import routes from './routes';
</script>

<nav>
	{#if !$page.data.user}
		<p>not logged in</p>
		<a href="/login">login</a>
	{/if}
	{#if $page.data.user}
		<p>logged in</p>
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
