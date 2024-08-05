package org.netcorepal.cap4j.ddd.share;

/**
 * 领域异常
 *
 * @author binking338
 * @date 2023/8/15
 */
public class DomainException extends RuntimeException{
    public DomainException(String message) {
        super(message);
    }
    public DomainException(String message, Throwable innerException) {
        super(message, innerException);
    }
}
