package org.netcorepal.cap4j.ddd.domain.repo.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JpaUnitOfWork配置类
 *
 * @author binking338
 * @date 2024/8/11
 */
@Data
@Configuration
@ConfigurationProperties("cap4j.ddd.application.jpa-uow")
public class JpaUnitOfWorkProperties {
    /**
     * 单次获取记录数
     */
    int retrieveCountWarnThreshold = 3000;

    /**
     * 是否支持实体内联持久化监听器
     * 创建 onCreate
     * 更新 onUpdate
     * 删除 onDelete | onRemove
     */
    boolean supportEntityInlinePersistListener = true;

    /**
     * 是否在保存时检查值对象是否存在
     */
    boolean supportValueObjectExistsCheckOnSave = true;

    /**
     * 通用主键字段名
     */
    String generalIdFieldName = "id";
}
