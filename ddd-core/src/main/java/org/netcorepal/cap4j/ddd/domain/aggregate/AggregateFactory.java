package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 聚合工厂
 *
 * @author binking338
 * @date 2024/9/3
 */
public interface AggregateFactory<ENTITY> {

    /**
     * 创建新聚合实例
     *
     * @param initHandler
     * @return
     */
    ENTITY create(InitHandler<ENTITY> initHandler);

    public static interface InitHandler<ENTITY> {
        public static InitHandler<Object> getDefault() {
            return new EmptyInitHandler();
        }

        void init(ENTITY entity);

        static class EmptyInitHandler implements InitHandler<Object> {
            public void init(Object entity) {
            }
        }
    }
}
