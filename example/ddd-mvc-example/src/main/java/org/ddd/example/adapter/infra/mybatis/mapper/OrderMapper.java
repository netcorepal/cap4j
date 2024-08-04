package org.ddd.example.adapter.infra.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ddd.example.application.samples.queries.JdbcTemplateQry;

/**
 * @author binking338
 * @date 2024/4/18
 */
@Mapper
public interface OrderMapper extends JdbcTemplateQry.SelectByIdJdbcTemplateQryDto {
    JdbcTemplateQry.JdbcTemplateQryDto selectOrderById(@Param("id") int id, @Param("type") JdbcTemplateQry.Handler.AppointmentType type);
}
