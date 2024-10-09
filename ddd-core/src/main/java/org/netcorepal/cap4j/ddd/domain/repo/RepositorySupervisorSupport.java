package org.netcorepal.cap4j.ddd.domain.repo;

/**
 * 仓储管理器配置
 *
 * @author binking338
 * @date 2024/8/25
 */
public class RepositorySupervisorSupport {
    static RepositorySupervisor instance = null;

    public static void configure(RepositorySupervisor repositorySupervisor) {
        instance = repositorySupervisor;
    }
}
