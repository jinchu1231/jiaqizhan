package jnpf.permission.util.socials;

import com.xkcoding.http.config.HttpConfig;
import jnpf.config.JnpfOauthConfig;
import jnpf.util.StringUtil;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.scope.AuthBaiduScope;
import me.zhyd.oauth.enums.scope.AuthCodingScope;
import me.zhyd.oauth.enums.scope.AuthFacebookScope;
import me.zhyd.oauth.enums.scope.AuthGiteeScope;
import me.zhyd.oauth.enums.scope.AuthGithubScope;
import me.zhyd.oauth.enums.scope.AuthGitlabScope;
import me.zhyd.oauth.enums.scope.AuthGoogleScope;
import me.zhyd.oauth.enums.scope.AuthHuaweiScope;
import me.zhyd.oauth.enums.scope.AuthMicrosoftScope;
import me.zhyd.oauth.enums.scope.AuthWeiboScope;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.AuthAlipayRequest;
import me.zhyd.oauth.request.AuthAliyunRequest;
import me.zhyd.oauth.request.AuthBaiduRequest;
import me.zhyd.oauth.request.AuthCodingRequest;
import me.zhyd.oauth.request.AuthCsdnRequest;
import me.zhyd.oauth.request.AuthDouyinRequest;
import me.zhyd.oauth.request.AuthElemeRequest;
import me.zhyd.oauth.request.AuthFacebookRequest;
import me.zhyd.oauth.request.AuthFeishuRequest;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthGitlabRequest;
import me.zhyd.oauth.request.AuthGoogleRequest;
import me.zhyd.oauth.request.AuthHuaweiRequest;
import me.zhyd.oauth.request.AuthKujialeRequest;
import me.zhyd.oauth.request.AuthLinkedinRequest;
import me.zhyd.oauth.request.AuthMeituanRequest;
import me.zhyd.oauth.request.AuthMiRequest;
import me.zhyd.oauth.request.AuthMicrosoftRequest;
import me.zhyd.oauth.request.AuthOschinaRequest;
import me.zhyd.oauth.request.AuthPinterestRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRenrenRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthStackOverflowRequest;
import me.zhyd.oauth.request.AuthTaobaoRequest;
import me.zhyd.oauth.request.AuthTeambitionRequest;
import me.zhyd.oauth.request.AuthToutiaoRequest;
import me.zhyd.oauth.request.AuthTwitterRequest;
import me.zhyd.oauth.request.AuthWeChatEnterpriseQrcodeRequest;
import me.zhyd.oauth.request.AuthWeChatEnterpriseWebRequest;
import me.zhyd.oauth.request.AuthWeChatMpRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.request.AuthWeiboRequest;
import me.zhyd.oauth.request.AuthXmlyRequest;
import me.zhyd.oauth.utils.AuthScopeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/7/21 12:00:56
 */
@Component
public class AuthSocialsUtil {
    @Autowired
    private SocialsConfig socialsConfig;
    @Autowired
    private JnpfOauthConfig oauthConfig;

    /**
     * 根据配置信息获取请求对象
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/7/21
     */
    public AuthRequest getAuthRequest(String source, String userId, boolean isLogin, String ticket, String tenantId) {
        AuthRequest authRequest = null;
        String addUrlStr = "";
        String urlStr = oauthConfig.getJnpfDomain() + "/api/oauth/Login/socials?source=" + source;
        if (!isLogin) {
            urlStr = oauthConfig.getJnpfDomain() + "/api/oauth/Login/socials?source=" + source;
        }
        if (StringUtil.isNotEmpty(userId)) {
            addUrlStr = "&userId=" + userId;
        }
        if (StringUtil.isNotEmpty(ticket)) {
            addUrlStr = "&jnpf_ticket=" + ticket;
        }
        if (StringUtil.isNotEmpty(tenantId)) {
            addUrlStr += "&tenantId=" + tenantId;
        }
        String url = urlStr + addUrlStr;
        SocialsConfig.Config socialConfig;
        switch (source.toLowerCase()) {
            //todo 官方登录api调整目前数据问题
            case "dingtalk":
                socialConfig = socialsConfig.getSocialMap().get("dingtalk");
                authRequest = new AuthDingTalkNewRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            //todo 未申请企业
            case "qq":
                socialConfig = socialsConfig.getSocialMap().get("qq");
                authRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "wechat_open":
                socialConfig = socialsConfig.getSocialMap().get("wechat_open");
                authRequest = new AuthWeChatOpenRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(URLEncoder.encode(url))
                        .build());
                break;
            case "github":
                socialConfig = socialsConfig.getSocialMap().get("github");
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(URLEncoder.encode(url))
                        .scopes(AuthScopeUtils.getScopes(AuthGithubScope.values()))
                        // 针对国外平台配置代理
//                        .httpConfig(HttpConfig.builder()
//                                .timeout(15000)
//                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
//                                .build())
                        .build());
                break;
            case "wechat_enterprise":
                socialConfig = socialsConfig.getSocialMap().get("wechat_enterprise");
                authRequest = new AuthWeChatEnterpriseQrcodeRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(URLEncoder.encode(url))
                        .agentId(socialConfig.getAgentId())
                        .build());
                break;
            case "feishu":
                socialConfig = socialsConfig.getSocialMap().get("feishu");
                authRequest = new AuthFeishuRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "baidu":
                socialConfig = socialsConfig.getSocialMap().get("baidu");
                authRequest = new AuthBaiduRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(Arrays.asList(
                                AuthBaiduScope.BASIC.getScope(),
                                AuthBaiduScope.SUPER_MSG.getScope(),
                                AuthBaiduScope.NETDISK.getScope()
                        ))
//                        .clientId("")
//                        .clientSecret("")
//                        .redirectUri("http://localhost:9001/oauth/baidu/callback")
                        .build());
                break;
            case "gitee":
                socialConfig = socialsConfig.getSocialMap().get("gitee");
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(AuthScopeUtils.getScopes(AuthGiteeScope.values()))
                        .build());
                break;
            case "weibo":
                socialConfig = socialsConfig.getSocialMap().get("weibo");
                authRequest = new AuthWeiboRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(Arrays.asList(
                                AuthWeiboScope.EMAIL.getScope(),
                                AuthWeiboScope.FRIENDSHIPS_GROUPS_READ.getScope(),
                                AuthWeiboScope.STATUSES_TO_ME_READ.getScope()
                        ))
                        .build());
                break;
            case "coding":
                socialConfig = socialsConfig.getSocialMap().get("coding");
                authRequest = new AuthCodingRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .domainPrefix("")
                        .scopes(Arrays.asList(
                                AuthCodingScope.USER.getScope(),
                                AuthCodingScope.USER_EMAIL.getScope(),
                                AuthCodingScope.USER_PHONE.getScope()
                        ))
                        .build());
                break;
            case "oschina":
                socialConfig = socialsConfig.getSocialMap().get("oschina");
                authRequest = new AuthOschinaRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "alipay":
                socialConfig = socialsConfig.getSocialMap().get("alipay");
                // 支付宝在创建回调地址时，不允许使用localhost或者127.0.0.1，所以这儿的回调地址使用的局域网内的ip
                authRequest = new AuthAlipayRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .alipayPublicKey("")
                        .build());
                break;
            case "csdn":
                socialConfig = socialsConfig.getSocialMap().get("csdn");
                authRequest = new AuthCsdnRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "taobao":
                socialConfig = socialsConfig.getSocialMap().get("taobao");
                authRequest = new AuthTaobaoRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "google":
                socialConfig = socialsConfig.getSocialMap().get("google");
                authRequest = new AuthGoogleRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(AuthScopeUtils.getScopes(AuthGoogleScope.USER_EMAIL, AuthGoogleScope.USER_PROFILE, AuthGoogleScope.USER_OPENID))
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build());
                break;
            case "facebook":
                socialConfig = socialsConfig.getSocialMap().get("facebook");
                authRequest = new AuthFacebookRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(AuthScopeUtils.getScopes(AuthFacebookScope.values()))
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build());
                break;
            case "douyin":
                socialConfig = socialsConfig.getSocialMap().get("douyin");
                authRequest = new AuthDouyinRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "linkedin":
                socialConfig = socialsConfig.getSocialMap().get("linkedin");
                authRequest = new AuthLinkedinRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(null)
                        .build());
                break;
            case "microsoft":
                socialConfig = socialsConfig.getSocialMap().get("microsoft");
                authRequest = new AuthMicrosoftRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(Arrays.asList(
                                AuthMicrosoftScope.USER_READ.getScope(),
                                AuthMicrosoftScope.USER_READWRITE.getScope(),
                                AuthMicrosoftScope.USER_READBASIC_ALL.getScope(),
                                AuthMicrosoftScope.USER_READ_ALL.getScope(),
                                AuthMicrosoftScope.USER_READWRITE_ALL.getScope(),
                                AuthMicrosoftScope.USER_INVITE_ALL.getScope(),
                                AuthMicrosoftScope.USER_EXPORT_ALL.getScope(),
                                AuthMicrosoftScope.USER_MANAGEIDENTITIES_ALL.getScope(),
                                AuthMicrosoftScope.FILES_READ.getScope()
                        ))
                        .build());
                break;
            case "mi":
                socialConfig = socialsConfig.getSocialMap().get("mi");
                authRequest = new AuthMiRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "toutiao":
                socialConfig = socialsConfig.getSocialMap().get("toutiao");
                authRequest = new AuthToutiaoRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "teambition":
                socialConfig = socialsConfig.getSocialMap().get("teambition");
                authRequest = new AuthTeambitionRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "pinterest":
                socialConfig = socialsConfig.getSocialMap().get("pinterest");
                authRequest = new AuthPinterestRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build());
                break;
            case "renren":
                socialConfig = socialsConfig.getSocialMap().get("renren");
                authRequest = new AuthRenrenRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "stack_overflow":
                socialConfig = socialsConfig.getSocialMap().get("stack_overflow");
                authRequest = new AuthStackOverflowRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .stackOverflowKey("")
                        .build());
                break;
            case "huawei":
                socialConfig = socialsConfig.getSocialMap().get("huawei");
                authRequest = new AuthHuaweiRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(Arrays.asList(
                                AuthHuaweiScope.BASE_PROFILE.getScope(),
                                AuthHuaweiScope.MOBILE_NUMBER.getScope(),
                                AuthHuaweiScope.ACCOUNTLIST.getScope(),
                                AuthHuaweiScope.SCOPE_DRIVE_FILE.getScope(),
                                AuthHuaweiScope.SCOPE_DRIVE_APPDATA.getScope()
                        ))
                        .build());
                break;
            case "kujiale":
                socialConfig = socialsConfig.getSocialMap().get("kujiale");
                authRequest = new AuthKujialeRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "gitlab":
                socialConfig = socialsConfig.getSocialMap().get("gitlab");
                authRequest = new AuthGitlabRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .scopes(AuthScopeUtils.getScopes(AuthGitlabScope.values()))
                        .build());
                break;
            case "meituan":
                socialConfig = socialsConfig.getSocialMap().get("meituan");
                authRequest = new AuthMeituanRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "eleme":
                socialConfig = socialsConfig.getSocialMap().get("eleme");
                authRequest = new AuthElemeRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
//            case "mygitlab":
//                authRequest = new AuthMyGitlabRequest(AuthConfig.builder()
//                        .clientId("")
//                        .clientSecret("")
//                        .redirectUri("http://127.0.0.1:8443/oauth/callback/mygitlab")
//                        .build());
//                break;
            case "twitter":
                socialConfig = socialsConfig.getSocialMap().get("twitter");
                authRequest = new AuthTwitterRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)))
                                .build())
                        .build());
                break;
            case "wechat_mp":
                socialConfig = socialsConfig.getSocialMap().get("wechat_mp");
                authRequest = new AuthWeChatMpRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "aliyun":
                socialConfig = socialsConfig.getSocialMap().get("aliyun");
                authRequest = new AuthAliyunRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "xmly":
                socialConfig = socialsConfig.getSocialMap().get("xmly");
                authRequest = new AuthXmlyRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            case "wechat_enterprise_web":
                socialConfig = socialsConfig.getSocialMap().get("wechat_enterprise_web");
                authRequest = new AuthWeChatEnterpriseWebRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .agentId(socialConfig.getAgentId())
                        .build());
                break;
            case "wechat_applets":
                socialConfig = socialsConfig.getSocialMap().get("wechat_applets");
                authRequest = new AuthWechatAppletsRequest(AuthConfig.builder()
                        .clientId(socialConfig.getClientId())
                        .clientSecret(socialConfig.getClientSecret())
                        .redirectUri(url)
                        .build());
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException("未获取到有效的Auth配置");
        }
        return authRequest;
    }
}
