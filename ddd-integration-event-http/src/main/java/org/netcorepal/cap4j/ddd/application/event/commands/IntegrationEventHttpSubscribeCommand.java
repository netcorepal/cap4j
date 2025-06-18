package org.netcorepal.cap4j.ddd.application.event.commands;

import com.alibaba.fastjson.JSON;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.netcorepal.cap4j.ddd.application.event.HttpIntegrationEventSubscriberAdapter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 集成事件HTTP订阅命令
 *
 * @author binking338
 * @date 2025/6/18
 */
public class IntegrationEventHttpSubscribeCommand {
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Command<Request, Response> {
        private final RestTemplate restTemplate;
        private final String eventParamName;
        private final String subscriberParamName;

        @Override
        public Response exec(Request param) {
            Map<String, Object> uriParams = new HashMap<>();
            try {
                uriParams.put(eventParamName, URLEncoder.encode(param.getEvent(), StandardCharsets.UTF_8.name()));
                uriParams.put(subscriberParamName, URLEncoder.encode(param.getSubscriber(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String url = param.getUrl();
            for (Map.Entry<String, Object> entry : uriParams.entrySet()) {
                url = url.contains("?")
                        ? url + "&" + entry.getKey() + "={" + entry.getKey() + "}"
                        : url + "?" + entry.getKey() + "={" + entry.getKey() + "}";
            }
            ResponseEntity<HttpIntegrationEventSubscriberAdapter.OperationResponse> response = null;
            try {
                Object payload = param.getCallbackUrl();
                String payloadJsonStr = payload == null ? null : JSON.toJSONString(payload);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/json; charset=utf-8");
                HttpEntity<byte[]> payloadJsonStrEntity = new HttpEntity<>(payloadJsonStr.getBytes(StandardCharsets.UTF_8.name()), headers);
                response = restTemplate.postForEntity(url, payloadJsonStrEntity, HttpIntegrationEventSubscriberAdapter.OperationResponse.class, uriParams);
            } catch (Throwable throwable) {
                log.error(String.format("集成事件HTTP订阅失败, %s (Client)", param.getEvent()), throwable);
                throw new RuntimeException(String.format("集成事件HTTP订阅失败, %s (Client)", param.getEvent()), throwable);
            }
            if (response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody().isSuccess()) {
                    log.info(String.format("集成事件HTTP订阅成功, %s", param.getEvent()));
                    return Response.builder().success(true).build();
                } else {
                    log.error(String.format("集成事件HTTP订阅失败, %s (Consume) %s", param.getEvent(), response.getBody().getMessage()));
                    throw new RuntimeException(String.format("集成事件HTTP订阅失败, %s (Consume) %s", param.getEvent(), response.getBody().getMessage()));
                }
            } else {
                log.error(String.format("集成事件HTTP订阅失败, %s (Server) %d", param.getEvent(), response.getStatusCode().value()));
                throw new RuntimeException(String.format("集成事件HTTP订阅失败, %s (Server) %d", param.getEvent(), response.getStatusCode().value()));
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request implements RequestParam<Response> {
        private String url;
        private String event;
        private String subscriber;
        private String callbackUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
    }
}
