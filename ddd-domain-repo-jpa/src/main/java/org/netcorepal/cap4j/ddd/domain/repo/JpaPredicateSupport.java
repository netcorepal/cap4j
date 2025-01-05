package org.netcorepal.cap4j.ddd.domain.repo;

import org.springframework.data.jpa.domain.Specification;

import java.util.Iterator;

/**
 * 断言Support
 *
 * @author binking338
 * @date 2024/9/11
 */
public class JpaPredicateSupport {

    /**
     * 复原ID
     *
     * @param predicate
     * @param <Entity>
     * @return
     */
    public static <Entity> Object resumeId(Predicate<Entity> predicate) {
        Iterable<Object> ids = ((JpaPredicate<Entity>) predicate).ids;
        if (ids == null) {
            return null;
        }
        Iterator<Object> iterator = ids.iterator();
        if (!iterator.hasNext()){
            return null;
        }
        return iterator.next();
    }

    /**
     * 复原IDS
     *
     * @param predicate
     * @param <Entity>
     * @return
     */
    public static <Entity> Iterable<Object> resumeIds(Predicate<Entity> predicate) {
        return ((JpaPredicate<Entity>) predicate).ids;
    }

    /**
     * 复原Specification
     *
     * @param predicate
     * @param <Entity>
     * @return
     */
    public static <Entity> Specification<Entity> resumeSpecification(Predicate<Entity> predicate) {
        return ((JpaPredicate<Entity>) predicate).spec;
    }

    /**
     * 获取断言实体类型
     *
     * @param predicate
     * @param <Entity>
     * @return
     */
    public static <Entity> Class<Entity> reflectEntityClass(Predicate<Entity> predicate) {
        return ((JpaPredicate<Entity>) predicate).entityClass;
    }
}
