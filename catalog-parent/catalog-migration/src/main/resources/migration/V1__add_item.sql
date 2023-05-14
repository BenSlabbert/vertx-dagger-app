CREATE
	TABLE
		item(
			id uuid NOT NULL UNIQUE PRIMARY KEY DEFAULT gen_random_uuid(),
			name VARCHAR(255) NOT NULL UNIQUE,
			price_in_cents int8 NOT NULL
		);

CREATE
	INDEX item_id_idx ON
	item(id);
