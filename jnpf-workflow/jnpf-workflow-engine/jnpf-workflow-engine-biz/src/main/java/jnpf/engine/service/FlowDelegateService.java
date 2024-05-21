package jnpf.engine.service;

import jnpf.base.service.SuperService;
import jnpf.base.vo.ListVO;
import jnpf.engine.entity.FlowDelegateEntity;
import jnpf.engine.model.flowcandidate.FlowCandidateUserModel;
import jnpf.engine.model.flowdelegate.FlowDelegateCrForm;
import jnpf.engine.model.flowdelegate.FlowDelegatePagination;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.engine.model.flowtemplate.FlowPageListVO;
import jnpf.exception.WorkFlowException;

import java.util.List;

/**
 * 流程委托
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowDelegateService extends SuperService<FlowDelegateEntity> {

    /**
     * 列表
     *
     * @param pagination 请求参数
     * @return
     */
    List<FlowDelegateEntity> getList(FlowDelegatePagination pagination);

    /**
     * 列表
     *
     * @return
     */
    List<FlowDelegateEntity> getList();


    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowDelegateEntity getInfo(String id);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(FlowDelegateEntity entity);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(FlowDelegateEntity entity);

    /**
     * 获取被委托人的表单
     *
     * @param touserId 被委托人
     * @return
     */
    List<FlowDelegateEntity> getUser(String touserId);

    /**
     * 获取委托的表单
     *
     * @param userId   委托人
     * @param flowId   流程引擎
     * @param touserId 被委托人
     * @return
     */
    List<FlowDelegateEntity> getUser(String userId, String flowId, String touserId);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    boolean update(String id, FlowDelegateEntity entity);

    /**
     * 委托结束
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    boolean updateStop(String id, FlowDelegateEntity entity);

    /**
     * 获取我的委托发起
     *
     * @return
     */
    List<FlowPageListVO> getflow(FlowPagination pagination);

    /**
     * 根据流程获取委托人列表。
     *
     * @param flowId 流程版本id
     * @return
     */
    ListVO<FlowCandidateUserModel> getUserListByFlowId(String flowId) throws WorkFlowException;

    /**
     * 根据条件查询相关委托信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/10/28
     */
    List<FlowDelegateEntity> selectSameParamAboutDelaget(FlowDelegateCrForm model);

}
