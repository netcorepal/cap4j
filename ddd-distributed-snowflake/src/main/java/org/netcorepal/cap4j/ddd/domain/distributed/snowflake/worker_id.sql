CREATE TABLE `__worker_id` (
                               `id` int unsigned NOT NULL AUTO_INCREMENT,
                               `datacenter_id` int unsigned NOT NULL DEFAULT '0' COMMENT '数据分区',
                               `worker_id` int unsigned NOT NULL DEFAULT '0' COMMENT '机器分区',
                               `dispatch_to` varchar(100) NOT NULL DEFAULT '' COMMENT '分配对象名称',
                               `dispatch_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '分配时间',
                               `expire_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '过期时间',
                               `version` bigint unsigned NOT NULL DEFAULT '0',
                               `db_created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `db_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_db_created_at` (`db_created_at`),
                               KEY `idx_db_updated_at` (`db_updated_at`)
) COMMENT='雪花机器码 support by cap4j\n@I;';