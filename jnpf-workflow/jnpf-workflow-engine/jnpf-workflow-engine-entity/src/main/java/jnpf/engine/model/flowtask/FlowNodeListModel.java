package jnpf.engine.model.flowtask;

import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowengine.FlowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodeListModel {
    private List<FlowTaskNodeEntity> dataAll = new ArrayList<>();
    private FlowModel flowModel = new FlowModel();
    private Boolean isAdd = false;
    private FlowTaskNodeEntity taskNode = new FlowTaskNodeEntity();
    private Long num = 1L;
}
