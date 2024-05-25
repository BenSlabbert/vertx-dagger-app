CREATE
	TABLE
		purchase_order(
			id serial8 PRIMARY KEY,
			version int8 NOT NULL DEFAULT 0,
			quantity int8 NOT NULL,
			item_id int8 NOT NULL
		);

CREATE
	INDEX purchase_order_item_id_idx ON
	purchase_order(item_id);

ALTER TABLE
	purchase_order ADD CONSTRAINT purchase_order_item_id_fk FOREIGN KEY(item_id) REFERENCES item(id);
