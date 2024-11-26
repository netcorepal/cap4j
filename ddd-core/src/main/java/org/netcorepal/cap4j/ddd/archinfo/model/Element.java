package org.netcorepal.cap4j.ddd.archinfo.model;

/**
 * 元素
 *
 * @author binking338
 * @date 2024/11/21
 */
public interface Element {
    static final String TYPE_REF = "ref";
    static final String TYPE_NONE = "none";
    static final String TYPE_CATALOG = "catalog";
    static final String TYPE_REPOSITORY = "repository";
    static final String TYPE_FACTORY = "factory";
    static final String TYPE_ENTITY = "entity";
    static final String TYPE_VALUE_OBJECT = "value-object";
    static final String TYPE_ENUM = "enum";
    static final String TYPE_SPECIFICATION = "specification";
    static final String TYPE_DOMAIN_SERVICE = "domain-service";
    static final String TYPE_DOMAIN_EVENT = "domain-event";
    static final String TYPE_INTEGRATION_EVENT = "integration-event";
    static final String TYPE_SUBSCRIBER = "subscriber";
    static final String TYPE_COMMAND = "command";
    static final String TYPE_QUERY = "query";
    static final String TYPE_SAGA = "saga";
    static final String TYPE_REQUEST = "request";

    String getType();
    String getName();
    String getDescription();
}
