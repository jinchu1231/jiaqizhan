package jnpf.onlinedev.util.onlineDevUtil;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.datainterface.DataInterfaceActionVo;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.ProvinceService;
import jnpf.base.service.VisualdevService;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.analysis.FormModel;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.onlinedev.model.OnlineDevEnum.MultipleControlEnum;
import jnpf.onlinedev.model.OnlineDevEnum.OnlineDataTypeEnum;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualDevInfoService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.permission.entity.GroupEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.GroupService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserService;
import jnpf.util.DateTimeFormatConstant;
import jnpf.util.DateUtil;
import jnpf.util.FormInfoUtils;
import jnpf.util.FormPublicUtils;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jnpf.util.Constants.ADMIN_KEY;

/**
 * 在线详情编辑工具类
 *
 * @author JNPF开发平台组
 * @version V3.2
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/10/27
 */
@Slf4j
@Component
public class OnlineDevInfoUtils {

    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private UserService userService;
    @Autowired
    private FormInfoUtils formInfoUtils;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;
    @Autowired
    private DataInterfaceService dataInterFaceService;
    @Autowired
    private VisualDevInfoService visualDevInfoService;
    @Autowired
    private ProvinceService areaService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;

    private Map<String, String> nullDatamap = new HashMap<>();

    /**
     * 数据转换(不取缓存)
     *
     * @param modelList
     * @param dataMap
     * @return
     */
    public Map<String, Object> swapChildTableDataInfo(List<FieLdsModel> modelList, Map<String, Object> dataMap, List<FormModel> codeList) {
        Map<String, Object> dataCopyMap = new HashMap<>();
        dataCopyMap.putAll(dataMap);

        Map<String, Map<String, Object>> dataDetailMap = new HashMap<>();
        try {
            for (FieLdsModel swapDataVo : modelList) {
                String jnpfKey = swapDataVo.getConfig().getJnpfKey();
                String dataType = swapDataVo.getConfig().getDataType();
                String vModel = swapDataVo.getVModel();
                Object val = dataMap.get(vModel);
                String modelValue = String.valueOf(val);
                if (StringUtil.isEmpty(modelValue) || "null".equals(modelValue)) {
                    continue;
                }
                if (dataType != null) {
                    //数据接口的数据存放
                    String label = swapDataVo.getProps() != null ? swapDataVo.getProps().getLabel() : "" ;
                    String value = swapDataVo.getProps() != null ? swapDataVo.getProps().getValue() : "" ;
                    String Children = swapDataVo.getProps() != null ? swapDataVo.getProps().getChildren() : "" ;
                    List<Map<String, Object>> options = new ArrayList<>();

                    if (dataType.equals(OnlineDataTypeEnum.STATIC.getType())) {
                        if (StringUtil.isNotEmpty(swapDataVo.getOptions())) {
                            options = JsonUtil.getJsonToListMap(swapDataVo.getOptions());

                            JSONArray data = JsonUtil.getListToJsonArray(options);
                            OnlineDevListUtils.getOptions(label, value, Children, data, options);
                        } else {
                            options = JsonUtil.getJsonToListMap(swapDataVo.getOptions());
                        }
                    }
                    if (dataType.equals(OnlineDataTypeEnum.DYNAMIC.getType())) {
                        ActionResult data = dataInterFaceService.infoToId(swapDataVo.getInterfaceId(),null, nullDatamap);
                        //api调用 序列化为linkedHashMap
                        LinkedHashMap<String, List<Map<String, Object>>> actionVo = (LinkedHashMap<String, List<Map<String, Object>>>) data.getData();
                        if (actionVo != null) {
                            List<Map<String, Object>> dataList = actionVo.get("data" );
                            JSONArray dataAll = JsonUtil.getListToJsonArray(dataList);
                            treeToList(label, value, Children, dataAll, options);
                        }
                    }
                    if (dataType.equals(OnlineDataTypeEnum.DICTIONARY.getType())) {
                        List<DictionaryDataEntity> list = dictionaryDataService.getDicList(swapDataVo.getConfig().getDictionaryType());
                        options = list.stream().map(dic -> {
                            Map<String, Object> dictionaryMap = new HashMap<>(16);
                            dictionaryMap.put("id" , dic.getId());
                            dictionaryMap.put("enCode" , dic.getEnCode());
                            dictionaryMap.put("fullName" , dic.getFullName());
                            return dictionaryMap;
                        }).collect(Collectors.toList());
                    }

                    Map<String, String> dataInterfaceMap = new HashMap<>(16);
                    options.stream().forEach(o -> {
                        dataInterfaceMap.put(String.valueOf(o.get(value)), String.valueOf(o.get(label)));
                    });

                    List<String> valueList = new ArrayList<>();
                    if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
                        String[][] data = JsonUtil.getJsonToBean(modelValue, String[][].class);
                        for (String[] casData : data) {
                            for (String s : casData) {
                                valueList.add(s);
                            }
                        }
                    } else if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
                        valueList = JsonUtil.getJsonToList(modelValue, String.class);
                    } else {
                        valueList.add(modelValue);
                    }
                    String allValue = valueList.stream().map(va -> dataInterfaceMap.get(va)).collect(Collectors.joining("," ));
                    dataMap.put(vModel, allValue);
                } else {
                    switch (jnpfKey) {
                        //公司组件
                        case JnpfKeyConsts.COMSELECT:
                            //部门组件
                        case JnpfKeyConsts.DEPSELECT:
                            //所属部门
                        case JnpfKeyConsts.CURRDEPT:
                            dataMap.put(vModel, getOrgValue(modelValue));
                            break;

                        //所属组织
                        case JnpfKeyConsts.CURRORGANIZE:
                            boolean isAll = "all".equals(swapDataVo.getShowLevel());
                            if (isAll) {
                                List<OrganizeEntity> organizeList = new ArrayList<>();
                                organizeService.getOrganizeId(modelValue,organizeList);
                                Collections.reverse(organizeList);
                                String value = organizeList.stream().map(OrganizeEntity::getFullName).collect(Collectors.joining("/" ));
                                dataMap.put(vModel, value);
                            } else {
                                OrganizeEntity organizeEntity = organizeService.getInfo(modelValue);
                                dataMap.put(vModel, Objects.nonNull(organizeEntity) ? organizeEntity.getFullName() : modelValue);
                            }
                            break;

                        //岗位组件
                        case JnpfKeyConsts.POSSELECT:
                            //所属岗位
                        case JnpfKeyConsts.CURRPOSITION:
                            dataMap.put(vModel, getPosValue(modelValue));
                            break;

                        //用户组件
                        case JnpfKeyConsts.USERSELECT:
                            //创建用户
                        case JnpfKeyConsts.CREATEUSER:
                            //修改用户
                        case JnpfKeyConsts.MODIFYUSER:
                            if (ADMIN_KEY.equals(modelValue)){
                                dataMap.put(vModel, "管理员");
                            } else {
                                dataMap.put(vModel,getUserValue(modelValue));
                            }
                            break;

                        //省市区联动
                        case JnpfKeyConsts.ADDRESS:
                            String value = String.valueOf(dataMap.get(vModel));
                            if (OnlinePublicUtils.getMultiple(value, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
                                String[][] data = JsonUtil.getJsonToBean(value, String[][].class);
                                List<String> addList = new ArrayList<>();
                                for (String[] AddressData : data) {
                                    List<String> adList = new ArrayList<>();
                                    for (String s : AddressData) {
                                        adList.add(s);
                                    }
                                    addList.add(String.join("/" , areaService.getProList(adList).stream().map(pro -> pro.getFullName()).collect(Collectors.toList())));
                                }
                                dataMap.put(vModel, String.join(";" , addList));
                            } else {
                                List<String> proDataS = JsonUtil.getJsonToList(value, String.class);
                                dataMap.put(vModel, String.join("," , areaService.getProList(proDataS).stream().map(pro -> pro.getFullName()).collect(Collectors.toList())));
                            }
                            break;

                        case JnpfKeyConsts.RELATIONFORM:
                            VisualdevEntity entity = visualdevService.getInfo(swapDataVo.getModelId());
                            VisualdevModelDataInfoVO infoVO;
                            String keyId = String.valueOf(dataMap.get(vModel));
                            Map<String, Object> formDataMap = new HashMap<>(16);
                            if (!StringUtil.isEmpty(entity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(entity.getVisualTables())) {
                                infoVO = visualDevInfoService.getDetailsDataInfo(keyId, entity);
                            } else {
                                infoVO = visualdevModelDataService.infoDataChange(keyId, entity);
                            }
                            formDataMap = JsonUtil.stringToMap(infoVO.getData());
                            String relationField = swapDataVo.getRelationField();
                            if (formDataMap != null && formDataMap.size() > 0) {
                                dataMap.put(vModel + "_id" , dataMap.get(vModel));
                                dataMap.put(vModel, formDataMap.get(relationField));
                                dataDetailMap.put(vModel, formDataMap);
                            }
                            break;

                        case JnpfKeyConsts.POPUPSELECT:
                            ActionResult data = dataInterFaceService.infoToId(swapDataVo.getInterfaceId(), null,nullDatamap);
                            //api调用 序列化为linkedHashMap
                            LinkedHashMap<String, List<Map<String, Object>>> actionVo = (LinkedHashMap<String, List<Map<String, Object>>>) data.getData();
                            List<Map<String, Object>> mapList = actionVo.get("data" ) != null ? actionVo.get("data" ) : new ArrayList<>();
                            Map<String, Object> PopMap = mapList.stream().filter(map -> map.get(swapDataVo.getPropsValue()).equals(dataMap.get(vModel))).findFirst().orElse(null);
                            if (PopMap.size() > 0) {
                                dataMap.put(vModel + "_id" , dataMap.get(vModel));
                                dataMap.put(vModel, PopMap.get(swapDataVo.getColumnOptions().get(0).getValue()));
                                dataDetailMap.put(vModel, PopMap);
                            }
                            break;
                        case JnpfKeyConsts.POPUPTABLESELECT:
                            Object popData = dataInterFaceService.infoToId(swapDataVo.getInterfaceId(),null,null).getData();
                            DataInterfaceActionVo actionPo = (DataInterfaceActionVo) popData;
                            List<Map<String, Object>> popMapList = new ArrayList<>();
                            if (actionPo.getData() instanceof List) {
                                popMapList = (List<Map<String, Object>>) actionPo.getData();
                            }
                            String popValue = String.valueOf(dataMap.get(vModel));
                            List<String> idList = new ArrayList<>();
                            if (popValue.contains("[")) {
                                idList = JsonUtil.getJsonToList(popValue, String.class);
                            } else {
                                idList.add(popValue);
                            }
                            List<String> swapValue = new ArrayList<>();
                            for (String id : idList) {
                                popMapList.stream().filter(map ->
                                        map.get(swapDataVo.getPropsValue()).equals(id)
                                ).forEach(
                                        modelMap -> swapValue.add(String.valueOf(modelMap.get(swapDataVo.getRelationField())))
                                );
                            }
                            dataMap.put(vModel, swapValue.stream().collect(Collectors.joining(",")));
                            break;
                        case JnpfKeyConsts.MODIFYTIME:
                        case JnpfKeyConsts.CREATETIME:
                        case JnpfKeyConsts.DATE:
                            //判断是否为时间戳格式
                            String format;
                            String dateData = String.valueOf(dataMap.get(vModel));
                            String dateSwapInfo = swapDataVo.getFormat() != null ? swapDataVo.getFormat() : swapDataVo.getType() != null && swapDataVo.getType().equals(JnpfKeyConsts.DATE) ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm:ss" ;
                            if (!dateData.contains("-" ) && !dateData.contains(":" ) && dateData.length() > 10) {
                                DateTimeFormatter ftf = DateTimeFormatter.ofPattern(dateSwapInfo);
                                format = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) dataMap.get(vModel)), ZoneId.of("+8" )));
                            } else {
                                format = dateData;
                            }
                            if (format.contains("." )) {
                                format = format.substring(0, format.lastIndexOf("." ));
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat(dateSwapInfo);
                            try {
                                Date date = sdf.parse(format);
                                String outTime = sdf.format(sdf.parse(DateUtil.dateFormat(date)));
                                dataMap.put(vModel, outTime);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;

                        //开关 滑块
                        case JnpfKeyConsts.SWITCH:
                            String switchValue = String.valueOf(dataMap.get(vModel)).equals("1" ) ? swapDataVo.getActiveTxt() : swapDataVo.getInactiveTxt();
                            dataMap.put(vModel, switchValue);
                            break;
                        case JnpfKeyConsts.RATE:
                            BigDecimal ratevalue=new BigDecimal(0);
                            if(dataMap.get(vModel)!=null){
                                ratevalue= new BigDecimal(dataMap.get(vModel).toString());
                            }
                            dataMap.put(vModel, ratevalue);
                            break;
                        case JnpfKeyConsts.SLIDER:
                            dataMap.put(vModel, dataMap.get(vModel) != null ? Integer.parseInt(String.valueOf(dataMap.get(vModel))) : null);
                            break;

                        case JnpfKeyConsts.UPLOADFZ:
                        case JnpfKeyConsts.UPLOADIMG:
                            List<Map<String, Object>> fileList = JsonUtil.getJsonToListMap(String.valueOf(dataMap.get(vModel)));
                            dataMap.put(vModel, fileList);
                            break;

                        default:
                            break;
                    }
                }
            }
            //转换二维码
            swapCodeDataInfo(codeList, dataMap, dataCopyMap);
            //关联选择属性
            if (dataDetailMap.size() > 0) {
                getDataAttr(modelList, dataMap, dataDetailMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataMap;
    }


    /**
     * 转换数据格式(编辑页)
     *
     * @param modelList 控件
     * @param dataMap   数据
     * @return
     */
    public Map<String, Object> swapDataInfoType(List<FieLdsModel> modelList, Map<String, Object> dataMap) {
        return formInfoUtils.swapDataInfoType(modelList,dataMap);
    }


    /**
     * 转换数据格式(编辑页)
     *
     * @param modelList 控件
     * @param dataMap   数据
     * @return
     */
    public Map<String, Object> getInitLineData(List<FieLdsModel> modelList, Map<String, Object> dataMap) {
        for (FieLdsModel swapDataVo : modelList) {
            String jnpfKey = swapDataVo.getConfig().getJnpfKey();
            String vModel = swapDataVo.getVModel();
            Object value = dataMap.get(vModel);
            if (value == null || ObjectUtil.isEmpty(value)) {
                continue;
            }
            switch (jnpfKey) {
                case JnpfKeyConsts.RATE:
                case JnpfKeyConsts.SLIDER:
                    BigDecimal ratevalue=new BigDecimal(0);
                    if(dataMap.get(vModel)!=null){
                        ratevalue= new BigDecimal(dataMap.get(vModel).toString());
                    }
                    dataMap.put(vModel, ratevalue);
                    break;
                case JnpfKeyConsts.UPLOADFZ:
                case JnpfKeyConsts.UPLOADIMG:
                    List<Map<String, Object>> fileList = JsonUtil.getJsonToListMap(String.valueOf(value));
                    dataMap.put(vModel, fileList);
                    break;

                case JnpfKeyConsts.DATE:
                    Long dateTime = DateTimeFormatConstant.getDateObjToLong(dataMap.get(vModel));
                    dataMap.put(vModel, dateTime != null ? dateTime : dataMap.get(vModel));
                    break;

                case JnpfKeyConsts.SWITCH:
                    dataMap.put(vModel, value != null ? Integer.parseInt(String.valueOf(value)) : null);
                    break;
                //系统自动生成控件
                case JnpfKeyConsts.CURRORGANIZE:
                case JnpfKeyConsts.CURRDEPT:
                    //多级组
                    String orgIds = String.valueOf(dataMap.get(vModel));
                    String orgId = "";
                    String orgName = "";
                    try{
                        List<String> jsonToList = JsonUtil.getJsonToList(orgIds, String.class);
                        orgId = jsonToList.get(jsonToList.size()-1);
                    }catch (Exception e){
                        orgId = orgIds;
                    }
                    OrganizeEntity organizeEntity = StringUtil.isNotEmpty(orgId) ? Optional.ofNullable(organizeService.getInfo(orgId)).orElse(new OrganizeEntity()) : null;
                    if ("all".equals(swapDataVo.getShowLevel())) {
                        if (organizeEntity != null) {
                            List<OrganizeEntity> organizeList = organizeService.getOrganizeId(String.valueOf(value));
                            Collections.reverse(organizeList);
                            orgName = organizeList.stream().map(OrganizeEntity::getFullName).collect(Collectors.joining("/" ));
                        }
                    } else {
                        if (organizeEntity != null) {
                            orgName = organizeEntity.getFullName();
                        } else {
                            orgName = " ";
                        }
                    }
                    dataMap.put(vModel,orgName);
                    break;
                case JnpfKeyConsts.CURRPOSITION:
                    PositionEntity positionEntity = positionService.getInfo(String.valueOf(value));
                    dataMap.put(vModel, Objects.nonNull(positionEntity) ? positionEntity.getFullName() : value);
                    break;

                case JnpfKeyConsts.CREATEUSER:
                case JnpfKeyConsts.MODIFYUSER:
                    UserEntity userEntity = userService.getInfo(String.valueOf(value));
                    String userValue = Objects.nonNull(userEntity) ? userEntity.getRealName()+"/"+userEntity.getAccount() : String.valueOf(value);
                    dataMap.put(vModel, userValue);
                    break;
                default:
                    dataMap.put(vModel, FormPublicUtils.getDataConversion(value));
                    break;
            }
        }
        return dataMap;
    }

    /**
     * 二维码 条形码详情数据
     *
     * @param codeList    控件集合
     * @param swapDataMap 转换后的数据
     * @param dataMap     转换前
     * @return
     */
    public static void swapCodeDataInfo(List<FormModel> codeList, Map<String, Object> swapDataMap, Map<String, Object> dataMap) {
        for (FormModel formModel : codeList) {
            String jnpfKey = formModel.getConfig().getJnpfKey();
            if (jnpfKey.equals(JnpfKeyConsts.QR_CODE) || jnpfKey.equals(JnpfKeyConsts.BARCODE)) {
                String codeDataType = formModel.getDataType();
                if (OnlineDataTypeEnum.RELATION.getType().equals(codeDataType)) {
                    String relationFiled = formModel.getRelationField();
                    if (StringUtil.isNotEmpty(relationFiled)) {
                        Object relationValue = dataMap.get(relationFiled);
                        if (ObjectUtil.isNotEmpty(relationValue)) {
                            swapDataMap.put(relationFiled + "_id" , relationValue);
                        }
                    }
                }
            }
        }
    }

    private static void treeToList(String value, String label, String children, JSONArray data, List<Map<String, Object>> result) {
        for (int i = 0; i < data.size(); i++) {
            JSONObject ob = data.getJSONObject(i);
            Map<String, Object> tree = new HashMap<>(16);
            tree.put(value, String.valueOf(ob.get(value)));
            tree.put(label, String.valueOf(ob.get(label)));
            result.add(tree);
            if (ob.get(children) != null) {
                JSONArray childArray = ob.getJSONArray(children);
                treeToList(value, label, children, childArray, result);
            }
        }
    }

    /**
     * 生成关联属性（弹窗选择属性,关联表单属性）
     *
     * @param fieLdsModelList
     * @param dataMap
     * @param dataDetailMap
     */
    private static void getDataAttr(List<FieLdsModel> fieLdsModelList, Map<String, Object> dataMap, Map<String, Map<String, Object>> dataDetailMap) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            String jnpfKey = fieLdsModel.getConfig().getJnpfKey();
            if (jnpfKey.equals(JnpfKeyConsts.RELATIONFORM_ATTR) || jnpfKey.equals(JnpfKeyConsts.POPUPSELECT_ATTR)) {
                String relationField = fieLdsModel.getRelationField();
                String showField = fieLdsModel.getShowField();
                Map<String, Object> formDataMap = dataDetailMap.get(relationField);
                dataMap.put(relationField + "_" + showField, formDataMap.get(showField));
            }
        }
    }

    /**
     * 转换组织
     *
     * @param modelValue
     * @return
     */
    private String getOrgValue(String modelValue) {
        String orgValue;
        List<String> valueList;
        if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
            String[][] prgArray = JsonUtil.getJsonToBean(modelValue, String[][].class);
            List<String> addList = new ArrayList<>();
            for (String[] prgData : prgArray) {
                List<String> adList = new ArrayList<>();
                for (String s : prgData) {
                    OrganizeEntity info = organizeService.getInfo(s);
                    adList.add(Objects.nonNull(info) ? info.getFullName() : "" );
                }
                String porData = adList.stream().collect(Collectors.joining("/" ));
                addList.add(porData);
            }
            orgValue = String.join(";" , addList);
        } else {
            if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
                valueList = JsonUtil.getJsonToList(modelValue, String.class);
            } else {
                valueList = Stream.of(modelValue.split("," )).collect(Collectors.toList());
            }
            String allValue = valueList.stream().map(va -> {
                OrganizeEntity organizeEntity = organizeService.getInfo(va);
                return Objects.nonNull(organizeEntity) ? organizeEntity.getFullName() : va;
            }).collect(Collectors.joining("," ));
            orgValue = allValue;
        }
        return orgValue;
    }

    /**
     * 转换岗位
     *
     * @param modelValue
     * @return
     */
    private String getPosValue(String modelValue) {
        String posValue;
        List<String> valueList;
        if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
            String[][] prgArray = JsonUtil.getJsonToBean(modelValue, String[][].class);
            List<String> addList = new ArrayList<>();
            for (String[] prgData : prgArray) {
                List<String> adList = new ArrayList<>();
                for (String s : prgData) {
                    PositionEntity info = positionService.getInfo(s);
                    adList.add(Objects.nonNull(info) ? info.getFullName() : "" );
                }
                String porData = adList.stream().collect(Collectors.joining("/" ));
                addList.add(porData);
            }
            posValue = String.join(";" , addList);
        } else {
            if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
                valueList = JsonUtil.getJsonToList(modelValue, String.class);
            } else {
                valueList = Stream.of(modelValue.split("," )).collect(Collectors.toList());
            }
            String allValue = valueList.stream().map(va -> {
                PositionEntity positionEntity = positionService.getInfo(va);
                return Objects.nonNull(positionEntity) ? positionEntity.getFullName() : va;
            }).collect(Collectors.joining("," ));
            posValue = allValue;
        }
        return posValue;
    }

    /**
     * 转换用户
     *
     * @param modelValue
     * @return
     */
    private String getUserValue(String modelValue) {
        String userValue;
        List<String> valueList;
        if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
            String[][] prgArray = JsonUtil.getJsonToBean(modelValue, String[][].class);
            List<String> addList = new ArrayList<>();
            for (String[] prgData : prgArray) {
                List<String> adList = new ArrayList<>();
                for (String s : prgData) {
                    UserEntity info = userService.getInfo(s);
                    adList.add(Objects.nonNull(info) ? info.getRealName() + "/" + info.getAccount() : "" );
                }
                String porData = adList.stream().collect(Collectors.joining("/" ));
                addList.add(porData);
            }
            userValue = String.join(";" , addList);
        } else {
            if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
                valueList = JsonUtil.getJsonToList(modelValue, String.class);
            } else {
                valueList = Stream.of(modelValue.split("," )).collect(Collectors.toList());
            }
            String allValue = valueList.stream().map(va -> {
                UserEntity userEntity = userService.getInfo(va);
                return Objects.nonNull(userEntity) ? userEntity.getRealName() + "/" + userEntity.getAccount() : va;
            }).collect(Collectors.joining("," ));
            userValue = allValue;
        }
        return userValue;
    }


    /**
     * 转换角色
     *
     * @param modelValue
     * @return
     */
    private String getRoleValue(String modelValue) {
        String value;
        List<String> valueList;
        if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
            String[][] prgArray = JsonUtil.getJsonToBean(modelValue, String[][].class);
            List<String> addList = new ArrayList<>();
            for (String[] prgData : prgArray) {
                List<String> adList = new ArrayList<>();
                for (String s : prgData) {
                    RoleEntity info = roleService.getInfo(s);
                    adList.add(Objects.nonNull(info) ? info.getFullName() : "");
                }
                String porData = adList.stream().collect(Collectors.joining("/"));
                addList.add(porData);
            }
            value = String.join(";", addList);
        } else {
            if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
                valueList = JsonUtil.getJsonToList(modelValue, String.class);
            } else {
                valueList = Stream.of(modelValue.split(",")).collect(Collectors.toList());
            }
            String allValue = valueList.stream().map(va -> {
                RoleEntity userEntity = roleService.getInfo(va);
                return Objects.nonNull(userEntity) ? userEntity.getFullName() : va;
            }).collect(Collectors.joining(","));
            value = allValue;
        }
        return value;
    }

    /**
     * 转换分组
     *
     * @param modelValue
     * @return
     */
    private String getGroupValue(String modelValue) {
        String value;
        List<String> valueList;
        if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_TWO.getMultipleChar())) {
            String[][] prgArray = JsonUtil.getJsonToBean(modelValue, String[][].class);
            List<String> addList = new ArrayList<>();
            for (String[] prgData : prgArray) {
                List<String> adList = new ArrayList<>();
                for (String s : prgData) {
                    GroupEntity info = groupService.getInfo(s);
                    adList.add(Objects.nonNull(info) ? info.getFullName() : "");
                }
                String porData = adList.stream().collect(Collectors.joining("/"));
                addList.add(porData);
            }
            value = String.join(";", addList);
        } else {
            if (OnlinePublicUtils.getMultiple(modelValue, MultipleControlEnum.MULTIPLE_JSON_ONE.getMultipleChar())) {
                valueList = JsonUtil.getJsonToList(modelValue, String.class);
            } else {
                valueList = Stream.of(modelValue.split(",")).collect(Collectors.toList());
            }
            String allValue = valueList.stream().map(va -> {
                GroupEntity info = groupService.getInfo(va);
                return Objects.nonNull(info) ? info.getFullName() : va;
            }).collect(Collectors.joining(","));
            value = allValue;
        }
        return value;
    }


}
