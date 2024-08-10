CREATE TABLE `__event` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `event_uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '事件uuid',
                           `svc_name` varchar(255) NOT NULL DEFAULT '' COMMENT '服务',
                           `event_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件类型',
                           `data` text COMMENT '事件数据',
                           `data_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件数据类型',
                           `expire_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
                           `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `event_state` int(11) NOT NULL DEFAULT '0' COMMENT '分发状态 @E=0:INIT:初始化|1:DELIVERED:已发送|-1:DELIVERING:发送中|-2:CANCEL:|-3:EXPIRED:|-4:EXHAUSTED:超过重试次数|-9:EXCEPTION:异常; @T=EventState;',
                           `last_try_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次尝试时间',
                           `next_try_time` datetime NOT NULL DEFAULT '0001-01-01 00:00:00' COMMENT '下次尝试时间',
                           `tried_times` int(11) NOT NULL DEFAULT '0' COMMENT '已尝试次数',
                           `try_times` int(11) NOT NULL DEFAULT '0' COMMENT '尝试次数',
                           `version` int(11) NOT NULL DEFAULT '0',
                           `db_created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `db_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (
                                        `id`
                                        -- , `db_created_at`
                                       ),
                           KEY `idx_event_uuid` (`event_uuid`),
                           KEY `idx_next_try_time` (`next_try_time`,`event_type`,`svc_name`),
                           KEY `idx_expire_at` (`expire_at`),
                           KEY `idx_create_at` (`create_at`),
                           KEY `idx_db_created_at` (`db_created_at`),
                           KEY `idx_db_updated_at` (`db_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='集成事件\n@I;'
-- partition by range(to_days(db_created_at))
-- (partition p202201 values less than (to_days('2022-02-01')) ENGINE=InnoDB)
;

CREATE TABLE `__archived_event` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `event_uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '事件uuid',
                           `svc_name` varchar(255) NOT NULL DEFAULT '' COMMENT '服务',
                           `event_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件类型',
                           `data` text COMMENT '事件数据',
                           `data_type` varchar(255) NOT NULL DEFAULT '' COMMENT '事件数据类型',
                           `expire_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
                           `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `event_state` int(11) NOT NULL DEFAULT '0' COMMENT '分发状态 @E=0:INIT:初始化|1:DELIVERED:已发送|-1:DELIVERING:发送中|-2:CANCEL:|-3:EXPIRED:|-4:EXHAUSTED:超过重试次数|-9:EXCEPTION:异常; @T=EventState;',
                           `last_try_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次尝试时间',
                           `next_try_time` datetime NOT NULL DEFAULT '0001-01-01 00:00:00' COMMENT '下次尝试时间',
                           `tried_times` int(11) NOT NULL DEFAULT '0' COMMENT '已尝试次数',
                           `try_times` int(11) NOT NULL DEFAULT '0' COMMENT '尝试次数',
                           `version` int(11) NOT NULL DEFAULT '0',
                           `db_created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `db_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (
                                        `id`
                               -- , `db_created_at`
                               ),
                           KEY `idx_event_uuid` (`event_uuid`),
                           KEY `idx_next_try_time` (`next_try_time`,`event_type`,`svc_name`),
                           KEY `idx_expire_at` (`expire_at`),
                           KEY `idx_create_at` (`create_at`),
                           KEY `idx_db_created_at` (`db_created_at`),
                           KEY `idx_db_updated_at` (`db_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='集成事件\n@I;'
-- partition by range(to_days(db_created_at))
-- (partition p202201 values less than (to_days('2022-02-01')) ENGINE=InnoDB)
;