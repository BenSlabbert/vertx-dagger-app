CREATE
	TABLE
		user_projection(
			id serial8 PRIMARY KEY,
			user_id int8 NOT NULL,
			version int8 NOT NULL DEFAULT 0
		);

CREATE
	INDEX user_projection_user_id_idx ON
	user_projection(user_id);

CREATE
	TABLE
		account(
			id serial8 PRIMARY KEY,
			user_id int8 NOT NULL REFERENCES user_projection(id),
			name text NOT NULL,
			version int8 NOT NULL DEFAULT 0
		);

CREATE
	INDEX account_user_id_idx ON
	account(user_id);

CREATE
	TABLE
		saga(
			id serial8 PRIMARY KEY,
			reference text NOT NULL
		);
