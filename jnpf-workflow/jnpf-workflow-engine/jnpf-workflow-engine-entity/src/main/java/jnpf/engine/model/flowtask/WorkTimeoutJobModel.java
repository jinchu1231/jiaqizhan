package jnpf.engine.model.flowtask;

import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowengine.FlowModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/17 17:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkTimeoutJobModel {

    private String tenantId;
    private String tenantDbConnectionString;
    private boolean isAssignDataSource;


    private FlowModel flowModel;
    private String taskId;
    private String taskNodeId;
    private String taskNodeOperatorId;
    private FlowTaskOperatorEntity operatorEntity;
    private Integer counter;
    private Integer overtimeNum;
    private boolean isSuspend = false;


}
