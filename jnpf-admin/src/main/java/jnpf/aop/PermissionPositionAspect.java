package jnpf.aop;

import jnpf.constant.PermissionConstant;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.model.position.PositionCrForm;
import jnpf.permission.model.position.PositionUpForm;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.util.PermissionAspectUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@Slf4j
@Aspect
@Component
public class PermissionPositionAspect implements PermissionAdminBase{

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;

    /**
     * 分级管理切点
     */
    @Pointcut("@annotation(jnpf.annotation.PositionPermission)")
    public void pointcut() {
    }

    /**
     * 分级管理切点
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return PermissionAdminBase.permissionCommon(pjp, userProvider, this);
    }

    @Override
    public Boolean detailPermission(ProceedingJoinPoint pjp, String operatorUserId, String methodName) {
        switch (methodName){
            case PermissionConstant.METHOD_CREATE:
                return PermissionAspectUtil.getPermitByOrgId(
                        // 操作目标对象组织ID集合
                        ((PositionCrForm) pjp.getArgs()[0]).getOrganizeId(),
                        operatorUserId,
                        methodName);
            case PermissionConstant.METHOD_UPDATE:
                // 得到岗位信息后，判断是否有修改前的权限
                PositionEntity info = positionService.getInfo(((String) pjp.getArgs()[0]));
                if (PermissionAspectUtil.getPermitByOrgId(
                        // 操作目标对象组织ID集合
                        info.getOrganizeId(),
                        operatorUserId,
                        methodName)) {
                    return PermissionAspectUtil.getPermitByOrgId(
                            // 操作目标对象组织ID集合
                            ((PositionUpForm) pjp.getArgs()[1]).getOrganizeId(),
                            operatorUserId,
                            methodName);
                }
                return false;
            case PermissionConstant.METHOD_DELETE:
                // 获取岗位所关联的组织ID字符串
                String positionId = String.valueOf(pjp.getArgs()[0]);
                String orgIds = organizeService.getInfo(positionService.getInfo(positionId).getOrganizeId()).getId();
                return PermissionAspectUtil.getPermitByOrgId(
                        // 操作目标对象组织ID集合
                        orgIds,
                        operatorUserId,
                        PermissionConstant.METHOD_DELETE);
            default:
                return false;
        }
    }
}
