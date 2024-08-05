package org.netcorepal.cap4j.ddd.domain.repo;

import lombok.Getter;

/**
 * 实体规格约束
 *
 * @author binking338
 * @date 2023/8/5
 */
public interface Specification<Entity> {
    /**
     * 校验实体是否符合规格约束
     *
     * @param entity
     * @return
     */
    Result specify(Entity entity);

    /**
     * 规格校验结果
     */
    @Getter
    public static class Result {
        /**
         * 是否通过规格校验
         */
        private boolean passed;
        /**
         * 规格校验反馈消息
         */
        private String message;

        public Result(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }

        public static Result pass() {
            return new Result(true, null);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
