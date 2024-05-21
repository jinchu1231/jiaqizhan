package jnpf.engine.model.flowtask;

import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowtask.method.TaskOperatoUser;
import jnpf.permission.entity.UserEntity;
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
 * @date ：2022/4/25 13:57
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowAgreeRuleModel {
    private List<FlowTaskOperatorEntity> operatorListAll = new ArrayList<>();
    private TaskOperatoUser taskOperatoUser;
    private FlowTaskEntity flowTask;
    private Boolean reject = false;
    private List<UserEntity> userName = new ArrayList<>();
    private ChildNodeList childNode;
    private List<FlowTaskNodeEntity> taskNodeList = new ArrayList<>();
}
