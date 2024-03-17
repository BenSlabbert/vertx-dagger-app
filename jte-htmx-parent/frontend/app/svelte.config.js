import adapter from '@sveltejs/adapter-static';

/** @type {import("@sveltejs/kit").Config} */
const config = {
	kit: {
		adapter: adapter({
			// may differ from host to host
			fallback: 'index.html',
			precompress: true
		}),
		paths: {
			assets: '',
			base: process.env.BASE_URL
		}
	}
};

export default config;
