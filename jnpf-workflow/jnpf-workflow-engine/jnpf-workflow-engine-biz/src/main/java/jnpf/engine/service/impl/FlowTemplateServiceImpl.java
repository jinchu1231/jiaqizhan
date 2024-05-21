package jnpf.engine.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.ImmutableList;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.mapper.FlowTemplateMapper;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.engine.model.flowengine.PaginationFlowEngine;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.MsgConfig;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.engine.model.flowtemplate.FlowExportModel;
import jnpf.engine.model.flowtemplate.FlowTemplateCrForm;
import jnpf.engine.model.flowtemplate.FlowTemplateInfoVO;
import jnpf.engine.model.flowtemplate.FlowTemplateListVO;
import jnpf.engine.model.flowtemplate.FlowTemplateVO;
import jnpf.engine.model.flowtemplatejson.FlowJsonModel;
import jnpf.engine.model.flowtemplatejson.FlowTemplateJsonPage;
import jnpf.engine.service.FlowEngineVisibleService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateJsonService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.engine.util.FlowJsonUtil;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.permission.entity.UserEntity;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022年7月11日 上午9:18
 */
@Service
public class FlowTemplateServiceImpl extends SuperServiceImpl<FlowTemplateMapper, FlowTemplateEntity> implements FlowTemplateService {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private FlowEngineVisibleService flowEngineVisibleService;
    @Autowired
    private FlowTemplateJsonService flowTemplateJsonService;

    @Override
    public List<FlowTemplateEntity> getPageList(FlowPagination pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().and(
                    t -> t.like(FlowTemplateEntity::getFullName, pagination.getKeyword())
                            .or().like(FlowTemplateEntity::getEnCode, pagination.getKeyword())
            );
        }
        if (ObjectUtil.isNotEmpty(pagination.getCategory())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getCategory, pagination.getCategory());
        }
        if (ObjectUtil.isNotEmpty(pagination.getType())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getType, pagination.getType());
        }
        if (ObjectUtil.isNotEmpty(pagination.getEnabledMark())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getEnabledMark, pagination.getEnabledMark());
        }
        //排序
        queryWrapper.lambda().orderByAsc(FlowTemplateEntity::getSortCode).orderByDesc(FlowTemplateEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(FlowTemplateEntity::getLastModifyTime);
        }
        Page<FlowTemplateEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<FlowTemplateEntity> userPage = this.page(page, queryWrapper);
        return pagination.setData(userPage.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTemplateEntity> getList(PaginationFlowEngine pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(pagination.getType())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getType, pagination.getType());
        }
        if (ObjectUtil.isNotEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().like(FlowTemplateEntity::getFullName, pagination.getKeyword());
        }
        if (ObjectUtil.isNotEmpty(pagination.getEnabledMark())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getEnabledMark, pagination.getEnabledMark());
        }
        //排序
        queryWrapper.lambda().orderByAsc(FlowTemplateEntity::getSortCode).orderByDesc(FlowTemplateEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(FlowTemplateEntity::getLastModifyTime);
        }
        queryWrapper.lambda().select(
                FlowTemplateEntity::getId, FlowTemplateEntity::getEnCode,
                FlowTemplateEntity::getFullName,
                FlowTemplateEntity::getType, FlowTemplateEntity::getIcon,
                FlowTemplateEntity::getCategory, FlowTemplateEntity::getIconBackground,
                FlowTemplateEntity::getSortCode, FlowTemplateEntity::getEnabledMark,
                FlowTemplateEntity::getCreatorTime, FlowTemplateEntity::getCreatorUserId
        );
        return this.list(queryWrapper);
    }

    @Override
    public FlowTemplateEntity getInfo(String id) throws WorkFlowException {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getId, id);
        FlowTemplateEntity FlowTemplateEntity = this.getOne(queryWrapper);
        if (FlowTemplateEntity == null) {
            throw new WorkFlowException(MsgCode.WF113.get());
        }
        return FlowTemplateEntity;
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getFullName, fullName);
        queryWrapper.lambda().eq(FlowTemplateEntity::getType, 0);
        if (!ObjectUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(FlowTemplateEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getEnCode, enCode);
        if (!ObjectUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(FlowTemplateEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    @DSTransactional
    public void create(FlowTemplateEntity entity, List<FlowTemplateJsonEntity> templateJsonList) throws WorkFlowException {
        if (isExistByFullName(entity.getFullName(), entity.getId())) {
            throw new WorkFlowException("流程名称不能重复");
        }
        if (isExistByEnCode(entity.getEnCode(), entity.getId())) {
            throw new WorkFlowException("流程编码不能重复");
        }
        boolean formType = entity.getType() == 1;
        UserInfo userInfo = userProvider.get();
        entity.setId(StringUtil.isNotEmpty(entity.getId()) ? entity.getId() : RandomUtil.uuId());
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(new Date());
        entity.setEnabledMark(0);
        entity.setLastModifyUserId(null);
        entity.setLastModifyTime(null);
        this.setIgnoreLogicDelete().removeById(entity.getId());
        this.setIgnoreLogicDelete().saveOrUpdate(entity);
        this.clearIgnoreLogicDelete();
        List<String> tempIdList = ImmutableList.of(entity.getId());
        List<FlowTemplateJsonEntity> templateList = flowTemplateJsonService.getTemplateList(tempIdList);
        Set<String> formIdList = new HashSet<>();
        //保存引擎数据
        for (int k = 0; k < templateJsonList.size(); k++) {
            FlowTemplateJsonEntity templateJson = templateJsonList.get(k);
            templateJson.setId(StringUtil.isNotEmpty(templateJson.getId()) ? templateJson.getId() : RandomUtil.uuId());
            templateJson.setTemplateId(entity.getId());
            templateJson.setGroupId(StringUtil.isNotEmpty(templateJson.getGroupId()) ? templateJson.getGroupId() : RandomUtil.uuId());
            List<FlowEngineVisibleEntity> visibleList = visibleList(templateJson, formType, formIdList);
            flowEngineVisibleService.deleteVisible(templateJson.getId());
            int version = templateList.stream().filter(t -> t.getGroupId().equals(templateJson.getGroupId())).map(FlowTemplateJsonEntity::getVersion).mapToInt(Integer::parseInt).max().orElse(0) + 1;
            templateJson.setVersion(version + "");
            templateJson.setSortCode(Long.parseLong(k + ""));
            templateJson.setVisibleType(visibleList.size() == 0 ? 0 : 1);
            templateJson.setEnabledMark(1);
            flowTemplateJsonService.setIgnoreLogicDelete().removeById(templateJson.getId());
            flowTemplateJsonService.setIgnoreLogicDelete().saveOrUpdate(templateJson);
            flowTemplateJsonService.clearIgnoreLogicDelete();
            for (int i = 0; i < visibleList.size(); i++) {
                FlowEngineVisibleEntity visibleEntity = visibleList.get(i);
                visibleEntity.setId(RandomUtil.uuId());
                visibleEntity.setFlowId(templateJson.getId());
                visibleEntity.setSortCode(Long.parseLong(i + ""));
                flowEngineVisibleService.save(visibleEntity);
            }
        }
        if (formType && formIdList.size() > 1) {
            throw new WorkFlowException("流程表单不一致，请重新选择。");
        }
        serviceUtil.formIdList(new ArrayList<>(formIdList), entity.getId());
    }

    @Override
    public void create(FlowTemplateEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(StringUtil.isNotEmpty(entity.getId()) ? entity.getId() : RandomUtil.uuId());
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(new Date());
        entity.setEnabledMark(0);
        this.saveOrUpdate(entity);
    }

    @Override
    public FlowTemplateInfoVO info(String id) throws WorkFlowException {
        FlowTemplateEntity flowEntity = getInfo(id);
        FlowTemplateInfoVO vo = JsonUtil.getJsonToBean(flowEntity, FlowTemplateInfoVO.class);
        FlowTemplateJsonPage page = new FlowTemplateJsonPage();
        page.setTemplateId(id);
        List<FlowTemplateJsonEntity> listAll = flowTemplateJsonService.getListPage(page, false);
        List<FlowTemplateJsonEntity> list = flowTemplateJsonService.getMainList(ImmutableList.of(id));
        List<FlowTaskEntity> flowTaskList = flowTaskService.getTemplateIdList(id);
        List<FlowJsonModel> templateList = new ArrayList<>();
        for (FlowTemplateJsonEntity templateJson : list) {
            FlowJsonModel model = new FlowJsonModel();
            model.setFlowId(templateJson.getId());
            model.setId(templateJson.getId());
            model.setFullName(templateJson.getFullName());
            model.setFlowTemplateJson(JsonUtil.stringToMap(templateJson.getFlowTemplateJson()));
            List<String> flowIdList = listAll.stream().filter(t -> templateJson.getGroupId().equals(t.getGroupId())).map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
            boolean isDelete = flowTaskList.stream().filter(t -> flowIdList.contains(t.getFlowId())).count() > 0;
            model.setIsDelete(isDelete);
            templateList.add(model);
        }
        //判断是否是在线开发同步过来的流程
        List<FlowFormEntity> formList = serviceUtil.getFlowIdList(id);
        String onlineFormId = "";
        if (formList.size() > 0) {
            FlowFormEntity form = formList.get(0);
            if (form != null && form.getFlowType() == 1 && form.getFormType() == 2) {
                onlineFormId = form.getId();
            }
        }
        vo.setOnlineDev(StringUtil.isNotEmpty(onlineFormId));
        vo.setOnlineFormId(onlineFormId);
        vo.setFlowTemplateJson(JsonUtil.getObjectToString(templateList));
        return vo;
    }

    @Override
    @DSTransactional
    public FlowTemplateVO updateVisible(String id, FlowTemplateEntity entity, List<FlowTemplateJsonEntity> templateJsonList) throws WorkFlowException {
        if (isExistByFullName(entity.getFullName(), id)) {
            throw new WorkFlowException("流程名称不能重复");
        }
        if (isExistByEnCode(entity.getEnCode(), id)) {
            throw new WorkFlowException("流程编码不能重复");
        }
        boolean formType = entity.getType() == 1;
        UserInfo userInfo = userProvider.get();
        FlowTemplateVO vo = new FlowTemplateVO();
        List<String> listVO = new ArrayList<>();
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userInfo.getUserId());
        this.saveOrUpdate(entity);
        //删除没有用到的流程
        List<FlowTemplateJsonEntity> templateList = flowTemplateJsonService.getMainList(ImmutableList.of(entity.getId()));
        for (int i = 0; i < templateList.size(); i++) {
            FlowTemplateJsonEntity templateJson = templateList.get(i);
            List<FlowTaskEntity> flowList = flowTaskService.getFlowList(templateJson.getId());
            if (flowList.size() > 0) {
                templateList.remove(i);
            }
        }
        List<String> tempJsonIdList = templateList.stream().map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
        List<String> templateJsonId = templateJsonList.stream().filter(t -> StringUtil.isNotEmpty(t.getId())).map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
        tempJsonIdList.removeAll(templateJsonId);
        for (String tempJsonId : tempJsonIdList) {
            FlowTemplateJsonEntity delTemplateJson = flowTemplateJsonService.getInfo(tempJsonId);
            flowEngineVisibleService.deleteVisible(delTemplateJson.getId());
            flowTemplateJsonService.delete(delTemplateJson);
        }
        Set<String> formIdList = new HashSet<>();
        //保存引擎数据
        for (int k = 0; k < templateJsonList.size(); k++) {
            FlowTemplateJsonEntity templateJson = templateJsonList.get(k);
            boolean isTempId = StringUtil.isNotEmpty(templateJson.getId());
            String jsonId = isTempId ? templateJson.getId() : RandomUtil.uuId();
            //json变化才新增版本，true=变化
            FlowTemplateJsonEntity info = isTempId ? flowTemplateJsonService.getInfo(jsonId) : null;
            boolean ischange = info != null && StringUtil.isNotEmpty(info.getFlowTemplateJson()) && !info.getFlowTemplateJson().equals(templateJson.getFlowTemplateJson());
            //判断流程任务是否被使用
            List<FlowTaskEntity> flowList = flowTaskService.getFlowList(jsonId);
            boolean isRand = flowList.size() > 0 && ischange;
            templateJson.setId(isRand ? RandomUtil.uuId() : jsonId);
            templateJson.setTemplateId(id);
            templateJson.setGroupId(isTempId ? info.getGroupId() : RandomUtil.uuId());
            int version = 1;
            //判断是否在使用,新增版本
            if (isRand) {
                version = flowTemplateJsonService.getTemplateList(ImmutableList.of(id)).stream().filter(t -> t.getGroupId().equals(templateJson.getGroupId())).map(FlowTemplateJsonEntity::getVersion).mapToInt(Integer::parseInt).max().orElse(0) + 1;
                listVO.add(templateJson.getId());
            }
            List<FlowEngineVisibleEntity> visibleList = visibleList(templateJson, formType, formIdList);
            flowEngineVisibleService.deleteVisible(templateJson.getId());
            templateJson.setVisibleType(visibleList.size() == 0 ? 0 : 1);
            templateJson.setSortCode(Long.parseLong(k + ""));
            templateJson.setEnabledMark(isRand ? 0 : isTempId ? info.getEnabledMark() : 1);
            templateJson.setVersion(isRand ? version + "" : isTempId ? info.getVersion() : version + "");
            flowTemplateJsonService.saveOrUpdate(templateJson);
            for (int i = 0; i < visibleList.size(); i++) {
                FlowEngineVisibleEntity visibleEntity = visibleList.get(i);
                visibleEntity.setId(RandomUtil.uuId());
                visibleEntity.setFlowId(templateJson.getId());
                visibleEntity.setSortCode(Long.parseLong(i + ""));
                flowEngineVisibleService.save(visibleEntity);
            }
            flowTemplateJsonService.updateFullName(templateJson.getGroupId(), templateJson.getFullName());
        }
        vo.setIsMainVersion(listVO.size() > 0);
        vo.setId(String.join(",", listVO));
        if (formType && formIdList.size() > 1) {
            throw new WorkFlowException("流程表单不一致，请重新选择。");
        }
        serviceUtil.formIdList(new ArrayList<>(formIdList), entity.getId());
        return vo;
    }

    @Override
    public boolean update(String id, FlowTemplateEntity entity) {
        entity.setId(id);
        boolean flag = this.updateById(entity);
        return flag;
    }

    @Override
    @DSTransactional
    public void copy(FlowTemplateEntity entity, List<FlowTemplateJsonEntity> templateJsonEntity) throws WorkFlowException {
        try {
            String copyNum = UUID.randomUUID().toString().substring(0, 5);
            entity.setFullName(entity.getFullName() + ".副本" + copyNum);
            entity.setEnCode(entity.getEnCode() + copyNum);
            entity.setId(null);
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            for (FlowTemplateJsonEntity jsonEntity : templateJsonEntity) {
                jsonEntity.setCreatorUserId(userProvider.get().getUserId());
                jsonEntity.setCreatorTime(new Date());
                jsonEntity.setLastModifyUserId(null);
                jsonEntity.setLastModifyTime(null);
                jsonEntity.setGroupId(null);
                jsonEntity.setId(null);
            }
            this.create(entity, templateJsonEntity);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new WorkFlowException(MsgCode.WF103.get());
        }
    }

    @Override
    @DSTransactional
    public void delete(FlowTemplateEntity entity) throws WorkFlowException {
        FlowFormEntity form = serviceUtil.getForm(entity.getId());
        if (form != null && form.getFlowType() == 1 && form.getFormType() == 2) {
            throw new WorkFlowException("该流程由在线开发生成的，无法直接删除，请在功能设计中删除相关功能！");
        }
        List<FlowTaskEntity> flowTaskList = flowTaskService.getTemplateIdList(entity.getId());
        if (flowTaskList.size() > 0) {
            throw new WorkFlowException("该流程内工单任务流转未结束，无法删除！");
        }
        List<FlowTemplateJsonEntity> list = flowTemplateJsonService.getTemplateList(ImmutableList.of(entity.getId()));
        this.removeById(entity.getId());
        serviceUtil.formIdList(new ArrayList<>(), entity.getId());
        for (FlowTemplateJsonEntity templateJson : list) {
            List<String> sendConfigIdList = flowTemplateJsonService.sendMsgConfigList(templateJson);
            if (sendConfigIdList != null && sendConfigIdList.size() > 0) {
                serviceUtil.updateSendConfigUsed(entity.getId(), sendConfigIdList);
            }
            serviceUtil.deleteFormId(templateJson.getId());
            flowEngineVisibleService.deleteVisible(templateJson.getId());
            flowTemplateJsonService.deleteFormFlowId(templateJson);
        }
    }

    @Override
    public FlowExportModel exportData(String id) throws WorkFlowException {
        FlowTemplateEntity entity = getInfo(id);
        List<FlowTemplateJsonEntity> templateJsonList = flowTemplateJsonService.getMainList(ImmutableList.of(id));
        FlowExportModel model = new FlowExportModel();
        model.setFlowTemplate(entity);
        model.setFlowTemplateJson(templateJsonList);
        return model;
    }

    @Override
    @DSTransactional
    public void ImportData(FlowExportModel flowExportModel, String type) throws WorkFlowException {
        FlowTemplateEntity entity = flowExportModel.getFlowTemplate();
        List<FlowTemplateJsonEntity> flowTemplateJson = flowExportModel.getFlowTemplateJson();
        if (entity != null) {
            StringJoiner joiner = new StringJoiner("；");
            entity.setCreatorUserId(UserProvider.getLoginUserId());
            entity.setCreatorTime(new Date());
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            entity.setEnabledMark(0);
            List<String> templateList = new ArrayList<>();
            FlowTemplateEntity templateEntity = Import(entity, type, templateList);
            if (templateList.size() > 0) {
                joiner.add(String.join("、", templateList) + "重复");
            }
            List<FlowTemplateJsonEntity> jsonList = new ArrayList<>();
            if (ObjectUtil.isNotEmpty(flowTemplateJson)) {
                List<String> idList = new ArrayList<>();
                List<String> nameList = new ArrayList<>();
                StringJoiner childJoiner = new StringJoiner("、");
                for (FlowTemplateJsonEntity templateJsonEntity : flowTemplateJson) {
                    templateJsonEntity.setCreatorUserId(UserProvider.getLoginUserId());
                    templateJsonEntity.setCreatorTime(new Date());
                    templateJsonEntity.setLastModifyTime(null);
                    templateJsonEntity.setLastModifyUserId(null);
                    templateJsonEntity.setGroupId(null);
                    FlowTemplateJsonEntity jsonEntity = Import(templateJsonEntity, type, idList, nameList);
                    jsonList.add(jsonEntity);
                }
                if (idList.size() > 0) {
                    childJoiner.add("ID(" + String.join("、", idList) + ")");
                }
                if (nameList.size() > 0) {
                    childJoiner.add("名称(" + String.join("、", nameList) + ")");
                }
                if (childJoiner.length() > 0) {
                    joiner.add("flowTemplateJson：" + childJoiner + "重复");
                }
            }
            if (StringUtil.isNotEmpty(joiner.toString())) {
                throw new WorkFlowException(joiner.toString());
            }
            create(templateEntity, jsonList);
        }
    }

    @Override
    public List<FlowTemplateListVO> getTreeList(PaginationFlowEngine pagination, boolean isList) {
        List<FlowTemplateEntity> data = isList ? getList(pagination) : getFlowFormList();
        List<DictionaryDataEntity> dictionList = serviceUtil.getDiList();
        Map<String, List<FlowTemplateEntity>> dataList = data.stream().collect(Collectors.groupingBy(FlowTemplateEntity::getCategory));
        List<FlowTemplateListVO> listVOS = new LinkedList<>();
        for (DictionaryDataEntity entity : dictionList) {
            FlowTemplateListVO model = new FlowTemplateListVO();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            List<FlowTemplateEntity> childList = dataList.get(entity.getId()) != null ? dataList.get(entity.getId()) : new ArrayList<>();
            model.setNum(childList.size());
            if (childList.size() > 0) {
                model.setChildren(JsonUtil.getJsonToList(childList, FlowTemplateListVO.class));
            }
            listVOS.add(model);
        }
        return listVOS;
    }

    @Override
    public List<FlowTemplateEntity> getFlowFormList() {
        FlowPagination flowPagination = new FlowPagination();
        flowPagination.setFlowType(0);
        List<FlowTemplateEntity> data = getListAll(flowPagination, false);
        return data;
    }

    @Override
    public List<FlowTemplateEntity> getTemplateList(List<String> id) {
        List<FlowTemplateEntity> list = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(FlowTemplateEntity::getId, id);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public FlowTemplateEntity getFlowIdByCode(String code) throws WorkFlowException {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getEnCode, code);
        FlowTemplateEntity FlowTemplateEntity = this.getOne(queryWrapper);
        if (FlowTemplateEntity == null) {
            throw new WorkFlowException(MsgCode.WF113.get());
        }
        return FlowTemplateEntity;
    }

    @Override
    public List<FlowTemplateEntity> getListAll(FlowPagination pagination, boolean isPage) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getEnabledMark, 1);
        if (ObjectUtil.isNotEmpty(pagination.getFlowType())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getType, pagination.getFlowType());
        }
        if (ObjectUtil.isNotEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().like(FlowTemplateEntity::getFullName, pagination.getKeyword());
        }
        if (ObjectUtil.isNotEmpty(pagination.getCategory())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getCategory, pagination.getCategory());
        }
        if (ObjectUtil.isNotEmpty(pagination.getTemplateIdList())) {
            queryWrapper.lambda().in(FlowTemplateEntity::getId, pagination.getTemplateIdList());
        }
        queryWrapper.lambda().orderByAsc(FlowTemplateEntity::getSortCode).orderByDesc(FlowTemplateEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(FlowTemplateEntity::getLastModifyTime);
        }
        queryWrapper.lambda().select(
                FlowTemplateEntity::getId, FlowTemplateEntity::getEnCode,
                FlowTemplateEntity::getFullName,
                FlowTemplateEntity::getType, FlowTemplateEntity::getIcon,
                FlowTemplateEntity::getCategory, FlowTemplateEntity::getIconBackground,
                FlowTemplateEntity::getCreatorUserId, FlowTemplateEntity::getSortCode,
                FlowTemplateEntity::getEnabledMark, FlowTemplateEntity::getCreatorTime
        );
        if (isPage) {
            Page<FlowTemplateEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
            IPage<FlowTemplateEntity> userPage = this.page(page, queryWrapper);
            return pagination.setData(userPage.getRecords(), page.getTotal());
        } else {
            return this.list(queryWrapper);
        }
    }

    private List<FlowEngineVisibleEntity> visibleList(FlowTemplateJsonEntity entity, boolean formType, Set<String> formIdList) throws WorkFlowException {
        List<FlowEngineVisibleEntity> visibleList = new ArrayList<>();
        String templeId = entity.getTemplateId();
        String formData = StringUtil.isNotEmpty(entity.getFlowTemplateJson()) ? entity.getFlowTemplateJson() : "{}";
        ChildNode childNode = JsonUtil.getJsonToBean(formData, ChildNode.class);
        Properties properties = childNode.getProperties();
        if (formType) {
            FlowFormEntity form = serviceUtil.getForm(properties.getFormId());
            if (form != null && StringUtil.isNotEmpty(form.getFlowId()) && !form.getFlowId().equals(templeId)) {
                throw new WorkFlowException("表单已被引用，请重新选择！");
            }
            //清空 原先绑定的功能表单流程id
            List<FlowFormEntity> flowIdList = serviceUtil.getFlowIdList(templeId);
            for (FlowFormEntity formEntity : flowIdList) {
                formEntity.setFlowId(null);
                serviceUtil.updateForm(formEntity);
            }
            if (form != null) {
                form.setFlowId(templeId);
                serviceUtil.updateForm(form);
            }
        }
        if (StringUtil.isNotEmpty(properties.getFormId())) {
            formIdList.add(properties.getFormId());
        }
        //流程可见
        for (String initiator : properties.getInitiator()) {
            String type = initiator.split("--").length > 1 ? initiator.split("--")[1] : PermissionConst.USER;
            String operatorId = initiator.split("--")[0];
            FlowEngineVisibleEntity visible = new FlowEngineVisibleEntity();
            visible.setOperatorId(operatorId);
            visible.setOperatorType(type);
            visible.setType(1);
            visibleList.add(visible);
        }
        List<ChildNodeList> nodeListAll = new ArrayList<>();
        List<ConditionList> conditionListAll = new ArrayList<>();
        FlowJsonUtil.getTemplateAll(childNode, nodeListAll, conditionListAll);
        List<String> sendIdList = new ArrayList<>();
        for (ChildNodeList childNodeList : nodeListAll) {
            Properties childProperties = childNodeList.getProperties();
            MsgConfig waitMsgConfig = childProperties.getWaitMsgConfig();
            if (StringUtil.isNotEmpty(waitMsgConfig.getMsgId())) {
                sendIdList.add(waitMsgConfig.getMsgId());
            }
            MsgConfig endMsgConfig = childProperties.getEndMsgConfig();
            if (StringUtil.isNotEmpty(endMsgConfig.getMsgId())) {
                sendIdList.add(endMsgConfig.getMsgId());
            }
            MsgConfig approveMsgConfig = childProperties.getApproveMsgConfig();
            if (StringUtil.isNotEmpty(approveMsgConfig.getMsgId())) {
                sendIdList.add(approveMsgConfig.getMsgId());
            }
            MsgConfig rejectMsgConfig = childProperties.getRejectMsgConfig();
            if (StringUtil.isNotEmpty(rejectMsgConfig.getMsgId())) {
                sendIdList.add(rejectMsgConfig.getMsgId());
            }
            MsgConfig copyMsgConfig = childProperties.getCopyMsgConfig();
            if (StringUtil.isNotEmpty(copyMsgConfig.getMsgId())) {
                sendIdList.add(copyMsgConfig.getMsgId());
            }
            MsgConfig launchMsgConfig = childProperties.getLaunchMsgConfig();
            if (StringUtil.isNotEmpty(launchMsgConfig.getMsgId())) {
                sendIdList.add(launchMsgConfig.getMsgId());
            }
            MsgConfig overtimeMsgConfig = childProperties.getOvertimeMsgConfig();
            if (StringUtil.isNotEmpty(overtimeMsgConfig.getMsgId())) {
                sendIdList.add(overtimeMsgConfig.getMsgId());
            }
            MsgConfig noticeMsgConfig = childProperties.getNoticeMsgConfig();
            if (StringUtil.isNotEmpty(noticeMsgConfig.getMsgId())) {
                sendIdList.add(noticeMsgConfig.getMsgId());
            }
        }
        entity.setSendConfigIds(JsonUtil.getObjectToString(sendIdList));
        return visibleList;
    }

    @Override
    public List<FlowTemplateEntity> getListByFlowIds(FlowPagination pagination, List<String> listAll, Boolean isAll, Boolean isPage, String userId) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getEnabledMark, 1);
        List<String> listIn = new ArrayList<>();
        List<String> listVisible = null;
        if (StringUtil.isNotEmpty(userId)) {//当用户不为空的时候【判断该用户的权限
            UserEntity userInfo = serviceUtil.getUserInfo(userId);
            boolean visibleType = "1".equals(userInfo.getIsAdministrator());
            if (!visibleType) {
                List<String> id = flowEngineVisibleService.getVisibleFlowList(userInfo.getId()).stream().map(FlowEngineVisibleEntity::getFlowId).collect(Collectors.toList());
                //可见列表
                listVisible = flowTemplateJsonService.getListAll(id).stream().map(FlowTemplateJsonEntity::getTemplateId).collect(Collectors.toList());
            }
            if (CollectionUtil.isNotEmpty(listVisible) && CollectionUtil.isNotEmpty(listAll)) {
                for (String str : listAll) {
                    if (listVisible.contains(str)) {
                        listIn.add(str);
                    }
                }
            } else if (isAll) {
                listIn = listVisible;
            }
        } else {//不判断用户权限
            listIn = listAll;
        }
        if (CollectionUtil.isEmpty(listIn) && !isAll) {
            return new ArrayList<>();
        }
        if (CollectionUtil.isNotEmpty(listIn)) {
            queryWrapper.lambda().in(FlowTemplateEntity::getId, listIn);
        }
        if (ObjectUtil.isNotEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().like(FlowTemplateEntity::getFullName, pagination.getKeyword());
        }
        if (ObjectUtil.isNotEmpty(pagination.getCategory())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getCategory, pagination.getCategory());
        }
        if (ObjectUtil.isNotEmpty(pagination.getFlowType())) {
            flag = true;
            queryWrapper.lambda().eq(FlowTemplateEntity::getType, pagination.getFlowType());
        }
        queryWrapper.lambda().orderByAsc(FlowTemplateEntity::getSortCode).orderByDesc(FlowTemplateEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(FlowTemplateEntity::getLastModifyTime);
        }
        queryWrapper.lambda().select(
                FlowTemplateEntity::getId, FlowTemplateEntity::getEnCode,
                FlowTemplateEntity::getFullName,
                FlowTemplateEntity::getType, FlowTemplateEntity::getIcon,
                FlowTemplateEntity::getCategory, FlowTemplateEntity::getIconBackground,
                FlowTemplateEntity::getCreatorUserId, FlowTemplateEntity::getSortCode,
                FlowTemplateEntity::getEnabledMark, FlowTemplateEntity::getCreatorTime
        );
        if (isPage) {
            Page<FlowTemplateEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
            IPage<FlowTemplateEntity> userPage = this.page(page, queryWrapper);
            return pagination.setData(userPage.getRecords(), page.getTotal());
        } else {
            return list(queryWrapper);
        }

    }

    @Override
    public void saveLogicFlowAndForm(String id) {
        FlowTemplateEntity flowEnt = this.setIgnoreLogicDelete().getById(id);
        if (flowEnt != null) {
            flowEnt.setDeleteMark(null);
            this.setIgnoreLogicDelete().updateById(flowEnt);
            QueryWrapper<FlowTemplateJsonEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(FlowTemplateJsonEntity::getTemplateId, id);
            List<FlowTemplateJsonEntity> list = flowTemplateJsonService.setIgnoreLogicDelete().list(queryWrapper);
            list.forEach(t -> t.setDeleteMark(null));
            flowTemplateJsonService.setIgnoreLogicDelete().updateBatchById(list);
        }
        this.clearIgnoreLogicDelete();
    }
    @Override
    public ActionResult createTemplate(FlowTemplateCrForm form) {
        String msg = "";
        try {
            if (this.isExistByFullName(form.getFullName(), form.getId())) {
                return ActionResult.fail("流程名称不能重复");
            }
            if (this.isExistByEnCode(form.getEnCode(), form.getId())) {
                return ActionResult.fail("流程编码不能重复");
            }
            FlowTemplateEntity entity = JsonUtil.getJsonToBean(form, FlowTemplateEntity.class);
            this.create(entity);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        return StringUtil.isNotEmpty(msg) ? ActionResult.fail(msg) : ActionResult.success();
    }
    @Override
    public ActionResult updateTemplate(String id, FlowTemplateCrForm form) {
        String msg = "";
        try {
            if (this.isExistByFullName(form.getFullName(), id)) {
                ActionResult.success("流程名称不能重复");
            }
            if (this.isExistByEnCode(form.getEnCode(),id)) {
                ActionResult.success("流程编码不能重复");
            }
            FlowTemplateEntity entity = JsonUtil.getJsonToBean(form, FlowTemplateEntity.class);
            this.update(id, entity);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        return StringUtil.isNotEmpty(msg) ? ActionResult.fail(msg) : ActionResult.success();
    }

    private FlowTemplateEntity Import(FlowTemplateEntity templateEntity, String type, List<String> errList) {
        FlowTemplateEntity entity = JsonUtil.getJsonToBean(templateEntity, FlowTemplateEntity.class);
        boolean skip = Objects.equals("0", type);
        int num = 0;
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getId, entity.getId());
        if (this.count(queryWrapper) > 0) {
            num++;
            if (skip) {
                errList.add("ID");
            }
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getEnCode, entity.getEnCode());
        if (this.count(queryWrapper) > 0) {
            num++;
            if (skip) {
                errList.add("编码");
            }
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getFullName, entity.getFullName());
        if (this.count(queryWrapper) > 0) {
            num++;
            if (skip) {
                errList.add("名称");
            }
        }
        if (num > 0 && !skip) {
            String copyNum = UUID.randomUUID().toString().substring(0, 5);
            entity.setFullName(entity.getFullName() + ".副本" + copyNum);
            entity.setEnCode(entity.getEnCode() + copyNum);
        }
        entity.setId(RandomUtil.uuId());
        return entity;
    }

    private FlowTemplateJsonEntity Import(FlowTemplateJsonEntity templateEntity, String type, List<String> idList, List<String> nameList) {
        FlowTemplateJsonEntity entity = JsonUtil.getJsonToBean(templateEntity, FlowTemplateJsonEntity.class);
        boolean skip = Objects.equals("0", type);
        QueryWrapper<FlowTemplateJsonEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateJsonEntity::getId, entity.getId());
        if (flowTemplateJsonService.count(queryWrapper) > 0) {
            if (skip) {
                idList.add(entity.getId());
            }
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateJsonEntity::getFullName, entity.getFullName());
        queryWrapper.lambda().eq(FlowTemplateJsonEntity::getTemplateId, entity.getId());
        if (flowTemplateJsonService.count(queryWrapper) > 0) {
            if (skip) {
                nameList.add(entity.getFullName());
            }
        }
        entity.setId(RandomUtil.uuId());
        return entity;
    }
}
