<script lang="ts">
  import type {GetOneItemResponse} from '$lib/api/catalog';

  export let data: GetOneItemResponse;
</script>

<svelte:head>
	<title>Edit Item{data.item.id}</title>
	<meta name="description" content="Edit Item {data.item.id}" />
</svelte:head>

<slot />
