package org.ddd.example.adapter.portal.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.ddd.domain.repo.Repository;
import org.ddd.example.application.samples.commands.bill.PayBillCmd;
import org.ddd.example.application.samples.commands.order.CloseOrderCmd;
import org.ddd.example.application.samples.queries.GetOrderQry;
import org.ddd.example.application.samples.queries.JdbcTemplateQry;
import org.ddd.example.application.samples.commands.order.PlaceOrderCmd;
import org.ddd.example.domain.aggregates.samples.Account;
import org.ddd.example.adapter.portal.api._share.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * TODO 后续可以考虑基于Command和Query的注解，自动生成Controller
 *
 * @author <template/>
 * @date
 */
@Tag(name = "测试控制器")
@RestController
@RequestMapping(value = "/appApi/test")
@Slf4j
public class TestAppController {

    @Autowired
    Repository<Account> accountRepository;
    @Autowired
    Environment environment;

    @PostMapping(value = "")
    public ResponseData<String> test(@RequestBody() String body) {
        return ResponseData.success("hello world");
    }

    @Autowired
    PlaceOrderCmd.Handler placeOrderCmdHandler;

    @PostMapping("/placeOrderCmd")
    public ResponseData<Long> placeOrderCmd(@RequestBody @Validated PlaceOrderCmd cmd) {
        Long result = placeOrderCmdHandler.exec(cmd);
        return ResponseData.success(result);
    }


    @Autowired
    CloseOrderCmd.Handler closeOrderCmdHandler;

    @PostMapping("/closeOrderCmd")
    public ResponseData<Boolean> closeOrderCmd(@RequestBody CloseOrderCmd cmd) {
        Boolean result = closeOrderCmdHandler.exec(cmd);
        return ResponseData.success(result);
    }


    @Autowired
    PayBillCmd.Handler payBillCmdHandler;

    @PostMapping("/payBillCmd")
    public ResponseData<Boolean> payBillCmd(@RequestBody PayBillCmd cmd) {
        Boolean result = payBillCmdHandler.exec(cmd);
        return ResponseData.success(result);
    }


    @Autowired
    JdbcTemplateQry.Handler jdbcTemplateQryHandler;

    @GetMapping("/jdbcTemplateQry")
    public ResponseData<JdbcTemplateQry.JdbcTemplateQryDto> jdbcTemplateQry(JdbcTemplateQry param) {
        JdbcTemplateQry.JdbcTemplateQryDto result = jdbcTemplateQryHandler.exec(param);
        return ResponseData.success(result);
    }


    @Autowired
    GetOrderQry.Handler getOrderQryHandler;

    @GetMapping("/getOrderQry")
    public ResponseData<GetOrderQry.GetOrderQryDto> getOrderQry(GetOrderQry param) {
        GetOrderQry.GetOrderQryDto result = getOrderQryHandler.exec(param);
        return ResponseData.success(result);
    }

}
