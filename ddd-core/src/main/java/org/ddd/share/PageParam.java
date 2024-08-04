package org.ddd.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author <template/>
 * @date
 */
@Schema(description = "分页参数")
@ApiModel(description = "分页参数")
@Data
public class PageParam {
    /**
     * 页码
     */
    @Schema(description="页码")
    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    /**
     * 页大小
     */
    @Schema(description="页大小")
    @ApiModelProperty(value = "页大小")
    private Integer pageSize;

    /**
     * 排序
     */
    @Schema(description="排序")
    @ApiModelProperty(value = "排序")
    private List<OrderInfo> sort;
}
