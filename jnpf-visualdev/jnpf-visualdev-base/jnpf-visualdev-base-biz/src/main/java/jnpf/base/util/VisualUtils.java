package jnpf.base.util;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.hutool.core.util.StrUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.ExportSelectedModel;
import jnpf.base.model.Template6.ColumnListField;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.database.model.dbtable.JdbcTableModel;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.model.interfaces.DbSourceOrDbLink;
import jnpf.database.util.ConnUtil;
import jnpf.exception.DataException;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.config.HeaderModel;
import jnpf.util.DateUtil;
import jnpf.util.ExcelUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.context.SpringContext;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 可视化工具类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021年3月13日16:37:40
 */
@Slf4j
public class VisualUtils {

    private static ConfigValueUtil configValueUtil = SpringContext.getBean(ConfigValueUtil.class);

    /**
     * 去除多级嵌套控件
     *
     * @return
     */
    public static List<FieLdsModel> deleteMoreVmodel(FieLdsModel model) {
        if ("".equals(model.getVModel()) && model.getConfig().getChildren() != null) {
            List<FieLdsModel> childModelList = JsonUtil.getJsonToList(model.getConfig().getChildren(), FieLdsModel.class);
            return childModelList;
        }
        return null;
    }

    public static List<FieLdsModel> deleteMore(List<FieLdsModel> modelList) {
        List<FieLdsModel> newModelList = new ArrayList<>();
        for (FieLdsModel model : modelList) {
            List<FieLdsModel> newList = deleteMoreVmodel(model);
            if (newList == null || JnpfKeyConsts.CHILD_TABLE.equals(model.getConfig().getJnpfKey())) {
                newModelList.add(model);
            } else {
                newModelList.addAll(deleteMore(newList));
            }
        }
        return newModelList;
    }

    /**
     * 返回主键名称
     *
     * @param dbSourceOrDbLink
     * @param mainTable
     * @return
     */
    public static String getpKey(DbSourceOrDbLink dbSourceOrDbLink, String mainTable) throws SQLException {
        String pKeyName = "f_id";
        //catalog 数据库名
        String tmpKey = JdbcTableModel.getPrimary(dbSourceOrDbLink, mainTable);
        if (StrUtil.isNotEmpty(tmpKey)) {
            pKeyName = tmpKey;
        }
        return pKeyName;
    }

    /**
     * 判断主键是否为自增长
     * @param dbSourceOrDbLink
     * @param table
     * @param key
     * @return
     * @throws SQLException
     */
    public static String primaryKeyTypeIsAuto(DbSourceOrDbLink dbSourceOrDbLink, String table, String key) throws SQLException {
        Connection conn = ConnUtil.getConnOrDefault(dbSourceOrDbLink);
        ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(), (String) null, table, key);

        String var3;
        try {
            if (!rs.next()) {
                var3 = "";
                return var3;
            }

            var3 = rs.getString("IS_AUTOINCREMENT");
        } finally {
            if (Collections.singletonList(rs).get(0) != null) {
                rs.close();
            }

        }

        return var3;
    }

    /**
     * 在线开发多数据源连接
     *
     * @return
     */
    public static Connection getDataConn(DbLinkEntity linkEntity) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnUtil.getConnOrDefault(linkEntity);
        } catch (DataException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 导出在线开发的表格
     *
     * @param visualdevEntity
     * @param list
     * @param keys
     * @param sheetName
     * @return
     */
    public static DownloadVO createModelExcel(VisualdevEntity visualdevEntity, List<Map<String, Object>> list, Collection<String> keys, String sheetName,String preName) {
        //判断sheetName
        boolean SheetTitleWithField = !sheetName.equals("表单信息");
        DownloadVO vo = DownloadVO.builder().build();
        try {
            //去除空数据
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Map<String, Object> map : list) {
                int i = 0;
                for (String key : keys) {
                    //子表
                    if (key.toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                        String tableField = key.substring(0, key.indexOf("-"));
                        String field = key.substring(key.indexOf("-") + 1);
                        Object o = map.get(tableField);
                        if (o != null) {
                            List<Map<String, Object>> childList = (List<Map<String, Object>>) o;
                            for (Map<String, Object> childMap : childList) {
                                if (childMap.get(field) != null) {
                                    i++;
                                }
                            }
                        }
                    } else {
                        Object o = map.get(key);
                        if (o != null) {
                            i++;
                        }
                    }
                }
                if (i > 0) {
                    dataList.add(map);
                }
            }

            FormDataModel formDataModel = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
            List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
            //递归
            List<FieLdsModel> allFields = new ArrayList<>();
            recursionFields(fieLdsModelList, allFields);

            Map<String, String> mainMap = new HashMap<>();
            allFields.stream().filter(a -> !a.getConfig().getJnpfKey().equals(JnpfKeyConsts.CHILD_TABLE)).forEach(m -> mainMap.put(m.getVModel(), m.getConfig().getLabel()));
            List<FieLdsModel> childFields = allFields.stream().filter(a -> a.getConfig().getJnpfKey().equals(JnpfKeyConsts.CHILD_TABLE)).collect(Collectors.toList());
            //创建导出属性对象
            List<ExportSelectedModel> child = new ArrayList<>();
            List<ExportSelectedModel> allExportModelList = new ArrayList<>();
            for (String key : keys) {
                ExportSelectedModel exportSelectedModel = new ExportSelectedModel();
                if (key.toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                    String tableField = key.substring(0, key.indexOf("-"));
                    String field = key.substring(key.indexOf("-") + 1);
                    exportSelectedModel.setTableField(tableField);
                    exportSelectedModel.setField(field);
                    child.add(exportSelectedModel);
                } else {
                    exportSelectedModel.setField(key);
                    exportSelectedModel.setLabel(mainMap.get(key));
                    allExportModelList.add(exportSelectedModel);
                }
            }
            Map<String, List<ExportSelectedModel>> childGroups = child.stream()
                    .collect(Collectors.groupingBy(ExportSelectedModel::getTableField, LinkedHashMap::new, Collectors.toList()));
            List<String> keyForIndex = new ArrayList<>();
            for (String key : keys) {
                keyForIndex.add(key);
            }
            for (Map.Entry<String, List<ExportSelectedModel>> entry : childGroups.entrySet()) {
                String key = entry.getKey();
                List<String> collect = keyForIndex.stream().filter(k -> k.startsWith(key)).collect(Collectors.toList());
                String s = keyForIndex.stream().filter(keyF -> keyF.startsWith(key)).findFirst().orElse("");
                int i = keyForIndex.indexOf(s);
                keyForIndex.removeAll(collect);
                List<ExportSelectedModel> value = entry.getValue();
                FieLdsModel fieLdsModel = childFields.stream().filter(c -> c.getVModel().equals(key)).findFirst().orElse(null);
                Map<String, String> childMap = new HashMap<>(16);
                fieLdsModel.getConfig().getChildren().stream().forEach(c -> childMap.put(c.getVModel(), c.getConfig().getLabel()));
                value.stream().forEach(v ->
                        v.setLabel(childMap.get(v.getField()))
                );
                ExportSelectedModel exportSelectedModel = new ExportSelectedModel();
                exportSelectedModel.setTableField(key);
                exportSelectedModel.setSelectedModelList(value);
                exportSelectedModel.setLabel(fieLdsModel.getConfig().getLabel());
                allExportModelList.add(i, exportSelectedModel);
            }

            List<ExcelExportEntity> entitys = new ArrayList<>();
            for (ExportSelectedModel selectModel : allExportModelList) {
                ExcelExportEntity exportEntity;
                if (StringUtil.isNotEmpty(selectModel.getTableField())) {
                    exportEntity = new ExcelExportEntity(selectModel.getLabel() + "(" + selectModel.getTableField() + ")", selectModel.getTableField());
                    //+"("+selectModel.getTableField()+"-"+m.getField()+")"
                    exportEntity.setList(selectModel.getSelectedModelList().stream().map(m -> new ExcelExportEntity(m.getLabel() + (SheetTitleWithField ? "(" + selectModel.getTableField() + "-" + m.getField() + ")" : "")
                            , m.getField())).collect(Collectors.toList()));
                } else {
                    // +"("+selectModel.getField()+")"
                    exportEntity = new ExcelExportEntity(selectModel.getLabel() + (SheetTitleWithField ? "(" + selectModel.getField() + ")" : ""), selectModel.getField());
                }
                entitys.add(exportEntity);
            }

            if (sheetName.equals("错误报告")) {
                entitys.add(new ExcelExportEntity("异常原因", "errorsInfo"));
            }

            //原数据和表头用于合并处理
            List<ExcelExportEntity> mergerEntitys = new ArrayList<>(entitys);
            List<Map<String, Object>> mergerList=new ArrayList<>(list);

            //复杂表头-表头和数据处理
            ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
            if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
                //数据导出判断是否是行内
                boolean isLineEidtExport= SheetTitleWithField ? Objects.equals(columnDataModel.getType(), 4) :false;
                entitys = complexHeaderHandel(entitys, complexHeaderList, isLineEidtExport);
                dataList = complexHeaderDataHandel(dataList, complexHeaderList, isLineEidtExport);
            }

            ExportParams exportParams = new ExportParams(null, sheetName);
            @Cleanup Workbook workbook = new HSSFWorkbook();
            if (entitys.size() > 0) {
                if (dataList.size() == 0) {
                    dataList.add(new HashMap<>());
                }
                workbook = ExcelExportUtil.exportExcel(exportParams, entitys, dataList);

                mergerVertical(workbook, mergerEntitys, mergerList);
            }

            String fileName = preName + "_" + DateUtil.dateNow("yyyyMMddHHmmss") + ".xls";
            MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, fileName);
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, temporaryFilePath, fileName);
            vo.setName(fileInfo.getFilename());
            vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + "Temporary") + "&name=" + fileName);
        } catch (Exception e) {
            log.error("信息导出Excel错误:{}", e.getMessage());
            e.printStackTrace();
        }
        return vo;
    }

    public static void recursionFields(List<FieLdsModel> fieLdsModelList, List<FieLdsModel> allFields) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            if (JnpfKeyConsts.CHILD_TABLE.equals(fieLdsModel.getConfig().getJnpfKey())) {
                allFields.add(fieLdsModel);
            } else {
                if (fieLdsModel.getConfig().getChildren() != null) {
                    recursionFields(fieLdsModel.getConfig().getChildren(), allFields);
                } else {
                    if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                        allFields.add(fieLdsModel);
                    }
                }
            }
        }
    }

    /**
     * 视图导出
     *
     * @param visualdevEntity
     * @param list
     * @param keys
     * @param sheetName
     * @return
     */
    public static DownloadVO createModelExcelServiceData(VisualdevEntity visualdevEntity, List<Map<String, Object>> list, Collection<String> keys, String sheetName,String preName) {

        //判断sheetName
        DownloadVO vo = DownloadVO.builder().build();
        try {
            ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            List<ColumnListField> columnListAll = JsonUtil.getJsonToList(columnDataModel.getColumnList(), ColumnListField.class);
            List<ExcelExportEntity> entitys = new ArrayList<>();

            for (ColumnListField selectModel : columnListAll) {
                if (keys.contains(selectModel.getProp())) {
                    ExcelExportEntity exportEntity = new ExcelExportEntity(selectModel.getLabel());
                    exportEntity.setKey(selectModel.getProp());
                    exportEntity.setName(selectModel.getLabel());
                    entitys.add(exportEntity);
                }
            }

            if (sheetName.equals("错误报告")) {
                entitys.add(new ExcelExportEntity("异常原因", "errorsInfo"));
            }

            //原数据和表头用于合并处理
            List<ExcelExportEntity> mergerEntitys = new ArrayList<>(entitys);
            List<Map<String, Object>> mergerList=new ArrayList<>(list);

            //复杂表头-表头和数据处理
            List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
            if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
                entitys = complexHeaderHandel(entitys, complexHeaderList, false);
                list = complexHeaderDataHandel(list, complexHeaderList, false);
            }

            ExportParams exportParams = new ExportParams(null, sheetName);
            @Cleanup Workbook workbook = new HSSFWorkbook();
            if (entitys.size() > 0) {
                if (list.size() == 0) {
                    list.add(new HashMap<>());
                }
                workbook = ExcelExportUtil.exportExcel(exportParams, entitys, list);

                mergerVertical(workbook, mergerEntitys, mergerList);
            }
            String fileName = preName + DateUtil.dateNow("yyyyMMddHHmmss") + ".xls";
            MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, fileName);
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, temporaryFilePath, fileName);
            vo.setName(fileInfo.getFilename());
            vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + "Temporary") + "&name=" + fileName);
        } catch (Exception e) {
            log.error("信息导出Excel错误:{}", e.getMessage());
            e.printStackTrace();
        }
        return vo;
    }

    /**
     * @param mapList
     * @return List<Map < String, Object>>
     * @Date 21:51 2020/11/11
     * @Description 将map中的所有key转化为小写
     */
    public static List<Map<String, Object>> toLowerKeyList(List<Map<String, Object>> mapList) {
        List<Map<String, Object>> newMapList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            Map<String, Object> resultMap = new HashMap(16);
            Set<String> sets = map.keySet();
            for (String key : sets) {
                resultMap.put(key.toLowerCase(), map.get(key));
            }
            newMapList.add(resultMap);
        }
        return newMapList;
    }

    /**
     * @param map
     * @return
     * @Description 删除模板字段下划线
     */
    public static Map<String, Object> toLowerKey(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Set<String> sets = map.keySet();
        for (String key : sets) {
            resultMap.put(key.toLowerCase(), map.get(key));
        }
        return resultMap;
    }

    //    /**
//     * 审批流提交
//     * @param visualdevEntity
//     * @param flowTaskId
//     * @param formdata
//     * @param userInfo
//     */
//    public static void submitFlowTask(VisualdevEntity visualdevEntity, String flowTaskId, Object formdata, UserInfo userInfo, FlowModel flowModel) throws WorkFlowException {
//        //审批流
//        if (visualdevEntity.getWebType().equals(VisualWebTypeEnum.FLOW_FROM.getType() )){
//            try {
//                FlowEngineService flowEngineService = SpringContext.getBean(FlowEngineService.class);
//                FlowEngineEntity flowEngineEntity = flowEngineService.getInfo(visualdevEntity.getFlowId());
//                FlowTaskService flowTaskService = SpringContext.getBean(FlowTaskService.class);
//                FlowTaskNewService flowTaskNewService = SpringContext.getBean(FlowTaskNewService.class);
//                FlowTaskEntity flowTaskEntity = flowTaskService.getInfoSubmit(flowTaskId, FlowTaskEntity::getId);
//                String id = null;
//                if (flowTaskEntity != null) {
//                    id = flowTaskEntity.getId();
//                }
//                String flowTitle = userInfo.getUserName() +"的"+ visualdevEntity.getFullName();
//                String billNo ="#Visual"+ DateUtil.getNow();
//                flowModel.setId(id);
//                flowModel.setFlowId(flowEngineEntity.getId());
//                flowModel.setProcessId(flowTaskId);
//                flowModel.setFlowTitle(flowTitle);
//                flowModel.setBillNo(billNo);
//                Map<String, Object> data = JsonUtil.entityToMap(formdata);
//                flowModel.setFormData(data);
//                flowModel.setUserInfo(userInfo);
//                flowTaskNewService.submit(flowModel);
//            } catch (WorkFlowException e) {
//                throw new WorkFlowException(e.getCode(),e.getMessage());
//            }
//        }
//    }
    public static String exampleExcelMessage(FieLdsModel model) {
        String message = "";
        String jnpfKey = model.getConfig().getJnpfKey();
        switch (jnpfKey) {
            case JnpfKeyConsts.CREATEUSER:
            case JnpfKeyConsts.MODIFYUSER:
            case JnpfKeyConsts.CREATETIME:
            case JnpfKeyConsts.MODIFYTIME:
            case JnpfKeyConsts.CURRORGANIZE:
            case JnpfKeyConsts.CURRPOSITION:
            case JnpfKeyConsts.CURRDEPT:
            case JnpfKeyConsts.BILLRULE:
                message = "系统自动生成";
                break;
            case JnpfKeyConsts.COMSELECT:
                message = model.getMultiple() ? "例:引迈信息/产品部,引迈信息/技术部" : "例:引迈信息/技术部";
                break;
            case JnpfKeyConsts.DEPSELECT:
                message = model.getMultiple() ? "例:产品部/部门编码,技术部/部门编码" : "例:技术部/部门编码";
                break;
            case JnpfKeyConsts.POSSELECT:
                message = model.getMultiple() ? "例:技术经理/岗位编码,技术员/岗位编码" : "例:技术员/岗位编码";
                break;
            case JnpfKeyConsts.USERSELECT:
                message = model.getMultiple() ? "例:张三/账号,李四/账号" : "例:张三/账号";
                break;
            case JnpfKeyConsts.CUSTOMUSERSELECT:
                message = model.getMultiple() ? "例:方方/账号,技术部/部门编码" : "例:方方/账号";
                break;
            case JnpfKeyConsts.ROLESELECT:
                message = model.getMultiple() ? "例:研发人员/角色编码,测试人员/角色编码" : "例:研发人员/角色编码";
                break;
            case JnpfKeyConsts.GROUPSELECT:
                message = model.getMultiple() ? "例:A分组/分组编码,B分组/分组编码" : "例:A分组/分组编码";
                break;
            case JnpfKeyConsts.DATE:
                message = String.format("例: %s", model.getFormat());
                break;
            case JnpfKeyConsts.TIME:
//                message = "例: HH:mm:ss";
                message = String.format("例: %s", model.getFormat());
                break;
            case JnpfKeyConsts.ADDRESS:
                switch (model.getLevel()) {
                    case 0:
                        message = model.getMultiple() ? "例:福建省,广东省" : "例:福建省";
                        break;
                    case 1:
                        message = model.getMultiple() ? "例:福建省/莆田市,广东省/广州市" : "例:福建省/莆田市";
                        break;
                    case 2:
                        message = model.getMultiple() ? "例:福建省/莆田市/城厢区,广东省/广州市/荔湾区" : "例:福建省/莆田市/城厢区";
                        break;
                    case 3:
                        message = model.getMultiple() ? "例:福建省/莆田市/城厢区/霞林街道,广东省/广州市/荔湾区/沙面街道" : "例:福建省/莆田市/城厢区/霞林街道";
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return message;
    }

    /**
     * 复杂表头表头处理--代码生成
     *
     * @param dataList
     * @param complexHeaderList
     * @return
     */
    public static List<ExcelExportEntity> complexHeaderHandel(List<ExcelExportEntity> dataList, List<HeaderModel> complexHeaderList, boolean isLineEidtExport) {
        List<String> complexHeaderListStr = new ArrayList<>();
        complexHeaderList.forEach(item -> complexHeaderListStr.addAll(item.getChildColumns()));
        Map<String, Integer> complexMap1 = new HashMap<>();
        List<ExcelExportEntity> dataListRes = new ArrayList<>();
        int n = 0;//记录新数组下标用的，(dataListRes.add的地方就要n++)
        for (HeaderModel item : complexHeaderList) {
            complexMap1.put(item.getId(), n);
            ExcelExportEntity export = new ExcelExportEntity(item.getFullName() + "(" + item.getId() + ")", item.getId());
            List<ExcelExportEntity> list = new ArrayList<>();
            export.setList(list);
            dataListRes.add(export);
            n++;
        }
        for (ExcelExportEntity entity : dataList) {
            String entityKey = isLineEidtExport ? entity.getKey().toString().split("_name")[0] : entity.getKey().toString();
            if (complexHeaderListStr.contains(entityKey)) {
                for (HeaderModel item : complexHeaderList) {
                    if (item.getChildColumns().contains(entityKey)) {
                        ExcelExportEntity export = dataListRes.get(complexMap1.get(item.getId()));
                        List<ExcelExportEntity> list = export.getList() != null ? export.getList() : new ArrayList<>();
                        list.add(entity);
                        export.setList(list);
                        dataListRes.set(complexMap1.get(item.getId()), export);
                        continue;
                    }
                }
            } else {
                dataListRes.add(entity);
                n++;
            }
        }
        return dataListRes;
    }

    /**
     * 复杂表头数据处理
     *
     * @param dataListRes
     * @param complexHeaderList
     * @return
     */
    public static List<Map<String, Object>> complexHeaderDataHandel(List<Map<String, Object>> dataListRes, List<HeaderModel> complexHeaderList) {
        List<String> complexHeaderListStr = new ArrayList<>();
        complexHeaderList.forEach(item -> complexHeaderListStr.addAll(item.getChildColumns()));
        List<String> complexMap1 = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>(dataListRes);
        for (Map<String, Object> map : dataList) {
            Set<String> keyset = new HashSet<>(map.keySet());
            for (String key : keyset) {
                if (complexHeaderListStr.contains(key)) {
                    for (HeaderModel item : complexHeaderList) {
                        if (item.getChildColumns().contains(key)) {
                            if (complexMap1.contains(item.getId())) {
                                List<Object> list1 = (List<Object>) map.get(item.getId());
                                Map<String, Object> obj = list1 != null && list1.get(0) != null ? (Map<String, Object>) list1.get(0) : new HashMap<>();
                                obj.put(key, map.get(key));
                                map.put(item.getId(), new ArrayList() {{
                                    add(obj);
                                }});
                            } else {
                                complexMap1.add(item.getId());
                                Map<String, Object> obj = new HashMap<>();
                                obj.put(key, map.get(key));
                                map.put(item.getId(), new ArrayList() {{
                                    add(obj);
                                }});
                            }
                            continue;
                        }
                    }
                }
            }
        }
        return dataList;
    }

    /**
     * 复杂表头数据处理
     *
     * @param dataListRes
     * @param complexHeaderList
     * @return
     */
    public static List<Map<String, Object>> complexHeaderDataHandel(List<Map<String, Object>> dataListRes, List<HeaderModel> complexHeaderList, boolean isLineEidtExport) {
        List<String> complexHeaderListStr = new ArrayList<>();
        complexHeaderList.forEach(item -> complexHeaderListStr.addAll(item.getChildColumns()));
        List<String> complexMap1 = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>(dataListRes);
        for (Map<String, Object> map : dataList) {
            Set<String> keyset = new HashSet<>(map.keySet());
            for (String key : keyset) {
                String keyName = isLineEidtExport ? key.split("_name")[0] : key;
                if (complexHeaderListStr.contains(keyName)) {
                    for (HeaderModel item : complexHeaderList) {
                        if (item.getChildColumns().contains(keyName)) {
                            if (complexMap1.contains(item.getId())) {
                                List<Object> list1 = (List<Object>) map.get(item.getId());
                                Map<String, Object> obj = list1 != null && list1.get(0) != null ? (Map<String, Object>) list1.get(0) : new HashMap<>();
                                obj.put(key, map.get(key));
                                map.put(item.getId(), new ArrayList() {{
                                    add(obj);
                                }});
                            } else {
                                complexMap1.add(item.getId());
                                Map<String, Object> obj = new HashMap<>();
                                obj.put(key, map.get(key));
                                map.put(item.getId(), new ArrayList() {{
                                    add(obj);
                                }});
                            }
                            continue;
                        }
                    }
                }
            }
        }
        return dataList;
    }

    /**
     * 复杂表头表头处理--在线开发
     *
     * @param dataList
     * @param complexHeaderList
     * @return
     */
    public static List<Map<String, Object>> complexHeaderHandelOnline(List<Map<String, Object>> dataList, List<HeaderModel> complexHeaderList) {
        List<String> complexHeaderListStr = new ArrayList<>();
        complexHeaderList.forEach(item -> complexHeaderListStr.addAll(item.getChildColumns()));
        List<Object> uploadColumn = dataList.stream().map(t -> t.get("id")).collect(Collectors.toList());
        Map<String, Integer> complexMap1 = new HashMap<>();
        List<Map<String, Object>> dataListRes = new ArrayList<>();
        int n = 0;//记录新数组下标用的，(dataListRes.add的地方就要n++)
        for (HeaderModel item : complexHeaderList) {
            if (item.getChildColumns().size() > 0) {
                List<String> complexHasColumn = item.getChildColumns().stream().filter(t -> uploadColumn.contains(t)).collect(Collectors.toList());
                //判断复杂表头的字段是否有可导入字段--没有的话不生成复杂表头
                if (complexHasColumn.size() > 0) {
                    complexMap1.put(item.getId(), n);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", item.getId());
                    map.put("fullName", item.getFullName());
                    map.put("jnpfKey", "complexHeader");
                    dataListRes.add(map);
                    n++;
                }
            }
        }
        for (Map<String, Object> entity : dataList) {
            if (complexHeaderListStr.contains(entity.get("id"))) {
                for (HeaderModel item : complexHeaderList) {
                    if (item.getChildColumns().contains(entity.get("id"))) {
                        Map<String, Object> map = dataListRes.get(complexMap1.get(item.getId()));
                        List<Map<String, Object>> listmap = new ArrayList<>();
                        if (map.get("children") == null) {
                            listmap.add(entity);
                        } else {
                            listmap = (List<Map<String, Object>>) map.get("children");
                            listmap.add(entity);
                        }
                        map.put("children", listmap);
                        dataListRes.set(complexMap1.get(item.getId()), map);
                        continue;
                    }
                }
            } else {
                dataListRes.add(entity);
            }
        }
        return dataListRes;
    }

    /**
     * 复杂表头数据导入处理--在线开发
     *
     * @param dataList
     * @param entity
     * @return
     */
    public static List<Map<String, Object>> complexImportsDataOnline(List<Map<String, Object>> dataList, VisualdevEntity entity) {
        List<Map<String, Object>> listRes = new ArrayList<>();
        //复杂表头-表头和数据处理
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class);
        List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
        if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
            for (Map<String, Object> mapone : dataList) {
                for (HeaderModel item : complexHeaderList) {
                    Object remove = mapone.remove(item.getId());
                    if (remove != null) {
                        List<Map<String, Object>> listC = (List<Map<String, Object>>) remove;
                        if (listC.size() > 0) {
                            mapone.putAll(listC.get(0));
                        }
                    }
                }
                listRes.add(mapone);
            }
        } else {
            listRes = dataList;
        }
        return listRes;
    }

    /**
     * 单元格垂直合并
     * @param workbook
     * @param entityList
     * @param dataList
     */
    public static void mergerVertical(Workbook workbook, List<ExcelExportEntity> entityList, List<Map<String, Object>> dataList) {
        Sheet sheet = workbook.getSheet("表单信息");
        //当前行
        int firstRow = 0;
        int lastRow = 0;
        for (Map<String, Object> obj : dataList) {
            //取出子表最大数量
            int size = 1;
            //判断有无子表
            List<ExcelExportEntity> hasChildList = entityList.stream().filter(t ->
                    t.getKey().toString().toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList());
            if(hasChildList.size() > 0){
                for (ExcelExportEntity item : hasChildList) {
                    String key = String.valueOf(item.getKey());
                    if (obj.get(key) instanceof List) {
                        List arr = (List) obj.get(key);
                        if (arr.size() > size) {
                            size = arr.size();
                        }
                    }
                }
            }
            //标题行数量
            int headSize = 1;
            List<ExcelExportEntity> collect = entityList.stream().filter(t -> t.getList() != null).collect(Collectors.toList());
            if (collect.size() >= 1) {
                headSize = 2;
            }

            if(size == 0) {
                firstRow = lastRow == 0 ?  headSize : lastRow + 1;
                lastRow = firstRow;
                continue;
            }else{
                firstRow = lastRow == 0 ?  headSize : lastRow + 1;
                lastRow = firstRow + size - 1;
            }

            for (int n = 0; n < entityList.size(); n++) {
                ExcelExportEntity export = entityList.get(n);
                if (export.getList() == null && firstRow != lastRow) {
                    sheet.addMergedRegionUnsafe(new CellRangeAddress(firstRow , lastRow, n, n));
                }
            }
        }
    }
}
