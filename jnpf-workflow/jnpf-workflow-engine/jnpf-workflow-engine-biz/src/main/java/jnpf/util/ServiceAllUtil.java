package jnpf.util;

import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.util.SentMessageUtil;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.engine.model.flowtemplate.FlowExportModel;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.message.model.SentMessageForm;
import jnpf.message.service.SendMessageConfigService;
import jnpf.model.flow.FlowFormDataModel;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorModel;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.service.FlowFormRelationService;
import jnpf.service.FlowFormService;
import jnpf.service.FormDataService;
import jnpf.util.enums.DictionaryDataEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/9 13:28
 */
@Component
public class ServiceAllUtil {

    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private OrganizeAdministratorService organizeAdminTratorService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private SentMessageUtil sentMessageUtil;
    @Autowired
    private SendMessageConfigService sendMessageConfigService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private FlowFormDataUtil flowDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private FlowFormRelationService flowFormRelationService;

    //--------------------------------数据字典------------------------------
    public List<DictionaryDataEntity> getDiList() {
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getList(dictionaryTypeService.getInfoByEnCode(DictionaryDataEnum.FLOWWOEK_ENGINE.getDictionaryTypeId()).getId());
        return dictionList;
    }

    public List<DictionaryDataEntity> getDictionName(List<String> id) {
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getDictionName(id);
        return dictionList;
    }

    //--------------------------------用户关系表------------------------------
    public List<UserRelationEntity> getListByUserIdAll(List<String> id) {
        List<UserRelationEntity> list = userRelationService.getListByUserIdAll(id).stream().filter(t -> StringUtil.isNotEmpty(t.getObjectId())).collect(Collectors.toList());
        return list;
    }

    public List<UserRelationEntity> getListByObjectIdAll(List<String> id) {
        List<UserRelationEntity> list = userRelationService.getListByObjectIdAll(id);
        return list;
    }

    public String getAdmin() {
        UserEntity admin = userService.getUserByAccount("admin");
        return admin.getId();
    }

    //--------------------------------用户------------------------------
    public List<UserEntity> getUserName(List<String> id) {
        List<UserEntity> list = getUserName(id, false);
        return list;
    }

    public List<UserEntity> getListByManagerId(String managerId) {
        List<UserEntity> list = StringUtil.isNotEmpty(managerId) ? userService.getListByManagerId(managerId, null) : new ArrayList<>();
        return list;
    }

    public List<UserEntity> getUserName(List<String> id, boolean enableMark) {
        List<UserEntity> list = userService.getUserName(id);
        if (enableMark) list = list.stream().filter(t -> t.getEnabledMark() != 0).collect(Collectors.toList());
        return list;
    }

    public List<UserEntity> getUserName(List<String> id, Pagination pagination) {
        List<UserEntity> list = userService.getUserName(id, pagination);
        return list;
    }

    public UserEntity getUserInfo(String id) {
        UserEntity entity = null;
        if (StringUtil.isNotEmpty(id))
            entity = id.equalsIgnoreCase("admin") ? userService.getUserByAccount(id) : userService.getInfo(id);
        return entity;
    }

    public List<String> getUserListAll(List<String> idList) {
        List<String> userIdList = userService.getUserIdList(idList, null);
        return userIdList;
    }

    public List<String> getOrganizeUserList(String type) {
        OrganizeAdministratorModel model = organizeAdminTratorService.getOrganizeAdministratorList();
        Map<String, List<String>> map = new HashMap<>();
        map.put("select", model.getSelectList());
        map.put("add", model.getAddList());
        map.put("delete", model.getDeleteList());
        map.put("edit", model.getEditList());
        List<String> list = map.get(type) != null ? map.get(type) : new ArrayList<>();
        List<String> userList = userRelationService.getListByObjectIdAll(list).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
        return userList;
    }

    //--------------------------------单据规则------------------------------

    public void useBillNumber(String enCode) {
        billRuleService.useBillNumber(enCode);
    }

    //--------------------------------角色------------------------------
    public List<RoleEntity> getListByIds(List<String> id) {
        List<RoleEntity> list = roleService.getListByIds(id, null, false);
        return list;
    }

    //--------------------------------组织------------------------------
    public List<OrganizeEntity> getOrganizeName(List<String> id) {
        List<OrganizeEntity> list = organizeService.getOrganizeName(id);
        return list;
    }

    public OrganizeEntity getOrganizeInfo(String id) {
        OrganizeEntity entity = StringUtil.isNotEmpty(id) ? organizeService.getInfo(id) : null;
        return entity;
    }

    public List<OrganizeEntity> getOrganizeId(String organizeId) {
        List<OrganizeEntity> organizeList = organizeService.getOrganizeId(organizeId);
        Collections.reverse(organizeList);
        return organizeList;
    }

    public List<OrganizeEntity> getDepartmentAll(String organizeId) {
        List<OrganizeEntity> departmentAll = organizeService.getDepartmentAll(organizeId);
        return departmentAll;
    }

    //--------------------------------岗位------------------------------
    public List<PositionEntity> getPositionName(List<String> id) {
        List<PositionEntity> list = positionService.getPositionName(id, false);
        return list;
    }

    //--------------------------------远端------------------------------
    public ActionResult infoToId(String interId, Map<String, String> parameterMap) {
        return dataInterfaceService.infoToIdById(interId, parameterMap);
    }

    public List<DataInterfaceEntity> getInterfaceList(List<String> id){
        return dataInterfaceService.getList(id);
    }


    //--------------------------------发送消息------------------------------
    public void sendMessage(List<SentMessageForm> messageListAll) {
        for (SentMessageForm messageForm : messageListAll)
            if (messageForm.isSysMessage()) sentMessageUtil.sendMessage(messageForm);
    }

    public void updateSendConfigUsed(String id, List<String> idList) {
        sendMessageConfigService.updateUsed(id, idList);
    }

    public void sendDelegateMsg(List<SentMessageForm> messageListAll) {
        for (SentMessageForm messageForm : messageListAll) {
            sentMessageUtil.sendDelegateMsg(messageForm);
        }
    }

    //------------------------------导出-------------------------------
    public DownloadVO exportData(FlowExportModel model){
        DownloadVO downloadVO = fileExport.exportFile(model, configValueUtil.getTemporaryFilePath(), model.getFlowTemplate().getFullName(), ModuleTypeEnum.FLOW_FLOWENGINE.getTableName());
        return downloadVO;
    }

    //------------------------------表单数据-------------------------------
    public void createOrUpdate(String formId, String id, Map<String, Object> map) throws WorkFlowException {
        createOrUpdateDelegateUser(formId, id, map, null);
    }
    public void createOrUpdate(FlowFormDataModel flowFormDataModel) throws WorkFlowException {
        formDataService.saveOrUpdate(flowFormDataModel);
    }
    public void createOrUpdateDelegateUser(String formId, String id, Map<String, Object> map, UserEntity delegetUser) throws WorkFlowException {
        formDataService.saveOrUpdate(formId, id, map, delegetUser);
    }

    public Map<String, Object> infoData(String formId, String id) throws WorkFlowException {
        Map<String, Object> dataAll = new HashMap<>();
        if (StringUtil.isNotEmpty(formId) && StringUtil.isNotEmpty(id)) {
            Map<String, Object> info = new HashMap<>();
            ActionResult result = formDataService.info(formId, id);
            if (result.getCode() != 200) {
                throw new WorkFlowException(result.getMsg());
            }
            if (result.getData() instanceof Map) {
                info.putAll((Map) result.getData());
            }
            Map<String, Object> data = new HashMap(16) {{
                putAll(info);
            }};
            for (String key : info.keySet()) {
                if (info.get(key) instanceof Map) {
                    Map<String, Object> mastTableMap = (Map<String, Object>) info.get(key);
                    for (String mastKey : mastTableMap.keySet()) {
                        data.put("jnpf_" + key + "_jnpf_" + mastKey, mastTableMap.get(mastKey));
                    }
                }
            }
            dataAll.putAll(data);
        }
        return dataAll;
    }

    //------------------------------表单对象-------------------------------
    public FlowFormEntity getForm(String id) {
        FlowFormEntity form = StringUtil.isNotEmpty(id) ? flowFormService.getById(id) : null;
        return form;
    }

    public List<FlowFormEntity> getFlowIdList(String id) {
        List<FlowFormEntity> list = StringUtil.isNotEmpty(id) ? flowFormService.getFlowIdList(id) : new ArrayList<>();
        return list;
    }

    public void updateForm(FlowFormEntity entity) {
        flowFormService.updateForm(entity);
    }

    public void formIdList(List<String> formId, String tempJsonId) {
        flowFormRelationService.saveFlowIdByFormIds(tempJsonId, formId);
    }

    public void deleteFormId(String tempJsonId) {
        flowFormRelationService.saveFlowIdByFormIds(tempJsonId, new ArrayList<>());
    }

}
