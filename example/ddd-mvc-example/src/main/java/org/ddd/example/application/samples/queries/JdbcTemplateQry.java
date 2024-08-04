package org.ddd.example.application.samples.queries;

import com.alibaba.fastjson.JSON;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.ddd.example.adapter.infra.jdbc.NamedParameterJdbcTemplateDao;
import org.ddd.application.query.Query;
import org.ddd.domain.repo.JpaUnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import java.util.*;


/**
 * 查询描述
 *
 * @date 2023/12/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcTemplateQry {
    private long id;
    private String name;

    @Convert(converter = Handler.AppointmentType.Converter.class)
    private Handler.AppointmentType type = Handler.AppointmentType.OTHER_PLACE;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements Query<JdbcTemplateQry, JdbcTemplateQryDto> {
        private final JdbcTemplate jdbcTemplate;
        private final JpaUnitOfWork jpaUnitOfWork;
        public static enum AppointmentType {

            /**
             * 到店
             */
            STORE(0, "到店"),
            /**
             * 远程
             */
            REMOTE(2, "远程"),
            /**
             * 店外
             */
            OTHER_PLACE(1, "店外"),
            /**
             * 确认到店
             */
            VISIT_STORE(3, "确认到店"),
            ;
            @Getter
            private int code;
            @Getter
            private String name;

            AppointmentType(Integer code, String name){
                this.code = code;
                this.name = name;
            }

            private static Map<Integer, AppointmentType> enums = null;
            public static AppointmentType valueOf(Integer code) {
                if(enums == null) {
                    enums = new HashMap<>();
                    for (AppointmentType val : AppointmentType.values()) {
                        enums.put(val.code, val);
                    }
                }
                if(enums.containsKey(code)){
                    return enums.get(code);
                }
                throw new RuntimeException("枚举类型AppointmentType枚举值转换异常，不存在的值" + code);
            }

            /**
             * JPA转换器
             */
            public static class Converter implements AttributeConverter<AppointmentType, Integer> {
                @Override
                public Integer convertToDatabaseColumn(AppointmentType  val) {
                    return val.code;
                }

                @Override
                public AppointmentType convertToEntityAttribute(Integer code) {
                    return AppointmentType.valueOf(code);
                }
            }
        }
        @Autowired
        SelectByIdJdbcTemplateQryDto orderMapper;

        @Override
        public JdbcTemplateQryDto exec(JdbcTemplateQry param) {
            JdbcTemplateQryDto order = orderMapper.selectOrderById(1, AppointmentType.STORE);
            NamedParameterJdbcTemplateDao namedParameterJdbcTemplateDao = new NamedParameterJdbcTemplateDao(jdbcTemplate);
            Map<String,Object> params = (Map<String,Object>)JSON.toJSON(param);
            JdbcTemplateQryDto result = namedParameterJdbcTemplateDao.queryList(
                    JdbcTemplateQryDto.class,
                    "select 2 as type, o.id, name as order_name, o.amount, finished, closed, update_at from `order` o where id=1",
                    param).stream().findFirst().orElse(null);
                    //.stream().queryFirst().orElse(null)0
            Long id = namedParameterJdbcTemplateDao.queryFirst(
                    Long.class,
                    "select o.id from `order` o where id>0 ;",
                    param).orElse(null)
            //.stream().queryFirst().orElse(null)
            ;
            Long count = namedParameterJdbcTemplateDao.queryOne(Long.class,
                    "select count(*) from `order` where id=:id ",
                        param, params)
            //.stream().queryFirst().orElse(null)
            ;
            AppointmentType at = namedParameterJdbcTemplateDao.queryOne(AppointmentType.class,
                    "select count(*) from `order` where id=:id ",
                    param, params);
            return result;
        }
    }

    public static interface SelectByIdJdbcTemplateQryDto{
        JdbcTemplateQryDto selectOrderById(int id, Handler.AppointmentType type);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JdbcTemplateQryDto {
        private Long id;
        private String orderName;
        private int amount;
        //@Convert(converter = Handler.AppointmentType.Converter.class)
        private Handler.AppointmentType type;
        private boolean finished;
        private boolean closed;
        private Date updateAt;
    }
}