package org.netcorepal.cap4j.ddd.application.event;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.event.persistence.EventHttpSubscriber;
import org.netcorepal.cap4j.ddd.application.event.persistence.EventHttpSubscriberJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Jpa集成事件订阅注册器实现
 *
 * @author binking338
 * @date 2025/5/23
 */
@RequiredArgsConstructor
public class JpaHttpIntegrationEventSubscriberRegister implements HttpIntegrationEventSubscriberRegister {
    private final EventHttpSubscriberJpaRepository eventHttpSubscriberJpaRepository;

    @Override
    public boolean subscribe(String event, String subscriber, String callbackUrl) {
        Optional<EventHttpSubscriber> existEventHttpSubscriber = eventHttpSubscriberJpaRepository.findOne((root, cq, cb) -> {
            cq.where(cb.and(
                    cb.equal(root.get(EventHttpSubscriber.F_EVENT), event),
                    cb.equal(root.get(EventHttpSubscriber.F_SUBSCRIBER), subscriber)
            ));
            return null;
        });
        if (existEventHttpSubscriber.isPresent()) {
            return false;
        }
        EventHttpSubscriber eventHttpSubscriber = new EventHttpSubscriber(
                null,
                event,
                subscriber,
                callbackUrl,
                0
        );
        eventHttpSubscriberJpaRepository.saveAndFlush(eventHttpSubscriber);
        return true;
    }

    @Override
    public boolean unsubscribe(String event, String subscriber) {
        Optional<EventHttpSubscriber> eventHttpSubscriber = eventHttpSubscriberJpaRepository.findOne((root, cq, cb) -> {
            cq.where(cb.and(
                    cb.equal(root.get(EventHttpSubscriber.F_EVENT), event),
                    cb.equal(root.get(EventHttpSubscriber.F_SUBSCRIBER), subscriber)
            ));
            return null;
        });
        if (!eventHttpSubscriber.isPresent()) {
            return false;
        }
        eventHttpSubscriberJpaRepository.delete(eventHttpSubscriber.get());
        eventHttpSubscriberJpaRepository.flush();
        return true;
    }

    @Override
    public List<String> getCallbackUrls(String event) {
        List<EventHttpSubscriber> list = eventHttpSubscriberJpaRepository.findAll((root, cq, cb) -> {
            cq.where(cb.equal(root.get(EventHttpSubscriber.F_EVENT), event));
            return null;
        });
        return list.stream().map(EventHttpSubscriber::getCallbackUrl).collect(Collectors.toList());
    }
}
