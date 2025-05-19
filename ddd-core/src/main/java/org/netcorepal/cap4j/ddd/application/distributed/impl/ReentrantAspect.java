package org.netcorepal.cap4j.ddd.application.distributed.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.netcorepal.cap4j.ddd.application.distributed.Locker;
import org.netcorepal.cap4j.ddd.application.distributed.annotation.Reentrant;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可重入锁切面
 *
 * @author binking338
 * @date 2025/5/14
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class ReentrantAspect {
    private final Locker distributedLocker;
    private final Locker localLocker = new MemoryLocker();

    static final Duration DEFAULT_EXPIRE = Duration.ofHours(6);

    @Around("@annotation(reentrant)")
    public Object around(ProceedingJoinPoint joinPoint, Reentrant reentrant) {
        if (reentrant.value()) {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Locker locker = null;
        if (reentrant.distributed()) {
            locker = distributedLocker;
        } else {
            locker = localLocker;
        }
        // 生成唯一锁键
        String lockKey = generateLockKey(method, reentrant.key());
        String lockPwd = UUID.randomUUID().toString();

        Duration expire = parseDuration(reentrant.expire());
        if (locker.acquire(lockKey, lockPwd, expire)) {
            log.debug("获取锁成功:{}", lockKey);
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                locker.release(lockKey, lockPwd);
                log.debug("释放锁成功:{}", lockKey);
            }
        } else {
            log.debug("获取锁失败:{}", lockKey);
        }

        return null;
    }

    private String generateLockKey(Method method, String key) {
        return key == null || key.isEmpty()
                ? String.format("%s:%s", method.getDeclaringClass().getName(), method.getName())
                : key;
    }

    private Duration parseDuration(String expireStr) {
        Duration expire = DEFAULT_EXPIRE;
        if (expireStr == null || expireStr.isEmpty()) {
            return expire;
        }

        expireStr = expireStr.toLowerCase();
        if (expireStr.matches("\\d+")) {
            expire = Duration.ofSeconds(Long.parseLong(expireStr));
        } else if (expireStr.matches("\\d+([smhd]|ms)")) {
            String numericPart = expireStr.replaceAll("[^\\d]", "");
            String unit = expireStr.replaceAll("\\d", "");
            switch (unit) {
                case "ms":
                    expire = Duration.ofMillis(Long.parseLong(numericPart));
                    break;
                case "s":
                    expire = Duration.ofSeconds(Long.parseLong(numericPart));
                    break;
                case "m":
                    expire = Duration.ofMinutes(Long.parseLong(numericPart));
                    break;
                case "h":
                    expire = Duration.ofHours(Long.parseLong(numericPart));
                    break;
                case "d":
                    expire = Duration.ofDays(Long.parseLong(numericPart));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid expire string: " + expireStr);
            }
        } else {
            expire = Duration.parse(expireStr);
        }
        return expire;
    }

    private static class MemoryLocker implements Locker {
        private final Object[] DEFAULT_CONTROL = new Object[]{"", 0L};
        private final ConcurrentHashMap<String, Object[]> expireMap = new ConcurrentHashMap<>();

        @Override
        public boolean acquire(String key, String pwd, Duration expireDuration) {
            Long now = System.currentTimeMillis();
            Object[] control = expireMap.getOrDefault(key, DEFAULT_CONTROL);
            Long timestamp = (Long) control[1];
            synchronized (this) {
                if (timestamp > now) {
                    if (Objects.equals(pwd, control[0])) {
                        return true;
                    } else {
                        return false;
                    }
                }
                expireMap.put(key, new Object[]{pwd, now + expireDuration.toMillis()});
                return true;
            }
        }

        @Override
        public boolean release(String key, String pwd) {
            synchronized (this) {
                if (!expireMap.containsKey(key)) {
                    return true;
                }
                Object[] control = expireMap.get(key);
                if (Objects.equals(pwd, control[0])) {
                    expireMap.remove(key);
                    return true;
                }
                return false;
            }
        }
    }
}
