package org.ddd.example.adapter._share.configure;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Apollo配置
 */
@Slf4j
@Configuration
@AllArgsConstructor
@ConditionalOnClass(com.ctrip.framework.apollo.Apollo.class)
public class ApolloConfig {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 自动更新环境变量
     *
     * @param changeEvent
     */
    @ApolloConfigChangeListener(ConfigConsts.NAMESPACE_APPLICATION)
    public void onChange(ConfigChangeEvent changeEvent) {
        Set<String> changedKeys = changeEvent.changedKeys();
        log.info("Apollo Refreshing properties changedKeys:{}!", changedKeys);
        eventPublisher.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
        log.info("Apollo Refreshing properties refreshed!");
    }

}