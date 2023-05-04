<script>
	import Nav from './Nav.svelte';
	import Footer from './Footer.svelte';
	// import global styles
	import './styles.css';
</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="App" />
</svelte:head>

<header>
	<Nav />
</header>

<main>
	<slot />
</main>

<footer>
	<Footer />
</footer>

<style>
	:global(body) {
		padding: 2rem;
	}

	:global(input, button) {
		border-radius: 1rem;
	}

	main {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding: 1rem;
		width: 100%;
		max-width: 64rem;
		margin: 0 auto;
		box-sizing: border-box;
	}

	footer {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		padding: 12px;
	}

	@media (min-width: 480px) {
		footer {
			padding: 12px 0;
		}
	}
</style>
