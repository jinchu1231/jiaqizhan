package jnpf.base.util.fuctionFormVue3.common;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.google.common.base.CaseFormat;
import jnpf.base.model.DownloadCodeForm;
import jnpf.base.model.Template6.ColumnListField;
import jnpf.base.model.Template7.Template7Model;
import jnpf.base.model.printdev.PrintOption;
import jnpf.base.service.IPrintDevService;
import jnpf.base.util.common.DataControlUtils;
import jnpf.constant.FileTypeConstant;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.TemplateJsonModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.FormColumnModel;
import jnpf.model.visualJson.analysis.FormEnum;
import jnpf.util.DateUtil;
import jnpf.util.FilePathUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.XSSEscape;
import jnpf.util.context.SpringContext;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/5/31
 */
public class GenerateCommon {
    public static final String IS_CLOUD = "single";

    private static IPrintDevService iPrintDevService = SpringContext.getBean(IPrintDevService.class);


    public static String getLocalBasePath() {
        return FileUploadUtils.getLocalBasePath();
    }

    public static String getPath(String type) {
        return FilePathUtil.getFilePath(type);
    }

    public static List<PrintOption> getList(List<String> ids) {
        return iPrintDevService.getPrintTemplateOptions(ids);
    }


    /**
     * 获取代码生成基础信息
     *
     * @param className
     * @return
     */
    public static Template7Model getTemplate7Model(String className) {
        Template7Model temModel = new Template7Model();
        temModel.setServiceDirectory(GenerateCommon.getPath(FileTypeConstant.CODETEMP));
        temModel.setCreateDate(DateUtil.daFormat(new Date()));
        temModel.setCreateUser(GenerateConstant.AUTHOR);
        temModel.setCopyright(GenerateConstant.COPYRIGHT);
        temModel.setClassName(DataControlUtils.captureName(className));
        temModel.setVersion(GenerateConstant.VERSION);
        temModel.setDescription(GenerateConstant.DESCRIPTION);
        return temModel;
    }

    /**
     * 获取微服务框架路径
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/3/22
     */
    public static String getCloudPath(String houzui, DownloadCodeForm downloadCodeForm) {
        //发起表单
        if ("form".equals(downloadCodeForm.getModule())) {
            return "jnpf-workflow" + File.separator + "jnpf-workflow-form" + File.separator + "jnpf-workflow-" + downloadCodeForm.getModule() + houzui + File.separator
                    + "src" + File.separator + "main" + File.separator + "java" + File.separator + "jnpf" + File.separator + "form" ;
        }
        String framePath = "jnpf-" + downloadCodeForm.getModule() + File.separator + "jnpf-" + downloadCodeForm.getModule() + houzui + File.separator
                + "src" + File.separator + "main" + File.separator + "java" + File.separator + downloadCodeForm.getModulePackageName();
        return framePath;
    }

    /**
     * 获取需要生成的文件对象
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/3/22
     */
    public static FileOutConfig getFileOutConfig(String fileName, DownloadCodeForm downloadCodeForm, String javaPath, String temName,
                                                 String typeStr, boolean concurrencyLock) {
        return new FileOutConfig("java" + File.separator + temName) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                if ("entity".equals(typeStr)) {
                    List<TableField> fieldAll = tableInfo.getFields();
                    TableField mainTableField = fieldAll.stream().filter(tableField -> tableField.isKeyFlag()).findFirst().orElse(null);
                    fieldAll = fieldAll.stream().filter(DataControlUtils.distinctByKey(t -> t.getName())).collect(Collectors.toList());
                    if (mainTableField != null) {
                        fieldAll.stream().filter(tableField -> tableField.getName().equals(mainTableField.getName())).forEach(t -> t.setKeyFlag(mainTableField.isKeyFlag()));
                    }
                    for (TableField field : fieldAll) {
                        String name = field.getName().toLowerCase();
                        name = name.startsWith("f_") ? name.substring(2) : name;
                        field.setPropertyName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
                        boolean fv = field.getName().equalsIgnoreCase(TableFeildsEnum.VERSION.getField());
                        if (fv && concurrencyLock) {
                            field.setFill("1");
                        }
                    }
                    tableInfo.setFields(fieldAll);
                }
                String eachName = "" ;
                String frontName = "" ;
                String modulName = downloadCodeForm.getModule();
                String framePath = downloadCodeForm.getModulePackageName();
                switch (typeStr) {
                    case "controller":
                        eachName = tableInfo.getControllerName();
                        framePath = getCloudPath("-controller" , downloadCodeForm);
                        break;
                    case "entity":
                        eachName = tableInfo.getEntityName();
                        framePath = getCloudPath("-entity" , downloadCodeForm);
                        break;
                    case "mapper":
                        eachName = tableInfo.getMapperName();
                        framePath = getCloudPath("-biz" , downloadCodeForm);
                        break;
                    case "xml":
                        eachName = tableInfo.getMapperName();
                        if ("cloud".equals(GenerateCommon.IS_CLOUD)) {
                            String fileFront = "jnpf-" + modulName + File.separator + "jnpf-" + modulName + "-biz" + File.separator;
                            if ("form".equals(modulName)) {
                                fileFront = "jnpf-workflow" + File.separator + "jnpf-workflow-form" + File.separator + "jnpf-workflow-form-biz" + File.separator;
                            }
                            framePath = fileFront + "src" + File.separator + "main" + File.separator + "resources" ;
                            return javaPath + fileName + File.separator + "java" + File.separator + framePath + File.separator + "mapper"
                                    + File.separator + eachName + StringPool.DOT_XML;
                        }
                        return javaPath + fileName + File.separator + "resources" + File.separator + "mapper"
                                + File.separator + eachName + StringPool.DOT_XML;
                    case "service":
                        eachName = tableInfo.getServiceName();
                        framePath = getCloudPath("-biz" , downloadCodeForm);
                        break;
                    case "impl":
                        eachName = tableInfo.getServiceImplName();
                        frontName = "service" + File.separator;
                        framePath = getCloudPath("-biz" , downloadCodeForm);
                    default:
                        break;
                }
                return javaPath + fileName + File.separator + "java" + File.separator + framePath + File.separator + frontName + typeStr
                        + File.separator + eachName + StringPool.DOT_JAVA;
            }
        };
    }

    /**
     * 下载文件列表
     *
     * @param generateParamModel
     * @return
     */
    public static List<FileOutConfig> getJavaFiles(GenerateParamModel generateParamModel) {
        String fileName = generateParamModel.getFileName();
        DownloadCodeForm downloadCodeForm = generateParamModel.getDownloadCodeForm();
        Template7Model template7Model = generateParamModel.getTemplate7Model();
        List<FileOutConfig> focList = new ArrayList<>();
        String javaPath = GenerateCommon.getLocalBasePath() + template7Model.getServiceDirectory();
        if (generateParamModel.isMainTable()) {
            focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "Controller.java.vm" , "controller" , false));
            focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "Entity.java.vm" , "entity" , generateParamModel.isConcurrencyLock()));
        } else {
            focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "Entity.java.vm" , "entity" , false));
        }
        focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "Mapper.java.vm" , "mapper" , false));
        focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "Mapper.xml.vm" , "xml" , false));
        focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "Service.java.vm" , "service" , false));
        focList.add(GenerateCommon.getFileOutConfig(fileName, downloadCodeForm, javaPath, "ServiceImpl.java.vm" , "impl" , false));
        return focList;
    }

    /**
     * 获取导出字段
     *
     * @param columnList
     * @return
     */
    public static List<ColumnListField> getExpotColumn(List<ColumnListField> columnList) {
        List<ColumnListField> listOptions = new ArrayList<>();
        columnList.forEach(item -> {
            ColumnListField columnListField = new ColumnListField();
            BeanUtil.copyProperties(item, columnListField);
            if (item.getVModel().toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                columnListField.setTableType(2);
                columnListField.setVModel(item.getVModel().split("-")[1]);
            } else if (item.getVModel().toLowerCase().contains("_jnpf_")) {
                columnListField.setTableType(1);
                columnListField.setVModel(item.getVModel().split("_jnpf_")[1]);
            } else {
                columnListField.setTableType(0);
            }
            if ("static".equals(item.getConfig().getDataType())) {
                columnListField.setOptions(JsonUtil.getObjectToString(item.getOptions()));
                if (item.getJnpfKey().equals(JnpfKeyConsts.CHECKBOX)) {
                    columnListField.setMultiple(true);
                }
            }
            listOptions.add(columnListField);
        });
        return listOptions;
    }

    /**
     * 合计和千分位字段转换至前端可用
     *
     * @param sourceList 原字段列表
     * @param type       列表类型4为行内编辑
     * @return 新字段列表
     */
    public static List<String> getSummaryList(List<String> sourceList, Integer type) {
        List<String> finalFieldsTotal = new ArrayList<>();
        String suffix = "" ;
//        if (type == 4) {
//            suffix = "_name" ;
//        }
        if (CollectionUtils.isEmpty(sourceList)) {
            return finalFieldsTotal;
        }
        for (String field : sourceList) {
            String finalField;
            if (field.startsWith("jnpf")) {
                String fieldName = field.substring(field.lastIndexOf("jnpf_")).replace("jnpf_" , "");
                String tableName = field.substring(field.indexOf("_") + 1, field.lastIndexOf("_jnpf"));
                finalField = tableName + "." + fieldName + suffix;
            } else {
                finalField = field + suffix;
            }
            finalFieldsTotal.add(finalField);
        }
        return finalFieldsTotal;
    }


    /**
     * 合计千分位字段列表
     *
     * @param mast      主表字段
     * @param mastTable 副表字段
     * @param type      列表类型 4-行内编辑
     * @return
     */
    public static List<String> getSummaryThousandList(List<FormAllModel> mast, List<FormAllModel> mastTable, Integer type) {
        String suffix = "_name" ;
        if (type == 4) {
            suffix = "" ;
        }
        List<String> thousandsField = new ArrayList<>();
        for (FormAllModel f : mast) {
            FieLdsModel fm = f.getFormColumnModel().getFieLdsModel();
            if (fm.isThousands()) {
                thousandsField.add(fm.getVModel() + suffix);
            }
        }
        for (FormAllModel f : mastTable) {
            FieLdsModel fm = f.getFormMastTableModel().getMastTable().getFieLdsModel();
            if (fm.isThousands()) {
                thousandsField.add(f.getFormMastTableModel().getTable() + "." + fm.getVModel() + suffix);
            }
        }
        return thousandsField;
    }


    /**
     * 根据模板 获取文件名
     *
     * @param path      路径
     * @param template  模板名称
     * @param className 文件名称
     * @return
     */
    public static String getFileName(String path, String template, String className, DownloadCodeForm downloadCodeForm) {
        String framePath = GenerateCommon.getCloudPath("-entity" , downloadCodeForm);
        String modelfolder = downloadCodeForm.getClassName();
        String modelPath = XSSEscape.escapePath(path + File.separator + "java" + File.separator + framePath + File.separator + "model"
                + File.separator + modelfolder.toLowerCase());
        String htmlPath = XSSEscape.escapePath(path + File.separator + "html" + File.separator + "web" + File.separator + modelfolder.toLowerCase());
        File htmlfile = new File(htmlPath);
        File modelfile = new File(modelPath);
        if (!htmlfile.exists()) {
            htmlfile.mkdirs();
        }
        if (!modelfile.exists()) {
            modelfile.mkdirs();
        }

        if (template.contains("extraForm.vue.vm")) {
            return htmlPath + File.separator + "ExtraForm.vue" ;
        }
        if (template.contains("Form.vue.vm")) {
            return htmlPath + File.separator + "Form.vue" ;
        }
        if (template.contains("FormPopup.vue.vm")) {
            return htmlPath + File.separator + "FormPopup.vue" ;
        }
        if (template.contains("index.vue.vm")) {
            return htmlPath + File.separator + "index.vue" ;
        }
        if (template.contains("indexEdit.vue.vm")) {
            return htmlPath + File.separator + "index.vue" ;
        }
        if (template.contains("Detail.vue.vm")) {
            return htmlPath + File.separator + "Detail.vue" ;
        }
        if (template.contains("api.ts.vm")) {
            //vue3生成ts文件夹
            String htmlTSPath = XSSEscape.escapePath(path + File.separator + "html" + File.separator + "web" + File.separator
                    + modelfolder.toLowerCase() + File.separator + "helper");
            File htmlJSfile = new File(htmlTSPath);
            if (!htmlJSfile.exists() && !"form".equals(downloadCodeForm.getModule())) {
                htmlJSfile.mkdirs();
            }

            return htmlPath + File.separator + "helper" + File.separator + "api.ts" ;
        }
        //后端代码
        if (template.contains("InfoVO.java.vm")) {
            return modelPath + File.separator + className + "InfoVO.java" ;
        }
        if (template.contains("Form.java.vm")) {
            return modelPath + File.separator + className + "Form.java" ;
        }
        if (template.contains("ListVO.java.vm")) {
            return modelPath + File.separator + className + "ListVO.java" ;
        }
        if (template.contains("GroupVO.java.vm")) {
            return modelPath + File.separator + className + "GroupVO.java" ;
        }
        if (template.contains("Pagination.java.vm")) {
            return modelPath + File.separator + className + "Pagination.java" ;
        }
        if (template.contains("ExcelVO.java.vm")) {
            return modelPath + File.separator + className + "ExcelVO.java" ;
        }
        if (template.contains("ExcelErrorVO.java.vm")) {
            return modelPath + File.separator + className + "ExcelErrorVO.java" ;
        }
        if (template.contains("Model.java.vm")) {
            return modelPath + File.separator + className + "Model.java" ;
        }
        if (template.contains("ListVO.java.vm")) {
            return modelPath + File.separator + className + "ListVO.java" ;
        }
        if (template.contains("Constant.java.vm")) {
            return modelPath + File.separator + className + "Constant.java" ;
        }
        return null;
    }

    /**
     * 生成代码
     *
     * @param context
     * @param template
     * @param fileNames
     */
    public static void velocityWriterFile(VelocityContext context, String template, String fileNames) {
        try {
            // 渲染模板
            @Cleanup StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF_8);
            tpl.merge(context, sw);
            if (fileNames != null) {
                File file = new File(XSSEscape.escapePath(fileNames));
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

    /**
     * 获取接口参数配置，templatejson
     *
     * @param allModels
     * @return
     */
    public static Map<String, Object> getInterTemplateJson(List<FormAllModel> allModels, Map<String, String> childTableKey) {
        Map<String, Object> map = new HashMap<>();
        for (FormAllModel item : allModels) {
            if (FormEnum.mast.getMessage().equals(item.getJnpfKey())) {
                FieLdsModel fieLdsModel = BeanUtil.copyProperties(item.getFormColumnModel().getFieLdsModel(), FieLdsModel.class);
                if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                    map.put(fieLdsModel.getVModel(), getTemJsonModel(fieLdsModel, childTableKey));
                }
            }
            if (FormEnum.mastTable.getMessage().equals(item.getJnpfKey())) {
                FieLdsModel fieLdsModel = BeanUtil.copyProperties(item.getFormMastTableModel().getMastTable().getFieLdsModel(), FieLdsModel.class);
                if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                    map.put(item.getFormMastTableModel().getVModel(), getTemJsonModel(fieLdsModel, childTableKey));
                }
            }
            if (FormEnum.table.getMessage().equals(item.getJnpfKey())) {
                List<FormColumnModel> childList = item.getChildList().getChildList();
                for (FormColumnModel columnModel : childList) {
                    FieLdsModel fieLdsModel = BeanUtil.copyProperties(columnModel.getFieLdsModel(), FieLdsModel.class);
                    if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                        map.put(item.getChildList().getAliasLowName() + fieLdsModel.getVModel(), getTemJsonModel(fieLdsModel, childTableKey));
                    }
                }
            }
        }
        return map;
    }

    private static List<TemplateJsonModel> getTemJsonModel(FieLdsModel fieLdsModel, Map<String, String> childTableKey) {
        List<TemplateJsonModel> templateJson = fieLdsModel.getConfig().getTemplateJson();
        List<TemplateJsonModel> json = templateJson.size() > 0 ? templateJson :
                JsonUtil.getJsonToList(fieLdsModel.getTemplateJson(), TemplateJsonModel.class);
        for (TemplateJsonModel t : json) {
            if (t.getRelationField() != null && t.getRelationField().contains("-")) {
                String[] split = t.getRelationField().split("-");
                t.setRelationField(childTableKey.get(split[0]) + "List-" + split[1]);
            }
        }
        return json;
    }

    /**
     * 获取非系统控件字段
     *
     * @return
     */
    public static List<String> getNotSystemFields(List<FormAllModel> mast, List<FormAllModel> mastTable, List<FormAllModel> childTable,
                                                  GenerateParamModel generateParamModel) {
        List<String> list = new ArrayList<>();
        String table = generateParamModel.getTable();
        boolean mainTable = generateParamModel.isMainTable();
        List<FormColumnModel> fields = new ArrayList<>();
        for (FormAllModel fam : mast) {
            if (mainTable) {
                fields.add(fam.getFormColumnModel());
            }
        }
        for (FormAllModel fam : mastTable) {
            if (table.equals(fam.getFormMastTableModel().getTable())) {
                fields.add(fam.getFormMastTableModel().getMastTable());
            }
        }
        for (FormAllModel fam : childTable) {
            if (table.equals(fam.getChildList().getTableName())) {
                fields.addAll(fam.getChildList().getChildList());
            }
        }
        for (FormColumnModel fcm : fields) {
            if (!JnpfKeyConsts.getSystemKey().contains(fcm.getFieLdsModel().getConfig().getJnpfKey())) {
                list.add(fcm.getFieLdsModel().getVModel().toUpperCase());
            }
        }
        return list;
    }


    /**
     * 移除对象内的json字符串
     *
     * @param str
     * @return
     */
    public static String objRemoveJson(String str) {
        JSONObject object = JSONObject.parseObject(str);

        JSONArray columnList = object.getJSONArray("columnList");
        removeJson(columnList);
        object.put("columnList" , columnList);

        JSONArray searchList = object.getJSONArray("searchList");
        removeJson(searchList);
        object.put("searchList" , searchList);

        JSONObject ruleList = object.getJSONObject("ruleList");
        ruleRemoveJson(ruleList);
        object.put("ruleList" , ruleList);

        JSONObject ruleListApp = object.getJSONObject("ruleListApp");
        ruleRemoveJson(ruleListApp);
        object.put("ruleListApp" , ruleListApp);

        JSONArray columnOptions = object.getJSONArray("columnOptions");
        removeJson(columnOptions);
        object.put("columnOptions" , columnOptions);

        JSONArray defaultColumnList = object.getJSONArray("defaultColumnList");
        removeJson(defaultColumnList);
        object.put("defaultColumnList" , defaultColumnList);

        JSONArray sortList = object.getJSONArray("sortList");
        removeJson(sortList);
        object.put("sortList" , sortList);

        JSONArray fields = object.getJSONArray("fields");
        removeJson(fields);
        object.put("fields" , fields);

        object.remove("funcs");
        return object.toJSONString();
    }

    private static void ruleRemoveJson(JSONObject ruleList) {
        if (ruleList != null) {
            JSONArray conditionList = ruleList.getJSONArray("conditionList");
            if (conditionList != null) {
                for (Object o : conditionList) {
                    JSONObject obj = (JSONObject) o;
                    JSONArray groups = obj.getJSONArray("groups");
                    removeJson(groups);
                    obj.put("groups" , groups);
                }
                ruleList.put("conditionList" , conditionList);
            }
        }
    }

    /**
     * 递归移除对应属性
     *
     * @param jsonArray
     */
    public static void removeJson(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String jnpfkey = jsonObject.getJSONObject("__config__").getString("jnpfKey");
            List<String> childrenListAll = new ArrayList() {{
                add(FormEnum.card.getMessage());
                add(FormEnum.row.getMessage());
                add(FormEnum.tab.getMessage());
                add(FormEnum.collapse.getMessage());
                add(FormEnum.collapseItem.getMessage());
                add(FormEnum.tabItem.getMessage());
                add(FormEnum.tableGrid.getMessage());
                add(FormEnum.tableGridTr.getMessage());
                add(FormEnum.tableGridTd.getMessage());
            }};
            if (childrenListAll.contains(jnpfkey) || StringUtil.isEmpty(jnpfkey)) {
                JSONObject config = jsonObject.getJSONObject("__config__");
                config.remove("on");
                JSONArray childArray = config.getJSONArray("children");
                removeJson(childArray);
                config.put("children" , childArray);
                jsonObject.put("__config__" , config);
            } else if (FormEnum.table.getMessage().equals(jnpfkey)) {
                JSONObject configA = jsonObject.getJSONObject("__config__");
                configA.remove("on");
                JSONArray children = configA.getJSONArray("children");
                for (int k = 0; k < children.size(); k++) {
                    JSONObject childrenObject = (JSONObject) children.get(k);
                    childrenObject.remove("on");
                    JSONObject config = childrenObject.getJSONObject("__config__");
                    config.remove("on");
                    if (!"static".equals(config.get("dataType"))) {
                        childrenObject.remove("options");
                        config.remove("options");
                    }
                    childrenObject.put("__config__" , config);
                }
                configA.put("children" , children);
                if (!"static".equals(configA.get("dataType"))) {
                    jsonObject.remove("options");
                    configA.remove("options");
                }
                jsonObject.put("__config__" , configA);
            }
            jsonObject.remove("on");
            JSONObject config = jsonObject.getJSONObject("__config__");
            config.remove("on");
            if (!"static".equals(config.get("dataType"))) {
                jsonObject.remove("options");
                config.remove("options");
            }
            jsonObject.put("__config__" , config);
        }
    }

}
