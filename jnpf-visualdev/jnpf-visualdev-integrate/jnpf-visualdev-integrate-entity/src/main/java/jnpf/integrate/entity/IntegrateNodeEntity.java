package jnpf.integrate.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

import java.util.Date;

/**
 *
 * @version V3.4.5
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 */
@Data
@TableName("base_integrate_node")
public class IntegrateNodeEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 任务主键
     */
    @TableField("F_TASK_ID")
    private String taskId;

    /**
     * 节点编码
     */
    @TableField("F_NODE_CODE")
    private String nodeCode;

    /**
     * 节点编码
     */
    @TableField("F_FORM_ID")
    private String formId;

    /**
     * 节点类型
     */
    @TableField("F_NODE_TYPE")
    private String nodeType;

    /**
     * 节点名称
     */
    @TableField("F_NODE_NAME")
    private String nodeName;

    /**
     * 父节点id
     */
    @TableField("F_PARENT_ID")
    private String parentId;

    /**
     * 节点属性Json
     */
    @TableField("F_NODE_PROPERTY_JSON")
    private String nodePropertyJson;

    /**
     * 下一节点
     */
    @TableField("F_NODE_NEXT")
    private String nodeNext;

    /**
     * 运行结果
     */
    @TableField("F_RESULT_TYPE")
    private Integer resultType;

    /**
     * 异常
     */
    @TableField("F_ERROR_MSG")
    private String errorMsg;

    /**
     * 开始时间
     */
    @TableField("f_start_time")
    private Date startTime;

    /**
     * 能否重试
     */
    @TableField("f_is_retry")
    private Integer isRetry;

    /**
     * 结束时间
     */
    @TableField("F_END_Time")
    private Date endTime;
}
