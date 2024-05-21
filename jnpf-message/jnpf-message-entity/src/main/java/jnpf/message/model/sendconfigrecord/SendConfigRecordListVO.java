

package jnpf.message.model.sendconfigrecord;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2022-09-21
 */
@Data
public class SendConfigRecordListVO {
    @Schema(description = "id")
    private String id;


    /**
     * 发送配置id
     **/
    @Schema(description = "发送配置id")
    @JSONField(name = "sendConfigId")
    private String sendConfigId;

    /**
     * 消息来源
     **/
    @Schema(description = "消息来源")
    @JSONField(name = "messageSource")
    private String messageSource;

    /**
     * 被引用id
     **/
    @Schema(description = "被引用id")
    @JSONField(name = "usedId")
    private String usedId;

    /**
     * 创建时间
     **/
    @Schema(description = "创建时间")
    @JSONField(name = "creatorTime")
    private Long creatorTime;

    /**
     * 创建人员
     **/
    @Schema(description = "创建人员")
    @JSONField(name = "creatorUserId")
    private String creatorUserId;

    /**
     * 修改时间
     **/
    @Schema(description = "修改时间")
    @JSONField(name = "lastModifyTime")
    private Long lastModifyTime;

    /**
     * 修改人员
     **/
    @Schema(description = "修改人员")
    @JSONField(name = "lastModifyUserId")
    private String lastModifyUserId;


}