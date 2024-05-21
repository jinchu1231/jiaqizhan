package jnpf.granter;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.consts.AuthConsts;
import jnpf.consts.LoginTicketStatus;
import jnpf.exception.LoginException;
import jnpf.model.BaseSystemInfo;
import jnpf.model.LoginTicketModel;
import jnpf.model.LoginVO;
import jnpf.model.SocialUnbindModel;
import jnpf.permission.controller.SocialsUserController;
import jnpf.permission.model.socails.SocialsUserInfo;
import jnpf.util.ServletUtil;
import jnpf.util.StringUtil;
import jnpf.util.TicketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

import static jnpf.granter.SocialsTokenGranter.GRANT_TYPE;


@Slf4j
@Component(GRANT_TYPE)
public class SocialsTokenGranter extends AbstractTokenGranter {


    public static final String GRANT_TYPE = "socials";
    public static final Integer ORDER = 5;
    private static final String URL_LOGIN = "/Login/socials/**";

    @Autowired
    private SocialsUserController socialsUserApi;

    public SocialsTokenGranter() {
        super(URL_LOGIN);
    }

    protected String getGrantType() {
        return GRANT_TYPE;
    }

    public ActionResult granter(Map<String, String> map) throws LoginException {
        SaRequest req = SaHolder.getRequest();
        String code = req.getParam("code");
        String state = req.getParam("state");
        String source = req.getParam("source");
        String uuid=req.getParam("uuid");
        if (StringUtil.isEmpty(code)) {
            code = req.getParam("authCode") != null ? req.getParam("authCode") : req.getParam("auth_code");
        }
        //是否是微信qq唤醒或者小程序登录
        if(StringUtil.isNotEmpty(uuid)){
            try {
                return loginByCode(source, code, null,uuid,null);
            } catch (Exception e) {
                //更新登录结果
                outError(e.getMessage());
            }
        }
        if (StringUtil.isEmpty(req.getParam(AuthConsts.PARAMS_JNPF_TICKET))) {
            //租户列表登陆标识
            boolean  tenantLogin=StringUtil.isEmpty(req.getParam("tenantLogin"))?false:req.getParam("tenantLogin").equals("true")?true:false;
            //租户列表点击登录调用
            if (!tenantLogin) {
                //绑定
                socialsBinding(req, code, state, source);
                return null;
            }else{//租户列表点击登录
                LoginVO loginVO = tenantLogin(req);
                return ActionResult.success("登录成功！",loginVO);
            }
        } else {
            //票据登陆
            if (!isValidJnpfTicket()) {
                outError("登录票据已失效");
                return null;
            }
            //接受CODE 进行登录
            if (SaFoxUtil.isNotEmpty(code)) {
                try {
                    String socialName= req.getParam("socialName");
                    ActionResult actionResult = loginByCode(source, code, state, null, socialName);
                    if(400==actionResult.getCode()||"wechat_applets".equals(req.getParam("source"))){
                        return actionResult;
                    }
                    return null;
                } catch (Exception e) {
                    //更新登录结果
                    outError(e.getMessage());
                }
                return null;
            }
            return null;
        }

    }

    /**
     * 租户列表登录
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/9/21
     */
    private LoginVO tenantLogin(SaRequest req) throws LoginException {
        String userId = req.getParam("userId");
        String account = req.getParam("account");
        String tenantId = req.getParam("tenantId");
        UserInfo userInfo=new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setTenantId(tenantId);
        userInfo.setUserAccount(tenantId+"@"+account);
        //切换租户
        switchTenant(userInfo);
        //获取系统配置
        BaseSystemInfo baseSystemInfo = getSysconfig(userInfo);
        //登录账号
        super.loginAccount(userInfo, baseSystemInfo);
        //返回登录信息
        LoginVO loginVo = getLoginVo(userInfo);
        return loginVo;
    }
    /**
     * 第三方绑定
     */
    private void socialsBinding(SaRequest req, String code, String state, String source) {
        String userId = req.getParam("userId");
        String tenantId = req.getParam("tenantId");
        PrintWriter out = null;
        try {
            HttpServletResponse response = ServletUtil.getResponse();
            response.setCharacterEncoding("utf-8");
            response.setHeader("content-type", "text/html;charset=utf-8");
            out = response.getWriter();
            JSONObject binding = socialsUserApi.binding(source, userId, tenantId, code, state);
            out.print(
                    "<script>\n" +
                            "window.opener.postMessage(\'" + binding.toJSONString() + "\', '*');\n" +
                            "window.open('','_self','');\n" +
                            "window.close();\n" + "</script>");
            out.close();
        } catch (Exception e) {

        }
    }

    public ActionResult logout() {
        return super.logout();
    }

    public int getOrder() {
        return ORDER;
    }

    private void loginUser(UserInfo userInfo) throws LoginException {
        //切换租户
        switchTenant(userInfo);
        //获取系统配置
        BaseSystemInfo baseSystemInfo = getSysconfig(userInfo);
        //登录账号
        super.loginAccount(userInfo, baseSystemInfo);
    }

    @Override
    protected void loginSuccess(UserInfo userInfo, BaseSystemInfo baseSystemInfo) {
        super.loginSuccess(userInfo, baseSystemInfo);
        //更新轮询登录结果
        updateTicketSuccess(userInfo);
    }

    @Override
    protected void loginFailure(UserInfo userInfo, BaseSystemInfo baseSystemInfo, Exception e) {
        super.loginFailure(userInfo, baseSystemInfo, e);
    }


    protected void outError(String message) {
        updateTicketError(message);
    }

    @Override
    protected String getUserDetailKey() {
        return AuthConsts.USERDETAIL_USER_ID;
    }

    protected void updateTicketUnbind(String socialType,String socialUnionid,String socialName) {
        String ticket = this.getJnpfTicket();
        SocialUnbindModel obj=new SocialUnbindModel(socialType,socialUnionid,socialName);
        if (!ticket.isEmpty()) {
            LoginTicketModel loginTicketModel = (new LoginTicketModel()).setStatus(LoginTicketStatus.UnBind.getStatus()).setValue(JSONObject.toJSONString(obj));
            TicketUtil.updateTicket(ticket, loginTicketModel, (Long) 300L);
        }
    }

    protected LoginTicketModel updateTicketSuccessReturn(UserInfo userInfo){
        LoginTicketModel loginTicketModel = null;
        String ticket = getJnpfTicket();
        if(!ticket.isEmpty()) {
            loginTicketModel = new LoginTicketModel()
                    .setStatus(LoginTicketStatus.Success.getStatus())
                    .setValue(StpUtil.getTokenValueNotCut())
                    .setTheme(userInfo.getTheme());
            TicketUtil.updateTicket(ticket, loginTicketModel, null);
        }
        return loginTicketModel;
    }

    protected LoginTicketModel updateTicketMultitenancy(JSONArray jsonArray) {
        LoginTicketModel loginTicketModel = null;
        String ticket = this.getJnpfTicket();
        if (!ticket.isEmpty()) {
            loginTicketModel = (new LoginTicketModel()).setStatus(LoginTicketStatus.Multitenancy.getStatus()).setValue(jsonArray.toJSONString());
            TicketUtil.updateTicket(ticket, loginTicketModel, (Long) null);
        }
        return loginTicketModel;
    }

    protected LoginVO getLoginVo(UserInfo userInfo){
        LoginVO loginVO = new LoginVO();
        loginVO.setTheme(userInfo.getTheme());
        loginVO.setToken(userInfo.getToken());
        return loginVO;
    }
    /**
     * 小程序登录微信授权
     * app微信，qq唤醒
     *
     * @param code
     * @throws LoginException
     */
    protected ActionResult loginByCode(String source,String code,String state,String uuid,String socialName) throws LoginException {
        log.debug("Auth2 Code: {}", code);
        SocialsUserInfo socialsUserInfo =null;
        if (StringUtil.isNotEmpty(code)) {
            socialsUserInfo = socialsUserApi.getSocialsUserInfo(source, code, state);
        } else if (StringUtil.isNotEmpty(uuid)) {//微信和qq唤醒
            socialsUserInfo = socialsUserApi.getUserInfo(source, uuid, state);
            if (StringUtil.isEmpty(socialsUserInfo.getSocialName()) && StringUtil.isNotEmpty(socialName)) {
                socialsUserInfo.setSocialName(socialName);//小程序名称前端传递
            }
        }
        if (configValueUtil.isMultiTenancy()) {
            if (socialsUserInfo==null||CollectionUtil.isEmpty(socialsUserInfo.getTenantUserInfo())) {
                updateTicketUnbind(source,socialsUserInfo.getSocialUnionid(),socialsUserInfo.getSocialName());//第三方未绑定账号!
                return ActionResult.fail("第三方未绑定账号!");
            }
            if (socialsUserInfo.getTenantUserInfo().size() == 1) {
                UserInfo userInfo = socialsUserInfo.getUserInfo();
                //切换租户
                switchTenant(userInfo);
                //获取系统配置
                BaseSystemInfo baseSystemInfo = getSysconfig(userInfo);
                //登录账号
                super.loginAccount(userInfo, baseSystemInfo);
                //返回登录信息
                LoginTicketModel loginTicketModel = updateTicketSuccessReturn(userInfo);
                return ActionResult.success(loginTicketModel);
            } else {
                JSONArray tenantUserInfo = socialsUserInfo.getTenantUserInfo();
                LoginTicketModel loginTicketModel = updateTicketMultitenancy(tenantUserInfo);
                return ActionResult.success(loginTicketModel);
            }
        } else {
            if (socialsUserInfo==null||socialsUserInfo.getUserInfo() == null) {
                updateTicketUnbind(source,socialsUserInfo.getSocialUnionid(),socialsUserInfo.getSocialName());//第三方未绑定账号!
                return ActionResult.fail("第三方未绑定账号!");
            }
            UserInfo userInfo = socialsUserInfo.getUserInfo();
            //切换租户
            switchTenant(userInfo);
            //获取系统配置
            BaseSystemInfo baseSystemInfo = getSysconfig(userInfo);
            //登录账号
            super.loginAccount(userInfo, baseSystemInfo);
            LoginTicketModel loginTicketModel = updateTicketSuccessReturn(userInfo);
            return ActionResult.success(loginTicketModel);
        }
    }
}
