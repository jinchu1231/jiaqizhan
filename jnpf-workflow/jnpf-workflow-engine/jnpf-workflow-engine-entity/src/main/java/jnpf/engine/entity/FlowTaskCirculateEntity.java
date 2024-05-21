package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程传阅
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("flow_task_circulate")
public class FlowTaskCirculateEntity extends SuperExtendEntity<String> {

    /**
     * 对象类型
     */
    @TableField("F_OBJECT_TYPE")
    private String objectType;

    /**
     * 对象主键
     */
    @TableField("F_OBJECT_ID")
    private String objectId;

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
     * 节点主键
     */
    @TableField("F_TASK_NODE_ID")
    private String taskNodeId;

    /**
     * 任务主键
     */
    @TableField("F_TASK_ID")
    private String taskId;

}
