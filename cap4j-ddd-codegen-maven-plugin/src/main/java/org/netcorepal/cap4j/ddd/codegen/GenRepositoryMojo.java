package org.netcorepal.cap4j.ddd.codegen;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils.writeLine;

/**
 * 生成仓储
 *
 * @author binking338
 * @date 2022-02-14
 */
@Mojo(name = "gen-repository")
public class GenRepositoryMojo extends MyAbstractMojo {

    Map<String, String> AggregateRoot2AggregateNameMap = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("当前默认编码：" + Charset.defaultCharset().name());
        getLog().info("聚合根标注注解：" + getAggregateRootAnnotation());
        getLog().info("聚合根基类：" + getAggregateRepositoryBaseClass());
        getLog().info("跳过生成仓储的聚合根：" + ignoreAggregateRoots);
        getLog().info("聚合仓储自定义代码：" + getAggregateRepositoryCustomerCode());
        getLog().info("");
        this.getLog().info("开始生成仓储代码");

        // 项目结构解析
        String absoluteCurrentDir, projectDir, domainModulePath, applicationModulePath, adapterModulePath;
        absoluteCurrentDir = new File("").getAbsolutePath();
        if (multiModule) {
            projectDir = new File(absoluteCurrentDir + File.separator + "pom.xml").exists()
                    ? absoluteCurrentDir
                    : new File(absoluteCurrentDir).getParent();

            domainModulePath = Arrays.stream(new File(projectDir).listFiles())
                    .filter(path -> path.getAbsolutePath().endsWith(moduleNameSuffix4Domain))
                    .findFirst().get().getAbsolutePath();
            applicationModulePath = Arrays.stream(new File(projectDir).listFiles())
                    .filter(path -> path.getAbsolutePath().endsWith(moduleNameSuffix4Application))
                    .findFirst().get().getAbsolutePath();
            adapterModulePath = Arrays.stream(new File(projectDir).listFiles())
                    .filter(path -> path.getAbsolutePath().endsWith(moduleNameSuffix4Adapter))
                    .findFirst().get().getAbsolutePath();
        } else {
            projectDir = absoluteCurrentDir;
            domainModulePath = absoluteCurrentDir;
            applicationModulePath = absoluteCurrentDir;
            adapterModulePath = absoluteCurrentDir;
        }
        String basePackage = StringUtils.isNotBlank(this.basePackage)
                ? this.basePackage
                : SourceFileUtils.resolveDefaultBasePackage(domainModulePath);
        getLog().info(multiModule ? "多模块项目" : "单模块项目");
        getLog().info("项目目录：" + projectDir);
        getLog().info("适配层目录：" + adapterModulePath);
        getLog().info("应用层目录：" + applicationModulePath);
        getLog().info("领域层目录：" + domainModulePath);
        getLog().info("基础包名：" + basePackage);

        try {
            //renameAggregateRepositorySourceFiles(basePackage, adapterModulePath);
            List<File> files = SourceFileUtils.loadFiles(domainModulePath);
            files = files.stream()
                    .filter(file -> "java".equalsIgnoreCase(FileUtils.extension(file.getName())))
                    .collect(Collectors.toList());
            files.forEach(file -> {
                this.getLog().debug("发现Java文件: " + SourceFileUtils.resolveClassName(file.getAbsolutePath()));
            });
            getLog().info("发现java文件数量：" + files.size());
            files.forEach(file -> {
                String className = SourceFileUtils.resolveClassName(file.getAbsolutePath());
                this.getLog().debug("解析Java文件: " + className);
                String content = "";
                try {
                    content = FileUtils.fileRead(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                boolean isAggregateRoot = isAggregateRoot(content, className);
                if (isAggregateRoot) {
                    this.getLog().info("发现聚合根: " + className);

                    String simpleClassName = SourceFileUtils.resolveSimpleClassName(file.getAbsolutePath());
                    if (Arrays.stream(ignoreAggregateRoots.split("[\\,\\;]"))
                            .anyMatch(i -> i.equalsIgnoreCase(simpleClassName))) {
                        return;
                    }
                    try {
                        writeAggregateRepositorySourceFile(file.getAbsolutePath(), basePackage, adapterModulePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            this.getLog().error("发生异常，", e);
        }

        this.getLog().info("结束生成仓储代码");
    }

    Pattern AGGREGATE_PATTERN = Pattern.compile("\\((aggregate\\s*=\\s*){1}\\\"(){1}\\\"\\)");

    private boolean isAggregateRoot(String content, String className) {
        return Arrays.stream(content.split("(\r)|(\n)|(\r\n)"))
                .filter(line -> line.trim().startsWith("@") || line.replace("\\s", "").equals("/*@AggregateRoot*/"))
                .anyMatch(line -> {
                            boolean hasAggregateRoot = false;
                            if (StringUtils.isBlank(getAggregateRootAnnotation())) {
                                boolean oldAggregateRoot1 = line.matches("@AggregateRoot(\\(.*\\))?");
                                boolean oldAggregateRoot2 = line.matches("^\\s*\\/\\*\\s*@AggregateRoot\\s*\\*\\/\\s*$");
                                hasAggregateRoot = oldAggregateRoot1 || oldAggregateRoot2;
                            } else {
                                boolean isAggregateRootAnnotation = line.matches("@AggregateRoot(\\(.*\\))?");
                                boolean isAggregateRootAnnotationFullName = line.matches(getAggregateRootAnnotation() + "(\\(.*\\))?");
                                hasAggregateRoot = (isAggregateRootAnnotationFullName || isAggregateRootAnnotation);
                            }
                            if (hasAggregateRoot) {
                                return hasAggregateRoot;
                            }

                            boolean hasAggregate = line.matches("@Aggregate\\s*\\(.*type\\s*=\\s*\\\"root\\\".*\\)");
                            if (hasAggregate) {
                                Matcher matcher = AGGREGATE_PATTERN.matcher(line);
                                if (matcher.find() && matcher.groupCount() > 1) {
                                    AggregateRoot2AggregateNameMap.put(className, matcher.group(2));
                                }
                            }
                            boolean aggregateRoot = hasAggregate;

                            getLog().debug("annotationline: " + line);
                            getLog().debug("hasAggregateRoot=" + hasAggregateRoot);
                            getLog().debug("hasAggregate=" + hasAggregate);
                            return aggregateRoot;
                        }
                );
    }

    /**
     * 返回能否从新生成实体
     *
     * @param filePath
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
//                if (line.contains("【自定义代码开始】")) {
//                    startLine = i;
//                } else if (line.contains("【自定义代码结束】")) {
//                    endLine = i;
//                } else
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
        String packageName = basePackage + ".adapter.domain.repositories";

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

    public void writeAggregateRepositorySourceFile(String entitySourceFilePath, String basePackage, String baseDir) throws IOException {
        String packageName = basePackage + ".adapter.domain.repositories";

        String simpleClassName = SourceFileUtils.resolveSimpleClassName(entitySourceFilePath);
        String className = SourceFileUtils.resolveClassName(entitySourceFilePath);
        String aggregate = AggregateRoot2AggregateNameMap.getOrDefault(className, simpleClassName);

        new File(SourceFileUtils.resolveDirectory(baseDir, packageName)).mkdirs();
        String filePath = SourceFileUtils.resolveSourceFile(baseDir, packageName, simpleClassName + "Repository");
        Optional<File> fileAlreadyExisit = SourceFileUtils.findJavaFileBySimpleClassName(baseDir, simpleClassName + "Repository");
        if (fileAlreadyExisit.isPresent()) {
            filePath = fileAlreadyExisit.get().getAbsolutePath();
            packageName = SourceFileUtils.resolvePackage(filePath);
            FileUtils.rename(fileAlreadyExisit.get(), new File(fileAlreadyExisit.get().getAbsolutePath() + ".remove"));
        }

        List<String> customerLines = new ArrayList<>();
        List<String> importLines = new ArrayList<>();
        List<String> annotationLines = new ArrayList<>();
        readCustomerSourceFile(filePath, importLines, annotationLines, customerLines);
//        importLines.removeIf(l -> l.contains(".convention.AggregateRepository;"));
        importLines.removeIf(l -> l.contains("." + simpleClassName + ";"));

        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        writeLine(out, "package " + packageName + ";");
        writeLine(out, "");
//        writeLine(out, "import " + basePackage + ".convention.AggregateRepository;");
        writeLine(out, "import " + className + ";");
        if (importLines.size() > 0) {
            if (StringUtils.isEmpty(importLines.get(0))) {
                importLines.remove(0);
            }
            importLines.forEach(l -> writeLine(out, l));
        } else {
            writeLine(out, "");
            writeLine(out, "/**");
            writeLine(out, " * 本文件由[cap4j-ddd-codegen-maven-plugin]生成");
            writeLine(out, " */");
        }
        writeLine(out, "public interface " + simpleClassName + "Repository extends " + replacePlaceholder(getAggregateRepositoryBaseClass(), simpleClassName, aggregateIdentityClass, aggregate) + " {");
        if (customerLines.size() > 0) {
            for (String line : customerLines) {
                writeLine(out, line);
            }
        } else {
            writeLine(out, "    // 【自定义代码开始】本段落之外代码由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动");
            writeLine(out, "");
            writeLine(out, "    " + replacePlaceholder(getAggregateRepositoryCustomerCode(), simpleClassName, aggregateIdentityClass, aggregate));
            writeLine(out, "");
            writeLine(out, "    // 【自定义代码结束】本段落之外代码由[cap4j-ddd-codegen-maven-plugin]维护，请不要手工改动");

        }
        writeLine(out, "}");
        out.close();
    }

    private String replacePlaceholder(
            String template,
            String entityType,
            String identityType,
            String aggregate
    ) {
        return template.replace("${EntityType}", entityType)
                .replace("${IdentityType}", identityType)
                .replace("${Aggregate}", aggregate);
    }
}
