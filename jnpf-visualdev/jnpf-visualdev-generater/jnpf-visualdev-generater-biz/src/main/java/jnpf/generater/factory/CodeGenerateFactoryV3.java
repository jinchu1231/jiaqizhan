package jnpf.generater.factory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.google.common.base.CaseFormat;
import jnpf.base.UserInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.DownloadCodeForm;
import jnpf.base.model.Template6.AuthorityModel;
import jnpf.base.model.Template6.BtnData;
import jnpf.base.model.Template6.ColumnListField;
import jnpf.base.model.Template7.Template7Model;
import jnpf.base.model.VisualWebTypeEnum;
import jnpf.base.util.SourceUtil;
import jnpf.base.util.VisualUtils;
import jnpf.base.util.app.AppGenModel;
import jnpf.base.util.app.AppGenUtil;
import jnpf.base.util.common.DataControlUtils;
import jnpf.base.util.common.FormCommonUtil;
import jnpf.base.util.common.FunctionFormPublicUtil;
import jnpf.base.util.common.SuperQueryUtil;
import jnpf.base.util.custom.CustomGenerator;
import jnpf.base.util.fuctionFormVue3.GenerateForm;
import jnpf.base.util.fuctionFormVue3.GenerateFormFlow;
import jnpf.base.util.fuctionFormVue3.GenerateFormList;
import jnpf.base.util.fuctionFormVue3.GenerateFormListFlow;
import jnpf.base.util.fuctionFormVue3.GenerateWorkFlow;
import jnpf.base.util.fuctionFormVue3.common.GenerateCommon;
import jnpf.base.util.fuctionFormVue3.common.GenerateInterface;
import jnpf.base.util.fuctionFormVue3.common.GenerateParamModel;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.entity.FlowFormEntity;
import jnpf.generater.model.FormDesign.ColumnListDataModel;
import jnpf.generater.model.FormDesign.ListSearchGroupModel;
import jnpf.generater.model.FormDesign.SearchTypeModel;
import jnpf.generater.model.FormDesign.TemplateMethodEnum;
import jnpf.generater.model.GenFileNameSuffix;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormCloumnUtil;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableFields;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.TemplateJsonModel;
import jnpf.model.visualJson.UploaderTemplateModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.FormColumnModel;
import jnpf.model.visualJson.analysis.FormColumnTableModel;
import jnpf.model.visualJson.analysis.FormEnum;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.model.visualJson.config.HeaderModel;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.XSSEscape;
import jnpf.util.visiual.JnpfKeyConsts;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.velocity.VelocityContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/5/31
 */
@Component
public class CodeGenerateFactoryV3 {

    /**
     * 根据模板路径对应实体
     *
     * @param templateMethod
     * @return
     */
    public void runGenerator(String templateMethod, GenerateParamModel generateParamModel) throws Exception {
        GenerateInterface codeGenerateUtil = null;
        if (templateMethod.equals(TemplateMethodEnum.T1.getMethod())) {
            codeGenerateUtil = new GenerateWorkFlow();
        } else if (templateMethod.equals(TemplateMethodEnum.T2.getMethod())) {
            codeGenerateUtil = new GenerateFormList();
        } else if (templateMethod.equals(TemplateMethodEnum.T3.getMethod())) {
            codeGenerateUtil = new GenerateFormListFlow();
        } else if (templateMethod.equals(TemplateMethodEnum.T4.getMethod())) {
            codeGenerateUtil = new GenerateForm();
        } else if (templateMethod.equals(TemplateMethodEnum.T5.getMethod())) {
            codeGenerateUtil = new GenerateFormFlow();
        } else {
            codeGenerateUtil = null;
        }
        //生成后端代码
        GenerateParamModel javaObj = BeanUtil.copyProperties(generateParamModel, GenerateParamModel.class);
        this.generateJava(javaObj, codeGenerateUtil);
    }

    /**
     * 生成java代码
     *
     * @param codeUtil           生成重写接口
     * @param generateParamModel
     * @throws Exception
     */
    private void generateJava(GenerateParamModel generateParamModel, GenerateInterface codeUtil) throws Exception {
        List<TableModel> list = JsonUtil.getJsonToList(generateParamModel.getEntity().getVisualTables(), TableModel.class);
        //表别名
        Map<String, String> tableNameAll = tableName(list, generateParamModel.getDownloadCodeForm());
        for (TableModel model : list) {
            generateParamModel.setTable(model.getTable());
            generateParamModel.setClassName(tableNameAll.get(model.getTable()));
            generateParamModel.setMainTable(false);
            if ("1".equals(model.getTypeId())) {
                generateParamModel.setMainTable(true);
                //生成主表代码
                this.setCode(generateParamModel, codeUtil);
                //前端代码
                this.generateHtml(generateParamModel, codeUtil);
                //生成app代码
                this.generateApp(generateParamModel, codeUtil);
            } else if ("0".equals(model.getTypeId())) {
                //生成子表代码
                this.setCode(generateParamModel, codeUtil);
            }
        }
    }

    /**
     * 封装表对应的输出名字
     */
    private Map<String, String> tableName(List<TableModel> tableModelList, DownloadCodeForm downloadCodeForm) {
        Map<String, String> tableClass = new HashMap<>(16);
        int i = 0;
        for (TableModel tableModel : tableModelList) {
            if ("0".equals(tableModel.getTypeId())) {
                String[] subClassName = downloadCodeForm.getSubClassName().split(",");
                tableClass.put(tableModel.getTable(), subClassName[i]);
                i++;
            }
            if ("1".equals(tableModel.getTypeId())) {
                tableClass.put(tableModel.getTable(), downloadCodeForm.getClassName());
            }
        }
        return tableClass;
    }

    /**
     * 获取传递参数
     *
     * @param generateParamModel
     * @return
     * @throws Exception
     */
    private Map<String, Object> getcolumndata(GenerateParamModel generateParamModel) throws Exception {
        Map<String, Object> columndata = new HashMap<>(16);
        DownloadCodeForm downloadCodeForm = generateParamModel.getDownloadCodeForm();
        VisualdevEntity entity = generateParamModel.getEntity();
        DbLinkEntity linkEntity = generateParamModel.getLinkEntity();
        String className = generateParamModel.getClassName();
        String pKeyName = generateParamModel.getPKeyName();
        //代码生成基础信息
        Template7Model template7Model = GenerateCommon.getTemplate7Model(className);
        template7Model.setTableName(downloadCodeForm.getClassName());
        template7Model.setDescription(downloadCodeForm.getDescription());
        generateParamModel.setTemplate7Model(template7Model);
        //自定义包名
        String modulePackageName = downloadCodeForm.getModulePackageName();
        //tableJson
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class)
                .stream().sorted(Comparator.comparing(TableModel::getTypeId).reversed()).collect(Collectors.toList());
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        //设置乐观锁参数用于其他位置判断
        generateParamModel.setConcurrencyLock(formData.getConcurrencyLock());
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        //主表
        TableModel mainTable = tableModelList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);
        //表别名
        Map<String, String> tableRenames = tableName(tableModelList, generateParamModel.getDownloadCodeForm());
        String mainModelName = DataControlUtils.captureName(tableRenames.get(mainTable.getTable()));
        //赋值主键
        tableModelList.stream().forEach(t -> {
            try {
                t.setTableKey(VisualUtils.getpKey(linkEntity, t.getTable()));
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });

        //所有控件
        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        recursionForm.setList(list);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        //主表控件
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //子表控件
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //副表控件
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());

        List<String> subTableName = new ArrayList<>();
        for (FormAllModel allModel : table) {
            FormColumnTableModel childList = allModel.getChildList();
            if (childList != null) {
                subTableName.add(childList.getTableName());
            }
        }
        //子表（tableField,tableName）->tablefield1->realname
        Map<String, String> childKeyTableNameMap = new HashMap<>(8);
        table.stream().forEach(t -> childKeyTableNameMap.put(t.getChildList().getTableModel(), t.getChildList().getTableName()));

        //子表集合
        List<TableModel> childTableNameList = new ArrayList<>();
        //全部表
        List<TableModel> allTableNameList = new ArrayList<>();
        for (TableModel tableModel : tableModelList) {
            TableModel Model = new TableModel();
            Model.setInitName(tableModel.getTable());
            Model.setTable(tableRenames.get(tableModel.getTable()));
            Model.setTableField(DataControlUtils.captureName(tableModel.getTableField()));
            Model.setFields(tableModel.getFields());
            if (tableModel.getTable().equals(mainTable.getTable())) {
                Model.setTableTag("main");
            } else {
                Model.setRelationField(DataControlUtils.captureName(tableModel.getRelationField()));
                Model.setTableTag(subTableName.contains(tableModel.getTable()) ? "sub" : "sub-jnpf");
            }
            allTableNameList.add(Model);
            if ("0".equals(tableModel.getTypeId())) {
                childTableNameList.add(Model);
            }
        }
        TableModel mainTableModel = tableModelList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);
        //主表的字段
        if (mainTableModel == null || CollectionUtils.isEmpty(mainTableModel.getFields())) {
            throw new SQLException(MsgCode.COD001.get());
        }

        //主表的属性
        List<FieLdsModel> mastTableHandle = new ArrayList<>();
        for (int i = 0; i < mast.size(); i++) {
            FormAllModel mastModel = mast.get(i);
            FieLdsModel fieLdsModel = mastModel.getFormColumnModel().getFieLdsModel();
            //接口templatejson转换
            if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                List<TemplateJsonModel> templateJson = fieLdsModel.getConfig().getTemplateJson();
                String json = templateJson.size() > 0 ? JsonUtil.getObjectToString(templateJson) : fieLdsModel.getTemplateJson();
                fieLdsModel.setTemplateJson(json);
                mastTableHandle.add(fieLdsModel);
            }
        }
        //副表模型
        List<ColumnListDataModel> columnTableHandle = new ArrayList<>();
        //副表数据model
        Map<String, List<FormAllModel>> groupColumnDataMap = mastTable.stream().collect(Collectors.groupingBy(m -> m.getFormMastTableModel().getTable()));
        for (String key : groupColumnDataMap.keySet()) {
            String classNameMast = DataControlUtils.captureName(tableRenames.get(key));
            ColumnListDataModel columnListDataModel = new ColumnListDataModel();
            columnListDataModel.setModelName(classNameMast);
            columnListDataModel.setModelUpName(DataControlUtils.captureName(classNameMast));
            columnListDataModel.setModelLowName(DataControlUtils.initialLowercase(classNameMast));
            List<FormAllModel> allModels = groupColumnDataMap.get(key);
            List<String> fields = allModels.stream().map(m -> m.getFormMastTableModel().getField()).collect(Collectors.toList());
            columnListDataModel.setFieldList(fields);
            columnListDataModel.setFieLdsModelList(allModels.stream().map(al -> al.getFormMastTableModel()).collect(Collectors.toList()));
            columnListDataModel.setTableName(key);
            List<FieLdsModel> collect = allModels.stream().map(all -> all.getFormMastTableModel().getMastTable().getFieLdsModel()).collect(Collectors.toList());
            collect.stream().forEach(c -> {
                String vmodel = c.getVModel().substring(c.getVModel().lastIndexOf("jnpf_")).replace("jnpf_", "");
                c.setVModel(vmodel);
                List<TemplateJsonModel> templateJson = c.getConfig().getTemplateJson();
                String json = templateJson.size() > 0 ? JsonUtil.getObjectToString(templateJson) : c.getTemplateJson();
                c.setTemplateJson(json);
            });
            columnListDataModel.setFieLdsModels(collect);

            TableModel tableModel = tableModelList.stream().filter(t -> t.getTable().equalsIgnoreCase(columnListDataModel.getTableName())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(tableModel)) {
                columnListDataModel.setMainKey(tableModel.getRelationField());
                columnListDataModel.setRelationField(tableModel.getTableField());
                columnListDataModel.setMainUpKey(DataControlUtils.captureName(tableModel.getRelationField()));
                columnListDataModel.setRelationUpField(DataControlUtils.captureName(tableModel.getTableField()));
                String tableKey = tableModel.getTableKey().toLowerCase().replace("f_", "");
                tableKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableKey);
                columnListDataModel.setMainField(DataControlUtils.captureName(tableKey));
            }
            columnTableHandle.add(columnListDataModel);
        }
        //子表的属性
        List<Map<String, Object>> childTableHandle = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            List<FormColumnModel> childListAll = childList.getChildList();
            String classNameChild = DataControlUtils.captureName(tableRenames.get(childList.getTableName()));
            //子表别名
            childList.setAliasClassName(classNameChild);
            childList.setAliasUpName(DataControlUtils.captureName(classNameChild));
            childList.setAliasLowName(DataControlUtils.initialLowercase(classNameChild));
            for (FormColumnModel columnModel : childListAll) {
                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                List<TemplateJsonModel> templateJson = fieLdsModel.getConfig().getTemplateJson();
                String json = templateJson.size() > 0 ? JsonUtil.getObjectToString(templateJson) : fieLdsModel.getTemplateJson();
                fieLdsModel.setTemplateJson(json);
            }
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            Optional<TableModel> first1 = tableModelList.stream().filter(t -> t.getTable().equals(childList.getTableName())).findFirst();
            if (!first1.isPresent()) {
                throw new SQLException(MsgCode.COD001.get());
            }
            TableModel tableModel = first1.get();
            //获取主键-外键字段-关联主表字段
            String chidKeyName = VisualUtils.getpKey(linkEntity, tableModel.getTable());
            String chidKeyField = chidKeyName.trim().toLowerCase().replaceAll("f_", "");
            chidKeyField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, chidKeyField);
            childs.put("chidKeyName", chidKeyField);
            String tableField = tableModel.getTableField().trim().replaceAll(":\"f_", ":\"");
            childs.put("tablefield", tableField);
            String relationField = tableModel.getRelationField().trim().replaceAll(":\"f_", ":\"");
            childs.put("relationField", relationField);
            childTableHandle.add(childs);
        }

        //++++++++++++++++++++++++++++主副子通用参数++++++++++++++++++++++++++//
        //微服务标识
        columndata.put("isCloud", GenerateCommon.IS_CLOUD);
        //是列表，是流程判断
        if (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType()) && !Objects.equals(entity.getType(), 3)) {
            columndata.put("isList", true);
            //添加列表参数
            getListColumndata(generateParamModel, columndata);
        }
        if (Objects.equals(entity.getEnableFlow(), 1)) {
            columndata.put("isFlow", true);
        }
        //后台
        columndata.put("module", downloadCodeForm.getModule());
        columndata.put("genInfo", template7Model);
        columndata.put("modelName", template7Model.getClassName());
        //表单非系统控件字段--为了加null可以更新
        columndata.put("tableNotSystemField", GenerateCommon.getNotSystemFields(mast, mastTable, table, generateParamModel));
        //主副子 控件字段（已处理数据）
        columndata.put("mastTableHandle", mastTableHandle);//原system
        columndata.put("columnTableHandle", columnTableHandle);//原columnChildren
        columndata.put("childTableHandle", childTableHandle);//原child
        columndata.put("mainModelName", mainModelName);
        //数据源
        if (ObjectUtil.isNotEmpty(linkEntity)) {
            columndata.put("DS", linkEntity.getFullName());
        }
        // 数据源配置
        DataSourceConfig dsc = SourceUtil.dbConfig(TenantDataSourceUtil.getTenantSchema(), linkEntity);
        //数据库类型
        columndata.put("dbType", dsc.getDbType().getDb());
        // 包名
        columndata.put("modulePackageName", modulePackageName);
        columndata.put("pKeyName", pKeyName);
        columndata.put("VisualDevId", entity.getId());
        columndata.put("allTableNameList", allTableNameList);
        //++++++++++++++++++++++++++++仅主表参数++++++++++++++++++++++++++//
        if (generateParamModel.isMainTable()) {
            //后台
            columndata.put("main", true);
            //模板名称
            columndata.put("formModelName", entity.getFullName());
            //乐观锁
            columndata.put("version", formData.getConcurrencyLock());
            TableFields versionField = mainTable.getFields().stream().filter(t ->
                    TableFeildsEnum.VERSION.getField().equalsIgnoreCase(t.getColumnName())).findFirst().orElse(null);
            columndata.put("versionType", versionField != null ? versionField.getDataType() : "");
            //删除标志
            columndata.put("logicalDelete", formData.getLogicalDelete());
            //雪花
            columndata.put("snowflake", formData.getPrimaryKeyPolicy() == 1);
        }
        return columndata;
    }

    /**
     * 获取列表传递参数
     *
     * @param generateParamModel
     * @return
     * @throws Exception
     */
    private void getListColumndata(GenerateParamModel generateParamModel, Map<String, Object> listMap) throws Exception {
        DownloadCodeForm downloadCodeForm = generateParamModel.getDownloadCodeForm();
        VisualdevEntity entity = generateParamModel.getEntity();
        DbLinkEntity linkEntity = generateParamModel.getLinkEntity();
        String className = generateParamModel.getClassName();
        //代码生成基础信息
        Template7Model template7Model = GenerateCommon.getTemplate7Model(className);
        template7Model.setTableName(downloadCodeForm.getClassName());
        template7Model.setDescription(downloadCodeForm.getDescription());
        generateParamModel.setTemplate7Model(template7Model);
        //tableJson
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class)
                .stream().sorted(Comparator.comparing(TableModel::getTypeId).reversed()).collect(Collectors.toList());
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        //设置乐观锁参数用于其他位置判断
        generateParamModel.setConcurrencyLock(formData.getConcurrencyLock());
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        //主表
        TableModel mainTable = tableModelList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);
        //表别名
        Map<String, String> tableRenames = tableName(tableModelList, generateParamModel.getDownloadCodeForm());
        //赋值主键
        tableModelList.stream().forEach(t -> {
            try {
                t.setTableKey(VisualUtils.getpKey(linkEntity, t.getTable()));
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });
        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        recursionForm.setList(list);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        //主表数据
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //子表数据
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //副表数据
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<String> subTableName = new ArrayList<>();
        for (FormAllModel allModel : table) {
            FormColumnTableModel childList = allModel.getChildList();
            if (childList != null) {
                subTableName.add(childList.getTableName());
            }
        }
        //子表集合
        List<TableModel> childTableNameList = new ArrayList<>();
        //全部表
        List<TableModel> allTableNameList = new ArrayList<>();
        for (TableModel tableModel : tableModelList) {
            TableModel Model = new TableModel();
            Model.setInitName(tableModel.getTable());
            Model.setTable(tableRenames.get(tableModel.getTable()));
            Model.setTableField(DataControlUtils.captureName(tableModel.getTableField()));
            Model.setFields(tableModel.getFields());
            if (tableModel.getTable().equals(mainTable.getTable())) {
                Model.setTableTag("main");
            } else {
                Model.setRelationField(DataControlUtils.captureName(tableModel.getRelationField()));
                Model.setTableTag(subTableName.contains(tableModel.getTable()) ? "sub" : "sub-jnpf");
            }
            allTableNameList.add(Model);
            if ("0".equals(tableModel.getTypeId())) {
                childTableNameList.add(Model);
            }
        }
        //子表（tableField,tableName）->tablefield1->realname
        Map<String, String> childKeyTableNameMap = new HashMap<>(8);
        table.stream().forEach(t -> childKeyTableNameMap.put(t.getChildList().getTableModel(), t.getChildList().getTableName()));
        TableModel mainTableModel = tableModelList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);
        //主表的字段
        if (mainTableModel == null || CollectionUtils.isEmpty(mainTableModel.getFields())) {
            throw new SQLException(MsgCode.COD001.get());
        }

        //columnTempJson
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class);
        //app 列表对象
        ColumnDataModel appColumnDataModel = JsonUtil.getJsonToBean(entity.getAppColumnData(), ColumnDataModel.class);

        //按钮
        List<BtnData> btnPcList = JsonUtil.getJsonToList(columnDataModel.getBtnsList(), BtnData.class);
        List<BtnData> columnBtnPcList = JsonUtil.getJsonToList(columnDataModel.getColumnBtnsList(), BtnData.class);
        List<BtnData> btnAppList = JsonUtil.getJsonToList(appColumnDataModel.getBtnsList(), BtnData.class);
        List<BtnData> columnBtnAppList = JsonUtil.getJsonToList(appColumnDataModel.getColumnBtnsList(), BtnData.class);
        List<BtnData> btnDataList = new ArrayList<>(btnPcList);
        List<String> collect3 = btnPcList.stream().map(BtnData::getValue).collect(Collectors.toList());
        btnDataList.addAll(btnAppList.stream().filter(t -> !collect3.contains(t.getValue())).collect(Collectors.toList()));
        List<BtnData> columnBtnDataList = new ArrayList<>(columnBtnPcList);
        List<String> collect4 = columnBtnPcList.stream().map(BtnData::getValue).collect(Collectors.toList());
        columnBtnDataList.addAll(columnBtnAppList.stream().filter(t -> !collect4.contains(t.getValue())).collect(Collectors.toList()));
        //是否有导入按钮--webtype==2开启列表
        boolean hasUploadBtn = Objects.equals(entity.getWebType(), 2) && btnDataList.stream().anyMatch(btn -> btn.getValue().equals("upload"));
        //是否有下载按钮
        boolean hasDownloadBtn = Objects.equals(entity.getWebType(), 2) && btnDataList.stream().anyMatch(btn -> btn.getValue().equals("download"));
        //是否有下载按钮
        boolean hasPrintBtn = Objects.equals(entity.getWebType(), 2) && btnDataList.stream().anyMatch(btn -> btn.getValue().equals("batchPrint"));
        boolean hasRemoveBtn = Objects.equals(entity.getWebType(), 2) && btnDataList.stream().anyMatch(btn -> btn.getValue().equals("batchRemove"));
        //列表和查询
        List<ColumnListField> columnList = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListField.class);
        List<SearchTypeModel> searchList = JsonUtil.getJsonToList(columnDataModel.getSearchList(), SearchTypeModel.class);
        List<ColumnListField> columnAppList = JsonUtil.getJsonToList(appColumnDataModel.getColumnList(), ColumnListField.class);
        List<SearchTypeModel> searchAppList = JsonUtil.getJsonToList(appColumnDataModel.getSearchList(), SearchTypeModel.class);
        //-----------------------------------------------------search start---------------------------------------
        //列表全字段
        List<ColumnListField> columnListAll = new ArrayList<>(columnList);
        List<String> cLaArr = columnListAll.stream().map(ColumnListField::getProp).collect(Collectors.toList());
        columnAppList.stream().forEach(t -> {
            if (!cLaArr.contains(t.getProp())) {
                columnListAll.add(t);
                cLaArr.add(t.getProp());
            }
        });
        //查询全字段
        List<SearchTypeModel> searchListAll = new ArrayList<>(searchList);
        List<String> cSaArr = searchListAll.stream().map(SearchTypeModel::getId).collect(Collectors.toList());
        searchAppList.stream().forEach(t -> {
            if (!cSaArr.contains(t.getId())) {
                searchListAll.add(t);
                cSaArr.add(t.getId());
            }
        });

        //添加左侧树查询字段
        addTreeSearchField(mainTableModel, columnDataModel, searchList);
        addTreeSearchField(mainTableModel, columnDataModel, searchAppList);
        addTreeSearchField(mainTableModel, columnDataModel, searchListAll);

        //查询字段转换
        List<ListSearchGroupModel> groupModels = getListSearchGroupModels(tableModelList, mainTable, tableRenames, childKeyTableNameMap, mainTableModel, searchList);
        List<ListSearchGroupModel> groupAppModels = getListSearchGroupModels(tableModelList, mainTable, tableRenames, childKeyTableNameMap, mainTableModel, searchAppList);
        List<ListSearchGroupModel> groupAllModels = getListSearchGroupModels(tableModelList, mainTable, tableRenames, childKeyTableNameMap, mainTableModel, searchListAll);

        //判断是否有关键词搜索
        boolean keywordModels = searchList.stream().filter(t->t.getIsKeyword()!=null && t.getIsKeyword()).collect(Collectors.toList()).size()>0;
        boolean keywordAppModels = searchAppList.stream().filter(t->t.getIsKeyword()!=null && t.getIsKeyword()).collect(Collectors.toList()).size()>0;

        //-----------------------------------------------------search enddd---------------------------------------
        //权限
        AuthorityModel authority = new AuthorityModel();
        BeanUtil.copyProperties(columnDataModel, authority);
        //导入字段
        List<Map<String, Object>> allUploadTemplates = new ArrayList<>();
        boolean importHasChildren = false;
        String importType = "1";
        UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
        if (hasUploadBtn && uploaderTemplateModel != null && uploaderTemplateModel.getSelectKey() != null) {
            importType = uploaderTemplateModel.getDataType();
            List<String> selectKey = uploaderTemplateModel.getSelectKey();
            Map<String, List<String>> childMap = new HashMap<>();
            //判断是否存在子表的导入导出
            for (String item : selectKey) {
                if (item.toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                    importHasChildren = true;
                    String[] split = item.split("-");
                    if (childMap.get(split[0]) == null) {
                        List<String> keys = new ArrayList<>();
                        keys.add(split[1]);
                        childMap.put(split[0], keys);
                    } else {
                        List<String> keys = childMap.get(split[0]);
                        keys.add(split[1]);
                        childMap.replace(split[0], keys);
                    }
                } else {
                    //主表字段
                    for (FormAllModel fam : mast) {
                        FieLdsModel fieLdsModel = fam.getFormColumnModel().getFieLdsModel();
                        if (item.equals(fieLdsModel.getVModel())) {
                            Map<String, Object> map = JsonUtil.entityToMap(fieLdsModel);
                            map.put("label", fieLdsModel.getConfig().getLabel());
                            allUploadTemplates.add(map);
                        }
                    }
                    //副表字段
                    for (FormAllModel fam : mastTable) {
                        FieLdsModel fieLdsModel = fam.getFormMastTableModel().getMastTable().getFieLdsModel();
                        if (item.equals(fieLdsModel.getVModel())) {
                            Map<String, Object> map = JsonUtil.entityToMap(fieLdsModel);
                            map.put("label", fieLdsModel.getConfig().getLabel());
                            allUploadTemplates.add(map);
                        }
                    }
                }
            }
            for (FormAllModel fam : table) {
                FormColumnTableModel child = fam.getChildList();
                FormColumnTableModel childRes = new FormColumnTableModel();
                BeanUtil.copyProperties(child, childRes);
                String childClassName = DataControlUtils.captureName(tableRenames.get(childRes.getTableName()));
                //子表别名
                childRes.setAliasClassName(childClassName);
                childRes.setAliasUpName(DataControlUtils.captureName(childClassName));
                childRes.setAliasLowName(DataControlUtils.initialLowercase(childClassName));
                String tableModel = child.getTableModel();
                List<FormColumnModel> childList1 = child.getChildList();
                //获取该子表的所有需要导入字段
                List<String> keys = childMap.get(tableModel) != null ? childMap.get(tableModel) : Collections.EMPTY_LIST;
                List<FormColumnModel> collect = childList1.stream().filter(t -> keys.contains(t.getFieLdsModel().getVModel())).collect(Collectors.toList());
                childRes.setChildList(collect);
                Map<String, Object> childFilesMap = JsonUtil.entityToMap(childRes);
                childFilesMap.put("vModel", childRes.getTableModel());
                //添加整个子表
                allUploadTemplates.add(childFilesMap);
            }
        }
        //导入重复字段，需要标记（子表也以label判断重复）
        Set<String> set = new HashSet<>();
        List<String> nameAgain = new ArrayList<>();
        for (Map<String, Object> f : allUploadTemplates) {
            if (!set.add(String.valueOf(f.get("label")))) {
                nameAgain.add(String.valueOf(f.get("vModel")));
            }
        }

        //树形列表参数
        if (Objects.equals(columnDataModel.getType(), 5)) {
            columnDataModel.setHasPage(false);
        }
        String parentField = StringUtil.isNotEmpty(columnDataModel.getParentField()) ? columnDataModel.getParentField() : "";
        if (StringUtil.isNotEmpty(parentField)) {
            parentField = parentField.substring(0, 1).toUpperCase() + parentField.substring(1);
        }
        String subField = StringUtil.isNotEmpty(columnDataModel.getSubField()) ? columnDataModel.getSubField() : "";
        if (StringUtil.isNotEmpty(subField)) {
            subField = subField.substring(0, 1).toUpperCase() + subField.substring(1);
        }
        //导出字段属性转换
        List<ColumnListField> listOptions = GenerateCommon.getExpotColumn(columnList);
        //++++++++++++++++++++++++++++主副子通用参数++++++++++++++++++++++++++/
        listMap.put("hasPage", columnDataModel.getHasPage());
        listMap.put("defaultSidx", columnDataModel.getDefaultSidx());
        listMap.put("sort", columnDataModel.getSort());
        listMap.put("authority", authority);
        //app pc 数据权限是否开启
        listMap.put("pcDataPermisson", columnDataModel.getUseDataPermission());
        listMap.put("appDataPermisson", appColumnDataModel.getUseDataPermission());
        listMap.put("groupModels", groupModels);
        listMap.put("groupAppModels", groupAppModels);
        listMap.put("keywordModels", keywordModels);
        listMap.put("keywordAppModels", keywordAppModels);
        listMap.put("childTableNameList", childTableNameList);
        listMap.put("allTableNameList", allTableNameList);
        //是否开启高级查询
        listMap.put("superQuery", columnDataModel.getHasSuperQuery());
        listMap.put("ruleQuery", true);

        //++++++++++++++++++++++++++++仅主表参数++++++++++++++++++++++++++//
        if (generateParamModel.isMainTable()) {
            //列表全属性
            listMap.put("columnDataStr", JSONObject.toJSONString(GenerateCommon.objRemoveJson(entity.getColumnData())));
            listMap.put("appColumnDataStr", JSONObject.toJSONString(GenerateCommon.objRemoveJson(entity.getAppColumnData())));
            listMap.put("columnData", JsonUtil.stringToMap(entity.getColumnData()));
            //列表-pc-app-并集
            listMap.put("columnList", columnList);
            listMap.put("searchList", searchList);
            listMap.put("columnAppList", columnAppList);
            listMap.put("searchAppList", searchAppList);
            listMap.put("columnListAll", columnListAll);
            listMap.put("searchListAll", searchListAll);
            //子表样式
            listMap.put("childTableStyle", columnDataModel.getChildTableStyle());

            //左侧树
            listMap.put("leftTreeTable", columnDataModel.getType() == 2);
            //分组
            listMap.put("groupTable", columnDataModel.getType() == 3);
            listMap.put("groupField", columnDataModel.getGroupField());
            //行内编辑
            listMap.put("lineEdit", columnDataModel.getType() == 4);
            //树形参数
            listMap.put("treeTable", columnDataModel.getType() == 5);
            //合计
            boolean configurationTotal = columnDataModel.isShowSummary();
            if (columnDataModel.getType() == 3 || columnDataModel.getType() == 5) {
                configurationTotal = false;
            }
            listMap.put("configurationTotal", configurationTotal);
            List<String> summaryList = CollectionUtils.isEmpty(columnDataModel.getSummaryField()) ? Collections.EMPTY_LIST : columnDataModel.getSummaryField();
            listMap.put("fieldsTotal", JsonUtil.getObjectToString(summaryList));
            //按键
            listMap.put("btnsList", btnDataList);
            listMap.put("columnBtnsList", columnBtnDataList);
            listMap.put("btnPcList", btnPcList);
            listMap.put("columnBtnPcList", columnBtnPcList);

            listMap.put("hasDownloadBtn", hasDownloadBtn);
            listMap.put("hasUploadBtn", hasUploadBtn);
            listMap.put("hasPrintBtn", hasPrintBtn);
            listMap.put("hasRemoveBtn", hasRemoveBtn);

            listMap.put("parentField", parentField);
            listMap.put("subField", subField);
            //导入的字段
            listMap.put("importFields", allUploadTemplates);
            listMap.put("selectKey", JSONObject.toJSONString( JsonUtil.getListToJsonArray(uploaderTemplateModel.getSelectKey()).toJSONString()));
            //是否有子表-用于判断导入excel表头是否有两行
            listMap.put("importHasChildren", importHasChildren);
            listMap.put("importType", importType);
            //导入字段名称是否重复
            listMap.put("nameAgain", nameAgain);
            listMap.put("listOptions", listOptions);

            // 是否存在列表子表数据
            listMap.put("hasSub", mastTable.size() > 0 ? true : false);

            //复杂表头
            List<String> complexFieldList=new ArrayList<>();
            List<Map<String, Object>> complexHeaderList=new ArrayList<>();
            if(!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)){
                for(HeaderModel headerModel:columnDataModel.getComplexHeaderList()){
                    complexFieldList.addAll(headerModel.getChildColumns());
                    Map<String, Object> map = JsonUtil.entityToMap(headerModel);
                    //复杂表头添加导入字段信息
                    List<Map<String, Object>> uploadFieldList=new ArrayList<>();
                    for(Map<String, Object> uploadmap:allUploadTemplates){
                        if(headerModel.getChildColumns().contains(uploadmap.get("vModel"))){
                            Map<String, Object> objectObjectHashMap = new HashMap<>();
                            objectObjectHashMap.put("vModel",uploadmap.get("vModel"));
                            objectObjectHashMap.put("label",uploadmap.get("label"));
                            uploadFieldList.add(objectObjectHashMap);
                        }
                    }
                    map.put("uploadFieldList",uploadFieldList);
                    complexHeaderList.add(map);
                }
            }
            listMap.put("complexHeaderList", JsonUtil.getListToJsonArray(complexHeaderList));
            listMap.put("complexFieldList", JsonUtil.getListToJsonArray(complexFieldList));
        }
    }

    /**
     * 添加左侧树查询字段
     *
     * @param mainTableModel
     * @param columnDataModel
     * @param searchListAll
     */
    private void addTreeSearchField(TableModel mainTableModel, ColumnDataModel columnDataModel, List<SearchTypeModel> searchListAll) {
        List<String> cSaArr = searchListAll.stream().map(SearchTypeModel::getId).collect(Collectors.toList());
        //左侧树-若查询列表内没有需要添加到查询字段内
        if (Objects.equals(columnDataModel.getType(), 2)) {
            String treeRelationField = columnDataModel.getTreeRelation();
            if (!cSaArr.contains(treeRelationField)) {
                SearchTypeModel searchTypeModel = new SearchTypeModel();
                searchTypeModel.setId(treeRelationField);
                searchTypeModel.setSearchType(2);
                if (columnDataModel.getTreeDataSource().equals("organize")) {
                    searchTypeModel.setJnpfKey(JnpfKeyConsts.COMSELECT);
                    searchTypeModel.setSearchType(1);
                }
                searchTypeModel.setLabel("tree");
                searchTypeModel.setTableName(mainTableModel.getTable());
                searchListAll.add(searchTypeModel);
                cSaArr.add(searchTypeModel.getId());
            }
        }
    }

    /**
     * 查询字段配置调整
     *
     * @param tableModelList
     * @param mainTable
     * @param tableRenames
     * @param childKeyTableNameMap
     * @param mainTableModel
     * @param searchListAll
     * @return
     */
    private List<ListSearchGroupModel> getListSearchGroupModels(List<TableModel> tableModelList, TableModel mainTable, Map<String, String> tableRenames, Map<String, String> childKeyTableNameMap, TableModel mainTableModel, List<SearchTypeModel> searchListAll) {
        List<ListSearchGroupModel> groupModels = new ArrayList<>();
        List<String> rangeToLike = new ArrayList() {{
            add(JnpfKeyConsts.COM_INPUT);
            add(JnpfKeyConsts.TEXTAREA);
        }};
        //查询全字段-转换--pagenation-字段不用替换了
        searchListAll.stream().forEach(t -> {
            t.setId(t.getId().replace("-", "_"));
            //单行和多行范围查询转模糊
            if (Objects.equals(t.getSearchType(), 3) && rangeToLike.contains(t.getConfig().getJnpfKey())) {
                t.setSearchType(2);
            }
        });
        if (searchListAll.size() > 0) {
            //主表
            for (TableFields tableFields : mainTableModel.getFields()) {
                searchListAll.stream().forEach(searchTypeModel -> {
                    if (searchTypeModel.getId().equals(tableFields.getField())) {
                        searchTypeModel.setDataType(tableFields.getDataType());
                    }
                });
            }
            //鉴别列表子表正则
            String reg = "^[jnpf_]\\S*_jnpf\\S*";
            searchListAll.stream().filter(s -> s.getId().matches(reg)).forEach(cl -> {
                        String s = cl.getId();
                        String s1 = s.substring(s.lastIndexOf("jnpf_")).replace("jnpf_", "");
                        String s2 = s.substring(s.indexOf("_") + 1, s.lastIndexOf("_jnpf"));
                        cl.setAfterVModel(s1);
                        cl.setTableName(s2);
                        TableModel tableModel = tableModelList.stream().filter(t -> t.getTable().equalsIgnoreCase(s2)).findFirst().orElse(null);
                        cl.setDataType(tableModel.getFields().stream().filter(t -> t.getField().equalsIgnoreCase(s1)).findFirst().orElse(null).getDataType());
                    }
            );
            //副表--普通查询放回主表
            Map<String, List<SearchTypeModel>> collect = searchListAll.stream().filter(s -> s.getId().matches(reg)).collect(Collectors.groupingBy(t -> t.getTableName()));
            groupModels = collect.entrySet().stream().map(c -> {
                        ListSearchGroupModel groupModel = new ListSearchGroupModel();
                        groupModel.setModelName(tableRenames.get(c.getKey()));
                        groupModel.setTableName(c.getKey());
                        TableModel tableModel = tableModelList.stream().filter(t -> t.getTable().equalsIgnoreCase(c.getKey())).findFirst().orElse(null);
                        groupModel.setForeignKey(tableModel.getTableField());
                        groupModel.setMainKey(tableModel.getRelationField());
                        groupModel.setSearchTypeModelList(c.getValue());
                        return groupModel;
                    }
            ).collect(Collectors.toList());

            //子表--普通查询放回主表
            Map<String, List<SearchTypeModel>> collect1 = searchListAll.stream().filter(s -> s.getId().toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX))
                    .collect(Collectors.groupingBy(t -> t.getId().substring(0, t.getId().lastIndexOf("_"))));
            List<ListSearchGroupModel> collect2 = collect1.entrySet().stream().map(c -> {
                        ListSearchGroupModel groupModel = new ListSearchGroupModel();
                        String tableName = childKeyTableNameMap.get(c.getKey());
                        groupModel.setModelName(tableRenames.get(tableName));
                        groupModel.setTableName(tableName);
                        TableModel tableModel = tableModelList.stream().filter(t -> t.getTable().equalsIgnoreCase(tableName)).findFirst().orElse(null);
                        groupModel.setForeignKey(tableModel.getTableField());
                        groupModel.setMainKey(tableModel.getRelationField());
                        List<SearchTypeModel> value = c.getValue();
                        value.stream().forEach(v -> {
                            String vmodelall = v.getId();
                            String substring = vmodelall.substring(vmodelall.lastIndexOf("_") + 1);
                            v.setAfterVModel(substring);
                        });
                        groupModel.setSearchTypeModelList(value);
                        return groupModel;
                    }
            ).collect(Collectors.toList());
            groupModels.addAll(collect2);

            ListSearchGroupModel groupModel = new ListSearchGroupModel();
            groupModel.setSearchTypeModelList(searchListAll.stream().filter(s -> !s.getId().matches(reg)
                    && !s.getId().toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList()));
            groupModel.setTableName(mainTable.getTable());
            groupModel.setModelName(tableRenames.get(mainTable.getTable()));
            groupModels.add(groupModel);
        }
        return groupModels;
    }

    /**
     * 生成java代码
     *
     * @param generateParamModel
     * @param codeUtil           模板对象
     */
    private void setCode(GenerateParamModel generateParamModel, GenerateInterface codeUtil) throws Exception {
        //获取传递参数
        Map<String, Object> columndata = this.getcolumndata(generateParamModel);
        String fileName = generateParamModel.getFileName();
        String templatesPath = generateParamModel.getTemplatesPath();
        DownloadCodeForm downloadCodeForm = generateParamModel.getDownloadCodeForm();
        DbLinkEntity linkEntity = generateParamModel.getLinkEntity();
        String className = generateParamModel.getClassName();
        String table = generateParamModel.getTable();
        String path = generateParamModel.getPath();
        Template7Model template7Model = generateParamModel.getTemplate7Model();
        CustomGenerator mpg = new CustomGenerator(columndata);

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setFileOverride(true);
        // 不需要ActiveRecord特性的请改为false
        gc.setActiveRecord(false);
        // XML 二级缓存
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        gc.setAuthor(template7Model.getCreateUser());
        gc.setOpen(false);

        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        if (generateParamModel.isMainTable()) {
            gc.setControllerName(template7Model.getClassName() + GenFileNameSuffix.CONTROLLER);
        }
        gc.setEntityName(template7Model.getClassName() + GenFileNameSuffix.ENTITY);
        gc.setMapperName(template7Model.getClassName() + GenFileNameSuffix.MAPPER);
        gc.setServiceName(template7Model.getClassName() + GenFileNameSuffix.SERVICE);
        gc.setServiceImplName(template7Model.getClassName() + GenFileNameSuffix.SERVICEIMPL);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        mpg.setDataSource(SourceUtil.dbConfig(TenantDataSourceUtil.getTenantSchema(), linkEntity));

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        // 表名生成策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // 需要生成的表
        strategy.setInclude(table);
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent(downloadCodeForm.getModulePackageName());
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        //后端代码文件生成
        cfg.setFileOutConfigList(GenerateCommon.getJavaFiles(generateParamModel));
        mpg.setTemplate(new TemplateConfig().setXml(null).setMapper(null).setController(null).setEntity(null).setService(null).setServiceImpl(null));
        mpg.setCfg(cfg);
        // 执行生成
        mpg.execute(path);
    }

    /**
     * 生成前端代码
     *
     * @param generateParamModel
     * @param codeUtil
     * @throws Exception
     */
    private void generateHtml(GenerateParamModel generateParamModel, GenerateInterface codeUtil) throws Exception {
        String fileName = generateParamModel.getFileName();
        String templatesPath = generateParamModel.getTemplatesPath();
        DownloadCodeForm downloadCodeForm = generateParamModel.getDownloadCodeForm();
        DbLinkEntity linkEntity = generateParamModel.getLinkEntity();
        String className = generateParamModel.getClassName();
        String path = generateParamModel.getPath();
        VisualdevEntity entity = generateParamModel.getEntity();
        ConfigValueUtil configValueUtil = generateParamModel.getConfigValueUtil();

        Template7Model template7Model = GenerateCommon.getTemplate7Model(className);
        template7Model.setTableName(downloadCodeForm.getClassName());
        template7Model.setDescription(downloadCodeForm.getDescription());
        generateParamModel.setTemplate7Model(template7Model);
        //自定义包名
        String modulePackageName = downloadCodeForm.getModulePackageName();
        Map<String, Object> map = new HashMap<>(16);
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);

        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        recursionForm.setList(list);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);

        //form的属性
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());

        //tableList
        List<TableModel> tablesList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
        //取对应表的别名
        Map<String, String> tableRenames = FunctionFormPublicUtil.tableNameRename(downloadCodeForm, tablesList);
        //子表（tableField,tableName）->tablefield1->realname
        Map<String, String> childKeyTableNameMap = new HashMap<>(8);
        table.stream().forEach(t -> childKeyTableNameMap.put(t.getChildList().getTableModel(), t.getChildList().getTableName()));
        Map<String, String> childKeyRenameMap = new HashMap<>(8);
        for (String key : childKeyTableNameMap.keySet()) {
            childKeyRenameMap.put(key, tableRenames.get(childKeyTableNameMap.get(key)));
        }
        //主表
        TableModel mainTableModel = tablesList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);

        //主表赋值
        for (int i = 0; i < mast.size(); i++) {
            FieLdsModel fieLdsModel = mast.get(i).getFormColumnModel().getFieLdsModel();
            ConfigModel configModel = fieLdsModel.getConfig();
            if (configModel.getDefaultValue() instanceof String) {
                configModel.setValueType("String");
            }
            if (configModel.getDefaultValue() == null) {
                configModel.setValueType("undefined");
            }
            fieLdsModel.setConfig(configModel);
        }
        //副表列表字段赋值
        List<ColumnListDataModel> formChildList = new ArrayList<>();
        Map<String, List<FormAllModel>> groupColumnDataMap = mastTable.stream().collect(Collectors.groupingBy(m -> m.getFormMastTableModel().getTable()));
        //副表model
        for (String key : groupColumnDataMap.keySet()) {
            Map<String, Object> objectMap = new HashMap<>();
            String childClassName = DataControlUtils.captureName(tableRenames.get(key));
            ColumnListDataModel columnListDataModel = new ColumnListDataModel();
            columnListDataModel.setModelName(childClassName);
            columnListDataModel.setModelUpName(DataControlUtils.captureName(childClassName));
            columnListDataModel.setModelLowName(DataControlUtils.initialLowercase(childClassName));
            List<FormAllModel> allModels = groupColumnDataMap.get(key);
            allModels.stream().forEach(m -> {
                String vModel = m.getFormMastTableModel().getField();
                m.getFormMastTableModel().getMastTable().getFieLdsModel().setVModel(vModel);
            });
            List<String> fields = allModels.stream().map(m ->
                    m.getFormMastTableModel().getField()).collect(Collectors.toList());
            columnListDataModel.setFieldList(fields);
            columnListDataModel.setFieLdsModelList(allModels.stream().map(al -> al.getFormMastTableModel()).collect(Collectors.toList()));
            columnListDataModel.setTableName(key);
            formChildList.add(columnListDataModel);
            List<FormColumnModel> children = allModels.stream().map(allModel -> allModel.getFormMastTableModel().getMastTable()).collect(Collectors.toList());
            FormColumnTableModel formColumnTableModel = new FormColumnTableModel();
            formColumnTableModel.setChildList(children);
            objectMap.put("children", formColumnTableModel);
            objectMap.put("genInfo", generateParamModel.getTemplate7Model());
            objectMap.put("package", modulePackageName);
            objectMap.put("module", downloadCodeForm.getModule());
            objectMap.put("className", childClassName);
            childrenTemplates(FormCommonUtil.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName,
                    objectMap, downloadCodeForm, false, codeUtil);
        }
        //子表赋值
        List<Map<String, Object>> child = new ArrayList<>();
        //子表model
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            Map<String, Object> objectMap = JsonUtil.entityToMap(childList);
            List<FormColumnModel> tableList = childList.getChildList();

            TableFields thisKeyFields = null;
            TableModel thisTable = tablesList.stream().filter(t -> t.getTable().equals(childList.getTableName())).findFirst().orElse(null);
            if(thisTable!=null){
                thisKeyFields = thisTable.getFields().stream().filter(t -> Objects.equals(t.getPrimaryKey(), 1)).findFirst().orElse(null);
            }
            String childClassName = DataControlUtils.captureName(tableRenames.get(childList.getTableName()));
            //导入字段属性设置
            if (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType()) && !Objects.equals(entity.getType(), 3)) {
                ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class);
                if (columnDataModel.getUploaderTemplateJson() != null) {
                    UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
                    List<String> selectKey = uploaderTemplateModel.getSelectKey() != null ? uploaderTemplateModel.getSelectKey() : Collections.EMPTY_LIST;
                    tableList.stream().forEach(item -> {
                        String childFieldKey = item.getFieLdsModel().getConfig().getParentVModel() + "-" + item.getFieLdsModel().getVModel();
                        if (selectKey.contains(childFieldKey)) {
                            item.getFieLdsModel().setNeedImport(true);
                        }
                    });
                }
            }

            //子表别名
            childList.setAliasClassName(childClassName);
            childList.setAliasUpName(DataControlUtils.captureName(childClassName));
            childList.setAliasLowName(DataControlUtils.initialLowercase(childClassName));
            //去除复杂表头里面的字段（无用信息）
            List<HeaderModel> complexHeaderList = childList.getComplexHeaderList();
            for (HeaderModel headerModel : complexHeaderList) {
                headerModel.setChildList(null);
            }
            objectMap.put("children", childList);
            objectMap.put("genInfo", generateParamModel.getTemplate7Model());
            objectMap.put("package", modulePackageName);
            objectMap.put("module", downloadCodeForm.getModule());
            objectMap.put("className", childClassName);
            objectMap.put("thisKeyFields", thisKeyFields);//子表主键字段
            //生成xxxmodel 和 xxxlist
            childrenTemplates(FormCommonUtil.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName,
                    objectMap, downloadCodeForm, true, codeUtil);
            for (FormColumnModel columnModel : tableList) {
                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                List<TemplateJsonModel> templateJson = fieLdsModel.getConfig().getTemplateJson();
                String json = templateJson.size() > 0 ? JsonUtil.getObjectToString(templateJson) : fieLdsModel.getTemplateJson();
                fieLdsModel.setTemplateJson(json);
            }
            childList.setChildList(tableList);
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            child.add(childs);
        }

        //微服务标识
        map.put("isCloud", GenerateCommon.IS_CLOUD);
        //是列表，是流程判断
        if (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType())) {
            map.put("isList", true);
        }
        if (Objects.equals(entity.getEnableFlow(), 1)) {
            map.put("isFlow", true);
        }
        //界面
        map.put("genInfo", generateParamModel.getTemplate7Model());
        map.put("modelName", downloadCodeForm.getClassName());
        map.put("package", modulePackageName);
        map.put("isMain", true);
        map.put("moduleId", entity.getId());

        map.put("module", downloadCodeForm.getModule());
        map.put("className", DataControlUtils.captureName(downloadCodeForm.getClassName()));
        map.put("tableRenames", JSONObject.toJSONString(JsonUtil.getObjectToString(tableRenames)));
        map.put("childKeyTableNameMap", JSONObject.toJSONString(JsonUtil.getObjectToString(childKeyTableNameMap)));
        map.put("templateJsonAll", JSONObject.toJSONString(GenerateCommon.getInterTemplateJson(formAllModel, childKeyRenameMap)));
        //乐观锁
        map.put("version", formData.getConcurrencyLock());
        map.put("formRef", formData.getFormRef());
        map.put("formModel", formData.getFormModel());
        map.put("size", formData.getSize());
        map.put("labelPosition", formData.getLabelPosition());
        map.put("generalWidth", formData.getGeneralWidth());
        map.put("drawerWidth", formData.getDrawerWidth());
        map.put("fullScreenWidth", formData.getFullScreenWidth());
        map.put("formStyle", formData.getFormStyle());
        map.put("labelWidth", formData.getLabelWidth());
        map.put("labelSuffix", formData.getLabelSuffix());
        map.put("formRules", formData.getFormRules());
        map.put("gutter", formData.getGutter());
        map.put("disabled", formData.getDisabled());
        map.put("span", formData.getSpan());
        map.put("formBtns", formData.getFormBtns());
        map.put("idGlobal", formData.getIdGlobal());
        map.put("popupType", formData.getPopupType());
        //表单按钮
        map.put("HasCancelBtn", formData.getHasCancelBtn());
        map.put("HasConfirmBtn", formData.getHasConfirmBtn());
        map.put("HasPrintBtn", formData.getHasPrintBtn());
        map.put("CancelButton", formData.getCancelButtonText());
        map.put("ConfirmButton", formData.getConfirmButtonText());
        map.put("PrintButton", formData.getPrintButtonText());
        map.put("PrintId", JsonUtil.getObjectToString(formData.getPrintId()));
        map.put("form", formAllModel);

        map.put("groupColumnDataMap", groupColumnDataMap);
        map.put("formModelName", entity.getFullName());
        map.put("dbLinkId", entity.getDbLinkId());
        //共用
        map.put("children", child);
        map.put("fields", mast);
        map.put("mastTable", mastTable);
        map.put("columnChildren", formChildList);
        map.put("pKeyName", generateParamModel.getPKeyName());
        String modelPathName = downloadCodeForm.getClassName().toLowerCase();
        map.put("modelPathName", modelPathName);
        map.put("formModelName", entity.getFullName());
        map.put("formDataStr", JSONObject.toJSONString(GenerateCommon.objRemoveJson(entity.getFormData())));
        map.put("tableListStr", JSONObject.toJSONString(entity.getVisualTables()));
        map.put("ableAll", JsonUtil.getListToJsonArray(formAllModel));
        map.put("hasConfirmAndAddBtn", formData.getHasConfirmAndAddBtn());

        boolean hasUploadBtn = false;
        boolean hasSuperQuery = false;
        int columnTtype = 0;
        //webType=2 列表生成 高级查询json，列表json，查询json  enableflow启用流程
        if (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType()) && !Objects.equals(entity.getType(), 3)) {
            //添加行参数
            generateParamModel.setMainTable(true);
            getListColumndata(generateParamModel, map);
            Map<String, Object> columnDataModel = JsonUtil.stringToMap(entity.getColumnData());
            //按钮
            List<BtnData> btnDataList = JsonUtil.getJsonToList(columnDataModel.get("btnsList"), BtnData.class);
            hasUploadBtn = btnDataList.stream().anyMatch(btn -> btn.getValue().equals("upload"));
            //是否开启高级查询
            hasSuperQuery = (boolean) columnDataModel.get("hasSuperQuery");
            columnTtype = (int) columnDataModel.get("type");
            //最外层zip包路径名称
            String zipName = FormCommonUtil.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName;
            //生成文件夹
            String htmlTSPath = XSSEscape.escapePath(zipName + File.separator + "html" + File.separator + "web" + File.separator + modelPathName + File.separator + "helper");
            File htmlJSfile = new File(htmlTSPath);
            if (!htmlJSfile.exists() && !"form".equals(downloadCodeForm.getModule())) {
                htmlJSfile.mkdirs();
            }
            if (hasSuperQuery) {
                String superSqJsPath = htmlTSPath + File.separator + "superQueryJson.ts";
                String data = JSONObject.toJSONString(columnDataModel.get("columnOptions"), SerializerFeature.WriteMapNullValue);
                SuperQueryUtil.CreateJsFile(data, superSqJsPath, "superQueryJson");
            }

            String colData = JSONObject.toJSONString(columnDataModel.get("columnList"), SerializerFeature.WriteMapNullValue);
            String colListJsPath = htmlTSPath + File.separator + "columnList.ts";
            SuperQueryUtil.CreateJsFile(colData, colListJsPath, "columnList");

            String searchData = JSONObject.toJSONString(columnDataModel.get("searchList"), SerializerFeature.WriteMapNullValue);
            String searchListJsPath = htmlTSPath + File.separator + "searchList.ts";
            SuperQueryUtil.CreateJsFile(searchData, searchListJsPath, "searchList");

            //生成复杂表头对象
            createComplexHeaderExcelVo(zipName, generateParamModel, entity, downloadCodeForm, map);
        }

        /**
         * 生成前端及后端model文件
         */
        htmlTemplates(FormCommonUtil.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName,
                map, templatesPath, columnTtype, hasUploadBtn, downloadCodeForm, codeUtil);

        /**
         * 生成表单设计json文件
         */
        if (Objects.equals(entity.getEnableFlow(), 1) || Objects.equals(entity.getType(), 3)) {
            FlowFormEntity flowFormEntity = FunctionFormPublicUtil.exportFlowFormJson(entity, downloadCodeForm);
            SuperQueryUtil.CreateFlowFormJsonFile(JsonUtil.getObjectToString(flowFormEntity),
                    FormCommonUtil.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName);
        }
    }

    /**
     * 渲染html模板
     *
     * @param path         路径
     * @param object       模板数据
     * @param templatePath 模板路径
     */
    private void htmlTemplates(String path, Map<String, Object> object, String templatePath, int type, boolean hasImport,
                               DownloadCodeForm downloadCodeForm, GenerateInterface codeUtil) throws Exception {
        //获取模板列表
        List<String> templates = codeUtil.getTemplates(templatePath, type, hasImport);
        //界面模板
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String template : templates) {
            String className = object.get("className").toString();
            String fileNames = GenerateCommon.getFileName(path, template, className, downloadCodeForm);
            GenerateCommon.velocityWriterFile(context, template, fileNames);
        }
    }

    /**
     * 副子表model
     *
     * @param path   路径
     * @param object 模板数据
     */
    private void childrenTemplates(String path, Map<String, Object> object, DownloadCodeForm downloadCodeForm,
                                   Boolean isChild, GenerateInterface codeUtil) {
        //获取模板列表
        List<String> templates = codeUtil.getChildTemps(isChild);
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String templateName : templates) {
            String className = object.get("className").toString();
            String fileNames = GenerateCommon.getFileName(path, templateName, className, downloadCodeForm);
            GenerateCommon.velocityWriterFile(context, templateName, fileNames);
        }
    }

    /**
     * 复杂表头 对象生成。
     * @param path
     * @param generateParamModel
     * @param entity
     * @param downloadCodeForm
     * @param objectAll
     */
    private void createComplexHeaderExcelVo(String path, GenerateParamModel generateParamModel, VisualdevEntity entity,
                                            DownloadCodeForm downloadCodeForm, Map<String, Object> objectAll) {
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class);
        JsonUtil.getListToJsonArray(columnDataModel.getComplexHeaderList());
        List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
        String templateName = File.separator + "java" + File.separator + "ExcelVO.java.vm" ;
        VelocityContext context = new VelocityContext();
        Map<String, Object> object = new HashMap<>();
        object.put("genInfo", generateParamModel.getTemplate7Model());
        object.put("package", generateParamModel.getDownloadCodeForm().getModulePackageName());
        object.put("module", downloadCodeForm.getModule());
        object.put("isMain", true);
        object.put("isComplexVo", true);
        object.put("importFields", objectAll.get("importFields"));
        for (HeaderModel item : complexHeaderList) {
            if(item.getChildColumns().size()>0){
                String className = "Complex" + item.getId();
                object.put("complexList", JsonUtil.getListToJsonArray(item.getChildColumns()));
                object.put("className", className);
                context.put("context", object);
                String fileNames = GenerateCommon.getFileName(path, templateName, className, downloadCodeForm);
                GenerateCommon.velocityWriterFile(context, templateName, fileNames);
            }
        }
    }

    /**
     * app代码生成
     *
     * @param generateParamModel 参数
     * @param codeUtil           模板
     */
    private void generateApp(GenerateParamModel generateParamModel, GenerateInterface codeUtil) throws Exception {
        VisualdevEntity entity = generateParamModel.getEntity();
        ConfigValueUtil configValueUtil = generateParamModel.getConfigValueUtil();
        DownloadCodeForm downloadCodeForm = generateParamModel.getDownloadCodeForm();
        UserInfo userInfo = generateParamModel.getUserInfo();
        String templatesPath = generateParamModel.getTemplatesPath();
        String fileName = generateParamModel.getFileName();
        DataSourceUtil dataSourceUtil = generateParamModel.getDataSourceUtil();
        DbLinkEntity linkEntity = generateParamModel.getLinkEntity();
        String column = StringUtil.isNotEmpty(entity.getColumnData()) ? entity.getColumnData() : "{}";
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(column, ColumnDataModel.class);
        boolean groupTable = "3".equals(String.valueOf(columnDataModel.getType()));
        FormDataModel model = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        model.setModule(downloadCodeForm.getModule());
        model.setClassName(downloadCodeForm.getClassName());
        model.setAreasName(downloadCodeForm.getModule());
        model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
        List<FieLdsModel> filterFeildList = JsonUtil.getJsonToList(model.getFields(), FieLdsModel.class);
        model.setFields(JSON.toJSONString(filterFeildList));
        //app信息调整
        VisualdevEntity entityCopy = BeanUtil.copyProperties(entity, VisualdevEntity.class);
        entityCopy.setColumnData(entity.getAppColumnData());
        entityCopy.setEnableFlow(Objects.equals(entity.getType(),3)?1:entity.getEnableFlow());
        AppGenModel appGenModel = new AppGenModel();
        appGenModel.setEntity(entityCopy);
        appGenModel.setPKeyName(generateParamModel.getPKeyName());
        appGenModel.setServiceDirectory(FormCommonUtil.getLocalBasePath() + configValueUtil.getServiceDirectoryPath());
        appGenModel.setDownloadCodeForm(downloadCodeForm);
        appGenModel.setUserInfo(userInfo);
        appGenModel.setTemplatePath(templatesPath);
        appGenModel.setFileName(fileName);
        appGenModel.setLinkEntity(linkEntity);
        appGenModel.setDataSourceUtil(dataSourceUtil);
        appGenModel.setGroupTable(groupTable);
        appGenModel.setType(String.valueOf(columnDataModel.getType()));
        appGenModel.setModel(model);
        AppGenUtil appGenUtil = new AppGenUtil();
        appGenUtil.htmlTemplates(appGenModel);
    }
}
