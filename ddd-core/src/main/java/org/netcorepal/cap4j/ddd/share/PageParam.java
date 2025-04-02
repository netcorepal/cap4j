package org.netcorepal.cap4j.ddd.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 分页参数
 *
 * @author binking338
 * @date
 */
@Schema(description = "分页参数")
@ApiModel(description = "分页参数")
@Data
public class PageParam {
    /**
     * 页码
     */
    @Schema(description = "页码")
    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    /**
     * 页大小
     */
    @Schema(description = "页大小")
    @ApiModelProperty(value = "页大小")
    private Integer pageSize;

    /**
     * 排序
     */
    @Schema(description = "排序")
    @ApiModelProperty(value = "排序")
    private Collection<OrderInfo> sort;

    /**
     * 添加排序字段
     *
     * @param field
     * @param desc
     * @return
     */
    public PageParam orderBy(String field, boolean desc) {
        if (null == this.sort) {
            this.sort = new ArrayList<>();
        }
        this.sort.add(desc ? OrderInfo.desc(field) : OrderInfo.asc(field));
        return this;
    }

    /**
     * 添加排序字段
     *
     * @param field
     * @param desc
     * @return
     */
    public PageParam orderBy(Object field, boolean desc) {
        if (null == this.sort) {
            this.sort = new ArrayList<>();
        }
        this.sort.add(desc ? OrderInfo.desc(field.toString()) : OrderInfo.asc(field.toString()));
        return this;
    }

    /**
     * 添加排序字段
     *
     * @param field
     * @return
     */
    public PageParam orderByDesc(String field) {
        return orderBy(field, true);
    }

    /**
     * 添加排序字段
     *
     * @param field
     * @return
     */
    public PageParam orderByDesc(Object field) {
        return orderBy(field, true);
    }

    /**
     * 添加排序字段
     *
     * @param field
     * @return
     */
    public PageParam orderByAsc(String field) {
        return orderBy(field, false);
    }

    /**
     * 添加排序字段
     *
     * @param field
     * @return
     */
    public PageParam orderByAsc(Object field) {
        return orderBy(field, false);
    }

    /**
     * 重置排序字段
     *
     * @return
     */
    public PageParam orderReset() {
        if (null != this.sort) {
            this.sort.clear();
        }
        return this;
    }

    /**
     * 创建分页参数
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public static PageParam of(int pageNum, int pageSize) {
        return of(pageNum, pageSize, null);
    }

    /**
     * 创建分页参数
     *
     * @param pageNum
     * @param pageSize
     * @param sort
     * @return
     */
    public static PageParam of(int pageNum, int pageSize, List<OrderInfo> sort) {
        PageParam pageParam = new PageParam();
        pageParam.pageNum = pageNum;
        pageParam.pageSize = pageSize;
        pageParam.sort = sort;
        return pageParam;
    }

    /**
     * 创建分页参数，pageNum=1
     *
     * @param pageSize
     * @return
     */
    public static PageParam limit(int pageSize) {
        return of(1, pageSize);
    }

    /**
     * 创建分页参数，pageNum=1
     *
     * @param pageSize
     * @param sort
     * @return
     */
    public static PageParam limit(int pageSize, List<OrderInfo> sort) {
        return of(1, pageSize, sort);
    }
}
