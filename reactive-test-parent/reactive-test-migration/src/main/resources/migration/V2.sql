CREATE
	TABLE
		outbox(
			id serial8 PRIMARY KEY,
			headers bytea NOT NULL,
			KEY TEXT NOT NULL,
			value bytea NOT NULL
		);
