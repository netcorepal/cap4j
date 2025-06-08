package ${basePackage}.adapter.portal.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import ${basePackage}.adapter.portal.api._share.ResponseData;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author cap4j-ddd-codegen
 */
@Tag(name = "测试控制器")
@RestController
@RequestMapping(value = "/test")
@Slf4j
public class TestController {

    @PostMapping(value = "")
    public ResponseData<String> test(@RequestParam("msg") String msg) {
        return ResponseData.success("echo: " + msg);
    }

}
