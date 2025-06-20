package org.netcorepal.cap4j.ddd.codegen;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.TextUtils;
import org.netcorepal.cap4j.ddd.codegen.template.PathNode;
import org.netcorepal.cap4j.ddd.codegen.template.TemplateNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 生成设计框架代码
 *
 * @author binking338
 * @date 2024/9/13
 */
@Mojo(name = "gen-design")
public class GenDesignMojo extends GenArchMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.renderFileSwitch = false;
        super.execute();
    }

    public String alias4Design(String name) {
        switch (name.toLowerCase()) {
            case "commands":
            case "command":
            case "cmd":
                return "command";
            case "saga":
                return "saga";
            case "queries":
            case "query":
            case "qry":
                return "query";
            case "clients":
            case "client":
            case "cli":
                return "client";
            case "integration_events":
            case "integration_event":
            case "events":
            case "event":
            case "evt":
            case "i_e":
            case "ie":
                return "integration_event";
            case "integration_event_handlers":
            case "integration_event_handler":
            case "event_handlers":
            case "event_handler":
            case "evt_hdl":
            case "i_e_h":
            case "ieh":
            case "integration_event_subscribers":
            case "integration_event_subscriber":
            case "event_subscribers":
            case "event_subscriber":
            case "evt_sub":
            case "i_e_s":
            case "ies":
                return "integration_event_handler";
            case "repositories":
            case "repository":
            case "repos":
            case "repo":
                return "repository";
            case "factories":
            case "factory":
            case "fac":
                return "factory";
            case "specifications":
            case "specification":
            case "specs":
            case "spec":
            case "spe":
                return "specification";
            case "domain_events":
            case "domain_event":
            case "d_e":
            case "de":
                return "domain_event";
            case "domain_event_handlers":
            case "domain_event_handler":
            case "d_e_h":
            case "deh":
            case "domain_event_subscribers":
            case "domain_event_subscriber":
            case "d_e_s":
            case "des":
                return "domain_event_handler";
            case "domain_service":
            case "service":
            case "svc":
                return "domain_service";
            default:
                return name;
        }
    }

    public Map<String, Set<String>> resolveLiteralDesign(String design) {
        if (StringUtils.isBlank(design)) {
            return new HashMap<>();
        }
        return Arrays.stream(escape(design).replaceAll("\\r\\n|\\r|\\n", ";").split(PATTERN_SPLITTER))
                .map(item -> TextUtils.splitWithTrim(item, PATTERN_DESIGN_PARAMS_SPLITTER, 2))
                .filter(item -> item.length == 2)
                .collect(Collectors.groupingBy(
                        g -> alias4Design(g[0]),
                        Collectors.mapping(
                                g -> g[1].trim(),
                                Collectors.toSet()
                        )
                ));
    }

    /**
     * 构建模型设计元素
     *
     * @param templateNodes
     * @param parentPath
     * @throws IOException
     */
    @Override
    public void renderTemplate(List<TemplateNode> templateNodes, String parentPath) throws IOException {
        String design = "";
        if (StringUtils.isNotBlank(this.design)) {
            design += this.design;
        }
        if (StringUtils.isNotBlank(this.designFile) && FileUtils.fileExists(this.designFile)) {
            design += (";" + FileUtils.fileRead(this.designFile, this.archTemplateEncoding));
        }
        Map<String, Set<String>> designMap = resolveLiteralDesign(design);

        for (TemplateNode templateNode : templateNodes) {
            switch (alias4Design(templateNode.getTag())) {
                case "command":
                    if (designMap.containsKey("command")) {
                        for (String literalDesign : designMap.get("command")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerCommand(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "saga":
                    if (designMap.containsKey("saga")) {
                        for (String literalDesign : designMap.get("saga")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerSaga(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "query":
                case "query_handler":
                    if (designMap.containsKey("query")) {
                        for (String literalDesign : designMap.get("query")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerQuery(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "client":
                case "client_handler":
                    if (designMap.containsKey("client")) {
                        for (String literalDesign : designMap.get("client")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerClient(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "integration_event":
                    if (designMap.containsKey("integration_event")) {
                        for (String literalDesign : designMap.get("integration_event")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerIntegrationEvent(true, "integration_event", literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    if (designMap.containsKey("integration_event_handler")) {
                        for (String literalDesign : designMap.get("integration_event_handler")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerIntegrationEvent(false, "integration_event", literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "integration_event_handler":
                    if (designMap.containsKey("integration_event_handler")) {
                        for (String literalDesign : designMap.get("integration_event_handler")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerIntegrationEvent(false, "integration_event_handler", literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "domain_event":
                    if (designMap.containsKey("domain_event")) {
                        for (String literalDesign : designMap.get("domain_event")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerDomainEvent(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "domain_event_handler":
                    if (designMap.containsKey("domain_event")) {
                        for (String literalDesign : designMap.get("domain_event")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerDomainEvent(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    if (designMap.containsKey("domain_event_handler")) {
                        for (String literalDesign : designMap.get("domain_event_handler")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerDomainEvent(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "specification":
                    if (designMap.containsKey("specification")) {
                        for (String literalDesign : designMap.get("specification")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerSpecification(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "factory":
                    if (designMap.containsKey("factory")) {
                        for (String literalDesign : designMap.get("factory")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerAggregateFactory(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "domain_service":
                    if (designMap.containsKey("domain_service")) {
                        for (String literalDesign : designMap.get("domain_service")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerDomainService(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                default:
                    if (designMap.containsKey(templateNode.getTag())) {
                        for (String literalDesign : designMap.get(templateNode.getTag())) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderGenericDesign(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * @param literalCommandDeclaration 文本化命令声明 CommandName
     * @param templateNode              模板配置
     */
    public void renderAppLayerCommand(String literalCommandDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析命令设计：" + literalCommandDeclaration);
        String path = internalRenderGenericDesign(literalCommandDeclaration, parentPath, templateNode, context -> {
            String Name = context.get("Name");
            if (!Name.endsWith("Cmd") && !Name.endsWith("Command")) {
                Name += "Cmd";
            }
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "Command", context.get("Name"), context);
            putContext(templateNode.getTag(), "Request", context.get("Command") + "Request", context);
            putContext(templateNode.getTag(), "Response", context.get("Command") + "Response", context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 命令描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成命令代码：" + path);
    }

    public void renderAppLayerSaga(String literalSagaDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析Saga设计：" + literalSagaDeclaration);
        String path = internalRenderGenericDesign(literalSagaDeclaration, parentPath, templateNode, context -> {
            String Name = context.get("Name");
            if (!Name.endsWith("Saga")) {
                Name += "Saga";
            }
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "Saga", context.get("Name"), context);
            putContext(templateNode.getTag(), "Request", context.get("Saga") + "Request", context);
            putContext(templateNode.getTag(), "Response", context.get("Saga") + "Response", context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: Saga描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成Saga代码：" + path);
    }

    /**
     * @param literalQueryDeclaration 文本化查询声明 QueryName
     * @param templateNode            模板配置
     */
    public void renderAppLayerQuery(String literalQueryDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析查询设计：" + literalQueryDeclaration);
        String path = internalRenderGenericDesign(literalQueryDeclaration, parentPath, templateNode, context -> {
            String Name = context.get("Name");
            if (!Name.endsWith("Qry") && !Name.endsWith("Query")) {
                Name += "Qry";
            }
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "Query", context.get("Name"), context);
            putContext(templateNode.getTag(), "Request", context.get("Query") + "Request", context);
            putContext(templateNode.getTag(), "Response", context.get("Query") + "Response", context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 查询描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成查询代码：" + path);
    }

    /**
     * @param literalClientDeclaration 文本化防腐端声明 ClientName
     * @param templateNode             模板配置
     */
    public void renderAppLayerClient(String literalClientDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析防腐端设计：" + literalClientDeclaration);
        String path = internalRenderGenericDesign(literalClientDeclaration, parentPath, templateNode, context -> {
            String Name = context.get("Name");
            if (!Name.endsWith("Cli") && !Name.endsWith("Client")) {
                Name += "Cli";
            }
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "Client", context.get("Name"), context);
            putContext(templateNode.getTag(), "Request", context.get("Name") + "Request", context);
            putContext(templateNode.getTag(), "Response", context.get("Name") + "Response", context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 防腐端描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成防腐端代码：" + path);
    }

    /**
     * @param internal                           是否内部集成
     * @param designType                         生成设计类型
     * @param literalIntegrationEventDeclaration 文本化集成事件声明 IntegrationEventName[:mq-topic[:mq-consumer]]
     * @param templateNode                       模板配置
     */
    public void renderAppLayerIntegrationEvent(boolean internal, String designType, String literalIntegrationEventDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析集成事件设计：" + literalIntegrationEventDeclaration);
        if (Objects.equals(designType, "integration_event")) {
            parentPath += File.separator + (internal ? "" : "external");
        }
        String path = internalRenderGenericDesign(literalIntegrationEventDeclaration, parentPath, templateNode, context -> {
            putContext(templateNode.getTag(), "subPackage", internal ? "" : ".external", context);
            String Name = context.get("Name");
            if (!Name.endsWith("Evt") && !Name.endsWith("Event")) {
                Name += "IntegrationEvent";
            }
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "IntegrationEvent", context.get("Name"), context);
            if (context.containsKey("Val1")) {
                putContext(templateNode.getTag(), "MQ_TOPIC", StringUtils.isBlank(context.get("Val1"))
                                ? ("\"" + context.get("Val0") + "\"")
                                : ("\"" + context.get("Val1") + "\""),
                        context
                );
            } else {
                putContext(templateNode.getTag(), "MQ_TOPIC", ("\"" + context.get("Val0") + "\""), context);
            }
            if (internal) {
                putContext(templateNode.getTag(), "MQ_CONSUMER", "IntegrationEvent.NONE_SUBSCRIBER", context);
                putContext(templateNode.getTag(), "Comment", context.containsKey("Val2") ? context.get("Val2") : "todo: 集成事件描述", context);
            } else {
                if (context.containsKey("Val2")) {
                    putContext(templateNode.getTag(), "MQ_CONSUMER", StringUtils.isBlank(context.get("Val2"))
                                    ? "\"${spring.application.name}\""
                                    : ("\"" + context.get("Val2") + "\""),
                            context
                    );
                } else {
                    putContext(templateNode.getTag(), "MQ_CONSUMER", "\"${spring.application.name}\"", context);
                }
                putContext(templateNode.getTag(), "Comment", context.containsKey("Val3") ? context.get("Val3") : "todo: 集成事件描述", context);
            }
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成集成事件代码：" + path);
    }

    /**
     * @param literalDomainEventDeclaration 文本化领域事件声明 AggregateRootEntityName:DomainEventName
     * @param templateNode                  模板配置
     */
    public void renderDomainLayerDomainEvent(String literalDomainEventDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析领域事件设计：" + literalDomainEventDeclaration);
        String path = internalRenderGenericDesign(literalDomainEventDeclaration, parentPath, templateNode, context -> {
            String reletivePath = NamingUtils.parentPackageName(context.get("Val0"))
                    .replace(".", File.separator);
            if (StringUtils.isNotBlank(reletivePath)) {
                putContext(templateNode.getTag(), "path", reletivePath, context);
                putContext(templateNode.getTag(), "package", StringUtils.isEmpty(reletivePath) ? "" : ("." + reletivePath.replace(File.separator, ".")), context);
            }
            if (!context.containsKey("Val1")) {
                throw new RuntimeException("缺失领域事件名称，领域事件设计格式：AggregateRootEntityName:DomainEventName");
            }
            String Name = NamingUtils.toUpperCamelCase(context.get("Val1"));
            if (!Name.endsWith("Evt") && !Name.endsWith("Event")) {
                Name += "DomainEvent";
            }
            String entity = NamingUtils.toUpperCamelCase(
                    NamingUtils.getLastPackageName(context.get("Val0"))
            );
            boolean persist = false;
            if (context.containsKey("val2") && "`true`persist`1`".contains("`" + context.get("val2") + "`")) {
                persist = true;
            }
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "DomainEvent", context.get("Name"), context);
            putContext(templateNode.getTag(), "persist", persist ? "true" : "false", context);
            putContext(templateNode.getTag(), "Aggregate", entity, context);
            putContext(templateNode.getTag(), "Entity", entity, context);
            putContext(templateNode.getTag(), "EntityVar", NamingUtils.toLowerCamelCase(entity), context);
            putContext(templateNode.getTag(), "AggregateRoot", context.get("Entity"), context);

            if (Objects.equals("domain_event_handler", alias4Design(templateNode.getTag()))) {
                putContext(templateNode.getTag(), "Comment", context.containsKey("Val2") ? context.get("Val2") : "todo: 领域事件订阅描述", context);
            } else {
                putContext(templateNode.getTag(), "Comment", context.containsKey("Val2") ? context.get("Val2") : "todo: 领域事件描述", context);
            }
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成领域事件代码：" + path);
    }

    /**
     * @param literalAggregateFactoryDeclaration 文本化聚合工厂声明 AggregateRootEntityName
     * @param templateNode                       模板配置
     */
    public void renderDomainLayerAggregateFactory(String literalAggregateFactoryDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析聚合工厂设计：" + literalAggregateFactoryDeclaration);
        String path = internalRenderGenericDesign(literalAggregateFactoryDeclaration, parentPath, templateNode, context -> {
            String entity = context.get("Name");
            String Name = entity + "Factory";
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "Factory", context.get("Name"), context);
            putContext(templateNode.getTag(), "Aggregate", entity, context);
            putContext(templateNode.getTag(), "Entity", entity, context);
            putContext(templateNode.getTag(), "EntityVar", NamingUtils.toLowerCamelCase(entity), context);
            putContext(templateNode.getTag(), "AggregateRoot", context.get("Entity"), context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 聚合工厂描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成聚合工厂代码：" + path);
    }

    /**
     * @param literalSpecificationDeclaration 文本化聚合工厂声明 AggregateRootEntityName
     * @param templateNode                    模板配置
     */
    public void renderDomainLayerSpecification(String literalSpecificationDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析实体规约设计：" + literalSpecificationDeclaration);
        String path = internalRenderGenericDesign(literalSpecificationDeclaration, parentPath, templateNode, context -> {
            String entity = context.get("Name");
            String Name = entity + "Specification";
            putContext(templateNode.getTag(), "Name", Name, context);
            putContext(templateNode.getTag(), "Specification", context.get("Name"), context);
            putContext(templateNode.getTag(), "Aggregate", entity, context);
            putContext(templateNode.getTag(), "Entity", entity, context);
            putContext(templateNode.getTag(), "EntityVar", NamingUtils.toLowerCamelCase(entity), context);
            putContext(templateNode.getTag(), "AggregateRoot", context.get("Entity"), context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 实体规约描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成实体规约代码：" + path);
    }

    /**
     * @param literalDomainServiceDeclaration 文本化领域服务声明 DomainServiceName
     * @param templateNode                    模板配置
     */
    public void renderDomainLayerDomainService(String literalDomainServiceDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析领域服务设计：" + literalDomainServiceDeclaration);
        String path = internalRenderGenericDesign(literalDomainServiceDeclaration, parentPath, templateNode, context -> {
            String name = generateDomainServiceName(context.get("Name"));


            putContext(templateNode.getTag(), "Name", name, context);
            putContext(templateNode.getTag(), "DomainService", context.get("Name"), context);

            putContext(templateNode.getTag(), "Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 领域服务描述", context);
            putContext(templateNode.getTag(), "CommentEscaped", context.get("Comment").replaceAll(PATTERN_LINE_BREAK, " "), context);
            return context;
        });
        getLog().info("生成领域服务代码：" + path);
    }

    /**
     * @param literalGenericDeclaration 文本化自定义元素声明 Val1[:Val2[:...]]
     * @param templateNode              模板配置
     */
    public void renderGenericDesign(String literalGenericDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析自定义元素设计：" + literalGenericDeclaration);
        String path = internalRenderGenericDesign(literalGenericDeclaration, parentPath, templateNode, null);
        getLog().info("生成自定义元素代码：" + path);
    }

    public String internalRenderGenericDesign(
            String literalGenericDeclaration,
            String parentPath,
            TemplateNode templateNode,
            Function<Map<String, String>, Map<String, String>> contextBuilder
    ) throws IOException {
        String[] segments = TextUtils.splitWithTrim(escape(literalGenericDeclaration), PATTERN_DESIGN_PARAMS_SPLITTER);
        for (int i = 0; i < segments.length; i++) {
            segments[i] = unescape(segments[i]);
        }
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            putContext(templateNode.getTag(), "Val" + i, segments[i], context);
            putContext(templateNode.getTag(), "val" + i, segments[i].toLowerCase(), context);
        }

        String name = segments[0].toLowerCase();
        String Name = NamingUtils.toUpperCamelCase(NamingUtils.getLastPackageName(segments[0]));
        String path = NamingUtils.parentPackageName(segments[0])
                .replace(".", File.separator);

        putContext(templateNode.getTag(), "Name", Name, context);
        putContext(templateNode.getTag(), "name", name, context);
        putContext(templateNode.getTag(), "path", path, context);
        putContext(templateNode.getTag(), "package", StringUtils.isEmpty(path) ? "" : ("." + path.replace(File.separator, ".")), context);
        if (null != contextBuilder) {
            context = contextBuilder.apply(context);
        }
        PathNode pathNode = templateNode.clone().resolve(context);
        return forceRender(pathNode, parentPath);
    }

}
