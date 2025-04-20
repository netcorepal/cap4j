package org.netcorepal.cap4j.ddd.domain.aggregate;

import org.netcorepal.cap4j.ddd.Mediator;

import static org.netcorepal.cap4j.ddd.domain.event.DomainEventSupervisorSupport.events;

/**
 * 聚合封装
 *
 * @author binking338
 * @date 2025/1/9
 */
public interface Aggregate<ENTITY> {
    /**
     * 获取ORM实体
     * 仅供框架调用使用，勿在业务逻辑代码中使用
     * @return
     */
    ENTITY _unwrap();

    /**
     * 封装ORM实体
     * 仅供框架调用使用，勿在业务逻辑代码中使用
     * @param root
     */
    void _wrap(ENTITY root);

    abstract class Default<ENTITY> implements Aggregate<ENTITY> {
        protected ENTITY root;

        public Default(Object payload) {
            if(payload != null && !(payload instanceof AggregatePayload)){
                throw new IllegalArgumentException("payload must be AggregatePayload");
            }
            ENTITY root = Mediator.factories().create((AggregatePayload<ENTITY>) payload);
            _wrap(root);
        }

        /**
         * 获取ORM实体
         * 仅供框架调用使用，勿在业务逻辑代码中使用
         * @return
         */
        @Override
        public ENTITY _unwrap() {
            return this.root;
        }

        /**
         * 封装ORM实体
         * 仅供框架调用使用，勿在业务逻辑代码中使用
         * @param root
         */
        @Override
        public void _wrap(ENTITY root) {
            this.root = root;
        }


        /**
         * 注册领域事件到持久化上下文
         *
         * @param event
         */
        protected void registerDomainEvent(Object event) {
            events().attach(event, this);
        }

        /**
         * 从当前持久化上下文中取消领域事件
         *
         * @param event
         */
        protected void cancelDomainEvent(Object event) {
            events().detach(event, this);
        }

    }
}
