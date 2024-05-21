package jnpf.engine.service;

import jnpf.engine.enums.FlowStatusEnum;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.exception.WorkFlowException;

/**
 * 在线开发工作流
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:19
 */
public interface FlowDynamicService {

    /**
     * 流程数据
     *
     * @param flowModel
     */
    void flowTask(FlowModel flowModel, FlowStatusEnum flowStatus, ChildNode childNode) throws WorkFlowException;

    /**
     * 保存流程
     *
     * @param flowModel
     * @throws WorkFlowException
     */
    void createOrUpdate(FlowModel flowModel) throws WorkFlowException;

    /**
     * 批量保存流程
     *
     * @param flowModel
     * @throws WorkFlowException
     */
    void batchCreateOrUpdate(FlowModel flowModel) throws WorkFlowException;
}
