--
-- Table structure for table `api_key_usage_logs`
--
DROP TABLE IF EXISTS `api_key_usage_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_key_usage_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `api_key_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `endpoint` varchar(255) DEFAULT NULL,
  `ip_address` varchar(100) DEFAULT NULL,
  `method` varchar(20) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_api_key_usage_tenant` (`tenant_id`),
  KEY `idx_api_key_usage_key` (`api_key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `api_keys`
--

DROP TABLE IF EXISTS `api_keys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_keys` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `api_key_hash` varchar(64) DEFAULT NULL,
  `api_secret_hash` varchar(64) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `expiry_date` datetime(6) DEFAULT NULL,
  `ip_whitelist` text,
  `key_name` varchar(150) DEFAULT NULL,
  `masked_key` varchar(100) DEFAULT NULL,
  `permissions` text,
  `revoked_at` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','EXPIRED','REVOKED') DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_api_key_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `assets`
--

DROP TABLE IF EXISTS `assets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_name` varchar(255) NOT NULL,
  `asset_type` varchar(255) DEFAULT NULL,
  `purchase_date` varchar(255) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmxkdbqw5qai7o3kcwsllg1u1q` (`vendor_id`),
  CONSTRAINT `FKmxkdbqw5qai7o3kcwsllg1u1q` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attendance_shifts`
--

DROP TABLE IF EXISTS `attendance_shifts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_shifts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `end_time` time NOT NULL,
  `grace_minutes` int NOT NULL,
  `long_break_end_time` time DEFAULT NULL,
  `long_break_start_time` time DEFAULT NULL,
  `min_full_day_minutes` int NOT NULL,
  `min_half_day_minutes` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `short_break_end_time` time DEFAULT NULL,
  `short_break_start_time` time DEFAULT NULL,
  `start_time` time NOT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `office_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shift_name` (`name`),
  KEY `idx_shift_tenant` (`tenant_id`),
  KEY `idx_shift_office` (`office_id`),
  CONSTRAINT `FKfim1rrr7v7jswmha8fweehfse` FOREIGN KEY (`office_id`) REFERENCES `office_locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business_entities`
--

DROP TABLE IF EXISTS `business_entities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_entities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `company_name` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `entity_code` varchar(100) NOT NULL,
  `show_in_user_form` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `campaign_performance`
--

DROP TABLE IF EXISTS `campaign_performance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaign_performance` (
  `performance_id` bigint NOT NULL AUTO_INCREMENT,
  `clicks` bigint DEFAULT NULL,
  `conversions` bigint DEFAULT NULL,
  `cost` decimal(19,4) DEFAULT NULL,
  `impressions` bigint DEFAULT NULL,
  `recorded_date` date NOT NULL,
  `revenue` decimal(19,4) DEFAULT NULL,
  `campaign_id` bigint NOT NULL,
  PRIMARY KEY (`performance_id`),
  KEY `idx_perf_campaign_date` (`campaign_id`,`recorded_date`),
  CONSTRAINT `FKs2wu33yc84791j2r9ovo7nw7a` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `campaign_recipients`
--

DROP TABLE IF EXISTS `campaign_recipients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaign_recipients` (
  `campaign_id` bigint NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  KEY `FKi1w5k8nyjn12urpjki6bxk49o` (`campaign_id`),
  CONSTRAINT `FKi1w5k8nyjn12urpjki6bxk49o` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `campaigns`
--

DROP TABLE IF EXISTS `campaigns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaigns` (
  `campaign_id` bigint NOT NULL AUTO_INCREMENT,
  `archived_at` datetime(6) DEFAULT NULL,
  `audience_filters` text,
  `audience_source` varchar(50) DEFAULT NULL,
  `budget` decimal(19,2) NOT NULL,
  `campaign_name` varchar(150) NOT NULL,
  `campaign_type` varchar(50) DEFAULT NULL,
  `channel` enum('ADS','EMAIL','EMAIL_RESEND','EMAIL_SMTP','OTHER','SMS','SOCIAL','WHATSAPP') NOT NULL,
  `click_count` int DEFAULT NULL,
  `content` text,
  `description` varchar(500) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `failed_count` int DEFAULT NULL,
  `module_type` varchar(50) DEFAULT NULL,
  `open_count` int DEFAULT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `sent_count` int DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `status` enum('ACTIVE','ARCHIVED','COMPLETED','DRAFT','FAILED','PAUSED','SCHEDULED','SENDING') DEFAULT NULL,
  `subject` varchar(200) DEFAULT NULL,
  `target_audience` varchar(50) NOT NULL,
  PRIMARY KEY (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `company_profiles`
--

DROP TABLE IF EXISTS `company_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `address_line1` varchar(255) DEFAULT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `company_code` varchar(100) NOT NULL,
  `company_name` varchar(255) NOT NULL,
  `country` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `currency` varchar(50) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `favicon_url` varchar(500) DEFAULT NULL,
  `footer_image_url` varchar(500) DEFAULT NULL,
  `gst_number` varchar(100) DEFAULT NULL,
  `header_image_url` varchar(500) DEFAULT NULL,
  `logo_url` varchar(500) DEFAULT NULL,
  `pan_number` varchar(100) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `pincode` varchar(50) DEFAULT NULL,
  `registration_number` varchar(100) DEFAULT NULL,
  `signature_url` varchar(500) DEFAULT NULL,
  `stamp_url` varchar(500) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `timezone` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKn84193orqk4tpo02n04i1vdu0` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `content`
--

DROP TABLE IF EXISTS `content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content` (
  `content_id` bigint NOT NULL AUTO_INCREMENT,
  `content_title` varchar(150) NOT NULL,
  `content_type` varchar(50) DEFAULT NULL,
  `content_url` varchar(255) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `platform` varchar(50) DEFAULT NULL,
  `campaign_id` bigint DEFAULT NULL,
  PRIMARY KEY (`content_id`),
  KEY `FK2ggtjfjavb46u1ywt2hma5bn5` (`campaign_id`),
  CONSTRAINT `FK2ggtjfjavb46u1ywt2hma5bn5` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coupon_usage`
--

DROP TABLE IF EXISTS `coupon_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `learner_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `usage_count` int NOT NULL,
  `used_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKec17jt7hsq0783cegholhs6if` (`coupon_id`,`learner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `affiliate_id` bigint DEFAULT NULL,
  `auto_apply` bit(1) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `discount_cap` double DEFAULT NULL,
  `discount_type` enum('FIXED','PERCENT') NOT NULL,
  `discount_value` double NOT NULL,
  `expiry_date` datetime(6) DEFAULT NULL,
  `is_first_order_only` bit(1) DEFAULT NULL,
  `learner_id` bigint DEFAULT NULL,
  `max_usage` int DEFAULT NULL,
  `min_purchase_amount` double DEFAULT NULL,
  `per_user_limit` int DEFAULT NULL,
  `status` enum('ACTIVE','DELETED','EXPIRED','INACTIVE') DEFAULT NULL,
  `used_count` int DEFAULT NULL,
  `campaign_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeplt0kkm9yf2of2lnx6c1oy9b` (`code`),
  KEY `idx_coupon_code` (`code`),
  KEY `FKj9h2qsbp2c4vnjeak6umm8y1u` (`campaign_id`),
  CONSTRAINT `FKj9h2qsbp2c4vnjeak6umm8y1u` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `dept_code` varchar(100) NOT NULL,
  `dept_name` varchar(255) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `show_in_user_form` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `designations`
--

DROP TABLE IF EXISTS `designations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `designations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `show_in_user_form` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_campaigns`
--

DROP TABLE IF EXISTS `email_campaigns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_campaigns` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campaign_type` varchar(255) DEFAULT NULL,
  `channel` varchar(255) DEFAULT NULL,
  `content` text,
  `created_at` datetime(6) DEFAULT NULL,
  `failed_count` int DEFAULT NULL,
  `from_email` varchar(255) DEFAULT NULL,
  `from_name` varchar(255) DEFAULT NULL,
  `last_executed_at` datetime(6) DEFAULT NULL,
  `reply_to` varchar(255) DEFAULT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','CANCELLED','COMPLETED','DRAFT','FAILED','IN_PROGRESS','PAUSED','PENDING','SCHEDULED') DEFAULT NULL,
  `subject` varchar(255) NOT NULL,
  `success_count` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `total_recipients` int DEFAULT NULL,
  `trigger_event` varchar(255) DEFAULT NULL,
  `core_campaign_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt6ojrxofhgbetxx14cgpbibn4` (`core_campaign_id`),
  CONSTRAINT `FKt6ojrxofhgbetxx14cgpbibn4` FOREIGN KEY (`core_campaign_id`) REFERENCES `campaigns` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_recipients`
--

DROP TABLE IF EXISTS `email_recipients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_recipients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `failure_reason` text,
  `full_name` varchar(255) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','FAILED','PENDING','RETRYING','SENT') DEFAULT NULL,
  `campaign_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6wqah059q60cspof1jjuhqm68` (`campaign_id`,`email`),
  CONSTRAINT `FKcg5vvrw6hd6fkkb9bal35aait` FOREIGN KEY (`campaign_id`) REFERENCES `email_campaigns` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `employee_certificates`
--

DROP TABLE IF EXISTS `employee_certificates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_certificates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certificate_no` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `custom_html` text,
  `expiry_date` datetime(6) DEFAULT NULL,
  `issued_date` datetime(6) NOT NULL,
  `pdf_url` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `verification_token` varchar(255) NOT NULL,
  `template_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs9ndmeykk79t7sqbnhgyl6jwy` (`verification_token`),
  KEY `FKcg4pbfkxkjvrqkyk3vvk2ur0l` (`template_id`),
  CONSTRAINT `FKcg4pbfkxkjvrqkyk3vvk2ur0l` FOREIGN KEY (`template_id`) REFERENCES `template_definitions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `employee_types`
--

DROP TABLE IF EXISTS `employee_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `show_in_user_form` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `external_event_mappings`
--

DROP TABLE IF EXISTS `external_event_mappings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `external_event_mappings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `external_event_id` varchar(255) DEFAULT NULL,
  `internal_module` varchar(100) DEFAULT NULL,
  `internal_reference_id` bigint DEFAULT NULL,
  `metadata_json` longtext,
  `provider` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_external_event_tenant` (`tenant_id`),
  KEY `idx_external_event_provider` (`tenant_id`,`provider`,`external_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `global_users`
--

DROP TABLE IF EXISTS `global_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `global_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `tenant_code` varchar(255) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email_tenant` (`email`,`tenant_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `id_format_settings`
--

DROP TABLE IF EXISTS `id_format_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `id_format_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `entity_type` varchar(255) NOT NULL,
  `include_year` bit(1) NOT NULL,
  `next_sequence` bigint NOT NULL,
  `padding_length` int NOT NULL,
  `prefix` varchar(255) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4uyh8h04ot0gcur450cwinhv8` (`tenant_id`,`entity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integration_credentials`
--

DROP TABLE IF EXISTS `integration_credentials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_credentials` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `access_token_encrypted` text,
  `api_key_encrypted` text,
  `api_secret_encrypted` text,
  `client_id_encrypted` text,
  `client_secret_encrypted` text,
  `created_at` datetime(6) DEFAULT NULL,
  `redirect_uri` text,
  `refresh_token_encrypted` text,
  `scopes` text,
  `tenant_integration_id` bigint NOT NULL,
  `token_expiry` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgxun7n07wuj8ox18i4ie87lyf` (`tenant_integration_id`),
  CONSTRAINT `FKgxun7n07wuj8ox18i4ie87lyf` FOREIGN KEY (`tenant_integration_id`) REFERENCES `tenant_integrations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integration_definitions`
--

DROP TABLE IF EXISTS `integration_definitions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_definitions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `code` varchar(100) NOT NULL,
  `color` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  `provider` varchar(100) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr4cdn4x1x6u84geoat5pu7unx` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integration_logs`
--

DROP TABLE IF EXISTS `integration_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(150) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `error_message` text,
  `event_name` varchar(150) DEFAULT NULL,
  `http_status` int DEFAULT NULL,
  `integration_code` varchar(100) DEFAULT NULL,
  `request_payload` longtext,
  `response_payload` longtext,
  `retry_count` int DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `tenant_integration_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_integration_log_tenant` (`tenant_id`),
  KEY `idx_integration_log_code` (`tenant_id`,`integration_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integration_settings`
--

DROP TABLE IF EXISTS `integration_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `encrypted` bit(1) NOT NULL,
  `setting_key` varchar(150) DEFAULT NULL,
  `setting_value` text,
  `tenant_integration_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_setting_tenant_integration_key` (`tenant_integration_id`,`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integration_sync_history`
--

DROP TABLE IF EXISTS `integration_sync_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_sync_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `message` text,
  `records_failed` int DEFAULT NULL,
  `records_processed` int DEFAULT NULL,
  `records_success` int DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `sync_type` varchar(100) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `tenant_integration_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sync_history_tenant` (`tenant_id`),
  KEY `idx_sync_history_tenant_integration` (`tenant_integration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interactions`
--

DROP TABLE IF EXISTS `interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interactions` (
  `interaction_id` bigint NOT NULL AUTO_INCREMENT,
  `action_type` varchar(20) DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `customer_email` varchar(255) NOT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `campaign_id` bigint NOT NULL,
  PRIMARY KEY (`interaction_id`),
  KEY `idx_interaction_campaign` (`campaign_id`),
  KEY `idx_interaction_user` (`customer_email`),
  KEY `idx_interaction_content` (`content_id`),
  KEY `idx_interaction_date` (`timestamp`),
  CONSTRAINT `FKshvqt0nouxjmxqpstqf5x2vvy` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `landing_page_features`
--

DROP TABLE IF EXISTS `landing_page_features`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `landing_page_features` (
  `landing_page_id` bigint NOT NULL,
  `feature` varchar(255) DEFAULT NULL,
  KEY `FKf1pvwbw0fhv34ctusmpswrrok` (`landing_page_id`),
  CONSTRAINT `FKf1pvwbw0fhv34ctusmpswrrok` FOREIGN KEY (`landing_page_id`) REFERENCES `landing_pages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `landing_pages`
--

DROP TABLE IF EXISTS `landing_pages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `landing_pages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ad_budget` decimal(38,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `cta_text` varchar(255) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `headline` varchar(255) DEFAULT NULL,
  `landing_page_type` varchar(255) DEFAULT NULL,
  `module_type` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `slug` varchar(255) NOT NULL,
  `subtitle` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT 'DRAFT',
  `campaign_name` varchar(255) DEFAULT NULL,
  `coupon_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_lp_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lead_profiles`
--

DROP TABLE IF EXISTS `lead_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lead_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `lead_id` varchar(255) NOT NULL,
  `roll_no` varchar(255) DEFAULT NULL,
  `course_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `lead_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `marketing_leads`
--

DROP TABLE IF EXISTS `marketing_leads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `marketing_leads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_interest` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `utm_campaign` varchar(255) DEFAULT NULL,
  `utm_medium` varchar(255) DEFAULT NULL,
  `utm_source` varchar(255) DEFAULT NULL,
  `coupon` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `modules`
--

DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `is_active` bit(1) NOT NULL,
  `module_code` varchar(255) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr5wyr91uuw11l607lacs1l7us` (`module_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `office_locations`
--

DROP TABLE IF EXISTS `office_locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `office_locations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `latitude` decimal(10,7) NOT NULL,
  `longitude` decimal(10,7) NOT NULL,
  `max_accuracy_meters` int NOT NULL,
  `max_idle_minutes` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `radius_meters` double NOT NULL,
  `tenant_id` bigint NOT NULL,
  `tracking_interval_sec` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_office_location_name` (`name`),
  KEY `idx_office_location_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `onboarding_configs`
--

DROP TABLE IF EXISTS `onboarding_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onboarding_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `auto_generate_id` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `generate_certificate` bit(1) NOT NULL,
  `generate_document` bit(1) NOT NULL,
  `send_welcome_email` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `certificate_template_id` bigint DEFAULT NULL,
  `document_template_id` bigint DEFAULT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK336a6g62qwoamxsfxib64qp60` (`certificate_template_id`),
  KEY `FKoy2nllhvf0dy8mubdwy0pd0fo` (`document_template_id`),
  KEY `FKixcvtnlfk2a9gvn3x7ngcc1tm` (`role_id`),
  CONSTRAINT `FK336a6g62qwoamxsfxib64qp60` FOREIGN KEY (`certificate_template_id`) REFERENCES `template_definitions` (`id`),
  CONSTRAINT `FKixcvtnlfk2a9gvn3x7ngcc1tm` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKoy2nllhvf0dy8mubdwy0pd0fo` FOREIGN KEY (`document_template_id`) REFERENCES `template_definitions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `active` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `module` varchar(255) NOT NULL,
  `permission_key` varchar(255) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKioiivntb41pl8dflhx6ymk4dr` (`tenant_id`,`permission_key`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_stages`
--

DROP TABLE IF EXISTS `pipeline_stages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pipeline_stages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `analytic_bucket` varchar(255) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `create_task` bit(1) DEFAULT NULL,
  `label` varchar(255) NOT NULL,
  `order_index` int NOT NULL,
  `require_date` bit(1) DEFAULT NULL,
  `require_note` bit(1) DEFAULT NULL,
  `status_value` varchar(255) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pipeline_tenant` (`tenant_id`),
  KEY `idx_pipeline_order` (`tenant_id`,`order_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plan_modules`
--

DROP TABLE IF EXISTS `plan_modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_modules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `module_code` varchar(255) NOT NULL,
  `plan_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plans`
--

DROP TABLE IF EXISTS `plans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `billing_cycle` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `currency` varchar(255) NOT NULL,
  `description` text,
  `is_active` bit(1) NOT NULL,
  `plan_code` varchar(255) NOT NULL,
  `plan_name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `trial_days` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7qyjs47jwent8nhsif7rjjp3a` (`plan_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `platform_users`
--

DROP TABLE IF EXISTS `platform_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9p4hqt7xpd0abiqnt1xhg9rxs` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_order_items`
--

DROP TABLE IF EXISTS `purchase_order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand` varchar(100) DEFAULT NULL,
  `item_description` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `total_price` decimal(15,2) DEFAULT NULL,
  `unit_price` decimal(15,2) DEFAULT NULL,
  `purchase_order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo3yj8ocbw2kav38548t22hgh8` (`purchase_order_id`),
  CONSTRAINT `FKo3yj8ocbw2kav38548t22hgh8` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_orders`
--

DROP TABLE IF EXISTS `purchase_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `delivery_date` varchar(255) DEFAULT NULL,
  `notes` text,
  `order_date` varchar(255) DEFAULT NULL,
  `po_number` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpbiykvcpyg0jslne4gviyeuc2` (`po_number`),
  KEY `FKn3rssy7613r6x49ax30e2nbay` (`vendor_id`),
  CONSTRAINT `FKn3rssy7613r6x49ax30e2nbay` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `push_notifications`
--

DROP TABLE IF EXISTS `push_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `push_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `body` text NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `recipients_count` int NOT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `target_channel` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `push_subscriptions`
--

DROP TABLE IF EXISTS `push_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `push_subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `device_token` varchar(255) NOT NULL,
  `learner_id` bigint NOT NULL,
  `platform` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKei2xed55gsp1yihx6342re836` (`device_token`),
  KEY `idx_push_learner` (`learner_id`),
  KEY `idx_push_token` (`device_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `requirement_items`
--

DROP TABLE IF EXISTS `requirement_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `requirement_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand` varchar(255) DEFAULT NULL,
  `item_name` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `requirement_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKph8b91s1hvtmhrguusiob98do` (`requirement_id`),
  CONSTRAINT `FKph8b91s1hvtmhrguusiob98do` FOREIGN KEY (`requirement_id`) REFERENCES `vendor_requirements` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_extra_fields`
--

DROP TABLE IF EXISTS `role_extra_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_extra_fields` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `display_order` int DEFAULT NULL,
  `field_label` varchar(255) NOT NULL,
  `field_name` varchar(255) NOT NULL,
  `field_type` varchar(255) NOT NULL,
  `options_json` text,
  `required` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsaf71oji513q4cktt6ysqk7qg` (`role_id`,`field_name`),
  CONSTRAINT `FKrjlv73nnfr4sq5qe2xcvx2s7e` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_hierarchy`
--

DROP TABLE IF EXISTS `role_hierarchy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_hierarchy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `reports_to_role_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgsomqxvqqitqndoc76x1svcyn` (`tenant_id`,`role_id`,`reports_to_role_id`),
  KEY `FKsp6xdf87jurfjm2g2rgv4bfht` (`reports_to_role_id`),
  KEY `FK3mwupyunbl6ej65bsqwnhfj3w` (`role_id`),
  CONSTRAINT `FK3mwupyunbl6ej65bsqwnhfj3w` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKsp6xdf87jurfjm2g2rgv4bfht` FOREIGN KEY (`reports_to_role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FKegdk29eiy7mdtefy5c7eirr6e` (`permission_id`),
  CONSTRAINT `FKegdk29eiy7mdtefy5c7eirr6e` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKn5fotdgk8d1xvo8nav9uv3muc` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `code` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `show_in_user_form` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcbslvfahs4bjq6kk9glx0p1x9` (`tenant_id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subscription_plan_modules`
--

DROP TABLE IF EXISTS `subscription_plan_modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_plan_modules` (
  `plan_id` bigint NOT NULL,
  `module_name` varchar(255) DEFAULT NULL,
  KEY `FKwd0bvqutoayrsweipn08jy0l` (`plan_id`),
  CONSTRAINT `FKwd0bvqutoayrsweipn08jy0l` FOREIGN KEY (`plan_id`) REFERENCES `subscription_plans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subscription_plans`
--

DROP TABLE IF EXISTS `subscription_plans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_plans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `max_users` int DEFAULT NULL,
  `monthly_price` double NOT NULL,
  `name` varchar(255) NOT NULL,
  `yearly_price` double NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoim1kg8luw8o6q3ayhcup6vtl` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subscriptions`
--

DROP TABLE IF EXISTS `subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `amount_paid` double DEFAULT NULL,
  `amount_pending` double DEFAULT NULL,
  `billing_interval` varchar(255) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `payment_history` text,
  `payment_reference` varchar(255) DEFAULT NULL,
  `plan_id` bigint DEFAULT NULL,
  `plan_name` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6cld8vfsju3r2hur8pytoanbj` (`tenant_id`),
  CONSTRAINT `FK6cld8vfsju3r2hur8pytoanbj` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_definitions`
--

DROP TABLE IF EXISTS `template_definitions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_definitions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `background_image_url` varchar(500) DEFAULT NULL,
  `content_html` text NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `is_editable` bit(1) NOT NULL,
  `is_system_template` bit(1) NOT NULL,
  `template_code` varchar(100) NOT NULL,
  `template_name` varchar(255) NOT NULL,
  `template_type` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_integrations`
--

DROP TABLE IF EXISTS `tenant_integrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_integrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `connected` bit(1) NOT NULL,
  `connected_at` datetime(6) DEFAULT NULL,
  `connected_by` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `disconnected_at` datetime(6) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `environment` varchar(50) DEFAULT NULL,
  `health` enum('ERROR','HEALTHY','UNKNOWN','WARNING') DEFAULT NULL,
  `integration_definition_id` bigint NOT NULL,
  `last_synced_at` datetime(6) DEFAULT NULL,
  `status` enum('CONNECTED','DISCONNECTED','EXPIRED','FAILED','PENDING') DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_integration_tenant` (`tenant_id`),
  KEY `idx_tenant_integration_code` (`tenant_id`,`code`),
  KEY `FKr1r286tv8wilmjm8mwh1ogpae` (`integration_definition_id`),
  CONSTRAINT `FKr1r286tv8wilmjm8mwh1ogpae` FOREIGN KEY (`integration_definition_id`) REFERENCES `integration_definitions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_invoice_installments`
--

DROP TABLE IF EXISTS `tenant_invoice_installments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_invoice_installments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `due_date` date NOT NULL,
  `installment_no` int NOT NULL,
  `invoice_id` bigint NOT NULL,
  `paid` bit(1) NOT NULL,
  `paid_date` date DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_invoice_items`
--

DROP TABLE IF EXISTS `tenant_invoice_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_invoice_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `tax_rate` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `extra_charges` double DEFAULT NULL,
  `invoice_id` bigint NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `start_date` date DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_invoices`
--

DROP TABLE IF EXISTS `tenant_invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `due_date` date DEFAULT NULL,
  `gst_amount` double DEFAULT NULL,
  `invoice_date` date DEFAULT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `invoice_type` varchar(255) DEFAULT NULL,
  `paid_amount` double DEFAULT NULL,
  `payment_type` varchar(255) DEFAULT NULL,
  `pending_amount` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `subtotal` double DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `total_amount` double DEFAULT NULL,
  `customer_address` text,
  `gstin` varchar(50) DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `discount` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsvc6y8k138wmdt0emi5ng8emt` (`invoice_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_modules`
--

DROP TABLE IF EXISTS `tenant_modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_modules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `amount` double DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `extra_charges` double DEFAULT NULL,
  `module_name` varchar(255) NOT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `special_requirements` text,
  `start_date` date DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_tenant_module` (`tenant_id`,`module_name`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_sequences`
--

DROP TABLE IF EXISTS `tenant_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_sequences` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `current_sequence` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `year` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKba870wdfdklk3737dbxmdt4o2` (`tenant_id`,`year`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_settings`
--

DROP TABLE IF EXISTS `tenant_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `employee_id_format` varchar(255) DEFAULT NULL,
  `employee_sequence` bigint DEFAULT NULL,
  `lead_id_format` varchar(255) DEFAULT NULL,
  `lead_sequence` bigint DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqeu728yvicy5pono3chubpcic` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenants`
--

DROP TABLE IF EXISTS `tenants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `admin_email` varchar(255) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `db_name` varchar(255) NOT NULL,
  `db_password` varchar(255) DEFAULT NULL,
  `db_user` varchar(255) DEFAULT NULL,
  `domain` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `subscription_end_date` date DEFAULT NULL,
  `subscription_start_date` date DEFAULT NULL,
  `subscription_type` varchar(255) DEFAULT NULL,
  `super_admin_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKaye4nalvpsbxpjq0vt2ulaiqi` (`code`),
  UNIQUE KEY `UK4moql6miwoh3w0drxa2gmjbll` (`name`),
  UNIQUE KEY `UKehgpgu3yilhiprwm3wbipxt43` (`domain`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tracked_links`
--

DROP TABLE IF EXISTS `tracked_links`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tracked_links` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ad_budget` decimal(38,2) DEFAULT NULL,
  `campaign` varchar(255) DEFAULT NULL,
  `generated_link` varchar(255) DEFAULT NULL,
  `landing_slug` varchar(255) DEFAULT NULL,
  `medium` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `tracked_link_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `traffic_events`
--

DROP TABLE IF EXISTS `traffic_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `traffic_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `tracked_link_id` varchar(255) DEFAULT NULL,
  `utm_campaign` varchar(255) DEFAULT NULL,
  `utm_medium` varchar(255) DEFAULT NULL,
  `utm_source` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_te_tid` (`tracked_link_id`),
  KEY `idx_te_session` (`session_id`),
  KEY `idx_te_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_departments`
--

DROP TABLE IF EXISTS `user_departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_departments` (
  `user_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`department_id`),
  KEY `FKe1yilu7bslmau7ojj0n2pu812` (`department_id`),
  CONSTRAINT `FKe1yilu7bslmau7ojj0n2pu812` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `FKeklynfw1mm4x2289n61pj0ojn` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_entities`
--

DROP TABLE IF EXISTS `user_entities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_entities` (
  `user_id` bigint NOT NULL,
  `entity_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`entity_id`),
  KEY `FK5nxfgnx0xy89cvhtg944qh124` (`entity_id`),
  CONSTRAINT `FK5nxfgnx0xy89cvhtg944qh124` FOREIGN KEY (`entity_id`) REFERENCES `business_entities` (`id`),
  CONSTRAINT `FKi5v4w0tgs4xhi91dw1gl8lvgf` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_extra_field_values`
--

DROP TABLE IF EXISTS `user_extra_field_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_extra_field_values` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `field_value` text,
  `tenant_id` bigint NOT NULL,
  `field_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg5vtu56gwwt1l80duety0mj38` (`user_id`,`field_id`),
  KEY `FKf7fx805jtybodrk6otc9mt6m7` (`field_id`),
  CONSTRAINT `FKf7fx805jtybodrk6otc9mt6m7` FOREIGN KEY (`field_id`) REFERENCES `role_extra_fields` (`id`),
  CONSTRAINT `FKl2wxa81aixm8cioiuo3p8b3kr` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_modules`
--

DROP TABLE IF EXISTS `user_modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_modules` (
  `user_id` bigint NOT NULL,
  `module_name` varchar(255) DEFAULT NULL,
  KEY `FKp1l9rawdxoof0bcuh6d1os794` (`user_id`),
  CONSTRAINT `FKp1l9rawdxoof0bcuh6d1os794` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_permissions`
--

DROP TABLE IF EXISTS `user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_permissions` (
  `user_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`permission_id`),
  KEY `FKq4qlrabt4s0etm9tfkoqfuib1` (`permission_id`),
  CONSTRAINT `FKkowxl8b2bngrxd1gafh13005u` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKq4qlrabt4s0etm9tfkoqfuib1` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_reporting`
--

DROP TABLE IF EXISTS `user_reporting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_reporting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `supervisor_user_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe8wfw8va3hu47n5vfl12xxo0w` (`tenant_id`,`user_id`,`supervisor_user_id`),
  KEY `FKirsm6kkypmmi7b2hvvl2g1mvg` (`supervisor_user_id`),
  KEY `FKr7trl8jnasd6isw4vpnir7lkn` (`user_id`),
  CONSTRAINT `FKirsm6kkypmmi7b2hvvl2g1mvg` FOREIGN KEY (`supervisor_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKr7trl8jnasd6isw4vpnir7lkn` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `employee_id` varchar(100) DEFAULT NULL,
  `first_name` varchar(255) NOT NULL,
  `gender` enum('FEMALE','MALE','OTHER','PREFER_NOT_TO_SAY') DEFAULT NULL,
  `joining_date` date DEFAULT NULL,
  `last_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `office_location_id` bigint DEFAULT NULL,
  `designation_id` bigint DEFAULT NULL,
  `employee_type_id` bigint DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  `work_mode_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9nrgtpbp3g2kvyone0fkd7ry5` (`tenant_id`,`email`),
  UNIQUE KEY `UKjxejm3bfjmpvhwrjb9cr4yj35` (`tenant_id`,`employee_id`),
  KEY `FKndw7g795pd7q3csek5amfg990` (`office_location_id`),
  KEY `FK1x1ua9gs3wykwoajj3fv5s863` (`designation_id`),
  KEY `FK745coo8pvmlqt32hqbct0qw72` (`employee_type_id`),
  KEY `FKp56c1712k691lhsyewcssf40f` (`role_id`),
  KEY `FKmh3cydqn6n0m2eysmm1y2mg0i` (`work_mode_id`),
  CONSTRAINT `FK1x1ua9gs3wykwoajj3fv5s863` FOREIGN KEY (`designation_id`) REFERENCES `designations` (`id`),
  CONSTRAINT `FK745coo8pvmlqt32hqbct0qw72` FOREIGN KEY (`employee_type_id`) REFERENCES `employee_types` (`id`),
  CONSTRAINT `FKmh3cydqn6n0m2eysmm1y2mg0i` FOREIGN KEY (`work_mode_id`) REFERENCES `work_modes` (`id`),
  CONSTRAINT `FKndw7g795pd7q3csek5amfg990` FOREIGN KEY (`office_location_id`) REFERENCES `office_locations` (`id`),
  CONSTRAINT `FKp56c1712k691lhsyewcssf40f` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendor_audits`
--

DROP TABLE IF EXISTS `vendor_audits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_audits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `audit_date` varchar(255) DEFAULT NULL,
  `auditor` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `findings` text,
  `next_audit` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `type` varchar(100) NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKplkujmtugrv6ogxdkcya2wxnv` (`vendor_id`),
  CONSTRAINT `FKplkujmtugrv6ogxdkcya2wxnv` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendor_categories`
--

DROP TABLE IF EXISTS `vendor_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `description` text,
  `name` varchar(100) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsb78h8rtfw96e8jfr6g85lxwk` (`tenant_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendor_complaints`
--

DROP TABLE IF EXISTS `vendor_complaints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_complaints` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `complaint_type` varchar(100) NOT NULL,
  `date_reported` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `description` text,
  `product_or_service` varchar(255) NOT NULL,
  `resolution` text,
  `resolved_date` varchar(255) DEFAULT NULL,
  `severity` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnr4tr4968uplf7exus0petboi` (`vendor_id`),
  CONSTRAINT `FKnr4tr4968uplf7exus0petboi` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendor_contracts`
--

DROP TABLE IF EXISTS `vendor_contracts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_contracts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `amount` decimal(15,2) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `document_url` varchar(500) DEFAULT NULL,
  `expires` varchar(255) DEFAULT NULL,
  `notes` text,
  `start_date` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa5iua5qd0uremyurrcb3m7a40` (`vendor_id`),
  CONSTRAINT `FKa5iua5qd0uremyurrcb3m7a40` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendor_invoices`
--

DROP TABLE IF EXISTS `vendor_invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `amount_paid` decimal(15,2) DEFAULT NULL,
  `amount_pending` decimal(15,2) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `due_date` varchar(255) DEFAULT NULL,
  `invoice_date` varchar(255) DEFAULT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `notes` text,
  `payment_history` text,
  `po_ref` varchar(255) DEFAULT NULL,
  `receipt_url` text,
  `status` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `requirement_id` bigint DEFAULT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK99mkd20nmx2fa6lp0tsvi37ov` (`invoice_number`),
  KEY `FKemqfqt27hgj9xm6m5opacx157` (`requirement_id`),
  KEY `FKj9wrh8nnl8heg29srogfdeg24` (`vendor_id`),
  CONSTRAINT `FKemqfqt27hgj9xm6m5opacx157` FOREIGN KEY (`requirement_id`) REFERENCES `vendor_requirements` (`id`),
  CONSTRAINT `FKj9wrh8nnl8heg29srogfdeg24` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendor_requirements`
--

DROP TABLE IF EXISTS `vendor_requirements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_requirements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `description` text,
  `required_date` date DEFAULT NULL,
  `requirement_type` varchar(255) DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `vendor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKka486ho86tyhhdlxub40ajhdh` (`vendor_id`),
  CONSTRAINT `FKka486ho86tyhhdlxub40ajhdh` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendors`
--

DROP TABLE IF EXISTS `vendors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `address` text,
  `alternate_mobile_number` varchar(20) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `document_url` text,
  `email` varchar(255) NOT NULL,
  `gst_number` varchar(50) DEFAULT NULL,
  `mobile_number` varchar(20) NOT NULL,
  `pan_number` varchar(50) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `vendor_code` varchar(100) NOT NULL,
  `vendor_name` varchar(255) NOT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKdl9lj2vh3u89wfjbc7a5m8jcx` (`tenant_id`,`vendor_code`),
  UNIQUE KEY `UKfvr7h82bad2wi9s3fae6b8uyw` (`tenant_id`,`email`),
  UNIQUE KEY `UK293lapdtey2ak78o3bty3n53c` (`tenant_id`,`mobile_number`),
  UNIQUE KEY `UKbxu926yx3k16281t81hp1b835` (`tenant_id`,`gst_number`),
  UNIQUE KEY `UKqif44ovyie5rbof6r7ywtnwkg` (`tenant_id`,`pan_number`),
  KEY `FKl9qkmwxudqr9jh13eoo4bpbup` (`category_id`),
  CONSTRAINT `FKl9qkmwxudqr9jh13eoo4bpbup` FOREIGN KEY (`category_id`) REFERENCES `vendor_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `webhook_delivery_logs`
--

DROP TABLE IF EXISTS `webhook_delivery_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `webhook_delivery_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `event_name` varchar(150) DEFAULT NULL,
  `http_status` int DEFAULT NULL,
  `next_retry_at` datetime(6) DEFAULT NULL,
  `payload` longtext,
  `response` longtext,
  `retry_count` int DEFAULT NULL,
  `status` enum('FAILED','PENDING','RETRYING','SUCCESS') DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `webhook_subscription_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_webhook_delivery_tenant` (`tenant_id`),
  KEY `idx_webhook_delivery_sub` (`webhook_subscription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `webhook_subscriptions`
--

DROP TABLE IF EXISTS `webhook_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `webhook_subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `events` text,
  `name` varchar(150) DEFAULT NULL,
  `secret_key_encrypted` text,
  `tenant_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `webhook_url` text,
  PRIMARY KEY (`id`),
  KEY `idx_webhook_sub_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wfh_requests`
--

DROP TABLE IF EXISTS `wfh_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wfh_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_notes` text,
  `created_at` datetime(6) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `reason` text,
  `requested_days` int DEFAULT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `responded_by_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaxf4pthsoyg1csstiyyrlkrm6` (`responded_by_id`),
  KEY `FKb6wbx350aqarufyqbdyyk3qd8` (`user_id`),
  CONSTRAINT `FKaxf4pthsoyg1csstiyyrlkrm6` FOREIGN KEY (`responded_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKb6wbx350aqarufyqbdyyk3qd8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `work_modes`
--

DROP TABLE IF EXISTS `work_modes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_modes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `show_in_user_form` bit(1) NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invoice_configurations`
--

DROP TABLE IF EXISTS `invoice_configurations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice_configurations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `company_details` text,
  `company_logo` text,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `gst_tax_details` text,
  `invoice_name` varchar(100) NOT NULL,
  `invoice_number_format` varchar(50) DEFAULT NULL,
  `invoice_prefix` varchar(20) DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `terms_conditions` text,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_invoice_name_tenant` (`tenant_id`,`invoice_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-24 19:16:23
