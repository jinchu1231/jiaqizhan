package jnpf.engine.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowtasknode.TaskNodeListModel;

import java.util.List;

/**
 * 流程节点
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskNodeService extends SuperService<FlowTaskNodeEntity> {

    /**
     * 列表
     *
     * @param taskId 任务主键
     * @return
     */
    List<FlowTaskNodeEntity> getList(String taskId, SFunction<FlowTaskNodeEntity, ?>... columns);

    /**
     * 列表
     *
     * @param id 节点主键
     * @return
     */
    List<FlowTaskNodeEntity> getList(List<String> id, SFunction<FlowTaskNodeEntity, ?>... columns);

    /**
     * 列表
     *
     * @param nodeListModel
     * @return
     */
    List<FlowTaskNodeEntity> getList(TaskNodeListModel nodeListModel, SFunction<FlowTaskNodeEntity, ?>... columns);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowTaskNodeEntity getInfo(String id, SFunction<FlowTaskNodeEntity, ?>... columns);

    /**
     * 删除（根据实例Id）
     *
     * @param taskId 任务主键
     */
    void deleteByTaskId(String taskId);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(FlowTaskNodeEntity entity);

    /**
     * 更新
     *
     * @param entity 实体对象
     */
    void update(FlowTaskNodeEntity entity);

    /**
     * 更新驳回开始流程节点
     *
     * @param taskId 流程id
     */
    void update(String taskId);

    /**
     * 修改节点的审批状态
     *
     * @param id    主键值
     * @param start 状态
     */
    void updateCompletion(List<String> id, int start);

    /**
     * 修改节点数据
     *
     * @param taskNodeLis
     */
    void updateTaskNode(List<FlowTaskNodeEntity> taskNodeLis);

    /**
     * 修改节点的选择分支数据
     *
     * @param id
     */
    void updateTaskNodeCandidates(List<String> id, String candidates);

    /**
     * 保存子流程任务id
     *
     * @param entity
     */
    void updateTaskIdList(FlowTaskNodeEntity entity);


}
