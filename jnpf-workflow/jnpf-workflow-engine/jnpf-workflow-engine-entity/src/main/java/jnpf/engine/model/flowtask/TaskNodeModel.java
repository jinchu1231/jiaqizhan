package jnpf.engine.model.flowtask;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/13 15:57
 */
@Data
public class TaskNodeModel {
    @Schema(description = "主键")
    private String id;
    @Schema(description = "节点名称")
    private String nodeName;
    @Schema(description = "节点编码")
    private String nodeCode;
}
