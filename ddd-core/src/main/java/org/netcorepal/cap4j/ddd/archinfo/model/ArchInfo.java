package org.netcorepal.cap4j.ddd.archinfo.model;

import lombok.Builder;
import lombok.Data;

/**
 * 架构信息
 *
 * @author binking338
 * @date 2024/11/21
 */
@Data
@Builder
public class ArchInfo {
    String name;
    String version;
    String archInfoVersion;
    Architecture architecture;
}
