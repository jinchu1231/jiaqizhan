package jnpf.engine.model.flowengine;

import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowDataModel {
    private ChildNodeList childNodeList;
    private List<FlowTaskNodeEntity> taskNodeList;
    private FlowModel flowModel;
    private Boolean isAssig = true;
    private Boolean isData = true;

}
