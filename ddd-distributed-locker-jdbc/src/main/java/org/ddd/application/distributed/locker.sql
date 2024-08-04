CREATE TABLE `__locker` (
                            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                            `name` varchar(100) NOT NULL DEFAULT '',
                            `pwd` varchar(100) NOT NULL DEFAULT '',
                            `lock_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00',
                            `unlock_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00',
                            `version` bigint(20) unsigned NOT NULL DEFAULT '0',
                            `db_created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `db_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uniq_name` (`name`),
                            KEY `idx_db_created_at` (`db_created_at`),
                            KEY `idx_db_updated_at` (`db_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锁\n@I;';