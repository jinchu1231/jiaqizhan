package jnpf.engine.model.flowengine;

import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/1 12:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowUpdateNode {
    private UserInfo userInfo;
    private FlowTaskEntity flowTask;
    private ChildNode childNodeAll;
    private List<ChildNodeList> nodeListAll;
    private List<ConditionList> conditionListAll;
    private List<FlowTaskNodeEntity> taskNodeList;
    private boolean isSubmit = false;
}
