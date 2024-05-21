package jnpf.service.impl;

import jnpf.base.ActionResult;
import jnpf.base.service.DbLinkService;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.FlowFormDataModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.permission.entity.UserEntity;
import jnpf.service.FlowFormService;
import jnpf.service.FormDataService;
import jnpf.util.FlowFormCustomUtils;
import jnpf.util.FlowFormDataUtil;
import jnpf.util.FlowFormHttpReqUtils;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class FlowDataServiceImpl implements FormDataService {
    @Autowired
    private FlowFormCustomUtils flowFormCustomUtils;
    @Autowired
    private FlowFormHttpReqUtils flowFormHttpReqUtils;

    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private DbLinkService dblinkService;
    @Autowired
    private FlowFormDataUtil flowDataUtil;

    @Override
    public void create(String formId, String id, Map<String, Object> map) throws WorkFlowException {
        FlowFormEntity flowFormEntity = flowFormService.getById(formId);
        //判断是否为系统表单
        boolean b = flowFormEntity.getFormType() == 1;
        if (b) {
            flowFormHttpReqUtils.create(flowFormEntity, id, UserProvider.getToken(), map);
        } else {
            flowFormCustomUtils.create(flowFormEntity, id, map, null, null);
        }
    }

    @Override
    public void update(String formId, String id, Map<String, Object> map) throws WorkFlowException, SQLException, DataException {
        FlowFormEntity flowFormEntity = flowFormService.getById(formId);
        //判断是否为系统表单
        boolean b = flowFormEntity.getFormType() == 1;
        if (b) {
            flowFormHttpReqUtils.update(flowFormEntity, id, UserProvider.getToken(), map);
        } else {
            flowFormCustomUtils.update(flowFormEntity, id, map, null);
        }
    }



    @Override
    public void saveOrUpdate(FlowFormDataModel flowFormDataModel) throws WorkFlowException {
        String id = flowFormDataModel.getId();
        String formId = flowFormDataModel.getFormId();
        Map<String, Object> map = flowFormDataModel.getMap();
        List<Map<String, Object>> formOperates = flowFormDataModel.getFormOperates();
        FlowFormEntity flowFormEntity = flowFormService.getById(formId);
        Integer formType = flowFormEntity.getFormType();
        if(map.get(TableFeildsEnum.VERSION.getField().toUpperCase())!=null){//针对Oracle数据库大小写敏感，出现大写字段补充修复
            map.put(TableFeildsEnum.VERSION.getField(),map.get(TableFeildsEnum.VERSION.getField().toUpperCase()));
        }
        //系统表单
        if (formType == 1){
            map.put("formOperates",formOperates);
            flowFormHttpReqUtils.saveOrUpdate(flowFormEntity,id,UserProvider.getToken(),map);
        } else {
            try {
                flowFormCustomUtils.saveOrUpdate(flowFormEntity,flowFormDataModel);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveOrUpdate(String formId, String id, Map<String, Object> map, UserEntity delegateUser) throws WorkFlowException {
        FlowFormEntity flowFormEntity = flowFormService.getById(formId);
        Integer formType = flowFormEntity.getFormType();
        if(map.get(TableFeildsEnum.VERSION.getField().toUpperCase())!=null){//针对Oracle数据库大小写敏感，出现大写字段补充修复
            map.put(TableFeildsEnum.VERSION.getField(),map.get(TableFeildsEnum.VERSION.getField().toUpperCase()));
        }
        //系统表单
        if (formType == 1) {
            flowFormHttpReqUtils.saveOrUpdate(flowFormEntity, id, UserProvider.getToken(), map);
        } else {
            try {
                flowFormCustomUtils.saveOrUpdate(flowFormEntity, id, map, delegateUser);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean delete(String formId, String id) throws Exception {
        FlowFormEntity flowFormEntity = flowFormService.getById(formId);
        List<TableModel> tableModels = JsonUtil.getJsonToList(flowFormEntity.getTableJson(), TableModel.class);
        FormDataModel formData = JsonUtil.getJsonToBean(flowFormEntity.getPropertyJson(), FormDataModel.class);
        Integer primaryKeyPolicy = formData.getPrimaryKeyPolicy();
        DbLinkEntity linkEntity = StringUtil.isNotEmpty(flowFormEntity.getDbLinkId()) ? dblinkService.getInfo(flowFormEntity.getDbLinkId()) : null;
        flowDataUtil.deleteTable(id, primaryKeyPolicy, tableModels, linkEntity);
        return true;
    }

    @Override
    public ActionResult info(String formId, String id){
        ActionResult result = new ActionResult();
        Map<String, Object> allDataMap = new HashMap();
        FlowFormEntity flowFormEntity = flowFormService.getById(formId);
        result.setCode(flowFormEntity==null?400:200);
        result.setMsg(flowFormEntity==null?"表单信息不存在":"");
        if(flowFormEntity!=null){
            //判断是否为系统表单
            boolean b = flowFormEntity.getFormType() == 1;
            if (b) {
                allDataMap.putAll(flowFormHttpReqUtils.info(flowFormEntity, id, UserProvider.getToken()));
            } else {
                allDataMap.putAll(flowFormCustomUtils.info(flowFormEntity, id));
            }
        }
        result.setData(allDataMap);
        return result;
    }
}
