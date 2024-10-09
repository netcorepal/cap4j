package org.netcorepal.cap4j.ddd.codegen.template;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.IOException;
import java.util.Map;

/**
 * 脚手架模板模板节点
 *
 * @author binking338
 * @date 2024/9/13
 */
@Data
public class TemplateNode extends PathNode {

    /**
     * 元素匹配正则
     */
    String pattern;

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
