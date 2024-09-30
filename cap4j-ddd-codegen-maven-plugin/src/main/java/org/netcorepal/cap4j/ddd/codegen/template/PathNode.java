package org.netcorepal.cap4j.ddd.codegen.template;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.netcorepal.cap4j.ddd.codegen.misc.SourceFileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 脚手架模板文件节点
 *
 * @author binking338
 * @date 2024/9/13
 */
@Data
public class PathNode {
    /**
     * 节点类型：root|dir|file|segment
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
