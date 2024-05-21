package jnpf.engine.model.flowengine;

import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class FlowOperator {
    private UserInfo userInfo;
    private FlowModel flowModel;
    private FlowTaskEntity flowTask;
    private List<ChildNodeList> nodeList;
    private List<FlowTaskNodeEntity> taskNodeListAll;
    private List<FlowTaskOperatorEntity> operatorListAll;
    private boolean reject = false;
    private Map<String, List<String>> asyncTaskList = new HashMap<>();
    private Map<String, List<String>> nodeTaskIdList = new HashMap<>();
}
