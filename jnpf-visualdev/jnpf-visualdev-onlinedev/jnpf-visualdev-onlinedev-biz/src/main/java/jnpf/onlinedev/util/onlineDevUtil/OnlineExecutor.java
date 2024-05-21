package jnpf.onlinedev.util.onlineDevUtil;

import com.alibaba.fastjson.JSONArray;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.model.datainterface.DataInterfaceModel;
import jnpf.base.model.datainterface.DataInterfacePage;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.ProvinceService;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.TemplateJsonModel;
import jnpf.onlinedev.model.OnlineDevEnum.CacheKeyEnum;
import jnpf.onlinedev.model.OnlineDevEnum.OnlineDataTypeEnum;
import jnpf.permission.service.GroupService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.data.DataSourceContextHolder;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static jnpf.onlinedev.util.onlineDevUtil.OnlineSwapDataUtils.NEEDCACHE_REMOTE;

/**
 * 在线开发数据缓存获取，多线程
 *
 * @author JNPF开发平台组
 * @version V3.6.x
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2023/12/19
 */
@Component
@Slf4j
public class OnlineExecutor {
    @Autowired
    private Executor executor;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ProvinceService areaService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DataInterfaceService dataInterFaceService;

    private String dsName = "";
    private static long DEFAULT_CACHE_TIME = OnlineSwapDataUtils.DEFAULT_CACHE_TIME;
    private static final String KEY_USER = "user";
    private static final String KEY_ORG = "org";
    private static final String KEY_POS = "pos";
    private static final String KEY_ROLE = "role";
    private static final String KEY_GROUP = "group";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_POP = "pop";
    private static final String KEY_SELECT = "select";
    private static final String KEY_DATATYPE = "datatype";

    /**
     * 遍历需要多线程缓存
     */
    public void executorRedis(Map<String, Object> localCache, List<FieLdsModel> swapDataVoList, String visualDevId, Boolean inlineEdit,
                              List<Map<String, Object>> list, Map<String, Object> mainAndMast) {
        dsName = Optional.ofNullable(DataSourceContextHolder.getDatasourceId()).orElse("");
        Map<String, OnlineExecutorParam> listExecutor = new HashMap<>();
        for (int x = 0; x < list.size(); x++) {
            Map<String, Object> dataMap = list.get(x);
            if (dataMap == null) {
                continue;
            }
            for (FieLdsModel swapDataVo : swapDataVoList) {
                String jnpfKey = swapDataVo.getConfig().getJnpfKey();
                if (StringUtil.isEmpty(swapDataVo.getVModel())) {
                    continue;
                }
                String swapVModel = swapDataVo.getVModel();
                String dataType = swapDataVo.getConfig().getDataType();
                String redisKey;
                boolean needUser = false, needOrg = false, needPos = false, needRole = false, needGroup = false, needProvince = false;

                switch (jnpfKey) {
                    //用户组件
                    case JnpfKeyConsts.USERSELECT:
                        //创建用户
                    case JnpfKeyConsts.CREATEUSER:
                        //修改用户
                    case JnpfKeyConsts.MODIFYUSER:
                        needUser = true;
                        break;
                    //公司组件
                    case JnpfKeyConsts.COMSELECT:
                        //部门组件
                    case JnpfKeyConsts.DEPSELECT:
                        //所属部门
                    case JnpfKeyConsts.CURRDEPT:
                        //所属组织
                    case JnpfKeyConsts.CURRORGANIZE:
                        needOrg = true;
                        break;
                    //岗位组件
                    case JnpfKeyConsts.POSSELECT:
                        //所属岗位
                    case JnpfKeyConsts.CURRPOSITION:
                        needPos = true;
                        break;
                    //角色选择
                    case JnpfKeyConsts.ROLESELECT:
                        needRole = true;
                        break;
                    //分组选择
                    case JnpfKeyConsts.GROUPSELECT:
                        needGroup = true;
                        break;
                    //用户选择组件
                    case JnpfKeyConsts.CUSTOMUSERSELECT:
                        needUser = needOrg = needPos = needGroup = needRole = true;
                        break;
                    //省市区选择组件
                    case JnpfKeyConsts.ADDRESS:
                        needProvince = true;
                        break;
                    case JnpfKeyConsts.POPUPSELECT:
                    case JnpfKeyConsts.POPUPTABLESELECT:
                        List<TemplateJsonModel> templateJsonModels = JsonUtil.getJsonToList(swapDataVo.getTemplateJson(), TemplateJsonModel.class);
                        String value = String.valueOf(dataMap.get(swapVModel));
                        List<DataInterfaceModel> listParam = new ArrayList<>();
                        for (TemplateJsonModel templateJsonModel : templateJsonModels) {
                            String relationField = templateJsonModel.getRelationField();
                            DataInterfaceModel dataInterfaceModel = JsonUtil.getJsonToBean(templateJsonModel, DataInterfaceModel.class);
                            if (StringUtil.isEmpty(relationField)) {
                                listParam.add(dataInterfaceModel);
                                continue;
                            }
                            String obj = inlineEdit ? "" : Optional.ofNullable(dataMap.get(relationField)).orElse("").toString();
                            if (relationField.toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                                String childField = relationField.split("-")[1];
                                obj = Optional.ofNullable(dataMap.get(childField)).orElse("").toString();
                            } else if (mainAndMast != null) {
                                obj = Optional.ofNullable(mainAndMast.get(relationField)).orElse("").toString();
                            }
                            dataInterfaceModel.setDefaultValue(obj);
                            listParam.add(dataInterfaceModel);
                        }
                        DataInterfacePage dataInterfacePage = new DataInterfacePage();
                        dataInterfacePage.setParamList(listParam);
                        dataInterfacePage.setInterfaceId(swapDataVo.getInterfaceId());
                        List<String> ids = new ArrayList<>();
                        if (value.startsWith("[")) {
                            ids = JsonUtil.getJsonToList(value, String.class);
                        } else {
                            ids.add(value);
                        }
                        dataInterfacePage.setIds(ids);
                        //缓存Key 租户-远端数据-base64({id, params, ids})
                        redisKey = String.format("%s-%s-%s-%s", dsName, OnlineDataTypeEnum.DYNAMIC.getType(), swapDataVo.getInterfaceId(),
                                Base64.getEncoder().encodeToString(JsonUtil.getObjectToString(dataInterfacePage).getBytes(StandardCharsets.UTF_8)));

                        if (!localCache.containsKey(redisKey)) {
                            dataInterfacePage.setPropsValue(swapDataVo.getPropsValue());
                            dataInterfacePage.setRelationField(swapDataVo.getRelationField());
                            listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_POP, swapDataVo.getInterfaceId(), dataInterfacePage));
                        }
                        break;
                    case JnpfKeyConsts.CASCADER:
                    case JnpfKeyConsts.RADIO:
                    case JnpfKeyConsts.CHECKBOX:
                    case JnpfKeyConsts.SELECT:
                    case JnpfKeyConsts.TREESELECT:
                        //动态
                        List<TemplateJsonModel> templateList = JsonUtil.getJsonToList(swapDataVo.getConfig().getTemplateJson(), TemplateJsonModel.class);
                        if (templateList.size() > 0) {
                            Map<String, String> paramMap = new HashMap<>();
                            for (TemplateJsonModel templateJsonModel : templateList) {
                                String relationField = templateJsonModel.getRelationField();
                                String Field = templateJsonModel.getField();
                                String obj = inlineEdit ? "" : Optional.ofNullable(dataMap.get(relationField)).orElse("").toString();
                                if (StringUtil.isEmpty(relationField)) {
                                    paramMap.put(Field, templateJsonModel.getDefaultValue());
                                    continue;
                                }
                                if (relationField.toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                                    String childField = relationField.split("-")[1];
                                    obj = Optional.ofNullable(dataMap.get(childField)).orElse("").toString();
                                } else if (mainAndMast != null) {
                                    obj = Optional.ofNullable(mainAndMast.get(relationField)).orElse("").toString();
                                }
                                paramMap.put(Field, obj);
                            }
                            //缓存Key 租户-远端数据-id-base64({params})
                            redisKey = String.format("%s-%s-%s-%s", dsName, OnlineDataTypeEnum.DYNAMIC.getType(), swapDataVo.getConfig().getPropsUrl(),
                                    Base64.getEncoder().encodeToString(JsonUtil.getObjectToString(paramMap).getBytes(StandardCharsets.UTF_8)));

                            if (!localCache.containsKey(redisKey)) {
                                listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_SELECT, swapDataVo.getConfig().getPropsUrl(), paramMap));
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (dataType != null) {
                    //数据接口的数据存放
                    String label = swapDataVo.getProps().getLabel() != null ? swapDataVo.getProps().getLabel() : "";
                    String value = swapDataVo.getProps().getValue() != null ? swapDataVo.getProps().getValue() : "";
                    String children = swapDataVo.getProps().getChildren() != null ? swapDataVo.getProps().getChildren() : "";
                    if (swapDataVo.getConfig().getJnpfKey().equals(JnpfKeyConsts.POPUPSELECT) || swapDataVo.getConfig().getJnpfKey().equals(JnpfKeyConsts.POPUPTABLESELECT)) {
                        label = swapDataVo.getRelationField();
                        value = swapDataVo.getPropsValue();
                    }
                    //静态数据
                    if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())) {
                        redisKey = String.format("%s-%s-%s", visualDevId, swapDataVo.getConfig().getRelationTable() + swapDataVo.getVModel(), OnlineDataTypeEnum.STATIC.getType());
                        if (!localCache.containsKey(redisKey)) {
                            listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_DATATYPE, null, swapDataVo));
                        }
                    }
                    //远端数据
                    if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())) {
                        //联动状态下不做缓存， 具体查数据时做缓存
                        boolean dynamicIsNeedCache = swapDataVo.getConfig().getTemplateJson().size() == 0;
                        if (dynamicIsNeedCache) {
                            redisKey = String.format("%s-%s-%s-%s-%s-%s", dsName, OnlineDataTypeEnum.DYNAMIC.getType(), swapDataVo.getConfig().getPropsUrl(), value, label, children);
                            if (!localCache.containsKey(redisKey)) {
                                listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_DATATYPE, null, swapDataVo));
                            }
                        }
                    }
                    //数据字典
                    if (dataType.equals(OnlineDataTypeEnum.DICTIONARY.getType())) {
                        redisKey = String.format("%s-%s-%s", dsName, OnlineDataTypeEnum.DICTIONARY.getType(), swapDataVo.getConfig().getDictionaryType());
                        if (!localCache.containsKey(redisKey)) {
                            listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_DATATYPE, null, swapDataVo));
                        }
                    }
                }

                if (needUser) {
                    //人员
                    redisKey = dsName + CacheKeyEnum.USER.getName();
                    if (!localCache.containsKey(redisKey)) {
                        listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_USER, null, null));
                    }
                }
                if (needOrg) {
                    //组织
                    redisKey = dsName + CacheKeyEnum.ORG.getName();
                    if (!localCache.containsKey(redisKey)) {
                        listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_ORG, null, null));
                    }
                }
                if (needPos) {
                    //岗位
                    redisKey = dsName + CacheKeyEnum.POS.getName();
                    if (!localCache.containsKey(redisKey)) {
                        listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_POS, null, null));
                    }
                }
                if (needRole) {
                    //角色
                    redisKey = dsName + CacheKeyEnum.ROLE.getName();
                    if (!localCache.containsKey(redisKey)) {
                        listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_ROLE, null, null));
                    }
                }
                if (needGroup) {
                    //分组
                    redisKey = dsName + CacheKeyEnum.GROUP.getName();
                    if (!localCache.containsKey(redisKey)) {
                        listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_GROUP, null, null));
                    }
                }
                //地区数据过大, 取消缓存
                /*if (needProvince) {
                    //省市区
                    redisKey = String.format("%s-%s-%d", dsName, "province", 1);
                    if (!localCache.containsKey(redisKey)) {
                        listExecutor.putIfAbsent(redisKey, new OnlineExecutorParam(redisKey, KEY_PROVINCE, null, null));
                    }
                }*/

            }
        }
        //执行多线程方法
        if (!listExecutor.isEmpty()) {
            this.execute(localCache, listExecutor);
        }
    }

    /**
     * 执行多线程
     */
    private void execute(Map<String, Object> localCache, Map<String, OnlineExecutorParam> listExecutor) {
        CountDownLatch countDownLatch = new CountDownLatch(listExecutor.size());
        for (String key : listExecutor.keySet()) {
            OnlineExecutorParam item = listExecutor.get(key);
            String redisKey = item.getRedisKey();
            executor.execute(() -> {
                try {
                    switch (item.getType()) {
                        case KEY_USER:
                            //人员
                            Map<String, Object> userMap;
                            if (redisUtil.exists(redisKey)) {
                                userMap = redisUtil.getMap(redisKey);
                                userMap = Optional.ofNullable(userMap).orElse(new HashMap<>(20));
                            } else {
                                userMap = userService.getUserMap("id-fullName");
                                if (OnlineSwapDataUtils.NEEDCACHE_SYS) {
                                    redisUtil.insert(redisKey, userMap, DEFAULT_CACHE_TIME);
                                }
                            }
                            localCache.put("__user_map", userMap);
                            break;
                        case KEY_ORG:
                            Map<String, Object> orgMap;
                            if (redisUtil.exists(redisKey)) {
                                orgMap = redisUtil.getMap(redisKey);
                                orgMap = Optional.ofNullable(orgMap).orElse(new HashMap<>(20));
                            } else {
                                orgMap = organizeService.getOrgMap("id-fullName", "");
                                if (OnlineSwapDataUtils.NEEDCACHE_SYS) {
                                    redisUtil.insert(redisKey, orgMap, DEFAULT_CACHE_TIME);
                                }
                            }
                            localCache.put("__org_map", orgMap);
                            break;
                        case KEY_POS:
                            Map<String, Object> posMap;
                            if (redisUtil.exists(redisKey)) {
                                posMap = redisUtil.getMap(redisKey);
                                posMap = Optional.ofNullable(posMap).orElse(new HashMap<>(20));
                            } else {
                                posMap = positionService.getPosMap("id-fullName");
                                if (OnlineSwapDataUtils.NEEDCACHE_SYS) {
                                    redisUtil.insert(redisKey, posMap, DEFAULT_CACHE_TIME);
                                }
                            }
                            localCache.put("__pos_map", posMap);
                            break;
                        case KEY_ROLE:
                            Map<String, Object> roleMap;
                            if (redisUtil.exists(redisKey)) {
                                roleMap = redisUtil.getMap(redisKey);
                                roleMap = Optional.ofNullable(roleMap).orElse(new HashMap<>(20));
                            } else {
                                roleMap = roleService.getRoleMap("id-fullName");
                                if (OnlineSwapDataUtils.NEEDCACHE_SYS) {
                                    redisUtil.insert(redisKey, roleMap, DEFAULT_CACHE_TIME);
                                }
                            }
                            localCache.put("__role_map", roleMap);
                            break;
                        case KEY_GROUP:
                            Map<String, Object> groupMap;
                            if (redisUtil.exists(redisKey)) {
                                groupMap = redisUtil.getMap(redisKey);
                                groupMap = Optional.ofNullable(groupMap).orElse(new HashMap<>(20));
                            } else {
                                groupMap = groupService.getGroupMap("id-fullName");
                                if (OnlineSwapDataUtils.NEEDCACHE_SYS) {
                                    redisUtil.insert(redisKey, groupMap, DEFAULT_CACHE_TIME);
                                }
                            }
                            localCache.put("__group_map", groupMap);
                            break;
                        case KEY_PROVINCE:
                            //省市区
                            Map<Object, Object> proMap = redisUtil.getMap(redisKey);
                            List<Map<String, String>> proMapList = new ArrayList<>();
                            if (proMap.size() == 0) {
                                //分级存储
                                for (int i = 1; i <= 4; i++) {
                                    String redisKeyEach = String.format("%s-%s-%d", dsName, "province", i);
                                    if (!redisUtil.exists(redisKeyEach)) {
                                        List<ProvinceEntity> provinceEntityList = areaService.getProListBytype(String.valueOf(i));
                                        Map<String, String> provinceMap = new HashMap<>(16);
                                        if (provinceEntityList != null) {
                                            provinceEntityList.stream().forEach(p -> provinceMap.put(p.getId(), p.getFullName()));
                                        }
                                        proMapList.add(provinceMap);
                                        //区划基本不修改 不做是否缓存判断
                                        redisUtil.insert(redisKeyEach, provinceMap, RedisUtil.CAHCEWEEK);
                                    }
                                }
                            } else {
                                for (int i = 1; i <= 4; i++) {
                                    proMapList.add(redisUtil.getMap(String.format("%s-%s-%d", dsName, "province", i)));
                                }
                            }
                            localCache.put("__pro_maplist", proMapList);
                            break;
                        case KEY_POP:
                            List<Map<String, Object>> mapList = null;
                            if (!redisUtil.exists(redisKey)) {
                                mapList = dataInterFaceService.infoByIds(item.getInterfaceId(), (DataInterfacePage) item.getParam()).getData();
                                if (NEEDCACHE_REMOTE && mapList != null && mapList.size() > 0) {
                                    redisUtil.insert(item.getRedisKey(), mapList, DEFAULT_CACHE_TIME);
                                }
                            } else {
                                List<Object> tmpList = redisUtil.get(redisKey, 0, -1);
                                List<Map<String, Object>> tmpMapList = new ArrayList<>();
                                tmpList.forEach(itemx -> {
                                    tmpMapList.add(JsonUtil.entityToMap(itemx));
                                });
                                mapList = tmpMapList;
                            }
                            localCache.put(item.getRedisKey(), mapList);
                            break;
                        case KEY_SELECT:
                            List<Map<String, Object>> dataList = null;
                            if (!redisUtil.exists(redisKey)) {
                                ActionResult data = dataInterFaceService.infoToIdById(item.getInterfaceId(), (Map) item.getParam());
                                if (data != null && data.getData() != null) {
                                    if (data.getData() instanceof List) {
                                        dataList = (List<Map<String, Object>>) data.getData();
                                        if (NEEDCACHE_REMOTE && CollectionUtils.isNotEmpty(dataList)) {
                                            redisUtil.insert(redisKey, dataList, DEFAULT_CACHE_TIME);
                                        }
                                    }
                                }
                            } else {
                                List<Object> tmpList = redisUtil.get(redisKey, 0, -1);
                                List<Map<String, Object>> tmpMapList = new ArrayList<>();
                                tmpList.forEach(itemx -> {
                                    tmpMapList.add(JsonUtil.entityToMap(itemx));
                                });
                                dataList = tmpMapList;
                            }
                            localCache.put(redisKey, dataList);
                            break;
                        case KEY_DATATYPE:
                            //数据接口的数据存放
                            FieLdsModel swapDataVo = (FieLdsModel) item.getParam();
                            String dataType = swapDataVo.getConfig().getDataType();
                            String label = swapDataVo.getProps().getLabel() != null ? swapDataVo.getProps().getLabel() : "";
                            String value = swapDataVo.getProps().getValue() != null ? swapDataVo.getProps().getValue() : "";
                            String children = swapDataVo.getProps().getChildren() != null ? swapDataVo.getProps().getChildren() : "";
                            List<Map<String, Object>> options = new ArrayList<>();
                            if (swapDataVo.getConfig().getJnpfKey().equals(JnpfKeyConsts.POPUPSELECT) || swapDataVo.getConfig().getJnpfKey().equals(JnpfKeyConsts.POPUPTABLESELECT)) {
                                label = swapDataVo.getRelationField();
                                value = swapDataVo.getPropsValue();
                            }
                            Map<String, String> dataInterfaceMap = new HashMap<>(16);
                            String finalValue = value;
                            String finalLabel = label;
                            //静态数据
                            if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())) {
                                if (!localCache.containsKey(redisKey)) {
                                    if (!redisUtil.exists(redisKey)) {
                                        if (swapDataVo.getOptions() != null) {
                                            options = JsonUtil.getJsonToListMap(swapDataVo.getOptions());
                                            OnlineSwapDataUtils.getOptions(label, value, children, JsonUtil.getListToJsonArray(options), options);
                                        } else {
                                            options = JsonUtil.getJsonToListMap(swapDataVo.getOptions());
                                        }

                                        options.stream().forEach(o -> {
                                            dataInterfaceMap.put(String.valueOf(o.get(finalValue)), String.valueOf(o.get(finalLabel)));
                                        });
                                        if (NEEDCACHE_REMOTE) {
                                            redisUtil.insert(redisKey, dataInterfaceMap, DEFAULT_CACHE_TIME);
                                        }
                                        localCache.put(redisKey, dataInterfaceMap);
                                    } else {
                                        localCache.put(redisKey, redisUtil.getMap(redisKey));
                                    }
                                }
                            }
                            //远端数据
                            if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())) {
                                //联动状态下不做缓存， 具体查数据时做缓存
                                boolean dynamicIsNeedCache = swapDataVo.getConfig().getTemplateJson().size() == 0;
                                if (dynamicIsNeedCache) {
                                    if (!localCache.containsKey(redisKey)) {
                                        if (!redisUtil.exists(redisKey)) {
                                            ActionResult dataRes = dataInterFaceService.infoToId(swapDataVo.getConfig().getPropsUrl(),null,null);
                                            if (dataRes != null && dataRes.getData() != null) {
                                                List<Map<String, Object>> dataList2 = new ArrayList<>();
                                                if (dataRes.getData() instanceof List) {
                                                    dataList2 = (List<Map<String, Object>>) dataRes.getData();
                                                }
                                                JSONArray dataAll = JsonUtil.getListToJsonArray(dataList2);
                                                OnlineSwapDataUtils.treeToList(label, value, children, dataAll, options);
                                                options.stream().forEach(o -> {
                                                    dataInterfaceMap.put(String.valueOf(o.get(finalValue)), String.valueOf(o.get(finalLabel)));
                                                });
                                                if (NEEDCACHE_REMOTE && CollectionUtils.isNotEmpty(dataList2)) {
                                                    redisUtil.insert(redisKey, dataInterfaceMap, DEFAULT_CACHE_TIME);
                                                }
                                                localCache.put(redisKey, dataInterfaceMap);
                                            }
                                        } else {
                                            localCache.put(redisKey, redisUtil.getMap(redisKey));
                                        }
                                    }
                                }
                            }
                            //数据字典
                            if (dataType.equals(OnlineDataTypeEnum.DICTIONARY.getType())) {
                                if (!localCache.containsKey(redisKey)) {
                                    if (!redisUtil.exists(redisKey)) {
                                        List<DictionaryDataEntity> list = dictionaryDataService.getDicList(swapDataVo.getConfig().getDictionaryType());
                                        options = list.stream().map(dic -> {
                                            Map<String, Object> dictionaryMap = new HashMap<>(16);
                                            dictionaryMap.put("id", dic.getId());
                                            dictionaryMap.put("enCode", dic.getEnCode());
                                            dictionaryMap.put("fullName", dic.getFullName());
                                            return dictionaryMap;
                                        }).collect(Collectors.toList());
                                        String dictionaryData = JsonUtil.getObjectToString(options);
                                        if (NEEDCACHE_REMOTE) {
                                            redisUtil.insert(redisKey, dictionaryData, DEFAULT_CACHE_TIME);
                                        }
                                        localCache.put(redisKey, options);
                                    } else {
                                        String dictionaryStringData = redisUtil.getString(redisKey).toString();
                                        localCache.put(redisKey, JsonUtil.getJsonToListMap(dictionaryStringData));
                                    }
                                }
                            }
                            break;
                        default:

                            break;
                    }
                } catch (Exception e) {
                    log.error("线程执行错误：" + e.getMessage());
//                    e.printStackTrace();
                } finally {
                    //每执行一次数值减少一
                    countDownLatch.countDown();
                    //也可以给await()设置超时时间，如果超过300s（也可以是时，分）则不再等待，直接执行下面代码。
                    //countDownLatch.await(300,TimeUnit.SECONDS);
                }
            });
        }

        try {
            //等待计数器归零
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("线程计数错误：" + e.getMessage());
//            e.printStackTrace();
        }
    }
}

@Data
class OnlineExecutorParam {
    private String redisKey;
    private String type;
    private String interfaceId;
    private Object param;

    public OnlineExecutorParam(String redisKey, String type, String interfaceId, Object param) {
        this.redisKey = redisKey;
        this.type = type;
        this.interfaceId = interfaceId;
        this.param = param;
    }
}
