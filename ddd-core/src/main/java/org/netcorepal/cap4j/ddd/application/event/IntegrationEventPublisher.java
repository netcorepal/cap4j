package org.netcorepal.cap4j.ddd.application.event;

import org.netcorepal.cap4j.ddd.domain.event.EventRecord;

/**
 * 集成事件发布器
 *
 * @author binking338
 * @date 2024/8/29
 */
public interface IntegrationEventPublisher {


    /**
     * 发布事件
     *
     * @param event
     * @param publishCallback
     */
    void publish(EventRecord event, PublishCallback publishCallback);

    public interface PublishCallback {
        /**
         * 发布成功
         *
         * @param event
         */
        void onSuccess(EventRecord event);

        /**
         * @param event
         * @param throwable
         */
        void onException(EventRecord event, Throwable throwable);
    }

}
