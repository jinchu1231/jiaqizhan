package jnpf.aop;

import jnpf.constant.PermissionConst;
import jnpf.constant.PermissionConstant;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.user.UserCrForm;
import jnpf.permission.model.user.UserUpForm;
import jnpf.permission.model.userrelation.UserRelationForm;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserRelationService;
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
import java.util.List;
import java.util.StringJoiner;
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
public class PermissionUserAspect implements PermissionAdminBase{

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private UserRelationService userRelationService;

    /**
     * 分级管理切点
     */
    @Pointcut("@annotation(jnpf.annotation.UserPermission)")
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
                UserCrForm userCrForm = (UserCrForm) pjp.getArgs()[0];
                return PermissionAspectUtil.getPermitByOrgId(
                                        // 操作目标对象组织ID集合
                                        userCrForm.getOrganizeId(),
                                        operatorUserId,
                                        PermissionConstant.METHOD_CREATE);
            case PermissionConstant.METHOD_UPDATE:
                // 得到修改的用户以前的信息
                String userId = (String) pjp.getArgs()[0];
                List<String> collect = userRelationService.getListByUserId(userId, PermissionConst.ORGANIZE).stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList());
                StringJoiner stringJoiner = new StringJoiner(",");
                collect.forEach(t -> {
                    stringJoiner.add(t);
                });
                if (PermissionAspectUtil.getPermitByOrgId(
                                // 操作目标对象组织ID集合
                                stringJoiner.toString(),
                                operatorUserId,
                                PermissionConstant.METHOD_UPDATE)) {
                    return PermissionAspectUtil.getPermitByOrgId(
                                    // 操作目标对象组织ID集合
                                    ((UserUpForm) pjp.getArgs()[1]).getOrganizeId(),
                                    operatorUserId,
                                    PermissionConstant.METHOD_UPDATE);
                }
                return false;
            case PermissionConstant.METHOD_MODIFY_PW:
                return PermissionAspectUtil.getPermitByUserId(
                                        // 操作目标对象的ID
                                        String.valueOf(pjp.getArgs()[0]),
                                        operatorUserId,
                                        PermissionConstant.METHOD_UPDATE);
            case PermissionConstant.METHOD_DELETE:
                return PermissionAspectUtil.getPermitByUserId(
                                        // 操作目标对象的ID
                                        pjp.getArgs()[0].toString(),
                                        operatorUserId,
                                        PermissionConstant.METHOD_DELETE);
            case PermissionConstant.METHOD_SAVE:
                String objId = pjp.getArgs()[0].toString();
                UserRelationForm userRelationForm = (UserRelationForm)pjp.getArgs()[1];

                List<String> orgIds = new ArrayList<>();
                if(userRelationForm.getObjectType().equals(PermissionConst.ROLE)){
                    // 角色目前修改为只有超管才能够修改
                    if(userProvider.get().getIsAdministrator()){
                        return true;
                    }
                    orgIds.addAll(organizeRelationService.getRelationListByRoleId(objId).stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList()));
                    return PermissionAspectUtil.getPermitByOrgId(
                            // 操作目标对象组织ID集合
                            String.join(",", orgIds),
                            operatorUserId,
                            PermissionConstant.METHOD_UPDATE);
                }else {
                    if(userRelationForm.getObjectType().equals(PermissionConst.GROUP)) {
                        return true;
                    }
                    if(userRelationForm.getObjectType().equals(PermissionConst.POSITION)) {
                        orgIds.add(positionService.getInfo(objId).getOrganizeId());
                    }
                    return PermissionAspectUtil.getPermitByOrgId(
                            String.join(",", orgIds),
                            operatorUserId,
                            PermissionConstant.METHOD_UPDATE);
                }
            case PermissionConstant.METHOD_DELETE_SOCIALS:
                if(pjp.getArgs()[0].toString().equals(operatorUserId)){return true;}
                return PermissionAspectUtil.getPermitByUserId(
                        // 操作目标对象的ID
                        pjp.getArgs()[0].toString(),
                        operatorUserId,
                        PermissionConstant.METHOD_UPDATE);
            default:
                return false;
        }
    }





}
