package jnpf.service;

import jnpf.base.ActionResult;
import jnpf.base.service.SuperService;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.FlowTempInfoModel;
import jnpf.model.form.FlowFormPage;

import java.util.List;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 18:01
 */
public interface FlowFormService extends SuperService<FlowFormEntity> {

    /**
     * 判断名称是否重复
     *
     * @param fullName 名称
     * @param id       主键
     * @return ignore
     */
    boolean isExistByFullName(String fullName, String id);

    /**
     * 判断code是否重复
     *
     * @param enCOde 名称
     * @param id       主键
     * @return ignore
     */
    boolean isExistByEnCode(String enCOde, String id);
    /**
     * 创建
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/9/29
     */
    Boolean create(FlowFormEntity entity) throws WorkFlowException;

    /**
     * 修改
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/9/29
     */
    Boolean update(FlowFormEntity entity) throws Exception;
    /**
     * 查询列表
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/7/1
     */
    List<FlowFormEntity> getList(FlowFormPage flowFormPage);
    /**
     * 查询列表
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/7/1
     */
    List<FlowFormEntity> getListForSelect(FlowFormPage flowFormPage);
    /**
     * 发布/回滚
     * @param isRelease 是否发布：1-发布 0-回滚
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/7/1
     */
    ActionResult release(String id, Integer isRelease) throws WorkFlowException ;
    /**
     * 复制表单
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/7/1
     */
    boolean copyForm(String id);
    /**
     * 导入表单
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/7/1
     */
    ActionResult ImportData(FlowFormEntity entity, String type) throws WorkFlowException;

    /**
     * 获取表单流程引擎
     * @param flowId
     * @return
     */
    List<FlowFormEntity> getFlowIdList(String flowId);

    /**
     * 获取流程信息
     * @param id
     * @return
     */
    FlowTempInfoModel getFormById(String id) throws WorkFlowException;

    /**
     * 修改流程的引擎id
     * @param entity
     */
    void updateForm(FlowFormEntity entity);

    void saveLogicFlowAndForm(String id);
}
