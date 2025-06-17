package org.netcorepal.cap4j.ddd.console.event.http;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件HTTP订阅
 *
 * @author binking338
 * @date 2025/6/10
 */
@RequiredArgsConstructor
public class EventHttpSubscriberConsoleService {
    private final JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    public void init() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Data
    public static class HttpSubscriberInfo {
        private Long id;
        private String event;
        private String subscriber;
        private String callbackUrl;
    }


    @Data
    public static class SearchParam extends PageParam {
        private String event;
        private String subscriber;
    }

    private static final String SQL_SELECT = "SELECT " +
            "id, " +
            "event, " +
            "subscriber, " +
            "callback_url as callbackUrl " +
            "FROM __event_http_subscriber " +
            "WHERE %s " +
            "ORDER BY id " +
            "LIMIT %d,%d";

    public PageData<HttpSubscriberInfo> search(SearchParam param) {
        Map<String, Object> params = new HashMap<>();
        List<HttpSubscriberInfo> list = Collections.emptyList();
        StringBuilder where = new StringBuilder("true");
        if (param.getEvent() != null && !param.getEvent().isEmpty()) {
            where.append(" and event like concat('%', :event, '%')");
            params.put("event", param.getEvent());
        }
        if (param.getSubscriber() != null && !param.getSubscriber().isEmpty()) {
            where.append(" and subscriber like concat('%', :subscriber, '%')");
            params.put("subscriber", param.getSubscriber());
        }

        long count = namedParameterJdbcTemplate.queryForObject(String.format(
                        "SELECT count(*) FROM __event_http_subscriber where %s",
                        where
                ),
                params, Long.class
        );
        if (count > 0) {
            list = namedParameterJdbcTemplate.query(String.format(
                            SQL_SELECT,
                            where,
                            param.getPageSize() * (param.getPageNum() - 1),
                            param.getPageSize()
                    ),
                    params, new BeanPropertyRowMapper(HttpSubscriberInfo.class));
        }
        return PageData.create(param, count, list);
    }
}
