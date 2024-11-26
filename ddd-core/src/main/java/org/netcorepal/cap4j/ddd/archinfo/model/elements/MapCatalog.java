package org.netcorepal.cap4j.ddd.archinfo.model.elements;

import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 目录
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
public class MapCatalog extends HashMap<String, Element> implements Element {
    String name;
    String description;

    public MapCatalog(String name, String description, Map<String, Element> elements){
        this.name = name;
        this.description = description;
        for (Map.Entry<String, Element> entry : elements.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public MapCatalog(String name, String description, Collection<Element> elements){
        this.name = name;
        this.description = description;
        for (Element element : elements) {
            put(element.getName(), element);
        }
    }

    @Override
    public String getType() {
        return Element.TYPE_CATALOG;
    }
}
