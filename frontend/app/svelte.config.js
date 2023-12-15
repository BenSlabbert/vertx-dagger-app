import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

const config = {
	preprocess: vitePreprocess(),

	kit: {
		adapter: adapter({
			out: 'build',
			precompress: true,
			envPrefix: '',
			polyfill: false
		}),
		paths: {
			assets: '',
			base: process.env.BASE_URL
		}
	}
};

export default config;
