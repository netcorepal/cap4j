package org.netcorepal.cap4j.ddd.archinfo.model.elements;

import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.Element;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 目录
 *
 * @author binking338
 * @date 2024/11/25
 */
@Data
public class ListCatalog extends ArrayList<Element> implements Element {
    String name;
    String description;

    public ListCatalog(String name, String description, Collection<Element> elements){
        this.name = name;
        this.description = description;
        for (Element element : elements) {
            add(element);
        }
    }

    @Override
    public String getType() {
        return Element.TYPE_CATALOG;
    }
}
