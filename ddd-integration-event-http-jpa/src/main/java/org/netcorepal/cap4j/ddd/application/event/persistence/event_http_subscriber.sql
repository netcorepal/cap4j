-- Create syntax for TABLE '__event_http_subscriber'
CREATE TABLE `__event_http_subscriber`
(
    `id`            bigint        NOT NULL AUTO_INCREMENT,
    `event`         varchar(255)  NOT NULL DEFAULT '' COMMENT '事件',
    `subscriber`    varchar(255)  NOT NULL DEFAULT '' COMMENT '订阅者',
    `callback_url`  varchar(1024) NOT NULL DEFAULT '' COMMENT '事件回调地址',
    `version`       int           NOT NULL DEFAULT '0',
    `db_created_at` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_updated_at` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `db_deleted`    tinyint(1)    NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_db_created_at` (`db_created_at`),
    KEY `idx_db_updated_at` (`db_updated_at`),
    KEY `idx_event` (`event`)
) COMMENT ='事件HTTP订阅 support by cap4j\n@I;'
;