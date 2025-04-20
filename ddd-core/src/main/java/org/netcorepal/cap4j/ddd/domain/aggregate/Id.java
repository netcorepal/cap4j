package org.netcorepal.cap4j.ddd.domain.aggregate;

/**
 * 实体ID
 *
 * @author binking338
 * @date 2025/4/8
 */
public interface Id<AGGREGATE, KEY> {

    /**
     * 获取实体Key
     *
     * @return
     */
    KEY getValue();

    abstract class Default<AGGREGATE, KEY> implements Id<AGGREGATE, KEY> {
        protected final KEY key;

        public Default(KEY key) {
            this.key = key;
        }

        public KEY getValue() {
            return key;
        }

        @Override
        public String toString() {
            return key == null ? null : key.toString();
        }

        @Override
        public int hashCode() {
            return key == null ? 0 : key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Default) || !obj.getClass().equals(this.getClass())) {
                return false;
            }
            return java.util.Objects.equals(key, ((Default<AGGREGATE, KEY>) obj).key);
        }
    }
}
