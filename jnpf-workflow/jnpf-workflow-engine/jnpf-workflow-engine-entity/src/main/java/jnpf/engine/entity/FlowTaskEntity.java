package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

import java.util.Date;

/**
 * 流程任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("flow_task")
public class FlowTaskEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 实例进程
     */
    @TableField("F_PROCESS_ID")
    private String processId;

    /**
     * 任务编码
     */
    @TableField("F_EN_CODE")
    private String enCode;

    /**
     * 任务标题
     */
    @TableField("F_FULL_NAME")
    private String fullName;

    /**
     * 紧急程度
     */
    @TableField("F_FLOW_URGENT")
    private Integer flowUrgent;

    /**
     * 流程主键
     */
    @TableField("F_FLOW_ID")
    private String flowId;

    /**
     * 流程模板id
     */
    @TableField("F_TEMPLATE_ID")
    private String templateId;

    /**
     * 流程编码
     */
    @TableField("F_FLOW_CODE")
    private String flowCode;

    /**
     * 流程名称
     */
    @TableField("F_FLOW_NAME")
    private String flowName;

    /**
     * 流程类型
     */
    @TableField("F_FLOW_TYPE")
    private Integer flowType;

    /**
     * 流程分类
     */
    @TableField("F_FLOW_CATEGORY")
    private String flowCategory;

    /**
     * 表单内容
     */
    @TableField("F_FLOW_FORM_DATA_JSON")
    private String flowFormContentJson;

    /**
     * 流程版本
     */
    @TableField("F_FLOW_VERSION")
    private String flowVersion;

    /**
     * 开始时间
     */
    @TableField("F_START_TIME")
    private Date startTime;

    /**
     * 结束时间
     */
    @TableField("F_END_TIME")
    private Date endTime;

    /**
     * 当前步骤
     */
    @TableField("F_CURRENT_NODE_NAME")
    private String thisStep;

    /**
     * 当前步骤Id
     */
    @TableField("F_CURRENT_NODE_CODE")
    private String thisStepId;

    /**
     * 任务状态 0-草稿、1-处理、2-通过、3-驳回、4-撤销、5-终止、6-挂起
     */
    @TableField("F_STATUS")
    private Integer status;

    /**
     * 挂起之前状态
     */
    @TableField(value = "F_SUSPEND", fill = FieldFill.UPDATE)
    private Integer suspend;

    /**
     * 完成情况
     */
    @TableField("F_COMPLETION")
    private Integer completion;

    /**
     * 父节点id
     */
    @TableField("F_PARENT_ID")
    private String parentId;

    /**
     * 被委托用户
     */
    @TableField("F_DELEGATE_USER_ID")
    private String delegateUser;

    /**
     * 关联系统id
     */
    @TableField("F_SYSTEM_ID")
    private String systemId;

    /**
     * 节点主键
     */
    @TableField(value = "F_REVIVE_NODE_ID", fill = FieldFill.UPDATE)
    private String taskNodeId;

    /**
     * 是否批量（0：否，1：是）
     */
    @TableField("F_IS_BATCH")
    private Integer isBatch;

    /**
     * 是否能恢复（0：能，1：不能）
     */
    @TableField("F_RESTORE")
    private Integer frestore;

    /**
     * 同步异步（0：同步，1：异步）
     */
    @TableField("F_IS_ASYNC")
    private Integer isAsync;

    /**
     * 冻结审批
     */
    @TableField(value = "F_REJECT_DATA_ID", fill = FieldFill.UPDATE)
    private String rejectId;

}
