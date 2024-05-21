package jnpf.engine.model.flowengine;

import jnpf.engine.entity.FlowTaskNodeEntity;
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
 * @date ：2022/6/1 11:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowNextModel {
    private List<FlowTaskNodeEntity> nodeListAll = new ArrayList<>();
    private List<FlowTaskNodeEntity> nextNodeEntity = new ArrayList<>();
    private FlowTaskNodeEntity taskNode;
    private FlowModel flowModel;
    private Boolean isCountersign = false;
}
