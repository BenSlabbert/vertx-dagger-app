<script lang="ts">
	import routes from '$lib/routes';
    import type { PageData } from './$types';
	export let data: PageData;
</script>

<div>deleted? {data.success}</div>

<a href="{routes.catalog}">go back</a>
