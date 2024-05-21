package jnpf.base.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.mapper.VisualdevMapper;
import jnpf.base.model.PaginationVisualdev;
import jnpf.base.service.DbTableService;
import jnpf.base.service.FilterService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.VisualdevReleaseService;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.VisualFlowFormUtil;
import jnpf.database.model.dbfield.DbFieldModel;
import jnpf.database.model.dbfield.base.DbFieldModelBase;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.model.form.VisualTableModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormCloumnUtil;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.service.FlowFormService;
import jnpf.util.ConcurrencyUtils;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.VisualDevTableCre;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 可视化开发功能表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-02
 */
@Service
public class VisualdevServiceImpl extends SuperServiceImpl<VisualdevMapper, VisualdevEntity> implements VisualdevService {

    @Autowired
    private ConcurrencyUtils concurrencyUtils;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private VisualDevTableCre visualDevTableCre;
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FilterService filterService;
    @Autowired
    private VisualdevReleaseService visualdevReleaseService;
    @Autowired
    private VisualFlowFormUtil visualFlowFormUtil;
    @Autowired
    private DbTableService dbTableService;

    @Override
    public List<VisualdevEntity> getList(PaginationVisualdev paginationVisualdev) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(VisualdevEntity::getId, VisualdevEntity::getCategory, VisualdevEntity::getEnCode, VisualdevEntity::getFullName,
                VisualdevEntity::getCreatorTime, VisualdevEntity::getCreatorUserId, VisualdevEntity::getLastModifyTime, VisualdevEntity::getLastModifyUserId,
                VisualdevEntity::getEnableFlow, VisualdevEntity::getEnabledMark, VisualdevEntity::getSortCode, VisualdevEntity::getState, VisualdevEntity::getType,
                VisualdevEntity::getWebType, VisualdevEntity::getVisualTables);

        if (!StringUtil.isEmpty(paginationVisualdev.getKeyword())) {
            flag = true;
            queryWrapper.lambda().and(t -> t.like(VisualdevEntity::getFullName, paginationVisualdev.getKeyword())
                    .or().like(VisualdevEntity::getEnCode, paginationVisualdev.getKeyword()));
        }

        queryWrapper.lambda().eq(VisualdevEntity::getType, paginationVisualdev.getType());

        if (StringUtil.isNotEmpty(paginationVisualdev.getCategory())) {
            flag = true;
            queryWrapper.lambda().eq(VisualdevEntity::getCategory, paginationVisualdev.getCategory());
        }

        //---功能类型查询
        if (paginationVisualdev.getWebType() != null) {//普通表单
            flag = true;
            if (Objects.equals(paginationVisualdev.getWebType(), 1)) {
                queryWrapper.lambda().eq(VisualdevEntity::getEnableFlow, 0);
                queryWrapper.lambda().ne(VisualdevEntity::getWebType, 4);
            } else if (Objects.equals(paginationVisualdev.getWebType(), 2)) {
                queryWrapper.lambda().eq(VisualdevEntity::getEnableFlow, 1);
                queryWrapper.lambda().ne(VisualdevEntity::getWebType, 4);
            } else {
                queryWrapper.lambda().eq(VisualdevEntity::getWebType, paginationVisualdev.getWebType());
            }
        }

        //是否流程分类
        if (paginationVisualdev.getEnableFlow() != null) {
            flag = true;
            queryWrapper.lambda().eq(VisualdevEntity::getEnableFlow, paginationVisualdev.getEnableFlow());
        }

        //状态
        if (paginationVisualdev.getIsRelease() != null) {
            flag = true;
            queryWrapper.lambda().eq(VisualdevEntity::getState, paginationVisualdev.getIsRelease());
        }

        // 排序
        queryWrapper.lambda().orderByAsc(VisualdevEntity::getSortCode).orderByDesc(VisualdevEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(VisualdevEntity::getLastModifyTime);
        }
        Page<VisualdevEntity> page = new Page<>(paginationVisualdev.getCurrentPage(), paginationVisualdev.getPageSize());
        IPage<VisualdevEntity> userPage = this.page(page, queryWrapper);
        return paginationVisualdev.setData(userPage.getRecords(), page.getTotal());
    }

    @Override
    public List<VisualdevEntity> getPageList(PaginationVisualdev paginationVisualdev) {
        QueryWrapper<VisualdevReleaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(
                VisualdevReleaseEntity::getId,
                VisualdevReleaseEntity::getEnableFlow,
                VisualdevReleaseEntity::getFullName,
                VisualdevReleaseEntity::getEnCode);
        if (!StringUtil.isEmpty(paginationVisualdev.getKeyword())) {
            queryWrapper.lambda().like(VisualdevReleaseEntity::getFullName, paginationVisualdev.getKeyword());
        }
        if (ObjectUtil.isNotEmpty(paginationVisualdev.getType())) {
            queryWrapper.lambda().eq(VisualdevReleaseEntity::getType, paginationVisualdev.getType());
        }
        if (ObjectUtil.isNotEmpty(paginationVisualdev.getWebType())) {
            queryWrapper.lambda().eq(VisualdevReleaseEntity::getWebType, paginationVisualdev.getWebType());
        }
        if (ObjectUtil.isNotEmpty(paginationVisualdev.getEnableFlow())) {
            queryWrapper.lambda().eq(VisualdevReleaseEntity::getEnableFlow, paginationVisualdev.getEnableFlow());
        }
        if (StringUtil.isNotEmpty(paginationVisualdev.getCategory())) {
            queryWrapper.lambda().eq(VisualdevReleaseEntity::getCategory, paginationVisualdev.getCategory());
        }
        // 排序
        queryWrapper.lambda().orderByAsc(VisualdevReleaseEntity::getSortCode).orderByDesc(VisualdevReleaseEntity::getCreatorTime);
        Page<VisualdevReleaseEntity> page = new Page<>(paginationVisualdev.getCurrentPage(), paginationVisualdev.getPageSize());
        IPage<VisualdevReleaseEntity> userPage = visualdevReleaseService.page(page, queryWrapper);
        List<VisualdevEntity> list = JsonUtil.getJsonToList(userPage.getRecords(), VisualdevEntity.class);
        return paginationVisualdev.setData(list, page.getTotal());
    }


    @Override
    public List<VisualdevEntity> getList() {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(VisualdevEntity::getSortCode).orderByDesc(VisualdevEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public VisualdevEntity getInfo(String id) {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public VisualdevEntity getReleaseInfo(String id) {
        VisualdevReleaseEntity visualdevReleaseEntity = visualdevReleaseService.getById(id);
        VisualdevEntity visualdevEntity = null;
        if (visualdevReleaseEntity != null) {
            visualdevEntity = JsonUtil.getJsonToBean(visualdevReleaseEntity, VisualdevEntity.class);
        }
        if (visualdevEntity == null) {
            visualdevEntity = getById(id);
        }
        return visualdevEntity;
    }

    @Override
    public Map<String, String> getTableMap(String formData) {
        Map<String, String> tableMap = new HashMap<>();
        if (StringUtil.isEmpty(formData)) {
            return tableMap;
        }
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formData, FormDataModel.class);
        String fields = formDataModel.getFields();
        List<FieLdsModel> list = JsonUtil.getJsonToList(fields, FieLdsModel.class);
        list.forEach(item -> {
            this.solveTableName(item, tableMap);
        });
        return tableMap;
    }

    private void solveTableName(FieLdsModel item, Map tableMap) {
        ConfigModel config = item.getConfig();
        if (config != null) {
            List<FieLdsModel> children = config.getChildren();
            if ("table".equals(config.getJnpfKey())) {
                if (children != null && children.size() > 0) {
                    FieLdsModel fieLdsModel = children.get(0);
                    String parentVModel = item.getVModel();
                    String relationTable = fieLdsModel.getConfig().getRelationTable();
                    tableMap.put(parentVModel, relationTable);
                }
            }
            if (children != null) {
                children.forEach(item2 -> {
                    this.solveTableName(item2, tableMap);
                });
            }
        }
    };

    @Override
    @SneakyThrows
    public Boolean create(VisualdevEntity entity) {
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(RandomUtil.uuId());
        }
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        if (formDataModel != null) {
            Map<String, String> tableMap = this.getTableMap(entity.getFormData());
            // 保存app,pc过滤配置
            filterService.saveRuleList(entity.getId(), entity, 1, 1, tableMap);
            //是否开启安全锁
            Boolean concurrencyLock = formDataModel.getConcurrencyLock();
            int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
            Boolean logicalDelete = formDataModel.getLogicalDelete();

            //判断是否要创表
            List<TableModel> tableModels = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
            //有表
            if (tableModels.size() > 0) {
                List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
                TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);

                for (TableModel tableModel : visualTables) {
                    Boolean isAutoIncre = this.getPrimaryDbField(entity.getDbLinkId(), tableModel.getTable());
                    // 1:雪花ID 2:自增ID
                    if (primaryKeyPolicy == 1) {
                        if (isAutoIncre != null && isAutoIncre) {
                            throw new WorkFlowException("主键策略:[雪花ID],表[ " + tableModel.getTable() + " ]主键设置不支持!");
                        }
                    } else if (primaryKeyPolicy == 2) {
                        if (isAutoIncre == null || !isAutoIncre) {
                            throw new WorkFlowException("主键策略:[自增ID],表[ " + tableModel.getTable() + " ]主键设置不支持!");
                        }
                    }
                }
                //在主表创建锁字段
                try {
                    if (logicalDelete && mainTable != null) {
                        //在主表创建逻辑删除
                        concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                    }
                    if (concurrencyLock) {
                        concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                    }
                    if (entity.getEnableFlow() == 1) {
                        concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                    }
                    if (TenantDataSourceUtil.isTenantColumn()) {
                        for (TableModel tableModel : visualTables) {
                            concurrencyUtils.createTenantId(tableModel.getTable(), entity.getDbLinkId());
                        }
                    }
                    concurrencyUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("创建锁字段失败");
                    e.printStackTrace();
                }
            }
        }
        entity.setEnabledMark(0);
        entity.setState(0);
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(UserProvider.getLoginUserId());
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        // 启用流程 在表单新增一条 提供给流程使用
        if (Objects.equals(OnlineDevData.STATE_ENABLE, entity.getEnableFlow()) && entity.getType() < 3) {
            visualFlowFormUtil.saveLogicFlowAndForm(entity);
        }
        this.setIgnoreLogicDelete().removeById(entity.getId());
        boolean result = this.setIgnoreLogicDelete().saveOrUpdate(entity);
        this.clearIgnoreLogicDelete();
        return result;
    }

    @Override
    public boolean update(String id, VisualdevEntity entity) throws Exception {
        entity.setId(id);
        entity.setLastModifyUserId(userProvider.get().getUserId());
        boolean b = this.updateById(entity);
        //代码生成修改时就要生成字段
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        if (formDataModel != null) {
            //是否开启安全锁
            Boolean concurrencyLock = formDataModel.getConcurrencyLock();
            Boolean logicalDelete = formDataModel.getLogicalDelete();
            int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
            //判断是否要创表
            List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
            //有表
            if (visualTables.size() > 0) {
                if (formDataModel != null) {
                    try {
                        TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);
                        if (logicalDelete && mainTable != null) {
                            //在主表创建逻辑删除
                            concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                        }
                        if (concurrencyLock) {
                            //在主表创建锁字段
                            concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                        }
                        concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                        if (TenantDataSourceUtil.isTenantColumn()) {
                            for (TableModel tableModel : visualTables) {
                                concurrencyUtils.createTenantId(tableModel.getTable(), entity.getDbLinkId());
                            }
                        }
                        concurrencyUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //判断自增是否匹配
                    concurrencyUtils.checkAutoIncrement(primaryKeyPolicy, entity.getDbLinkId(), visualTables);
                }
            }
        }
        return b;
    }

    @Override
    public void delete(VisualdevEntity entity) throws WorkFlowException {
        if (entity != null) {
            //删除表单
            flowFormService.removeById(entity.getId());
            List<String> ids = new ArrayList<>();
            ids.add(entity.getId());
            this.removeByIds(ids);
        }
    }

    @Override
    public Integer getObjByEncode(String encode, Integer type) {
        QueryWrapper<VisualdevEntity> visualWrapper = new QueryWrapper<>();
        visualWrapper.lambda().eq(VisualdevEntity::getEnCode, encode).eq(VisualdevEntity::getType, type);
        Integer count = (int) this.count(visualWrapper);
        return count;
    }

    @Override
    public Integer getCountByName(String name, Integer type) {
        QueryWrapper<VisualdevEntity> visualWrapper = new QueryWrapper<>();
        visualWrapper.lambda().eq(VisualdevEntity::getFullName, name).eq(VisualdevEntity::getType, type);
        Integer count = (int) this.count(visualWrapper);
        return count;
    }

    @Override
    public void createTable(VisualdevEntity entity) throws WorkFlowException {
        boolean isTenant = TenantDataSourceUtil.isTenantColumn();
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        //是否开启安全锁
        Boolean concurrencyLock = formDataModel.getConcurrencyLock();
        int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
        Boolean logicalDelete = formDataModel.getLogicalDelete();

        Map<String, Object> formMap = JsonUtil.stringToMap(entity.getFormData());
        List<FieLdsModel> list = JsonUtil.getJsonToList(formMap.get("fields"), FieLdsModel.class);
        JSONArray formJsonArray = JsonUtil.getJsonToJsonArray(String.valueOf(formMap.get("fields")));
        List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);

        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(visualTables);
        recursionForm.setList(list);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);

        String tableName = "mt" + RandomUtil.uuId();

        String dbLinkId = entity.getDbLinkId();
        VisualTableModel model = new VisualTableModel(formJsonArray, formAllModel, tableName, dbLinkId, entity.getFullName(), concurrencyLock, primaryKeyPolicy, logicalDelete);
        List<TableModel> tableModelList = visualDevTableCre.tableList(model);

        if (formDataModel != null) {
            try {
                TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);
                if (OnlineDevData.STATE_ENABLE.equals(entity.getEnableFlow()) && mainTable != null) {
                    concurrencyUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
                }
                if (logicalDelete && mainTable != null) {
                    //在主表创建逻辑删除
                    concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                }
                if (concurrencyLock) {
                    //在主表创建锁字段
                    concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                }
                if (entity.getEnableFlow() == 1) {
                    concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                }
                if (isTenant) {
                    for (TableModel tableModel : visualTables) {
                        concurrencyUtils.createTenantId(tableModel.getTable(), entity.getDbLinkId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        formMap.put("fields", formJsonArray);
        //更新
        entity.setFormData(JsonUtil.getObjectToString(formMap));
        entity.setVisualTables(JsonUtil.getObjectToString(tableModelList));
    }

    @Override
    public Map<String, String> getTableNameToKey(String modelId) {
        Map<String, String> childKeyMap = new HashMap<>();
        VisualdevEntity info = this.getInfo(modelId);
        FormDataModel formDataModel = JsonUtil.getJsonToBean(info.getFormData(), FormDataModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> childFields = fieLdsModels.stream().filter(f -> JnpfKeyConsts.CHILD_TABLE.equals(f.getConfig().getJnpfKey())).collect(Collectors.toList());
        childFields.stream().forEach(c ->
                childKeyMap.put(c.getConfig().getTableName().toLowerCase(), c.getVModel())
        );
        return childKeyMap;
    }

    @Override
    public Boolean getPrimaryDbField(String linkId, String table) throws Exception {
        // List<DbFieldModelBase> fieldList = dbTableService.getDbTableModel(linkId, table);

        List<DbFieldModel> dbFieldModelList = dbTableService.getDbTableModel(linkId, table).getDbFieldModelList();
//        List<DbFieldModel> data = JsonUtil.getJsonToList(dbFieldModelList, DbFieldModel.class);
        DbFieldModel dbFieldModel = dbFieldModelList.stream().filter(DbFieldModel::getIsPrimaryKey).findFirst().orElse(null);
        if (dbFieldModel != null) {
            return dbFieldModel.getIsAutoIncrement() != null && dbFieldModel.getIsAutoIncrement();
        } else {
            return null;
        }
    }

    @Override
    public List<VisualdevEntity> selectorList() {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(
                VisualdevEntity::getId,
                VisualdevEntity::getFullName,
                VisualdevEntity::getWebType,
                VisualdevEntity::getEnableFlow,
                VisualdevEntity::getType,
                VisualdevEntity::getCategory);
        return this.list(queryWrapper);
    }
}
