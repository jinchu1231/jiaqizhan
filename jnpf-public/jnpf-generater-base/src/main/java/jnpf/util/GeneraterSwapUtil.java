package jnpf.util;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jnpf.annotation.JnpfField;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.datainterface.DataInterfaceActionVo;
import jnpf.base.model.datainterface.DataInterfaceModel;
import jnpf.base.model.datainterface.DataInterfacePage;
import jnpf.base.model.filter.RuleInfo;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.FilterService;
import jnpf.base.service.ProvinceService;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.VisualUtils;
import jnpf.base.util.common.DataControlUtils;
import jnpf.constant.PermissionConst;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.model.superQuery.ConditionJsonModel;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.database.model.superQuery.SuperQueryConditionModel;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.model.flowtemplate.FlowTemplateInfoVO;
import jnpf.engine.model.flowtemplatejson.FlowJsonModel;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.QueryAllModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.TemplateJsonModel;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.onlinedev.model.OnlineDevEnum.CacheKeyEnum;
import jnpf.onlinedev.model.OnlineDevEnum.OnlineDataTypeEnum;
import jnpf.onlinedev.model.OnlineImport.ExcelImportModel;
import jnpf.onlinedev.model.OnlineImport.ImportDataModel;
import jnpf.onlinedev.model.OnlineImport.ImportFormCheckUniqueModel;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualDevInfoService;
import jnpf.onlinedev.util.onlineDevUtil.OnlineDevListUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlineSwapDataUtils;
import jnpf.permission.entity.GroupEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.GroupService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.service.FlowFormService;
import jnpf.util.context.RequestContext;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 数据转换(代码生成器用)
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Component
public class GeneraterSwapUtil {

    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private FilterService filterService;
    @Autowired
    private UserService userService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualDevInfoService visualDevInfoService;
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private OnlineSwapDataUtils swapDataUtils;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private FlowFormDataUtil flowFormDataUtil;
    @Autowired
    private DbLinkService dblinkService;
    
    public final String regEx = "[\\[\\]\"]";

    @Autowired
    private RedisUtil redisUtil;

    private static long DEFAULT_CACHE_TIME = 60 * 5;

    /**
     * 日期时间戳字符串转换
     *
     * @param date
     * @param format
     * @return
     */
    public String dateSwap(String date, String format) {
        if (StringUtil.isNotEmpty(date)) {
            DateTimeFormatter ftf = DateTimeFormatter.ofPattern(format);
            if (date.contains(",")) {
                String[] dates = date.split(",");
                long time1 = Long.parseLong(dates[0]);
                long time2 = Long.parseLong(dates[1]);
                String value1 = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time1), ZoneId.systemDefault()));
                String value2 = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time2), ZoneId.systemDefault()));
                return value1 + "至" + value2;
            }
            long time = Long.parseLong(date);
            String value = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
            return value;
        }
        return date;
    }

    /**
     * 行政区划转换
     *
     * @param data
     * @return
     */
    public String provinceData(String data, Map<String, Object> localCache) {
        Map<String, String> proMap = new HashMap<>();
        if (localCache != null && localCache.containsKey("__pro_map")) {
            proMap = (Map<String, String>) localCache.get("__pro_map");
        }
        if (StringUtil.isNotEmpty(data)) {
            try {
                if (data.contains("[[")) {
                    List<String> addList = new ArrayList<>();
                    String[][] provinceDataS = JsonUtil.getJsonToBean(data, String[][].class);
                    for (String[] AddressData : provinceDataS) {
                        List<String> provList = new ArrayList(Arrays.asList(AddressData));
                        List<String> nameList = new ArrayList<>();
                        if (localCache != null) {
                            for (String info : provList) {
                                nameList.add(proMap.get(info));
                            }
                        }else{
                            List<ProvinceEntity> proList = provinceService.getProList(provList);
                            for(ProvinceEntity info : proList){
                                nameList.add(info.getFullName());
                            }
                        }
                        addList.add(String.join("/", nameList));
                    }
                    return String.join(";", addList);
                } else if (data.contains("[")) {
                    List<String> provList = JsonUtil.getJsonToList(data, String.class);
                    List<String> nameList = new ArrayList<>();
                    if (localCache != null) {
                        for (String info : provList) {
                            nameList.add(proMap.get(info));
                        }
                    }else{
                        List<ProvinceEntity> proList = provinceService.getProList(provList);
                        for(ProvinceEntity info : proList){
                            nameList.add(info.getFullName());
                        }
                    }
                    return String.join("/", nameList);
                } else {
                    String[] strs = data.split(",");
                    List<String> provList = new ArrayList(Arrays.asList(strs));
                    List<String> proNameList = new ArrayList<>();
                    if (localCache != null) {
                        for (String info : provList) {
                            proNameList.add(proMap.get(info));
                        }
                    }else{
                        List<ProvinceEntity> proList = provinceService.getProList(provList);
                        for(ProvinceEntity info : proList){
                            proNameList.add(info.getFullName());
                        }
                    }
                    return String.join("/", proNameList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public Map<String, Object> localCache() {
        //公共数据
        String dsName = Optional.ofNullable(TenantHolder.getDatasourceId()).orElse("");
        Map<String, Object> localCache = new HashMap<>();
        if (!localCache.containsKey("__pro_map")) {
            //省市区
            Map<Object, Object> proMap = redisUtil.getMap(String.format("%s-%s-%d", dsName, "province", 1));
            List<Map<String, String>> proMapList = new ArrayList<>();
            if (proMap.size() == 0) {
                //分级存储
                for (int i = 1; i <= 4; i++) {
                    String redisKey = String.format("%s-%s-%d", dsName, "province", i);
                    if (!redisUtil.exists(redisKey)) {
                        List<ProvinceEntity> provinceEntityList = provinceService.getProListBytype(String.valueOf(i));
                        Map<String, String> provinceMap = new HashMap<>(16);
                        if (provinceEntityList != null) {
                            provinceEntityList.stream().forEach(p -> provinceMap.put(p.getId(), p.getFullName()));
                        }
                        proMapList.add(provinceMap);
                        //区划基本不修改 不做是否缓存判断
                        redisUtil.insert(redisKey, provinceMap, RedisUtil.CAHCEWEEK);
                    }
                }
            } else {
                for (int i = 1; i <= 4; i++) {
                    proMapList.add(redisUtil.getMap(String.format("%s-%s-%d", dsName, "province", i)));
                }
            }

            Map<String, String> proMapr = new HashMap<>();
            proMapList.forEach(item -> proMapr.putAll(item));
            localCache.put("__pro_map", proMapr);
        }
        return localCache;
    }

    /**
     * 公司部门id转名称
     *
     * @param value
     * @return
     */
    public String comSelectValue(String value, String showLevel) {
        if (StringUtil.isNotEmpty(String.valueOf(value))) {
            OrganizeEntity organizeEntity = organizeService.getInfo(String.valueOf(value));
            if ("all".equals(showLevel)) {
                List<OrganizeEntity> organizeListAll = organizeService.getList(false);
                String[] organizeTreeId = StringUtil.isNotEmpty(organizeEntity.getOrganizeIdTree()) ? organizeEntity.getOrganizeIdTree().split(",") : new String[]{};
                List<String> organizeTreeList = Arrays.asList(organizeTreeId).stream().filter(t -> !t.isEmpty()).collect(Collectors.toList());
                StringJoiner joiner = new StringJoiner("/");
                for (int i = 0; i < organizeTreeList.size(); i++) {
                    String id = organizeTreeList.get(i);
                    OrganizeEntity entity = organizeListAll.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
                    if (entity != null) {
                        joiner.add(entity.getFullName());
                    }
                }
                value = joiner.toString();
            } else {
                if (organizeEntity != null) {
                    if (organizeEntity.getCategory().equals("company")) {
                        return " ";
                    }
                    value = organizeEntity.getFullName();
                }
            }
        } else {
            value = " ";
        }
        return value;
    }

    /**
     * 公司部门id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String comSelectValues(String ids, Boolean mul) {
        List<String> comValueList = new ArrayList<>();
        if (StringUtil.isEmpty(ids)) {
            return null;
        }
        String Separator = mul ? "," : "/";
        if (ids.contains("[[")) {
            String[][] idArrays = JsonUtil.getJsonToBean(ids, String[][].class);
            for (String[] array : idArrays) {
                List<String> idList = new ArrayList<>();
                for (String s : array) {
                    OrganizeEntity info = organizeService.getInfo(s);
                    idList.add(Objects.nonNull(info) ? info.getFullName() : s);
                }
                String orgCom = idList.stream().collect(Collectors.joining("/"));
                comValueList.add(orgCom);
            }
            return comValueList.stream().collect(Collectors.joining(";"));
        } else if (ids.contains("[")) {
            List<String> idList = JsonUtil.getJsonToList(ids, String.class);
            List<String> nameList = new ArrayList<>();
            for (String orgId : idList) {
                OrganizeEntity info = organizeService.getInfo(orgId);
                nameList.add(Objects.nonNull(info) ? info.getFullName() : orgId);
            }
            return nameList.stream().collect(Collectors.joining(Separator));
        } else {
            ids = ids.replaceAll("\"", "");
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                List<String> comSelectList = new ArrayList<>();
                for (String id : idList) {
                    OrganizeEntity organizeEntity = organizeService.getInfo(id);
                    if (organizeEntity != null) {
                        comSelectList.add(organizeEntity.getFullName());
                    }
                }
                return String.join(",", comSelectList);
            }
        }
        return null;
    }


    /**
     * 岗位id转名称
     *
     * @param id
     * @return
     */
    public String posSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            PositionEntity positionServiceInfo = positionService.getInfo(id);
            if (ObjectUtil.isNotEmpty(positionServiceInfo)) {
                return positionServiceInfo.getFullName();
            }
            return id;
        }
        return " ";
    }

    /**
     * 岗位id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String posSelectValues(String ids) {
        if (StringUtil.isEmpty(ids)) {
            return "";
        }
        List<String> posList = new ArrayList<>();
        if (ids.contains("[")) {
            List<String> idList = JsonUtil.getJsonToList(ids, String.class);
            List<String> nameList = new ArrayList<>();
            for (String orgId : idList) {
                PositionEntity info = positionService.getInfo(orgId);
                nameList.add(Objects.nonNull(info) ? info.getFullName() : orgId);
            }
            posList = nameList;
        } else {
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                for (String id : idList) {
                    PositionEntity positionEntity = positionService.getInfo(id);
                    if (ObjectUtil.isNotEmpty(positionEntity)) {
                        posList.add(positionEntity.getFullName());
                    }
                }
            }
        }
        return String.join(",", posList);
    }

    /**
     * 用户id转名称
     *
     * @param id
     * @return
     */
    public String userSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            UserEntity userEntity = userService.getInfo(id);
            if (ObjectUtil.isNotEmpty(userEntity)) {
                return userEntity.getRealName() + "/" + userEntity.getAccount();
            }
            return id;
        }
        return "";
    }

    /**
     * 用户id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String userSelectValues(String ids) {
        //公共数据
        String dsName = Optional.ofNullable(TenantHolder.getDatasourceId()).orElse("");
        //人员
        String redisKey = dsName + CacheKeyEnum.USER.getName();
        Map<String, Object> userMap;
        if (redisUtil.exists(redisKey)) {
            userMap = redisUtil.getMap(redisKey);
            userMap = Optional.ofNullable(userMap).orElse(new HashMap<>(20));
        }else{
            userMap = userService.getUserMap("id-fullName");
            redisUtil.insert(redisKey, userMap, DEFAULT_CACHE_TIME);
        }

        if (StringUtil.isEmpty(ids)) {
            return ids;
        }
        if (ids.contains("[")) {
            List<String> nameList = new ArrayList<>();
            List<String> jsonToList = JsonUtil.getJsonToList(ids, String.class);
            for (String userId : jsonToList) {
                nameList.add(Objects.nonNull(userMap.get(userId)) ? userMap.get(userId).toString() : userId);
            }
            return String.join(";", nameList);
        } else {
            List<String> userInfoList = new ArrayList<>();
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                for (String userId : idList) {
                    userInfoList.add(Objects.nonNull(userMap.get(userId)) ? userMap.get(userId).toString() : userId);
                }
            }
            return String.join("-", userInfoList);
        }
    }

    /**
     * 用户组件id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String usersSelectValues(String ids) {
        if (StringUtil.isEmpty(ids)) {
            return ids;
        }
        List<String> dataNoSwapInMethod = OnlinePublicUtils.getDataNoSwapInMethod(ids);
        StringJoiner valueJoin = new StringJoiner(",");
        for (String data : dataNoSwapInMethod) {
            String id = data.contains("--") ? data.substring(0, data.lastIndexOf("--")) : data;
            String type = data.contains("--") ? data.substring(data.lastIndexOf("--") + 2) : "";
            switch (type) {
                case "role":
                    RoleEntity roleEntity = roleService.getInfo(id);
                    if (roleEntity != null) {
                        valueJoin.add(roleEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "position":
                    PositionEntity positionEntity = positionService.getInfo(id);
                    if (positionEntity != null) {
                        valueJoin.add(positionEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "company":
                case "department":
                    OrganizeEntity organizeEntity = organizeService.getInfo(id);
                    if (organizeEntity != null) {
                        valueJoin.add(organizeEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "group":
                    GroupEntity groupEntity = groupService.getInfo(id);
                    if (groupEntity != null) {
                        valueJoin.add(groupEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "user":
                default:
                    UserEntity userEntity = userService.getInfo(id);
                    if (userEntity != null) {
                        valueJoin.add(userEntity.getRealName() + "/" + userEntity.getAccount());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
            }
        }
        return valueJoin.toString();
    }


    /**
     * 开关
     *
     * @param data
     * @return
     */
    public String switchSelectValue(String data, String activeTxt, String inactiveTxt) {
        if (StringUtil.isNotEmpty(data)) {
            if (data.equals("0") || data.equals("false")) {
                return inactiveTxt;
            } else if (data.equals("1") || data.equals("true")) {
                return activeTxt;
            } else {
                return data;
            }
        }
        return null;
    }

    /**
     * 关联表单数据转换
     *
     * @param vmodel
     * @param value
     * @param modelId
     * @return
     */
    public String swapRelationFormValue(String vmodel, String value, String modelId, Map<String, Object> formDataMaps) {
        if (StringUtil.isEmpty(value)) {
            return "";
        }
        try {
            VisualdevModelDataInfoVO infoVO = null;
            VisualdevEntity entity = visualdevService.getInfo(modelId);
            if (!StringUtil.isEmpty(entity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(entity.getVisualTables())) {
                infoVO = visualDevInfoService.getDetailsDataInfoNew(value, modelId);
            } else {
                infoVO = visualDevInfoService.infoDataChangeNew(value, modelId);
            }
            if (infoVO != null) {
                Map<String, Object> formDataMap = infoVO.getData() != null ? JsonUtil.stringToMap(infoVO.getData()) : new HashMap<>();
                if (formDataMap.size() > 0) {
                    formDataMaps.putAll(formDataMap);
                    formDataMap = OnlinePublicUtils.mapKeyToLower(formDataMap);
                    value = String.valueOf(formDataMap.get(vmodel.toLowerCase()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    /**
     * 弹窗
     *
     * @param interfaceId
     * @param propsValue
     * @param relationField
     * @param dataValue
     * @return
     */
    public String getPopupSelectValue11(String interfaceId, String propsValue, String relationField, String dataValue, Map<String, Object> dataMaps, String json, int num, Map<String, Object> dataAll) {
        if (StringUtil.isEmpty(interfaceId)) {
            return null;
        }
        if (StringUtil.isNotEmpty(dataValue)) {
            Object data = dataInterfaceService.infoToId(interfaceId,null,null).getData();
            List<Map<String, Object>> dataInterfaceDataList;
            if (data instanceof ActionResult) {
                ActionResult actionVo = (ActionResult) data;
                dataInterfaceDataList = (List<Map<String, Object>>) actionVo.getData();
            } else {
                dataInterfaceDataList = (List<Map<String, Object>>) data;
            }
            if (dataValue.contains("[")) {
                List<String> valueList = JsonUtil.getJsonToList(dataValue, String.class);
                List<String> swapValue = new ArrayList<>();
                for (String va : valueList) {
                    dataInterfaceDataList.stream().filter(map ->
                            map.get(propsValue).equals(va)
                    ).forEach(
                            modelMap -> swapValue.add(String.valueOf(modelMap.get(relationField)))
                    );
                }
                return swapValue.stream().collect(Collectors.joining(","));
            }
            Map<String, Object> dataMap = dataInterfaceDataList.stream().filter(d -> d.get(propsValue).equals(dataValue)).findFirst().orElse(null);
            if (dataMap != null) {
                dataMaps.putAll(dataMap);
                return String.valueOf(dataMap.get(relationField));
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * 弹窗
     *
     * @param interfaceId
     * @param propsValue
     * @param relationField
     * @param dataValue
     * @return
     */
    public String getPopupSelectValue(String interfaceId, String propsValue, String relationField, String dataValue, Map<String, Object> dataMaps, String json, int num, Map<String, Object> dataAll) {
        if (StringUtil.isEmpty(interfaceId)) {
            return null;
        }
        List<TemplateJsonModel> list = JsonUtil.getJsonToList(json, TemplateJsonModel.class);
        Map<String, String> infoMap = new HashMap<>();
        List<DataInterfaceModel> listParam = new ArrayList<>();
        for (TemplateJsonModel templateJsonModel : list) {
            DataInterfaceModel dataInterfaceModel = JsonUtil.getJsonToBean(templateJsonModel, DataInterfaceModel.class);
            String defaultV = "";
            if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                String[] mastTable = templateJsonModel.getRelationField().split("_jnpf_");
                String[] child = templateJsonModel.getRelationField().split("-");
                if (mastTable.length > 1) {
                    if (dataAll.get(mastTable[0]) instanceof Map) {
                        Map<String, Object> mastTableData = (Map<String, Object>) dataAll.get(mastTable[0]);
                        infoMap.put(templateJsonModel.getField(), String.valueOf(mastTableData.get(mastTable[1])));
                        defaultV = String.valueOf(mastTableData.get(mastTable[1]));
                    }
                } else if (child.length > 1) {
                    if (dataAll.get(child[0]) instanceof List) {
                        List<Map<String, Object>> chidList = (List<Map<String, Object>>) dataAll.get(child[0]);
                        for (int i = 0; i < chidList.size(); i++) {
                            Map<String, Object> objectMap = chidList.get(i);
                            if (i == num) {
                                infoMap.put(templateJsonModel.getField(), String.valueOf(objectMap.get(child[1])));
                                defaultV = String.valueOf(objectMap.get(child[1]));
                            }
                        }
                    }
                } else {
                    infoMap.put(templateJsonModel.getField(), String.valueOf(dataAll.get(templateJsonModel.getRelationField())));
                    defaultV = String.valueOf(String.valueOf(dataAll.get(templateJsonModel.getRelationField())));
                }
            }
            dataInterfaceModel.setDefaultValue(defaultV);
            listParam.add(dataInterfaceModel);
        }
        if (StringUtil.isNotEmpty(dataValue)) {
//            Object data = dataInterfaceService.infoToId(interfaceId, null, infoMap).getData();
//            List<Map<String, Object>> dataInterfaceDataList;
//            if (data instanceof ActionResult) {
//                ActionResult actionVo = (ActionResult) data;
//                dataInterfaceDataList = (List<Map<String, Object>>) actionVo.getData();
//            } else {
//                dataInterfaceDataList = (List<Map<String, Object>>) data;
//            }
            DataInterfacePage dataInterfacePage = new DataInterfacePage();
            dataInterfacePage.setParamList(listParam);
            dataInterfacePage.setInterfaceId(interfaceId);
            List<String> ids = new ArrayList<>();
            if (dataValue.startsWith("[")) {
                ids = JsonUtil.getJsonToList(dataValue, String.class);
            } else {
                ids.add(dataValue);
            }
            dataInterfacePage.setIds(ids);
            dataInterfacePage.setPropsValue(propsValue);
            dataInterfacePage.setRelationField(relationField);
            List<Map<String, Object>> dataInterfaceDataList = dataInterfaceService.infoByIds(interfaceId, dataInterfacePage).getData();
            if (dataValue.contains("[")) {
                List<String> valueList = JsonUtil.getJsonToList(dataValue, String.class);
                List<String> swapValue = new ArrayList<>();
                for (String va : valueList) {
                    dataInterfaceDataList.stream().filter(map ->
                            map.get(propsValue).equals(va)
                    ).forEach(
                            modelMap -> swapValue.add(String.valueOf(modelMap.get(relationField)))
                    );
                }
                return swapValue.stream().collect(Collectors.joining(","));
            }
            if (dataInterfaceDataList != null) {
                Map<String, Object> dataMap = dataInterfaceDataList.stream().filter(d -> d.get(propsValue).equals(dataValue)).findFirst().orElse(null);
                if (dataMap != null) {
                    dataMaps.putAll(dataMap);
                    return String.valueOf(dataMap.get(relationField));
                }
            }
            return null;
        } else {
            return null;
        }
    }


    public String getFileNameInJson(String fileJson) {
        if (StringUtil.isNotEmpty(fileJson) && !"null".equals(fileJson)) {
            return fileJson;
        }
        return "";
    }


    /**
     * 获取数据字典数据
     *
     * @param feild
     * @return
     */
    public String getDicName(String feild, String dictionaryTypeId) {
        if (StringUtil.isNotEmpty(feild)) {
            //去除中括号以及双引号
            feild = feild.replaceAll(regEx, "");
            //判断多选框
            String[] feilds = feild.split(",");
            if (feilds.length > 1) {
                StringBuilder feildsValue = new StringBuilder();
                DictionaryDataEntity dictionaryDataEntity;
                for (String feil : feilds) {
                    dictionaryDataEntity = dictionaryDataService.getSwapInfo(feil, dictionaryTypeId);
                    if (dictionaryDataEntity != null) {
                        feildsValue.append(dictionaryDataEntity.getFullName() + ",");
                    } else {
                        feildsValue.append(feil + ",");
                    }
                }
                String finalValue;
                if (StringUtil.isEmpty(feildsValue) || feildsValue.equals("")) {
                    finalValue = feildsValue.toString();
                } else {
                    finalValue = feildsValue.substring(0, feildsValue.length() - 1);
                }
                return finalValue;
            }
            DictionaryDataEntity dictionaryDataentity = dictionaryDataService.getSwapInfo(feild, dictionaryTypeId);
            if (dictionaryDataentity != null) {
                return dictionaryDataentity.getFullName();
            }
            return feild;
        }
        if (StringUtil.isNotEmpty(feild)) {
            List<DictionaryDataEntity> dicList = dictionaryDataService.getDicList(dictionaryTypeId);
        }
        return feild;
    }

    /**
     * 获取数据字典数据-
     *
     * @param feild
     * @param keyName id或encode
     * @return
     */
    public String getDicName(String feild, String dictionaryTypeId, String keyName, boolean isMultiple, String separator) {
        Object dataConversion = "";
        String redisKey = dictionaryTypeId + "-" + feild + "-" + keyName;
        if (StringUtil.isNotEmpty(feild)) {
            List<DictionaryDataEntity> dicList;
            if (redisUtil.exists(redisKey)) {
                List<Object> tmpList = redisUtil.get(redisKey, 0, -1);
                dicList = JsonUtil.getJsonToList(tmpList, DictionaryDataEntity.class);
            } else {
                dicList = dictionaryDataService.getDicList(dictionaryTypeId);
                redisUtil.insert(redisKey, dicList, DEFAULT_CACHE_TIME);
            }
            Map<String, Object> idMap = new HashMap<>(dicList.size());
            Map<String, Object> enCodeMap = new HashMap<>(dicList.size());
            for (DictionaryDataEntity dd : dicList) {
                idMap.put(dd.getId(), dd.getFullName());
                enCodeMap.put(dd.getEnCode(), dd.getFullName());
            }
            if (StringUtil.isNotEmpty(separator)) {
                separator = "/";
            }
            if ("enCode".equals(keyName)) {
                dataConversion = FormPublicUtils.getDataConversion(enCodeMap, feild, isMultiple, separator);
            } else {
                dataConversion = FormPublicUtils.getDataConversion(idMap, feild, isMultiple, separator);
            }
        }
        return dataConversion.toString();
    }

    /**
     * 获取远端数据
     *
     * @param urlId
     * @param label
     * @param value
     * @param feildValue
     * @return
     * @throws IOException
     */
    public String getDynName(String urlId, String label, String value, String feildValue, String json, int num, Map<String, Object> dataAll) {
        List<TemplateJsonModel> list = JsonUtil.getJsonToList(json, TemplateJsonModel.class);
        Map<String, String> infoMap = list.size() > 0 ? new HashMap<>() : null;
        for (TemplateJsonModel templateJsonModel : list) {
            if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                String[] mastTable = templateJsonModel.getRelationField().split("_jnpf_");
                String[] child = templateJsonModel.getRelationField().split("-");
                if (mastTable.length > 1) {
                    if (dataAll.get(mastTable[0]) instanceof Map) {
                        Map<String, Object> mastTableData = (Map<String, Object>) dataAll.get(mastTable[0]);
                        infoMap.put(templateJsonModel.getField(), String.valueOf(mastTableData.get(mastTable[1])));
                    }
                } else if (child.length > 1) {
                    if (dataAll.get(child[0]) instanceof List) {
                        List<Map<String, Object>> chidList = (List<Map<String, Object>>) dataAll.get(child[0]);
                        for (int i = 0; i < chidList.size(); i++) {
                            Map<String, Object> objectMap = chidList.get(i);
                            if (i == num) {
                                infoMap.put(templateJsonModel.getField(), String.valueOf(objectMap.get(child[1])));
                            }
                        }
                    }
                } else {
                    infoMap.put(templateJsonModel.getField(), String.valueOf(dataAll.get(templateJsonModel.getRelationField())));
                }
            }
        }
        if (StringUtil.isNotEmpty(feildValue)) {
            //去除中括号以及双引号
            feildValue = feildValue.replaceAll(regEx, "");
            //获取远端数据
            Map<String, String> a = new HashMap<>();
            ActionResult object = dataInterfaceService.infoToIdById(urlId, infoMap);
            if (object.getData() != null && object.getData() instanceof DataInterfaceActionVo) {
                DataInterfaceActionVo vo = (DataInterfaceActionVo) object.getData();
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) vo.getData();
                //判断是否多选
                String[] feildValues = feildValue.split(",");
                if (feildValues.length > 0) {
                    //转换的真实值
                    StringBuilder feildVa = new StringBuilder();
                    for (String feild : feildValues) {
                        for (Map<String, Object> data : dataList) {
                            if (String.valueOf(data.get(value)).equals(feild)) {
                                feildVa.append(data.get(label) + ",");
                            }
                        }
                    }
                    String finalValue;
                    if (StringUtil.isEmpty(feildVa) || feildVa.equals("")) {
                        finalValue = feildVa.toString();
                    } else {
                        finalValue = feildVa.substring(0, feildVa.length() - 1);
                    }
                    return finalValue;
                }
                for (Map<String, Object> data : dataList) {
                    if (feildValue.equals(String.valueOf(data.get(value)))) {
                        return data.get(label).toString();
                    }
                    return feildValue;
                }
            }
            return feildValue;
        }
        return feildValue;
    }

    /**
     * 获取远端数据
     *
     * @param urlId
     * @param name
     * @param id
     * @param children
     * @param feildValue
     * @return
     */
    public String getDynName(String urlId, String name, String id, String children, String feildValue, boolean mul) {
        List<String> result = new ArrayList<>();
        String sep = ",";
        if (mul) {
            sep = "/";
        }
        if (StringUtil.isNotEmpty(feildValue)) {
            Map<String, String> a = new HashMap<>();
            ActionResult data = dataInterfaceService.infoToId(urlId,null,null);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.getData();
//			if (actionVo.getData() instanceof List) {
//				dataList = (List<Map<String, Object>>) actionVo.getData();
//			}
            JSONArray dataAll = JsonUtil.getListToJsonArray(dataList);
            List<Map<String, Object>> list = new ArrayList<>();
            treeToList(id, name, children, dataAll, list);
            String value = feildValue.replaceAll("\\[", "").replaceAll("\\]", "");
            Map<String, String> resultMap = new HashMap<>();
            list.stream().forEach(t -> {
                resultMap.put(String.valueOf(t.get(id)), String.valueOf(t.get(name)));
            });

            if (feildValue.startsWith("[[")) {
                String[][] fv = JsonUtil.getJsonToBean(feildValue, String[][].class);
                StringJoiner f1 = new StringJoiner(",");
                for (String[] f : fv) {
                    StringJoiner v1 = new StringJoiner("/");
                    for (String v : f) {
                        v1.add(resultMap.get(v));
                    }
                    f1.add(v1.toString());
                }
                return f1.toString();
            } else if (feildValue.startsWith("[")) {
                List<String> fvs = JsonUtil.getJsonToList(feildValue, String.class);
                return fvs.stream().map(m -> resultMap.get(m)).collect(Collectors.joining(sep));
            } else {
                return resultMap.get(feildValue);
            }
        }
        return feildValue;
    }


    /**
     * 获取远端数据
     *
     * @param urlId
     * @param name
     * @param id
     * @param children
     * @param feildValue
     * @param mul        是否多选
     * @param isFullPath 全路径
     * @return
     */
    public String getDynName(String urlId, String name, String id, String children, String feildValue, boolean mul, boolean isFullPath, String json, int num, Map<String, Object> dataAll1) {
        List<TemplateJsonModel> list = JsonUtil.getJsonToList(json, TemplateJsonModel.class);
        Map<String, String> infoMap = list.size() > 0 ? new HashMap<>() : null;
        for (TemplateJsonModel templateJsonModel : list) {
            if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                String[] mastTable = templateJsonModel.getRelationField().split("_jnpf_");
                String[] child = templateJsonModel.getRelationField().split("-");
                if (mastTable.length > 1) {
                    if (dataAll1.get(mastTable[0]) instanceof Map) {
                        Map<String, Object> mastTableData = (Map<String, Object>) dataAll1.get(mastTable[0]);
                        infoMap.put(templateJsonModel.getField(), String.valueOf(mastTableData.get(mastTable[1])));
                    }
                } else if (child.length > 1) {
                    if (dataAll1.get(child[0]) instanceof List) {
                        List<Map<String, Object>> chidList = (List<Map<String, Object>>) dataAll1.get(child[0]);
                        for (int i = 0; i < chidList.size(); i++) {
                            Map<String, Object> objectMap = chidList.get(i);
                            if (i == num) {
                                infoMap.put(templateJsonModel.getField(), String.valueOf(objectMap.get(child[1])));
                            }
                        }
                    }
                } else {
                    infoMap.put(templateJsonModel.getField(), String.valueOf(dataAll1.get(templateJsonModel.getRelationField())));
                }
            }
        }

        if (StringUtil.isNotEmpty(feildValue)) {
            Map<String, String> a = new HashMap<>();
            ActionResult data = dataInterfaceService.infoToIdById(urlId, Optional.ofNullable(infoMap).orElse(new HashMap<>()));
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.getData();
            JSONArray dataAll = JsonUtil.getListToJsonArray(dataList);
            List<Map<String, Object>> datalist = new ArrayList<>();
            treeToList(id, name, children, dataAll, datalist);
            String value = feildValue.replaceAll("\\[", "").replaceAll("\\]", "");
            Map<String, Object> resultMap = new HashMap<>();
            datalist.stream().forEach(t -> {
                resultMap.put(String.valueOf(t.get(id)), String.valueOf(t.get(name)));
            });
            Object dataConversion = FormPublicUtils.getDataConversion(resultMap, feildValue, mul, "/");
            feildValue = String.valueOf(dataConversion);
        }
        return feildValue;
    }

    /**
     * 树转成list
     **/
    private void treeToList(String id, String fullName, String children, JSONArray data, List<Map<String, Object>> result) {
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject ob = data.getJSONObject(i);
                Map<String, Object> tree = new HashMap<>(16);
                tree.put(id, String.valueOf(ob.get(id)));
                tree.put(fullName, String.valueOf(ob.get(fullName)));
                result.add(tree);
                if (ob.get(children) != null) {
                    JSONArray childArray = ob.getJSONArray(children);
                    treeToList(id, fullName, children, childArray, result);
                }
            }
        }
    }

    /**
     * 生成单据规则
     *
     * @param encode
     * @param isCache
     * @return
     * @throws DataException
     */
    public String getBillNumber(String encode, Boolean isCache) throws DataException {
        return billRuleService.getBillNumber(encode, isCache);
    }

    /**
     * 功能流程 获取可视化实体
     *
     * @param visualId
     * @return
     */
    public VisualdevEntity getVisualEntity(String visualId) {
        VisualdevEntity info = visualdevService.getInfo(visualId);
        if (info != null) {
            return info;
        }
        return new VisualdevEntity();
    }

    @DS("")
    public UserEntity getUser(String userId) {
        return userService.getInfo(userId);
    }

    /**
     * 获取流程任务
     *
     * @param id
     * @param columns
     * @return
     */
    public FlowTaskEntity getInfoSubmit(String id, SFunction<FlowTaskEntity, ?>... columns) {
        return flowTaskService.getInfoSubmit(id);
    }

    public void deleteFlowTask(FlowTaskEntity flowTaskEntity) throws WorkFlowException {
        flowTaskService.delete(flowTaskEntity);
    }

    public void hasFlowTemplate(String flowId) throws WorkFlowException {
        boolean hasFlow = StringUtil.isEmpty(flowId);
        if (hasFlow) {
            throw new WorkFlowException("该功能未配置流程不可用");
        }
        FlowTemplateJsonEntity info = flowTaskService.getFlowTemplateJsonEntity(flowId);
    }

    public String getGroupSelect(String groupIds) {
        if (StringUtil.isEmpty(groupIds)) {
            return groupIds;
        }
        List<String> swapList = new ArrayList<>();
        if (groupIds.contains("[")) {
            List<String> groups = JsonUtil.getJsonToList(groupIds, String.class);
            for (String g : groups) {
                GroupEntity info = groupService.getInfo(g);
                String s = info != null ? info.getFullName() : "";
                swapList.add(s);
            }
        } else {
            GroupEntity info = groupService.getInfo(groupIds);
            swapList.add(info != null ? info.getFullName() : "");
        }
        return swapList.stream().collect(Collectors.joining(","));
    }

    public String getRoleSelect(String roleIds) {
        if (StringUtil.isEmpty(roleIds)) {
            return roleIds;
        }
        List<String> swapList = new ArrayList<>();
        if (roleIds.contains("[")) {
            List<String> groups = JsonUtil.getJsonToList(roleIds, String.class);
            for (String g : groups) {
                RoleEntity info = roleService.getInfo(g);
                String s = info != null ? info.getFullName() : "";
                swapList.add(s);
            }
        } else {
            RoleEntity info = roleService.getInfo(roleIds);
            swapList.add(info != null ? info.getFullName() : "");
        }
        return swapList.stream().collect(Collectors.joining(","));
    }


    /**
     * 高级查询
     *
     * @param conditionModel
     * @param entity
     * @param num
     * @return
     */
    public Integer getCondition(SuperQueryConditionModel conditionModel, Object entity, int num) {
        QueryWrapper<?> queryWrapper = conditionModel.getObj();
        List<ConditionJsonModel> queryConditionModels = conditionModel.getConditionList();
        String op = conditionModel.getMatchLogic();
        String tableName = conditionModel.getTableName();
        List<ConditionJsonModel> useCondition = new ArrayList<>();
        for (ConditionJsonModel queryConditionModel : queryConditionModels) {
            if (queryConditionModel.getTableName().equalsIgnoreCase(tableName)) {
                if (queryConditionModel.getField().contains("jnpf")) {
                    String child = queryConditionModel.getField();
                    String s1 = child.substring(child.lastIndexOf("jnpf_")).replace("jnpf_", "");
                    queryConditionModel.setField(s1);
                }
                if (queryConditionModel.getField().startsWith("tableField")) {
                    String child = queryConditionModel.getField();
                    String s1 = child.substring(child.indexOf("-") + 1);
                    queryConditionModel.setField(s1);
                }
                useCondition.add(queryConditionModel);
            }
        }

        if (queryConditionModels.size() < 1 || useCondition.size() < 1) {
            return num;
        }
        if (useCondition.size() > 0) {
            num += 1;
        }
        //处理控件 转换为有效值
        for (ConditionJsonModel queryConditionModel : useCondition) {
            String jnpfKey = queryConditionModel.getJnpfKey();
            String fieldValue = queryConditionModel.getFieldValue();
            if (StringUtil.isEmpty(fieldValue)) {
                if (jnpfKey.equals(JnpfKeyConsts.CASCADER) || jnpfKey.equals(JnpfKeyConsts.CHECKBOX) || jnpfKey.equals(JnpfKeyConsts.COMSELECT) || jnpfKey.equals(JnpfKeyConsts.ADDRESS)) {
                    queryConditionModel.setFieldValue("[]");
                } else {
                    queryConditionModel.setFieldValue("");
                }
                if (queryConditionModel.getSymbol().equals("like")) {
                    queryConditionModel.setSymbol("==");
                } else if (queryConditionModel.getSymbol().equals("notLike")) {
                    queryConditionModel.setSymbol("<>");
                }
            }
            if (jnpfKey.equals(JnpfKeyConsts.DATE)) {
                String startTime = "";
                if (StringUtil.isNotEmpty(fieldValue)) {
                    Long o1 = Long.valueOf(fieldValue);
                    startTime = DateUtil.daFormatHHMMSS(o1);
                }
                queryConditionModel.setFieldValue(startTime);
            } else if (jnpfKey.equals(JnpfKeyConsts.CREATETIME) || jnpfKey.equals(JnpfKeyConsts.MODIFYTIME)) {
                String startTime = "";
                if (StringUtil.isNotEmpty(fieldValue)) {
                    Long o1 = Long.valueOf(fieldValue);
                    startTime = DateUtil.daFormatHHMMSS(o1);
                }
                queryConditionModel.setFieldValue(startTime);
            }
        }
        //反射获取数据库实际字段
        Class<?> aClass = entity.getClass();

        queryWrapper.and(tw -> {
            for (ConditionJsonModel conditionJsonModel : useCondition) {
                String conditionField = conditionJsonModel.getField();
                String jnpfKey = conditionJsonModel.getJnpfKey();
                Field declaredField = null;
                try {
                    declaredField = aClass.getDeclaredField(conditionField);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                declaredField.setAccessible(true);
                String field = declaredField.getAnnotation(TableField.class).value();
                String fieldValue = conditionJsonModel.getFieldValue();
                String symbol = conditionJsonModel.getSymbol();
                if ("AND".equalsIgnoreCase(op)) {
                    if (symbol.equals("==")) {
                        tw.and(qw -> {
                                    List<String> multJnpf = new ArrayList() {{
                                        add(JnpfKeyConsts.CASCADER);
                                        add(JnpfKeyConsts.COMSELECT);
                                        add(JnpfKeyConsts.ADDRESS);
                                        add(JnpfKeyConsts.SELECT);
                                        add(JnpfKeyConsts.TREESELECT);
                                    }};
                                    if (JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || (multJnpf.contains(jnpfKey) && conditionJsonModel.isFormMultiple())) {
                                        //todo 多选，高级查询只选一个，需要拼成数组查询，其他控件目前没发现，后续添加至此
                                        String eavalue = "";
                                        if (fieldValue.contains("[")) {
                                            eavalue = "[" + fieldValue + "]";
                                        } else {
                                            JSONArray jarr = new JSONArray();
                                            jarr.add(fieldValue);
                                            eavalue = jarr.toJSONString();
                                        }
                                        qw.eq(field, eavalue);
                                    } else if (!jnpfKey.equals(JnpfKeyConsts.NUM_INPUT) && !jnpfKey.equals(JnpfKeyConsts.CALCULATE)) {
                                        qw.eq(field, fieldValue);
                                    } else {
                                        if (StringUtil.isNotEmpty(fieldValue)) {
                                            qw.eq(field, fieldValue);
                                        }
                                    }
                                    if (StringUtil.isEmpty(fieldValue)) {
                                        qw.or(
                                                ew -> ew.isNull(field)
                                        );
                                    }
                                }
                        );
                    } else if (symbol.equals(">=")) {
                        tw.ge(field, fieldValue);
                    } else if (symbol.equals("<=")) {
                        tw.and(ew -> {
                            ew.le(field, fieldValue);
                            ew.and(
                                    qw -> qw.ne(field, "")
                            );
                        });
                    } else if (symbol.equals(">")) {
                        tw.gt(field, fieldValue);
                    } else if (symbol.equals("<")) {
                        tw.and(ew -> {
                            ew.lt(field, fieldValue);
                            ew.and(
                                    qw -> qw.ne(field, "")
                            );
                        });
                    } else if (symbol.equals("<>")) {
                        tw.and(ew -> {
                            ew.ne(field, fieldValue);
                            if (StringUtil.isNotEmpty(fieldValue)) {
                                ew.or(
                                        qw -> qw.isNull(field)
                                );
                            } else {
                                ew.and(
                                        qw -> qw.isNotNull(field)
                                );
                            }
                        });
                    } else if (symbol.equals("like")) {
                        tw.and(ew -> {
                            if (StringUtil.isNotEmpty(fieldValue)) {
                                ew.like(field, fieldValue);
                            } else {
                                ew.isNull(field);
                            }
                        });
                    } else if (symbol.equals("notLike")) {
                        tw.and(ew -> {
                            if (StringUtil.isNotEmpty(fieldValue)) {
                                ew.notLike(field, fieldValue);
                                ew.or(
                                        qw -> qw.isNull(field)
                                );
                            } else {
                                ew.isNotNull(field);
                            }
                        });
                    }
                } else {
                    if (symbol.equals("==")) {
                        tw.or(
                                qw -> qw.eq(field, fieldValue)
                        );
                    } else if (symbol.equals(">=")) {
                        tw.or(
                                qw -> qw.ge(field, fieldValue)
                        );
                    } else if (symbol.equals("<=")) {
                        tw.or(
                                qw -> qw.le(field, fieldValue)
                        );
                    } else if (symbol.equals(">")) {
                        tw.or(
                                qw -> qw.gt(field, fieldValue)
                        );
                    } else if (symbol.equals("<")) {
                        tw.or(
                                qw -> qw.lt(field, fieldValue)
                        );
                    } else if (symbol.equals("<>")) {
                        tw.or(
                                qw -> qw.ne(field, fieldValue)
                        );
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            tw.or(
                                    qw -> qw.isNull(field)
                            );
                        }
                    } else if (symbol.equals("like")) {
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            tw.or(
                                    qw -> qw.like(field, fieldValue)
                            );
                        } else {
                            tw.or(
                                    qw -> qw.isNull(field)
                            );
                        }
                    } else if (symbol.equals("notLike")) {
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            tw.or(
                                    qw -> qw.notLike(field, fieldValue)
                            );
                            tw.or(
                                    qw -> qw.isNull(field)
                            );
                        } else {
                            tw.or(
                                    qw -> qw.isNotNull(field)
                            );
                        }
                    }
                }
            }
        });
        return num;
    }

    /**
     * 取主表交集
     *
     * @param lists
     * @return
     */
    public List<String> getIntersection(List<List<String>> lists) {
        if (lists == null || lists.size() == 0) {
            return new ArrayList<>();
        }
        ArrayList<List<String>> arrayList = new ArrayList<>(lists);
        for (int i = 0; i < arrayList.size(); i++) {
            List<String> list = arrayList.get(i);
            if (list == null || list.size() == 0) {
                return new ArrayList<>();
            }
        }
        List<String> intersection = arrayList.get(0);
        for (int i = 0; i < arrayList.size(); i++) {
            List<String> list = arrayList.get(i);
            intersection.retainAll(arrayList.get(i));
        }
        return intersection;
    }

    public Map<String, Object> putCache(Map<String, Object> localCache) {
        //读取系统控件 所需编码 id
        Map<String, Object> depMap = organizeService.getOrgMap("encode-name-id", "department");
        localCache.put("_dep_map", depMap);
        Map<String, Object> comMap = organizeService.getOrgMap("fullName-id", "");
        localCache.put("_com_map", comMap);
        Map<String, Object> posMap = positionService.getPosMap("fullName-id");
        localCache.put("_pos_map", posMap);
        Map<String, Object> userMap = userService.getUserMap("fullName-id");
        localCache.put("_user_map", userMap);
        Map<String, Object> roleMap = roleService.getRoleMap("fullName-id");
        localCache.put("_role_map", roleMap);
        Map<String, Object> groupMap = groupService.getGroupMap("fullName-id");
        localCache.put("_group_map", groupMap);
        return localCache;
    }


    /**
     * 时间是否在范围内
     *
     * @param jnpfField
     * @param parse
     * @return
     */
    private boolean timeInRange(JnpfField jnpfField, Date parse) {
        boolean flag = true;
        if (StringUtil.isNotEmpty(jnpfField.startTime())) {
            Long startTime = Long.parseLong(jnpfField.startTime());
            flag = parse.after(new Date(startTime));
        }
        if (flag && StringUtil.isNotEmpty(jnpfField.endTime())) {
            Long endTime = Long.parseLong(jnpfField.endTime());
            flag = parse.before(new Date(endTime));
        }
        return flag;
    }

    public String excelCheckForm(JnpfField jnpfField, String value, Map<String, Object> insMap, Map<String, Object> localCache, StringJoiner errorInfo) throws Exception {
        String jnpfKey = jnpfField.jnpfKey();
        String label = jnpfField.label();
        String vModel = jnpfField.vModel();
        if (value == null || "null".equals(value) || StringUtil.isEmpty(value)) {
            return null;
        }

        boolean multiple = jnpfField.multiple();
        if (JnpfKeyConsts.CHECKBOX.equals(jnpfKey)) {
            multiple = true;
        }

        boolean isCascaer = JnpfKeyConsts.CASCADER.equals(jnpfKey);
        /**
         * 数据接口
         */
        if (StringUtil.isNotEmpty(jnpfField.dataType())) {
            List<Map<String, Object>> options = new ArrayList<>();
            String dataLabel = jnpfField.dataLabel();
            String dataValue = jnpfField.dataValue();
            String children = jnpfField.dataChildren();
            String localCacheKey;
            Map<String, Object> dataInterfaceMap = new HashMap<>();
            List<String> dicSplict = Arrays.asList(value.split(","));
            if (!multiple) {
                if (dicSplict.size() > 1) {
                    errorInfo.add(label + "非多选");
                }
            }
            //静态数据
            if (jnpfField.dataType().equals(OnlineDataTypeEnum.STATIC.getType())) {
                localCacheKey = String.format("%s-%s", jnpfField.vModel(), OnlineDataTypeEnum.STATIC.getType());
                if (!localCache.containsKey(localCacheKey)) {
                    if (StringUtil.isNotEmpty(jnpfField.options())) {
                        options = JsonUtil.getJsonToListMap(jnpfField.options());
                        String Children = jnpfField.dataChildren();
                        JSONArray staticData = JsonUtil.getListToJsonArray(options);
                        getOptions(dataLabel, dataValue, Children, staticData, options);
                    } else {
                        options = JsonUtil.getJsonToListMap(jnpfField.options());
                    }
                    Map<String, Object> finalDataInterfaceMap = new HashMap<>(16);
                    String finalDataLabel = dataLabel;
                    String finalDataValue = dataValue;
                    options.stream().forEach(o -> {
                        finalDataInterfaceMap.put(String.valueOf(o.get(finalDataLabel)), o.get(finalDataValue));
                    });
                    localCache.put(localCacheKey, finalDataInterfaceMap);
                    dataInterfaceMap = finalDataInterfaceMap;
                } else {
                    dataInterfaceMap = (Map<String, Object>) localCache.get(localCacheKey);
                }
                swapDataUtils.checkFormDataInteface(multiple, insMap, vModel, label, dataInterfaceMap, dicSplict, errorInfo, isCascaer);
                //远端数据
            } else if (jnpfField.dataType().equals(OnlineDataTypeEnum.DYNAMIC.getType())) {
                localCacheKey = String.format("%s-%s-%s-%s", OnlineDataTypeEnum.DYNAMIC.getType(), jnpfField.propsUrl(), dataValue, dataLabel);
                if (!localCache.containsKey(localCacheKey)) {
                    ActionResult actionResult = dataInterfaceService.infoToId(jnpfField.propsUrl(),null,null);
                    if (actionResult != null && actionResult.getData() != null) {
                        List<Map<String, Object>> dycDataList = new ArrayList<>();
                        if (actionResult.getData() instanceof List) {
                            dycDataList = (List<Map<String, Object>>) actionResult.getData();
                        }
                        JSONArray dataAll = JsonUtil.getListToJsonArray(dycDataList);
                        String finalDataLabel2 = dataLabel;
                        String finalDataValue1 = dataValue;
                        treeToList(finalDataLabel2, finalDataValue1, children, dataAll, options);
                        Map<String, Object> finalDataInterfaceMap1 = new HashMap<>(16);

                        options.stream().forEach(o -> {
                            finalDataInterfaceMap1.put(String.valueOf(o.get(finalDataLabel2)), o.get(finalDataValue1));
                        });
                        dataInterfaceMap = finalDataInterfaceMap1;
                        localCache.put(localCacheKey, dataInterfaceMap);
                    }
                } else {
                    dataInterfaceMap = (Map<String, Object>) localCache.get(localCacheKey);
                }
                swapDataUtils.checkFormDataInteface(multiple, insMap, vModel, label, dataInterfaceMap, dicSplict, errorInfo, isCascaer);
                //数据字典
            } else if (jnpfField.dataType().equals(OnlineDataTypeEnum.DICTIONARY.getType())) {
                localCacheKey = String.format("%s-%s", OnlineDataTypeEnum.DICTIONARY.getType(), jnpfField.dictionaryType());
                dataLabel = jnpfField.dataLabel();
                dataValue = jnpfField.dataValue();
                if (!localCache.containsKey(localCacheKey)) {
                    List<DictionaryDataEntity> list = dictionaryDataService.getDicList(jnpfField.dictionaryType());
                    options = list.stream().map(dic -> {
                        Map<String, Object> dictionaryMap = new HashMap<>(16);
                        dictionaryMap.put("id", dic.getId());
                        dictionaryMap.put("enCode", dic.getEnCode());
                        dictionaryMap.put("fullName", dic.getFullName());
                        return dictionaryMap;
                    }).collect(Collectors.toList());
                    localCache.put(localCacheKey, options);
                } else {
                    options = (List<Map<String, Object>>) localCache.get(localCacheKey);
                }
                Map<String, Object> finalDataInterfaceMap1 = new HashMap<>(16);
                String finalDataLabel3 = dataLabel;
                String finalDataValue3 = dataValue;
                options.stream().forEach(o -> finalDataInterfaceMap1.put(String.valueOf(o.get(finalDataLabel3)), o.get(finalDataValue3)));
                swapDataUtils.checkFormDataInteface(multiple, insMap, vModel, label, finalDataInterfaceMap1, dicSplict, errorInfo, isCascaer);
            }
        }
        return errorInfo != null ? errorInfo.toString() : null;
    }

    private List<String> checkOptionsControl(boolean multiple, Map<String, Object> insMap, String vModel, String label, Map<String, Object> cacheMap, List<String> valueList, StringJoiner errInfo) {
        boolean error = false;
        if (!multiple) {
            //非多选填入多选值
            if (valueList.size() > 1) {
                error = true;
                errInfo.add(label + "非多选");
            }
        }
        List<String> dataList = new ArrayList<>();
        if (!error) {
            boolean errorHapen = false;
            for (String va : valueList) {
                Object vo = cacheMap.get(va);
                if (vo == null) {
                    errorHapen = true;
                } else {
                    dataList.add(vo.toString());
                }

            }
            if (errorHapen) {
                errInfo.add(label + "值不正确");
            } else {
                insMap.put(vModel, !multiple ? dataList.get(0) : JsonUtil.getObjectToString(dataList));
            }
        }
        return dataList;
    }

    /**
     * 递归查询
     *
     * @param label
     * @param value
     * @param Children
     * @param data
     * @param options
     */
    public static void getOptions(String label, String value, String Children, JSONArray data, List<Map<String, Object>> options) {
        for (int i = 0; i < data.size(); i++) {
            JSONObject ob = data.getJSONObject(i);
            Map<String, Object> tree = new HashMap<>(16);
            tree.put(value, String.valueOf(ob.get(value)));
            tree.put(label, String.valueOf(ob.get(label)));
            options.add(tree);
            if (ob.get(Children) != null) {
                JSONArray childrenArray = ob.getJSONArray(Children);
                getOptions(label, value, Children, childrenArray, options);
            }
        }
    }

    /**
     * 获取用户主件查询条件
     *
     * @param value
     * @return
     */
    public List<String> usersSelectQuery(String value) {
        List<String> userSList = new ArrayList<>();
        String userValue = value.substring(0, value.indexOf("--"));
        UserEntity userEntity = userService.getInfo(userValue);
        if (userEntity != null) {
            //在用户关系表中取出
            List<UserRelationEntity> groupRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.GROUP)).orElse(new ArrayList<>());
            List<UserRelationEntity> orgRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ORGANIZE)).orElse(new ArrayList<>());
            List<UserRelationEntity> posRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.POSITION)).orElse(new ArrayList<>());
            List<UserRelationEntity> roleRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ROLE)).orElse(new ArrayList<>());

            if (groupRel.size() > 0) {
                for (UserRelationEntity split : groupRel) {
                    userSList.add(split.getObjectId());
                }
            }
            if (StringUtil.isNotEmpty(userEntity.getOrganizeId())) {
                //向上递归 查出所有上级组织
                List<String> allUpOrgIDs = new ArrayList<>();
                organizeService.upWardRecursion(allUpOrgIDs, userEntity.getOrganizeId());
                for (String orgID : allUpOrgIDs) {
                    userSList.add(orgID);
                }
            }
            if (posRel.size() > 0) {
                for (UserRelationEntity split : posRel) {
                    userSList.add(split.getObjectId());
                }
            }
            if (roleRel.size() > 0) {
                for (UserRelationEntity split : roleRel) {
                    userSList.add(split.getObjectId());
                }
            }
            return userSList;
        } else {
            return null;
        }
    }

    /**
     * 获取用户主件查询条件(多选)
     *
     * @param values
     * @return
     */
    public List<String> usersSelectQuery(List<String> values) {
        List<String> userSList = new ArrayList<>();
        for (String value : values) {
            String userValue = value.substring(0, value.indexOf("--"));
            UserEntity userEntity = userService.getInfo(userValue);
            if (userEntity != null) {
                //在用户关系表中取出
                List<UserRelationEntity> groupRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.GROUP)).orElse(new ArrayList<>());
                List<UserRelationEntity> orgRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ORGANIZE)).orElse(new ArrayList<>());
                List<UserRelationEntity> posRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.POSITION)).orElse(new ArrayList<>());
                List<UserRelationEntity> roleRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ROLE)).orElse(new ArrayList<>());

                if (groupRel.size() > 0) {
                    for (UserRelationEntity split : groupRel) {
                        userSList.add(split.getObjectId());
                    }
                }
                if (StringUtil.isNotEmpty(userEntity.getOrganizeId())) {
                    //向上递归 查出所有上级组织
                    List<String> allUpOrgIDs = new ArrayList<>();
                    organizeService.upWardRecursion(allUpOrgIDs, userEntity.getOrganizeId());
                    for (String orgID : allUpOrgIDs) {
                        userSList.add(orgID);
                    }
                }
                if (posRel.size() > 0) {
                    for (UserRelationEntity split : posRel) {
                        userSList.add(split.getObjectId());
                    }
                }
                if (roleRel.size() > 0) {
                    for (UserRelationEntity split : roleRel) {
                        userSList.add(split.getObjectId());
                    }
                }
            }
        }
        return userSList;
    }

    @DS("")
    public List<RuleInfo> getFilterCondition(String id) {
        return filterService.getCondition(id);
    }

    public static List convertToList(Object obj) {
        return OnlineSwapDataUtils.convertToList(obj);
    }

    public static String convertValueToString(String obj, boolean mult, boolean isOrg) {
        return OnlineSwapDataUtils.convertValueToString(obj, mult, isOrg);
    }

    /**
     * 获取数据连接
     *
     * @param dbLink
     * @return
     */
    public DbLinkEntity getDataSource(String dbLink) {
        return dblinkService.getInfoByFullName(dbLink);
    }


    /**
     * 静态数据转换
     *
     * @param param    需要转换的值
     * @param options  静态数据模型
     * @param key      label key-value编码对应
     * @param multiple 是否多选
     * @return 转换后的值
     */
    public static String selectStaitcSwap(String param, String options, String key, String label, boolean multiple) {
        List<String> textList = new ArrayList<>();
        List<Map> optionsList = JsonUtil.getJsonToList(options, Map.class);
        if (multiple) {
            List<String> jsonToList = JsonUtil.getJsonToList(param, String.class);
            for (String list1 : jsonToList) {
                if (list1.contains("[")) {
                    List<String> textList2 = new ArrayList<>();
                    List<String> jsonToList2 = JsonUtil.getJsonToList(list1, String.class);
                    for (String str : jsonToList2) {
                        textList2.add(loop(optionsList, str, key, label));
                    }
                    textList.add(String.join("/", textList2));
                } else {
                    textList.add(loop(optionsList, list1, key, label));
                }
            }
        } else {
            if (param.contains("[")) {
                List<String> textList2 = new ArrayList<>();
                List<String> jsonToList = JsonUtil.getJsonToList(param, String.class);
                for (String str : jsonToList) {
                    textList2.add(loop(optionsList, str, key, label));
                }
                textList.add(String.join("/", textList2));
            } else {
                textList.add(loop(optionsList, param, key, label));
            }
        }
        return String.join(",", textList);
    }

    public static String loop(List<Map> options, String oneData, String key, String label) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).get(key).equals(oneData)) {
                return options.get(i).get(label).toString();
            } else if (options.get(i).get("children") != null) {
                List<Map> children = JsonUtil.getJsonToList(options.get(i).get("children"), Map.class);
                String loop = loop(children, oneData, key, label);
                if (loop != null) {
                    return loop;
                }
            }
        }
        return null;
    }

    /**
     * 功能表单获取流程信息-导入绑定多流程的第一个
     *
     * @param formId
     * @return
     * @throws WorkFlowException
     */
    public String getFlowTempJsonId(String formId) throws WorkFlowException {
        String flowTemjsonId = "";
        FlowFormEntity form = flowFormService.getById(formId);
        if (form == null || StringUtil.isEmpty(form.getFlowId())) {
            throw new WorkFlowException("该功能未配置流程不可用");
        }
        FlowTemplateInfoVO vo = flowTemplateService.info(form.getFlowId());
        if (vo == null || StringUtil.isEmpty(vo.getFlowTemplateJson()) || "[]".equals(vo.getFlowTemplateJson())) {
            throw new WorkFlowException("流程未设计！");
        }
        List<FlowJsonModel> collect = JsonUtil.getJsonToList(vo.getFlowTemplateJson(), FlowJsonModel.class);
        flowTemjsonId = collect.get(0).getId();
        return flowTemjsonId;
    }


    /**
     * 输入时表单时间字段根据格式转换去尾巴
     *
     * @param form
     */
    public static void swapDatetime(Object form) {
        Field[] declaredFields = form.getClass().getDeclaredFields();
        for (Field f : declaredFields) {
            try {
                //副表处理
                if (f.getType().getName().startsWith("jnpf.model")) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    Object o = f.get(form);
                    if (o == null) {
                        continue;
                    }
                    swapDatetime(o);
                    f.set(form, o);
                    continue;
                }
                //子表处理
                if (List.class.isAssignableFrom(f.getType())) {
                    Type type = f.getGenericType();
                    if (type instanceof ParameterizedType) {
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        List list = getList(f, f.get(form));
                        for (Object o : list) {
                            swapDatetime(o);
                        }
                        if (list.size() > 0) {
                            f.set(form, list);
                        }
                    }
                    continue;
                }
                //主表处理
                if (f.getAnnotation(JnpfField.class) == null) continue;
                JnpfField annotation = f.getAnnotation(JnpfField.class);
                if (!"date".equals(annotation.jnpfKey()) || StringUtil.isEmpty(annotation.format())) continue;
                String format = annotation.format();
                f.setAccessible(true);
                if (f.get(form) != null && Long.parseLong(String.valueOf(f.get(form))) > 0) {
                    Date date = new Date(Long.parseLong(String.valueOf(f.get(form))));
                    String completionStr = "";
                    switch (format) {
                        case "yyyy":
                            completionStr = "-01-01 00:00:00";
                            break;
                        case "yyyy-MM":
                            completionStr = "-01 00:00:00";
                            break;
                        case "yyyy-MM-dd":
                            completionStr = " 00:00:00";
                            break;
                        case "yyyy-MM-dd HH":
                            completionStr = ":00:00";
                            break;
                        case "yyyy-MM-dd HH:mm":
                            completionStr = ":00";
                            break;
                        default:
                            break;
                    }
                    String datestr = DateUtil.dateToString(date, format);
                    long time = DateUtil.stringToDate(datestr + completionStr).getTime();
                    f.set(form, String.valueOf(time));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static List getList(Field field, Object object) {
        List resultList = new ArrayList<>();
        if (object != null) {
            try {
                Class clzz = object.getClass();
                //反射调用获取到list的size方法来获取到集合的大小
                Method sizeMethod = clzz.getDeclaredMethod("size");
                if (!sizeMethod.isAccessible()) {
                    sizeMethod.setAccessible(true);
                }
                //集合长度
                int size = (int) sizeMethod.invoke(object);
                //循环遍历获取到数据
                for (int i = 0; i < size; i++) {
                    //反射获取到list的get方法
                    Method getMethod = clzz.getDeclaredMethod("get", int.class);
                    //调用get方法获取数据
                    if (!getMethod.isAccessible()) {
                        getMethod.setAccessible(true);
                    }
                    Object invoke = getMethod.invoke(object, i);
                    resultList.add(invoke);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;

    }

    /**
     * 小数转换带上0
     *
     * @param decimalValue
     */
    public static String getDecimalStr(Object decimalValue){
        if(Objects.isNull(decimalValue)){
            return "";
        }
        if (decimalValue instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) decimalValue;
            return bd.toPlainString();
        }
        return String.valueOf(decimalValue);
    }

    /**
     * 获取当前组织完整路径
     *
     * @param orgId
     * @return
     */
    public String getCurrentOrgIds(String orgId, String showLevel) {
        return flowFormDataUtil.getCurrentOrgIds(orgId, showLevel);
    }

    /**
     * 获取当前组织名称（all-显示组织名,else 显示部门名）
     *
     * @param value
     * @param showLevel
     * @return
     */
    public String getCurrentOrganizeName(Object value, String showLevel) {
        return flowFormDataUtil.getCurrentOrganizeName(value, showLevel);
    }

    /* ****************以下vue3转换信息****************** */

    /**
     * 通过副表名取副表数据map
     *
     * @param data
     * @param tableName
     */
    public static Map<String, Object> getMastTabelData(Object data, String tableName) {
        Map<String, Object> map = JsonUtil.entityToMap(data);
        Map<String, Object> mapRes = new HashMap<>();
        for (String key : map.keySet()) {
            String[] jnpf_s = key.split("_jnpf_");
            if (jnpf_s.length == 2 && jnpf_s[0].contains(tableName)) {
                mapRes.put(jnpf_s[1], map.get(key));
            }
        }
        return mapRes;
    }


    /**
     * List数据转换
     *
     * @param realList
     * @param formDataStr
     * @return
     */
    public List<Map<String, Object>> swapDataList(List<Map<String, Object>> realList, String formDataStr, String columnDataStr, String moduleId, boolean inlineEdit) {
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formDataStr, FormDataModel.class);
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnDataStr, ColumnDataModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        OnlinePublicUtils.recursionFields(fields, fieLdsModels);
        //树形-添加父级字段+_id
        if (OnlineDevData.TYPE_FIVE_COLUMNDATA.equals(columnDataModel.getType())) {
            realList.forEach(item -> {
                item.put(columnDataModel.getParentField() + "_id", item.get(columnDataModel.getParentField()));
            });
        }
        //数据转换
        realList = swapDataUtils.getSwapList(realList, fields, moduleId, inlineEdit, new ArrayList<>());
        return realList;
    }

    /**
     * List数据转树形和分组
     *
     * @param realList
     * @param columnDataStr
     * @return
     */
    public List<Map<String, Object>> swapDataList(List<Map<String, Object>> realList, String columnDataStr, String subField) {
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnDataStr, ColumnDataModel.class);
        //判断数据是否分组
        if (OnlineDevData.TYPE_THREE_COLUMNDATA.equals(columnDataModel.getType())) {
            realList = OnlineDevListUtils.groupData(realList, columnDataModel);
        }
        //树形列表
        if (OnlineDevData.TYPE_FIVE_COLUMNDATA.equals(columnDataModel.getType())) {
            columnDataModel.setSubField(subField);
            realList = OnlineDevListUtils.treeListData(realList, columnDataModel);
        }
        return realList;
    }

    /**
     * 编辑form数据转换
     *
     * @param dataMap
     * @param formDataStr
     * @return
     */
    public Map<String, Object> swapDataForm(Map<String, Object> dataMap, String formDataStr, Map<String, String> tableField, Map<String, String> tableRename) {
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formDataStr, FormDataModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        OnlinePublicUtils.recursionFields(fields, fieLdsModels);
        //数据转换
        return this.swapDataForm(dataMap, fields, null, tableField, tableRename);
    }

    private Map<String, Object> swapDataForm(Map<String, Object> dataMap, List<FieLdsModel> fields, Map<String, Object> mainMap
            , Map<String, String> tableField, Map<String, String> tableRename) {
        if (dataMap == null || dataMap.isEmpty()) return new HashMap<>();
        for (FieLdsModel item : fields) {
            String jnpfKey = item.getConfig().getJnpfKey();
            String vModel = item.getVModel();
            String dataType = item.getConfig().getDataType();
            Boolean isMultiple = Objects.nonNull(item.getMultiple()) ? item.getMultiple() : false;
            List<String> systemConditions = new ArrayList() {{
                add(JnpfKeyConsts.CURRORGANIZE);
                add(JnpfKeyConsts.CURRDEPT);
                add(JnpfKeyConsts.CURRPOSITION);
            }};
            //多选二维数组
            List<String> multTow = new ArrayList() {{
                add(JnpfKeyConsts.CASCADER);
                add(JnpfKeyConsts.CHECKBOX);
                add(JnpfKeyConsts.ADDRESS);
                add(JnpfKeyConsts.COMSELECT);
            }};
            //一维维数组
            List<String> multOne = new ArrayList() {{
                add(JnpfKeyConsts.CHECKBOX);
            }};
            if (Objects.nonNull(dataMap.get(vModel))) {
                if (multTow.contains(jnpfKey) && isMultiple) {
                    //二维数据转换
                    dataMap.replace(vModel, JSONObject.parseArray(dataMap.get(vModel).toString(), List.class));
                } else if (multTow.contains(jnpfKey) || isMultiple || multOne.contains(jnpfKey)) {
                    //一维数据转换
                    dataMap.replace(vModel, JSONObject.parseArray(dataMap.get(vModel).toString(), String.class));
                }
            } else if (!JnpfKeyConsts.CHILD_TABLE.equals(jnpfKey)) {
                if (systemConditions.contains(jnpfKey)) {
                    dataMap.put(vModel, " ");
                }
                continue;
            }
            switch (jnpfKey) {
                case JnpfKeyConsts.RATE:
                case JnpfKeyConsts.SLIDER:
                    BigDecimal value = new BigDecimal(0);
                    if (dataMap.get(vModel) != null) {
                        value = new BigDecimal(dataMap.get(vModel).toString());
                    }
                    dataMap.put(vModel, value);
                    break;
                case JnpfKeyConsts.SWITCH:
                    dataMap.put(vModel, dataMap.get(vModel) != null ? Integer.parseInt(String.valueOf(dataMap.get(vModel))) : null);
                    break;
                case JnpfKeyConsts.DATE:
                    Long dateTime = DateTimeFormatConstant.getDateObjToLong(dataMap.get(vModel));
                    dataMap.put(vModel, dateTime != null ? dateTime : dataMap.get(vModel));
                    break;
                //编辑是的系统控件转换
                case JnpfKeyConsts.CURRORGANIZE:
                case JnpfKeyConsts.CURRDEPT:
                    dataMap.put(vModel, this.getCurrentOrganizeName(dataMap.get(vModel), item.getShowLevel()));
                    break;
                case JnpfKeyConsts.CREATEUSER:
                case JnpfKeyConsts.MODIFYUSER:
                    UserEntity userEntity = userService.getInfo(String.valueOf(dataMap.get(vModel)));
                    String userValue = Objects.nonNull(userEntity) ? userEntity.getAccount().equalsIgnoreCase("admin")
                            ? "管理员/admin" : userEntity.getRealName() + "/" + userEntity.getAccount() : String.valueOf(dataMap.get(vModel));
                    dataMap.put(vModel, userValue);
                    break;
                case JnpfKeyConsts.CURRPOSITION:
                    PositionEntity positionEntity = positionService.getInfo(String.valueOf(dataMap.get(vModel)));
                    dataMap.put(vModel, Objects.nonNull(positionEntity) ? positionEntity.getFullName() : dataMap.get(vModel));
                    break;
                case JnpfKeyConsts.CREATETIME:
                case JnpfKeyConsts.MODIFYTIME:
                    if (ObjectUtil.isNotEmpty(dataMap.get(vModel))) {
                        Long dateLong = Long.parseLong(String.valueOf(dataMap.get(vModel)));
                        String dateStr = DateUtil.dateFormat(new Date(dateLong));
                        dataMap.put(vModel, dateStr);
                    }
                    break;
                case JnpfKeyConsts.UPLOADFZ:
                case JnpfKeyConsts.UPLOADIMG:
                    if (ObjectUtil.isNotEmpty(dataMap.get(vModel))) {
                        dataMap.put(vModel, JsonUtil.getJsonToListMap(dataMap.get(vModel).toString()));
                    }
                    break;
                case JnpfKeyConsts.CHILD_TABLE:
                    List<FieLdsModel> childrens = item.getConfig().getChildren();
                    String childTableRename = "";
                    try {
                        childTableRename = tableRename.get(tableField.get(vModel));
                    } catch (Exception e) {
                    }
                    if (StringUtil.isNotEmpty(childTableRename)) {
                        vModel = childTableRename + "List";
                    }
                    List<Map<String, Object>> childList = (List<Map<String, Object>>) dataMap.get(vModel);
                    if (CollectionUtils.isEmpty(childList)) break;
                    for (int i = 0; i < childList.size(); i++) {
                        Map<String, Object> childMap = childList.get(i);
                        childList.set(i, this.swapDataForm(childMap, childrens, dataMap, tableField, tableRename));
                    }
                    dataMap.put(vModel, childList);
                    break;
                default:
                    dataMap.put(vModel, dataMap.get(vModel));
                    break;
            }
        }
        return dataMap;
    }


    /**
     * 详情Detail数据转换
     *
     * @param map
     * @param formDataStr
     * @return
     */
    public Map<String, Object> swapDataDetail(Map<String, Object> map, String formDataStr, String moduleId, boolean inlineEdit) {
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formDataStr, FormDataModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        OnlinePublicUtils.recursionFields(fields, fieLdsModels);
        //数据转换
        if (map != null) {
            Map<String, Object> finalMap = map;
            List<Map<String, Object>> realList = new ArrayList() {{
                add(finalMap);
            }};
            realList = swapDataUtils.getSwapInfo(realList, fields, moduleId, inlineEdit, new ArrayList<>(),null);
            map = realList.get(0);
        }
        return map;
    }

    /**
     * 导入数据
     */
    public ExcelImportModel importData(String formData, List<Map<String, Object>> dataList, ImportFormCheckUniqueModel uniqueModel, Map<String, String> table, String tableListStr) throws WorkFlowException {
        ExcelImportModel importModel = new ExcelImportModel();
        Map<String, Object> localCache = swapDataUtils.getlocalCache();
        List<Map<String, Object>> failResult = new ArrayList<>();
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formData, FormDataModel.class);
        List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> allFieLds = new ArrayList<>();
        VisualUtils.recursionFields(fieLdsModelList, allFieLds);
        uniqueModel.setMain(true);
        uniqueModel.setPrimaryKeyPolicy(formDataModel.getPrimaryKeyPolicy());
        uniqueModel.setLogicalDelete(formDataModel.getLogicalDelete());
        uniqueModel.setMain(true);
        uniqueModel.setTableModelList(JsonUtil.getJsonToList(tableListStr, TableModel.class));
        try {
            for (Map<String, Object> data : dataList) {
                Map<String, Object> resultMap = new HashMap<>(data);
                StringJoiner errInfo = new StringJoiner(",");
                Map<String, Object> errorMap = new HashMap<>(data);
                boolean hasError = swapDataUtils.checkExcelData(allFieLds, data, localCache, resultMap, errInfo, errorMap, uniqueModel);
                if (hasError) {
                    failResult.add(errorMap);
                } else {
                    List<ImportDataModel> importDataModel = uniqueModel.getImportDataModel();
                    ImportDataModel model = new ImportDataModel();
                    model.setId(uniqueModel.getId());
                    Map<String, Map<String, Object>> map = new HashMap<>(16);
                    Map<String, Object> tableMap = new HashMap<>(16);
                    for (Object key : resultMap.keySet().toArray()) {
                        if (table.get(key) != null) {
                            tableMap.put(table.get(key) + "List", resultMap.remove(key));
                        }
                    }
                    resultMap.putAll(map);
                    resultMap.putAll(tableMap);
                    resultMap.put(FlowFormConstant.FLOWID, uniqueModel.getFlowId());
                    model.setResultData(resultMap);
                    importDataModel.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorkFlowException("导入异常！");
        }
        importModel.setFnum(failResult.size());
        importModel.setSnum(dataList.size() - failResult.size());
        importModel.setResultType(failResult.size() > 0 ? 1 : 0);
        importModel.setFailResult(failResult);
        return importModel;
    }

    public boolean checkExcelData(String formData, Map<String, Object> data, Map<String, Object> localCache, Map<String, Object> insMap, StringJoiner errInfo,
                                  Map<String, Object> errorMap, String importType, String unionId, String dbLinkId, String tableListStr) throws Exception {
        boolean hasError = false;
        try {
            FormDataModel formDataModel = JsonUtil.getJsonToBean(formData, FormDataModel.class);
            List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
            List<FieLdsModel> allFieLds = new ArrayList<>();
            VisualUtils.recursionFields(fieLdsModels, allFieLds);
            ImportFormCheckUniqueModel uniqueModel = new ImportFormCheckUniqueModel();
            uniqueModel.setMain(true);
            uniqueModel.setUpdate(Objects.equals(importType, "2"));//1、仅新增数据-2、更新和新增数据
            uniqueModel.setPrimaryKeyPolicy(formDataModel.getPrimaryKeyPolicy());
            uniqueModel.setLogicalDelete(formDataModel.getLogicalDelete());
            DbLinkEntity linkEntity = dblinkService.getInfo(dbLinkId);
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection connection = DynamicDataSourceUtil.getCurrentConnection();
            uniqueModel.setConnection(connection);
            hasError = swapDataUtils.checkExcelData(allFieLds, data, localCache, insMap, errInfo, errorMap, uniqueModel);
            if (StringUtil.isNotEmpty(uniqueModel.getId())) {
                unionId = uniqueModel.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return hasError;
    }

    /**
     * vue3获取数据过滤方案列表
     *
     * @param columnStr
     * @param appColumnStr
     * @return
     */
    public List<RuleInfo> getFilterRules(String columnStr, String appColumnStr) {
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnStr, ColumnDataModel.class);
        ColumnDataModel appColumnDataModel = JsonUtil.getJsonToBean(appColumnStr, ColumnDataModel.class);
        List<Map> ruleList = JsonUtil.getJsonToList(columnDataModel.getRuleList().getConditionList(), Map.class);
        List<Map> appRuleList = JsonUtil.getJsonToList(appColumnDataModel.getRuleList().getConditionList(), Map.class);
        boolean isPc = RequestContext.isOrignPc();
        List<RuleInfo> res = JsonUtil.getJsonToList(ruleList, RuleInfo.class);
        if (!isPc) {
            res = JsonUtil.getJsonToList(appRuleList, RuleInfo.class);
        }
        return res;
    }

    public QueryWrapper wrapperHandle(String columnStr, String appColumnStr, QueryWrapper<?> wrapper, Class<?> aClass, String type, String tableName) {
        try {
            // 避免空and
            wrapper.apply(" 1=1 ");
            List<RuleInfo> ruleInfos = getFilterRules(columnStr, appColumnStr);
            for (RuleInfo info : ruleInfos) {
                String field = info.getField();
                if ("main".equals(type) && field.contains("-")) {
                    continue;
                }
                if ("main".equals(type) && field.contains("_jnpf_")) {
                    continue;
                }
                if ("sub".equals(type) && !field.contains("-")) {
                    continue;
                }
                if ("sub-jnpf".equals(type) && !field.contains("_jnpf_")) {
                    continue;
                }
                String fieldName = field;
                String table = "";
                if (field.contains("-")) {
                    fieldName = field.split("-")[1];
                    if (!tableName.equals(field.split("-")[0])) {
                        continue;
                    }
                }
                if (field.contains("_jnpf_")) {
                    fieldName = field.split("_jnpf_")[1];
                    table = field.split("_jnpf_")[0];
                    table = table.replace("jnpf_", "");
                }
                if ("sub-jnpf".equals(type) && !tableName.equals(table)) {
                    continue;
                }
                Field declaredField = aClass.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                String fieldDb = declaredField.getAnnotation(TableField.class).value();
                GenUtil genUtil = JsonUtil.getJsonToBean(info, GenUtil.class);
                genUtil.setOperator(info.getOperator());
                genUtil.solveValue(wrapper, fieldDb);
            }
            return wrapper;
        } catch (Exception e) {
            return wrapper;
        }
    }

    /**
     * 是否只有主表过滤
     *
     * @param columnStr
     * @param appColumnStr
     * @return
     */
    public boolean onlyMainFilter(String columnStr, String appColumnStr) {
        List<RuleInfo> ruleInfos = getFilterRules(columnStr, appColumnStr);
        for (RuleInfo info : ruleInfos) {
            if (info.getField().contains("_jnpf_") || info.getField().contains("-")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 输入时表单时间字段根据格式转换去尾巴
     *
     * @param formDataStr 表单属性json
     * @param obj         数据
     */
    public static String swapDatetime(String formDataStr, Object obj,Map<String,String> tableRename) {
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formDataStr, FormDataModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        Map<String, Object> map = JsonUtil.entityToMap(obj);
        for (String tabelRealName : tableRename.keySet()) {
            String reName = DataControlUtils.initialLowercase(tableRename.get(tabelRealName));
            map.put(tabelRealName + "List", map.get(reName + "List"));
        }
        OnlineSwapDataUtils.swapDatetime(fieLdsModels, map);
        for (String tabelRealName : tableRename.keySet()) {
            if(map.get(tabelRealName + "List")!=null){
                String reName = DataControlUtils.initialLowercase(tableRename.get(tabelRealName));
                JSONArray listToJsonArray = JsonUtil.getListToJsonArray((List) map.get(tabelRealName + "List"));
                map.replace(reName + "List",listToJsonArray);
            }
        }
        return JsonUtil.getObjectToString(map);
    }

    /**
     * 三种搜索条件组合查询
     * @param queryAllModel
     * @return
     */
    @DS("")
    public MPJLambdaWrapper getConditionAllTable(QueryAllModel queryAllModel){
        try {
            DbLinkEntity linkEntity = getDataSource(queryAllModel.getDbLink());
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection connection = ConnUtil.getConnOrDefault(linkEntity);
            queryAllModel.setDbType(connection.getMetaData().getDatabaseProductName().trim());
        } catch (Exception e) {
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        MPJLambdaWrapper wrapper = queryAllModel.getWrapper();
        List<List<SuperJsonModel>> superList = new ArrayList<>();

        //高级查询
        String superQuery = queryAllModel.getSuperJson();
        if (StringUtil.isNotEmpty(superQuery)) {
            List<SuperJsonModel> list = new ArrayList<>();
            SuperJsonModel jsonToBean = JsonUtil.getJsonToBean(queryAllModel.getSuperJson(), SuperJsonModel.class);
            list.add(jsonToBean);
            superList.add(list);
        }
        //数据过滤
        String ruleQuery = queryAllModel.getRuleJson();
        if (StringUtil.isNotEmpty(ruleQuery)) {
            List<SuperJsonModel> list = new ArrayList<>();
            SuperJsonModel jsonToBean = JsonUtil.getJsonToBean(queryAllModel.getRuleJson(), SuperJsonModel.class);
            list.add(jsonToBean);
            superList.add(list);
        }
        //数据权限  不为空有开启权限（存在多权限--权限方案之间用or，和其他条件之间用and）
        if(queryAllModel.getModuleId()!=null){
            List<SuperJsonModel> authorizeListAll = authorizeService.getConditionSql(queryAllModel.getModuleId());
            if (authorizeListAll.size() > 0) {
                superList.add(authorizeListAll);
            }
        }
        QueryUtil queryUtil=new QueryUtil();
        queryAllModel.setQueryList(superList);
        queryUtil.queryList(queryAllModel);
        return wrapper;
    }

}
