package org.netcorepal.cap4j.ddd.domain.event.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 领域事件定时任务配置
 *
 * @author binking338
 * @date 2024/8/11
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.domain.event.schedule")
public class EventScheduleProperties {
    /**
     * 补偿发送-批量查询事件数量
     */
    int compenseBatchSize = 10;
    /**
     * 补偿发送-最大并行线程（进程）数
     */
    int compenseMaxConcurrency = 10;
    /**
     * 补偿发送-间隔（秒）
     */
    int compenseIntervalSeconds = 60;
    /**
     * 补偿发送-分布式锁时长（秒）
     */
    int compenseMaxLockSeconds = 30;
    /**
     * 补偿发送-CRON
     */
    String compenseCron = "0 */1 * * * ?";

    /**
     * 记录归档-批量查询事件数量
     */
    int archiveBatchSize = 100;
    /**
     * 记录归档-保留时长
     */
    int archiveExpireDays = 7;
    /**
     * 记录归档-分布式锁时长（秒）
     */
    int archiveMaxLockSeconds = 172800;
    /**
     * 记录归档-CRON
     */
    String archiveCron = "0 0 2 * * ?";

    /**
     * 分区表-启用添加分区
     */
    boolean addPartitionEnable = true;
    /**
     * 分区表-添加分区CRON
     */
    String addPartitionCron = "0 0 0 * * ?";
}
