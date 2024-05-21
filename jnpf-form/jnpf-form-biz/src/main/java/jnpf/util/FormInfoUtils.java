package jnpf.util;

import cn.hutool.core.util.ObjectUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
public class FormInfoUtils {
    @Autowired
    private ServiceBaseUtil serviceUtil;


    /**
     * 转换数据格式(编辑页)
     *
     * @param modelList 控件
     * @param dataMap   数据
     * @return
     */
    public Map<String, Object> swapDataInfoType(List<FieLdsModel> modelList, Map<String, Object> dataMap) {
        dataMap = Optional.ofNullable(dataMap).orElse(new HashMap<>());
        try {
            DynamicDataSourceUtil.switchToDataSource(null);
            List<String> systemConditions = new ArrayList() {{
                add(JnpfKeyConsts.CURRORGANIZE);
                add(JnpfKeyConsts.CURRDEPT);
                add(JnpfKeyConsts.CURRPOSITION);
            }};
            List<String> nullIsList = new ArrayList() {{
                add(JnpfKeyConsts.UPLOADFZ);
                add(JnpfKeyConsts.UPLOADIMG);
            }};
            for (FieLdsModel swapDataVo : modelList) {
                String jnpfKey = swapDataVo.getConfig().getJnpfKey();
                String vModel = swapDataVo.getVModel();
                Object value = dataMap.get(vModel);
                if (value == null || ObjectUtil.isEmpty(value)) {
                    if (systemConditions.contains(jnpfKey)) {
                        dataMap.put(vModel, " " );
                    }
                    if (nullIsList.contains(jnpfKey)) {
                        dataMap.put(vModel, Collections.emptyList());
                    }
                    continue;
                }
                switch (jnpfKey) {
                    case JnpfKeyConsts.UPLOADFZ:
                    case JnpfKeyConsts.UPLOADIMG:
                        List<Map<String, Object>> fileList = JsonUtil.getJsonToListMap(String.valueOf(value));
                        dataMap.put(vModel, fileList.size() == 0 ? new ArrayList<>() : fileList);
                        break;
                    case JnpfKeyConsts.DATE:
                        Long dateTime = DateTimeFormatConstant.getDateObjToLong(dataMap.get(vModel));
                        dataMap.put(vModel, dateTime != null ? dateTime : dataMap.get(vModel));
                        break;
                    case JnpfKeyConsts.CREATETIME:
                    case JnpfKeyConsts.MODIFYTIME:
                        String pattern = DateTimeFormatConstant.YEAR_MOnTH_DHMS;
                        Long time = DateTimeFormatConstant.getDateObjToLong(dataMap.get(vModel));
                        dataMap.put(vModel, time!=null?DateUtil.dateToString(new Date(time),pattern):"");
                        break;
                    case JnpfKeyConsts.SWITCH:
                    case JnpfKeyConsts.SLIDER:
                    case JnpfKeyConsts.RATE:
                    case JnpfKeyConsts.CALCULATE:
                    case JnpfKeyConsts.NUM_INPUT:
                        dataMap.put(vModel, value != null ? new BigDecimal(String.valueOf(value)) : null);
                        break;
                    case JnpfKeyConsts.CURRPOSITION:
                        PositionEntity positionEntity = serviceUtil.getPositionInfo(String.valueOf(value));
                        dataMap.put(vModel, Objects.nonNull(positionEntity) ? positionEntity.getFullName() : value);
                        break;

                    case JnpfKeyConsts.CREATEUSER:
                    case JnpfKeyConsts.MODIFYUSER:
                        UserEntity userEntity = serviceUtil.getUserInfo(String.valueOf(value));
                        String userValue = Objects.nonNull(userEntity) ? userEntity.getAccount().equalsIgnoreCase(ADMIN_KEY)
                                ? "管理员/admin" : userEntity.getRealName() + "/" + userEntity.getAccount() : String.valueOf(value);
                        dataMap.put(vModel, userValue);
                        break;
                    case JnpfKeyConsts.CURRORGANIZE:
                        String currentOrganizeName = serviceUtil.getCurrentOrganizeName(value, swapDataVo.getShowLevel());
                        dataMap.put(vModel, currentOrganizeName);
                        break;
                    default:
                        dataMap.put(vModel, FormPublicUtils.getDataConversion(value));
                        break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return dataMap;
    }

}
