package jnpf.aop;

import jnpf.constant.PermissionConstant;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.model.authorize.SaveBatchForm;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.util.PermissionAspectUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@Slf4j
@Aspect
@Component
public class PermissionAdminAspect implements PermissionAdminBase{

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private OrganizeRelationService organizeRelationService;

    /**
     * 分级管理切点
     */
    @Pointcut("@annotation(jnpf.annotation.OrganizeAdminIsTrator)")
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
    public Boolean detailPermission(ProceedingJoinPoint pjp, String operatorUserId, String methodName){
        switch (methodName) {
            case PermissionConstant.METHOD_SAVE:
                if(userProvider.get().getIsAdministrator()){
                    return true;
                }
                String roleId = (String) pjp.getArgs()[0];
                List<String> orgIdList = organizeRelationService.getRelationListByRoleId(roleId).stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList());
                StringBuilder orgId = new StringBuilder();
                orgIdList.stream().forEach(t->{
                    orgId.append(t + ",");
                });
                return PermissionAspectUtil.getPermitByOrgId(
                        // 操作目标对象组织ID集合
                        orgId.toString(),
                        operatorUserId,
                        PermissionConstant.METHOD_UPDATE);
            case PermissionConstant.METHOD_SAVE_BATCH:
                // 修改为只有超管才能操作
                if(userProvider.get().getIsAdministrator()){
                    return true;
                }
                // 得到角色id
                SaveBatchForm saveBatchForm = (SaveBatchForm) pjp.getArgs()[0];
                List<String> list = Arrays.asList(saveBatchForm.getRoleIds());
                if (list.size() == 0) {
                    list = new ArrayList<>();
                    list.add("");
                }
                // 得到组织id
                List<String> orgIdLists = organizeRelationService.getRelationListByRoleIdList(list).stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList());
                StringBuilder orgIds = new StringBuilder();
                orgIdLists.stream().forEach(t->{
                    orgIds.append(t + ",");
                });
                return PermissionAspectUtil.getPermitByOrgId(
                        // 操作目标对象组织ID集合
                        orgIds.toString(),
                        operatorUserId,
                        PermissionConstant.METHOD_UPDATE);
            case PermissionConstant.METHOD_UPDATE:
                //判断是否有当前组织的修改权限
                String organizeId = String.valueOf(pjp.getArgs()[0]);
                return PermissionAspectUtil.containPermission(organizeId, operatorUserId, methodName);
            default:
                return false;
        }
    }

}
