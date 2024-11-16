package org.netcorepal.cap4j.ddd.domain.aggregate.impl;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.domain.aggregate.Specification;
import org.netcorepal.cap4j.ddd.domain.aggregate.SpecificationManager;
import org.netcorepal.cap4j.ddd.share.misc.ClassUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 默认实体规约管理器
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
public class DefaultSpecificationManager implements SpecificationManager {
    protected final List<Specification<?>> specifications;
    protected Map<Class<?>, List<Specification<?>>> specificationMap;

    public void init() {
        if (null != specificationMap) {
            return;
        }
        synchronized (this) {
            if (null != specificationMap) {
                return;
            }
            specificationMap = new java.util.HashMap<>();
            specifications.sort(Comparator.comparingInt(a -> OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE))
            );
            for (Specification<?> specification : specifications) {
                Class<?> entityClass = ClassUtils.resolveGenericTypeClass(
                        specification, 0,
                        Specification.class
                );
                if (!specificationMap.containsKey(entityClass)) {
                    specificationMap.put(entityClass, new java.util.ArrayList<>());
                }
                List<Specification<?>> specificationList = specificationMap.get(entityClass);
                specificationList.add(specification);
            }
        }
    }

    @Override
    public <Entity> Specification.Result specifyInTransaction(Entity entity) {
        init();
        List<Specification<?>> specifications = specificationMap.get(entity.getClass());
        if (specifications != null) {
            for (Specification<?> specification : specifications) {
                if (specification.beforeTransaction()) {
                    continue;
                }
                Specification.Result result = ((Specification<Entity>) specification).specify(entity);
                if (!result.isPassed()) {
                    return result;
                }
            }
        }
        return Specification.Result.pass();
    }

    @Override
    public <Entity> Specification.Result specifyBeforeTransaction(Entity entity) {
        init();
        List<Specification<?>> specifications = specificationMap.get(entity.getClass());
        if (specifications != null) {
            for (Specification<?> specification : specifications) {
                if (!specification.beforeTransaction()) {
                    continue;
                }
                Specification.Result result = ((Specification<Entity>) specification).specify(entity);
                if (!result.isPassed()) {
                    return result;
                }
            }
        }
        return Specification.Result.pass();
    }
}
