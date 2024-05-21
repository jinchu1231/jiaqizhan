package jnpf.engine.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.engine.model.flowbefore.FlowTemplateAllModel;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.service.FlowDynamicService;
import jnpf.engine.service.FlowTaskNewService;
import jnpf.engine.util.FlowContextHolder;
import jnpf.engine.util.FlowTaskUtil;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.FlowFormDataModel;
import jnpf.permission.entity.UserEntity;
import jnpf.util.FlowFormConstant;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 在线开发工作流
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:19
 */
@Slf4j
@Service
public class FlowDynamicServiceImpl implements FlowDynamicService {


    @Autowired
    public ServiceAllUtil serviceUtil;
    @Autowired
    private FlowTaskUtil flowTaskUtil;
    @Autowired
    private FlowTaskNewService flowTaskNewService;

    @Override
    public void flowTask(FlowModel flowModel, FlowStatusEnum flowStatus, ChildNode childNode) throws WorkFlowException {
        Map<String, Object> formData = flowModel.getFormData();
        String id = flowModel.getProcessId();
        Map<String, Object> map = flowModel.getFormData();
        formData.put(FlowFormConstant.FLOWID, flowModel.getFlowId());
        String formId = childNode.getProperties().getFormId();
        List<Map<String, Object>> formOperates = childNode.getProperties().getFormOperates();
        FlowFormDataModel formDataModel = FlowFormDataModel.builder().
                formId(formId).id(id).delegateUser(serviceUtil.getUserInfo(flowModel.getUserId())).
                map(map).formOperates(formOperates).build();
        switch (flowStatus) {
            case save:
                flowTaskNewService.save(flowModel);
                serviceUtil.createOrUpdate(formDataModel);
                break;
            case submit:
                FlowContextHolder.addData(formId, formData);
                FlowContextHolder.addChildData(id, formId, formData);
                FlowContextHolder.addFormOperates(id, formId, formOperates);
                flowTaskNewService.submitAll(flowModel);
                break;
            case none:
                serviceUtil.createOrUpdate(formDataModel);
                break;
            default:
                break;
        }
    }

    @Override
    public void createOrUpdate(FlowModel flowModel) throws WorkFlowException {
        FlowTemplateAllModel model = flowTaskUtil.templateJson(flowModel.getFlowId());
        FlowTemplateJsonEntity templateJson = model.getTemplateJson();
        FlowTemplateEntity template = model.getTemplate();
        ChildNode childNode = JsonUtil.getJsonToBean(templateJson.getFlowTemplateJson(), ChildNode.class);
        FlowStatusEnum statusEnum = FlowStatusEnum.submit.getMessage().equals(flowModel.getStatus()) ? FlowStatusEnum.submit :
                template.getType() == 0 ? FlowStatusEnum.save : FlowStatusEnum.none;
        flowTask(flowModel, statusEnum, childNode);
    }

    @Override
    @DSTransactional
    public void batchCreateOrUpdate(FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        List<String> batchUserId = flowModel.getDelegateUserList();
        boolean isBatchUser = batchUserId.size() == 0;
        if (isBatchUser) {
            batchUserId.add(userInfo.getUserId());
        }
        for (String id : batchUserId) {
            FlowModel model = JsonUtil.getJsonToBean(flowModel, FlowModel.class);
            model.setDelegateUser(isBatchUser ? model.getDelegateUser() : userInfo.getUserId());
            model.setProcessId(StringUtil.isNotEmpty(model.getId()) ? model.getId() : RandomUtil.uuId());
            if (!isBatchUser) {
                UserEntity userEntity = serviceUtil.getUserInfo(id);
                if (userEntity != null) {
                    UserInfo info = new UserInfo();
                    info.setUserName(userEntity.getRealName());
                    info.setUserId(userEntity.getId());
                    model.setUserInfo(info);
                }
            }
            model.setUserId(id);
            createOrUpdate(model);
        }
    }


}
