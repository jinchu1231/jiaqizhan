package jnpf.base.util.form;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
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
import jnpf.base.util.common.DataControlUtils;
import jnpf.base.util.common.FormCommonUtil;
import jnpf.base.util.common.FunctionFormPublicUtil;
import jnpf.base.util.common.SuperQueryUtil;
import jnpf.base.util.custom.CustomGenerator;
import jnpf.base.util.fuctionFormVue3.common.GenerateCommon;
import jnpf.base.util.fuctionFormVue3.common.GenerateParamModel;
import jnpf.constant.MsgCode;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.entity.FlowFormEntity;
import jnpf.generater.model.FormDesign.ColumnChildListModel;
import jnpf.generater.model.FormDesign.ColumnListDataModel;
import jnpf.generater.model.FormDesign.ColumnListModel;
import jnpf.generater.model.FormDesign.ListSearchGroupModel;
import jnpf.generater.model.FormDesign.SearchTypeModel;
import jnpf.generater.model.GenBaseInfo;
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
import jnpf.model.visualJson.analysis.FormMastTableModel;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.model.visualJson.config.HeaderModel;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.XSSEscape;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FormGenUtil {


    //+-----------------------------界面2021.8.13------------------------------------------------------------

    public void htmlTemplates(FormGenModel genModel) throws Exception {
        //对象
        htmlTemplatesList(genModel);


        VisualdevEntity entity = genModel.getEntity();
        DbLinkEntity linkEntity = genModel.getLinkEntity();
        DownloadCodeForm downloadCodeForm = genModel.getDownloadCodeForm();
        String className = downloadCodeForm.getClassName().substring(0, 1).toUpperCase() + downloadCodeForm.getClassName().substring(1);
        Template7Model templateModel = this.templateModel(genModel, className);

        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        recursionForm.setList(JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class));
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);


        //执行代码生成器
        GenerateParamModel generateParamModel = new GenerateParamModel().builder()
                .dataSourceUtil(genModel.getDataSourceUtil())
                .path(FormCommonUtil.getLocalBasePath() + genModel.getConfigValueUtil())
                .fileName(genModel.getFileName())
                .templatesPath(genModel.getTemplatePath())
                .downloadCodeForm(downloadCodeForm)
                .entity(genModel.getEntity())
                .userInfo(genModel.getUserInfo())
                .configValueUtil(genModel.getConfigValueUtil())
                .linkEntity(genModel.getLinkEntity())
                .pKeyName(genModel.getPKeyName())
                .className(className)
                .isMainTable(true)
                .table(genModel.getTable())
                .build();
        Map<String, Object> columndata = getcolumndata(generateParamModel);

        //form的属性
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //子表导入
        List<TableModel> tablesList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
        Map<String, String> tableRenames = tableName(tablesList, generateParamModel.getDownloadCodeForm());
        List<ColumnListDataModel> formChildList = new ArrayList<>();
        Map<String, List<FormAllModel>> groupColumnDataMap = mastTable.stream().collect(Collectors.groupingBy(m -> m.getFormMastTableModel().getTable()));

        Map<String, String> childKeyTableNameMap = new HashMap<>(8);
        table.stream().forEach(t -> childKeyTableNameMap.put(t.getChildList().getTableModel(), t.getChildList().getTableName()));

        columndata.put("package", generateParamModel.getDownloadCodeForm().getModulePackageName());
        columndata.put("modelPathName", generateParamModel.getDownloadCodeForm().getModulePackageName());
        columndata.put("className", className);
        columndata.put("form", formAllModel);
        columndata.put("isMain", true);
        columndata.put("dbLinkId", linkEntity != null ? linkEntity.getId() : "0");
        columndata.put("tableRenames", JSONObject.toJSONString(JsonUtil.getObjectToString(tableRenames)));
        columndata.put("childKeyTableNameMap", JSONObject.toJSONString(JsonUtil.getObjectToString(childKeyTableNameMap)));


        List<String> getTemplate = this.getTemplate(genModel, false, false);
        String path = templateModel.getServiceDirectory() + genModel.getFileName();
        FormDataModel model = genModel.getModel();
        String modelPathName = model.getClassName().substring(0, 1).toLowerCase() + model.getClassName().substring(1);
        this.htmlTemplates(columndata, getTemplate, path, templateModel.getClassName(), modelPathName, downloadCodeForm);

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
            //表字段给的范围-转换json
            children.forEach(item -> {
                item.setFieLdsModel(DataControlUtils.setAbleIDs(item.getFieLdsModel()));
            });
            FormColumnTableModel formColumnTableModel = new FormColumnTableModel();
            formColumnTableModel.setChildList(children);
            objectMap.put("children", formColumnTableModel);
            objectMap.put("genInfo", generateParamModel.getTemplate7Model());
            objectMap.put("package", generateParamModel.getDownloadCodeForm().getModulePackageName());
            objectMap.put("module", downloadCodeForm.getModule());
            objectMap.put("className", childClassName);
            List<String> template = this.getTemplate(genModel, false, true);
            this.htmlTemplates(objectMap, template, path, childClassName, modelPathName, downloadCodeForm);
        }
        //子表model
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            Map<String, Object> objectMap = JsonUtil.entityToMap(childList);
            List<FormColumnModel> tableList = childList.getChildList();
            String childClassName = DataControlUtils.captureName(tableRenames.get(childList.getTableName()));

            TableFields thisKeyFields = null;
            TableModel thisTable = tablesList.stream().filter(t -> t.getTable().equals(childList.getTableName())).findFirst().orElse(null);
            if(thisTable!=null){
                thisKeyFields = thisTable.getFields().stream().filter(t -> Objects.equals(t.getPrimaryKey(), 1)).findFirst().orElse(null);
            }
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
            objectMap.put("children", childList);
            objectMap.put("genInfo", generateParamModel.getTemplate7Model());
            objectMap.put("package", generateParamModel.getDownloadCodeForm().getModulePackageName());
            objectMap.put("module", downloadCodeForm.getModule());
            objectMap.put("className", childClassName);
            objectMap.put("thisKeyFields", thisKeyFields);//子表主键字段
            List<String> template = this.getTemplate(genModel, true, false);
            this.htmlTemplates(objectMap, template, path, childClassName, modelPathName, downloadCodeForm);
        }

        //复杂表头
        createComplexHeaderExcelVo(path,generateParamModel,entity,downloadCodeForm,columndata);

        /**
         * 生成表单设计json文件
         */
        if (entity.getEnableFlow() == 1) {
            FlowFormEntity flowFormEntity = FunctionFormPublicUtil.exportFlowFormJson(entity, downloadCodeForm);
            SuperQueryUtil.CreateFlowFormJsonFile(JsonUtil.getObjectToString(flowFormEntity), path);
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
        if(VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType()) ) {
            JsonUtil.getListToJsonArray(columnDataModel.getComplexHeaderList());
            List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
            String templateName = "java" + File.separator + "ExcelVO.java.vm";
            VelocityContext context = new VelocityContext();
            Map<String, Object> object = new HashMap<>();
            object.put("genInfo", generateParamModel.getTemplate7Model());
            object.put("package", generateParamModel.getDownloadCodeForm().getModulePackageName());
            object.put("module", downloadCodeForm.getModule());
            object.put("isMain", true);
            object.put("isComplexVo", true);
            object.put("importFields", objectAll.get("importFields"));
            for (HeaderModel item : complexHeaderList) {
                if (item.getChildColumns().size() > 0) {
                    String className = "Complex" + item.getId();
                    object.put("complexList", JsonUtil.getListToJsonArray(item.getChildColumns()));
                    object.put("className", className);
                    context.put("context", object);
                    String fileNames = GenerateCommon.getFileName(path, templateName, className, downloadCodeForm);
                    GenerateCommon.velocityWriterFile(context, templateName, fileNames);
                }
            }
        }
    }

    /**
     * 获取模板
     *
     * @param genModel
     * @param isChild
     * @return
     */
    private List<String> getTemplate(FormGenModel genModel, boolean isChild, boolean isMastTable) {
        List<String> templates = new ArrayList<>();
        String template = this.tempPath(genModel);
        VisualdevEntity entity = genModel.getEntity();
        ColumnDataModel columnDataModel = StringUtil.isNotEmpty(entity.getColumnData()) ? JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class) : new ColumnDataModel();
        List<BtnData> btnDataList = JsonUtil.getJsonToList(columnDataModel.getBtnsList(), BtnData.class);
        List<String> importList = new ArrayList() {{
            add("upload");
        }};
        boolean hasImport = btnDataList.stream().filter(t -> importList.contains(t.getValue())).count() > 0;
        List<String> templateCode = new ArrayList() {{
            add("TemplateCode2");
            add("TemplateCode3");
        }};
        boolean hasExcel = hasImport && templateCode.contains(template);
        if (isChild || isMastTable) {
            templates.add("java" + File.separator + "Model.java.vm");
            if (hasExcel && isChild) {
                templates.add("java" + File.separator + "ExcelVO.java.vm");
            }
        } else {
            //后端
            templates.add("java" + File.separator + "Form.java.vm");
            templates.add("java" + File.separator + "Constant.java.vm");
            boolean isPage = this.isType(genModel);
            if (isPage) {
                templates.add("java" + File.separator + "Pagination.java.vm");
            }
            if (hasExcel) {
                templates.add("java" + File.separator + "ExcelVO.java.vm");
                templates.add("java" + File.separator + "ExcelErrorVO.java.vm");
            }
        }
        return templates;
    }

    private String tempPath(FormGenModel genModel) {
        String tempPath = genModel.getTemplatePath();
        VisualdevEntity entity = genModel.getEntity();
        if(!Objects.equals(entity.getType(),3)) {
            if (VisualWebTypeEnum.FORM.getType().equals(entity.getWebType())) {
                tempPath = entity.getEnableFlow() == 1 ? "TemplateCode5" : "TemplateCode4";
            } else if (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType())) {
                tempPath = entity.getEnableFlow() == 1 ? "TemplateCode3" : "TemplateCode2";
            }
        }
        return tempPath;
    }

    /**
     * 获取文件名
     *
     * @param path      路径
     * @param template  模板名称
     * @param className 文件名称
     * @return
     */
    private String getFileNames(String path, String template, String className, String modePath, DownloadCodeForm downloadCodeForm) {
        path = XSSEscape.escapePath(path);
        modePath = XSSEscape.escapePath(modePath);
        className = XSSEscape.escapePath(className);
        //是否微服务路径
        if (template.contains("extraForm.vue.vm") || template.contains("Form.vue.vm") || template.contains("index.vue.vm")
                || template.contains("indexEdit.vue.vm") ||  template.contains("Detail.vue.vm")) {
            String htmlPath = XSSEscape.escapePath(path + File.separator + "html" + File.separator + "web" + File.separator + className.toLowerCase());
            File htmlfile = new File(htmlPath);
            if (!htmlfile.exists()) {
                htmlfile.mkdirs();
            }
            if (template.contains("extraForm.vue.vm")) {
                className = "extraForm";
                return htmlPath + File.separator + className + ".vue";
            }
            if (template.contains("Form.vue.vm")) {
                className = "Form";
                return htmlPath + File.separator + className.toLowerCase() + ".vue";
            }
            if (template.contains("index.vue.vm")) {
                className = "index";
                return htmlPath + File.separator + className + ".vue";
            }
            if (template.contains("indexEdit.vue.vm")) {
                className = "index";
                return htmlPath + File.separator + className + ".vue";
            }
            if (template.contains("Detail.vue.vm")) {
                className = "Detail";
                return htmlPath + File.separator + className + ".vue";
            }
        }


        String framePath = "jnpf-" + downloadCodeForm.getModule()
                + File.separator
                + "jnpf-" + downloadCodeForm.getModule() + "-entity"
                + File.separator
                + "src" + File.separator + "main" + File.separator + "java"
                + File.separator
                + downloadCodeForm.getModulePackageName();
        if("form".equals(downloadCodeForm.getModule())){
            String flowWorkPath = "jnpf-workflow" + File.separator + "jnpf-workflow-form" + File.separator + "jnpf-workflow-form-%s" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "jnpf" + File.separator + "form" ;
            framePath = String.format(flowWorkPath, "entity", "entity");
        }
        String modelPath = XSSEscape.escapePath(path + File.separator + "java" + File.separator + framePath + File.separator + "model" + File.separator + modePath.toLowerCase());
        File modelfile = new File(modelPath);
        if (!modelfile.exists()) {
            modelfile.mkdirs();
        }
        if (template.contains("Model.java.vm")) {
            return modelPath + File.separator + className + "Model.java";
        }
        if (template.contains("InfoVO.java.vm")) {
            return modelPath + File.separator + className + "InfoVO.java";
        }
        if (template.contains("Form.java.vm")) {
            return modelPath + File.separator + className + "Form.java";
        }
        if (template.contains("Constant.java.vm")) {
            return modelPath + File.separator + className + "Constant.java";
        }
        if (template.contains("ListVO.java.vm")) {
            return modelPath + File.separator + className + "ListVO.java";
        }
        if (template.contains("Pagination.java.vm")) {
            return modelPath + File.separator + className + "Pagination.java";
        }
        if (template.contains("ExcelVO.java.vm")) {
            return modelPath + File.separator + className + "ExcelVO.java";
        }
        if (template.contains("ExcelErrorVO.java.vm")) {
            return modelPath + File.separator + className + "ExcelErrorVO.java";
        }
        return null;
    }

    /**
     * 渲染html模板
     *
     * @param path   路径
     * @param object 模板数据
     * @param path   模板路径
     */
    private void htmlTemplates(Object object, List<String> templates, String path, String className, String modePath, DownloadCodeForm downloadCodeForm) {
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String template : templates) {
            try {
                @Cleanup StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(template, Constants.UTF_8);
                tpl.merge(context, sw);
                String fileNames = getFileNames(path, template, className, modePath, downloadCodeForm);
                if (fileNames != null) {
                    File file = new File(fileNames);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    @Cleanup FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.write(sw.toString(), fos, Constants.UTF_8);
                    IOUtils.closeQuietly(sw);
                    IOUtils.closeQuietly(fos);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("渲染模板失败，表名：" + e);
            }
        }
    }

    /**
     * 封装列表数据
     */
    private void columnData(List<FormAllModel> formAllModel, ColumnDataModel columnDataModel, Map<String, String> tableNameAll, Map<String, Object> map) {
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //显示数据
        List<ColumnListField> columnListAll = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListField.class);
        Map<String, List<FormAllModel>> mastTableList = new HashMap<>();
        Map<String, List<ColumnListField>> childColumnList = new HashMap<>();
        List<ColumnListField> columnMastList = new ArrayList<>();
        for (ColumnListField columnList : columnListAll) {
            String prop = columnList.getProp();
            FormAllModel model = mastTable.stream().filter(t -> t.getFormMastTableModel().getVModel().equals(prop)).findFirst().orElse(null);
            if (model == null) {
                String[] split = prop.split("-");
                if (split.length == 1) {
                    columnMastList.add(columnList);
                } else {
                    List<ColumnListField> childList = childColumnList.get(split[0]) != null ? childColumnList.get(split[0]) : new ArrayList<>();
                    String vModel = split[1];
                    columnList.setVModel(vModel);
                    childList.add(columnList);
                    childColumnList.put(split[0], childList);
                }
            } else {
                FormMastTableModel formMastTableModel = model.getFormMastTableModel();
                String tableName = formMastTableModel.getTable();
                List<FormAllModel> columnListList = mastTableList.get(tableName) != null ? mastTableList.get(tableName) : new ArrayList<>();
                model.setFormMastTableModel(formMastTableModel);
                columnListList.add(model);
                mastTableList.put(tableName, columnListList);
            }
        }
        map.put("childColumnList", childColumnList);
        map.put("columnList", columnMastList);
        map.put("columnMastList", mastTableList);
        map.put("AppColumnList", columnDataModel.getColumnList());
        //导入
        UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
        List<String> selectKey = uploaderTemplateModel.getSelectKey();
        int headerRowIndex = selectKey.stream().filter(t -> t.split("-").length > 1).count() > 0 ? 2 : 1;
        map.put("headerRowIndex", headerRowIndex);
        map.put("uploaderDataType", "2".equals(uploaderTemplateModel.getDataType()));
        //排序
        String sort = StringUtil.isNotEmpty(columnDataModel.getSortList()) ? columnDataModel.getSortList() : "[]";
        List<ColumnListField> sortListAll = JsonUtil.getJsonToList(sort, ColumnListField.class);
        List<ColumnListField> sortList = new ArrayList<>();
        for (int i = 0; i < sortListAll.size(); i++) {
            ColumnListField columnList = sortListAll.get(i);
            String prop = columnList.getProp();
            FormAllModel model = mast.stream().filter(t -> prop.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (model != null) {
                sortList.add(columnList);
            }
        }
        map.put("sortList", sortList);
        boolean isSidx = sortList.stream().filter(t -> t.getProp().equals(columnDataModel.getDefaultSidx())).count() > 0;
        if (isSidx) {
            map.put("defaultSidx", columnDataModel.getDefaultSidx());
        }
        //搜索
        List<FieLdsModel> searchVOList = JsonUtil.getJsonToList(columnDataModel.getSearchList(), FieLdsModel.class);
        List<FieLdsModel> mastTableSearch = new ArrayList<>();
        List<FieLdsModel> childSearch = new ArrayList<>();
        List<FieLdsModel> mastSearch = new ArrayList<>();
        List<Map<String, Object>> searchAll = new LinkedList<>();
        for (FieLdsModel columnSearch : searchVOList) {
            List<TemplateJsonModel> templateJsonAll = new ArrayList<>();
            templateJsonAll.addAll(columnSearch.getConfig().getTemplateJson());
            List<TemplateJsonModel> templateJsonModelList = JsonUtil.getJsonToList(columnSearch.getTemplateJson(), TemplateJsonModel.class);
            templateJsonAll.addAll(templateJsonModelList);
            columnSearch.getConfig().setTemplateJson(templateJsonAll);
            Map<String, Object> column = new HashMap<>();
            String vmodel = columnSearch.getVModel();
            boolean isMast = mast.stream().filter(t -> vmodel.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).count() > 0;
            boolean isMastTable = mastTable.stream().filter(t -> vmodel.equals(t.getFormMastTableModel().getVModel())).count() > 0;
            if (isMast) {
                column.put("key", "mastSearch");
                mastSearch.add(columnSearch);
            } else if (isMastTable) {
                column.put("key", "mastTableSearch");
                mastTableSearch.add(columnSearch);
            } else {
                columnSearch.setVModel(columnSearch.getVModel().replaceAll("-", "_"));
                column.put("key", "childSearch");
                childSearch.add(columnSearch);
            }
            column.put("html", columnSearch);
            searchAll.add(column);
        }
        map.put("searchAll", searchAll);
        map.put("searchList", mastTableSearch);
        map.put("childSearch", childSearch);
        map.put("mastsearchList", mastSearch);
        map.put("useDataPermission", columnDataModel.getUseDataPermission() != null ? columnDataModel.getUseDataPermission() : false);
        map.put("useBtnPermission", columnDataModel.getUseBtnPermission() != null ? columnDataModel.getUseBtnPermission() : false);
        map.put("useFormPermission", columnDataModel.getUseFormPermission() != null ? columnDataModel.getUseFormPermission() : false);
        map.put("useColumnPermission", columnDataModel.getUseColumnPermission() != null ? columnDataModel.getUseColumnPermission() : false);
    }

    //小沈代码
    public void htmlTemplatesList(FormGenModel formGenModel) {
        VisualdevEntity entity = formGenModel.getEntity();
        DownloadCodeForm downloadCodeForm = formGenModel.getDownloadCodeForm();
        FormDataModel model = formGenModel.getModel();
        String template = formGenModel.getTemplatePath();
        String pKeyName = formGenModel.getPKeyName();
        ColumnDataModel columnDataModel = StringUtil.isNotEmpty(entity.getColumnData()) ? JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class) : new ColumnDataModel();
        Integer type = columnDataModel.getType();
        if(!VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType())){
            columnDataModel.setColumnList("[]");
            columnDataModel.setHasPage(true);
            columnDataModel.setRuleList(new SuperJsonModel());
        }

        //按钮
        List<BtnData> btnsListAll = new ArrayList<>();
        List<BtnData> bntList = StringUtil.isNotEmpty(columnDataModel.getBtnsList()) ? JsonUtil.getJsonToList(columnDataModel.getBtnsList(), BtnData.class) : new ArrayList<>();
        List<BtnData> columnBtnsList = StringUtil.isNotEmpty(columnDataModel.getColumnBtnsList()) ? JsonUtil.getJsonToList(columnDataModel.getColumnBtnsList(), BtnData.class) : new ArrayList<>();
        btnsListAll.addAll(bntList);
        btnsListAll.addAll(columnBtnsList);
        columnDataModel.setBtnsList(btnsListAll);
        List<String> templates = new ArrayList<>();
        //前端页面
        if("TemplateCode2".equals(template)){
            if(Objects.equals(4,type)){
                templates.add(template + File.separator + "html" + File.separator + "extraForm.vue.vm");
                templates.add(template + File.separator + "html" + File.separator + "indexEdit.vue.vm");
            }else {
                templates.add(template + File.separator + "html" + File.separator + "index.vue.vm");
                templates.add(template + File.separator + "html" + File.separator + "Form.vue.vm");
            }
            templates.add(template + File.separator + "html" + File.separator + "Detail.vue.vm");
        }
        if("TemplateCode3".equals(template)){
            if(Objects.equals(4,type)){
                templates.add(template + File.separator + "html" + File.separator + "indexEdit.vue.vm");
            }else {
                templates.add(template + File.separator + "html" + File.separator + "index.vue.vm");
            }
            templates.add(template + File.separator + "html" + File.separator + "Form.vue.vm");
        }

        if("TemplateCode4".equals(template) || "TemplateCode1".equals(template)){
            templates.add(template + File.separator + "html" + File.separator + "index.vue.vm");
        }

        if("TemplateCode5".equals(template)){
            templates.add(template + File.separator + "html" + File.separator + "Form.vue.vm");
            templates.add(template + File.separator + "html" + File.separator + "index.vue.vm");
        }

        //自定义包名
        Map<String, Object> map = new HashMap<>(16);
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<ColumnListField> columnList = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListField.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        recursionForm.setList(list);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        List<FormAllModel> formAllList = JsonUtil.getJsonToList(formAllModel, FormAllModel.class);

        List<FieLdsModel> tableModelFields = new ArrayList<>();
        FunctionFormPublicUtil.getTableModels(list, tableModelFields);

        //form的属性
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        map.put("ableAll", JsonUtil.getListToJsonArray(formAllModel));
        //columnTempJson

        //主表的属性
        for (int i = 0; i < mast.size(); i++) {
            FormAllModel mastModel =  mast.get(i);
            FieLdsModel fieLdsModel = mastModel.getFormColumnModel().getFieLdsModel();
            //接口templatejson转换
            if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                List<TemplateJsonModel> templateJson = fieLdsModel.getConfig().getTemplateJson();
                templateJson.addAll(JsonUtil.getJsonToList(fieLdsModel.getTemplateJson(),TemplateJsonModel.class));
                fieLdsModel.setTemplateJson(JsonUtil.getObjectToString(templateJson));
            }
        }

        //副表的属性
        for (int i = 0; i < mastTable.size(); i++) {
            FormAllModel mastModel =  mastTable.get(i);
            FieLdsModel fieLdsModel = mastModel.getFormMastTableModel().getMastTable().getFieLdsModel();
            //接口templatejson转换
            if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                List<TemplateJsonModel> templateJson = fieLdsModel.getConfig().getTemplateJson();
                templateJson.addAll(JsonUtil.getJsonToList(fieLdsModel.getTemplateJson(),TemplateJsonModel.class));
                fieLdsModel.setTemplateJson(JsonUtil.getObjectToString(templateJson));
            }
        }

        List<String> id = new ArrayList<>();
        List<ColumnListModel> complexHeaderModelList = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListModel.class);
        //复杂表头
        List<HeaderModel> complexHeaderList = new ArrayList<>();
        if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
            for (HeaderModel headerModel : columnDataModel.getComplexHeaderList()) {
                List<String> childColumns = headerModel.getChildColumns();
                List<ColumnListModel> columnListModelList = complexHeaderModelList.stream().filter(t -> childColumns.contains(t.getId())).collect(Collectors.toList());
                List<FormColumnModel> formColumnModel = new ArrayList<>();
                for (ColumnListModel columnListModel : columnListModelList) {
                    FormColumnModel columnModel = new FormColumnModel();
                    FieLdsModel fieLdsModel = JsonUtil.getJsonToBean(columnListModel, FieLdsModel.class);
                    columnModel.setFieLdsModel(fieLdsModel);
                    formColumnModel.add(columnModel);
                }
                headerModel.setChildList(formColumnModel);
                complexHeaderList.add(headerModel);
                id.addAll(childColumns);
            }
        }

        List<TableModel> tablesList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
        //列表字段
        List<ColumnListModel> columnListModelList = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListModel.class).stream().filter(t->!id.contains(t.getId())).collect(Collectors.toList());
        List<ColumnListModel> columnListModelList2 = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListModel.class);
        List<ColumnListModel> notChildColList = columnListModelList.stream().filter(copy -> !copy.getId().toLowerCase().startsWith("tablefield")).collect(Collectors.toList());
        List<ColumnListModel> notChildColList2 = columnListModelList2.stream().filter(copy -> !copy.getId().toLowerCase().startsWith("tablefield")).collect(Collectors.toList());

        //列表子表折叠
        List<ColumnListModel> cldColModelList = new ArrayList<>();

        List<String> tablekeys = table.stream().map(t -> t.getChildList().getTableModel()).collect(Collectors.toList());
        Map<String, Integer> keyNum = new HashMap<>();
        for (String key : tablekeys) {
            ColumnListModel columnListModel = columnListModelList.stream().filter(c -> c.getProp().startsWith(key)).findFirst().orElse(null);
            int i = columnListModelList.indexOf(columnListModel);
            keyNum.put(key, i);
        }

        Map<String, List<ColumnListModel>> childFiledLists = columnListModelList.stream().filter(c -> c.getProp().toLowerCase()
                .contains("tablefield")).collect(Collectors.groupingBy(t -> t.getProp().substring(0, t.getProp().lastIndexOf("-"))));
        List<ColumnChildListModel> childModels = new ArrayList<>();
        Iterator<Map.Entry<String, List<ColumnListModel>>> colentries = childFiledLists.entrySet().iterator();
        while (colentries.hasNext()) {
            Map.Entry<String, List<ColumnListModel>> next = colentries.next();
            ColumnListModel listModel = new ColumnListModel();
            ColumnChildListModel mod = new ColumnChildListModel();
            mod.setTableField(next.getKey());
            List<ColumnListModel> value = next.getValue();
            value.get(0).setFirst(true);
            String label = value.get(0).getLabel();
            String substring = label.substring(0, label.lastIndexOf("-"));
            mod.setLabel(substring);
            value.stream().forEach(v -> {
                String prop = v.getProp();
                String valueLabel = v.getLabel();
                String propSub = prop.substring(prop.lastIndexOf("-") + 1);
                String labelSub = valueLabel.substring(valueLabel.lastIndexOf("-") + 1);
                v.setLabel(labelSub);
                v.setProp(propSub);
            });
            mod.setFields(value);
            listModel.setProp(next.getKey());
            listModel.setColumnChildListModel(mod);
            listModel.setJnpfKey(JnpfKeyConsts.CHILD_TABLE);
            childModels.add(mod);
            int i = keyNum.get(next.getKey());
            columnListModelList.set(i, listModel);
            cldColModelList.add(listModel);
        }

        columnListModelList = columnListModelList.stream().filter(c -> {
            boolean b = true;
            if (c.getId() != null && c.getId().toLowerCase().contains("tablefield")) {
                if (!c.getJnpfKey().equals(JnpfKeyConsts.CHILD_TABLE)) {
                    b = false;
                }
            }
            return b;
        }).collect(Collectors.toList());

        //取对应表的别名
        Map<String, String> tableNameRenames = FunctionFormPublicUtil.tableNameRename(downloadCodeForm, tablesList);

        List<FieLdsModel> allColumnFiledModelList = new ArrayList<>();
        allColumnFiledModelList.addAll(mast.stream().map(ma -> ma.getFormColumnModel().getFieLdsModel()).collect(Collectors.toList()));
        allColumnFiledModelList.addAll(mastTable.stream().map(mt -> mt.getFormMastTableModel().getMastTable().getFieLdsModel()).collect(Collectors.toList()));
        for (FormAllModel allModel : table) {
            List<FormColumnModel> childList = allModel.getChildList().getChildList();
            for (FormColumnModel formColumnModel : childList) {
                allColumnFiledModelList.add(formColumnModel.getFieLdsModel());
            }
        }
        columnListModelList.stream().forEach(c -> {
            String prop = c.getProp();
            FieLdsModel fieLdsModel = allColumnFiledModelList.stream().filter(f -> prop.equals(f.getId())).findFirst().orElse(null);
            if (fieLdsModel != null) {
                c.setDataType(fieLdsModel.getConfig().getDataType());
                c.setVModel(fieLdsModel.getVModel());
                c.setMultiple(c.getMultiple() != null ? c.getMultiple() : fieLdsModel.getMultiple());
                //数字组件新增属性
                c.setAddonBefore(fieLdsModel.getAddonBefore());
                c.setAddonAfter(fieLdsModel.getAddonAfter());
                c.setIsAmountChinese(fieLdsModel.getIsAmountChinese());
                c.setThousands(fieLdsModel.isThousands());
                c.setControls(fieLdsModel.getControls());
                //时间控件添加字段
                c.setStartTime(fieLdsModel.getStartTime());
                c.setEndTime(fieLdsModel.getEndTime());
                c.setStartRelationField(fieLdsModel.getConfig().getStartRelationField());
                c.setEndRelationField(fieLdsModel.getConfig().getEndRelationField());

                if (fieLdsModel.getSelectType() != null) {
                    c.setSelectType(fieLdsModel.getSelectType());
                    c.setAbleIds(fieLdsModel.getAbleIds());
                    c.setAbleDepIds(fieLdsModel.getAbleDepIds());
                    c.setAbleGroupIds(fieLdsModel.getAbleGroupIds());
                    c.setAblePosIds(fieLdsModel.getAblePosIds());
                    c.setAbleRoleIds(fieLdsModel.getAbleRoleIds());
                    c.setAbleUserIds(fieLdsModel.getAbleUserIds());
                }
            }
        });

        //列表中区别子表正则
        String reg = "^[jnpf_]\\S*_jnpf\\S*";
        columnListModelList.stream().filter(c -> c.getProp().matches(reg)).forEach(
                cl -> {
                    String s = cl.getProp();
                    String s1 = s.substring(s.lastIndexOf("jnpf_")).replace("jnpf_", "");
                    String s2 = s.substring(s.indexOf("_") + 1, s.lastIndexOf("_jnpf"));
                    cl.setNewProp(s2.toLowerCase() + "." + s1);
                    cl.setColumnTableName(s2);
                }
        );

        List<FieLdsModel> searchList = StringUtil.isNotEmpty(columnDataModel.getSearchList()) ? JsonUtil.getJsonToList(columnDataModel.getSearchList(), FieLdsModel.class) : new ArrayList<>();
        boolean keywordModels = searchList.stream().filter(t->t.getIsKeyword()!=null && t.getIsKeyword()).collect(Collectors.toList()).size()>0;
        if(Objects.equals(columnDataModel.getType(), 4)){
            searchList = searchList.stream().filter(c->c.getId() != null && !c.getId().toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList());
        }

        for (FieLdsModel fieLdsModel : searchList) {
            Object value = fieLdsModel.getValue();
            if(value instanceof String){
                fieLdsModel.getConfig().setValueType("String");
            }
            String vModel = fieLdsModel.getId();
            if (vModel.contains("-")) {
                vModel = vModel.replaceAll("-", "_");
            }
            fieLdsModel.getConfig().setLabel(fieLdsModel.getLabel());
            fieLdsModel.setVModel(vModel);
        }

        //查询条件
        Map<String, String> searchAll = new HashMap<>();
        List<SearchTypeModel> searchTypeModelList = new ArrayList<>();
        List<String> jnpfkey = new ArrayList() {{
            add(JnpfKeyConsts.COM_INPUT);
            add(JnpfKeyConsts.TEXTAREA);
        }};
        searchList.stream().forEach(fieLdsModel -> {
            SearchTypeModel searchTypeModel = new SearchTypeModel();
            searchTypeModel.setJnpfKey(fieLdsModel.getConfig().getJnpfKey());
            Integer seachType = fieLdsModel.getSearchType();
            if (jnpfkey.contains(searchTypeModel.getJnpfKey()) && seachType.equals(3)) {
                seachType = 2;
            }
            searchTypeModel.setSearchType(seachType);
            searchTypeModel.setVModel(fieLdsModel.getVModel());
            searchTypeModel.setLabel(fieLdsModel.getConfig().getLabel());
            searchTypeModel.setFormat(fieLdsModel.getFormat());
            String placeholder = fieLdsModel.getSearchType().equals(3) ? fieLdsModel.getPlaceholder()
                    : DataControlUtils.getPlaceholder(fieLdsModel.getConfig().getJnpfKey());
            if (searchTypeModel.getSearchType().equals(3)) {
                searchAll.put(searchTypeModel.getVModel(), searchTypeModel.getLabel());
            }
            searchTypeModel.setPlaceholder(placeholder);
            ConfigModel config = fieLdsModel.getConfig();
            config.setDefaultValue(fieLdsModel.getValue());
            if (config.getDefaultValue() instanceof String) {
                config.setValueType("String");
            }
            if (config.getDefaultValue() == null) {
                config.setValueType("undefined");
            }
            searchTypeModel.setConfig(config);
            searchTypeModel.setShowLevel(fieLdsModel.getShowLevel());
            searchTypeModel.setMultiple(String.valueOf(fieLdsModel.getSearchMultiple()));
            searchTypeModelList.add(searchTypeModel);
        });
        TableModel mainTableModel = tablesList.stream().filter(t -> t.getTypeId().equals("1")).findFirst().orElse(null);

        if (searchTypeModelList.size() > 0) {
            for (TableFields tableFields : mainTableModel.getFields()) {
                searchTypeModelList.stream().forEach(searchTypeModel -> {
                    if (searchTypeModel.getVModel().equals(tableFields.getField())) {
                        searchTypeModel.setDataType(tableFields.getDataType());
                    }
                });
            }
        }
        //子表赋值
        List<Map<String, Object>> child = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            List<FormColumnModel> tableList = childList.getChildList();
            String sClassName = tableNameRenames.get(childList.getTableName());
            for (FormColumnModel columnModel : tableList) {
                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                List<TemplateJsonModel> templateJsonAll = new ArrayList<>();
                templateJsonAll.addAll(fieLdsModel.getConfig().getTemplateJson());
                List<TemplateJsonModel> templateJsonModelList = JsonUtil.getJsonToList(fieLdsModel.getTemplateJson(), TemplateJsonModel.class);
                templateJsonAll.addAll(templateJsonModelList);
                for (TemplateJsonModel templateJsonModel : templateJsonAll) {
                    if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                        String[] fieldList = templateJsonModel.getRelationField().split("-");
                        if (fieldList.length > 1) {
                            templateJsonModel.setRelationField(sClassName.toLowerCase() + "-" + fieldList[1]);
                        }
                    }
                }
                for (TemplateJsonModel templateJsonModel : templateJsonModelList) {
                    if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                        String[] fieldList = templateJsonModel.getRelationField().split("-");
                        if (fieldList.length > 1) {
                            templateJsonModel.setRelationField(sClassName.toLowerCase() + "List-" + fieldList[1]);
                        }
                    }
                }
                fieLdsModel.setTemplateJson(JsonUtil.getObjectToString(templateJsonModelList));
                fieLdsModel.getConfig().setTemplateJson(templateJsonAll);
            }
            childList.setChildList(tableList);
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            String className = DataControlUtils.captureName(sClassName);
            childs.put("className", className);
            child.add(childs);
        }

        //界面
        //是否开启行内编辑columnDataListFiled
        map.put("lineEdit", Objects.equals(columnDataModel.getType(), 4));
        //是否开启分组表格
        if (Objects.equals(columnDataModel.getType(), 3)) {
            ColumnListModel columnListModel = columnListModelList.get(0);
            columnListModel.setFirst(true);
            columnListModelList.set(0, columnListModel);
        }

        //判断列表是否存在冻结
        boolean none = columnListModelList.stream().anyMatch(c -> "left".equals(c.getFixed()) || "right".equals(c.getFixed()));
        map.put("hasFixed", none);
        Set<String> set = new HashSet<>();
        List<String> nameAgain = new ArrayList<>();

        map.put("nameAgain", nameAgain);
        map.put("groupTable", Objects.equals(columnDataModel.getType(), 3));
        //树形同步异步
        if (Objects.equals(columnDataModel.getType(), 5)) {
            columnDataModel.setHasPage(false);
        }
        map.put("treeTable", Objects.equals(columnDataModel.getType(), 5));
        map.put("treeLazyType", true);
        map.put("parentField", columnDataModel.getParentField());
        map.put("subField", columnDataModel.getSubField());
        //合计
        boolean configurationTotal = columnDataModel.isShowSummary();
        if (Objects.equals(columnDataModel.getType(), 3) || Objects.equals(columnDataModel.getType(), 5)) {
            configurationTotal = false;
        }
        map.put("configurationTotal", configurationTotal);
        List<String> summaryList = CollectionUtils.isEmpty(columnDataModel.getSummaryField()) ? Collections.EMPTY_LIST : columnDataModel.getSummaryField();
        map.put("fieldsTotal", JsonUtil.getObjectToString(summaryList));
        //合计千分位
        List<String> thousandsField = ObjectUtil.isNotEmpty(columnDataModel.getType())?FormCommonUtil.getSummaryThousandList(mast, mastTable, columnDataModel.getType()):new ArrayList<>();
        map.put("thousandsField", JsonUtil.getObjectToString(thousandsField));
        //判断是否有关键词搜索
        map.put("keywordModels", keywordModels);

        //乐观锁
        map.put("type", type);
        map.put("hasConfirmAndAddBtn", formData.getHasConfirmAndAddBtn());
        map.put("version", formData.getConcurrencyLock());
        map.put("logicalDelete", formData.getLogicalDelete());
        map.put("module", downloadCodeForm.getModule());
        map.put("className", DataControlUtils.captureName(model.getClassName()));
        map.put("formRef", model.getFormRef());
        map.put("formModel", model.getFormModel());
        map.put("size", model.getSize());
        map.put("labelPosition", model.getLabelPosition());
        map.put("generalWidth", model.getGeneralWidth());
        map.put("fullScreenWidth", model.getFullScreenWidth());
        map.put("drawerWidth", model.getDrawerWidth());
        map.put("formStyle", model.getFormStyle());
        map.put("labelWidth", model.getLabelWidth());
        map.put("labelSuffix", formData.getLabelSuffix());
        map.put("formRules", model.getFormRules());
        map.put("gutter", model.getGutter());
        map.put("disabled", model.getDisabled());
        map.put("span", model.getSpan());
        map.put("formBtns", model.getFormBtns());
        map.put("idGlobal", model.getIdGlobal());
        map.put("popupType", Objects.equals(entity.getWebType(),1) ? "fullScreen": model.getPopupType());
        map.put("form", formAllList);
        map.put("columnData", JsonUtil.stringToMap(entity.getColumnData()));

        //todo 1231546
        map.put("complexHeaderList",complexHeaderList);
        map.put("columnDataListFiled", columnListModelList);
        map.put("childModels", childModels);
        map.put("searchList", searchList);
        map.put("childTableStyle", columnDataModel.getChildTableStyle());
        map.put("cldColModelList", cldColModelList);
        map.put("notChildColList", notChildColList);
        map.put("notChildColList2", notChildColList2);

        map.put("flowFormId", entity.getId());
        map.put("enCode", entity.getEnCode());

        map.put("dataType", columnDataModel.getHasPage() ? 0 : 1);
        map.put("ruleQueryJson", JSONObject.toJSONString(columnDataModel.getRuleList()));

        //按钮
        map.put("HasCancelBtn", formData.getHasCancelBtn());
        map.put("HasConfirmBtn", formData.getHasConfirmBtn());
        map.put("HasPrintBtn", formData.getHasPrintBtn());
        map.put("CancelButton", formData.getCancelButtonText());
        map.put("ConfirmButton", formData.getConfirmButtonText());
        map.put("PrintButton", formData.getPrintButtonText());
        map.put("webType", entity.getWebType());
        map.put("isFlow", Objects.equals(entity.getEnableFlow(),1));
        map.put("TemplateCode",!"TemplateCode1".equals(template));
        String[] printId = formData.getPrintId() != null ? formData.getPrintId() : new String[]{};
        // 打印模板如果是有多个需要查询标题
        if (printId.length > 1) {
            map.put("printOptions", FormCommonUtil.getList(Arrays.asList(printId)));
        }

        // 打印模板批量的标题
        List<String> printIds = StringUtil.isNotEmpty(columnDataModel.getPrintIds()) ? JsonUtil.getJsonToList(columnDataModel.getPrintIds(), String.class) : new ArrayList<>();
        if (printIds != null && printIds.size() > 0) {
            map.put("printListOptions", FormCommonUtil.getList(printIds));
        }

        String printIdAll = String.join(",", printId);
        map.put("printId", printIdAll);
        map.put("printIds",  String.join(",", printIds));
        map.put("moduleId", entity.getId());

        map.put("groupField", columnDataModel.getGroupField());
        if(Objects.equals(columnDataModel.getType(),2)){
            String treeRelationField = columnDataModel.getTreeRelation().replaceAll("-", "_");
            map.put("treeRelationField", treeRelationField);
        }

        //列表子表数据model
        List<ColumnListDataModel> formChildList = new ArrayList<>();
        Map<String, List<FormAllModel>> groupColumnDataMap = mastTable.stream().collect(Collectors.groupingBy(m -> m.getFormMastTableModel().getTable()));
        Iterator<Map.Entry<String, List<FormAllModel>>> entries = groupColumnDataMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, List<FormAllModel>> formEntries = entries.next();
            Map<String, Object> objectMap = new HashMap<>();
            String className = DataControlUtils.captureName(tableNameRenames.get(formEntries.getKey()));
            ColumnListDataModel columnListDataModel = new ColumnListDataModel();
            columnListDataModel.setModelName(className);
            columnListDataModel.setModelUpName(DataControlUtils.captureName(className));
            columnListDataModel.setModelLowName(DataControlUtils.initialLowercase(className));
            List<FormAllModel> allModels = formEntries.getValue();
            allModels.stream().forEach(m -> {
                String vModel = m.getFormMastTableModel().getField();
                m.getFormMastTableModel().getMastTable().getFieLdsModel().setVModel(vModel);
            });
            List<String> fields = allModels.stream().map(m ->
                    m.getFormMastTableModel().getField()).collect(Collectors.toList());
            columnListDataModel.setFieldList(fields);
            columnListDataModel.setFieLdsModelList(allModels.stream().map(al -> al.getFormMastTableModel()).collect(Collectors.toList()));
            columnListDataModel.setTableName(formEntries.getKey());
            formChildList.add(columnListDataModel);
            List<FormColumnModel> children = formEntries.getValue().stream().map(allModel -> allModel.getFormMastTableModel().getMastTable()).collect(Collectors.toList());
            //表字段给的范围-转换json
            children.forEach(item -> {
                item.setFieLdsModel(DataControlUtils.setAbleIDs(item.getFieLdsModel()));
            });
            FormColumnTableModel formColumnTableModel = new FormColumnTableModel();
            formColumnTableModel.setChildList(children);
            objectMap.put("children", formColumnTableModel);
            objectMap.put("module", model.getAreasName());
            objectMap.put("className", className);
        }

        //共用
        map.put("children", child);
        map.put("mastTable", mastTable);
        map.put("fields", mast);
        map.put("columnChildren", formChildList);
        pKeyName = pKeyName.toLowerCase().trim().replaceAll("f_", "");
        pKeyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, pKeyName);
        map.put("pKeyName", pKeyName);
        map.put("columnList", columnList);
        String modelPathName = downloadCodeForm.getClassName().toLowerCase();
        map.put("modelPathName", modelPathName);
        map.put("formModelName", entity.getFullName());
        map.put("isMain", true);

        //是否开启高级查询
        Boolean hasSuperQuery = columnDataModel.getHasSuperQuery();
        map.put("superQuery", hasSuperQuery);

        String path = formGenModel.getServiceDirectory() + formGenModel.getFileName();
        String className = model.getClassName().substring(0, 1).toUpperCase() + model.getClassName().substring(1);
        String modePath = model.getClassName().substring(0, 1).toLowerCase() + model.getClassName().substring(1);
        htmlTemplates(map, templates, path, className, modePath, downloadCodeForm);

        try {
            if (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType()) && !Objects.equals(entity.getType(), 3)) {
                Map<String, Object> columnDataModelMap = JsonUtil.stringToMap(entity.getColumnData());
                String colData = JSONObject.toJSONString(columnDataModelMap.get("columnList"), SerializerFeature.WriteMapNullValue);
                String colListJsPath = path + File.separator + "html" + File.separator + "web" + File.separator + modelPathName + File.separator + "columnList.js";
                SuperQueryUtil.CreateJsFile(colData, colListJsPath, "columnList");
                if (hasSuperQuery) {
                    String superSqJsPath = path + File.separator + "html" + File.separator + "web" + File.separator + modelPathName + File.separator + "superQueryJson.js";
                    String data = JSONObject.toJSONString(columnDataModelMap.get("columnOptions"), SerializerFeature.WriteMapNullValue);
                    SuperQueryUtil.CreateJsFile(data, superSqJsPath, "superQueryJson");
                }
            }
        } catch (Exception e) {

        }
    }


    //----------------------------代码-------------------------------------------------------

    /**
     * 生成表集合
     *
     * @param genModel 对象
     * @throws SQLException
     */
    public void generate(FormGenModel genModel) throws Exception {
        VisualdevEntity entity = genModel.getEntity();
        List<TableModel> list = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
        DownloadCodeForm downloadCodeForm = genModel.getDownloadCodeForm();
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
        Map<String, String> tableNameAll = tableName(tableModelList, downloadCodeForm);
        //生成代码
        for (TableModel model : list) {
            String table = model.getTable();
            genModel.setTable(table);
            genModel.setClassName(tableNameAll.get(model.getTable()));
            genModel.setMainTable(false);
            if ("1".equals(model.getTypeId())) {
                genModel.setClassName(downloadCodeForm.getClassName());
                genModel.setMainTable(true);
                this.setCode(genModel);
            } else if ("0".equals(model.getTypeId())) {
                String name = tableNameAll.get(table);
                String className = name.substring(0, 1).toUpperCase() + name.substring(1);
                genModel.setClassName(className);
                this.setCode(genModel);
            }
        }
    }

    /**
     * 生成主表
     *
     * @param genModel 对象
     * @throws SQLException
     */
    private void setCode(FormGenModel genModel) throws Exception {
        DownloadCodeForm downloadCodeForm = genModel.getDownloadCodeForm();
        String className = genModel.getClassName().substring(0, 1).toUpperCase() + genModel.getClassName().substring(1);
        Template7Model model = this.templateModel(genModel, className);
        //执行代码生成器
        GenerateParamModel generateParamModel = new GenerateParamModel().builder()
                .dataSourceUtil(genModel.getDataSourceUtil())
                .path(FormCommonUtil.getLocalBasePath() + genModel.getConfigValueUtil())
                .fileName(genModel.getFileName())
                .templatesPath(genModel.getTemplatePath())
                .downloadCodeForm(downloadCodeForm)
                .entity(genModel.getEntity())
                .userInfo(genModel.getUserInfo())
                .configValueUtil(genModel.getConfigValueUtil())
                .linkEntity(genModel.getLinkEntity())
                .pKeyName(genModel.getPKeyName())
                .className(className)
                .isMainTable(genModel.isMainTable())
                .table(genModel.getTable())
                .build();
        Map<String, Object> columndata = getcolumndata(generateParamModel);
        this.javaGenerate(columndata, model, genModel, genModel.isMainTable());
    }

    /**
     * 封装数据
     *
     * @param genModel
     * @param className
     * @return
     */
    private Template7Model templateModel(FormGenModel genModel, String className) {
        DownloadCodeForm downloadCodeForm = genModel.getDownloadCodeForm();
        Template7Model template7Model = new Template7Model();
        template7Model.setClassName(className);
        template7Model.setServiceDirectory(genModel.getServiceDirectory());
        template7Model.setCreateDate(DateUtil.daFormat(new Date()));
        template7Model.setCreateUser(GenBaseInfo.AUTHOR);
        template7Model.setCopyright(GenBaseInfo.COPYRIGHT);
        template7Model.setDescription(downloadCodeForm.getDescription());
        return template7Model;
    }

    /**
     * 封装数据
     *
     * @param formAllModel
     */
    private Map<String, String> forDataMode(FormGenModel genModel, List<FormAllModel> formAllModel) {
        VisualdevEntity entity = genModel.getEntity();
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
        RecursionForm recursionForm = new RecursionForm(list, tableModelList);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        Map<String, String> tableNameAll = this.tableName(tableModelList, genModel.getDownloadCodeForm());
        return tableNameAll;
    }

    /**
     * 封装数据
     *
     * @param columndata
     * @param model
     * @param genModel
     * @param isMast
     */
    private void javaGenerate(Map<String, Object> columndata, Template7Model model, FormGenModel genModel, boolean isMast) {
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
        gc.setAuthor(model.getCreateUser());
        gc.setOpen(false);

        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        String className = model.getClassName();
        if (isMast) {
            gc.setControllerName(className + GenFileNameSuffix.CONTROLLER);
        }
        gc.setEntityName(className + GenFileNameSuffix.ENTITY);
        gc.setMapperName(className + GenFileNameSuffix.MAPPER);
        gc.setXmlName(className + GenFileNameSuffix.MAPPER_XML);
        gc.setServiceName(className + GenFileNameSuffix.SERVICE);
        gc.setServiceImplName(className + GenFileNameSuffix.SERVICEIMPL);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        UserInfo userInfo = genModel.getUserInfo();
        DbLinkEntity linkEntity = genModel.getLinkEntity();

        mpg.setDataSource(SourceUtil.dbConfig(TenantDataSourceUtil.getTenantSchema(), linkEntity));

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        // 表名生成策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // 需要生成的表
        String table = genModel.getTable();
        strategy.setInclude(table);
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        DownloadCodeForm downloadCodeForm = genModel.getDownloadCodeForm();
        pc.setParent(downloadCodeForm.getModulePackageName());
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        List<FileOutConfig> focList = new ArrayList<>();
        String javaPath = model.getServiceDirectory();
        String templatePath = this.tempPath(genModel);
        String fileName = genModel.getFileName();
        String path = genModel.getTemplateCodePath();
        String module = downloadCodeForm.getModule();
        String modulePackageName = downloadCodeForm.getModulePackageName();
        String flowWorkPath = "jnpf-" + module + File.separator + "jnpf-" + module + "-%s" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + modulePackageName + File.separator + "%s";
        if(Objects.equals(genModel.getEntity().getType(),3)){
            flowWorkPath = "jnpf-workflow" + File.separator + "jnpf-workflow-form" + File.separator + "jnpf-workflow-form-%s" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "jnpf" + File.separator + "form" + File.separator + "%s";
        }
        if (isMast) {
            String controller = String.format(flowWorkPath, "controller", "controller");
            focList.add(new FileOutConfig("java" + File.separator + "Controller.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + controller + File.separator + tableInfo.getControllerName() + StringPool.DOT_JAVA;
                }
            });
        }
        String entity = String.format(flowWorkPath, "entity", "entity");
        focList.add(new FileOutConfig("java" + File.separator + "Entity.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                List<TableField> fieldAll = tableInfo.getFields();
                TableField mainTableField = fieldAll.stream().filter(tableField -> tableField.isKeyFlag()).findFirst().orElse(null);
                fieldAll = fieldAll.stream().filter(DataControlUtils.distinctByKey(t -> t.getName())).collect(Collectors.toList());
                if (mainTableField != null) {
                    fieldAll.stream().filter(tableField -> tableField.getName().equals(mainTableField.getName())).forEach(t -> t.setKeyFlag(mainTableField.isKeyFlag()));
                }
                for (TableField field : fieldAll) {
                    String name = field.getName().toLowerCase().replaceAll("f_", "");
                    field.setPropertyName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
                }
                tableInfo.setFields(fieldAll);
                return javaPath + fileName + File.separator + "java" + File.separator + entity + File.separator + tableInfo.getEntityName() + StringPool.DOT_JAVA;
            }
        });
        String mapper = String.format(flowWorkPath, "biz", "mapper");
        focList.add(new FileOutConfig("java" + File.separator + "Mapper.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + mapper + File.separator + tableInfo.getMapperName() + StringPool.DOT_JAVA;
            }
        });
        String mapperxml = "resources" + File.separator + "mapper";
        focList.add(new FileOutConfig("java" + File.separator + "Mapper.xml.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + mapperxml + File.separator + tableInfo.getMapperName() + StringPool.DOT_XML;
            }
        });
        String service = String.format(flowWorkPath, "biz", "service");
        focList.add(new FileOutConfig("java" + File.separator + "Service.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + service + File.separator + tableInfo.getServiceName() + StringPool.DOT_JAVA;
            }
        });
        String serviceImpl = String.format(flowWorkPath, "biz", "service" + File.separator + "impl");
        focList.add(new FileOutConfig("java" + File.separator + "ServiceImpl.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + serviceImpl + File.separator + tableInfo.getServiceImplName() + StringPool.DOT_JAVA;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setTemplate(new TemplateConfig().setXml(null).setMapper(null).setController(null).setEntity(null).setService(null).setServiceImpl(null));
        mpg.setCfg(cfg);
        // 执行生成
        mpg.execute(path);
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
        columndata.put("children", childTableHandle);//原child
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

        columndata.put("formDataStr", JSONObject.toJSONString(GenerateCommon.objRemoveJson(entity.getFormData())));
        columndata.put("tableListStr", JSONObject.toJSONString(entity.getVisualTables()));
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
            List<String> complexFieldList = new ArrayList<>();
            List<Map<String, Object>> complexHeaderList = new ArrayList<>();
            if(!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
                for (HeaderModel headerModel : columnDataModel.getComplexHeaderList()) {
                    complexFieldList.addAll(headerModel.getChildColumns());
                    Map<String, Object> map = JsonUtil.entityToMap(headerModel);
                    //复杂表头添加导入字段信息
                    List<Map<String, Object>> uploadFieldList = new ArrayList<>();
                    for (Map<String, Object> uploadmap : allUploadTemplates) {
                        if (headerModel.getChildColumns().contains(uploadmap.get("vModel"))) {
                            uploadFieldList.add(uploadmap);
                        }
                    }
                    map.put("uploadFieldList", uploadFieldList);
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
    private static void addTreeSearchField(TableModel mainTableModel, ColumnDataModel columnDataModel, List<SearchTypeModel> searchListAll) {
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
    private static List<ListSearchGroupModel> getListSearchGroupModels(List<TableModel> tableModelList, TableModel mainTable, Map<String, String> tableRenames, Map<String, String> childKeyTableNameMap, TableModel mainTableModel, List<SearchTypeModel> searchListAll) {
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
            //副表
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

            //子表
            Map<String, List<SearchTypeModel>> collect1 = searchListAll.stream().filter(s -> s.getId().toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX))
                    .collect(Collectors.groupingBy(t -> t.getId().substring(0, t.getId().indexOf("_"))));
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
                            String substring = vmodelall.substring(vmodelall.indexOf("_") + 1);
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

    private boolean isType(FormGenModel genModel) {
        VisualdevEntity entity = genModel.getEntity();
        boolean type = !VisualWebTypeEnum.FORM.getType().equals(entity.getWebType());
        return type;
    }

    private boolean isForm(FormGenModel genModel) {
        VisualdevEntity entity = genModel.getEntity();
        boolean type = (VisualWebTypeEnum.FORM_LIST.getType().equals(entity.getWebType()) && entity.getEnableFlow() == 0);
        return type;
    }

    private void listToString(Map<String, Object> fieLdsModelMap, FieLdsModel fieLdsModel) {
        ConfigModel configModel = fieLdsModel.getConfig();
        String dataType = configModel.getDataType();
        List<TemplateJsonModel> templateJson = configModel.getTemplateJson();
        templateJson.addAll(JsonUtil.getJsonToList(fieLdsModel.getTemplateJson(), TemplateJsonModel.class));
        JSONArray jsonArray = JsonUtil.getListToJsonArray(templateJson);
        fieLdsModelMap.put("templateJsonString", JSONObject.toJSONString(jsonArray.toJSONString()));
        String options = fieLdsModel.getOptions();
        if (StringUtil.isNotEmpty(options)) {
            JSONArray list = !"dictionary".equals(dataType) ? JsonUtil.getJsonToJsonArray(options) : new JSONArray();
            fieLdsModelMap.put("optionsString", JSONObject.toJSONString(list.toJSONString()));
        }
    }


}
