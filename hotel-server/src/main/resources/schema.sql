-- 酒店预订系统建表脚本（幂等：CREATE TABLE IF NOT EXISTS）
-- 字符集 utf8mb4，InnoDB

CREATE TABLE IF NOT EXISTS `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `login_name`    VARCHAR(32)  NOT NULL COMMENT '登录名（住客=手机号，admin=admin）',
  `password_hash` VARCHAR(72)  NOT NULL COMMENT 'BCrypt 密文',
  `role`          VARCHAR(16)  NOT NULL COMMENT 'GUEST / ADMIN',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_login_name` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住客与前台账号';

CREATE TABLE IF NOT EXISTS `auth_token` (
  `token`      VARCHAR(36) NOT NULL COMMENT 'UUID token',
  `user_id`    BIGINT      NOT NULL,
  `expires_at` DATETIME    NOT NULL,
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`token`),
  KEY `idx_auth_token_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录 token';

CREATE TABLE IF NOT EXISTS `room_type` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64)   NOT NULL,
  `price`       DECIMAL(10,2) NOT NULL COMMENT '单价（元/晚）',
  `description` VARCHAR(512)  NOT NULL DEFAULT '',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房型';

CREATE TABLE IF NOT EXISTS `room` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `room_no`      VARCHAR(16) NOT NULL,
  `room_type_id` BIGINT      NOT NULL,
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_room_no` (`room_no`),
  KEY `idx_room_type` (`room_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物理房间（无状态字段，状态由在住订单推导）';

CREATE TABLE IF NOT EXISTS `hotel_order` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`      VARCHAR(32)   NOT NULL COMMENT 'HR+yyyyMMdd-NNNN',
  `request_no`    VARCHAR(64)   NOT NULL COMMENT '客户端幂等请求号，重复提交返回首次订单',
  `user_id`       BIGINT        NOT NULL COMMENT '下单账号',
  `guest_name`    VARCHAR(32)   NOT NULL COMMENT '住客姓名（支持代订）',
  `guest_phone`   VARCHAR(16)   NOT NULL COMMENT '住客手机号',
  `room_type_id`  BIGINT        NOT NULL,
  `room_id`       BIGINT        NULL COMMENT '入住前为 NULL',
  `id_card`       VARCHAR(18)   NULL COMMENT '入住前为 NULL',
  `checkin_date`  DATE          NOT NULL,
  `checkout_date` DATE          NOT NULL,
  `nights`        INT           NOT NULL,
  `amount`        DECIMAL(10,2) NOT NULL COMMENT '下单时固化的总金额（元）',
  `status`        VARCHAR(16)   NOT NULL COMMENT 'CONFIRMED / CHECKED_IN / COMPLETED / CANCELLED',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active_room_id` BIGINT GENERATED ALWAYS AS (IF(`status`='CHECKED_IN', `room_id`, NULL)) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_order_no` (`order_no`),
  UNIQUE KEY `uk_order_request_no` (`request_no`),
  UNIQUE KEY `uk_order_active_room` (`active_room_id`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_guest_phone` (`guest_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

CREATE TABLE IF NOT EXISTS `daily_inventory` (
  `id`             BIGINT  NOT NULL AUTO_INCREMENT,
  `room_type_id`   BIGINT  NOT NULL,
  `stay_date`      DATE    NOT NULL,
  `total_count`    INT     NOT NULL COMMENT '当日该房型房间数快照',
  `occupied_count` INT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_type_date` (`room_type_id`, `stay_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐日库存';

CREATE TABLE IF NOT EXISTS `order_seq` (
  `seq_date`      DATE   NOT NULL,
  `current_value` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`seq_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单号每日序号';
