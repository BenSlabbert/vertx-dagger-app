// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
// and what to do when importing types
declare namespace App {
	interface User {
		name: string;
		role: string;
		token: string;
		refreshToken: string;
	}
	interface Locals {
		user: User;
	}
	// interface Error {}
	// interface PageData {}
	// interface Platform {}
}
