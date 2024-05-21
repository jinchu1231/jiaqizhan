package jnpf.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.ActionResult;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.mapper.FlowFormMapper;
import jnpf.model.flow.FlowTempInfoModel;
import jnpf.model.form.FlowFormPage;
import jnpf.model.form.FormDraftJsonModel;
import jnpf.model.form.VisualTableModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormCloumnUtil;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.service.FlowFormService;
import jnpf.util.ConcurrencyUtils;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.VisualDevTableCre;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 18:01
 */
@Service
public class FlowFormServiceImpl extends SuperServiceImpl<FlowFormMapper, FlowFormEntity> implements FlowFormService {

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private VisualDevTableCre formTableCre;

    @Autowired
    private ConcurrencyUtils concurrencyUtils;

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<FlowFormEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(FlowFormEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCOde, String id) {
        QueryWrapper<FlowFormEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormEntity::getEnCode, enCOde);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(FlowFormEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    @SneakyThrows
    public Boolean create(FlowFormEntity entity) {
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(RandomUtil.uuId());
        }

        FormDataModel formDataModel = new FormDataModel();
        //判断是否要创表
        List<TableModel> tableModels = new ArrayList<>();
        Map<String, Object> formMap = null;
        if (entity.getFormType() == 2 && entity.getDraftJson() != null) {
            formDataModel = JsonUtil.getJsonToBean(entity.getDraftJson(), FormDataModel.class);
            tableModels = JsonUtil.getJsonToList(entity.getTableJson(), TableModel.class);
            formMap = JsonUtil.stringToMap(entity.getDraftJson());
        }
        //是否开启安全锁
        Boolean concurrencyLock = formDataModel.getConcurrencyLock();
        Boolean logicalDelete = formDataModel.getLogicalDelete();
        int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();

        //有表
        if (entity.getTableJson() != null) {
            List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getTableJson(), TableModel.class);
            TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1" )).findFirst().orElse(null);
            //判断自增是否匹配
            concurrencyUtils.checkAutoIncrement(primaryKeyPolicy,entity.getDbLinkId(),visualTables);
            //在主表创建锁字段是否开启安全锁
            try {
                if (logicalDelete && mainTable != null) {
                    concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                }
                if (concurrencyLock) {
                    concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                }
                if (mainTable != null && formDataModel.getPrimaryKeyPolicy() == 2) {
                    concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                }
                concurrencyUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
            } catch (Exception e) {
                log.error("创建字段失败！");
            }
        }
        entity.setDraftJson(JsonUtil.getObjectToString(new FormDraftJsonModel().setDraftJson(entity.getDraftJson()).setTableJson(entity.getTableJson())));
        return this.save(entity);
    }

    @Override
    public Boolean update(FlowFormEntity entity) throws Exception {
        List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getTableJson(), TableModel.class);
        if (entity.getFormType() == 2 && visualTables.size() > 0 && StringUtil.isNotEmpty(entity.getDraftJson())) {
            FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getDraftJson(), FormDataModel.class);
            TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);
            //是否开启安全锁
            Boolean concurrencyLock = formDataModel.getConcurrencyLock();
            int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
            Boolean logicalDelete = formDataModel.getLogicalDelete();
            if (logicalDelete && mainTable != null) {
                //在主表创建逻辑删除字段
                concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
            }
            if (concurrencyLock) {
                //在主表创建锁字段
                concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
            }
            if (mainTable != null && formDataModel.getPrimaryKeyPolicy() == 2) {
                concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
            }
            concurrencyUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
            //判断自增是否匹配
            concurrencyUtils.checkAutoIncrement(primaryKeyPolicy,entity.getDbLinkId(),visualTables);
        }
        entity.setDraftJson(JsonUtil.getObjectToString(new FormDraftJsonModel().setDraftJson(entity.getDraftJson()).setTableJson(entity.getTableJson())));
        return this.updateById(entity);
    }


    @Override
    public List<FlowFormEntity> getList(FlowFormPage flowFormPage) {
        QueryWrapper<FlowFormEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(flowFormPage.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(FlowFormEntity::getFullName, flowFormPage.getKeyword())
                            .or().like(FlowFormEntity::getEnCode, flowFormPage.getKeyword())
            );
        }
        if (flowFormPage.getFlowType() != null) {
            queryWrapper.lambda().eq(FlowFormEntity::getFlowType, flowFormPage.getFlowType());
        }
        if (flowFormPage.getFormType() != null) {
            queryWrapper.lambda().eq(FlowFormEntity::getFormType, flowFormPage.getFormType());
        }
        if (flowFormPage.getIsRelease() != null) {
            queryWrapper.lambda().eq(FlowFormEntity::getState, flowFormPage.getIsRelease());
        }
        if (flowFormPage.getEnabledMark() != null) {
            queryWrapper.lambda().eq(FlowFormEntity::getEnabledMark, flowFormPage.getEnabledMark());
        }
        if (flowFormPage.getFlowType() == null || !flowFormPage.getFlowType().equals(1)) {
            queryWrapper.lambda().and(t -> t.ne(FlowFormEntity::getFlowType, 1).or().ne(FlowFormEntity::getFormType, 2));
        }
        queryWrapper.lambda().orderByAsc(FlowFormEntity::getSortCode);
        queryWrapper.lambda().orderByDesc(FlowFormEntity::getCreatorTime);
        Page<FlowFormEntity> page = new Page<>(flowFormPage.getCurrentPage(), flowFormPage.getPageSize());
        IPage<FlowFormEntity> list = this.page(page, queryWrapper);
        return flowFormPage.setData(list.getRecords(), list.getTotal());
    }

    @Override
    public List<FlowFormEntity> getListForSelect(FlowFormPage flowFormPage) {
        QueryWrapper<FlowFormEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(flowFormPage.getKeyword())) {
            queryWrapper.lambda().like(FlowFormEntity::getFullName, flowFormPage.getKeyword());
        }
        if (flowFormPage.getFlowType() != null) {
            queryWrapper.lambda().eq(FlowFormEntity::getFlowType, flowFormPage.getFlowType());
        }
        if (flowFormPage.getFormType() != null) {
            queryWrapper.lambda().eq(FlowFormEntity::getFormType, flowFormPage.getFormType());
        }
        queryWrapper.lambda().eq(FlowFormEntity::getEnabledMark, 1);
        queryWrapper.lambda().orderByAsc(FlowFormEntity::getSortCode);
        queryWrapper.lambda().orderByDesc(FlowFormEntity::getCreatorTime);
        Page<FlowFormEntity> page = new Page<>(flowFormPage.getCurrentPage(), flowFormPage.getPageSize());
        IPage<FlowFormEntity> list = this.page(page, queryWrapper);
        return flowFormPage.setData(list.getRecords(), list.getTotal());
    }

    @Override
    public ActionResult release(String id, Integer isRelease) throws WorkFlowException {
        FlowFormEntity byId = this.getById(id);
        if (isRelease != null && isRelease == 0) {//回滚
            if (byId.getEnabledMark() != null && byId.getEnabledMark() == 1) {
                FlowFormEntity entity = new FlowFormEntity();
                entity.setId(id);
                entity.setState(1);
                entity.setDraftJson(JsonUtil.getObjectToString(new FormDraftJsonModel().setDraftJson(byId.getPropertyJson()).setTableJson(byId.getTableJson())));
//                entity.setDraftJson(byId.getPropertyJson());
                this.updateById(entity);
                return ActionResult.success("回滚成功");
            } else {
                return ActionResult.fail("该表单未发布，无法回滚表单内容");
            }
        }
        if (isRelease != null && isRelease == 1) {//发布
            FormDraftJsonModel formDraft = JsonUtil.getJsonToBean(byId.getDraftJson(), FormDraftJsonModel.class);
            if (StringUtil.isEmpty(formDraft.getDraftJson())) {
                return ActionResult.fail("该模板内表单内容为空，无法发布！");
            }
            FlowFormEntity entity = new FlowFormEntity();
            BeanUtils.copyProperties(byId, entity);
            entity.setId(id);
            entity.setEnabledMark(1);
            entity.setState(1);
            entity.setPropertyJson(formDraft.getDraftJson());
            entity.setTableJson(formDraft.getTableJson());
            entity.setLastModifyTime(new Date());
            entity.setLastModifyUserId(userProvider.get().getUserId());
            //判断是否要创表
            FormDataModel formDataModel = new FormDataModel();
            List<TableModel> tableModels = null;
            Map<String, Object> formMap = null;
            if (entity.getFormType() == 2 && formDraft.getDraftJson() != null) {
                formDataModel = JsonUtil.getJsonToBean(formDraft.getDraftJson(), FormDataModel.class);
                tableModels = JsonUtil.getJsonToList(formDraft.getTableJson(), TableModel.class);
                formMap = JsonUtil.stringToMap(formDraft.getDraftJson());
            }
            //是否开启安全锁
            Boolean concurrencyLock = formDataModel.getConcurrencyLock();
            int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
            Boolean logicalDelete = formDataModel.getLogicalDelete();
            //无表需要创表
            if (tableModels != null && tableModels.size() == 0) {
                List<FieLdsModel> list = JsonUtil.getJsonToList(formMap.get("fields"), FieLdsModel.class);
                JSONArray formJsonArray = JsonUtil.getJsonToJsonArray(String.valueOf(formMap.get("fields")));
                List<FormAllModel> formAllModel = new ArrayList<>();
                RecursionForm recursionForm = new RecursionForm();
                recursionForm.setTableModelList(JsonUtil.getJsonToList(entity.getTableJson(), TableModel.class));
                recursionForm.setList(list);
                FormCloumnUtil.recursionForm(recursionForm, formAllModel);
                String tableName = "mt" + RandomUtil.uuId();
                String dbLinkId = entity.getDbLinkId();
                VisualTableModel model = new VisualTableModel(formJsonArray, formAllModel, tableName, dbLinkId, entity.getFullName(), concurrencyLock, primaryKeyPolicy, logicalDelete);
                List<TableModel> tableModelList = formTableCre.tableList(model);
                formMap.put("fields", formJsonArray);
                //更新
                entity.setDraftJson(JsonUtil.getObjectToString(formMap));
                entity.setPropertyJson(JsonUtil.getObjectToString(formMap));
                entity.setTableJson(JsonUtil.getObjectToString(tableModelList));
                entity.setDraftJson(JsonUtil.getObjectToString(new FormDraftJsonModel().setDraftJson(entity.getDraftJson()).setTableJson(entity.getTableJson())));
            }
            this.updateById(entity);
            return ActionResult.success(MsgCode.SU011.get());
        }
        return ActionResult.fail(MsgCode.FA011.get());
    }

    @Override
    public boolean copyForm(String id) {
        FlowFormEntity byId = this.getById(id);
        FlowFormEntity entity = new FlowFormEntity();
        BeanUtils.copyProperties(byId, entity);
        entity.setId(null);
        entity.setPropertyJson(null);
        if (byId.getEnabledMark() != null && byId.getEnabledMark() == 1) {
            entity.setDraftJson(JsonUtil.getObjectToString(new FormDraftJsonModel().setDraftJson(byId.getPropertyJson()).setTableJson(entity.getTableJson())));
        }
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        entity.setFullName(byId.getFullName() + ".副本" + copyNum);
        entity.setEnCode(byId.getEnCode() + copyNum);
        entity.setEnabledMark(0);
        entity.setState(0);
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setCreatorTime(new Date());
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        return this.save(entity);
    }


    @Override
    @DSTransactional
    public ActionResult ImportData(FlowFormEntity entity, String type) {
        if (entity != null) {
            entity.setCreatorUserId(UserProvider.getLoginUserId());
            entity.setCreatorTime(new Date());
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            entity.setEnabledMark(0);
            entity.setState(0);
            entity.setDbLinkId("0");
            List<String> errList = new ArrayList<>();
            FlowFormEntity formEntity = Import(entity, type, errList);
            if (errList.size() > 0) {
                return ActionResult.fail(String.join("、", errList) + "重复");
            }
            try {
                this.setIgnoreLogicDelete().removeById(formEntity);
                this.setIgnoreLogicDelete().saveOrUpdate(formEntity);
                this.clearIgnoreLogicDelete();
            } catch (Exception e) {
                return ActionResult.fail("导入失败:" + e.getMessage());
            }
            return ActionResult.success(MsgCode.IMP001.get());
        }
        return ActionResult.fail("导入数据格式不正确");
    }

    @Override
    public List<FlowFormEntity> getFlowIdList(String flowId) {
        QueryWrapper<FlowFormEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormEntity::getFlowId, flowId);
        return this.list(queryWrapper);
    }

    @Override
    public FlowTempInfoModel getFormById(String id) throws WorkFlowException {
        FlowTempInfoModel model = new FlowTempInfoModel();
        FlowFormEntity form = this.getById(id);
        if (form == null) {
            throw new WorkFlowException("该功能未导入流程表单！");
        }
        if (form != null && StringUtil.isNotEmpty(form.getFlowId())) {
            model = this.baseMapper.findFLowInfo(form.getFlowId());
        }
        if (model == null || StringUtil.isEmpty(model.getId())) {
            throw new WorkFlowException("流程未设计，请先设计流程！");
        }
        if (form.getFlowType() == 1 && form.getFormType() == 1 && model.getEnabledMark() != 1) {
            //代码生成的功能流程需要判断流程是否启用。
            throw new WorkFlowException("该功能流程处于停用状态!");
        }
        return model;
    }

    @Override
    public void updateForm(FlowFormEntity entity) {
        UpdateWrapper<FlowFormEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(FlowFormEntity::getId, entity.getId());
        wrapper.lambda().set(FlowFormEntity::getFlowId, entity.getFlowId());
        this.update(wrapper);
    }

    @Override
    public void saveLogicFlowAndForm(String id) {
        FlowFormEntity formEnt = this.setIgnoreLogicDelete().getById(id);
        if(formEnt!=null){
            formEnt.setDeleteMark(null);
            this.setIgnoreLogicDelete().updateById(formEnt);
        }
        this.clearIgnoreLogicDelete();
    }

    private FlowFormEntity Import(FlowFormEntity flowFormEntity, String type, List<String> errList) {
        FlowFormEntity entity = JsonUtil.getJsonToBean(flowFormEntity, FlowFormEntity.class);
        boolean skip = Objects.equals("0", type);
        int num = 0;
        QueryWrapper<FlowFormEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormEntity::getId, entity.getId());
        if (this.count(queryWrapper) > 0) {
            num++;
            if (skip) {
                errList.add("ID");
            }
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormEntity::getEnCode, entity.getEnCode());
        if (this.count(queryWrapper) > 0) {
            num++;
            if (skip) {
                errList.add("编码");
            }
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormEntity::getFullName, entity.getFullName());
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
            entity.setFlowId(null);
        }
        entity.setId(RandomUtil.uuId());
        return entity;
    }
}
