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
public class FlowTaskOperatorModel {
    @Schema(description = "节点经办主键")
    private String id;
    @Schema(description = "经办对象")
    private String handleType;
    @Schema(description = "经办主键")
    private String handleId;
    @Schema(description = "处理状态 0-拒绝、1-同意")
    private Integer handleStatus;
    @Schema(description = "处理时间")
    private Long handleTime;
    @Schema(description = "节点编码")
    private String nodeCode;
    @Schema(description = "节点名称")
    private String nodeName;
    @Schema(description = "是否完成")
    private Integer completion;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "创建时间")
    private Long creatorTime;
    @Schema(description = "节点主键")
    private String taskNodeId;
    @Schema(description = "任务主键")
    private String taskId;
    @Schema(description = "草稿数据")
    private String draftData;
}
