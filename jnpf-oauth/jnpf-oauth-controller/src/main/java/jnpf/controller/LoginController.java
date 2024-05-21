package jnpf.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrPool;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.service.SysconfigService;
import jnpf.config.ConfigValueUtil;
import jnpf.config.JnpfOauthConfig;
import jnpf.constant.MsgCode;
import jnpf.consts.AuthConsts;
import jnpf.consts.DeviceType;
import jnpf.consts.LoginTicketStatus;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.LoginException;
import jnpf.granter.TokenGranter;
import jnpf.granter.TokenGranterBuilder;
import jnpf.granter.UserDetailsServiceBuilder;
import jnpf.model.BaseSystemInfo;
import jnpf.model.LoginConfigModel;
import jnpf.model.LoginForm;
import jnpf.model.LoginModel;
import jnpf.model.LoginTicketModel;
import jnpf.model.LoginVO;
import jnpf.model.WriteLogModel;
import jnpf.model.login.PcUserVO;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.socails.SocialsUserVo;
import jnpf.permission.model.user.UserUpdateModel;
import jnpf.permission.service.SocialsUserService;
import jnpf.permission.service.UserService;
import jnpf.service.AuthService;
import jnpf.service.LogService;
import jnpf.service.LoginService;
import jnpf.util.CodeUtil;
import jnpf.util.DownUtil;
import jnpf.util.Md5Util;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RedisUtil;
import jnpf.util.ServletUtil;
import jnpf.util.StringUtil;
import jnpf.util.TenantHolder;
import jnpf.util.TicketUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jnpf.consts.AuthConsts.PARAMS_JNPF_TICKET;

/**
 * 登录控制器
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "登陆数据", description = "oauth")
@Slf4j
@RestController
@RequestMapping("/api/oauth")
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private JnpfOauthConfig oauthConfig;
    @Autowired
    private SysconfigService sysConfigService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TokenGranterBuilder tokenGranterBuilder;
    @Autowired
    private SocialsUserService socialsUserService;
    @Autowired
    private UserDetailsServiceBuilder userDetailsServiceBuilder;
    @Autowired
    private LogService logService;

    /**
     * 登陆
     *
     * @param parameters 登录参数
     * @return
     * @throws LoginException 登录异常
     */
    @Operation(summary = "登陆")
    @Parameters({
            @Parameter(name = "parameters", description = "登录参数", required = true)
    })
    @RequestMapping(value = "/Login/**", method = {RequestMethod.GET, RequestMethod.POST})
    public ActionResult<LoginVO> login(@RequestParam Map<String, String> parameters) throws LoginException {
        return authService.login(parameters);
    }


    /**
     * 验证密码
     *
     * @param loginForm 登录模型
     * @return
     * @throws LoginException 登录异常
     */
    @Operation(summary = "锁屏解锁登录")
    @Parameters({
            @Parameter(name = "loginForm", description = "登录模型", required = true)
    })
    @PostMapping("/LockScreen")
    public ActionResult lockScreen(@RequestBody LoginForm loginForm) throws LoginException {
        UserEntity userEntity = userService.getUserByAccount(loginForm.getAccount());
        if (userEntity == null) {
            UserInfo userInfo = UserProvider.getUser();
            if (userInfo.getUserId() != null) {
                UserProvider.logoutByUserId(userInfo.getUserId());
            }
            throw new LoginException("账号不存在");
        }
        if (!Md5Util.getStringMd5(loginForm.getPassword().toLowerCase() + userEntity.getSecretkey().toLowerCase()).equals(userEntity.getPassword())) {
            throw new LoginException("账户或密码错误，请重新输入");
        }
        return ActionResult.success("验证成功");
    }

    /**
     * 登录注销
     *
     * @param grandtype 登录类型
     * @return
     */
    @NoDataSourceBind
    @Operation(summary = "登录注销")
    @Parameters({
            @Parameter(name = "grandtype", description = "登录类型", required = true)
    })
    @RequestMapping(value = {"/Logout", "/Logout/{grandtype}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ActionResult logout(@PathVariable(value = "grandtype", required = false) String grandtype) {
        long millis = System.currentTimeMillis();
        TokenGranter tokenGranter = tokenGranterBuilder.getGranterByLogin(grandtype);
        if(tokenGranter != null){
            UserInfo userInfo = UserProvider.getUser();
            try {
                WriteLogModel writeLogModel = WriteLogModel.builder()
                        .userId(userInfo.getUserId())
                        .userName(userInfo.getUserName() + "/" + userInfo.getUserAccount())
                        .abstracts("退出登录")
                        .userInfo(userInfo)
                        .loginMark(1)
                        .loginType(1)
                        .requestDuration(System.currentTimeMillis() - millis).build();

                logService.writeLogAsync(
                        writeLogModel.getUserId(), writeLogModel.getUserName(), writeLogModel.getAbstracts(),
                        writeLogModel.getUserInfo(), writeLogModel.getLoginMark(), writeLogModel.getLoginType(),
                        writeLogModel.getRequestDuration()
                );
            }catch (Exception e){
                e.printStackTrace();
            }
            return tokenGranter.logout();
        }
        return ActionResult.success();
    }

    /**
     * 踢出指定用户, 推送Websocket用户被强制下线
     *
     * @param tokens token集合
     * @param userId 租户id
     * @param tenantId 租户id
     */
    @NoDataSourceBind
    @Operation(summary = "踢出指定用户")
    @Parameters({
            @Parameter(name = "tokens", description = "token集合"),
            @Parameter(name = "userId", description = "租户id"),
            @Parameter(name = "tenantId", description = "租户id"),
    })
    @PostMapping(value = {"/KickoutToken"})
    public void kickoutByToken(@RequestParam(value = "tokens", required = false) String[] tokens, @RequestParam(name = "userId", required = false) String userId, @RequestParam(name = "tenantId", required = false) String tenantId) {
        if (StringUtil.isNotEmpty(tokens)) {
            authService.kickoutByToken(tokens);
        } else {
            authService.kickoutByUserId(userId, tenantId);
        }
    }

    /**
     * 获取用户登录信息
     *
     * @param type Web/App
     * @return
     * @throws LoginException 登录异常
     */
    @Operation(summary = "获取用户登录信息")
    @Parameters({
            @Parameter(name = "type", description = "Web/App")
    })
    @GetMapping("/CurrentUser")
    public ActionResult<PcUserVO> currentUser(String type, String systemCode) throws LoginException {
        if (StringUtil.isEmpty(type)) {
            type = "Web";
        } else {
            type = "App";
        }
        UserInfo userInfo = UserProvider.getUser();
        if(DeviceType.TEMPUSERLIMITED.getDevice().equals(userInfo.getLoginDevice())){
            throw new LoginException("限制会话, 不允许访问系统");
        }
        PcUserVO pcUserVO = loginService.getCurrentUser(type, systemCode);
        if (pcUserVO == null) {
            throw new LoginException("账户异常");
        }
        return ActionResult.success(pcUserVO);
    }

    /**
     * 修改密码信息发送
     *
     */
    @Operation(summary = "修改密码信息发送")
    @PostMapping("/updatePasswordMessage")
    public ActionResult updatePasswordMessage() {
        loginService.updatePasswordMessage();
        return ActionResult.success();
    }

    /**
     * 图形验证码
     *
     * @param codeLength 验证码长度
     * @param timestamp 验证码标识
     */
    @NoDataSourceBind()
    @Operation(summary = "图形验证码")
    @Parameters({
            @Parameter(name = "codeLength", description = "验证码长度", required = true),
            @Parameter(name = "timestamp", description = "验证码标识", required = true)
    })
    @GetMapping("/ImageCode/{codeLength}/{timestamp}")
    public void imageCode(@PathVariable("codeLength") Integer codeLength, @PathVariable("timestamp") String timestamp) {
        DownUtil.downCode(codeLength);
        redisUtil.insert(timestamp, ServletUtil.getSession().getAttribute(CodeUtil.RANDOMCODEKEY), 300);
    }

    /**
     * 注销用户
     *
     * @return
     */
    @Operation(summary = "注销用户")
    @PostMapping("/logoutCurrentUser")
    public ActionResult logoutCurrentUser() {
        UserInfo userInfo = UserProvider.getUser();
        if (userInfo.getIsAdministrator() != null && UserProvider.getUser().getIsAdministrator()) {
            return ActionResult.fail("管理员不能注销");
        }
        if (userInfo.getIsAdministrator() != null) {
            if (!userInfo.getIsAdministrator()) {
                userService.delete(userService.getInfo(userInfo.getUserId()));
                UserProvider.kickoutByUserId(userInfo.getUserId(), TenantHolder.getDatasourceId());
            }
        }
        return ActionResult.success("注销成功");
    }

    /**
     * 判断是否需要验证码
     *
     * @param account 账号
     */
    @NoDataSourceBind()
    @Operation(summary = "判断是否需要验证码")
    @Parameters({
            @Parameter(name = "account", description = "账号", required = true)
    })
    @GetMapping("/getConfig/{account}")
    public ActionResult<LoginModel> check(@PathVariable("account") String account) throws LoginException {
        LoginModel loginModel = new LoginModel();
        String tenantId = "0";
        if (configValueUtil.isMultiTenancy()) {
            LoginForm loginForm = new LoginForm();
            loginForm.setAccount(account);
            UserInfo userInfo = new UserInfo();
            userInfo.setUserAccount(loginForm.getAccount());
            userInfo = loginService.getTenantAccount(userInfo);
            tenantId = userInfo.getTenantId();
        }
        // 获取配置
        BaseSystemInfo sysConfigInfo = sysConfigService.getSysInfo(tenantId);
        // 是否开启验证码
        if (Objects.nonNull(sysConfigInfo) && "1".equals(String.valueOf(sysConfigInfo.getEnableVerificationCode()))) {
            loginModel.setEnableVerificationCode(1);
            Integer verificationCodeNumber = sysConfigInfo.getVerificationCodeNumber();
            loginModel.setVerificationCodeNumber(verificationCodeNumber == null ? 4 : verificationCodeNumber);
            return ActionResult.success(loginModel);
        }
        loginModel.setEnableVerificationCode(0);
        return ActionResult.success(loginModel);
    }

    /**
     * 获取登录配置, 是否需要跳转、第三方登录信息
     *
     * @return {re}
     * @throws LoginException 登录异常
     */
    @NoDataSourceBind()
    @Operation(summary = "获取登录配置")
    @GetMapping("/getLoginConfig")
    public ActionResult<LoginConfigModel> getLoginConfig() {
        LoginConfigModel loginConfigModel = new LoginConfigModel();
        if(oauthConfig.getSsoEnabled()){
            String url = oauthConfig.getLoginPath() + StrPool.SLASH + oauthConfig.getDefaultSSO();
            loginConfigModel.setRedirect(true);
            loginConfigModel.setUrl(url);
            loginConfigModel.setTicketParams(PARAMS_JNPF_TICKET);
        } else {
            //追加第三方登录配置
            List<SocialsUserVo> loginList = socialsUserService.getLoginList(PARAMS_JNPF_TICKET.toUpperCase());
            if (CollectionUtil.isNotEmpty(loginList)) {
                loginConfigModel.setSocialsList(loginList);
                loginConfigModel.setRedirect(false);
                loginConfigModel.setTicketParams(PARAMS_JNPF_TICKET);
            }
        }
        return ActionResult.success(loginConfigModel);
    }


    /**
     * 获取登录票据
     * @return {msg:有效期, data:票据}
     */
    @NoDataSourceBind()
    @Operation(summary = "获取登录票据")
    @GetMapping("/getTicket")
    public ActionResult<String> getTicket() {
        LoginTicketModel ticketModel = new LoginTicketModel();
        ticketModel.setTicketTimeout(System.currentTimeMillis() + oauthConfig.getTicketTimeout()*1000);
        String ticket = TicketUtil.createTicket(ticketModel, oauthConfig.getTicketTimeout());
        return ActionResult.success(ticketModel.getTicketTimeout().toString(), ticket);
    }

    /**
     * 检测票据登录状态
     *
     * @param ticket ticket
     * @return
     */
    @NoDataSourceBind()
    @Operation(summary = "获取登录状态")
    @Parameter(name = "ticket", description = "ticket", required = true)
    @GetMapping("/getTicketStatus/{ticket}")
    public ActionResult<LoginTicketModel> getTicketStatus(@PathVariable("ticket") String ticket) {
        LoginTicketModel ticketModel = TicketUtil.parseTicket(ticket);
        if (ticketModel == null) {
            ticketModel = new LoginTicketModel().setStatus(LoginTicketStatus.Invalid.getStatus()).setValue("票据失效！");
        } else {
            if (ticketModel.getStatus() != LoginTicketStatus.UnLogin.getStatus() &&ticketModel.getStatus() != LoginTicketStatus.UnBind.getStatus()) {
                TicketUtil.deleteTicket(ticket);
            }
        }
        return ActionResult.success(ticketModel);
    }

    /**
     * 官网重置密码专用
     *
     * @return
     */
    @NoDataSourceBind()
    @Operation(summary = "官网重置密码专用")
    @GetMapping("/resetOfficialPassword/{mobile}/{code}")
    public ActionResult resetOfficialPassword(@PathVariable("mobile") String mobile, @PathVariable("code")  String code) throws LoginException {
        //校验验证码
        TenantDataSourceUtil.checkOfficialSmsCode(mobile, code, 2);
        //切换租户
        LoginForm loginForm = new LoginForm();
        loginForm.setAccount(mobile);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserAccount(loginForm.getAccount());
        try{
            userInfo = loginService.getTenantAccount(userInfo);
        }catch (Exception e){
            return ActionResult.fail(MsgCode.LOG105.get());
        }

        // 重置密码 123456
        UserEntity userEntity = userDetailsServiceBuilder.getUserDetailService(AuthConsts.USERDETAIL_ACCOUNT).loadUserEntity(userInfo);
        userEntity.setPassword("e10adc3949ba59abbe56e057f20f883e");
        userEntity.setPassword(Md5Util.getStringMd5(userEntity.getPassword().toLowerCase() + userEntity.getSecretkey().toLowerCase()));
        boolean result = userService.updateByIdAndTenantId(new UserUpdateModel(userEntity, userInfo.getTenantId()));
        if(result){
            return ActionResult.success(MsgCode.LOG205.get());
        }else{
            return ActionResult.fail(MsgCode.LOG206.get());
        }
    }

}
