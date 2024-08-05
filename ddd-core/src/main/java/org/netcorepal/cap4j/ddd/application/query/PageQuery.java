package org.netcorepal.cap4j.ddd.application.query;


import org.netcorepal.cap4j.ddd.share.PageData;
import org.netcorepal.cap4j.ddd.share.PageParam;

/**
 * 分页查询
 *
 * @author binking338
 * @date
 */
public interface PageQuery<PARAM extends PageParam, ITEM> extends Query<PARAM, PageData<ITEM>> {
}
