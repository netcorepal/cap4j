package org.netcorepal.cap4j.ddd.application.query;

import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

/**
 * 分页查询参数
 *
 * @author binking338
 * @date 2024/9/6
 */
public abstract class PageQueryParam<RESPONSE_ITEM> extends PageParam implements RequestParam<PageData<RESPONSE_ITEM>> {
}
