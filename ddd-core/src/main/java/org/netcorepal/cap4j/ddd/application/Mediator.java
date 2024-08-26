package org.netcorepal.cap4j.ddd.application;

import org.netcorepal.cap4j.ddd.domain.event.EventSupervisor;
import org.netcorepal.cap4j.ddd.domain.repo.RepositorySupervisor;
import org.netcorepal.cap4j.ddd.domain.repo.UnitOfWork;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 中介者
 *
 * @author binking338
 * @date 2024/8/24
 */
public interface Mediator extends RequestSupervisor, EventSupervisor {
    static Mediator getInstance()
    {
        return MediatorConfiguration.instance;
    }

}
