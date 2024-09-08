package org.netcorepal.cap4j.ddd.codegen;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.TextUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成项目目录结构
 *
 * @author binking338
 * @date 2024/8/15
 */
@Mojo(name = "gen-arch")
public class GenArchMojo extends MyAbstractMojo {

    public final String PATTERN_SPLITTER = "[\\,\\;]";

    /**
     * 脚手架模板文件节点
     */
    @Data
    public static class PathNode {
        /**
         * 节点类型：root|dir|file
         */
        String type;
        /**
         * 节点标签：关联模板
         */
        String tag;
        /**
         * 节点名称
         */
        String name;
        /**
         * 模板源类型：raw|url
         */
        String format = "raw";
        /**
         * 模板数据数据
         */
        String data;
        /**
         * 冲突处理：skip|warn|overwrite
         */
        String conflict = "skip";

        /**
         * 下级节点
         */
        List<PathNode> children;

        public PathNode clone() {
            PathNode pathNode = JSON.parseObject(JSON.toJSONString(this), PathNode.class);
            return pathNode;
        }

        public PathNode resolve(Map<String, String> context) throws IOException {
            if (null != this.name) {
                this.name = this.name.replace("${basePackage}", "${basePackage__as_path}");
                this.name = escape(this.name, context);
            }
            String rawData = "";
            switch (this.format) {
                case "url":
                    if (null != this.data) {
                        rawData = SourceFileUtils.loadFileContent(this.data, context.get("archTemplateEncoding"));
                    }
                    break;
                case "raw":
                default:
                    rawData = this.data;
                    break;
            }
            this.data = escape(rawData, context);
            this.format = "raw";
            if (null != this.children) {
                for (PathNode child : this.children) {
                    child.resolve(context);
                }
            }
            return this;
        }

        protected String escape(String content, Map<String, String> context) {
            if (null == content) {
                return "";
            }
            String result = content;
            for (Map.Entry<String, String> kv : context.entrySet()) {
                result = result.replace("${" + kv.getKey() + "}", kv.getValue() == null ? "" : kv.getValue());
            }
            return result;
        }
    }

    /**
     * 脚手架模板模板节点
     */
    public static class TemplateNode extends PathNode {

        public TemplateNode clone() {
            TemplateNode templateNode = JSON.parseObject(JSON.toJSONString(this), TemplateNode.class);
            return templateNode;
        }

        @Override
        public PathNode resolve(Map<String, String> context) throws IOException {
            super.resolve(context);
            this.tag = "";
            return this;
        }
    }

    /**
     * 模板
     */
    @Data
    @NoArgsConstructor
    public static class Template extends PathNode {

        /**
         * 模板节点
         */
        List<TemplateNode> templates = null;

        /**
         * 获取模板
         *
         * @param tag
         * @return
         */
        public TemplateNode select(String tag) {
            if (this.templates == null) return null;
            Optional<TemplateNode> node = templates.stream().filter(t -> Objects.equals(t.tag, tag)).findFirst();
            return node.orElse(null);
        }
    }

    private String projectGroupId = "";
    private String projectArtifactId = "";
    private String projectVersion = "";

    private String projectDir = "";
    private Template template = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("当前系统默认编码：" + Charset.defaultCharset().name());
        getLog().info("设置模板读取编码：" + archTemplateEncoding + " (from archTemplateEncoding)");

        String templateContent = "";
        try {
            if (null == archTemplate || archTemplate.isEmpty()) {
//                templateContent = SourceFileUtils.loadResourceFileContent("template.json", archTemplateEncoding);
                getLog().error("请设置(archTemplate)参数");
                return;
            } else {
                templateContent = SourceFileUtils.loadFileContent(archTemplate, archTemplateEncoding);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLog().debug(templateContent);
        if (basePackage == null || basePackage.isEmpty()) {
            getLog().warn("请设置(basePackage)参数");
            return;
        }
        MavenProject mavenProject = ((MavenProject) getPluginContext().get("project"));
        if (mavenProject != null) {
            projectGroupId = mavenProject.getGroupId();
            projectArtifactId = mavenProject.getArtifactId();
            projectVersion = mavenProject.getVersion();
        }

        // 项目结构解析
        projectDir = new File("").getAbsolutePath();
        getLog().info("项目目录：" + projectDir);

        template = JSON.parseObject(templateContent, Template.class);
        try {
            template.resolve(getEscapeContext());
        } catch (IOException e) {
            getLog().error("模板文件加载失败！");
        }
        try {
            render(template, projectDir);
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
    public String render(PathNode pathNode, String parentPath) throws IOException {
        String path = parentPath;
        switch (pathNode.type) {
            case "file":
                path = renderFile(pathNode, parentPath);
                break;
            case "dir":
                path = renderDir(pathNode, parentPath);
                if (pathNode.children != null) {
                    for (PathNode childPathNode : pathNode.children) {
                        render(childPathNode, path);
                    }
                }
                break;
            case "root":
                if (pathNode.children != null) {
                    for (PathNode childPathNode : pathNode.children) {
                        render(childPathNode, parentPath);
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
                return "integration_event_handler";
            case "domain_events":
            case "domain_event":
            case "d_e":
            case "de":
                return "domain_event";
            case "domain_event_handlers":
            case "domain_event_handler":
            case "d_e_h":
            case "deh":
                return "domain_event_handler";
            case "domain_service":
            case "service":
            case "svc":
                return "domain_service";
            default:
                return name;
        }
    }

    /**
     * 构建模型设计元素
     *
     * @param templateNode
     * @param parentPath
     * @throws IOException
     */
    public void renderDesign(TemplateNode templateNode, String parentPath) throws IOException {
        Map<String, Set<String>> designMap = Arrays.stream(this.design.split(PATTERN_SPLITTER))
                .map(item -> TextUtils.splitWithTrim(item, ":", 2))
                .filter(item -> item.length == 2)
                .collect(Collectors.groupingBy(g -> alias4Design(g[0]), Collectors.mapping(g -> g[1].trim(), Collectors.toSet())));
        switch (alias4Design(templateNode.tag)) {
            case "command":
                if (designMap.containsKey("command")) {
                    for (String literalCommand :
                            designMap.get("command")) {
                        renderAppLayerCommand(literalCommand, parentPath, templateNode);
                    }
                }
                break;
            case "query":
            case "query_handler":
                if (designMap.containsKey("query")) {
                    for (String literalCommand :
                            designMap.get("query")) {
                        renderAppLayerQuery(literalCommand, parentPath, templateNode);
                    }
                }
                break;
            case "client":
            case "client_handler":
                if (designMap.containsKey("client")) {
                    for (String literalCommand :
                            designMap.get("client")) {
                        renderAppLayerClient(literalCommand, parentPath, templateNode);
                    }
                }
                break;
            case "integration_event":
                if (designMap.containsKey("integration_event")) {
                    for (String literalCommand :
                            designMap.get("integration_event")) {
                        renderAppLayerIntegrationEvent(literalCommand, parentPath, templateNode);
                    }
                }
            case "integration_event_handler":
                if (designMap.containsKey("integration_event_handler")) {
                    for (String literalCommand :
                            designMap.get("integration_event_handler")) {
                        renderAppLayerIntegrationEvent(literalCommand, parentPath, templateNode);
                    }
                }
                break;
            case "domain_event":
            case "domain_event_handler":
                if (designMap.containsKey("domain_event")) {
                    for (String literalCommand :
                            designMap.get("domain_event")) {
                        renderDomainLayerDomainEvent(literalCommand, parentPath, templateNode);
                    }
                }
                break;
            case "domain_service":
                if (designMap.containsKey("domain_service")) {
                    for (String literalCommand :
                            designMap.get("domain_service")) {
                        renderDomainLayerDomainService(literalCommand, parentPath, templateNode);
                    }
                }
                break;
            default:
                if (designMap.containsKey(templateNode.tag)) {
                    for (String literalCommand :
                            designMap.get(templateNode.tag)) {
                        renderGenericDesign(literalCommand, parentPath, templateNode);
                    }
                }
                break;
        }
    }

    /**
     * @param literalCommandDeclaration 文本化命令声明 CommandName[:ResponseType_default_is_Boolean]
     * @param templateNode              模板配置
     */
    public void renderAppLayerCommand(String literalCommandDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析命令设计：" + literalCommandDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalCommandDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        if (!name.endsWith("Cmd") && !name.endsWith("Command")) {
            name += "Cmd";
        }
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        context.put("ReturnType", segments.length > 1 ? segments[1] : "Boolean");
        context.put("Command", context.get("Name"));
        context.put("Request", context.get("Name"));
        context.put("command", context.get("name"));
        context.put("request", context.get("name"));
        context.put("Response", context.get("ReturnType"));
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成命令代码：" + path);
    }

    /**
     * @param literalQueryDeclaration 文本化查询声明 QueryName[:ResponseType_default_is_QueryNameResponse]
     * @param templateNode            模板配置
     */
    public void renderAppLayerQuery(String literalQueryDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析查询设计：" + literalQueryDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalQueryDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        if (!name.endsWith("Qry") && !name.endsWith("Query")) {
            name += "Qry";
        }
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        context.put("ReturnType", segments.length > 1 ? segments[1] : (segments[0] + "Response"));
        context.put("Query", context.get("Name"));
        context.put("Request", context.get("Name"));
        context.put("query", context.get("name"));
        context.put("request", context.get("name"));
        context.put("Response", context.get("ReturnType"));
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成查询代码：" + path);
    }

    /**
     * @param literalClientDeclaration 文本化防腐端声明 ClientName[:ResponseType_default_is_ClientNameResponse]
     * @param templateNode             模板配置
     */
    public void renderAppLayerClient(String literalClientDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析防腐端设计：" + literalClientDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalClientDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        if (!name.endsWith("Cli") && !name.endsWith("Client")) {
            name += "Client";
        }
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        context.put("ReturnType", NamingUtils.toUpperCamelCase(segments.length > 1 ? segments[1] : (segments[0] + "Response")));
        context.put("Client", context.get("Name"));
        context.put("Request", context.get("Name"));
        context.put("client", context.get("name"));
        context.put("request", context.get("name"));
        context.put("Response", context.get("ReturnType"));
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成防腐端代码：" + path);
    }

    /**
     * @param literalIntegrationEventDeclaration 文本化集成事件声明 IntegrationEventName[:mq-topic[:mq-consumer]]
     * @param templateNode                       模板配置
     */
    public void renderAppLayerIntegrationEvent(String literalIntegrationEventDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析集成事件设计：" + literalIntegrationEventDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalIntegrationEventDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        if (!name.endsWith("Evt") && !name.endsWith("Event")) {
            name += "IntegrationEvent";
        }
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        context.put("MQ_TOPIC", segments.length > 1 ? segments[1] : segments[0]);
        if (Objects.equals("event_handler", templateNode.tag)) {
            context.put("MQ_CONSUMER", segments.length > 2 ? segments[2] : "${spring.application.name}");
        } else {
            context.put("MQ_CONSUMER", "[none]");
        }
        context.put("IntegrationEvent", context.get("Name"));
        context.put("Event", context.get("Name"));
        context.put("integration_event", context.get("name"));
        context.put("event", context.get("name"));
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成集成事件代码：" + path);
    }

    /**
     * @param literalDomainEventDeclaration 文本化领域事件声明 Val1[:Val2[:...]]
     * @param templateNode                  模板配置
     */
    public void renderDomainLayerDomainEvent(String literalDomainEventDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析领域事件设计：" + literalDomainEventDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalDomainEventDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        if (!name.endsWith("Evt") && !name.endsWith("Event")) {
            name += "DomainEvent";
        }
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        context.put("DomainEvent", context.get("Name"));
        context.put("Event", context.get("Name"));
        context.put("domain_event", context.get("name"));
        context.put("event", context.get("name"));
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成领域事件代码：" + path);
    }


    /**
     * @param literalDomainServiceDeclaration 文本化领域服务声明 DomainServiceName
     * @param templateNode                    模板配置
     */
    public void renderDomainLayerDomainService(String literalDomainServiceDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析领域服务设计：" + literalDomainServiceDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalDomainServiceDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        if (!name.endsWith("Svc") && !name.endsWith("Service")) {
            name += "DomainService";
        }
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        context.put("DomainService", context.get("Name"));
        context.put("domain_service", context.get("name"));
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成领域服务代码：" + path);
    }

    /**
     * @param literalGenericDeclaration 文本化自定义元素声明 Val1[:Val2[:...]]
     * @param templateNode              模板配置
     */
    public void renderGenericDesign(String literalGenericDeclaration, String parentPath, TemplateNode templateNode) throws IOException {
        getLog().info("解析自定义元素设计：" + literalGenericDeclaration);
        String[] segments = TextUtils.splitWithTrim(literalGenericDeclaration, ":");
        Map<String, String> context = new HashMap<>(getEscapeContext());
        for (int i = 0; i < segments.length; i++) {
            context.put("Val" + i, segments[i]);
            context.put("val" + i, segments[i].toLowerCase());
        }
        String name = NamingUtils.toUpperCamelCase(segments[0]);
        context.put("Name", name);
        context.put("name", segments[0].toLowerCase());
        PathNode pathNode = templateNode.clone().resolve(context);
        String path = render(pathNode, parentPath);
        getLog().info("生成自定义元素代码：" + path);
    }

    /**
     * @param pathNode
     * @param parentPath
     * @return
     * @throws IOException
     */
    public String renderDir(PathNode pathNode, String parentPath) throws IOException {
        if (!"dir".equalsIgnoreCase(pathNode.type)) {
            throw new RuntimeException("节点类型必须是目录");
        }
        if (pathNode.name == null || pathNode.name.isEmpty()) {
            return parentPath;
        }
        String path = parentPath + File.separator + pathNode.name;
        if (FileUtils.fileExists(path)) {
            switch (pathNode.conflict) {
                case "warn":
                    getLog().warn("目录存在：" + path);
                    break;
                case "overwrite":
                    getLog().info("目录覆盖：" + path);
                    FileUtils.deleteDirectory(path);
                    FileUtils.mkdir(path);
                    break;
                case "skip":
                    getLog().info("目录存在：" + path);
                    break;
            }
        } else {
            getLog().warn("目录创建：" + path);
            FileUtils.mkdir(path);
        }

        if (StringUtils.isNotBlank(pathNode.tag)) {
            TemplateNode templateNode = template.select(pathNode.tag);
            if (null != templateNode) {
                renderDesign(templateNode, path);
            }
        }

        return path;
    }

    /**
     * @param pathNode
     * @param parentPath
     * @return
     */
    public String renderFile(PathNode pathNode, String parentPath) throws IOException {
        if (!"file".equalsIgnoreCase(pathNode.type)) {
            throw new RuntimeException("节点类型必须是文件");
        }
        if (pathNode.name == null || pathNode.name.isEmpty()) {
            throw new RuntimeException("模板节点配置 name 不得为空 parentPath = " + parentPath);
        }
        String path = parentPath + File.separator + pathNode.name;

        String content = pathNode.data;
        if (FileUtils.fileExists(path)) {
            switch (pathNode.conflict) {
                case "warn":
                    getLog().warn("文件存在：" + path);
                    break;
                case "overwrite":
                    getLog().info("文件覆盖：" + path);
                    FileUtils.fileDelete(path);
                    FileUtils.fileWrite(path, content);
                    break;
                case "skip":
                default:
                    getLog().info("文件存在：" + path);
                    break;
            }
        } else {
            getLog().warn("文件创建：" + path);
            FileUtils.fileWrite(path, content);
        }
        return path;
    }

    public Map<String, String> getEscapeContext() {
        Map<String, String> context = new HashMap<>();
        context.put("groupId", projectGroupId);
        context.put("artifactId", projectArtifactId);
        context.put("version", projectVersion);
        context.put("archTemplate", archTemplate);
        context.put("archTemplateEncoding", archTemplateEncoding);
        context.put("basePackage", basePackage);
        context.put("basePackage__as_path", basePackage.replace(".", File.separator));
        context.put("multiModule", multiModule ? "true" : "false");
        context.put("moduleNameSuffix4Adapter", moduleNameSuffix4Adapter);
        context.put("moduleNameSuffix4Domain", moduleNameSuffix4Domain);
        context.put("moduleNameSuffix4Application", moduleNameSuffix4Application);
        context.put("connectionString", connectionString);
        context.put("user", user);
        context.put("pwd", pwd);
        context.put("schema", schema);
        context.put("table", table);
        context.put("ignoreTable", ignoreTable);
        context.put("idField", idField);
        context.put("versionField", versionField);
        context.put("deletedField", deletedField);
        context.put("readonlyFields", readonlyFields);
        context.put("ignoreFields", ignoreFields);
        context.put("entityBaseClass", entityBaseClass);
        context.put("entityClassExtraImports", stringfyEntityClassImportPackages());
        context.put("entityMetaInfoClassOutputPackage", entityMetaInfoClassOutputPackage);
        context.put("entityMetaInfoClassOutputMode", entityMetaInfoClassOutputMode);
        context.put("idGenerator", idGenerator);
        context.put("fetchType", fetchType);
        context.put("fetchMode", fetchMode);
        context.put("enumValueField", enumValueField);
        context.put("enumNameField", enumNameField);
        context.put("enumUnmatchedThrowException", enumUnmatchedThrowException ? "true" : "false");
        context.put("datePackage4Java", datePackage4Java);
        context.put("typeRemapping", stringfyTypeRemapping());
        context.put("generateDefault", generateDefault ? "true" : "false");
        context.put("generateDbType", generateDbType ? "true" : "false");
        context.put("generateSchema", generateSchema ? "true" : "false");
        context.put("generateBuild", generateBuild ? "true" : "false");
        context.put("aggregateRootAnnotation", aggregateRootAnnotation);
        context.put("aggregateRepositoryBaseClass", aggregateRepositoryBaseClass);
        context.put("aggregateIdentityClass", aggregateIdentityClass);
        context.put("aggregateRepositoryCustomerCode", aggregateRepositoryCustomerCode);
        context.put("ignoreAggregateRoots", ignoreAggregateRoots);
        context.put("symbol_pound", "#");
        context.put("symbol_escape", "\\");
        context.put("symbol_dollar", "$");
        return context;
    }

    private String stringfyTypeRemapping() {
        if (typeRemapping == null || typeRemapping.isEmpty()) {
            return "";
        }
        String result = "";
        for (Map.Entry<String, String> kv :
                typeRemapping.entrySet()) {
            result += "<" + kv.getKey() + ">" + kv.getValue() + "</" + kv.getKey() + ">";
        }
        return result;
    }

    private String stringfyEntityClassImportPackages() {
        if (entityClassExtraImports == null || entityClassExtraImports.isEmpty()) {
            return "";
        }
        String result = "\n";
        for (String entityClassExtraImport : entityClassExtraImports) {
            result += "                        <import>" + entityClassExtraImport + "</import>\n";
        }
        result += "                    ";
        return result;
    }
}
