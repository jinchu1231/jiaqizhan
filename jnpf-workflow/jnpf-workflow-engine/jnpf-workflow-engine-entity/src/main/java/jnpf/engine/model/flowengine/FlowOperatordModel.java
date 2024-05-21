package jnpf.engine.model.flowengine;

import jnpf.engine.entity.FlowTaskOperatorEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/7/14 9:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowOperatordModel {
    /**
     * 审批状态
     */
    private Integer status;
    /**
     * 审批原因
     */
    private FlowModel flowModel;
    /**
     * 审核人
     */
    private String userId;
    /**
     * 节点对象
     */
    private FlowTaskOperatorEntity operator;
    /**
     * 流转操作人
     */
    private String operatorId;
    /**
     * 自动审批
     */
    private Boolean voluntarily = false;
}
