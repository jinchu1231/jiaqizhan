package jnpf.engine.model.flowmessage;

import jnpf.engine.entity.FlowTaskCirculateEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.model.flowbefore.FlowTemplateAllModel;
import jnpf.engine.model.flowengine.FlowModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/7/14 9:17
 */
@Data
@NoArgsConstructor
public class FlowMsgModel {
    private String title;
    private FlowTemplateAllModel flowTemplateAllModel = new FlowTemplateAllModel();
    private Map<String, Object> data = new HashMap<>();
    private FlowModel flowModel = new FlowModel();
    private FlowTaskEntity taskEntity = new FlowTaskEntity();
    private FlowTaskNodeEntity taskNodeEntity = new FlowTaskNodeEntity();
    private List<FlowTaskNodeEntity> nodeList = new ArrayList<>();
    private List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
    private List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
    /**
     * 代办 (通知代办)
     */
    private Boolean wait = true;
    /**
     * 同意
     */
    private Boolean approve = false;
    /**
     * 拒绝
     */
    private Boolean reject = false;
    /**
     * 抄送人
     */
    private Boolean copy = false;
    /**
     * 结束 (通知发起人)
     */
    private Boolean end = false;
    /**
     * 子流程通知
     */
    private Boolean launch = false;
    /**
     * 拒绝发起节点
     */
    private Boolean start = false;
    /**
     * 超时
     */
    private Boolean overtime = false;
    /**
     * 提醒
     */
    private Boolean notice = false;
}
