package jnpf.filter;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import jnpf.base.ActionResultCode;
import jnpf.config.ConfigValueUtil;
import jnpf.properties.GatewayWhite;
import jnpf.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;


/**
 * 网关验证token
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-24
 */
@Slf4j
@Configuration
public class AuthFilter {

    private static final String ALL = "*";
    private static final String MAX_AGE = "18000L";

    @Autowired
    private ConfigValueUtil configValueUtil;

    @Autowired
    private GatewayWhite gatewayWhite;

    // 注册 Sa-Token全局过滤器
    @Bean
    public SaServletFilter getSaReactorFilter(GatewayWhite gatewayWhite) {
        return new SaServletFilter()
                // 拦截地址
                .addInclude("/**")
                .setExcludeList(gatewayWhite.excludeUrl)
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    if(log.isInfoEnabled()){
                        log.info("请求路径: {}", SaHolder.getRequest().getRequestPath());
                    }
                    //拦截路径
                    SaRouter.match(gatewayWhite.blockUrl).match(o -> {
                        //禁止访问URL 排除白名单
                        String ip = getIpAddr();
                        for (String o1 : gatewayWhite.whiteIp) {
                            if(ip.startsWith(o1)){
                                return false;
                            }
                        }
                        log.info("非白名单IP访问限制接口：{}, {}", SaHolder.getRequest().getRequestPath(), ip);
                        return true;
                    }).back("接口无法访问");
                    //测试不验证 鉴权服务重启测试模式不清除Token就够了
                    //SaRouter.match((r)->"true".equals(configValueUtil.getTestVersion())).stop();
                    //白名单不拦截
                    SaRouter.match(gatewayWhite.whiteUrl).stop();
                    // 登录校验 -- 校验多租户管理模块TOKEN
                    //SaRouter.match("/api/tenant/**", r -> {
                    //    SaManager.getStpLogic(AuthConsts.ACCOUNT_TYPE_TENANT).checkLogin();
                    //}).stop();
                    // 登录校验 -- 拦截所有路由
                    SaRouter.match("/**", r -> {
                        StpUtil.checkLogin();
                    }).stop();
                }).setError(e -> {
                    SaHolder.getResponse().addHeader("Content-Type","application/json; charset=utf-8");
                    if(e instanceof NotLoginException){
                        return SaResult.error(ActionResultCode.SessionOverdue.getMessage()).setCode(ActionResultCode.SessionOverdue.getCode());
                    }
                    log.error(e.getMessage(), e);
                    return SaResult.error("系统异常.").setCode(ActionResultCode.Exception.getCode());
                })
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    HttpServletRequest request = (HttpServletRequest) SaHolder.getRequest().getSource();
                    // ---------- 设置跨域响应头 ----------
                    SaHolder.getResponse()
                            // 允许指定域访问跨域资源
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN))
                            // 允许的header参数
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALL)
                            // 允许所有请求方式
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALL)
                            .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                            .setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL)
                            // 有效时间
                            .setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);

                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS)
                            .back();
                });
    }

    public static String getIpAddr() {
        return IpUtil.getIpAddr();
    }


}
