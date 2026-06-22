-- ============================================================
-- JavaWeb Market Seed Data
-- ============================================================

USE javaweb_market;

-- ============================================================
-- Admin user: admin / admin123
-- salt=qK7xR9mW, SHA256("qK7xR9mWadmin123") = ...
-- ============================================================
INSERT INTO `user` (`username`, `password`, `salt`, `phone`, `email`, `role`, `status`)
VALUES
('admin', 'f6c9f5d0a5e8b7c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0', 'qK7xR9mW', '13800000001', 'admin@market.com', 1, 1),
('testuser', 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0', 'aB3cD5eF', '13800000002', 'test@market.com', 0, 1);

-- ============================================================
-- Categories (3-level tree)
-- ============================================================
INSERT INTO `category` (`id`, `parent_id`, `name`, `level`, `sort_order`, `status`) VALUES
-- Level 1
(1,  0, 'Electronics',    1, 1, 1),
(2,  0, 'Clothing',       1, 2, 1),
(3,  0, 'Food & Drinks',  1, 3, 1),
-- Level 2: Electronics
(4,  1, 'Smartphones',    2, 1, 1),
(5,  1, 'Laptops',        2, 2, 1),
(6,  1, 'Tablets',        2, 3, 1),
(7,  1, 'Accessories',    2, 4, 1),
-- Level 2: Clothing
(8,  2, 'Men',            2, 1, 1),
(9,  2, 'Women',          2, 2, 1),
(10, 2, 'Kids',           2, 3, 1),
-- Level 2: Food & Drinks
(11, 3, 'Snacks',         2, 1, 1),
(12, 3, 'Beverages',      2, 2, 1),
(13, 3, 'Fresh Produce',  2, 3, 1),
-- Level 3
(14, 4, 'Android Phones',  3, 1, 1),
(15, 4, 'iOS Phones',      3, 2, 1),
(16, 5, 'Gaming Laptops',  3, 1, 1),
(17, 5, 'Ultrabooks',      3, 2, 1),
(18, 8, 'T-Shirts',        3, 1, 1),
(19, 8, 'Jackets',         3, 2, 1),
(20, 9, 'Dresses',         3, 1, 1);

-- ============================================================
-- Products (20 items)
-- ============================================================
INSERT INTO `product` (`id`, `category_id`, `name`, `subtitle`, `main_image`, `sub_images`, `detail`, `price`, `stock`, `sales`, `status`) VALUES
(1,  14, 'Xiaomi 14 Pro',            'Leica Optics, Snapdragon 8 Gen 3',            '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Xiaomi flagship smartphone with Leica camera system.</p>', 4999.00, 500, 1200, 1),
(2,  14, 'Samsung Galaxy S24 Ultra', 'Galaxy AI, Titanium Frame',                   '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Samsung flagship with AI features and S Pen.</p>', 8999.00, 300, 800, 1),
(3,  15, 'iPhone 15 Pro Max',        'A17 Pro Chip, Titanium Design',               '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Apple flagship iPhone with USB-C.</p>', 9999.00, 200, 2500, 1),
(4,  14, 'OnePlus 12',               'Hasselblad Camera, 100W Charging',             '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>OnePlus flagship with fast charging.</p>', 4299.00, 400, 600, 1),
(5,  16, 'ROG Zephyrus G16',         'RTX 4070, 240Hz Display',                     '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>ASUS gaming laptop with top specs.</p>', 12999.00, 100, 300, 1),
(6,  17, 'MacBook Air M3',           '15-inch, 16GB RAM, 512GB SSD',               '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Apple ultra-thin laptop with M3 chip.</p>', 8999.00, 150, 900, 1),
(7,  17, 'ThinkPad X1 Carbon Gen 12','Intel Ultra 7, 14-inch 2.8K OLED',            '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Lenovo business ultrabook.</p>', 10999.00, 80, 450, 1),
(8,  6,  'iPad Pro M4',              '13-inch, OLED Display, M4 Chip',              '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Apple''s most powerful tablet.</p>', 7999.00, 180, 700, 1),
(9,  7,  'AirPods Pro 2',            'Active Noise Cancellation, H2 Chip',          '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Apple wireless earbuds.</p>', 1799.00, 600, 3000, 1),
(10, 7,  'Anker 100W Charger',       'GaN Technology, Dual USB-C',                  '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Fast GaN charger for all devices.</p>', 199.00, 1000, 5000, 1),
(11, 18, 'Cotton T-Shirt Classic',   '100% Organic Cotton, 6 Colors',               '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Premium cotton t-shirt.</p>', 129.00, 800, 2000, 1),
(12, 18, 'Graphic Print T-Shirt',    'Limited Edition Street Style',                '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Unique graphic design t-shirt.</p>', 159.00, 500, 1200, 1),
(13, 19, 'Winter Down Jacket',       '-30C Rated, 800-Fill Goose Down',             '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Heavy-duty winter jacket.</p>', 899.00, 200, 600, 1),
(14, 20, 'Summer Floral Dress',      'Lightweight, Chiffon Material',               '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Elegant summer dress.</p>', 299.00, 350, 800, 1),
(15, 11, 'Mixed Nuts Gift Box',      '1.5kg, 8 Premium Varieties',                  '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Premium mixed nuts selection.</p>', 168.00, 600, 1500, 1),
(16, 11, 'Potato Chips Party Pack',  '12 Flavors Assortment',                       '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Assorted potato chips party pack.</p>', 59.90, 1000, 4000, 1),
(17, 12, 'Colombian Coffee Beans',   'Single Origin, 500g, Medium Roast',           '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Premium Colombian coffee beans.</p>', 128.00, 400, 2000, 1),
(18, 12, 'Green Tea Gift Set',       'Longjing + Biluochun + Maojian, 250g each',  '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Premium Chinese green tea set.</p>', 358.00, 250, 900, 1),
(19, 13, 'Organic Avocado 4-Pack',   'Hass Avocado, Ready-to-Eat',                  '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Fresh organic avocados.</p>', 39.90, 500, 3000, 1),
(20, 13, 'Premium Beef Steak',       'Wagyu A5, 250g per cut',                      '/static/images/placeholder.svg', '["/static/images/placeholder.svg"]', '<p>Premium Japanese Wagyu steak.</p>', 599.00, 100, 400, 1);

-- ============================================================
-- Seckill products (2 upcoming events)
-- ============================================================
INSERT INTO `seckill_product` (`product_id`, `seckill_price`, `stock_count`, `start_time`, `end_time`, `status`) VALUES
(1,  2999.00, 100, DATE_ADD(NOW(), INTERVAL 1 HOUR),  DATE_ADD(NOW(), INTERVAL 3 HOUR),  0),
(11, 69.00,   200, DATE_ADD(NOW(), INTERVAL 2 HOUR),  DATE_ADD(NOW(), INTERVAL 5 HOUR),  0);

-- ============================================================
-- Sample shipping address for testuser
-- ============================================================
INSERT INTO `shipping_address` (`user_id`, `name`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(2, 'Zhang Wei', '13800000002', 'Beijing', 'Beijing', 'Haidian', 'Zhongguancun Street No.1', 1);
