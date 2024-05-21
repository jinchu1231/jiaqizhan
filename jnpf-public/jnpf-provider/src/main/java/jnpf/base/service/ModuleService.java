package jnpf.base.service;

import jnpf.base.ActionResult;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.model.module.ModuleExportModel;
import jnpf.base.vo.DownloadVO;
import jnpf.exception.DataException;

import java.util.List;
import java.util.Map;

/**
 * 系统功能
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface ModuleService extends SuperService<ModuleEntity> {

    /**
     * 列表
     *
     * @return ignore
     * @param filterFlowWork
     * @param moduleAuthorize
     * @param moduleUrlAddressAuthorize
     */
    List<ModuleEntity> getList(boolean filterFlowWork, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize);

    /**
     * 列表
     *
     * @return ignore
     */
    List<ModuleEntity> getList();

    /**
     * 列表
     *
     * @param systemId 系统id
     * @param category
     * @param keyword
     * @param parentId
     * @param release
     * @return ignore
     */
    List<ModuleEntity> getList(String systemId, String category, String keyword, Integer type, Integer enabledMark, String parentId, boolean release);

    /**
     * 通过id获取子菜单
     *
     * @param id 主键
     * @return ignore
     */
    List<ModuleEntity> getList(String id);

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    ModuleEntity getInfo(String id);

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    ModuleEntity getInfo(String id, String systemId);

    /**
     * 信息
     *
     * @param fullName 主键值
     * @return ignore
     */
    List<ModuleEntity> getInfoByFullName(String fullName, String systemId);

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    ModuleEntity getInfo(String id, String systemId, String parentId);

    /**
     * 验证名称
     *
     * @param entity   ignore
     * @param category 分类
     * @param systemId 分类
     * @return ignore
     */
    boolean isExistByFullName(ModuleEntity entity, String category, String systemId);

    /**
     * 验证编码
     *
     * @param entity   实体
     * @param category 分类
     * @param systemId 分类
     * @return ignore
     */
    boolean isExistByEnCode(ModuleEntity entity, String category, String systemId);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(ModuleEntity entity);

    /**
     * 删除
     *
     * @param systemId 实体对象
     */
    void deleteBySystemId(String systemId);


    /**
     * 删除权限（同步菜单 不处理数据权限）
     *
     * @param entity 实体对象
     */
    void deleteModule(ModuleEntity entity);
    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(ModuleEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return ignore
     */
    boolean update(String id, ModuleEntity entity);

    /**
     * 导出数据
     *
     * @param id 主键
     * @return DownloadVO ignore
     */
    DownloadVO exportData(String id);

    /**
     * 导入数据
     *
     * @param exportModel 导出模型
     * @param type
     * @return ignore
     * @throws DataException ignore
     */
    ActionResult importData(ModuleExportModel exportModel, Integer type) throws DataException;

    /**
     * 功能设计发布功能自动创建app pc菜单
     * @return
     */
    List<ModuleEntity> getModuleList(String visualId);

    /**
     * 通过系统id获取菜单
     *
     * @param ids
     * @param moduleAuthorize
     * @param moduleUrlAddressAuthorize
     * @return
     */
    List<ModuleEntity> getModuleBySystemIds(List<String> ids, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize);

    /**
     * 获取门户发布的菜单
     *
     * @param portalIds
     * @return
     */
    List<ModuleEntity> getModuleByPortal(List<String> portalIds);

    /**
     * 获取开发平台下的菜单
     *
     * @return
     * @param moduleAuthorize
     * @param moduleUrlAddressAuthorize
     */
    List<ModuleEntity> getMainModule(List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize, boolean singletonOrg);

    List<ModuleEntity> getModuleByIds(List<String> moduleIds, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize, boolean singletonOrg);

    List<ModuleEntity> getModuleByIds(List<String> moduleIds);

    /**
     * 通过ids获取系统菜单
     *
     * @param enCodeList
     * @return
     */
    List<ModuleEntity> getListByEnCode(List<String> enCodeList);

    /**
     *
     * @param mark
     * @param id
     * @param moduleAuthorize
     * @param moduleUrlAddressAuthorize
     * @return
     */
    List<ModuleEntity> findModuleAdmin(int mark, String id, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize);

    void getParentModule(List<ModuleEntity> data, Map<String, ModuleEntity> moduleEntityMap);

    /**
     * 通过urlAddressList找id
     *
     * @param ids
     * @param urlAddressList
     * @return
     */
    List<ModuleEntity> getListByUrlAddress(List<String> ids, List<String> urlAddressList);

}
