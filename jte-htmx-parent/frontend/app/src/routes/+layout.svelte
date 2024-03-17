<script>
	import Nav from '$lib/component/Nav.svelte';
	import Footer from '$lib/component/Footer.svelte';

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
