package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.exception.WorkFlowException;

import java.util.List;
import java.util.Set;

/**
 * 流程经办
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskOperatorService extends SuperService<FlowTaskOperatorEntity> {

    /**
     * 列表
     *
     * @param taskId 流程实例Id
     * @return
     */
    List<FlowTaskOperatorEntity> getList(String taskId);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowTaskOperatorEntity getInfo(String id) throws WorkFlowException;

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowTaskOperatorEntity getOperatorInfo(String id);

    /**
     * 创建
     *
     * @param entitys 实体对象
     */
    void create(List<FlowTaskOperatorEntity> entitys);

    /**
     * 更新
     *
     * @param entity 实体对象
     */
    void update(FlowTaskOperatorEntity entity);

    /**
     * 更新会签委托人的审核状态
     *
     * @param taskNodeId 流程节点id
     * @param userId     委托人id
     * @param completion 审批状态
     */
    void update(String taskNodeId, List<String> userId, String completion);

    /**
     * 更新流程经办审核状态
     *
     * @param taskNodeId 流程节点id
     * @param type       流程类型
     */
    void update(String taskNodeId, Integer type);

    /**
     * 更新驳回流程节点
     *
     * @param taskId 流程id
     */
    void update(String taskId);

    /**
     * 经办未审核人员
     *
     * @param taskId 任务id
     * @return
     */
    List<FlowTaskOperatorEntity> press(String taskId);

    /**
     * 驳回的节点之后的节点作废
     *
     * @param taskId
     * @param taskNodeId
     */
    void updateReject(String taskId, Set<String> taskNodeId);

    /**
     * 删除经办id
     *
     * @param idAll 经办id
     */
    void deleteList(List<String> idAll);

    /**
     * 查询加签人信息
     *
     * @param parentId 父节点Id
     * @return
     */
    List<FlowTaskOperatorEntity> getParentId(String parentId);

    /**
     * 更新经办记录作废
     *
     * @param idAll
     */
    void updateTaskOperatorState(List<String> idAll);

    /**
     * 获取自己代办的流程任务
     *
     * @return
     */
    List<FlowTaskOperatorEntity> getBatchList();
}
