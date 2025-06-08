package ${basePackage}.adapter.portal.api._share.configure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.web.bind.annotation.RestController;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger文档配置
 *
 * @author cap4j-ddd-codegen
 */
@Configuration
@Slf4j
//@EnableSwagger2
public class SwaggerConfig implements ApplicationListener<WebServerInitializedEvent> {
    @Value("${spring.application.name:${artifactId}}")
    private String applicationName;
    @Value("${spring.application.version:${version}}")
    private String applicationVersion;
    private String description = "";


    private OpenAPI openApiConfig(OpenAPI openAPI) {
        return openAPI.info(new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description(description));
    }

    /**
     * 用于单个文档情形，文档地址是统一的 /v3/api-docs，该文档地址可以通过配置项 springdoc.api-docs.path 进行自定义配置
     * 需依赖 com.github.xiaoymin:knife4j-springdoc-ui，并取消依赖 com.github.xiaoymin:knife4j-spring-boot-starter
     * 可配置项
     * springdoc.api-docs.path=/v3/openapi
     * springdoc.packagesToScan=package1, package2
     * springdoc.pathsToMatch=/v1, /api/balance/**
     *
     * @return
     */
    @Bean
    public OpenAPI openAPI() {
        return openApiConfig(new OpenAPI());
    }

    /**
     * 用于多个文档情形，文档地址会加分组名称 /v3/api-docs/{group}，该文档地址前缀可以通过配置项 springdoc.api-docs.path 进行自定义配置
     * knife4j ui需要分组支持
     *
     * @return
     */
    @Bean
    public GroupedOpenApi groupedOpenApi() {
        String[] paths = {"/**"};
        String pacakage = this.getClass().getPackage().getName().split(".adapter")[0]
                + ".adapter.portal.api";
        return GroupedOpenApi.builder()
                .group(applicationName)
                .pathsToMatch(paths)
                .packagesToScan(pacakage)
                .addOperationCustomizer((operation, handlerMethod) -> operation)
                .addOpenApiCustomizer(openApi -> openApiConfig(openApi))
                .build();
    }

//    /**
//     * springfox 文档配置
//     * 需依赖 com.github.xiaoymin:knife4j-spring-boot-starter，并取消依赖 com.github.xiaoymin:knife4j-springdoc-ui
//     * 标记 @EnableSwagger2
//     * @return
//     */
//    @Bean
//    public Docket docket() {
//        Docket docket=new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(new ApiInfoBuilder()
//                        .title(applicationName)
//                        .version(applicationVersion)
//                        .description(description)
//                        .build())
//                //.groupName(applicationName)
//                .select()
//                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
//                .paths(PathSelectors.any())
//                .build();
//        return docket;
//    }

    @Value("${server.port:80}")
    private String serverPort;
    @Value("${server.servlet.context-path:/}")
    private String serverServletContentPath;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
        log.info("swagger URL: http://localhost:" + serverPort + serverServletContentPath + "/swagger-ui/index.html");
        log.info("knife4j URL: http://localhost:" + serverPort + serverServletContentPath + "/doc.html");
    }
}
