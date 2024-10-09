package org.netcorepal.cap4j.ddd.application.query;

import org.netcorepal.cap4j.ddd.application.RequestParam;

import java.util.List;

/**
 * 列表查询接口
 *
 * @author binking338
 * @date
 */
public interface ListQuery<PARAM extends RequestParam<List<ITEM>>,ITEM> extends Query<PARAM, List<ITEM>> {
}
