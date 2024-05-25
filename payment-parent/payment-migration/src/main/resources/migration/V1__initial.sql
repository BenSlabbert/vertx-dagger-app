CREATE
	TABLE
		payment(
			id serial8 PRIMARY KEY,
			name text NOT NULL,
			version int8 NOT NULL DEFAULT 0
		);
