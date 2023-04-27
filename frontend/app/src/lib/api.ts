type LoginRequest = {
	username: string;
	password: string;
};

type LoginResponse = {
	token: string;
	refreshToken: string;
};

type RefreshRequest = {
	username: string;
	token: string;
};

type RefreshResponse = {
	token: string;
	refreshToken: string;
};

type RegisterRequest = {
	username: string;
	password: string;
};

type RegisterResponse = {};

interface IamApi {
	login(request: LoginRequest): Promise<LoginResponse>;
	refresh(request: RefreshRequest): Promise<RefreshResponse>;
	register(request: RegisterRequest): Promise<RegisterResponse>;
}

export type {
	IamApi,
	LoginRequest,
	LoginResponse,
	RefreshRequest,
	RefreshResponse,
	RegisterRequest,
	RegisterResponse
};

export default {
	login: async (request: LoginRequest) => {
		return {
			token: '',
			refreshToken: ''
		};
	},
	refresh: async (request: RefreshRequest) => {
		return {
			token: '',
			refreshToken: ''
		};
	},
	register: async (request: RegisterRequest) => {
		return {
			token: '',
			refreshToken: ''
		};
	}
} satisfies IamApi;
