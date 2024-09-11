package org.netcorepal.cap4j.ddd.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 排序定义
 *
 * @author binking338
 * @date 2023/8/13
 */
@Data
@Schema(description = "排序定义")
@ApiModel(description = "排序定义")
public class OrderInfo {
    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    @ApiModelProperty(value = "排序字段")
    String field;
    /**
     * 是否降序
     */
    @Schema(description = "是否降序")
    @ApiModelProperty(value = "是否降序")
    Boolean desc;

    /**
     * 降序
     *
     * @param field
     * @return
     */
    public static OrderInfo desc(String field) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.field = field;
        orderInfo.desc = true;
        return orderInfo;
    }

    /**
     * 升序
     *
     * @param field
     * @return
     */
    public static OrderInfo asc(String field) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.field = field;
        orderInfo.desc = false;
        return orderInfo;
    }
}
