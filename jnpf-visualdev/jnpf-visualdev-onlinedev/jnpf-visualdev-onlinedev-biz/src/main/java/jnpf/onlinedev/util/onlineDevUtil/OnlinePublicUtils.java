package jnpf.onlinedev.util.onlineDevUtil;


import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.model.VisualWebTypeEnum;
import jnpf.exception.WorkFlowException;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.FormEnum;
import jnpf.model.visualJson.analysis.FormModel;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.onlinedev.model.OnlineDevEnum.MultipleControlEnum;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.XSSEscape;
import jnpf.util.visiual.JnpfKeyConsts;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 在线开发公用
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/7/28
 */
public class OnlinePublicUtils {
    /**
     * 判断有表无表
     *
     * @return
     */
    public static Boolean isUseTables(String tableJson) {
        if (!StringUtil.isEmpty(tableJson) && !OnlineDevData.TABLE_CONST.equals(tableJson)) {
            return true;
        }
        return false;
    }

    /**
     * map key转小写
     *
     * @param requestMap
     * @return
     */
    public static Map<String, Object> mapKeyToLower(Map<String, ?> requestMap) {
        // 非空校验
        if (requestMap.isEmpty()) {
            return null;
        }
        // 初始化放转换后数据的Map
        Map<String, Object> responseMap = new HashMap<>(16);
        // 使用迭代器进行循环遍历
        Set<String> requestSet = requestMap.keySet();
        Iterator<String> iterator = requestSet.iterator();
        iterator.forEachRemaining(obj -> {
            // 判断Key对应的Value是否为Map
            if ((requestMap.get(obj) instanceof Map)) {
                // 递归调用，将value中的Map的key转小写
                responseMap.put(obj.toLowerCase(), mapKeyToLower((Map) requestMap.get(obj)));
            } else {
                // 直接将key小写放入responseMap
                responseMap.put(obj.toLowerCase(), requestMap.get(obj));
            }
        });

        return responseMap;
    }


    /**
     * 获取map中第一个数据值
     *
     * @param map 数据源
     * @return
     */
    public static Object getFirstOrNull(Map<String, Object> map) {
        Object obj = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

    /**
     * 去除列表里无用的控件
     *
     * @param fieldsModelList
     * @return
     */
    public static void removeUseless(List<FieLdsModel> fieldsModelList) {
        for (int i = 0; i < fieldsModelList.size(); i++) {
            if (fieldsModelList.get(i).getConfig().getJnpfKey() == null) {
                continue;
            }
            if (fieldsModelList.get(i).getConfig().getJnpfKey().equals(JnpfKeyConsts.CHILD_TABLE)) {
                continue;
            }
        }
    }


    /**
     * 递归控件
     *
     * @param allFields
     * @param fieLdsModelList
     * @return
     */
    public static void recursionFields(List<FieLdsModel> allFields, List<FieLdsModel> fieLdsModelList) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            ConfigModel config = fieLdsModel.getConfig();
            String jnpfKey = config.getJnpfKey();
            if (JnpfKeyConsts.CHILD_TABLE.equals(jnpfKey)) {
                allFields.add(fieLdsModel);
                continue;
            } else {
                if (config.getChildren() != null) {
                    recursionFields(allFields, config.getChildren());
                } else {
                    if (jnpfKey == null) {
                        continue;
                    }
                    allFields.add(fieLdsModel);
                }
            }
        }
    }

    /**
     * 判断字符串是否有某个字符存在
     *
     * @param var1 完整字符串
     * @param var2 统计字符
     * @return
     */
    public static Boolean getMultiple(String var1, String var2) {
        if (var1.startsWith(var2)) {
            return true;
        }
        return false;
    }

    /**
     * 数据字典处理（从缓存中取出）
     *
     * @param dataList
     * @param swapModel
     * @return
     */
    public static Map<String, Object> getDataMap(List<Map<String, Object>> dataList, FieLdsModel swapModel) {
        String label = swapModel.getProps() != null ? swapModel.getProps().getLabel() : "" ;
        String value = swapModel.getProps() != null ? swapModel.getProps().getValue() : "" ;
        Map<String, Object> dataInterfaceMap = new HashMap<>();
        dataList.stream().forEach(data -> {
            dataInterfaceMap.put(String.valueOf(data.get(value)), String.valueOf(data.get(label)));
        });
        return dataInterfaceMap;
    }


    /**
     * 获取时间(+8)
     *
     * @param date
     * @param format
     * @return
     */
    public static String getDateByFormat(Long date, String format) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern(format);
        String dateString = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("+8" )));
        return dateString;
    }

    /**
     * 递归表单控件
     *
     * @param modelList   所有控件
     * @param mainFields  主表
     * @param childFields 子表
     * @param models      二维码 条形码
     */
    public static void recurseFiled(List<FieLdsModel> modelList, List<FieLdsModel> mainFields, List<FieLdsModel> childFields, List<FormModel> models) {
        for (FieLdsModel fieLdsModel : modelList) {
            ConfigModel config = fieLdsModel.getConfig();
            String jnpfkey = config.getJnpfKey();
            List<FieLdsModel> childrenList = config.getChildren();
            boolean isJnpfKey = StringUtil.isEmpty(jnpfkey);
            List<String> keyList = new ArrayList() {
                {
                    this.add(FormEnum.collapseItem.getMessage());
                    this.add(FormEnum.collapseItem.getMessage());
                    this.add(FormEnum.row.getMessage());
                    this.add(FormEnum.card.getMessage());
                    this.add(FormEnum.tab.getMessage());
                    this.add(FormEnum.collapse.getMessage());
                    this.add(FormEnum.tableGrid.getMessage());
                    this.add(FormEnum.tableGridTr.getMessage());
                    this.add(FormEnum.tableGridTd.getMessage());
                }
            };
            if (keyList.contains(jnpfkey) || isJnpfKey) {
                if (childrenList.size() > 0) {
                    recurseFiled(childrenList, mainFields, childFields, models);
                } else {
                    mainFields.add(fieLdsModel);
                }
            } else if (FormEnum.table.getMessage().equals(jnpfkey)) {
                childFields.add(fieLdsModel);
            } else if (FormEnum.groupTitle.getMessage().equals(jnpfkey) || FormEnum.divider.getMessage().equals(jnpfkey) || FormEnum.JNPFText.getMessage().equals(jnpfkey)) {

            } else if (FormEnum.QR_CODE.getMessage().equals(jnpfkey) || FormEnum.BARCODE.getMessage().equals(jnpfkey)) {
                FormModel formModel = JsonUtil.getJsonToBean(fieLdsModel, FormModel.class);
                models.add(formModel);
            } else {
                mainFields.add(fieLdsModel);
            }
        }
    }

    /**
     * 递归控件
     *
     * @return
     */
    public static void recursionFormFields(List<FieLdsModel> allFields, List<FieLdsModel> fieLdsModelList) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            ConfigModel config = fieLdsModel.getConfig();
            String jnpfKey = config.getJnpfKey();
            if (JnpfKeyConsts.CHILD_TABLE.equals(jnpfKey)) {
                allFields.add(fieLdsModel);
                continue;
            } else {
                if (config.getChildren() != null) {
                    recursionFormFields(allFields, config.getChildren());
                } else {
                    allFields.add(fieLdsModel);
                }
            }
        }
    }

    /**
     * 递归控件(取出所有子集)
     *
     * @return
     */
    public static void recursionFormChildFields(List<FieLdsModel> allFields, List<FieLdsModel> fieLdsModelList) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            ConfigModel config = fieLdsModel.getConfig();
            String jnpfKey = config.getJnpfKey();
            if (JnpfKeyConsts.CHILD_TABLE.equals(jnpfKey)) {
                String childVmodel = fieLdsModel.getVModel();
                for (FieLdsModel child : Optional.ofNullable(fieLdsModel.getConfig().getChildren()).orElse(new ArrayList<>())) {
                    if (child.getVModel() != null) {
                        child.setVModel(childVmodel + "-" + child.getVModel());
                        allFields.add(child);
                    }
                }
            } else {
                if (config.getChildren() != null) {
                    recursionFormChildFields(allFields, config.getChildren());
                } else {
                    if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
                        allFields.add(fieLdsModel);
                    }
                }
            }
        }
    }

    public static void recurseOnlineFiled(List<FieLdsModel> modelList, List<FieLdsModel> mainFields, List<FieLdsModel> childFields) {
        for (FieLdsModel fieLdsModel : modelList) {
            ConfigModel config = fieLdsModel.getConfig();
            String jnpfkey = config.getJnpfKey();
            List<FieLdsModel> childrenList = config.getChildren();
            boolean isJnpfKey = StringUtil.isEmpty(jnpfkey);
            if (FormEnum.row.getMessage().equals(jnpfkey) || FormEnum.card.getMessage().equals(jnpfkey)
                    || FormEnum.tab.getMessage().equals(jnpfkey) || FormEnum.collapse.getMessage().equals(jnpfkey)
                    || isJnpfKey) {
                if (childrenList.size() > 0) {
                    recurseOnlineFiled(childrenList, mainFields, childFields);
                } else {
                    mainFields.add(fieLdsModel);
                }
            } else if (FormEnum.table.getMessage().equals(jnpfkey)) {
                childFields.add(fieLdsModel);
            } else if (FormEnum.groupTitle.getMessage().equals(jnpfkey) || FormEnum.divider.getMessage().equals(jnpfkey) || FormEnum.JNPFText.getMessage().equals(jnpfkey)) {

            } else {
                mainFields.add(fieLdsModel);
            }
        }
    }

    /**
     * @param redisMap   缓存集合
     * @param modelData  数据
     * @param isMultiple 是否多选
     * @return
     */
    public static String getDataInMethod(Map<String, Object> redisMap, Object modelData, Boolean isMultiple) {
        if (redisMap == null || redisMap.isEmpty()) {
            return modelData.toString();
        }
        String Separator = isMultiple ? ";" : "/";
        String s2;
        if (OnlinePublicUtils.getMultiple(String.valueOf(modelData), MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
            String[][] data = JsonUtil.getJsonToBean(String.valueOf(modelData), String[][].class);
            List<String> addList = new ArrayList<>();
            for (String[] AddressData : data) {
                List<String> adList = new ArrayList<>();
                for (String s : AddressData) {
                    adList.add(String.valueOf(redisMap.get(s)));
                }
                addList.add(String.join("/", adList));
            }
            s2 = String.join(";", addList);
        } else if (OnlinePublicUtils.getMultiple(String.valueOf(modelData), MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
            List<String> modelDataList = JsonUtil.getJsonToList(String.valueOf(modelData), String.class);
            modelDataList = modelDataList.stream().map(s -> String.valueOf(redisMap.get(s))).collect(Collectors.toList());
            s2 = String.join(Separator, modelDataList);
        } else {
            String[] modelDatas = String.valueOf(modelData).split(",");
            StringBuilder dynamicData = new StringBuilder();
            for (int i = 0; i < modelDatas.length; i++) {
                modelDatas[i] = String.valueOf(Objects.nonNull(redisMap.get(modelDatas[i])) ? redisMap.get(modelDatas[i]) : "");
                dynamicData.append(modelDatas[i] + Separator);
            }
            s2 = dynamicData.deleteCharAt(dynamicData.length() - 1).toString();
        }
        return StringUtil.isEmpty(s2) ? modelData.toString() : s2;
    }

    public static List<String> getDataNoSwapInMethod(Object modelData) {
        List<String> dataValueList = new ArrayList<>();
        if (OnlinePublicUtils.getMultiple(String.valueOf(modelData), MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
            List<String> modelDataList = JsonUtil.getJsonToList(String.valueOf(modelData), String.class);
            dataValueList = modelDataList;
        } else {
            String[] modelDatas = String.valueOf(modelData).split(",");
            for (int i = 0; i < modelDatas.length; i++) {
                dataValueList.add(modelDatas[i]);
            }
        }
        return dataValueList;
    }

    public static VisualDevJsonModel getVisualJsonModel(VisualdevEntity entity) {
        VisualDevJsonModel jsonModel = new VisualDevJsonModel();
        if (entity.getColumnData() != null) {
            jsonModel.setColumnData(JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class));
        }
        if (entity.getAppColumnData() != null) {
            jsonModel.setAppColumnData(JsonUtil.getJsonToBean(entity.getAppColumnData(), ColumnDataModel.class));
        }
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        jsonModel.setFormData(formDataModel);
        if (!VisualWebTypeEnum.DATA_VIEW.getType().equals(entity.getWebType())) {
            jsonModel.setFormListModels(JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class));
        }
        jsonModel.setVisualTables(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        jsonModel.setId(entity.getId());
        jsonModel.setDbLinkId(entity.getDbLinkId());
        jsonModel.setFullName(entity.getFullName());
        jsonModel.setType(entity.getType());
        jsonModel.setWebType(entity.getWebType());
        jsonModel.setFlowEnable(entity.getEnableFlow() == 1);
        return jsonModel;
    }

    public static VisualDevJsonModel getVisualJsonModel(VisualdevReleaseEntity entity) throws WorkFlowException {
        if (entity == null) throw new WorkFlowException("该表单已删除");
        VisualDevJsonModel jsonModel = new VisualDevJsonModel();
        if (entity.getColumnData() != null) {
            jsonModel.setColumnData(JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class));
        }
        if (entity.getAppColumnData() != null) {
            jsonModel.setAppColumnData(JsonUtil.getJsonToBean(entity.getAppColumnData(), ColumnDataModel.class));
        }
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        jsonModel.setFormData(formDataModel);
        if (!VisualWebTypeEnum.DATA_VIEW.getType().equals(entity.getWebType())) {
            jsonModel.setFormListModels(JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class));
        }
        jsonModel.setVisualTables(JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class));
        jsonModel.setId(entity.getId());
        jsonModel.setDbLinkId(entity.getDbLinkId());
        jsonModel.setFullName(entity.getFullName());
        jsonModel.setType(entity.getType());
        jsonModel.setWebType(entity.getWebType());
        jsonModel.setFlowEnable(entity.getEnableFlow() == 1);
        return jsonModel;
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
     * 字符串转数组
     *
     * @param value 值
     * @return
     */
    public static Object getDataConversion(Object value) {
        Object dataValue = getDataConversion(null, value, false, "/");
        return dataValue;
    }

    /**
     * 字符串转数组
     *
     * @param redis 转换对象
     * @param value 值
     * @return
     */
    public static Object getDataConversion(Map<String, Object> redis, Object value, boolean isMultiple, String separator) {
        Object dataValue = value;
        boolean iszhuanhuan = redis != null;
        try {
            List<List> list = JsonUtil.getJsonToList(String.valueOf(value), List.class);
            dataValue = list;
            if (iszhuanhuan) {
                //一级分隔符
                StringJoiner joiner = new StringJoiner(",");
                for (List listChild : list) {
                    StringJoiner aa = new StringJoiner(separator);
                    for (Object object : listChild) {
                        String value1 = redis.get(String.valueOf(object)) != null ? String.valueOf(redis.get(String.valueOf(object))) : "";
                        if (StringUtil.isNotEmpty(value1)) {
                            aa.add(value1);
                        }
                    }
                    joiner.add(aa.toString());
                }
                dataValue = joiner.toString();
            }
        } catch (Exception e) {
            try {
                List<String> list = JsonUtil.getJsonToList(String.valueOf(value), String.class);
                dataValue = list;
                if (iszhuanhuan) {
                    if (isMultiple) {//一级分隔符
                        separator = ",";
                    }
                    StringJoiner joiner = new StringJoiner(separator);
                    for (Object listChild : list) {
                        String value1 = redis.get(String.valueOf(listChild)) != null ? String.valueOf(redis.get(String.valueOf(listChild))) : "";
                        if (StringUtil.isNotEmpty(value1)) {
                            joiner.add(value1);
                        }
                    }
                    dataValue = joiner.toString();
                }
            } catch (Exception e1) {
                dataValue = String.valueOf(value);
                if (iszhuanhuan) {
                    dataValue = redis.get(String.valueOf(value)) != null ? String.valueOf(redis.get(String.valueOf(value))) : "";
                }
            }
        }
        return dataValue;
    }

    /**
     * 视图sql条件拼接
     *
     * @param info
     * @param queryCondition
     * @param parameterMap
     * @return
     */
    public static void getViewQuerySql(DataInterfaceEntity info, List<FieLdsModel> queryCondition, Map<String, String> parameterMap) {
        if (Objects.equals(info.getType(), 1) && queryCondition.size() > 0) {
            String searchSqlStr = "";
            for (FieLdsModel item : queryCondition) {
                switch (item.getSearchType()) {
                    case 1:
                        if (StringUtil.isNotEmpty(searchSqlStr)) {
                            searchSqlStr += " and t." + item.getVModel() + " = '" + XSSEscape.escape(item.getFieldValue()) + "'";
                        } else {
                            searchSqlStr = "t." + item.getVModel() + " = '" + XSSEscape.escape(item.getFieldValue()) + "'";
                        }
                        break;
                    case 2:
                        if (StringUtil.isNotEmpty(searchSqlStr)) {
                            searchSqlStr += " and t." + item.getVModel() + " like '%" + XSSEscape.escape(item.getFieldValue()) + "%'";
                        } else {
                            searchSqlStr = "t." + item.getVModel() + " like '%" + XSSEscape.escape(item.getFieldValue()) + "%'";
                        }
                        break;
                    case 3://between
                        if (StringUtil.isNotEmpty(searchSqlStr)) {
                            searchSqlStr += " and t." + item.getVModel() + " between '" + XSSEscape.escape(String.valueOf(item.getFieldValueOne()))
                                    + "' and '" + XSSEscape.escape(String.valueOf(item.getFieldValueTwo())) + "'";
                        } else {
                            searchSqlStr = "t." + item.getVModel() + " between '" + XSSEscape.escape(String.valueOf(item.getFieldValueOne()))
                                    + "' and '" + XSSEscape.escape(String.valueOf(item.getFieldValueTwo())) + "'";
                        }
                        break;
                    case 4://包含
                        List<String> dataList = item.getDataList();
                        if (dataList.size() > 0) {
                            if (StringUtil.isNotEmpty(searchSqlStr)) {
                                searchSqlStr += " and (";
                            } else {
                                searchSqlStr += " 1=1 and (";
                            }
                            int n = 0;
                            for (String value : dataList) {
                                if (n > 0) {
                                    searchSqlStr += " or t." + item.getVModel() + " like '%" + value + "%'";
                                } else {
                                    searchSqlStr += "t." + item.getVModel() + " like '%" + value + "%'";
                                }
                                n++;
                            }
                            searchSqlStr += ") ";
                        }
                        break;
                    default:
                        break;
                }
            }
            parameterMap.put("searchSqlStr", searchSqlStr);
        }
    }

    /**
     * 视图非sql条件过滤
     *
     * @param info
     * @param queryCondition
     * @param dataRes
     * @return
     */
    public static List<Map<String, Object>> getViewQueryNotSql(DataInterfaceEntity info, List<FieLdsModel> queryCondition, List<Map<String, Object>> dataRes) {
        List<Map<String, Object>> dataInterfaceList = new ArrayList<>();
        if (!Objects.equals(info.getType(), 1) && queryCondition.size() > 0) {
            if (queryCondition.size() > 0) {
                for (Map<String, Object> map : dataRes) {
                    if (OnlinePublicUtils.mapCompar(queryCondition, map)) {
                        dataInterfaceList.add(map);
                    }
                }
            } else {
                dataInterfaceList = dataRes;
            }
        } else {
            dataInterfaceList = dataRes;
        }
        return dataInterfaceList;
    }

    /**
     * 判断两个map有相同key-value
     *
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/1/5
     */
    public static boolean mapCompar(List<FieLdsModel> searchList, Map<String, Object> hashMap2) {
        boolean isChange = false;
        for (FieLdsModel item : searchList) {
            String realValue = hashMap2.get(item.getVModel()) == null ? "" : (String) hashMap2.get(item.getVModel());
            switch (item.getSearchType()) {
                case 2:
                    if (realValue.indexOf(item.getFieldValue()) >= 0) {
                        isChange = true;
                    }
                    break;
                case 3://between
                    List<String> longList = new ArrayList() {{
                        add(JnpfKeyConsts.NUM_INPUT);
                        add(JnpfKeyConsts.DATE);
                    }};
                    if (longList.contains(item.getConfig().getJnpfKey())) {
                        Long valueLong = Long.parseLong(realValue);
                        Long valueLongOne = (Long) item.getFieldValueOne();
                        Long valueLongTwo = (Long) item.getFieldValueTwo();
                        if (valueLong >= valueLongOne && valueLong <= valueLongTwo) {
                            isChange = true;
                        }
                    } else {
                        String valueLongOne = (String) item.getFieldValueOne();
                        String valueLongTwo = (String) item.getFieldValueTwo();
                        if (realValue.compareTo(valueLongOne) >= 0 && realValue.compareTo(valueLongTwo) <= 0) {
                            isChange = true;
                        }
                    }
                    break;
                case 4://包含
                    List<String> dataList = item.getDataList();
                    for (String value : dataList) {
                        isChange = value.indexOf(realValue) >= 0;
                    }
                    if (isChange) {
                        return true;
                    }
                    break;
                default://1,其他条件都按等于查询
                    isChange = item.getFieldValue().equals(realValue);
                    break;
            }
        }
        return isChange;
    }
}

