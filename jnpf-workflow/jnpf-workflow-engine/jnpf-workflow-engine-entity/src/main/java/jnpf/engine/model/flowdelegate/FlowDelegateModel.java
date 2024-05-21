package jnpf.engine.model.flowdelegate;

import jnpf.base.UserInfo;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowbefore.FlowTemplateAllModel;
import jnpf.engine.util.FlowNature;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:18
 */
@Data
public class FlowDelegateModel {
    //true 委托 false 审批
    private Boolean delegate = true;
    //0.发起 1.审批 2.结束
    private Integer type = FlowNature.StartMsg;
    private List<String> toUserIds = new ArrayList<>();
    private UserInfo userInfo = new UserInfo();
    private FlowTaskEntity flowTask = new FlowTaskEntity();
    private FlowTemplateAllModel templateAllModel = new FlowTemplateAllModel();
    //审批是否要发送消息
    private Boolean approve = true;
}
