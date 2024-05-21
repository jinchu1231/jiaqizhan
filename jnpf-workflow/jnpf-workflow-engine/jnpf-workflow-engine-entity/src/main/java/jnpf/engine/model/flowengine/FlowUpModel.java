package jnpf.engine.model.flowengine;

import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowUpModel {
    private FlowTaskEntity flowTask;
    private FlowTaskNodeEntity taskNode;
    private List<FlowTaskNodeEntity> taskNodeList;
    private Boolean isReject = false;
    private Boolean rejectType = true;
    private FlowModel flowModel;
    private Boolean isAudit = false;
}
