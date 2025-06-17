package org.netcorepal.cap4j.ddd.console.saga;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Saga服务
 *
 * @author binking338
 * @date 2025/6/10
 */
@RequiredArgsConstructor
public class SagaConsoleService {
    private final JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    public void init() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Data
    public static class SagaInfo {
        private Long id;
        private String uuid;
        private String type;
        private String service;
        private String payload;
        private String payloadType;
        private String result;
        private String resultType;
        private String scheduleAt;
        private int state;
        private String stateName;
        private String expireAt;
        private String lastTryTime;
        private String nextTryTime;
        private int retryLimit;
        private int retryCount;
        private String exception;
        private List<SagaProcessInfo> processes;
    }

    @Data
    public static class SagaProcessInfo {
        private String code;
        private String payload;
        private String payloadType;
        private String result;
        private String resultType;
        private String lastTryTime;
        private int retryCount;
        private String exception;
    }


    @Data
    public static class SearchParam extends PageParam {
        private String uuid;
        private String type;
        private int[] state;
        private LocalDateTime[] scheduleAt;
    }

    private static final String SQL_SELECT = "SELECT " +
            "id, " +
            "saga_uuid as uuid, " +
            "saga_type as type, " +
            "svc_name as service, " +
            "param as payload, " +
            "param_type as payloadType, " +
            "result as result, " +
            "result_type as resultType, " +
            "create_at as scheduleAt, " +
            "saga_state as state, " +
            "'' as stateName, " +
            "expire_at as expireAt, " +
            "last_try_time as lastTryTime, " +
            "next_try_time as nextTryTime, " +
            "try_times as retryLimit, " +
            "tried_times as retryCount, " +
            "exception as exception " +
            "FROM __saga " +
            "WHERE %s " +
            "ORDER BY id desc " +
            "LIMIT %d,%d";
    private static final String SQL_SUB_SELECT = "SELECT " +
            "process_code as code, " +
            "param as payload, " +
            "param_type as payloadType, " +
            "result as result, " +
            "result_type as resultType, " +
            "last_try_time as lastTryTime, " +
            "tried_times as retryCount, " +
            "exception as exception " +
            "FROM __saga_process " +
            "WHERE saga_id=%d";

    public PageData<SagaInfo> search(SearchParam param) {
        Map<String, Object> params = new HashMap<>();
        List<SagaInfo> list = Collections.emptyList();
        StringBuilder where = new StringBuilder("true");
        if (param.getUuid() != null && !param.getUuid().isEmpty()) {
            where.append(" and saga_uuid = :uuid");
            params.put("uuid", param.getUuid());
        }
        if (param.getType() != null && !param.getType().isEmpty()) {
            where.append(" and saga_type = :type");
            params.put("type", param.getType());
        }
        if (param.getState() != null && param.getState().length > 0) {
            where.append(" and saga_state in (");
            for (int i = 0; i < param.getState().length; i++) {
                where.append(i == 0 ? String.format(":state%d", i) : String.format(",:status%d", i));
                params.put(String.format("state%d", i), param.getState()[i]);
            }
            where.append(")");
        }
        if (param.getScheduleAt() != null && param.getScheduleAt().length > 0) {
            where.append(" and create_at > :schedule0");
            params.put("schedule0", param.getScheduleAt()[0]);
            if (param.getScheduleAt().length > 1) {
                where.append(" and create_at < :schedule1");
                params.put("schedule1", param.getScheduleAt()[1]);
            }
        }
        long count = namedParameterJdbcTemplate.queryForObject(String.format(
                        "SELECT count(*) FROM __saga where %s",
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
                    params, new BeanPropertyRowMapper(SagaInfo.class));
            for (SagaInfo info : list) {
                info.setStateName(getStateName(info.getState()));
                info.setProcesses(namedParameterJdbcTemplate.query(String.format(SQL_SUB_SELECT, info.id)
                        , new BeanPropertyRowMapper(SagaProcessInfo.class)));
            }
        }
        return PageData.create(param, count, list);
    }

    private String getStateName(int state) {
        switch (state) {
            case 0:
                return "初始";
            case 1:
                return "完成";
            case -1:
                return "执行中";
            case -2:
                return "取消";
            case -3:
                return "超时";
            case -4:
                return "超限";
            case -9:
                return "异常";
            default:
                return "未知";
        }
    }
}
