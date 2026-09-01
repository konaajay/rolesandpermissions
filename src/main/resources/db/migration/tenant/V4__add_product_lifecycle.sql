ALTER TABLE `requirement_items` ADD COLUMN `item_type` varchar(20) DEFAULT 'ASSET';

ALTER TABLE `product_assignments` ADD COLUMN `status` varchar(50) DEFAULT 'ASSIGNED';
ALTER TABLE `product_assignments` ADD COLUMN `asset_identifier` varchar(100) DEFAULT NULL;
ALTER TABLE `product_assignments` ADD COLUMN `replaced_by_assignment_id` bigint DEFAULT NULL;
ALTER TABLE `product_assignments` ADD CONSTRAINT `fk_assignment_replaced_by` FOREIGN KEY (`replaced_by_assignment_id`) REFERENCES `product_assignments` (`id`);

CREATE TABLE IF NOT EXISTS `product_lifecycle_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `assignment_id` bigint NOT NULL,
  `event_type` varchar(50) NOT NULL,
  `previous_status` varchar(50) DEFAULT NULL,
  `new_status` varchar(50) NOT NULL,
  `performed_by` bigint NOT NULL,
  `assigned_to` bigint DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_lifecycle_assignment` (`assignment_id`),
  CONSTRAINT `fk_lifecycle_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `product_assignments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
