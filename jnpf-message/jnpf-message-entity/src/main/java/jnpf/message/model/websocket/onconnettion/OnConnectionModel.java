package jnpf.message.model.websocket.onconnettion;

import jnpf.message.model.websocket.model.MessageModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 刚连接websocket时推送的模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-07
 */
@Data
public class OnConnectionModel extends MessageModel implements Serializable {

    private List<String> onlineUsers;

    private List unreadNums;

    private Integer unreadNoticeCount;

    private String noticeDefaultText;

    private Integer unreadMessageCount;

    private Integer unreadScheduleCount;

    private Integer unreadSystemMessageCount;

    private String messageDefaultText;

    private Long messageDefaultTime;

    private Integer unreadTotalCount;

    private String userId;

}
