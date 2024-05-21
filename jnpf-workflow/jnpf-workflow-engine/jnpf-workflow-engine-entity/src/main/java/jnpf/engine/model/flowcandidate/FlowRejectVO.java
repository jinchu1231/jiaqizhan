package jnpf.engine.model.flowcandidate;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.engine.model.flowtask.TaskNodeModel;
import lombok.Data;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class FlowRejectVO {
    @Schema(description = "节点")
    private List<TaskNodeModel> list;
    @Schema(description = "是否选择")
    private Boolean isLastAppro = true;
}
