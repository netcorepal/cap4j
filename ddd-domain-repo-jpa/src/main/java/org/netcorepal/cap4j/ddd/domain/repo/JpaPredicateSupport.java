package org.netcorepal.cap4j.ddd.domain.repo;

import org.springframework.data.jpa.domain.Specification;

import java.util.Iterator;

/**
 * Jpa仓储检索断言Support
 *
 * @author binking338
 * @date 2024/9/11
 */
public class JpaPredicateSupport {

    /**
     * 复原ID
     *
     * @param predicate
     * @param <ENTITY>
     * @param <ID>
     * @return
     */
    public static <ENTITY, ID> ID resumeId(Predicate<ENTITY> predicate) {
        if (!(predicate instanceof JpaPredicate)) {
            return null;
        }
        Iterable<Object> ids = ((JpaPredicate<ENTITY>) predicate).ids;
        if (ids == null) {
            return null;
        }
        Iterator<Object> iterator = ids.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        return (ID) iterator.next();
    }

    /**
     * 复原IDS
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    public static <ENTITY, ID> Iterable<ID> resumeIds(Predicate<ENTITY> predicate) {
        if (!(predicate instanceof JpaPredicate)) {
            return null;
        }
        return (Iterable<ID>) ((JpaPredicate<ENTITY>) predicate).ids;
    }

    /**
     * 复原Specification
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    public static <ENTITY> Specification<ENTITY> resumeSpecification(Predicate<ENTITY> predicate) {
        if (!(predicate instanceof JpaPredicate)) {
            return null;
        }
        return ((JpaPredicate<ENTITY>) predicate).spec;
    }

    /**
     * 获取断言实体类型
     *
     * @param predicate
     * @param <ENTITY>
     * @return
     */
    public static <ENTITY> Class<ENTITY> reflectEntityClass(Predicate<ENTITY> predicate) {
        if (!(predicate instanceof JpaPredicate)) {
            return null;
        }
        return ((JpaPredicate<ENTITY>) predicate).entityClass;
    }
}
