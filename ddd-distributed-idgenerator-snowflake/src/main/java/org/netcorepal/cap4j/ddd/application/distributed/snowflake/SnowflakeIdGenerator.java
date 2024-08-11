package org.netcorepal.cap4j.ddd.application.distributed.snowflake;

/**
 * SnowflakeId生成算法
 *
 * @author binking338
 * @date 2024/8/10
 */
public class SnowflakeIdGenerator {
    // 开始时间戳，用于计算相对时间
    static final long epoch = 1706716800000L; // 2024-01-01 00:00:00 UTC+8

    // 机器ID占用的位数
    public static final long workerIdBits = 5L;

    // 数据中心ID占用的位数
    public static final long datacenterIdBits = 5L;

    // 序列号占用的位数
    public static final long sequenceBits = 12L;

    // 最大机器ID（2的workerIdBits次方-1）
    public static final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    // 最大数据中心ID（2的datacenterIdBits次方-1）
    public static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 序列号的最大值（2的sequenceBits次方-1）
    public static final long maxSequence = -1L ^ (-1L << sequenceBits);

    // 机器ID向左移的位数
    private final long workerIdShift = sequenceBits;

    // 数据中心ID向左移的位数
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    // 时间戳向左移的位数
    private final long timestampShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 上一次生成ID的时间戳
    private long lastTimestamp = -1L;

    // 序列号
    private long sequence = 0L;

    // 机器ID
    private final long workerId;

    // 数据中心ID
    private final long datacenterId;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId must be between 0 and %d", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenterId must be between 0 and %d", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 如果当前时间小于上一次生成ID的时间戳，说明系统时钟回退过，应抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 如果是同一毫秒内生成的，则进行序列号自增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            // 序列号已经达到最大值，需要等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            // 不同毫秒内生成的序列号归零
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        // 生成全局唯一ID
        return ((timestamp - epoch) << timestampShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= currentTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}