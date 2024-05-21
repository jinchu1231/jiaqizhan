package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程候选人
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
@TableName("flow_candidates")
public class FlowCandidatesEntity extends SuperExtendEntity<String> {

    /**
     * 节点主键
     */
    @TableField("F_TASK_NODE_ID")
    private String taskNodeId;

    /**
     * 任务主键
     */
    @TableField("F_TASK_ID")
    private String taskId;

    /**
     * 代办主键
     */
    @TableField("F_TASK_OPERATOR_ID")
    private String operatorId;

    /**
     * 审批人主键
     */
    @TableField("F_HANDLE_ID")
    private String handleId;

    /**
     * 审批人账号
     */
    @TableField("F_ACCOUNT")
    private String account;

    /**
     * 候选人
     */
    @TableField("F_CANDIDATES")
    private String candidates;

    /**
     * 类型 1.候选人 2.异常人
     */
    @TableField("F_TYPE")
    private Integer type;

}
