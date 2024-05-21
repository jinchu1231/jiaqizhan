package jnpf.base.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserOnlineVO {
    private String userId;
    private String userName;
    private String loginTime;
    private String loginIPAddress;
    private String loginSystem;
    @Schema(description = "所属组织")
    private String organize;
    @Schema(description = "浏览器")
    private String loginBrowser;
    @Schema(description = "登录地址")
    private String loginAddress;
}
