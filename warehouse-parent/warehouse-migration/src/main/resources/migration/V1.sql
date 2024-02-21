CREATE
	TABLE
		truck(
			id serial8 PRIMARY KEY,
			version int4 NOT NULL DEFAULT 1,
			identifier uuid NOT NULL UNIQUE,
			name text NOT NULL
		);

CREATE
	TABLE
		package(
			id serial8 PRIMARY KEY,
			version int4 NOT NULL DEFAULT 1
		);

CREATE
	TABLE
		manifest(
			id serial8 PRIMARY KEY,
			version int4 NOT NULL DEFAULT 1
		);

CREATE
	TABLE
		package_bundle(
			id serial8 PRIMARY KEY,
			version int4 NOT NULL DEFAULT 1,
			manifest_id int8 NOT NULL REFERENCES manifest(id),
			package_id int8 NOT NULL REFERENCES package(id),
			CONSTRAINT package_bundle_unq UNIQUE(
				manifest_id,
				package_id
			),
			CONSTRAINT package_unq UNIQUE(package_id)
		);

CREATE
	TYPE delivery_status AS ENUM(
		'ASSIGNED',
		'IN_TRANSIT',
		'COMPLETE'
	);

CREATE
	TABLE
		delivery(
			id serial8 PRIMARY KEY,
			version int4 NOT NULL DEFAULT 1,
			truck_id int8 NOT NULL REFERENCES truck(id),
			manifest_id int8 NOT NULL REFERENCES manifest(id),
			status delivery_status NOT NULL,
			CONSTRAINT manifest_unq UNIQUE(manifest_id),
			CONSTRAINT truck_assignment_unq UNIQUE(
				truck_id,
				manifest_id
			)
		);
