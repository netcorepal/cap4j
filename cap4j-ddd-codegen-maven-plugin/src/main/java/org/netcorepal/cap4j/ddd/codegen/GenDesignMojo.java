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
    public final String DESIGN_PARAMS_SPLITTER = "[\\:]";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
    }

    @Override
    protected void startRender() {
        try {
            render(template, projectDir, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建路径节点
     *
     * @param pathNode
     * @param parentPath
     * @throws IOException
     */
    @Override
    public String render(PathNode pathNode, String parentPath, boolean onlyRenderDir) throws IOException {
        String path = parentPath;
        switch (pathNode.getType()) {
            case "file":
                path = renderFile(pathNode, parentPath, onlyRenderDir);
                break;
            case "dir":
                path = renderDir(pathNode, parentPath);
                if (pathNode.getChildren() != null) {
                    for (PathNode childPathNode : pathNode.getChildren()) {
                        render(childPathNode, path, onlyRenderDir);
                    }
                }
                break;
            case "root":
                if (pathNode.getChildren() != null) {
                    for (PathNode childPathNode : pathNode.getChildren()) {
                        render(childPathNode, parentPath, onlyRenderDir);
                    }
                }
                break;
        }
        return path;
    }

    public String alias4Design(String name) {
        switch (name.toLowerCase()) {
            case "commands":
            case "command":
            case "cmd":
                return "command";
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
                .map(item -> TextUtils.splitWithTrim(item, DESIGN_PARAMS_SPLITTER, 2))
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
    public void renderDesign(List<TemplateNode> templateNodes, String parentPath) throws IOException {
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
                                renderAppLayerIntegrationEvent("integration_event", literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                case "integration_event_handler":
                    if (designMap.containsKey("integration_event_handler")) {
                        for (String literalDesign : designMap.get("integration_event_handler")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderAppLayerIntegrationEvent("integration_event_handler", literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    if (designMap.containsKey("integration_event")) {
                        for (String literalDesign : designMap.get("integration_event")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                if (literalDesign.split(DESIGN_PARAMS_SPLITTER).length >= 3) {
                                    renderAppLayerIntegrationEvent("integration_event_handler", literalDesign, parentPath, templateNode);
                                }
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
                    if (designMap.containsKey("domain_event_handler")) {
                        for (String literalDesign : designMap.get("domain_event_handler")) {
                            if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(literalDesign)) {
                                renderDomainLayerDomainEvent(literalDesign, parentPath, templateNode);
                            }
                        }
                    }
                    break;
                case "domain_event_handler":
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
                                renderDomainLayerSpecificaton(literalDesign, parentPath, templateNode);
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
            context.put("Name", Name);
            context.put("Command", context.get("Name"));
            context.put("command", context.get("Name").toLowerCase());
            context.put("Request", context.get("Command") + "Request");
            context.put("Response", context.get("Command") + "Response");

            context.put("ReturnType", context.get("Response"));

            context.put("Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 命令描述");
            context.put("comment", context.get("Comment"));
            return context;
        });
        getLog().info("生成命令代码：" + path);
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
            context.put("Name", Name);
            context.put("Query", context.get("Name"));
            context.put("query", context.get("Name").toLowerCase());
            context.put("Request", context.get("Query") + "Request");
            context.put("Response", context.get("Query") + "Response");

            context.put("ReturnType", context.get("Response"));

            context.put("Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 查询描述");
            context.put("comment", context.get("Comment"));
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
            context.put("Name", Name);
            context.put("Client", context.get("Name"));
            context.put("client", context.get("Name").toLowerCase());
            context.put("Request", context.get("Name") + "Request");
            context.put("Response", context.get("Name") + "Response");

            context.put("ReturnType", context.get("Response"));

            context.put("Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 防腐端描述");
            context.put("comment", context.get("Comment"));
            return context;
        });
        getLog().info("生成防腐端代码：" + path);
    }

    /**
     * @param literalType                        设计类型
     * @param literalIntegrationEventDeclaration 文本化集成事件声明 IntegrationEventName[:mq-topic[:mq-consumer]]
     * @param templateNode                       模板配置
     */
    public void renderAppLayerIntegrationEvent(String literalType, String literalIntegrationEventDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析集成事件设计：" + literalIntegrationEventDeclaration);
        String path = internalRenderGenericDesign(literalIntegrationEventDeclaration, parentPath, templateNode, context -> {
            String Name = context.get("Name");
            if (!Name.endsWith("Evt") && !Name.endsWith("Event")) {
                Name += "IntegrationEvent";
            }
            context.put("Name", Name);
            context.put("IntegrationEvent", context.get("Name"));
            context.put("Event", context.get("IntegrationEvent"));
            context.put("INTEGRATION_EVENT", context.get("IntegrationEvent"));
            context.put("IE", context.get("IntegrationEvent"));
            context.put("I_E", context.get("IntegrationEvent"));
            context.put("integration_event", context.get("Name").toLowerCase());
            context.put("event", context.get("integration_event"));
            context.put("ie", context.get("integration_event"));
            context.put("i_e", context.get("integration_event"));
            if (context.containsKey("Val1")) {
                context.put("MQ_TOPIC", StringUtils.isBlank(context.get("Val1"))
                        ? ("\"" + context.get("Val0") + "\"")
                        : ("\"" + context.get("Val1") + "\""));
            } else {
                context.put("MQ_TOPIC", ("\"" + context.get("Val0") + "\""));
            }
            if (Objects.equals(literalType, "integration_event")) {
                context.put("MQ_CONSUMER", "IntegrationEvent.NONE_SUBSCRIBER");
                context.put("Comment", context.containsKey("Val2") ? context.get("Val2") : "todo: 集成事件描述");
            } else {
                if (context.containsKey("Val2")) {
                    context.put("MQ_CONSUMER", StringUtils.isBlank(context.get("Val2"))
                            ? "\"${spring.application.name}\""
                            : ("\"" + context.get("Val2") + "\"")
                    );
                } else {
                    context.put("MQ_CONSUMER", "\"${spring.application.name}\"");
                }
                context.put("Comment", context.containsKey("Val3") ? context.get("Val3") : "todo: 集成事件描述");
            }
            context.put("comment", context.get("Comment"));
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
                context.put("path", reletivePath);
                context.put("package", StringUtils.isEmpty(reletivePath) ? "" : ("." + reletivePath.replace(File.separator, ".")));
            }
            if (!context.containsKey("Val1")) {
                throw new RuntimeException("缺失领域事件名称，领域事件设计格式：AggregateRootEntityName:DomainEventName");
            }
            String Name = NamingUtils.toUpperCamelCase(context.get("Val1"));
            if (!Name.endsWith("Evt") && !Name.endsWith("Event")) {
                Name += "DomainEvent";
            }
            context.put("Name", Name);
            context.put("DomainEvent", context.get("Name"));
            context.put("DOMAIN_EVENT", context.get("DomainEvent"));
            context.put("Event", context.get("DomainEvent"));
            String entity = NamingUtils.toUpperCamelCase(
                    NamingUtils.getLastPackageName(context.get("Val0"))
            );
            boolean persist = false;
            if (context.containsKey("val2") && "`true`persist`1`".contains("`" + context.get("val2") + "`")) {
                persist = true;
            }
            context.put("persist", persist ? "true" : "false");
            context.put("PERSIST", context.get("persist"));
            context.put("Entity", entity);
            context.put("ENTITY", context.get("Entity"));
            context.put("AggregateRoot", context.get("Entity"));
            context.put("AGGREGATE_ROOT", context.get("Entity"));
            context.put("aggregate_root", context.get("Entity"));
            context.put("Aggregate", context.get("package"));
            context.put("aggregate", context.get("package"));
            if (Objects.equals("domain_event_handler", alias4Design(templateNode.getTag()))) {
                context.put("Comment", context.containsKey("Val2") ? context.get("Val2") : "todo: 领域事件订阅描述");
            } else {
                context.put("Comment", context.containsKey("Val2") ? context.get("Val2") : "todo: 领域事件描述");
            }
            context.put("comment", context.get("Comment"));
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
            context.put("Name", Name);
            context.put("name", Name.toLowerCase());
            context.put("Entity", entity);
            context.put("entity", entity.toLowerCase());
            context.put("ENTITY", context.get("Entity"));
            context.put("AggregateRoot", context.get("Entity"));
            context.put("AGGREGATE_ROOT", context.get("Entity"));
            context.put("aggregate_root", context.get("Entity"));
            context.put("Aggregate", context.get("package"));
            context.put("aggregate", context.get("package"));
            context.put("Factory", context.get("Name"));
            context.put("FACTORY", context.get("Factory"));

            context.put("Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 聚合工厂描述");
            context.put("comment", context.get("Comment"));
            return context;
        });
        getLog().info("生成聚合工厂代码：" + path);
    }

    /**
     * @param literalSpecificationDeclaration 文本化聚合工厂声明 AggregateRootEntityName
     * @param templateNode                    模板配置
     */
    public void renderDomainLayerSpecificaton(String literalSpecificationDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析实体规约设计：" + literalSpecificationDeclaration);
        String path = internalRenderGenericDesign(literalSpecificationDeclaration, parentPath, templateNode, context -> {
            String entity = context.get("Name");
            String Name = entity + "Specification";
            context.put("Name", Name);
            context.put("name", Name.toLowerCase());
            context.put("Entity", entity);
            context.put("entity", entity.toLowerCase());
            context.put("ENTITY", context.get("Entity"));
            context.put("AggregateRoot", context.get("Entity"));
            context.put("AGGREGATE_ROOT", context.get("Entity"));
            context.put("aggregate_root", context.get("Entity"));
            context.put("Aggregate", context.get("package"));
            context.put("aggregate", context.get("package"));
            context.put("Specification", context.get("Name"));
            context.put("SPECIFICATION", context.get("Specification"));

            context.put("Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 实体规约描述");
            context.put("comment", context.get("Comment"));
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
            String name = context.get("Name");
            if (!name.endsWith("Svc") && !name.endsWith("Service")) {
                name += "DomainService";
            }

            context.put("Name", name);
            context.put("DomainService", context.get("Name"));
            context.put("DOMAIN_SERVICE", context.get("DomainService"));

            context.put("Comment", context.containsKey("Val1") ? context.get("Val1") : "todo: 领域服务描述");
            context.put("comment", context.get("Comment"));
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
        String[] segments = TextUtils.splitWithTrim(escape(literalGenericDeclaration), DESIGN_PARAMS_SPLITTER);
        for (int i = 0; i < segments.length; i++) {
            segments[i] = unescape(segments[i]);
        }
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }

        String name = segments[0].toLowerCase();
        String Name = NamingUtils.toUpperCamelCase(NamingUtils.getLastPackageName(segments[0]));
        String path = NamingUtils.parentPackageName(segments[0])
                .replace(".", File.separator);

        context.put("Name", Name);
        context.put("name", name);
        context.put("path", path);
        context.put("package", StringUtils.isEmpty(path) ? "" : ("." + path.replace(File.separator, ".")));
        if (null != contextBuilder) {
            context = contextBuilder.apply(context);
        }
        PathNode pathNode = templateNode.clone().resolve(context);
        return render(pathNode, parentPath, false);
    }

    public String escape(String content) {
        return content
                .replace("\\\\", "${symbol_escape}")
                .replace("\\:", "${symbol_colon}")
                .replace("\\,", "${symbol_comma}")
                .replace("\\;", "${symbol_semicolon}");
    }

    public String unescape(String escape) {
        return escape
                .replace("${symbol_escape}", "\\")
                .replace("${symbol_colon}", ":")
                .replace("${symbol_comma}", ",")
                .replace("${symbol_semicolon}", ";");
    }

}
