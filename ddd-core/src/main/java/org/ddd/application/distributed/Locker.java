package org.ddd.application.distributed;

import java.time.Duration;

/**
 * @author binking338
 * @date 2023/8/17
 */
public interface Locker {
    /**
     * 获取锁
     * @param key
     * @param pwd
     * @param expireDuration
     * @return
     */
    boolean acquire(String key, String pwd, Duration expireDuration);

    /**
     * 释放锁
     * @param key
     * @param pwd
     * @return
     */
    boolean release(String key, String pwd);

}
