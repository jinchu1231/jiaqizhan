package jnpf.integrate.model.integrate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WebHookInfoVo {
    @Schema(description = "系统生成数据接收接口")
    private String webhookUrl;
    @Schema(description = "系统生成参数接收接口")
    private String requestUrl;
    @Schema(description = "base64未转换16进制字符串")
    private String enCodeStr;
    @Schema(description = "随机字符")
    private String randomStr;
}
