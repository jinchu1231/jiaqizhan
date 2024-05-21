package jnpf.permission.util.socials;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.support.HttpHeader;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.enums.AuthUserGender;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/7/15 17:19:14
 */
public class AuthDingTalkNewRequest extends AuthDefaultRequest {

    public AuthDingTalkNewRequest(AuthConfig config) {
        super(config, AuthDefaultSourceNew.DINGTALK_NEW);
    }

    public AuthDingTalkNewRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSourceNew.DINGTALK_NEW, authStateCache);
    }

    public String authorize(String state) {
        String encode = null;
        try {
            encode = URLEncoder.encode(this.config.getRedirectUri(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ;
        return UrlBuilder.fromBaseUrl(this.source.authorize()).queryParam("response_type", "code").queryParam("client_id", this.config.getClientId()).queryParam("scope", "openid").queryParam("redirect_uri", encode).queryParam("prompt", "consent").build();
    }

    protected AuthToken getAccessToken(AuthCallback authCallback) {
        return this.getToken(this.accessTokenUrl(authCallback.getCode()), authCallback);
    }

    protected String accessTokenUrl(String code) {
        return UrlBuilder.fromBaseUrl(this.source.accessToken()).build();
    }

    public AuthResponse login(AuthCallback authCallback) {
        try {
            this.checkCode(authCallback);
            AuthToken authToken = this.getAccessToken(authCallback);
            AuthUser user = this.getUserInfo(authToken);
            return AuthResponse.builder().code(AuthResponseStatus.SUCCESS.getCode()).data(user).build();
        } catch (Exception var4) {
            Log.error("Failed to login with oauth authorization.", var4);
            return this.responseError(var4);
        }
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

    private AuthToken getToken(String accessTokenUrl, AuthCallback authCallback) {
        JSONObject map = new JSONObject();
        map.put("clientId", this.config.getClientId());
        map.put("clientSecret", this.config.getClientSecret());
        map.put("code", authCallback.getCode());
        map.put("refreshToken", authCallback.getCode());
        map.put("grantType", "authorization_code");
        String response = (new HttpUtils(this.config.getHttpConfig())).post(accessTokenUrl, map.toJSONString(), new HttpHeader().add("Content-Type", "application/json")).getBody();
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AuthToken.builder().accessToken(accessTokenObject.getString("accessToken")).refreshToken(accessTokenObject.getString("refreshToken")).expireIn(accessTokenObject.getIntValue("expireIn")).openId(accessTokenObject.getString("openid")).build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("errcode")) {
            throw new AuthException(object.getIntValue("errcode"), object.getString("errmsg"));
        }
    }

    protected AuthUser getUserInfo(AuthToken authToken) {
        HttpHeader httpHeader = new HttpHeader()
                .add("x-acs-dingtalk-access-token", authToken.getAccessToken())
                .add("Content-Type", "application/json");
        String response = (new HttpUtils(this.config.getHttpConfig())).get(this.userInfoUrl(authToken), null, httpHeader, false).getHttpResponse().getBody();
        JSONObject object = JSON.parseObject(response);
        if (object.get("unionId") != null) {
            AuthToken token = AuthToken.builder().openId(object.getString("openId")).unionId(object.getString("unionId")).build();
            return AuthUser.builder().rawUserInfo(object).uuid(object.getString("unionId")).nickname(object.getString("nick")).username(object.getString("nick")).gender(AuthUserGender.UNKNOWN).source(this.source.toString()).token(token).build();
        } else {
            throw new AuthException("登录失败！");
        }
    }

    protected String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(this.source.userInfo()).build();
    }
}
