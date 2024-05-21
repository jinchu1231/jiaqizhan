package jnpf.base.util;

import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.constant.MsgCode;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.model.flowtemplate.FlowTemplateCrForm;
import jnpf.engine.model.flowtemplate.FlowTemplateInfoVO;
import jnpf.engine.service.FlowTemplateService;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.service.FlowFormService;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.enums.DictionaryDataEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 在线开发流程及表单相关方法
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/12/29 11:04:45
 */
@Component
@Slf4j
public class VisualFlowFormUtil {
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private UserProvider userProvider;

    /**
     * 修改流程基本信息及状态
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public ActionResult saveOrUpdateFlowTemp(VisualdevEntity entity, Integer state, Boolean isSave) throws WorkFlowException {
        ActionResult result;
        FlowTemplateCrForm flowTemplateCrForm = new FlowTemplateCrForm();
        BeanUtils.copyProperties(entity, flowTemplateCrForm);
        flowTemplateCrForm.setFullName(entity.getFullName());
        flowTemplateCrForm.setEnCode(entity.getEnCode());
        flowTemplateCrForm.setId(entity.getId());
        flowTemplateCrForm.setType(OnlineDevData.FLOW_TYPE_DEV);
        flowTemplateCrForm.setFormId(entity.getId());
        flowTemplateCrForm.setCategory(this.categaryMapping(dictionaryDataService, entity.getCategory()));
        FlowTemplateInfoVO byId= flowTemplateService.info(entity.getId());
        if (byId==null) {
            flowTemplateCrForm.setEnabledMark(OnlineDevData.STATE_DISABLE);
            result = flowTemplateService.createTemplate(flowTemplateCrForm);
        } else {
            if(Objects.equals(state,OnlineDevData.STATE_ENABLE)){
                flowTemplateCrForm.setEnabledMark(OnlineDevData.STATE_ENABLE);
            }
            result = flowTemplateService.updateTemplate(entity.getId(),flowTemplateCrForm);
        }
        return result;
    }

    /**
     * 保存或修改流程表单信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public void saveOrUpdateForm(VisualdevEntity entity, int enabledMark, boolean isSave) throws WorkFlowException {
        String userId = userProvider.get().getUserId();
        FlowFormEntity flowFormEntity = Optional.ofNullable(flowFormService.getById(entity.getId())).orElse(new FlowFormEntity());
        flowFormEntity.setId(entity.getId());
//        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        flowFormEntity.setEnCode(entity.getEnCode());
        flowFormEntity.setFullName(entity.getFullName());
        //功能流程（在线开发-自定义表单-隐藏）
        flowFormEntity.setFlowType(OnlineDevData.FLOW_TYPE_DEV);
        flowFormEntity.setFormType(OnlineDevData.FORM_TYPE_DEV);
        if (entity.getType() == 4) {//功能系统表单，代码生成-功能系统表单
            flowFormEntity.setFormType(OnlineDevData.FORM_TYPE_SYS);
        }
        if (entity.getType() == 3) {//发起系统表单，代码生成-发起系统表单
            flowFormEntity.setFlowType(OnlineDevData.FLOW_TYPE_FLOW);
            flowFormEntity.setFormType(OnlineDevData.FORM_TYPE_SYS);
        }
        flowFormEntity.setCategory(entity.getCategory());
        flowFormEntity.setPropertyJson(entity.getFormData());
        flowFormEntity.setDescription(entity.getDescription());
        flowFormEntity.setSortCode(entity.getSortCode());
        flowFormEntity.setEnabledMark(enabledMark);
        if (isSave) {
            flowFormEntity.setCreatorTime(new Date());
            flowFormEntity.setCreatorUserId(userId);
        } else {

            flowFormEntity.setLastModifyTime(new Date());
            flowFormEntity.setLastModifyUserId(userId);
        }
        flowFormEntity.setTableJson(entity.getVisualTables());
        flowFormEntity.setDbLinkId(entity.getDbLinkId());
        flowFormEntity.setFlowId(entity.getId());

        //判断名称是否重复
        if (flowFormService.isExistByFullName(flowFormEntity.getFullName(), flowFormEntity.getId())) {
            throw new WorkFlowException(MsgCode.EXIST001.get());
        }
        //判断编码是否重复
        if (flowFormService.isExistByEnCode(flowFormEntity.getEnCode(), flowFormEntity.getId())) {
            throw new WorkFlowException(MsgCode.EXIST002.get());
        }
        try {
            flowFormService.saveOrUpdate(flowFormEntity);
        }catch (Exception e){
            throw new WorkFlowException(e.getMessage());
        }
    }

    /**
     * 删除流程引擎信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public void deleteTemplateInfo(String id) {
        String msg = "";
        try {
            FlowTemplateEntity entity = flowTemplateService.getInfo(id);
            flowTemplateService.delete(entity);
        } catch (Exception e) {
            msg = e.getMessage();
        }
    }

    /**
     * 获取流程引擎信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public FlowTemplateInfoVO getTemplateInfo(String id) {
        FlowFormEntity byId = flowFormService.getById(id);
        FlowTemplateInfoVO vo = new FlowTemplateInfoVO();
        try {
            vo = flowTemplateService.info(byId.getFlowId());
        } catch (Exception e) {
            vo = null;
        }
        return vo;
    }

    /**
     * 获取字典相关列表
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public List<DictionaryDataEntity> getListByTypeDataCode(Integer type) {
        return dictionaryDataService.getListByTypeDataCode(DictionaryDataEnum.getTypeId(type));
    }

    /**
     * 获取字典数据
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public DictionaryDataEntity getdictionaryDataInfo(String category) {
        return dictionaryDataService.getInfo(category);
    }

    /**
     * 将在线开发分类字段转换成流程分类字段id
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/28
     */
    public static String categaryMapping(DictionaryDataService dictionaryDataService, String devCateId) {
        //流程分类
        String flowCateId = "";
        try {
            List<DictionaryDataEntity> flowDictionList = dictionaryDataService.getListByTypeDataCode(DictionaryDataEnum.FLOWWOEK_ENGINE.getDictionaryTypeId());
            List<DictionaryDataEntity> devDictionList = dictionaryDataService.getListByTypeDataCode(DictionaryDataEnum.VISUALDEV.getDictionaryTypeId());
            for (DictionaryDataEntity devItem : devDictionList) {
                if (devItem.getId().equals(devCateId)) {
                    for (DictionaryDataEntity flowItem : flowDictionList) {
                        if (flowItem.getEnCode().equals(devItem.getEnCode())) {
                            flowCateId = flowItem.getId();
                        }
                        if (StringUtil.isEmpty(flowCateId) && OnlineDevData.DEFAULT_CATEGATY_ENCODE.equals(flowItem.getEnCode())) {//没值，给默认
                            flowCateId = flowItem.getId();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("分类字段转换失败！:{}", e.getMessage());
        }
        return flowCateId;
    }

    /**
     * 删除流程引擎信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public void deleteFlowForm(String id) {
        try {
            flowFormService.removeById(id);
        } catch (Exception e) {
        }
    }

    /**
     * 逻辑删除恢复流程和表单
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/12/29
     */
    public void saveLogicFlowAndForm(VisualdevEntity entity) throws WorkFlowException {
        flowFormService.saveLogicFlowAndForm(entity.getId());
        flowTemplateService.saveLogicFlowAndForm(entity.getId());
        this.saveOrUpdateForm(entity, OnlineDevData.STATE_ENABLE, false);
        this.saveOrUpdateFlowTemp(entity, OnlineDevData.STATE_DISABLE, false);
    }

}
