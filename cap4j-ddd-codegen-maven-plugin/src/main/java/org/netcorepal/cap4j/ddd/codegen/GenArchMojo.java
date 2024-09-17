package org.netcorepal.cap4j.ddd.codegen;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;
import org.netcorepal.cap4j.ddd.codegen.template.PathNode;
import org.netcorepal.cap4j.ddd.codegen.template.Template;
import org.netcorepal.cap4j.ddd.codegen.template.TemplateNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成项目目录结构
 *
 * @author binking338
 * @date 2024/8/15
 */
@Mojo(name = "gen-arch")
public class GenArchMojo extends MyAbstractMojo {

    public final String PATTERN_SPLITTER = "[\\,\\;]";

    protected String projectGroupId = "";
    protected String projectArtifactId = "";
    protected String projectVersion = "";

    protected String projectDir = "";
    protected Template template = null;

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
        startRender();
    }

    protected void startRender(){
        try {
            render(template, projectDir, false);
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

    /**
     * @param pathNode
     * @param parentPath
     * @return
     * @throws IOException
     */
    public String renderDir(PathNode pathNode, String parentPath) throws IOException {
        if (!"dir".equalsIgnoreCase(pathNode.getType())) {
            throw new RuntimeException("节点类型必须是目录");
        }
        if (pathNode.getName() == null || pathNode.getName().isEmpty()) {
            return parentPath;
        }
        String path = parentPath + File.separator + pathNode.getName();
        if (FileUtils.fileExists(path)) {
            switch (pathNode.getConflict()) {
                case "warn":
                    getLog().warn("目录存在：" + path);
                    break;
                case "overwrite":
                    getLog().info("目录覆盖：" + path);
                    FileUtils.deleteDirectory(path);
                    FileUtils.mkdir(path);
                    break;
                case "skip":
//                    getLog().info("目录存在：" + path);
                    break;
            }
        } else {
            getLog().info("目录创建：" + path);
            FileUtils.mkdir(path);
        }

        if (StringUtils.isNotBlank(pathNode.getTag())) {
            String[] tags = pathNode.getTag().split(PATTERN_SPLITTER);
            for (String tag : tags) {
                List<TemplateNode> templateNodes = template.select(tag);
                if (null != templateNodes) {
                    renderDesign(templateNodes, path);
                }
            }
        }

        return path;
    }

    /**
     * 生成设计框架代码
     *
     * @param templateNodes
     * @param parentPath
     * @throws IOException
     */
    public void renderDesign(List<TemplateNode> templateNodes, String parentPath) throws IOException {

    }

    /**
     * @param pathNode
     * @param parentPath
     * @return
     */
    public String renderFile(PathNode pathNode, String parentPath, boolean onlyRenderDir) throws IOException {
        if (!"file".equalsIgnoreCase(pathNode.getType())) {
            throw new RuntimeException("节点类型必须是文件");
        }
        if (pathNode.getName() == null || pathNode.getName().isEmpty()) {
            throw new RuntimeException("模板节点配置 name 不得为空 parentPath = " + parentPath);
        }
        String path = parentPath + File.separator + pathNode.getName();
        if (onlyRenderDir) return path;

        String content = pathNode.getData();
        if (FileUtils.fileExists(path)) {
            switch (pathNode.getConflict()) {
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
//                    getLog().info("文件存在：" + path);
                    break;
            }
        } else {
            getLog().info("文件创建：" + path);
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
        context.put("designFile", designFile);
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
        context.put("aggregateRepositoryCustomerCode", aggregateRepositoryCustomerCode);
        context.put("ignoreAggregateRoots", ignoreAggregateRoots);
        context.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        context.put("SEPARATOR", File.separator);
        context.put("separator", File.separator);
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
