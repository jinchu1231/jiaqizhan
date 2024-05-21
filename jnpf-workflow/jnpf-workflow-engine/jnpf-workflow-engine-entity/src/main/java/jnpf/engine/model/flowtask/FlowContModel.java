package jnpf.engine.model.flowtask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/25 13:57
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowContModel {
    /**
     * 审批类型
     */
    private Integer type;
    /**
     * 编码
     */
    private String enCode;
    /**
     * 引擎id
     */
    private String flowId;
    /**
     * 表单分类
     */
    private Integer formType;
    /**
     * 任务id
     */
    private String processId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 代办id
     */
    private String taskOperatorId;
    /**
     * 节点id
     */
    private String taskNodeId;
}
