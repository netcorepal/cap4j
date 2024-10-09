package org.netcorepal.cap4j.ddd.domain.event;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;
import java.util.UUID;

/**
 * 领域事件消息拦截器
 *
 * @author binking338
 * @date 2024/8/8
 */
public interface EventMessageInterceptor {

    /**
     * 提交发布
     *
     * @param message
     * @return
     */
    void initPublish(Message message);

    /**
     * 发布前
     *
     * @param message
     */
    void prePublish(Message message);

    /**
     * 发布后
     *
     * @param message
     */
    void postPublish(Message message);

    /**
     * 订阅处理前
     *
     * @param message
     * @return
     */
    void preSubscribe(Message message);

    /**
     * 订阅处理后
     *
     * @param message
     * @return
     */
    void postSubscribe(Message message);

    /**
     * 可变更消息头
     */
    public static class ModifiableMessageHeaders extends MessageHeaders {

        public ModifiableMessageHeaders(@Nullable Map<String, Object> headers) {
            this(
                    headers,
                    headers.containsKey(ID) ? UUID.fromString(headers.get(ID).toString()) : null,
                    headers.containsKey(TIMESTAMP) ? Long.parseLong(headers.get(TIMESTAMP).toString()) : null
            );
        }

        public ModifiableMessageHeaders(@Nullable Map<String, Object> headers, @Nullable UUID id, @Nullable Long timestamp) {
            super(headers, id, timestamp);
        }

        @Override
        public void putAll(Map<? extends String, ?> map) {
            this.getRawHeaders().putAll(map);
        }

        @Override
        public Object put(String key, Object value) {
            return this.getRawHeaders().put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return this.getRawHeaders().remove(key);
        }

        @Override
        public void clear() {
            this.getRawHeaders().clear();
        }
    }
}
