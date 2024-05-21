package jnpf.onlinedev.util;

import cn.hutool.core.util.ObjectUtil;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;
import jnpf.util.visiual.JnpfKeyConsts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 处理自动生成字段
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-15
 */
public class AutoFeildsUtil {

    private static OrganizeService organizeService;
    private static UserService userService;
    private static PositionService positionService;

    //初始化
    public static void init() {
        userService = SpringContext.getBean(UserService.class);
        organizeService = SpringContext.getBean(OrganizeService.class);
        positionService=SpringContext.getBean(PositionService.class);
    }

    /**
     * 列表系统自动生成字段转换
     *
     * @return String
     */
    public static String autoFeilds(List<FieLdsModel> fieLdsModelList, String data) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            Map<String, Object> dataMap = JsonUtil.stringToMap(data);
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (Objects.isNull(entry.getValue())){
                        continue;
                    }
                if (fieLdsModel.getVModel().equals(entry.getKey())) {
                    String jnpfKeyType = fieLdsModel.getConfig().getJnpfKey();
                    switch (jnpfKeyType) {
                        case JnpfKeyConsts.CURRORGANIZE:
                        case JnpfKeyConsts.CURRDEPT:
                            if("all".equals(fieLdsModel.getShowLevel())){
                                List<OrganizeEntity> organizeList = organizeService.getOrganizeId(String.valueOf(entry.getValue()));
                                Collections.reverse(organizeList);
                                String value = organizeList.stream().map(OrganizeEntity::getFullName).collect(Collectors.joining("/"));
                                entry.setValue(value);
                            }else {
                                OrganizeEntity organizeEntity = organizeService.getInfo(String.valueOf(entry.getValue()));
                                entry.setValue(organizeEntity != null ? organizeEntity.getFullName() : "");
                            }
                            break;
                        case JnpfKeyConsts.CREATEUSER:
                        case JnpfKeyConsts.MODIFYUSER:
                            UserEntity userCreEntity = userService.getInfo(String.valueOf(entry.getValue()));
                            if (userCreEntity != null) {
                                entry.setValue(userCreEntity.getRealName());
                            }
                            break;
                        case JnpfKeyConsts.CURRPOSITION:
                            String[] curPos = String.valueOf(entry.getValue()).split(",");
                            List<String> curPosList = new ArrayList<>();
                            for (String pos : curPos){
                                PositionEntity posEntity = positionService.getInfo(pos);
                                String posName = Objects.nonNull(posEntity) ? posEntity.getFullName() : "";
                                curPosList.add(posName);
                            }
                            entry.setValue(curPosList.stream().collect(Collectors.joining(",")));
                            break;
                        case JnpfKeyConsts.CREATETIME:
                        case JnpfKeyConsts.MODIFYTIME:
                            if (ObjectUtil.isNotEmpty(entry.getValue())){
                                String dateStr=String.valueOf(entry.getValue());
                                dateStr=dateStr.length()>19?dateStr.substring(0,19):dateStr;
                                entry.setValue(dateStr);
                            }else {
                                entry.setValue(null);
                            }
                            break;
                        default:
                    }
                }
            }
            data = JsonUtilEx.getObjectToString(dataMap);
        }
        return data;
    }

    public FieLdsModel getTreeRelationSearch(List<FieLdsModel> FieLdsModels, String treeRelationField) {
        FieLdsModel fieLdsModel = new FieLdsModel();
        boolean treeIsChild = treeRelationField.toLowerCase().contains("tablefield");
        if (treeIsChild){
            String tableField = treeRelationField.substring(0,treeRelationField.indexOf("-"));
            String relationVmodel = treeRelationField.substring(treeRelationField.indexOf("-")+1);
            List<FieLdsModel> allFields = new ArrayList<>();
            recursionFields(FieLdsModels,allFields);
//            List<FieLdsModel> childFields = FieLdsModels.stream().filter(fieLd -> fieLd.getVModel().equals(tableField)).map(f -> f.getConfig().getChildren()).findFirst().orElse(new ArrayList<>());
            fieLdsModel = allFields.stream().filter(swap->relationVmodel.equalsIgnoreCase(swap.getVModel())
                    &&tableField.equals(swap.getConfig().getParentVModel())).findFirst().orElse(null);
        } else {
            //递归出所有表单控件从中去除左侧树的控件属性
            List<FieLdsModel> allFields = new ArrayList<>();
            this.recursionFields(FieLdsModels,allFields);
            fieLdsModel = allFields.stream().filter(swap -> treeRelationField.equalsIgnoreCase(swap.getVModel())).findFirst().orElse(null);
        }
        return fieLdsModel;
    }

    private void recursionFields(List<FieLdsModel> fieLdsModelList,List<FieLdsModel> allFields){
        for (FieLdsModel fieLdsModel : fieLdsModelList){
            if (fieLdsModel.getConfig().getChildren()!=null){
               this.recursionFields(fieLdsModel.getConfig().getChildren(),allFields);
            }else {
                if (StringUtil.isNotEmpty(fieLdsModel.getVModel())){
                    allFields.add(fieLdsModel);
                }
            }
        }
    }

}
