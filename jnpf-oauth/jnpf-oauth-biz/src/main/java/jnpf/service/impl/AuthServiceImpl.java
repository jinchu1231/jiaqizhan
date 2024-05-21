package jnpf.service.impl;

import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.exception.LoginException;
import jnpf.granter.TokenGranter;
import jnpf.granter.TokenGranterBuilder;
import jnpf.model.LoginVO;
import jnpf.model.WriteLogModel;
import jnpf.service.AuthService;
import jnpf.service.LogService;
import jnpf.util.StringUtil;
import jnpf.util.TenantProvider;
import jnpf.util.UserProvider;
import jnpf.utils.LoginHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 登录与退出服务 其他服务调用
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private TokenGranterBuilder tokenGranterBuilder;
    @Autowired
    private LogService logService;

    /**
     * 登录
     * @param parameters {grant_type}
     * @return
     * @throws LoginException
     */
    public ActionResult<LoginVO> login(Map<String, String> parameters) throws LoginException{
        long millis = System.currentTimeMillis();
        TokenGranter tokenGranter = tokenGranterBuilder.getGranter(parameters.getOrDefault("grant_type", ""));
        ActionResult<LoginVO> result;
        UserInfo userInfo = new UserInfo();
        try {
            String account = parameters.get("account");
            userInfo.setUserAccount(account);
            UserProvider.setLocalLoginUser(userInfo);
            result = tokenGranter.granter(parameters);
            //写入日志
            if (StringUtil.isEmpty(parameters.get("userId"))) {
                    WriteLogModel writeLogModel =  WriteLogModel.builder()
                            .userId(userInfo.getUserId())
                            .userInfo(userInfo)
                            .userName(userInfo.getUserName() + "/" + userInfo.getUserAccount())
                            .abstracts("登录成功")
                            .loginMark(1)
                            .requestDuration(System.currentTimeMillis() - millis).build();
                    logService.writeLogAsync(
                            writeLogModel.getUserId(), writeLogModel.getUserName(), writeLogModel.getAbstracts(),
                            writeLogModel.getUserInfo(), writeLogModel.getLoginMark(), writeLogModel.getLoginType(),
                            writeLogModel.getRequestDuration()
                    );
            }
        }catch (Exception e){
            if(!(e instanceof LoginException)){
                String msg = e.getMessage();
                if(msg == null){
                    msg = "登录异常";
                }
                log.error("登录异常 {}", e.getMessage(), e);
                throw new LoginException(msg);
            }
            String userName = StringUtil.isNotEmpty(userInfo.getUserName()) ? userInfo.getUserName()+"/"+userInfo.getUserAccount() : userInfo.getUserAccount();
            WriteLogModel writeLogModel =  WriteLogModel.builder()
                    .userId(userInfo.getUserId())
                    .userName(userName)
                    .abstracts(e.getMessage())
                    .userInfo(userInfo)
                    .loginMark(0)
                    .requestDuration(System.currentTimeMillis()-millis).build();
            logService.writeLogAsync(
                    writeLogModel.getUserId(), writeLogModel.getUserName(), writeLogModel.getAbstracts(),
                    writeLogModel.getUserInfo(), writeLogModel.getLoginMark(), writeLogModel.getLoginType(),
                    writeLogModel.getRequestDuration()
            );
            throw e;
        }finally{
            LoginHolder.clearUserEntity();
            TenantProvider.clearBaseSystemIfo();
        }
        return result;
    }


    /**
     * 踢出用户, 用户将收到Websocket下线通知
     * 执行流程：认证服务退出用户->用户踢出监听->消息服务发送Websocket推送退出消息
     * @param tokens
     */
    public ActionResult kickoutByToken(String... tokens){
        UserProvider.kickoutByToken(tokens);
        return ActionResult.success();
    }

    /**
     * 踢出用户, 用户将收到Websocket下线通知
     * 执行流程：认证服务退出用户->用户踢出监听->消息服务发送Websocket推送退出消息
     * @param userId
     * @param tenantId
     */
    public ActionResult kickoutByUserId(String userId, String tenantId){
        UserProvider.kickoutByUserId(userId, tenantId);
        return ActionResult.success();
    }
}
