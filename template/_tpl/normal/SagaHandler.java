package ${basePackage}.application.distributed.sagas${package};

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.saga.SagaHandler;
import org.springframework.stereotype.Service;

/**
 * ${Saga}事务请求实现
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ${Saga}Handler implements SagaHandler<${Saga}Request, ${Saga}Response> {

    @Override
    public ${Saga}Response exec(${Saga}Request cmd) {
        // this.execProcess("process1", req1);
        // this.execProcess("process2", req2);

        return ${Saga}Response.builder()
        .success(true)
        .build();
    }
}
