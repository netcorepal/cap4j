package org.ddd.example.adapter._share.configure;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Eureka注册配置
 *
 * @author <template/>
 * @date
 */
@Configuration
@ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "true")
public class EurekaConfig implements ApplicationListener<WebServerInitializedEvent> {
    /**
     1. 引入依赖
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
     </dependency>

     2. 配置
     eureka.client.enabled = true
     eureka.client.service-url.defaultZone = http://账号:密码@地址:端口/eureka,http://账号:密码@地址:端口/eureka
     eureka.client.registry-fetch-interval-seconds = 2
     eureka.instance.appname = ${spring.application.name}
     eureka.instance.initial-status = down
     eureka.instance.prefer-ip-address = true
     eureka.instance.lease-renewal-interval-in-seconds = 10
     eureka.instance.lease-expiration-duration-in-seconds = 30

     */
    private ApplicationInfoManager applicationInfoManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public EurekaConfig(ApplicationInfoManager applicationInfoManager){
        this.applicationInfoManager = applicationInfoManager;
    }


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<ServiceRegistryDownFilter> serviceRegistryDownFilterRegistration(
            EurekaAutoServiceRegistration eurekaAutoServiceRegistration
    ) {
        FilterRegistrationBean<ServiceRegistryDownFilter> registration = new FilterRegistrationBean<>();
        ServiceRegistryDownFilter serviceRegistryDownFilter = new ServiceRegistryDownFilter(eurekaAutoServiceRegistration, applicationInfoManager);
        registration.setFilter(serviceRegistryDownFilter);
        registration.addUrlPatterns("/actuator/service-registry");
        registration.setOrder(1);
        return registration;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        // todo 寻找合适的时机进行服务上线
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }


    /**
     * 如果本服务需要按条件注册到Eureka
     * 请配置 eureka.instance.initial-status=down
     * @author <template/>
     */
    @Slf4j
    public static class ServiceRegistryDownFilter extends OncePerRequestFilter {

        private static final String STATUS_DOWN = "DOWN";
        private static final String STATUS_UP = "UP";
        private static final String STATUS_PARAM = "status";
        private ThreadPoolExecutor EXECUTOR;
        private EurekaAutoServiceRegistration eurekaAutoServiceRegistration;
        private ApplicationInfoManager applicationInfoManager;

        public ServiceRegistryDownFilter(
                EurekaAutoServiceRegistration eurekaAutoServiceRegistration,
                ApplicationInfoManager applicationInfoManager) {
            this.EXECUTOR = newThreadPool(2, 2, "ServiceRegistryFilterPool");
            this.eurekaAutoServiceRegistration = eurekaAutoServiceRegistration;
            this.applicationInfoManager = applicationInfoManager;
        }

        private ThreadPoolExecutor newThreadPool(int corePoolSize,
                                                 int maximumPoolSize,
                                                 String threadPoolName) {
            ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat(threadPoolName + "-%d").build();
            return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        }


        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String status = request.getParameter(STATUS_PARAM);
            if (STATUS_DOWN.equalsIgnoreCase(status)) {
                EXECUTOR.execute(() -> {
                    applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
                });
                log.info("触发service registry down事件");
            } else if (STATUS_UP.equalsIgnoreCase(status)) {
                EXECUTOR.execute(() -> {
                    eurekaAutoServiceRegistration.start();
                    applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
                });
                log.info("触发service registry up事件");
            }

            filterChain.doFilter(request, response);
        }
    }
}
