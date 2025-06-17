package org.netcorepal.cap4j.ddd.console.snowflake;

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
 * Snowflake
 *
 * @author binking338
 * @date 2025/6/10
 */
@RequiredArgsConstructor
public class SnowflakeConsoleService {

    private final JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    public void init() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Data
    public static class WorkerIdInfo {
        /**
         * 数据中心ID
         */
        private int datacenterId;
        /**
         * 工作ID
         */
        private int workerId;
        /**
         * 是否空闲
         */
        private boolean free;
        /**
         * 最近分配对象
         */
        private String dispatchTo;
        /**
         * 最近分配时间
         */
        private String dispatchAt;
        /**
         * 最近过期时间
         */
        private String expireAt;
    }


    /**
     * 查询参数
     */
    @Data
    public static class SearchParam extends PageParam {
        /**
         * 是否空闲
         */
        private Boolean free;
        /**
         * 最近分配对象
         */
        private String dispatchTo;
    }

    private static final String SQL_SELECT = "SELECT " +
            "datacenter_id as datacenterId, " +
            "worker_id  as workerId, " +
            "expire_at<=now() as free, " +
            "dispatch_to as dispatchTo, " +
            "dispatch_at as dispatchAt, " +
            "expire_at as expireAt " +
            "FROM __worker_id " +
            "WHERE %s " +
            "ORDER BY id " +
            "LIMIT %d,%d";

    public PageData<WorkerIdInfo> search(SearchParam param) {
        Map<String, Object> params = new HashMap<>();
        List<WorkerIdInfo> list = Collections.emptyList();
        StringBuilder where = new StringBuilder("true");
        if (param.getFree() != null) {
            where.append(param.getFree()
                    ? " and expire_at <= now()"
                    : " and expire_at > now()"
            );
        }
        if (param.getDispatchTo() != null) {
            where.append(" and dispatch_to like concat('%', :dispatchTo, '%'）");
            params.put("dispatchTo", param.getDispatchTo());
        }

        long count = namedParameterJdbcTemplate.queryForObject(String.format(
                        "SELECT count(*) FROM __worker_id where %s",
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
                    params, new BeanPropertyRowMapper(WorkerIdInfo.class));
        }
        return PageData.create(param, count, list);
    }
}
