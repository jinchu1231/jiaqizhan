package jnpf.message.model.websocket;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息列表单个模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-05
 */
@Data
public class MessageListVo implements Serializable {

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private String id;

    /**
     * 已读时间
     */
    @Schema(description = "已读时间")
    private Long receiveTime;

    /**
     * 已读用户id
     */
    @Schema(description = "已读用户id")
    private String receiveUserId;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private Long sendTime;

    /**
     * 发送用户id
     */
    @Schema(description = "发送用户id")
    private String sendUserId;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer state;

}
