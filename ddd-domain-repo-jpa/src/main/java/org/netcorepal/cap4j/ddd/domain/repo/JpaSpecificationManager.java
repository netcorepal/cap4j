package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

import java.util.List;
import java.util.Map;

/**
 * 基于Jpa的实体规约管理器
 *
 * @author binking338
 * @date 2023/8/13
 */
@RequiredArgsConstructor
@Slf4j
public class JpaSpecificationManager implements SpecificationManager {
    private final List<AbstractJpaSpecification> specifications;
    private Map<Class, List<AbstractJpaSpecification>> specificationMap;

    public void init(){
        if(specificationMap != null){
            return;
        }
        synchronized (this){

            if(specificationMap != null){
                return;
            }
            specificationMap = new java.util.HashMap<Class, List<AbstractJpaSpecification>>();
            specifications.sort((a,b)->
                    OrderUtils.getOrder(a.getClass(), Ordered.LOWEST_PRECEDENCE) - OrderUtils.getOrder(b.getClass(), Ordered.LOWEST_PRECEDENCE)
            );
            for (AbstractJpaSpecification specification : specifications) {
                if(!specificationMap.containsKey(specification.forEntityClass())){
                    specificationMap.put(specification.forEntityClass(), new java.util.ArrayList<AbstractJpaSpecification>());
                }
                List<AbstractJpaSpecification> specificationList = specificationMap.get(specification.forEntityClass());
                specificationList.add(specification);
            }
        }
    }

    @Override
    public <Entity> Specification.Result specify(Entity entity) {
        init();
        List<AbstractJpaSpecification> specifications = specificationMap.get(entity.getClass());
        if(specifications != null) {
            for (AbstractJpaSpecification specification : specifications) {
                if(!specification.forceBeforeTransaction()) continue;
                Specification.Result result = specification.specify(entity);
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
        List<AbstractJpaSpecification> specifications = specificationMap.get(entity.getClass());
        if(specifications != null) {
            for (AbstractJpaSpecification specification : specifications) {
                if(!specification.forceBeforeTransaction()) continue;
                Specification.Result result = specification.specify(entity);
                if (!result.isPassed()) {
                    return result;
                }
            }
        }
        return Specification.Result.pass();
    }
}
