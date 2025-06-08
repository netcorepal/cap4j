package ${basePackage}.application.distributed.sagas${package};

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.saga.SagaParam;
import org.netcorepal.cap4j.ddd.application.saga.SagaHandler;
import org.springframework.stereotype.Service;

/**
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
public class ${Saga} {

    /**
     * ${Saga}事务请求实现
     */
    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class Handler implements SagaHandler<Request, Response> {
        @Override
        public Response exec(Request cmd) {
            // this.execProcess("process1", req1);
            // this.execProcess("process2", req2);

            return Response.builder()
                    .success(true)
                    .build();
        }
    }

    /**
     * ${Saga}事务请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request implements SagaParam<Response> {
        String param;
    }

    /**
     * ${Saga}事务响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        boolean success;
    }
}
