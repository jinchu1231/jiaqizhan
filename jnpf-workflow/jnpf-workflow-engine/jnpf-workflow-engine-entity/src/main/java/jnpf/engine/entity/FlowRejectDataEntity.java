package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 冻结审批
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
@TableName("flow_reject_data")
public class FlowRejectDataEntity extends SuperExtendEntity<String> {

    /**
     * 经办数据
     */
    @TableField("F_TASK_OPERATOR_JSON")
    public String taskOperatorJson;
    /**
     * 节点数据
     */
    @TableField("F_TASK_NODE_JSON")
    private String taskNodeJson;
    /**
     * 流程任务
     */
    @TableField("F_TASK_JSON")
    private String taskJson;


}
