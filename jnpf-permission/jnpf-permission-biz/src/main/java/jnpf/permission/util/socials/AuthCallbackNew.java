package jnpf.permission.util.socials;

import lombok.Data;
import me.zhyd.oauth.model.AuthCallback;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/7/21 17:17:44
 */
@Data
public class AuthCallbackNew extends AuthCallback {
    private String authCode;
}
