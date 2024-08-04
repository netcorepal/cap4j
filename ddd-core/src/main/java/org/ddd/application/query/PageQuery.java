package org.ddd.application.query;


import org.ddd.share.PageData;
import org.ddd.share.PageParam;

/**
 * @author <template/>
 * @date
 */
public interface PageQuery<PARAM extends PageParam, ITEM> extends Query<PARAM, PageData<ITEM>> {
}
