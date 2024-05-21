package jnpf.engine.model.flowtask;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class FlowAssistModel {
    @Schema(description = "主键")
    private String ids;
    @Schema(description = "用户")
    private List<String> list;
    @Schema(description = "流程基本主键")
    private String templateId;
}
