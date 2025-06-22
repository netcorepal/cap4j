package org.netcorepal.cap4j.ddd.console;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.RequestManager;
import org.netcorepal.cap4j.ddd.application.saga.SagaManager;
import org.netcorepal.cap4j.ddd.console.event.EventConsoleService;
import org.netcorepal.cap4j.ddd.console.locker.LockerConsoleService;
import org.netcorepal.cap4j.ddd.console.request.RequestConsoleService;
import org.netcorepal.cap4j.ddd.console.saga.SagaConsoleService;
import org.netcorepal.cap4j.ddd.console.snowflake.SnowflakeConsoleService;
import org.netcorepal.cap4j.ddd.domain.event.EventPublisher;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.HttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 控制台配置
 *
 * @author binking338
 * @date 2025/6/10
 */
@Configuration
@Slf4j
public class DDDConsoleAutoConfiguration {
    public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    @ConditionalOnMissingBean(EventConsoleService.class)
    public EventConsoleService eventConsoleService(JdbcTemplate jdbcTemplate, EventPublisher eventPublisher) {
        return new EventConsoleService(jdbcTemplate, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(RequestConsoleService.class)
    public RequestConsoleService requestConsoleService(JdbcTemplate jdbcTemplate, RequestManager requestManager) {
        return new RequestConsoleService(jdbcTemplate, requestManager);
    }

    @Bean
    @ConditionalOnMissingBean(SagaConsoleService.class)
    public SagaConsoleService sagaConsoleService(JdbcTemplate jdbcTemplate, SagaManager sagaManager) {
        return new SagaConsoleService(jdbcTemplate, sagaManager);
    }

    @Bean
    @ConditionalOnMissingBean(LockerConsoleService.class)
    public LockerConsoleService lockerConsoleService(JdbcTemplate jdbcTemplate) {
        return new LockerConsoleService(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SnowflakeConsoleService.class)
    public SnowflakeConsoleService snowflakeConsoleService(JdbcTemplate jdbcTemplate) {
        return new SnowflakeConsoleService(jdbcTemplate);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }

    @Bean(name = "/cap4j/console/event/search")
    public HttpRequestHandler eventSearch(
            EventConsoleService eventConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/event/search?uuid={uuid}&type={type}&state={state}&scheduleAt={scheduleAtBegin}&scheduleAt={scheduleAtEnd}&pageSize={pageSize}&pageNum={pageNum}");
        return (req, res) -> {
            EventConsoleService.SearchParam param = new EventConsoleService.SearchParam();
            param.setUuid(req.getParameter("uuid"));
            param.setType(req.getParameter("type"));
            String[] stateParam = req.getParameterValues("state");
            if (stateParam != null) {
                int[] state = new int[stateParam.length];
                for (int i = 0; i < state.length; i++) {
                    state[i] = Integer.parseInt(stateParam[i]);
                }
                param.setState(state);
            }
            String[] scheduleParams = req.getParameterValues("scheduleAt");
            if (scheduleParams != null) {
                LocalDateTime[] schedule = new LocalDateTime[scheduleParams.length];
                for (int i = 0; i < schedule.length; i++) {
                    schedule[i] = LocalDateTime.parse(scheduleParams[i], DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
                }
                param.setScheduleAt(schedule);
            }
            param.setPageNum(req.getParameter("pageNum") == null ? 1 : Integer.parseInt(req.getParameter("pageNum")));
            param.setPageSize(req.getParameter("pageSize") == null ? 20 : Integer.parseInt(req.getParameter("pageSize")));
            OperationResponse result = null;
            try {
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(eventConsoleService.search(param))
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/event/retry")
    public HttpRequestHandler eventRetry(
            EventConsoleService eventConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/event/retry?uuid={uuid}");
        return (req, res) -> {
            String uuid = req.getParameter("uuid");
            OperationResponse result = null;
            try {
                eventConsoleService.retry(uuid);
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(true)
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/request/search")
    public HttpRequestHandler requestSearch(
            RequestConsoleService requestConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/request/search?uuid={uuid}&type={type}&state={state}&scheduleAt={scheduleAtBegin}&scheduleAt={scheduleAtEnd}&pageSize={pageSize}&pageNum={pageNum}");
        return (req, res) -> {
            RequestConsoleService.SearchParam param = new RequestConsoleService.SearchParam();
            param.setUuid(req.getParameter("uuid"));
            param.setType(req.getParameter("type"));
            String[] stateParam = req.getParameterValues("state");
            if (stateParam != null) {
                int[] state = new int[stateParam.length];
                for (int i = 0; i < state.length; i++) {
                    state[i] = Integer.parseInt(stateParam[i]);
                }
                param.setState(state);
            }
            String[] scheduleParams = req.getParameterValues("scheduleAt");
            if (scheduleParams != null) {
                LocalDateTime[] schedule = new LocalDateTime[scheduleParams.length];
                for (int i = 0; i < schedule.length; i++) {
                    schedule[i] = LocalDateTime.parse(scheduleParams[i], DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
                }
                param.setScheduleAt(schedule);
            }
            param.setPageNum(req.getParameter("pageNum") == null ? 1 : Integer.parseInt(req.getParameter("pageNum")));
            param.setPageSize(req.getParameter("pageSize") == null ? 20 : Integer.parseInt(req.getParameter("pageSize")));
            OperationResponse result = null;
            try {
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(requestConsoleService.search(param))
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/request/retry")
    public HttpRequestHandler requestRetry(
            RequestConsoleService requestConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/request/retry?uuid={uuid}");
        return (req, res) -> {
            String uuid = req.getParameter("uuid");
            OperationResponse result = null;
            try {
                requestConsoleService.retry(uuid);
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(true)
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/saga/search")
    public HttpRequestHandler sagaSearch(
            SagaConsoleService sagaConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/saga/search?uuid={uuid}&type={type}&state={state}&scheduleAt={scheduleAtBegin}&scheduleAt={scheduleAtEnd}&pageSize={pageSize}&pageNum={pageNum}");
        return (req, res) -> {
            SagaConsoleService.SearchParam param = new SagaConsoleService.SearchParam();
            param.setUuid(req.getParameter("uuid"));
            param.setType(req.getParameter("type"));
            String[] stateParam = req.getParameterValues("state");
            if (stateParam != null) {
                int[] state = new int[stateParam.length];
                for (int i = 0; i < state.length; i++) {
                    state[i] = Integer.parseInt(stateParam[i]);
                }
                param.setState(state);
            }
            String[] scheduleParams = req.getParameterValues("scheduleAt");
            if (scheduleParams != null) {
                LocalDateTime[] schedule = new LocalDateTime[scheduleParams.length];
                for (int i = 0; i < schedule.length; i++) {
                    schedule[i] = LocalDateTime.parse(scheduleParams[i], DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
                }
                param.setScheduleAt(schedule);
            }
            param.setPageNum(req.getParameter("pageNum") == null ? 1 : Integer.parseInt(req.getParameter("pageNum")));
            param.setPageSize(req.getParameter("pageSize") == null ? 20 : Integer.parseInt(req.getParameter("pageSize")));
            OperationResponse result = null;
            try {
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(sagaConsoleService.search(param))
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/saga/retry")
    public HttpRequestHandler sagaRetry(
            SagaConsoleService sagaConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/saga/retry?uuid={uuid}");
        return (req, res) -> {
            String uuid = req.getParameter("uuid");
            OperationResponse result = null;
            try {
                sagaConsoleService.retry(uuid);
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(true)
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/locker/search")
    public HttpRequestHandler lockerSearch(
            LockerConsoleService lockerConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/locker/search?name={name}&lock={true|false}&pageSize={pageSize}&pageNum={pageNum}");
        return (req, res) -> {
            LockerConsoleService.SearchParam param = new LockerConsoleService.SearchParam();
            param.setName(req.getParameter("name"));
            if (req.getParameter("lock") != null) {
                param.setLock(Boolean.parseBoolean(req.getParameter("lock")));
            }
            param.setPageNum(req.getParameter("pageNum") == null ? 1 : Integer.parseInt(req.getParameter("pageNum")));
            param.setPageSize(req.getParameter("pageSize") == null ? 20 : Integer.parseInt(req.getParameter("pageSize")));
            OperationResponse result = null;
            try {
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(lockerConsoleService.search(param))
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/locker/unlock")
    public HttpRequestHandler lockerUnlock(
            LockerConsoleService lockerConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/locker/unlock?name={name}&pwd={pwd}");
        return (req, res) -> {
            String name = req.getParameter("name");
            String pwd = req.getParameter("pwd");
            OperationResponse result = null;
            try {
                boolean success = lockerConsoleService.unlock(name, pwd);
                result = OperationResponse.builder()
                        .success(success)
                        .message("ok")
                        .data(success)
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }

    @Bean(name = "/cap4j/console/snowflake/search")
    public HttpRequestHandler snowflakeSearch(
            SnowflakeConsoleService snowflakeConsoleService,
            @Value("${server.port:80}")
            String serverPort,
            @Value("${server.servlet.context-path:}")
            String serverServletContentPath
    ) {
        log.info("DDD Console URL: http://localhost:" + serverPort + serverServletContentPath + "/cap4j/console/snowflake/search?free={true|false}&dispatchTo={dispatchTo}&pageSize={pageSize}&pageNum={pageNum}");
        return (req, res) -> {
            SnowflakeConsoleService.SearchParam param = new SnowflakeConsoleService.SearchParam();
            if (req.getParameter("free") != null) {
                param.setFree(Boolean.parseBoolean(req.getParameter("free")));
            }
            param.setDispatchTo(req.getParameter("dispatchTo"));
            param.setPageNum(req.getParameter("pageNum") == null ? 1 : Integer.parseInt(req.getParameter("pageNum")));
            param.setPageSize(req.getParameter("pageSize") == null ? 20 : Integer.parseInt(req.getParameter("pageSize")));
            OperationResponse result = null;
            try {
                result = OperationResponse.builder()
                        .success(true)
                        .message("ok")
                        .data(snowflakeConsoleService.search(param))
                        .build();
            } catch (Throwable throwable){
                result = OperationResponse.builder()
                        .success(false)
                        .message(throwable.getMessage())
                        .build();
            }
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json; charset=utf-8");
            res.getWriter().println(JSON.toJSONString(result));
            res.getWriter().flush();
            res.getWriter().close();
        };
    }
}
