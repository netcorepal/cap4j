package org.netcorepal.cap4j.ddd.codegen.template;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模板
 *
 * @author binking338
 * @date 2024/9/13
 */
@Data
@NoArgsConstructor
public class Template extends PathNode {

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
    public List<TemplateNode> select(String tag) {
        if (this.templates == null) return null;
        List<TemplateNode> nodes = templates.stream().filter(t -> Objects.equals(t.tag, tag)).collect(Collectors.toList());
        return nodes;
    }
}
