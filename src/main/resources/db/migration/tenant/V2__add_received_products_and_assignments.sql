CREATE TABLE IF NOT EXISTS `received_products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  `requirement_item_id` bigint NOT NULL,
  `received_quantity` int NOT NULL,
  `assigned_quantity` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_received_product_vendor` (`vendor_id`),
  CONSTRAINT `fk_received_product_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`),
  CONSTRAINT `fk_received_product_req_item` FOREIGN KEY (`requirement_item_id`) REFERENCES `requirement_items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `product_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `received_product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `assigned_at` datetime(6) NOT NULL,
  `assigned_by` bigint NOT NULL,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_assignment_product` (`received_product_id`),
  KEY `fk_assignment_user` (`user_id`),
  CONSTRAINT `fk_assignment_product` FOREIGN KEY (`received_product_id`) REFERENCES `received_products` (`id`),
  CONSTRAINT `fk_assignment_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
