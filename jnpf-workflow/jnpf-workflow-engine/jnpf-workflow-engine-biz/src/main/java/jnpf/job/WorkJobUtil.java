package jnpf.job;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowmessage.FlowMsgModel;
import jnpf.engine.model.flowtask.WorkJobModel;
import jnpf.engine.util.FlowMsgUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程定时器工具类
 *
 * @author JNPF开发平台组
 * @version V3.3.0 flowable
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/2 9:04
 */
@Component
public class WorkJobUtil {
    /**
     * 缓存key
     */
    public static final String REDIS_KEY = "idgenerator_WorkJobNew";

    private static FlowMsgUtil flowMsgUtil;

    /**
     * 将数据放入缓存
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/6/2
     */
    public static void insertRedis(WorkJobModel workJobModel, RedisUtil redisUtil) {
        JSONObject json = new JSONObject();
        json.put("taskId", workJobModel.getTaskId());
        json.put("flowMsgModel", JsonUtil.getObjectToString(workJobModel.getFlowMsgModel()));
        json.put("userInfo", JsonUtil.getObjectToString(workJobModel.getUserInfo()));

        List<FlowTaskOperatorEntity> operatorList = workJobModel.getFlowMsgModel().getOperatorList();
        if (operatorList.isEmpty()) {
            return;
        }
        long time = System.currentTimeMillis();
        boolean isNext = operatorList.stream().filter(t -> t.getCreatorTime().getTime() > time).count() > 0;
        if (isNext) {
            Map<String, List<FlowTaskOperatorEntity>> operatorMap = operatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId));
            for (String key : operatorMap.keySet()) {
                redisUtil.insertHash(REDIS_KEY, workJobModel.getTaskId() + key, String.valueOf(operatorMap.get(key).get(0).getCreatorTime().getTime()));
                redisUtil.insertHash(REDIS_KEY + "_json", workJobModel.getTaskId() + key, json.toJSONString());
            }

        } else {
            try {
                flowMsgUtil.message(workJobModel.getFlowMsgModel());
            } catch (Exception e) {
            }
        }
    }

    /**
     * 定时器取用数据调用创建方法
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/6/2
     */
    public static List<WorkJobModel> getListRedis(RedisUtil redisUtil) {
        List<WorkJobModel> list = new ArrayList<>();
        if (redisUtil.exists(REDIS_KEY)) {
            Map<String, Object> map = redisUtil.getMap(REDIS_KEY);
            for (Object object : map.keySet()) {
                if (map.get(object) instanceof String) {
                    Map<String, Object> jsonMap = JsonUtil.stringToMap(String.valueOf(map.get(object)));
                    String taskId = jsonMap.get("taskId").toString();
                    FlowMsgModel flowMsgModel = JsonUtil.getJsonToBean(jsonMap.get("flowMsgModel").toString(), FlowMsgModel.class);
                    UserInfo userInfo = JsonUtil.getJsonToBean(jsonMap.get("userInfo").toString(), UserInfo.class);
                    list.add(new WorkJobModel(taskId, flowMsgModel, userInfo));
                }
            }
        }
        return list;
    }

    /**
     * 定时器取用数据调用创建方法
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/6/2
     */
    public static Map<String, Object> getListRedisTime(RedisUtil redisUtil) {
        Map<String, Object> map = null;
        if (redisUtil.exists(REDIS_KEY)) {
            map = redisUtil.getMap(REDIS_KEY);
        }
        return map;
    }

    /**
     * 获取缓存信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/6/2
     */
    public static WorkJobModel getListRedisJson(RedisUtil redisUtil, String key) {
        String hashValues = redisUtil.getHashValues(REDIS_KEY + "_json", key);
        Map<String, Object> jsonMap = JsonUtil.stringToMap(String.valueOf(hashValues));
        String taskId = jsonMap.get("taskId").toString();
        FlowMsgModel flowMsgModel = JsonUtil.getJsonToBean(jsonMap.get("flowMsgModel").toString(), FlowMsgModel.class);
        UserInfo userInfo = JsonUtil.getJsonToBean(jsonMap.get("userInfo").toString(), UserInfo.class);
        return new WorkJobModel(taskId, flowMsgModel, userInfo);
    }

    @Autowired
    public void setFlowMsgUtil(FlowMsgUtil flowMsgUtil) {
        WorkJobUtil.flowMsgUtil = flowMsgUtil;
    }

}
