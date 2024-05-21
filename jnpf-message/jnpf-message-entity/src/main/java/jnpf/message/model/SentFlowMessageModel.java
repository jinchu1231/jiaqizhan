package jnpf.message.model;

import jnpf.base.UserInfo;
import jnpf.message.entity.SendConfigTemplateEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 发送消息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2022-01-15
 */
@Data
public class SentFlowMessageModel implements Serializable {
    /**
     * 发送给哪些人
     */
    private List<String> toUserIdsList;

    /**
     * 消息实体
     */
    private SendConfigTemplateEntity entity;

    /**
     * 站内信时消息内容
     */
    private String content;

    /**
     * 参数Map
     */
    private Map<String, Object> parameterMap;

    /**
     * 当前用户信息
     */
    private UserInfo userInfo;

    /**
     * 消息类型
     */
    private String sendType;

    /**
     * 站内信
     */
    private Map<String,String> contentMsg;

    /**
     * 流程信息
     */
    private String flowName;

    /**
     * 发起人
     */
    private String userName;

    /**
     * 标题
     */
    private String title;

    public SentFlowMessageModel() {
    }

    public SentFlowMessageModel(List<String> toUserIdsList, SendConfigTemplateEntity entity, String content, Map<String, Object> parameterMap, UserInfo userInfo, String sendType,String title,String flowName,String userName) {
        this.toUserIdsList = toUserIdsList;
        this.entity = entity;
        this.content = content;
        this.parameterMap = parameterMap;
        this.userInfo = userInfo;
        this.sendType = sendType;
        this.title = title;
        this.flowName = flowName;
        this.userName = userName;
    }
}
