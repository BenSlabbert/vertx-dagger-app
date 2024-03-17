<script>
	const host = import.meta.env.VITE_JTE_HOST;
</script>

<button hx-post="{host}/clicked?q=345" hx-swap="outerHTML">Click Me</button>
