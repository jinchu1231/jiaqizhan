package jnpf.message.util;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.message.entity.MessageReceiveEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息推送工具类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-07
 */
@Component
public class PushMessageUtil {

    /**
     * 工作流消息发送
     *
     *
     * @param userInfo
     */
    public static void pushMessage(Map<String, MessageReceiveEntity> map, UserInfo userInfo, int messageType){
        for (String userId : map.keySet()) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (userId.equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("method", "messagePush");
                    jsonObject.put("unreadNoticeCount", 1);
                    jsonObject.put("messageType", messageType);
                    jsonObject.put("userId", userInfo.getTenantId());
                    jsonObject.put("toUserId", userId);
                    jsonObject.put("title", map.get(userId).getTitle());
                    jsonObject.put("id",map.get(userId).getId());
                    jsonObject.put("messageDefaultTime", map.get(userId).getLastModifyTime() != null ? map.get(userId).getLastModifyTime().getTime() : null);
                    OnlineUserProvider.sendMessage(item, jsonObject);
                }
            }
        }
    }

}
