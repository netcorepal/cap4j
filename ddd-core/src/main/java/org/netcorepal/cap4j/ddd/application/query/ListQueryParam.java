package org.netcorepal.cap4j.ddd.application.query;

import org.netcorepal.cap4j.ddd.application.RequestParam;

import java.util.List;

/**
 * 列表查询参数
 *
 * @author binking338
 * @date 2024/9/6
 */
public interface ListQueryParam<RESPONSE_ITEM> extends RequestParam<List<RESPONSE_ITEM>> {
}
