package ${basePackage}.adapter.domain._share.configure;

import org.netcorepal.cap4j.ddd.domain.event.DomainEventInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 领域事件拦截器
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Service
public class MyDomainEventInterceptor implements DomainEventInterceptor {
    @Override
    public void onAttach(Object eventPayload, Object entity, LocalDateTime schedule) {

    }

    @Override
    public void onDetach(Object eventPayload, Object entity) {

    }

    @Override
    public void prePersist(EventRecord event) {

    }

    @Override
    public void postPersist(EventRecord event) {

    }

    @Override
    public void preRelease(EventRecord event) {

    }

    @Override
    public void postRelease(EventRecord event) {

    }

    @Override
    public void onException(Throwable throwable, EventRecord event) {

    }
}
