package org.netcorepal.cap4j.ddd.codegen;

import com.alibaba.fastjson.JSON;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;
import org.netcorepal.cap4j.ddd.codegen.template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 生成项目目录结构
 *
 * @author binking338
 * @date 2024/8/15
 */
@Mojo(name = "gen-arch")
public class GenArchMojo extends MyAbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("当前系统默认编码：" + Charset.defaultCharset().name());
        getLog().info("设置模板读取编码：" + archTemplateEncoding + " (from archTemplateEncoding)");
        getLog().info("设置输出文件编码：" + outputEncoding + " (from outputEncoding)");
        getLog().info("基础包名：" + basePackage);
        getLog().info(multiModule ? "多模块项目" : "单模块项目");
        getLog().info("项目目录：" + getProjectDir());
        getLog().info("适配层目录：" + getAdapterModulePath());
        getLog().info("应用层目录：" + getApplicationModulePath());
        getLog().info("领域层目录：" + getDomainModulePath());
        this.renderFileSwitch = true;
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

        template = JSON.parseObject(templateContent, Template.class);
        try {
            template.resolve(getEscapeContext());
        } catch (IOException e) {
            getLog().error("模板文件加载失败！", e);
        }

        try {
            render(template, getProjectDir());
        } catch (IOException e) {
            getLog().error("模板文件写入失败！", e);
        }
    }
}
