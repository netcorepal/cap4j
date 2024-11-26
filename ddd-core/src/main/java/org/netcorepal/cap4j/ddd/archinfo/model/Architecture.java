package org.netcorepal.cap4j.ddd.archinfo.model;

import lombok.Builder;
import lombok.Data;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.ListCatalog;
import org.netcorepal.cap4j.ddd.archinfo.model.elements.MapCatalog;

/**
 * 架构
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
@Builder
public class Architecture {
    Application application;
    Domain domain;

    @Data
    @Builder
    public static class Application {
        MapCatalog requests;
        MapCatalog events;
    }

    @Data
    @Builder
    public static class Domain {
        MapCatalog aggregates;
        ListCatalog services;
    }
}
