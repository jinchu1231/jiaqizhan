package jnpf.engine.model.flowbefore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:18
 */
@Data
public class FlowTemplateModel {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "流程基本主键")
    private String templateId;
    @Schema(description = "名称")
    private String fullName;
    @Schema(description = "类型")
    private Integer visibleType;
    @Schema(description = "json字段")
    private String flowTemplateJson;
    @Schema(description = "版本")
    private String version;
    @Schema(description = "类型")
    private Integer type;
}
