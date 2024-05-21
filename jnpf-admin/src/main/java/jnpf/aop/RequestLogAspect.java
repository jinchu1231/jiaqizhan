package jnpf.aop;

import jnpf.annotation.HandleLog;
import jnpf.base.LogSortEnum;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.entity.LogEntity;
import jnpf.service.LogService;
import jnpf.util.IpUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ServletUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.Executor;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@Slf4j
@Aspect
@Component
@Order(2)
public class RequestLogAspect {

    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private LogService logService;
    @Autowired
    private Executor executor;

    @Pointcut("(execution(* jnpf.*.controller.*.*(..)) || execution(* jnpf.message.websocket.WebSocket.*(..)))&&!execution(* jnpf.controller.UtilsController.*(..)) ")
    public void requestLog() {

    }

    @Around("requestLog()")
    public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object obj = pjp.proceed();
        long costTime = System.currentTimeMillis() - startTime;
        UserInfo userInfo = UserProvider.getUser();
        if(userInfo.getUserId() != null) {
            printLog(userInfo, costTime);
            try {
                // 判断是否需要操作日志
                MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
                // 得到请求参数
                Object[] args = pjp.getArgs();
                // 得到请求方法
                Method method = methodSignature.getMethod();
                HandleLog methodAnnotation = method.getAnnotation(HandleLog.class);
                if (methodAnnotation != null) {
                    String moduleName = methodAnnotation.moduleName();
                    String requestMethod = methodAnnotation.requestMethod();
                    handleLog(userInfo, costTime, obj, moduleName, requestMethod, args);
                }
            } catch (Exception e) {
                log.error("记录操作日志发生错误：" + e.getMessage());
            }
        }
        return obj;
    }

    /**
     * 请求日志
     *
     * @param userInfo
     * @param costTime
     */
    private void printLog(UserInfo userInfo, long costTime) {
        LogEntity entity = new LogEntity();
        entity.setId(RandomUtil.uuId());
        entity.setType(LogSortEnum.Request.getCode());
        entity.setUserId(userInfo.getUserId());
        entity.setUserName(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        //请求耗时
        entity.setRequestDuration((int) costTime);
        entity.setRequestUrl(ServletUtil.getRequest().getServletPath());
        entity.setRequestMethod(ServletUtil.getRequest().getMethod());
        entity.setIpAddress(IpUtil.getIpAddr());
        entity.setCreatorTime(new Date());
        entity.setPlatForm(ServletUtil.getUserAgent());
        executor.execute(()->{
            logService.save(entity);
        });
    }

    /**
     * 添加操作日志
     *
     * @param userInfo      用户信息
     * @param costTime      操作耗时
     * @param obj           请求结果
     * @param moduleName    模块名称
     * @param requestMethod 请求方法
     * @param arg           请求参数
     */
    private void handleLog(UserInfo userInfo, long costTime, Object obj, String moduleName, String requestMethod, Object[] arg) {
        LogEntity entity = new LogEntity();
        entity.setId(RandomUtil.uuId());
        entity.setType(LogSortEnum.Operate.getCode());
        entity.setUserId(userInfo.getUserId());
        entity.setUserName(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        //请求耗时
        entity.setRequestDuration((int) costTime);
        entity.setRequestMethod(requestMethod);
        entity.setIpAddress(IpUtil.getIpAddr());
        entity.setCreatorTime(new Date());
        // 请求设备
        entity.setPlatForm(ServletUtil.getUserAgent());
        // 操作模块
        entity.setModuleName(moduleName);
        // 操作记录
        try {
            // 定义字符串
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : arg) {
                // 如果是MultipartFile则为导入
                if (o instanceof MultipartFile) {
                    stringBuilder.append("{\"originalFilename\":\"" + ((MultipartFile) o).getOriginalFilename() + "\",");
                    stringBuilder.append("\"contentType\":\"" + ((MultipartFile) o).getContentType() + "\",");
                    stringBuilder.append("\"name\":\"" + ((MultipartFile) o).getName() + "\",");
                    stringBuilder.append("\"resource\":\"" + ((MultipartFile) o).getResource() + "\",");
                    stringBuilder.append("\"size\":\"" + ((MultipartFile) o).getSize() + "\"}");
                }
            }
            if (stringBuilder.length() > 0) {
                entity.setJsons(requestMethod + "应用【" + stringBuilder + "】【" + obj + "】" );
            } else {
                entity.setJsons(requestMethod + "应用【" + JsonUtil.getObjectToString(arg) + "】【" + obj + "】" );
            }
        } catch (Exception e) {
            entity.setJsons(requestMethod + "应用【" + arg + "】【" + obj + "】" );
        }
        executor.execute(()->{
            logService.save(entity);
        });
    }

///    后面可能会用
//    /**
//     * 判断是否为导入导出
//     *
//     * @return
//     */
//    private String getRequestMethod() {
//        //得到请求方式
//        String methodType = ServletUtil.getRequest().getMethod();
//        // 得到当前请求的尾缀
//        String endWith = null;
//        String servletPath = ServletUtil.getServletPath();
//        if (StringUtil.isNotEmpty(servletPath)) {
//            String[] path = servletPath.split("/");
//            int length = path.length;
//            if (length > 5) {
//                endWith = path[length - 2] + "/" + path[length - 1];
//            }
//        }
//        // 如果是GET请求且请求后缀是'/Action/Export'则判定为导出
//        if (HandleMethodEnum.GET.getRequestType().equals(methodType)) {
//            methodType = "Action/Export".equals(endWith) ? "EXPORT" : "GET";
//        } else if (HandleMethodEnum.POST.getRequestType().equals(methodType)) {
//            methodType = "Action/Import".equals(endWith) ? "IMPORT" : "GET";
//        }
//        return methodType;
//    }
//    /**
//     * 判断是否为导入导出
//     *
//     * @return
//     */
//    private String getRequestModuleName() {
//        //得到Url
//        String requestURI = ServletUtil.getRequest().getRequestURI();
//        // 取模块名
//        if (StringUtil.isNotEmpty(requestURI)) {
//            String[] split = requestURI.split("/");
//            if (split.length > 2) {
//                String url = split[1];
//                // 得到所在模块
//                String moduleName = HandleModuleEnum.getModuleByURL(url);
//                return moduleName;
//            }
//        }
//        return "";
//    }
///

}
