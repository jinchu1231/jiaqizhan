package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程可见
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("flow_visible")
public class FlowEngineVisibleEntity extends SuperExtendEntity<String> {

    /**
     * 流程主键
     */
    @TableField("F_FLOW_ID")
    private String flowId;

    /**
     * 经办类型
     */
    @TableField("F_OPERATOR_TYPE")
    private String operatorType;

    /**
     * 经办主键
     */
    @TableField("F_OPERATOR_ID")
    private String operatorId;

    /**
     * 排序码
     */
    @TableField("F_SORT_CODE")
    private Long sortCode;

    /**
     * 可见类型 1.发起 2.协管
     */
    @TableField("F_TYPE")
    private Integer type;


}
