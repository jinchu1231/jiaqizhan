package jnpf.integrate.model.integrate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
public class IntegrateInfoVO {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "编码")
    private String enCode;
    @Schema(description = "有效标志")
    private Integer enabledMark;
    @Schema(description = "排序码")
    private Long sortCode;
    @Schema(description = "类型")
    private Integer type;
    @Schema(description = "json")
    private String templateJson;
}
