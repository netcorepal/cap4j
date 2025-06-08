package ${basePackage}.adapter.application.distributed.clients${package};

import ${basePackage}.application.distributed.clients${package}.${Client};
import org.netcorepal.cap4j.ddd.application.RequestHandler;
import org.springframework.stereotype.Service;

/**
 * ${Client}防腐端适配实现
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Service
public class ${Client}Handler implements RequestHandler<${Client}.Request, ${Client}.Response> {
    @Override
    public ${Client}.Response exec(${Client}.Request request) {
        return null;
    }
}
