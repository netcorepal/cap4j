package org.netcorepal.cap4j.ddd.application.query;


import org.netcorepal.cap4j.ddd.application.RequestParam;
import org.netcorepal.cap4j.ddd.share.PageData;

/**
 * 分页查询
 *
 * @author binking338
 * @date
 */
public interface PageQuery<PARAM extends RequestParam<PageData<ITEM>>, ITEM> extends Query<PARAM, PageData<ITEM>> {
}
