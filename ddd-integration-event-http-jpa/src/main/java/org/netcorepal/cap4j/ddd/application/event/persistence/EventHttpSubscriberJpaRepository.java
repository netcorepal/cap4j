package org.netcorepal.cap4j.ddd.application.event.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 集成事件HTTP订阅仓储
 *
 * @author binking338
 * @date 2025/5/23
 */
public interface EventHttpSubscriberJpaRepository extends JpaRepository<EventHttpSubscriber, Long>, JpaSpecificationExecutor<EventHttpSubscriber> {

}
