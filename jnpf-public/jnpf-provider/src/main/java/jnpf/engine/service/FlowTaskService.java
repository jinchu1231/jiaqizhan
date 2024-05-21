package jnpf.engine.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import jnpf.base.ActionResult;
import jnpf.base.service.SuperService;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.model.flowbefore.FlowBatchModel;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowtask.FlowTaskListModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.exception.WorkFlowException;
import jnpf.model.FlowWorkListVO;
import jnpf.permission.model.user.WorkHandoverModel;

import java.util.List;

/**
 * 流程任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskService extends SuperService<FlowTaskEntity> {

    /**
     * 列表（流程监控）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getMonitorList(PaginationFlowTask paginationFlowTask);

    /**
     * 列表（我发起的）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getLaunchList(PaginationFlowTask paginationFlowTask);


    /**
     * 列表（待我审批）
     * @param pagination
     * @return
     */
    List<FlowTaskListModel> getWaitList(PaginationFlowTask pagination);

    /**
     * 列表（抄送我的）
     *
     * @param pagination
     * @return
     */
    List<FlowTaskListModel> getCirculateList(PaginationFlowTask pagination);

    /**
     * 列表（我已审批）
     *
     * @param pagination
     * @return
     */
    List<FlowTaskListModel> getTrialList(PaginationFlowTask pagination);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     * @throws WorkFlowException 异常
     */
    FlowTaskEntity getInfo(String id, SFunction<FlowTaskEntity, ?>... columns) throws WorkFlowException;

    /**
     * 更新
     *
     * @param entity 主键值
     * @return
     */
    void update(FlowTaskEntity entity);

    /**
     * 创建
     *
     * @param entity 主键值
     * @return
     */
    void create(FlowTaskEntity entity);

    /**
     * 创建或者修改
     *
     * @param entity 主键值
     * @return
     */
    void createOrUpdate(FlowTaskEntity entity);

    /**
     * 信息
     *
     * @param id      主键值
     * @param columns 指定获取的列数据 , 任务中存了三个JSON数据 ， 排除后可以提高查询速度
     * @return
     */
    FlowTaskEntity getInfoSubmit(String id, SFunction<FlowTaskEntity, ?>... columns);

    /**
     * 信息
     *
     * @param ids     主键值
     * @param columns 指定获取的列数据 , 任务中存了三个JSON数据 ， 排除后可以提高查询速度
     * @return
     */
    List<FlowTaskEntity> getInfosSubmit(String[] ids, SFunction<FlowTaskEntity, ?>... columns);

    /**
     * 删除
     *
     * @param entity 实体对象
     * @throws WorkFlowException 异常
     */
    void delete(FlowTaskEntity entity) throws WorkFlowException;

    /**
     * 递归删除所有字节点
     *
     * @param id
     */
    void deleteChildAll(List<String> id);

    /**
     * 批量删除流程
     *
     * @param ids
     */
    void delete(String[] ids) throws WorkFlowException;

    /**
     * 查询订单状态
     *
     * @param id
     * @return
     */
    List<FlowTaskEntity> getOrderStaList(List<String> id, SFunction<FlowTaskEntity, ?>... columns);

    /**
     * 查询子流程
     *
     * @param id
     * @return
     */
    List<FlowTaskEntity> getChildList(String id, SFunction<FlowTaskEntity, ?>... columns);


    /**
     * 查询子流程
     *
     * @param id
     * @return
     */
    List<FlowTaskEntity> getChildList(List<String> id, SFunction<FlowTaskEntity, ?>... columns);

    /**
     * 查询流程列表
     *
     * @param tempId
     * @return
     */
    List<FlowTaskEntity> getTemplateIdList(String tempId);

    /**
     * 查询流程列表
     *
     * @param flowId
     * @return
     */
    List<FlowTaskEntity> getFlowList(String flowId);

    /**
     * 批量审批引擎
     *
     * @return
     */
    List<FlowBatchModel> batchFlowSelector();

    /**
     * 获取子节点下所有数据
     *
     * @param idList
     * @param idAll
     */
    void deleTaskAll(List<String> idList, List<String> idAll);

    /**
     * 获取所有子流程
     *
     * @param id
     * @return
     */
    List<String> getChildAllList(String id);

    /**
     * 获取子流程
     *
     * @param id
     * @return
     */
    void getChildList(String id, boolean suspend, List<String> list);

    /**
     * 流程交接
     * @return
     */
    FlowWorkListVO flowWork(String fromId);

    /**
     * 流程交接
     * @return
     */
    boolean flowWork(WorkHandoverModel workHandoverModel);

    ActionResult batchCreateOrUpdate(FlowModel flowModel);

    /**
     *
     * @param flowId
     * @return
     */
    FlowTemplateJsonEntity getFlowTemplateJsonEntity(String flowId) throws WorkFlowException;
}
