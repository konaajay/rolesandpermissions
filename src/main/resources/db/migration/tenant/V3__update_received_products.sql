SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE product_assignments;
TRUNCATE TABLE received_products;
SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE received_products DROP COLUMN product_name;
ALTER TABLE received_products ADD COLUMN requirement_item_id BIGINT NOT NULL;
ALTER TABLE received_products ADD CONSTRAINT fk_received_product_req_item FOREIGN KEY (requirement_item_id) REFERENCES requirement_items (id);
