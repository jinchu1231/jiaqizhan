package jnpf.engine.util;

import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowengine.shuntjson.childnode.LimitModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowtime.FlowTimeModel;
import jnpf.exception.WorkFlowException;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/17 17:41
 */
@Component
public class FlowTimerUtil {



    /**
     * 限时开始时间
     */
    public FlowTimeModel time(FlowTaskNodeEntity taskNodeEntity, List<FlowTaskNodeEntity> nodeList
            , FlowTaskEntity flowTaskEntity, FlowTaskOperatorEntity operatorInfo) throws WorkFlowException {
        FlowTaskNodeEntity startNode = nodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().orElse(null);
        String nodeJson = startNode != null ? startNode.getNodePropertyJson() : "{}";
        ChildNodeList childNode = JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class);
        FlowTimeModel date = new FlowTimeModel();
        date.setChildNode(JsonUtil.getJsonToBean(nodeJson, ChildNodeList.class));//开始节点
        date.setChildNodeEvnet(childNode);//当前节点
        Date(operatorInfo, flowTaskEntity, date);
        return date;
    }


    private void Date(FlowTaskOperatorEntity operatorInfo, FlowTaskEntity flowTaskEntity, FlowTimeModel flowTimeModel) throws WorkFlowException {
        Properties taskProperties = flowTimeModel.getChildNodeEvnet().getProperties();
        LimitModel limitModel = taskProperties.getTimeLimitConfig();
        boolean isOn = limitModel.getOn() != 0;
        if (limitModel.getOn() == 2) {
            taskProperties = flowTimeModel.getChildNode().getProperties();
            limitModel = taskProperties.getTimeLimitConfig();
        }
        Map<String, Object> data = JsonUtil.stringToMap(flowTaskEntity.getFlowFormContentJson());
        flowTimeModel.setOn(isOn);
        if (isOn) {
            Date date = null;
            if (limitModel.getNodeLimit() == 0) {
                date = operatorInfo.getCreatorTime();
            } else if (limitModel.getNodeLimit() == 1) {
                date = flowTaskEntity.getCreatorTime();
            } else {
                Object formData = data.get(limitModel.getFormField());
                try {
                    date = new Date((Long) formData);
                } catch (Exception e) {
                }
                if (date == null) {
                    try {
                        date = DateUtil.stringToDate(String.valueOf(formData));
                    } catch (Exception e) {
                    }
                }
            }
            if (date == null) {
                date = flowTaskEntity.getCreatorTime();
            }
            flowTimeModel.setDate(date);
        }
    }

}
