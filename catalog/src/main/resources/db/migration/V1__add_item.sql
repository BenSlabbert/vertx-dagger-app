CREATE
	TABLE
		item(
			id serial8 PRIMARY KEY,
			external_ref uuid NOT NULL UNIQUE DEFAULT gen_random_uuid(),
			name VARCHAR(255) NOT NULL UNIQUE,
			price_in_cents int8 NOT NULL
		);

CREATE
	INDEX item_id_idx ON
	item(id);

CREATE
	INDEX item_external_ref_idx ON
	item(external_ref);
