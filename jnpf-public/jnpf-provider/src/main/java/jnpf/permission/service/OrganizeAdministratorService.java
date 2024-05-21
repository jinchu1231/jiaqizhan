package jnpf.permission.service;


import jnpf.base.Pagination;
import jnpf.base.service.SuperService;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorListVo;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorModel;

import java.util.List;

/**
 *
 * 机构分级管理员
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface OrganizeAdministratorService extends SuperService<OrganizeAdministratorEntity> {



    /**
     * 获取 机构分级管理员信息
     * @param userId
     * @param organizeId
     * @return
     */
    OrganizeAdministratorEntity getOne(String userId, String organizeId);

    /**
     * 根据userId获取列表
     * @param userId
     * @return
     */
    List<OrganizeAdministratorEntity> getOrganizeAdministratorEntity(String userId);

    /**
     * 根据userId获取列表
     * @param userId
     * @param type
     * @param filterMain
     * @return
     */
    List<OrganizeAdministratorEntity> getOrganizeAdministratorEntity(String userId, String type, boolean filterMain);

    /**
     * 新建
     * @param entity  实体对象
     */
    void create(OrganizeAdministratorEntity entity);

    /**
     * 新建
     * @param list
     */
    void createList(List<OrganizeAdministratorEntity> list, String userId);

    /**
     * 更新
     * @param id     主键值
     * @param entity 实体对象
     */
    boolean update(String id, OrganizeAdministratorEntity entity);

    /**
     * 删除
     * @param userId 用户id
     */
    boolean deleteByUserId(String userId);

    /**
     * 删除
     * @param entity 实体对象
     */
    void delete(OrganizeAdministratorEntity entity);

    /**
     * 获取 OrganizeAdminIsTratorEntity 信息
     * @param id 主键值
     * @return
     */
    OrganizeAdministratorEntity getInfo(String id);

    /**
     * 获取 OrganizeAdminIsTratorEntity 信息
     * @param organizeId 机构主键值
     * @return
     */
    OrganizeAdministratorEntity getInfoByOrganizeId(String organizeId);

    /**
     * 获取 OrganizeAdminIsTratorEntity 列表
     * @param organizeIdList 机构主键值
     * @return
     */
    List<OrganizeAdministratorEntity> getListByOrganizeId(List<String> organizeIdList);

    /**
     * 获取二级管理员列表
     *
     * @param pagination 分页参数
     * @return
     */
    List<OrganizeAdministratorListVo> getList(Pagination pagination);

    List<String> getOrganizeUserList(String type);

    List<OrganizeEntity> getListByAuthorize();

    OrganizeAdministratorModel getOrganizeAdministratorList();

    /**
     * 获取 OrganizeAdminIsTratorEntity 信息
     * @param userId 主键值
     * @param tenantId
     * @return
     */
    List<OrganizeAdministratorEntity> getInfoByUserId(String userId, String tenantId);
}
