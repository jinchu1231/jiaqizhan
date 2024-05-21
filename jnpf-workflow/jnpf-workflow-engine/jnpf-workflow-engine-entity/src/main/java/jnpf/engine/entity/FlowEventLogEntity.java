package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperBaseEntity;
import lombok.Data;

/**
 * 流程事件日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("flow_event_log")
public class FlowEventLogEntity extends SuperBaseEntity.SuperCBaseEntity<String> {

    /**
     * 节点主键
     */
    @TableField("F_TASK_NODE_ID")
    private String taskNodeId;

    /**
     * 接口主键
     */
    @TableField("F_INTERFACE_ID")
    private String interfaceId;

    /**
     * 事件名称
     */
    @TableField("F_FULL_NAME")
    private String fullName;

    /**
     * 执行结果
     */
    @TableField("F_RESULT")
    private String result;

}
