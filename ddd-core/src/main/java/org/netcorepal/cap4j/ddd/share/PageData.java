package org.netcorepal.cap4j.ddd.share;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 请使用 PageData.create 静态方法创建实例
 *
 * @author binking338
 * @date
 */
@Data
public class PageData<T> {

    protected PageData() {
    }

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 记录列表
     */
    private List<T> list;

    /**
     * 生成空分页返回
     *
     * @param pageSize
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> PageData<T> empty(Integer pageSize, Class<T> clazz) {
        return create(pageSize, 1, 0L, new ArrayList<T>());
    }

    /**
     * 新建分页结果
     *
     * @param pageParam
     * @param list
     * @param <T>
     * @return
     */
    public static <T> PageData<T> create(PageParam pageParam, Long totalCount, List<T> list) {
        PageData<T> pageData = new PageData<>();
        pageData.pageSize = pageParam.getPageSize();
        pageData.pageNum = pageParam.getPageNum();
        pageData.totalCount = totalCount;
        pageData.list = list;
        return pageData;

    }

    /**
     * 新建分页结果
     *
     * @param pageSize
     * @param pageNum
     * @param list
     * @param <T>
     * @return
     */
    public static <T> PageData<T> create(Integer pageSize, Integer pageNum, Long totalCount, List<T> list) {
        PageData<T> pageData = new PageData<>();
        pageData.pageSize = pageSize;
        pageData.pageNum = pageNum;
        pageData.totalCount = totalCount;
        pageData.list = list;
        return pageData;
    }

    /**
     * 转换分页结果类型
     *
     * @param <D>
     * @return
     */
    public <D> PageData<D> transform(Function<T, D> map) {
        PageData<D> pageData = create(pageSize, pageNum, totalCount, getList().stream().map(map).collect(Collectors.toList()));
        return pageData;
    }
}
