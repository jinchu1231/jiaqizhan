package jnpf.base.service;


import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;

import java.util.List;

/**
 * 数据权限方案
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
public interface ModuleDataAuthorizeSchemeService extends SuperService<ModuleDataAuthorizeSchemeEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<ModuleDataAuthorizeSchemeEntity> getList();

    /**
     * 列表
     *
     * @return ignore
     */
    List<ModuleDataAuthorizeSchemeEntity> getEnabledMarkList(String enabledMark);

    /**
     * 列表
     *
     * @param moduleId 功能主键
     * @return
     */
    List<ModuleDataAuthorizeSchemeEntity> getList(String moduleId);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    ModuleDataAuthorizeSchemeEntity getInfo(String id);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(ModuleDataAuthorizeSchemeEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    boolean update(String id, ModuleDataAuthorizeSchemeEntity entity);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(ModuleDataAuthorizeSchemeEntity entity);

    /**
     * 上移
     *
     * @param id 主键值
     * @return
     */
    boolean first(String id);

    /**
     * 下移
     *
     * @param id 主键值
     * @return
     */
    boolean next(String id);

    /**
     * 判断名称是否重复
     * @param id
     * @param enCode
     * @return
     */
    Boolean isExistByEnCode(String id, String enCode, String moduleId);

    /**
     * 判断名称是否重复
     * @param id
     * @param fullName
     * @return
     */
    Boolean isExistByFullName(String id, String fullName, String moduleId);

    /**
     * 是否存在全部数据
     * @param moduleId
     * @return
     */
    Boolean isExistAllData(String moduleId);

    /**
     * 通过moduleIds获取权限
     *
     * @param ids
     * @return
     */
    List<ModuleDataAuthorizeSchemeEntity> getListByModuleId(List<String> ids);

    /**
     * 通过moduleIds获取权限
     *
     * @param ids
     * @return
     */
    List<ModuleDataAuthorizeSchemeEntity> getListByIds(List<String> ids);
}
