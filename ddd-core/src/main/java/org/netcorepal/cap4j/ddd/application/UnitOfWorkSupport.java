package org.netcorepal.cap4j.ddd.application;

/**
 * 工作单元配置
 *
 * @author binking338
 * @date 2024/8/25
 */
public class UnitOfWorkSupport {
    static UnitOfWork instance = null;

    /**
     * 配置工作单元
     *
     * @param unitOfWork 工作单元
     */
    public static void configure(UnitOfWork unitOfWork) {
        instance = unitOfWork;
    }
}
