package jnpf.engine.model.flowtask.method;

import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import lombok.Data;

import java.util.List;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/25 13:57
 */
@Data
public class TaskHandleIdStatus {
    /**
     * 审批类型（0：拒绝，1：同意）
     **/
    private Integer status;
    /**
     * 当前节点属性
     **/
    private ChildNodeList nodeModel;
    /**
     * 用户
     **/
    private UserInfo userInfo;
    /**
     * 审批对象
     **/
    private FlowModel flowModel;
    /**
     * 节点list
     **/
    private List<FlowTaskNodeEntity> taskNodeList;

}
