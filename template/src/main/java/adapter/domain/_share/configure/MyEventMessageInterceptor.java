package ${basePackage}.adapter.domain._share.configure;

import org.netcorepal.cap4j.ddd.domain.event.EventMessageInterceptor;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * 领域事件消息拦截器
 *
 * @author cap4j-ddd-codegen
 */
@Service
public class MyEventMessageInterceptor implements EventMessageInterceptor {

    @Override
    public void initPublish(Message message) {

    }

    @Override
    public void prePublish(Message message) {

    }

    @Override
    public void postPublish(Message message) {

    }

    @Override
    public void preSubscribe(Message message) {

    }

    @Override
    public void postSubscribe(Message message) {

    }
}
