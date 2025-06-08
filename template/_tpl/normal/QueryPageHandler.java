package ${basePackage}.adapter.application.queries;

import ${basePackage}.application.queries${package}.${Query}Request;
import ${basePackage}.application.queries${package}.${Query}Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.application.query.PageQuery;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.springframework.stereotype.Service;

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
public class ${Query}Handler implements PageQuery<${Query}Request, ${Query}Response> {

    @Override
    public PageData<${Query}Response> exec(${Query}Request request) {
        // mybatis / jpa 哪个顺手就用哪个吧！
        return null;
    }
}
