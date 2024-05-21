package jnpf.permission.util.socials;

import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.log.Log;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.StringUtils;
import me.zhyd.oauth.utils.UrlBuilder;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/9/16 12:11:36
 */
public class AuthWechatAppletsRequest extends AuthDefaultRequest {
    public AuthWechatAppletsRequest(AuthConfig config) {
        super(config, AuthDefaultSourceNew.WECHAT_APPLETS);
    }

    public AuthWechatAppletsRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSourceNew.WECHAT_APPLETS, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        return null;
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        return null;
    }

    protected String getuserInfoUrl(AuthCallback authCallback) {
        return UrlBuilder.fromBaseUrl(this.source.userInfo())
                .queryParam("appid", this.config.getClientId())
                .queryParam("secret", this.config.getClientSecret())
                .queryParam("js_code",authCallback.getCode())
                .queryParam("grant_type", "authorization_code").build();

    }

    AuthResponse responseError(Exception e) {
        int errorCode = AuthResponseStatus.FAILURE.getCode();
        String errorMsg = e.getMessage();
        if (e instanceof AuthException) {
            AuthException authException = (AuthException) e;
            errorCode = authException.getErrorCode();
            if (StringUtils.isNotEmpty(authException.getErrorMsg())) {
                errorMsg = authException.getErrorMsg();
            }
        }

        return AuthResponse.builder().code(errorCode).msg(errorMsg).build();
    }
    public AuthResponse login(AuthCallback authCallback) {
        try {
            AuthUser user = this.getUserUnionid(authCallback);
            return AuthResponse.builder().code(AuthResponseStatus.SUCCESS.getCode()).data(user).build();
        } catch (Exception var4) {
            Log.error("Failed to login with oauth authorization.", var4);
            return this.responseError(var4);
        }
    }

    protected AuthUser getUserUnionid(AuthCallback authCallback) {
        String response = (new HttpUtils(this.config.getHttpConfig())).get(this.getuserInfoUrl(authCallback)).getBody();
        JSONObject object = JSONObject.parseObject(response);
        AuthToken authToken=new AuthToken();
        if (object.containsKey("unionid")) {
            authToken.setUnionId(object.getString("unionid"));
        }
        return AuthUser.builder().rawUserInfo(object).token(authToken).source(this.source.toString()).build();
    }


}
