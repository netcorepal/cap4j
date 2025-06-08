package ${basePackage}.adapter.application.queries;

import ${basePackage}.application.queries${package}.${Query};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.query.ListQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${Query}查询请求适配实现
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ${Query}Handler implements ListQuery<${Query}.Request, ${Query}.Response> {

    @Override
    public List<${Query}.Response> exec(${Query}.Request request) {
        // mybatis / jpa 哪个顺手就用哪个吧！
        return null;
    }
}
