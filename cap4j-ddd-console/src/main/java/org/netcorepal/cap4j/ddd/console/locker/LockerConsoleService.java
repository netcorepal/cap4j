package org.netcorepal.cap4j.ddd.console.locker;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 锁
 *
 * @author binking338
 * @date 2025/6/10
 */
@RequiredArgsConstructor
public class LockerConsoleService {
    private final JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    public void init() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Data
    public static class LockerInfo {
        /**
         * 锁名称
         */
        private String name;
        /**
         * 是否锁定
         */
        private boolean lock;
        /**
         * 最近锁定时间
         */
        private String lockAt;
        /**
         * 最近解锁时间
         */
        private String unlockAt;
        /**
         * 最近锁定密码
         */
        private String pwd;
    }


    @Data
    public static class SearchParam extends PageParam {
        /**
         * 锁名称
         */
        private String name;
        /**
         * 是否锁定
         */
        private Boolean lock;
    }

    private static final String SQL_SELECT = "SELECT " +
            "name, " +
            "unlock_at>now() as `lock`, " +
            "lock_at as lockAt, " +
            "unlock_at as unlockAt, " +
            "pwd as pwd " +
            "FROM __locker " +
            "WHERE %s " +
            "ORDER BY id " +
            "LIMIT %d,%d";

    public PageData<LockerInfo> search(SearchParam param) {
        Map<String, Object> params = new HashMap<>();
        List<LockerInfo> list = Collections.emptyList();
        StringBuilder where = new StringBuilder("true");
        if (param.getName() != null && !param.getName().isEmpty()) {
            where.append(" and name like concat('%', :name, '%'）");
            params.put("name", param.getName());
        }
        if (param.getLock() != null) {
            where.append(param.getLock()
                    ? " and unlock_at > now()"
                    : " and unlock_at <= now()"
            );
        }

        long count = namedParameterJdbcTemplate.queryForObject(String.format(
                        "SELECT count(*) FROM __locker where %s",
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
                    params, new BeanPropertyRowMapper(LockerInfo.class));
        }
        return PageData.create(param, count, list);
    }

    public boolean unlock(String name, String pwd) {
        return jdbcTemplate.update(
                "UPDATE __locker SET unlock_at=now() WHERE name=? AND pwd=? AND unlock_at>now()",
                name, pwd
        ) > 0;
    }
}
