package jnpf.engine.model.flowtask;

import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.permission.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/25 13:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowConditionModel {
    private String data;
    private String nodeId;
    private UserInfo userInfo;
    private UserEntity userEntity;
    private FlowTaskEntity flowTaskEntity;
    private List<ChildNodeList> childNodeListAll;
    private List<ConditionList> conditionListAll;
}
