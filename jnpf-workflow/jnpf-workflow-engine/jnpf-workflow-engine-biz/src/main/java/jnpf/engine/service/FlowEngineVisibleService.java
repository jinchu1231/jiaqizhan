package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.model.flowtask.FlowAssistModel;

import java.util.List;

/**
 * 流程可见
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowEngineVisibleService extends SuperService<FlowEngineVisibleEntity> {

    /**
     * 列表
     *
     * @param flowIdList 流程主键
     * @return
     */
    List<FlowEngineVisibleEntity> getList(List<String> flowIdList);

    /**
     * 列表
     *
     * @return
     */
    List<FlowEngineVisibleEntity> getList();

    /**
     * 可见流程列表
     *
     * @param userId 用户主键
     * @return
     */
    List<FlowEngineVisibleEntity> getVisibleFlowList(String userId);

    /**
     * 可见流程列表
     *
     * @param userId 用户主键
     * @return
     */
    List<FlowEngineVisibleEntity> getVisibleFlowList(String userId, Integer type);

    /**
     * 删除流程可见
     */
    void deleteVisible(String flowId);

    /**
     * 保存协管数据
     */
    void assistList(FlowAssistModel assistModel);
}
