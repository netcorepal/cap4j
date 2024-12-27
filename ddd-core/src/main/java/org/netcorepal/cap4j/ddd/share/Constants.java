package org.netcorepal.cap4j.ddd.share;

/**
 * 常量
 *
 * @author binking338
 * @date 2023/11/2
 */
public class Constants {
    public static final String ARCH_INFO_VERSION = "3.1.0-alpha-1";
    public static final String HEADER_KEY_CAP4J_EVENT_ID = "cap4j-event-id";
    public static final String HEADER_KEY_CAP4J_EVENT_TYPE = "cap4j-event-type";
    public static final String HEADER_VALUE_CAP4J_EVENT_TYPE_DOMAIN = "domain";
    public static final String HEADER_VALUE_CAP4J_EVENT_TYPE_INTEGRATION = "integration";
    public static final String HEADER_KEY_CAP4J_TIMESTAMP = "cap4j-timestamp";
    public static final String HEADER_KEY_CAP4J_SCHEDULE = "cap4j-schedule";
    public static final String HEADER_KEY_CAP4J_PERSIST = "cap4j-persist";

    public static final String CONFIG_KEY_4_SVC_NAME = "${spring.application.name:default}";
    public static final String CONFIG_KEY_4_SVC_VERSION = "${spring.application.version:unknown}";
    public static final String CONFIG_KEY_4_ROCKETMQ_NAME_SERVER = "${rocketmq.name-server:}";
    public static final String CONFIG_KEY_4_ROCKETMQ_MSG_CHARSET = "${rocketmq.msg-charset:UTF-8}";
}
