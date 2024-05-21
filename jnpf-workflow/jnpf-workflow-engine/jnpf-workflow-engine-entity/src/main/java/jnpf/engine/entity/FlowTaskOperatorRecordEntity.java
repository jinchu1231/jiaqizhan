package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

import java.util.Date;

/**
 * 流程经办记录
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("flow_task_operator_record")
public class FlowTaskOperatorRecordEntity extends SuperExtendEntity<String> {

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
     * 经办状态 0-拒绝、1-同意、2-提交、3-撤回、4-终止、5-指派、6-加签、7-转办
     */
    @TableField("F_HANDLE_STATUS")
    private Integer handleStatus;

    /**
     * 经办人员
     */
    @TableField("F_HANDLE_ID")
    private String handleId;

    /**
     * 经办时间
     */
    @TableField("F_HANDLE_TIME")
    private Date handleTime;

    /**
     * 经办理由
     */
    @TableField("F_HANDLE_OPINION")
    private String handleOpinion;

    /**
     * 流转操作人
     */
    @TableField("F_OPERATOR_ID")
    private String operatorId;

    /**
     * 经办主键
     */
    @TableField(value = "F_TASK_OPERATOR_ID", fill = FieldFill.UPDATE)
    private String taskOperatorId;

    /**
     * 节点主键
     */
    @TableField(value = "F_TASK_NODE_ID", fill = FieldFill.UPDATE)
    private String taskNodeId;

    /**
     * 任务主键
     */
    @TableField("F_TASK_ID")
    private String taskId;

    /**
     * 签名图片
     */
    @TableField("F_SIGN_IMG")
    private String signImg;

    /**
     * 0.进行数据 1.加签数据 3.已办不显示数据
     */
    @TableField("F_STATUS")
    private Integer status;

    /**
     * 经办文件
     */
    @TableField("F_FILE_LIST")
    private String fileList;

    /**
     * 审批数据
     */
    @TableField("F_DRAFT_DATA")
    private String draftData;

    /**
     * 加签类型
     */
    @TableField("F_APPROVER_TYPE")
    private Integer approverType;

}
