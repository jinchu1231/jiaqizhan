package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

import java.util.Date;

/**
 * 流程经办
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("flow_task_operator")
public class FlowTaskOperatorEntity extends SuperExtendEntity.SuperExtendDescriptionEntity<String> {

    /**
     * 加签处理人
     */
    @TableField("F_APPEND_HANDLE_ID")
    private String appendHandleId;

    /**
     * 节点类型
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 经办主键
     */
    @TableField("F_HANDLE_ID")
    private String handleId;

    /**
     * 处理状态 0-拒绝、1-同意
     */
    @TableField(value = "F_HANDLE_STATUS", fill = FieldFill.UPDATE)
    private Integer handleStatus;

    /**
     * 处理时间
     */
    @TableField(value = "F_HANDLE_TIME", fill = FieldFill.UPDATE)
    private Date handleTime;

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
     * 状态 0.新流程 -1.无用数据 1加签人
     */
    @TableField("F_STATE")
    private Integer state;

    /**
     * 父节点id
     */
    @TableField("F_PARENT_ID")
    private String parentId;

    /**
     * 草稿数据
     */
    @TableField("F_DRAFT_DATA")
    private String draftData;

    /**
     * 自动审批
     */
    @TableField("F_AUTOMATION")
    private String automation;

    /**
     * 排序码
     */
    @TableField("F_SORT_CODE")
    private Long sortCode;

    /**
     * 回滚id
     */
    @TableField("F_ROLLBACK_ID")
    private String rollbackId;

    /**
     * 冻结
     */
    @TableField("F_REJECT")
    private String reject;

}
