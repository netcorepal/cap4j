package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 聚合封装
 *
 * @author binking338
 * @date 2025/1/9
 */
public interface Aggregate<ENTITY> {
    ENTITY _unwrap();

    void _wrap(ENTITY root);

    class Default<ENTITY> implements Aggregate<ENTITY>{
        protected ENTITY root;

        @Override
        public ENTITY _unwrap() {
            return this.root;
        }

        @Override
        public void _wrap(ENTITY root) {
            this.root = root;
        }
    }
}
