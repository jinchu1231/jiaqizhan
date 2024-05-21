package jnpf.scheduletask.task;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.service.DataInterfaceService;
import jnpf.scheduletask.entity.TimeTaskEntity;
import jnpf.scheduletask.model.ContentNewModel;
import jnpf.scheduletask.model.TaskParameterModel;
import jnpf.util.AuthUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Service和数据接口使用
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/3/23 9:25
 */
@Component
public class ScheduleTaskHandler {

    @Autowired
    private DataInterfaceService dataInterFaceService;


    @XxlJob("defaultHandler")
    public void defaultHandler() {
        long time = System.currentTimeMillis();
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        // 转换成模型
        TimeTaskEntity entity = JsonUtil.getJsonToBean(param, TimeTaskEntity.class);
        ContentNewModel model = JsonUtil.getJsonToBean(param, ContentNewModel.class);
        // 得到数据接口信息
        String tenantId = StringUtil.isNotEmpty(model.getUserInfo().getTenantId()) ? model.getUserInfo().getTenantId() : "";
        String userId = StringUtil.isNotEmpty(model.getUserInfo().getUserId()) ? model.getUserInfo().getUserId() : "";
        String token = AuthUtil.loginTempUser(userId, tenantId, true);
        DataInterfaceEntity dataInterfaceEntity = dataInterFaceService.getDataInterfaceInfo(model.getInterfaceId(), tenantId);
        // 如果是http
        if (entity != null && "3".equals(String.valueOf(dataInterfaceEntity.getType()))) {
            dataInterfaceEntity.setTenantId(tenantId);
            boolean callHttp = callHttp(model, dataInterfaceEntity, token);
        } else if (entity != null && "1".equals(String.valueOf(dataInterfaceEntity.getType()))) {
            dataInterfaceEntity.setTenantId(tenantId);
            boolean callSql = callSql(model, dataInterfaceEntity, token);
        }
    }

    // ---------------START callSQL

    /**
     * 调用SQL
     *
     * @param model 系统调度参数
     */
    private boolean callSql(ContentNewModel model, DataInterfaceEntity entity, String token) {
        try {
            if (entity != null) {
                Map<String, String> map = new HashMap<>(16);
                if (model.getParameter() != null && model.getParameter().size() > 0) {
                    for (TaskParameterModel parameterModel : model.getParameter()) {
                        if (StringUtil.isNotEmpty(parameterModel.getValue())) {
                            map.put(parameterModel.getField(), parameterModel.getValue());
                        } else {
                            map.put(parameterModel.getField(), parameterModel.getDefaultValue());
                        }
                    }
                }
                dataInterFaceService.infoToId(entity.getId(), entity.getTenantId(), map, token, null, null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------START callHttp

    /**
     * HTTP调用
     *
     * @param model 系统调度参数
     * @return
     */
    private Boolean callHttp(ContentNewModel model, DataInterfaceEntity entity, String token) {
        try {
            if (entity != null) {
                Map<String, String> map = new HashMap<>(16);
                if (model.getParameter() != null && model.getParameter().size() > 0) {
                    for (TaskParameterModel parameterModel : model.getParameter()) {
                        if (StringUtil.isNotEmpty(parameterModel.getValue())) {
                            map.put(parameterModel.getField(), parameterModel.getValue());
                        } else {
                            map.put(parameterModel.getField(), parameterModel.getDefaultValue());
                        }
                    }
                }
                dataInterFaceService.infoToId(entity.getId(), entity.getTenantId(), map, token, null, null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void scheldule() {

    }

}
