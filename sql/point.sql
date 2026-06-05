-- 积分系统
SET NAMES utf8mb4;

-- 用户积分账户
CREATE TABLE IF NOT EXISTS `point_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL UNIQUE COMMENT '用户ID',
  `balance` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前积分余额',
  `total_earned` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计获得积分',
  `year_year` int NOT NULL COMMENT '当前积分所属年份',
  `year_earned` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '本年度已获得积分',
  `year_spent` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '本年度已消费积分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分账户';

-- 积分明细
CREATE TABLE IF NOT EXISTS `point_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '类型: earn_learn/earn_complete/tip_out/tip_in/redeem/year_reset',
  `amount` decimal(10,2) NOT NULL COMMENT '积分变动(正为获得,负为消费)',
  `balance_after` decimal(10,2) NOT NULL COMMENT '变动后余额',
  `course_id` bigint DEFAULT NULL COMMENT '关联课程',
  `lesson_id` bigint DEFAULT NULL COMMENT '关联课时',
  `target_user_id` bigint DEFAULT NULL COMMENT '目标用户(打赏对象)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `year_year` int NOT NULL COMMENT '年份',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_year` (`user_id`, `year_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分明细';

-- 积分兑换商品
CREATE TABLE IF NOT EXISTS `point_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片',
  `points_required` decimal(10,2) NOT NULL COMMENT '所需积分',
  `stock` int NOT NULL DEFAULT -1 COMMENT '库存(-1无限)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商品';

-- 兑换记录
CREATE TABLE IF NOT EXISTS `point_exchange` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '兑换用户',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `points_cost` decimal(10,2) NOT NULL COMMENT '消耗积分',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待发放 1已发放 2已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录';
