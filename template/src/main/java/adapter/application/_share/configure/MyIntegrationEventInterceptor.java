package ${basePackage}.adapter.application._share.configure;

import org.netcorepal.cap4j.ddd.application.event.IntegrationEventInterceptor;
import org.netcorepal.cap4j.ddd.domain.event.EventRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 集成事件拦截器
 *
 * @author cap4j-ddd-codegen
 * @date 2024/09/14
 */
@Service
public class MyIntegrationEventInterceptor implements IntegrationEventInterceptor {

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

    @Override
    public void onAttach(Object eventPayload, LocalDateTime schedule) {

    }

    @Override
    public void onDetach(Object eventPayload) {

    }
}
