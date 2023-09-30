CREATE
	TABLE
		item(
			id serial8 PRIMARY KEY,
			name VARCHAR(255) NOT NULL,
			price_in_cents int8 NOT NULL,
			version int8 NOT NULL DEFAULT 0
		);
