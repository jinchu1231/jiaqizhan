package jnpf.engine.model.flowtask;

import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowengine.FlowModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/7/6 16:52
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowApproveModel {
    private List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
    private List<FlowTaskNodeEntity> taskNodeList = new ArrayList<>();
    private FlowTaskEntity flowTask = new FlowTaskEntity();
    private FlowModel flowModel = new FlowModel();
    private boolean isSubmit = false;
}
