package jnpf.base.model.synthirdinfo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钉钉发送信息配置模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/22 14:12
 */
@Data
public class DingTalkModel {
//    private String dingAppKey;
//    private String dingAppSecret;
    @Schema(description = "SynAppKey")
    private String dingSynAppKey;
    @Schema(description = "SynAppSecret")
    private String dingSynAppSecret;
    @Schema(description = "AgentId")
    private String dingAgentId;
}
