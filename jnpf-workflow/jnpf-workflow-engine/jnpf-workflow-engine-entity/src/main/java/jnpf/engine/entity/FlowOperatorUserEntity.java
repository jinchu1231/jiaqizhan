package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程依次审批
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
@TableName("flow_task_operator_user")
public class FlowOperatorUserEntity extends SuperExtendEntity.SuperExtendDescriptionEntity<String> {

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
     * 状态
     */
    @TableField("F_STATE")
    private Integer state;

    /**
     * 经办主键
     */
    @TableField("F_HANDLE_ID")
    private String handleId;

    /**
     * 节点类型
     */
    @TableField("F_Type")
    private Integer type;

    /**
     * 节点编码
     */
    @TableField("F_NODE_CODE")
    private String nodeCode;

    /**
     * 节点名称
     */
    @TableField("F_NODE_NAME")
    private String nodeName;

    /**
     * 是否完成
     */
    @TableField("F_COMPLETION")
    private Integer completion;

    /**
     * 父节点id
     */
    @TableField("F_PARENT_ID")
    private String parentId;

    /**
     * 自动审批
     */
    @TableField("F_AUTOMATION")
    private String automation;

    /**
     * 冻结
     */
    @TableField("F_REJECT")
    private String reject;

}
