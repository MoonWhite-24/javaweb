-- ============================================================
-- JavaWeb Market Database Initialization
-- Database: javaweb_market
-- Engine:   MySQL 8.0, utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS javaweb_market
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE javaweb_market;

-- ============================================================
-- Table 1: user
-- ============================================================
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `username`    VARCHAR(32)  NOT NULL COMMENT 'Username',
  `password`    VARCHAR(128) NOT NULL COMMENT 'Password (SHA256+salt)',
  `salt`        VARCHAR(8)   NOT NULL COMMENT 'Password salt',
  `phone`       VARCHAR(16)  DEFAULT NULL COMMENT 'Phone number',
  `email`       VARCHAR(64)  DEFAULT NULL COMMENT 'Email',
  `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT 'Role: 0=user, 1=admin',
  `avatar`      VARCHAR(256) DEFAULT NULL COMMENT 'Avatar URL',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 1=normal, 0=disabled',
  `last_login`  DATETIME     DEFAULT NULL COMMENT 'Last login time',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- ============================================================
-- Table 2: category
-- ============================================================
CREATE TABLE `category` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
  `parent_id`   INT          NOT NULL DEFAULT 0 COMMENT 'Parent category ID, 0=root',
  `name`        VARCHAR(64)  NOT NULL COMMENT 'Category name',
  `level`       TINYINT      NOT NULL DEFAULT 1 COMMENT 'Level: 1/2/3',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `icon`        VARCHAR(128) DEFAULT NULL COMMENT 'Icon class or URL',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 1=enabled, 0=disabled',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level_status` (`level`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Category table';

-- ============================================================
-- Table 3: product
-- ============================================================
CREATE TABLE `product` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Product ID',
  `category_id` INT           NOT NULL COMMENT 'Category ID',
  `name`        VARCHAR(256)  NOT NULL COMMENT 'Product name',
  `subtitle`    VARCHAR(512)  DEFAULT NULL COMMENT 'Subtitle',
  `main_image`  VARCHAR(256)  DEFAULT NULL COMMENT 'Main image URL',
  `sub_images`  TEXT          DEFAULT NULL COMMENT 'Sub images (JSON array)',
  `detail`      MEDIUMTEXT    DEFAULT NULL COMMENT 'Rich HTML detail',
  `price`       DECIMAL(10,2) NOT NULL COMMENT 'Price (CNY)',
  `stock`       INT           NOT NULL DEFAULT 0 COMMENT 'Stock quantity',
  `sales`       INT           NOT NULL DEFAULT 0 COMMENT 'Sales volume',
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT 'Status: 1=on, 2=off, 3=deleted',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_name` (`name`(100)),
  KEY `idx_status` (`status`),
  KEY `idx_sales` (`sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product table';

-- ============================================================
-- Table 4: order
-- ============================================================
CREATE TABLE `order` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Auto PK',
  `order_no`         BIGINT        NOT NULL COMMENT 'Order number (Snowflake)',
  `user_id`          BIGINT        NOT NULL COMMENT 'User ID',
  `shipping_name`    VARCHAR(32)   DEFAULT NULL COMMENT 'Receiver name',
  `shipping_phone`   VARCHAR(16)   DEFAULT NULL COMMENT 'Receiver phone',
  `shipping_address` VARCHAR(256)  DEFAULT NULL COMMENT 'Shipping address',
  `total_price`      DECIMAL(10,2) NOT NULL COMMENT 'Total price',
  `payment`          DECIMAL(10,2) DEFAULT NULL COMMENT 'Actual payment',
  `payment_type`     TINYINT       NOT NULL DEFAULT 1 COMMENT 'Payment type: 1=online',
  `payment_trade_no` VARCHAR(64)   DEFAULT NULL COMMENT 'External trade number',
  `status`           TINYINT       NOT NULL DEFAULT 0 COMMENT '0=unpaid,1=paid,2=shipped,3=received,4=done,5=cancelled',
  `payment_time`     DATETIME      DEFAULT NULL COMMENT 'Payment time',
  `send_time`        DATETIME      DEFAULT NULL COMMENT 'Ship time',
  `end_time`         DATETIME      DEFAULT NULL COMMENT 'Complete time',
  `close_time`       DATETIME      DEFAULT NULL COMMENT 'Cancel time',
  `remark`           VARCHAR(512)  DEFAULT NULL COMMENT 'Order remark',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id_status` (`user_id`, `status`),
  KEY `idx_status_create` (`status`, `create_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order table';

-- ============================================================
-- Table 5: order_item
-- ============================================================
CREATE TABLE `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Item ID',
  `order_no`      BIGINT        NOT NULL COMMENT 'Order number',
  `user_id`       BIGINT        NOT NULL COMMENT 'User ID',
  `product_id`    BIGINT        NOT NULL COMMENT 'Product ID',
  `product_name`  VARCHAR(256)  NOT NULL COMMENT 'Product name (snapshot)',
  `product_image` VARCHAR(256)  DEFAULT NULL COMMENT 'Product image (snapshot)',
  `unit_price`    DECIMAL(10,2) NOT NULL COMMENT 'Unit price',
  `quantity`      INT           NOT NULL COMMENT 'Quantity',
  `total_price`   DECIMAL(10,2) NOT NULL COMMENT 'Line total',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order item table';

-- ============================================================
-- Table 6: seckill_product
-- ============================================================
CREATE TABLE `seckill_product` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Seckill ID',
  `product_id`      BIGINT        NOT NULL COMMENT 'Product ID',
  `seckill_price`   DECIMAL(10,2) NOT NULL COMMENT 'Seckill price',
  `stock_count`     INT           NOT NULL COMMENT 'Seckill stock',
  `start_time`      DATETIME      NOT NULL COMMENT 'Start time',
  `end_time`        DATETIME      NOT NULL COMMENT 'End time',
  `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=pending, 1=active, 2=finished',
  `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_time_range` (`start_time`, `end_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seckill product table';

-- ============================================================
-- Table 7: operate_log
-- ============================================================
CREATE TABLE `operate_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
  `user_id`     BIGINT       DEFAULT NULL COMMENT 'User ID',
  `username`    VARCHAR(32)  DEFAULT NULL COMMENT 'Username',
  `module`      VARCHAR(32)  NOT NULL COMMENT 'Module name',
  `operation`   VARCHAR(128) NOT NULL COMMENT 'Operation description',
  `params`      TEXT         DEFAULT NULL COMMENT 'Request parameters (JSON)',
  `result`      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Result: 1=success, 0=failure',
  `error_msg`   VARCHAR(512) DEFAULT NULL COMMENT 'Error message',
  `ip`          VARCHAR(64)  DEFAULT NULL COMMENT 'Client IP',
  `cost_time`   INT          DEFAULT NULL COMMENT 'Cost time (ms)',
  `trace_id`    VARCHAR(64)  DEFAULT NULL COMMENT 'Trace ID',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  PRIMARY KEY (`id`, `create_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_module` (`module`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation log table'
  PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
  );

-- ============================================================
-- Table 8: shipping_address
-- ============================================================
CREATE TABLE `shipping_address` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Address ID',
  `user_id`     BIGINT       NOT NULL COMMENT 'User ID',
  `name`        VARCHAR(32)  NOT NULL COMMENT 'Receiver name',
  `phone`       VARCHAR(16)  NOT NULL COMMENT 'Receiver phone',
  `province`    VARCHAR(32)  NOT NULL COMMENT 'Province',
  `city`        VARCHAR(32)  NOT NULL COMMENT 'City',
  `district`    VARCHAR(32)  NOT NULL COMMENT 'District',
  `detail`      VARCHAR(256) NOT NULL COMMENT 'Detail address',
  `is_default`  TINYINT      NOT NULL DEFAULT 0 COMMENT 'Default: 1=yes, 0=no',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shipping address table';
