package jnpf.message.model.mq;

import jnpf.base.UserInfo;
import jnpf.message.entity.MessageEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发送公告到消息队列中的模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-12-01
 */
@Data
public class SentMessageModel implements Serializable {

    /**
     * 接收者的id
     */
    private List<String> idList;

    /**
     * 消息实体
     */
    private MessageEntity entity;

    /**
     * 用户模型
     */
    private UserInfo userInfo;

    /**
     * 当前是否为发送公告的最后一次
     */
    private Boolean countEnd;

    /**
     * redis中的键
     */
    private String cacheKey;

    private String receiveId;

    public SentMessageModel() {
    }

    public SentMessageModel(List<String> idList, MessageEntity entity, UserInfo userInfo, Boolean countEnd, String cacheKey, String receiveId) {
        this.idList = idList;
        this.entity = entity;
        this.userInfo = userInfo;
        this.countEnd = countEnd;
        this.cacheKey = cacheKey;
        this.receiveId = receiveId;
    }
}
