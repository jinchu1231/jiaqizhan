package jnpf.engine.model.flowtask;

import jnpf.base.UserInfo;
import jnpf.engine.model.flowmessage.FlowMsgModel;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * 流程监控器参数模型
 *
 * @author JNPF开发平台组
 * @version V3.3.0 flowable
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/2 10:11
 */

@Data
@AllArgsConstructor
public class WorkJobModel {
    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务对象
     */
    private FlowMsgModel flowMsgModel;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

}
