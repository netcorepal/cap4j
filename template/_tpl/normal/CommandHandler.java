package ${basePackage}.application.commands${package};

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netcorepal.cap4j.ddd.Mediator;
import org.netcorepal.cap4j.ddd.application.command.Command;
import org.springframework.stereotype.Service;

/**
 * ${Command}命令请求实现
 * ${CommentEscaped}
 *
 * @author cap4j-ddd-codegen
 * @date ${date}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ${Command}Handler implements Command<${Command}Request, ${Command}Response> {

    @Override
    public ${Command}Response exec(${Command}Request cmd) {
        Mediator.uow().save();

        return ${Command}Response.builder()
            .success(true)
            .build();
    }
}
