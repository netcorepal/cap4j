package org.netcorepal.cap4j.ddd.codegen;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;
import org.netcorepal.cap4j.ddd.codegen.template.PathNode;
import org.netcorepal.cap4j.ddd.codegen.template.TemplateNode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.codegen.misc.NamingUtils.toSnakeCase;
import static org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils.*;

/**
 * 生成仓储
 *
 * @author binking338
 * @date 2022-02-14
 */
@Mojo(name = "gen-repository")
public class GenRepositoryMojo extends GenArchMojo {
    Map<String, String> AggregateRoot2AggregateNameMap = new HashMap<>();
    boolean hasRepositoryTemplate = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (!hasRepositoryTemplate) {
            String repositoriesDir = resolveDirectory(getAdapterModulePath(), basePackage + "." + AGGREGATE_REPOSITORY_PACKAGE);
            List<TemplateNode> repositoryTemplateNodes = template.select("repository");
            if(null == repositoryTemplateNodes || repositoryTemplateNodes.isEmpty()){
                repositoryTemplateNodes = Arrays.asList(getDefaultRepositoryTemplate());
            }
            try {
                renderTemplate(repositoryTemplateNodes, repositoriesDir);
            } catch (IOException e) {
                getLog().error("模板文件写入失败！", e);
            }
        }
    }

    public TemplateNode getDefaultRepositoryTemplate() {
        String template = "package ${basePackage}." + AGGREGATE_REPOSITORY_PACKAGE + ";\n" +
                "\n" +
                "import ${EntityPackage}.${Entity};\n" +
                "import org.netcorepal.cap4j.ddd.domain.aggregate.annotation.Aggregate;\n" +
                "import org.netcorepal.cap4j.ddd.domain.repo.AbstractJpaRepository;\n" +
                "import org.netcorepal.cap4j.ddd.domain.repo.AggregateRepository;\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.data.jpa.repository.JpaSpecificationExecutor;\n" +
                "import org.springframework.stereotype.Component;" +
                "\n" +
                "/**\n" +
                " * 本文件由[cap4j-ddd-codegen-maven-plugin]生成\n" +
                " * @author cap4j-ddd-codegen\n" +
                " * @date ${date}\n" +
                " */\n" +
                "public interface ${Entity}Repository extends AggregateRepository<${Entity}, ${Identity}> {\n" +
                "\n" +
                "    @Component\n" +
                "    @Aggregate(aggregate = \"${Aggregate}\", name = \"${Entity}\", type = Aggregate.TYPE_REPOSITORY, description = \"\")\n" +
                "    public static class ${Entity}JpaRepositoryAdapter extends AbstractJpaRepository<${Entity}, ${IdentityType}>\n" +
                "    {\n" +
                "        public ${Entity}JpaRepositoryAdapter(JpaSpecificationExecutor<${Entity}> jpaSpecificationExecutor, JpaRepository<${Entity}, ${IdentityType}> jpaRepository) {\n" +
                "            super(jpaSpecificationExecutor, jpaRepository);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "}\n";
        TemplateNode templateNode = new TemplateNode();
        templateNode.setType("file");
        templateNode.setTag("repository");
        templateNode.setName("${Entity}Repository.java");
        templateNode.setFormat("raw");
        templateNode.setData(template);
        templateNode.setConflict("skip");
        return templateNode;
    }

    @Override
    public void renderTemplate(List<TemplateNode> templateNodes, String parentPath) throws IOException {
        for (TemplateNode templateNode : templateNodes) {
            if ("repository".equals(templateNode.getTag())) {
                hasRepositoryTemplate = true;
                getLog().info("开始生成仓储代码");
                getLog().info("聚合根标注注解：" + getAggregateRootAnnotation());
                getLog().info("聚合根基类：" + getAggregateRepositoryBaseClass());
                getLog().info("跳过生成仓储的聚合根：" + ignoreAggregateRoots);
                getLog().info("");

                List<File> files = SourceFileUtils.loadFiles(
                        getDomainModulePath()
                );
                files = files.stream()
                        .filter(file -> "java".equalsIgnoreCase(FileUtils.extension(file.getName())))
                        .collect(Collectors.toList());
                files.forEach(file -> {
                    this.getLog().debug("发现Java文件: " + resolveClassName(file.getAbsolutePath()));
                });
                getLog().info("发现java文件数量：" + files.size());
                files.forEach(file -> {
                    String fullClassName = resolveClassName(file.getAbsolutePath());
                    this.getLog().debug("解析Java文件: " + fullClassName);
                    String content = "";
                    try {
                        content = FileUtils.fileRead(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    boolean isAggregateRoot = isAggregateRoot(content, fullClassName);
                    if (isAggregateRoot) {
                        this.getLog().info("发现聚合根: " + fullClassName);

                        String simpleClassName = resolveSimpleClassName(file.getAbsolutePath());

                        String identityClass = getIdentityType(content);
                        this.getLog().info("聚合根ID类型: " + identityClass);
                        if (Arrays.stream(ignoreAggregateRoots.split("[\\,\\;]"))
                                .anyMatch(i -> i.equalsIgnoreCase(simpleClassName))) {
                            return;
                        }
                        if (StringUtils.isBlank(templateNode.getPattern()) || Pattern.compile(templateNode.getPattern()).asPredicate().test(fullClassName)) {
                            try {
                                Map<String, String> context = getEscapeContext();
                                context.put("EntityPackage", resolvePackage(file.getAbsolutePath()));
                                context.put("EntityType", simpleClassName);
                                context.put("Entity", simpleClassName);
                                context.put("IdentityClass", identityClass);
                                context.put("IdentityType", identityClass);
                                context.put("Identity", identityClass);
                                context.put("Aggregate", AggregateRoot2AggregateNameMap.containsKey(simpleClassName)
                                        ? AggregateRoot2AggregateNameMap.get(simpleClassName)
                                        : toSnakeCase(simpleClassName));
                                PathNode pathNode = templateNode.clone().resolve(context);
                                forceRender(pathNode, parentPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                this.getLog().info("结束生成仓储代码");
            }
        }
    }


    private boolean isAggregateRoot(String content, String className) {
        return Arrays.stream(content.split("(\r)|(\n)|(\r\n)"))
                .filter(line -> line.trim().startsWith("@") || line.replace("\\s", "").equals("/*@AggregateRoot*/"))
                .anyMatch(line -> {
                    boolean hasAggregateRoot = false;
                    if (StringUtils.isBlank(getAggregateRootAnnotation())) {
                        // 注解风格 @AggregateRoot()
                        boolean oldAggregateRoot1 = line.matches("@AggregateRoot(\\(.*\\))?");
                        // 注释风格 /* @AggregateRoot */
                        boolean oldAggregateRoot2 = line.matches("^\\s*\\/\\*\\s*@AggregateRoot\\s*\\*\\/\\s*$");
                        hasAggregateRoot = oldAggregateRoot1 || oldAggregateRoot2;
                    } else {
                        // 注解风格 @AggregateRoot()
                        boolean isAggregateRootAnnotation = line.matches("@AggregateRoot(\\(.*\\))?");
                        // 注解风格 配置
                        boolean isAggregateRootAnnotationFullName = line.matches(getAggregateRootAnnotation() + "(\\(.*\\))?");
                        hasAggregateRoot = (isAggregateRootAnnotationFullName || isAggregateRootAnnotation);
                    }
                    if (hasAggregateRoot) {
                        return hasAggregateRoot;
                    }

                    boolean aggregateRoot =
                            line.matches("@Aggregate\\s*\\(.*root\\s*=\\s*true.*\\)");
                    if (aggregateRoot) {
                        Pattern AGGREGATE_PATTERN = Pattern.compile("\\s*aggregate\\s*=\\s*\\\"([^\\\"]*)\\\"");
                        Matcher matcher = AGGREGATE_PATTERN.matcher(line);
                        if (matcher.find() && matcher.groupCount() == 1) {
                            AggregateRoot2AggregateNameMap.put(className, matcher.group(1));
                        }
                    }

                    getLog().debug("annotationline: " + line);
                    getLog().debug("hasAggregateRoot=" + hasAggregateRoot);
                    getLog().debug("aggregateRoot=" + aggregateRoot);
                    return aggregateRoot;
                });
    }

    private String getIdentityType(String content) {
        Pattern ID_FIELD_PATTERN = Pattern.compile("^\\s*([_A-Za-z][_A-Za-z0-9]*)\\s*" + idField + "\\s*;$");
        String idFieldLine = Arrays.stream(content.split("(\r)|(\n)|(\r\n)"))
                .filter(ID_FIELD_PATTERN.asPredicate())
                .findFirst()
                .orElse(null);
        if (null != idFieldLine) {
            Matcher matcher = ID_FIELD_PATTERN.matcher(idFieldLine);
            if (matcher.find() && matcher.groupCount() == 1) {
                return matcher.group(1);
            }
        }
        return "Long";
    }

    /**
     * 返回能否重新生成实体
     *
     * @param filePath
     * @param importLines
     * @param annotationLines
     * @param customerLines
     * @return
     * @throws IOException
     */
    public boolean readCustomerSourceFile(String filePath, List<String> importLines, List<String> annotationLines, List<String> customerLines) throws IOException {
        if (FileUtils.fileExists(filePath + ".remove")) {
            FileUtils.rename(new File(filePath + ".remove"), new File(filePath));
        }
        if (FileUtils.fileExists(filePath)) {
            String content = FileUtils.fileRead(filePath);
            List<String> lines = Arrays.asList(content.replace("\r\n", "\n").split("\n"));
            int startLine = 0;
            int endLine = 0;
            int startClassLine = 0;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                // 取第一个public
                if (line.trim().startsWith("public") && startClassLine == 0) {
                    startClassLine = i;
                    startLine = i;
                } else if ((line.trim().startsWith("@") || annotationLines.size() > 0) && startClassLine == 0) {
                    annotationLines.add(line);
                    getLog().debug("a " + line);
                } else if ((annotationLines.size() == 0 && startClassLine == 0)) {
                    importLines.add(line);
                    getLog().debug("i " + line);
                } else if (startLine > 0 && endLine == 0) {
                    customerLines.add(line);
                }
            }

            for (int i = customerLines.size() - 1; i >= 0; i--) {
                String line = customerLines.get(i);
                if (line.contains("}")) {
                    customerLines.remove(i);
                    if (!line.equalsIgnoreCase("}")) {
                        customerLines.add(i, line.substring(0, line.lastIndexOf("}")));
                    }
                    break;
                }
                customerLines.remove(i);
            }
            customerLines.forEach(l -> getLog().debug("c " + l));
            if (startLine + 1 >= endLine) {
                return false;
            }
            FileUtils.removePath(filePath);
        }
        return true;
    }

    public void renameAggregateRepositorySourceFiles(String basePackage, String baseDir) {
        String packageName = basePackage + "." + AGGREGATE_REPOSITORY_PACKAGE;

        List<File> files = SourceFileUtils.loadFiles(SourceFileUtils.resolveDirectory(baseDir, packageName));
        for (File file : files) {
            if (file.getAbsolutePath().endsWith(".java")) {
                try {
                    FileUtils.rename(file, new File(file.getAbsolutePath() + ".remove"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
