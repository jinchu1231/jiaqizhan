package jnpf.permission.service;

import jnpf.base.service.SuperService;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.model.permissiongroup.PaginationPermissionGroup;

import java.util.List;

public interface PermissionGroupService extends SuperService<PermissionGroupEntity> {

    /**
     * 列表
     * @param pagination
     * @return
     */
    List<PermissionGroupEntity> list(PaginationPermissionGroup pagination);

    /**
     * 列表
     * @param filterEnabledMark
     * @param ids
     * @return
     */
    List<PermissionGroupEntity> list(boolean filterEnabledMark, List<String> ids);

    /**
     * 详情
     * @param id
     * @return
     */
    PermissionGroupEntity info(String id);

    /**
     * 新建
     * @param entity
     * @return
     */
    boolean create(PermissionGroupEntity entity);

    /**
     * 修改
     * @param id 主键
     * @param entity 实体
     * @return
     */
    boolean update(String id, PermissionGroupEntity entity);

    /**
     * 删除
     * @param entity 实体
     * @return
     */
    boolean delete(PermissionGroupEntity entity);

    /**
     * 验证名称是否重复
     * @param id
     * @param entity
     */
    boolean isExistByFullName(String id, PermissionGroupEntity entity);

    /**
     * 验证编码是否重复
     * @param id
     * @param entity
     */
    boolean isExistByEnCode(String id, PermissionGroupEntity entity);

    /**
     * 获取权限成员
     *
     * @param id 主键
     * @return
     */
    PermissionGroupEntity permissionMember(String id);

    /**
     * 获取权限成员
     * @param userId
     * @param tenantId
     * @param systemId
     * @return
     */
    List<PermissionGroupEntity> getPermissionGroupByUserIdAndTenantId(String userId, String tenantId, String systemId);
    /**
     * 获取权限成员
     *
     * @param userId 用户主键
     * @param orgId
     * @param singletonOrg
     * @param systemId
     * @return
     */
    List<PermissionGroupEntity> getPermissionGroupByUserId(String userId, String orgId, boolean singletonOrg, String systemId);

    /**
     * 获取权限成员
     *
     * @param userId 用户主键
     * @return
     */
    String getPermissionGroupByUserId(String userId);

    /**
     * 获取权限成员
     *
     * @param userId 用户主键
     * @param systemId 应用主键
     * @return
     */
    String getOrgIdByUserIdAndSystemId(String userId, String systemId);

    /**
     * 通过用户id获取当前权限组（只查用户）
     *
     * @param userId 用户主键
     * @return
     */
    List<PermissionGroupEntity> getPermissionGroupAllByUserId(String userId);

    /**
     * 替换权限
     *
     * @param fromId
     * @param toId
     * @param permissionList
     * @return
     */
    boolean updateByUser(String fromId, String toId, List<String> permissionList);

    /**
     * 通过菜单获取权限组
     *
     * @param moduleId 菜单id
     * @return
     */
    List<PermissionGroupEntity> getPermissionGroupByModuleId(String moduleId);

    /**
     * 通过ids获取权限组列表
     *
     * @param ids
     * @return
     */
    List<PermissionGroupEntity> list(List<String> ids);


    /**
     * 通过对象id获取当前权限组
     *
     * @param objectId 对象主键
     * @param objectType 对象类型
     * @return
     */
    List<PermissionGroupEntity> getPermissionGroupByObjectId(String objectId, String objectType);

    /**
     *
     * @param userId
     * @param tenantId
     * @return
     */
    String getOrganizeIdByUserIdAndTenantId(String userId, String tenantId);
}
