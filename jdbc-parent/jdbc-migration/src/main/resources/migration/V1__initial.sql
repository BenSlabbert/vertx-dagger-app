CREATE
	TABLE
		person(
			id BIGINT NOT NULL UNIQUE,
			name VARCHAR(255) NOT NULL
		);

CREATE
	SEQUENCE person_id_seq INCREMENT 1 START 1;

CREATE
	INDEX person_id_idx ON
	person(id);

CREATE
	TABLE
		user_data(
			id serial8 PRIMARY KEY,
			first_name TEXT NOT NULL,
			last_name TEXT NOT NULL
		);
