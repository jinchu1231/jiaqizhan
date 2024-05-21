package jnpf.onlinedev.service.impl;

import cn.hutool.core.util.ObjectUtil;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.VisualdevService;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.mapper.FlowFormDataMapper;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormCloumnUtil;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableFields;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.FormColumnModel;
import jnpf.model.visualJson.analysis.FormEnum;
import jnpf.model.visualJson.analysis.FormMastTableModel;
import jnpf.model.visualJson.analysis.FormModel;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.onlinedev.model.OnlineInfoModel;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualDevInfoService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.onlinedev.util.onlineDevUtil.OnlineDevInfoUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlineSwapDataUtils;
import jnpf.util.FlowFormDataUtil;
import jnpf.util.FormPublicUtils;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.XSSEscape;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author JNPF开发平台组
 * @version V3.2
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/10/26
 */
@Service
public class VisualDevInfoServiceImpl implements VisualDevInfoService {

    @Autowired
    private DbLinkService dbLinkService;
    @Autowired
    private OnlineDevInfoUtils onlineDevInfoUtils;
    @Autowired
    private FlowFormDataMapper flowFormDataMapper;
    @Autowired
    private OnlineSwapDataUtils onlineSwapDataUtils;
    @Autowired
    private FlowFormDataUtil flowDataUtil;

    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;

    @Override
    public VisualdevModelDataInfoVO getEditDataInfo(String id, VisualdevEntity visualdevEntity) {
        VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
        Map<String, Object> allDataMap = new HashMap<>();

        FormDataModel formData = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        //是否开启并发锁
        String version = "";
        if (formData.getConcurrencyLock()) {
            //查询
            version = TableFeildsEnum.VERSION.getField();
        }

        Integer primaryKeyPolicy = formData.getPrimaryKeyPolicy();
        boolean isSnowFlake = visualdevEntity.getEnableFlow() == 0;
        if (primaryKeyPolicy == 2 && isSnowFlake) {
            primaryKeyPolicy = 1;
        }
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<TableModel> tableModelList = JsonUtil.getJsonToList(visualdevEntity.getVisualTables(), TableModel.class);
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setList(list);
        recursionForm.setTableModelList(tableModelList);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        //form的属性
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());

        TableModel mainTable = tableModelList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);

        DbLinkEntity linkEntity = dbLinkService.getInfo(visualdevEntity.getDbLinkId());
        try {
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            boolean oracle = databaseProductName.equalsIgnoreCase("oracle");
            boolean IS_DM = databaseProductName.equalsIgnoreCase("DM DBMS");
            Object idObj=id;
            if(formData.getPrimaryKeyPolicy() == 2){
                idObj=Long.parseLong(id);
            }
            if (visualdevEntity.getEnableFlow() == 0 && formData.getPrimaryKeyPolicy() == 2) {
                primaryKeyPolicy = 1;
            }
            //获取主键
            String pKeyName = flowDataUtil.getKey(conn, mainTable.getTable(), primaryKeyPolicy);
            //主表所有数据
            SqlTable mainSqlTable = SqlTable.of(mainTable.getTable());
            SelectStatementProvider render = SqlBuilder.select(mainSqlTable.allColumns()).from(mainSqlTable).where(mainSqlTable.column(pKeyName),
                    SqlBuilder.isEqualTo(idObj)).build().render(RenderingStrategies.MYBATIS3);
            Map<String, Object> mainAllMap = Optional.ofNullable(flowFormDataMapper.selectOneMappedRow(render)).orElse(new HashMap<>());
            if (mainAllMap.size() == 0) {
                return vo;
            }
            //主表
            List<String> mainTableFields = mast.stream().filter(m -> StringUtil.isNotEmpty(m.getFormColumnModel().getFieLdsModel().getVModel()))
                    .map(s ->
                            {
                                String jnpfKey = s.getFormColumnModel().getFieLdsModel().getConfig().getJnpfKey();
                                String modelFiled = s.getFormColumnModel().getFieLdsModel().getVModel();
                                if (oracle || IS_DM) {
                                    if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                                        modelFiled = "dbms_lob.substr( " + modelFiled + ")";
                                    }
                                }
                                return modelFiled;
                            }
                    ).collect(Collectors.toList());
            if (StringUtil.isNotEmpty(version)) {
                mainTableFields.add(version);
            }
            List<BasicColumn> mainTableBasicColumn = mainTableFields.stream().map(m -> {
                if (m.contains("(")) {
                    String replace = m.replace("dbms_lob.substr(", "");
                    String alisaName = replace.replace(")", "");
                    return SqlTable.of(mainTable.getTable()).column(m).as(alisaName);
                } else {
                    return SqlTable.of(mainTable.getTable()).column(m);
                }
            }).collect(Collectors.toList());
            //无字段时查询主键
            mainTableBasicColumn.add(SqlTable.of(mainTable.getTable()).column(pKeyName));

            SelectStatementProvider mainRender = SqlBuilder.select(mainTableBasicColumn).from(mainSqlTable).where(mainSqlTable.column(pKeyName),
                    SqlBuilder.isEqualTo(idObj)).build().render(RenderingStrategies.MYBATIS3);
            Map<String, Object> mainMap = flowFormDataMapper.selectOneMappedRow(mainRender);
            if (ObjectUtil.isNotEmpty(mainMap)) {
                //转换主表里的数据
                List<FieLdsModel> mainFieldList = mast.stream().filter(m -> StringUtil.isNotEmpty(m.getFormColumnModel().getFieLdsModel().getVModel()))
                        .map(t -> t.getFormColumnModel().getFieLdsModel()).collect(Collectors.toList());
                mainMap = onlineDevInfoUtils.swapDataInfoType(mainFieldList, mainMap);
                allDataMap.putAll(mainMap);
            }

            //列表子表
            Map<String, List<FormMastTableModel>> groupByTableNames = mastTable.stream().map(mt -> mt.getFormMastTableModel()).collect(Collectors.groupingBy(ma -> ma.getTable()));
            Iterator<Map.Entry<String, List<FormMastTableModel>>> entryIterator = groupByTableNames.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, List<FormMastTableModel>> next = entryIterator.next();
                String childTableName = next.getKey();
                List<FormMastTableModel> childMastTableList = next.getValue();
                TableModel childTableModel = tableModelList.stream().filter(t -> t.getTable().equals(childTableName)).findFirst().orElse(null);
                SqlTable mastSqlTable = SqlTable.of(childTableName);
                List<BasicColumn> mastTableBasicColumn = childMastTableList.stream().filter(m -> StringUtil.isNotEmpty(m.getField()))
                        .map(m -> {
                            String jnpfKey = m.getMastTable().getFieLdsModel().getConfig().getJnpfKey();
                            String modelFiled = m.getField();
                            String aliasName = "";
                            if (oracle || IS_DM) {
                                if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                                    aliasName = m.getField();
                                    modelFiled = "dbms_lob.substr( " + modelFiled + ")";
                                }
                            }
                            return StringUtil.isEmpty(aliasName) ? mastSqlTable.column(modelFiled) : mastSqlTable.column(modelFiled).as(aliasName);
                        }).collect(Collectors.toList());
                //添加副表关联字段，不然数据会空没有字段名称
                mastTableBasicColumn.add(mastSqlTable.column(childTableModel.getTableField()));
                String relation = isSnowFlake ? childTableModel.getRelationField().toLowerCase() : TableFeildsEnum.FLOWTASKID.getField();
                String relationValue = String.valueOf(OnlinePublicUtils.mapKeyToLower(mainAllMap).get(relation));
                SelectStatementProvider mastRender = SqlBuilder.select(mastTableBasicColumn).from(mastSqlTable).where(mastSqlTable.column(childTableModel.getTableField()),
                        SqlBuilder.isEqualTo(relationValue)).build().render(RenderingStrategies.MYBATIS3);
                Map<String, Object> soloDataMap = flowFormDataMapper.selectOneMappedRow(mastRender);
                if (ObjectUtil.isNotEmpty(soloDataMap)) {
                    Map<String, Object> renameKeyMap = new HashMap<>();
                    for (Map.Entry entry : soloDataMap.entrySet()) {
                        FormMastTableModel model = childMastTableList.stream().filter(child -> child.getField().equalsIgnoreCase(String.valueOf(entry.getKey()))).findFirst().orElse(null);
                        if (model != null) {
                            renameKeyMap.put(model.getVModel(), entry.getValue());
                        }
                    }
                    List<FieLdsModel> columnChildFields = childMastTableList.stream().map(cl -> cl.getMastTable().getFieLdsModel()).collect(Collectors.toList());
                    renameKeyMap = onlineDevInfoUtils.swapDataInfoType(columnChildFields, renameKeyMap);
                    allDataMap.putAll(renameKeyMap);
                }
            }

            //设计子表
            table.stream().map(t -> t.getChildList()).forEach(
                    t1 -> {
                        String childTableName = t1.getTableName();
                        TableModel tableModel = tableModelList.stream().filter(tm -> tm.getTable().equals(childTableName)).findFirst().orElse(null);
                        SqlTable childSqlTable = SqlTable.of(childTableName);
                        List<BasicColumn> childFields = t1.getChildList().stream().filter(t2 -> StringUtil.isNotEmpty(t2.getFieLdsModel().getVModel()))
                                .map(
                                        t2 -> {
                                            String jnpfKey = t2.getFieLdsModel().getConfig().getJnpfKey();
                                            String modelFiled = t2.getFieLdsModel().getVModel();
                                            String aliasName = "";
                                            if (oracle || IS_DM) {
                                                if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                                                    aliasName = t2.getFieLdsModel().getVModel();
                                                    modelFiled = "dbms_lob.substr( " + modelFiled + ")";
                                                }
                                            }
                                            return StringUtil.isEmpty(aliasName) ? childSqlTable.column(modelFiled) : childSqlTable.column(modelFiled).as(aliasName);
                                        }).collect(Collectors.toList());
                        String relation = Objects.equals(visualdevEntity.getEnableFlow(), 1) && Objects.equals(formData.getPrimaryKeyPolicy(), 2) ?
                                TableFeildsEnum.FLOWTASKID.getField() : tableModel.getRelationField().toLowerCase();
                        String relationValue = String.valueOf(OnlinePublicUtils.mapKeyToLower(mainAllMap).get(relation));
                        TableFields childKeyField = tableModel.getFields().stream().filter(t -> Objects.equals(1, t.getPrimaryKey())).findFirst().orElse(null);
                        String childKey = childKeyField == null ? TableFeildsEnum.FID.getField() : childKeyField.getColumnName();
                        childFields.add(childSqlTable.column(childKey));
                        SelectStatementProvider childRender = SqlBuilder.select(childFields).from(childSqlTable).where(childSqlTable.column(tableModel.getTableField()),
                                SqlBuilder.isEqualTo(relationValue)).build().render(RenderingStrategies.MYBATIS3);
                        try {
                            List<Map<String, Object>> childMapList = flowFormDataMapper.selectManyMappedRows(childRender);
                            Map<String, Object> childMap = new HashMap<>(1);
                            if (ObjectUtil.isNotEmpty(childMapList)) {
                                List<FieLdsModel> childFieldModels = t1.getChildList().stream().map(t2 -> t2.getFieLdsModel()).collect(Collectors.toList());
                                childMapList = childMapList.stream().map(c1 -> {
                                    try {
                                        return onlineDevInfoUtils.swapDataInfoType(childFieldModels, c1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return c1;
                                }).collect(Collectors.toList());
                                childMap.put(t1.getTableModel(), childMapList);
                            } else {
                                childMap.put(t1.getTableModel(), new ArrayList<>());
                            }
                            allDataMap.putAll(childMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        vo.setId(id);
        vo.setData(JsonUtilEx.getObjectToString(allDataMap));
        return vo;
    }


    @Override
    public VisualdevModelDataInfoVO getDetailsDataInfo(String id, VisualdevEntity visualdevEntity) {
        return this.getDetailsDataInfo(id,visualdevEntity, OnlineInfoModel.builder().needRlationFiled(true).build());

    }
    @Override
    public VisualdevModelDataInfoVO getDetailsDataInfoNew(String id, String visualdevId) {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(visualdevId);
        return getDetailsDataInfo(id, visualdevEntity);
    }
    public VisualdevModelDataInfoVO infoDataChangeNew(String id, String visualdevId) throws DataException, SQLException, IOException, ParseException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(visualdevId);
        return visualdevModelDataService.infoDataChange(id, visualdevEntity);
    }
    @Override
    public VisualdevModelDataInfoVO getDetailsDataInfo(String id, VisualdevEntity visualdevEntity, OnlineInfoModel infoModel) {
        id = XSSEscape.escape(id);
        VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
        Map<String, Object> allDataMap = new HashMap<>();
        Map<String, Object> allDataResMap = new HashMap<>();
        FormDataModel formData = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<TableModel> tableModelList = JsonUtil.getJsonToList(visualdevEntity.getVisualTables(), TableModel.class);
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setList(list);
        recursionForm.setTableModelList(tableModelList);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        //form的属性
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormModel> codeList = formAllModel.stream().filter(t -> t.getJnpfKey().equals(FormEnum.BARCODE.getMessage())
                || t.getJnpfKey().equals(FormEnum.QR_CODE.getMessage())).map(formModel -> formModel.getFormModel()).collect(Collectors.toList());

        List<String> relationFiled=new ArrayList(){{
            add(JnpfKeyConsts.RELATIONFORM);
            add(JnpfKeyConsts.RELATIONFORM_ATTR);
        }};

        TableModel mainTable = tableModelList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);
        boolean isSnowFlake = visualdevEntity.getEnableFlow() == 0;
        DbLinkEntity linkEntity = dbLinkService.getInfo(visualdevEntity.getDbLinkId());
        try {
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            boolean oracle = databaseProductName.equalsIgnoreCase("oracle");
            boolean IS_DM = databaseProductName.equalsIgnoreCase("DM DBMS");
            Object idObj=id;
            if(formData.getPrimaryKeyPolicy() == 2){
                idObj=Long.parseLong(id);
            }
            //获取主键
            Integer primaryKeyPolicy = formData.getPrimaryKeyPolicy();
            if (primaryKeyPolicy == 2 && isSnowFlake) {
                primaryKeyPolicy = 1;
            }
            String pKeyName = flowDataUtil.getKey(conn, mainTable.getTable(), primaryKeyPolicy);
            //主表所有数据
            SqlTable mainSqlTable = SqlTable.of(mainTable.getTable());
            SelectStatementProvider render = SqlBuilder.select(mainSqlTable.allColumns()).from(mainSqlTable).where(mainSqlTable.column(pKeyName),
                    SqlBuilder.isEqualTo(idObj)).build().render(RenderingStrategies.MYBATIS3);
            Map<String, Object> mainAllMap = Optional.ofNullable(flowFormDataMapper.selectOneMappedRow(render)).orElse(new HashMap<>());
            if (mainAllMap.size() == 0) {
                return vo;
            }
            //是否去除关联表单及关联表单字段
            if (!infoModel.isNeedRlationFiled()) {
                mast = mast.stream().filter(t -> !relationFiled.contains(t.getFormColumnModel().getFieLdsModel().getConfig().getJnpfKey())).collect(Collectors.toList());
            }
            //主表
            List<String> mainTableFields = mast.stream().filter(m -> StringUtil.isNotEmpty(m.getFormColumnModel().getFieLdsModel().getVModel()))
                    .map(s ->
                            {
                                String jnpfKey = s.getFormColumnModel().getFieLdsModel().getConfig().getJnpfKey();
                                String modelFiled = s.getFormColumnModel().getFieLdsModel().getVModel();
                                if (oracle || IS_DM) {
                                    if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                                        modelFiled = "dbms_lob.substr( " + modelFiled + ")";
                                    }
                                }
                                return modelFiled;
                            }
                    ).collect(Collectors.toList());
            List<BasicColumn> mainTableBasicColumn = mainTableFields.stream().map(m -> {
                if (m.contains("(")) {
                    String replace = m.replace("dbms_lob.substr(", "");
                    String alisaName = replace.replace(")", "");
                    return SqlTable.of(mainTable.getTable()).column(m).as(alisaName);
                } else {
                    return SqlTable.of(mainTable.getTable()).column(m);
                }
            }).collect(Collectors.toList());
            //无字段时查询主键
            mainTableBasicColumn.add(SqlTable.of(mainTable.getTable()).column(pKeyName));

            SelectStatementProvider mainRender = SqlBuilder.select(mainTableBasicColumn).from(mainSqlTable).where(mainSqlTable.column(pKeyName),
                    SqlBuilder.isEqualTo(idObj)).build().render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> mapList = flowFormDataMapper.selectManyMappedRows(mainRender);
            if (ObjectUtil.isNotEmpty(mapList) && mapList.size() > 0) {
                allDataMap.putAll(mapList.get(0));
            }

            //列表子表
            Map<String, List<FormMastTableModel>> groupByTableNames = mastTable.stream().map(mt -> mt.getFormMastTableModel()).collect(Collectors.groupingBy(ma -> ma.getTable()));
            Iterator<Map.Entry<String, List<FormMastTableModel>>> entryIterator = groupByTableNames.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, List<FormMastTableModel>> next = entryIterator.next();
                String childTableName = next.getKey();
                List<FormMastTableModel> childMastTableList = next.getValue();
                TableModel childTableModel = tableModelList.stream().filter(t -> t.getTable().equals(childTableName)).findFirst().orElse(null);
                SqlTable mastSqlTable = SqlTable.of(childTableName);
                //是否去除关联表单及关联表单字段
                if (!infoModel.isNeedRlationFiled()) {
                    childMastTableList = childMastTableList.stream().filter(t -> !relationFiled.contains(t.getMastTable().getFieLdsModel().getConfig().getJnpfKey())).collect(Collectors.toList());
                }
                List<BasicColumn> mastTableBasicColumn = childMastTableList.stream().filter(m -> StringUtil.isNotEmpty(m.getField()))
                        .map(m -> {
                            String jnpfKey = m.getMastTable().getFieLdsModel().getConfig().getJnpfKey();
                            String modelFiled = m.getField();
                            String aliasName = "";
                            if (oracle || IS_DM) {
                                if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                                    aliasName = m.getField();
                                    modelFiled = "dbms_lob.substr( " + modelFiled + ")";
                                }
                            }
                            return StringUtil.isEmpty(aliasName) ? mastSqlTable.column(modelFiled) : mastSqlTable.column(modelFiled).as(aliasName);
                        }).collect(Collectors.toList());
                //添加副表关联字段，不然数据会空没有字段名称
                mastTableBasicColumn.add(mastSqlTable.column(childTableModel.getTableField()));
                //当流程自增长是用FLOWTASKID
                String relation = formData.getPrimaryKeyPolicy()== 2 && visualdevEntity.getEnableFlow() == 1 ?
                        TableFeildsEnum.FLOWTASKID.getField() : childTableModel.getRelationField().toLowerCase();
                String relationValue = String.valueOf(OnlinePublicUtils.mapKeyToLower(mainAllMap).get(relation));
                SelectStatementProvider mastRender = SqlBuilder.select(mastTableBasicColumn).from(mastSqlTable).where(mastSqlTable.column(childTableModel.getTableField()),
                        SqlBuilder.isEqualTo(relationValue)).build().render(RenderingStrategies.MYBATIS3);
                Map<String, Object> soloDataMap = flowFormDataMapper.selectOneMappedRow(mastRender);
                if (ObjectUtil.isNotEmpty(soloDataMap)) {
                    Map<String, Object> renameKeyMap = new HashMap<>();
                    for (Map.Entry entry : soloDataMap.entrySet()) {
                        FormMastTableModel model = childMastTableList.stream().filter(child -> child.getField().equalsIgnoreCase(String.valueOf(entry.getKey()))).findFirst().orElse(null);
                        if (model != null) {
                            renameKeyMap.put(model.getVModel(), entry.getValue());
                        }
                    }
                    List<Map<String, Object>> mapList1 = new ArrayList<>();
                    mapList1.add(renameKeyMap);
                    allDataMap.putAll(mapList1.get(0));
                }
            }

            //设计子表
            table.stream().map(t -> t.getChildList()).forEach(
                    t1 -> {
                        String childTableName = t1.getTableName();
                        List<FormColumnModel> childList = t1.getChildList();
                        TableModel tableModel = tableModelList.stream().filter(tm -> tm.getTable().equals(childTableName)).findFirst().orElse(null);
                        SqlTable childSqlTable = SqlTable.of(childTableName);
                        //是否去除关联表单及关联表单字段
                        if (!infoModel.isNeedRlationFiled()) {
                            childList = childList.stream().filter(t -> !relationFiled.contains(t.getFieLdsModel().getConfig().getJnpfKey())).collect(Collectors.toList());
                        }
                        List<BasicColumn> childFields = childList.stream().filter(t2 -> StringUtil.isNotEmpty(t2.getFieLdsModel().getVModel()))
                                .map(
                                        t2 -> {
                                            String jnpfKey = t2.getFieLdsModel().getConfig().getJnpfKey();
                                            String modelFiled = t2.getFieLdsModel().getVModel();
                                            String aliasName = "";
                                            if (oracle || IS_DM) {
                                                if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                                                    aliasName = t2.getFieLdsModel().getVModel();
                                                    modelFiled = "dbms_lob.substr( " + modelFiled + ")";
                                                }
                                            }
                                            return StringUtil.isEmpty(aliasName) ? childSqlTable.column(modelFiled) : childSqlTable.column(modelFiled).as(aliasName);
                                        }).collect(Collectors.toList());
                        String relation = Objects.equals(visualdevEntity.getEnableFlow(), 1) && Objects.equals(formData.getPrimaryKeyPolicy(), 2) ?
                                TableFeildsEnum.FLOWTASKID.getField() : tableModel.getRelationField().toLowerCase();
                        String relationValue = String.valueOf(OnlinePublicUtils.mapKeyToLower(mainAllMap).get(relation));

                        SelectStatementProvider childRender = SqlBuilder.select(childFields).from(childSqlTable).where(childSqlTable.column(tableModel.getTableField()),
                                SqlBuilder.isEqualTo(relationValue)).build().render(RenderingStrategies.MYBATIS3);
                        List<Map<String, Object>> childMapList = flowFormDataMapper.selectManyMappedRows(childRender);
                        if (ObjectUtil.isNotEmpty(childMapList)) {
                            Map<String, Object> childMap = new HashMap<>(1);
                            childMap.put(t1.getTableModel(), childMapList);
                            allDataMap.putAll(childMap);
                        }
                    }
            );
            //数据转换
            List<FieLdsModel> fields = new ArrayList<>();
            OnlinePublicUtils.recursionFields(fields, list);

            VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
            //添加id属性
            List<Map<String,Object>> dataList = FormPublicUtils.addIdToList(new ArrayList() {{
                add(allDataMap);
            }},pKeyName,visualJsonModel.isFlowEnable());
            //详情没有区分行内编辑
            allDataResMap = (Map<String, Object>)onlineSwapDataUtils.getSwapInfo(dataList, fields, visualdevEntity.getId(), false, codeList, null).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        vo.setId(id);
        vo.setData(JsonUtilEx.getObjectToString(allDataResMap));
        return vo;
    }
}
