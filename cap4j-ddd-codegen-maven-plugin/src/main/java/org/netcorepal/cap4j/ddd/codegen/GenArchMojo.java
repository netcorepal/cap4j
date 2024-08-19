package org.netcorepal.cap4j.ddd.codegen;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;

import java.io.File;
import java.io.IOException;
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

    /**
     * 脚手架模板配置节点
     */
    @Data
    public static class PathNode {
        /**
         * 节点类型：root|dir|file
         */
        String type;
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
        String conflict = "warn";

        /**
         * 下级节点
         */
        List<PathNode> children;
    }

    private String projectGroupId = "";
    private String projectArtifactId = "";
    private String projectVersion = "";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String templateContent = "";
        try {
            if (null == archTemplate || archTemplate.isEmpty()) {
//                templateContent = SourceFileUtils.loadResourceFileContent("template.json");
                getLog().error("请设置archTemplate参数");
                return;
            } else {
                templateContent = SourceFileUtils.loadFileContent(archTemplate);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (basePackage == null || basePackage.isEmpty()) {
            getLog().warn("请设置basePackage参数");
            return;
        }
        MavenProject mavenProject = ((MavenProject) getPluginContext().get("project"));
        if (mavenProject != null) {
            projectGroupId = mavenProject.getGroupId();
            projectArtifactId = mavenProject.getArtifactId();
            projectVersion = mavenProject.getVersion();
        }
        PathNode arch = JSON.parseObject(templateContent, PathNode.class);

        // 项目结构解析
        String projectDir;
        projectDir = new File("").getAbsolutePath();
        getLog().info("项目目录：" + projectDir);

        render(arch, projectDir);
    }

    public void render(PathNode pathNode, String parentPath) {
        String path = parentPath + File.separator + pathNode.name;
        getLog().info("创建 " + escapePath(path));
        switch (pathNode.type) {
            case "file":
                renderFile(pathNode, parentPath);
                break;
            case "dir":
                renderDir(pathNode, parentPath);
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
    }

    public void renderDir(PathNode pathNode, String parentPath) {
        if (!"dir".equalsIgnoreCase(pathNode.type)) {
            throw new RuntimeException("节点类型必须是文件");
        }
        if (pathNode.name == null || pathNode.name.isEmpty()) {
            throw new RuntimeException("模板节点配置 name 不得为空 parentPath = " + parentPath);
        }
        String path = parentPath + File.separator + pathNode.name;
        path = escapePath(path);

        new File(path).mkdirs();
    }

    public void renderFile(PathNode pathNode, String parentPath) {
        if (!"file".equalsIgnoreCase(pathNode.type)) {
            throw new RuntimeException("节点类型必须是文件");
        }
        if (pathNode.name == null || pathNode.name.isEmpty()) {
            throw new RuntimeException("模板节点配置 name 不得为空 parentPath = " + parentPath);
        }
        String path = parentPath + File.separator + pathNode.name;
        path = escapePath(path);

        String content = "";
        switch (pathNode.format) {
            case "raw":
                content = pathNode.data;
                break;
            case "url":
                try {
                    content = SourceFileUtils.loadFileContent(pathNode.data);
                } catch (IOException ex) {
                    getLog().error("获取模板源文件异常", ex);
                }
                break;
        }
        content = escapeContent(content);
        if (FileUtils.fileExists(path)) {
            switch (pathNode.conflict) {
                case "warn":
                    getLog().warn("文件已存在：" + path);
                    return;
                case "overwrite":
                    getLog().info("文件将覆盖：" + path);
                    break;
                case "skip":
                default:
                    getLog().info("文件已存在：" + path);
                    return;
            }
        }

        try {
            FileUtils.fileDelete(path);
            FileUtils.fileWrite(path, "utf-8", content);
        } catch (IOException e) {
            getLog().error("写入模板文件异常", e);
        }
    }

    public String escapePath(String path) {
        path = path.replace("${basePackage}", basePackage.replace(".", File.separator));
        if (projectArtifactId != null && !projectArtifactId.isEmpty()) {
            path = path.replace("${artifactId}", projectArtifactId);
        }
        return path;
    }

    public String escapeContent(String content) {
        content = content.replace("${groupId}", projectGroupId);
        content = content.replace("${artifactId}", projectArtifactId);
        content = content.replace("${version}", projectVersion);
        content = content.replace("${archTemplate}", archTemplate);
        content = content.replace("${basePackage}", basePackage);
        content = content.replace("${multiModule}", multiModule ? "true" : "false");
        content = content.replace("${moduleNameSuffix4Adapter}", moduleNameSuffix4Adapter);
        content = content.replace("${moduleNameSuffix4Domain}", moduleNameSuffix4Domain);
        content = content.replace("${moduleNameSuffix4Application}", moduleNameSuffix4Application);
        content = content.replace("${connectionString}", connectionString);
        content = content.replace("${user}", user);
        content = content.replace("${pwd}", pwd);
        content = content.replace("${schema}", schema);
        content = content.replace("${table}", table);
        content = content.replace("${ignoreTable}", ignoreTable);
        content = content.replace("${idField}", idField);
        content = content.replace("${versionField}", versionField);
        content = content.replace("${deletedField}", deletedField);
        content = content.replace("${readonlyFields}", readonlyFields);
        content = content.replace("${ignoreFields}", ignoreFields);
        content = content.replace("${entityBaseClass}", entityBaseClass);
        content = content.replace("${entityMetaInfoClassOutputPackage}", entityMetaInfoClassOutputPackage);
        content = content.replace("${entityMetaInfoClassOutputMode}", entityMetaInfoClassOutputMode);
        content = content.replace("${idGenerator}", idGenerator);
        content = content.replace("${fetchType}", fetchType);
        content = content.replace("${fetchMode}", fetchMode);
        content = content.replace("${enumValueField}", enumValueField);
        content = content.replace("${enumNameField}", enumNameField);
        content = content.replace("${enumUnmatchedThrowException}", enumUnmatchedThrowException ? "true" : "false");
        content = content.replace("${datePackage4Java}", datePackage4Java);
        content = content.replace("${typeRemapping}", stringfyTypeRemapping());
        content = content.replace("${generateDefault}", generateDefault ? "true" : "false");
        content = content.replace("${generateDbType}", generateDbType ? "true" : "false");
        content = content.replace("${generateSchema}", generateSchema ? "true" : "false");
        content = content.replace("${generateBuild}", generateBuild ? "true" : "false");
        content = content.replace("${aggregateRootAnnotation}", aggregateRootAnnotation);
        content = content.replace("${aggregateRepositoryBaseClass}", aggregateRepositoryBaseClass);
        content = content.replace("${aggregateIdentityClass}", aggregateIdentityClass);
        content = content.replace("${aggregateRepositoryCustomerCode}", aggregateRepositoryCustomerCode);
        content = content.replace("${ignoreAggregateRoots}", ignoreAggregateRoots);
        content = content.replace("${symbol_pound}", "#");
        content = content.replace("${symbol_escape}", "\\");
        content = content.replace("${symbol_dollar}", "$");
        return content;
    }

    private String stringfyTypeRemapping() {
        if (typeRemapping == null || typeRemapping.isEmpty()) {
            return "";
        }
        String result = "";
        for (Map.Entry<String, String> kv :
                typeRemapping.entrySet()) {
            result += "<" + kv.getKey() + ">" + kv.getValue() + "</\"+kv.getKey()+\">";
        }
        return result;
    }
}
