package jnpf.permission.service;

import jnpf.base.UserInfo;
import jnpf.base.entity.SystemEntity;
import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.form.ModuleFormModel;
import jnpf.base.model.resource.ResourceModel;
import jnpf.base.service.SuperService;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.model.authorize.AuthorizeConditionModel;
import jnpf.permission.model.authorize.AuthorizeDataUpForm;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.model.authorize.SaveAuthForm;
import jnpf.permission.model.authorize.SaveBatchForm;
import jnpf.portal.model.PortalModel;

import java.util.List;

/**
 * 操作权限
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface AuthorizeService extends SuperService<AuthorizeEntity> {

    /**
     * 获取权限（菜单、按钮、列表）
     *
     * @param userInfo 对象
     * @param singletonOrg
     * @return
     */
    AuthorizeVO getAuthorize(UserInfo userInfo, boolean singletonOrg) throws Exception;

    /**
     * 获取权限（菜单、按钮、列表）
     *
     * @param isCache 是否存在redis
     * @param singletonOrg
     * @return
     */
    AuthorizeVO getAuthorize(boolean isCache, boolean singletonOrg);

    /**
     * 创建
     *
     * @param objectId      对象主键
     * @param authorizeList 实体对象
     */
    void save(String objectId, AuthorizeDataUpForm authorizeList);

    /**
     * 创建
     *
     * @param saveBatchForm 对象主键
     */
    void saveBatch(SaveBatchForm saveBatchForm, boolean isBatch);

    /**
     * 根据用户id获取列表
     *
     * @param isAdmin 是否管理员
     * @param userId  用户主键
     * @return
     */
    List<AuthorizeEntity> getListByUserId(boolean isAdmin, String userId);

    /**
     * 根据对象Id获取列表
     *
     * @param objectId 对象主键
     * @return
     */
    List<AuthorizeEntity> getListByObjectId(List<String> objectId);

    /**
     * 判断当前角色是否有权限
     *
     * @param roleId
     * @param systemId
     * @return
     */
    Boolean existAuthorize(String roleId, String systemId);

    /**
     * 判断当前角色是否有权限
     *
     * @param roleId
     * @return
     */
    List<AuthorizeEntity> getListByRoleId(String roleId);

    /**
     * 根据对象Id获取列表
     *
     * @param objectId 对象主键
     * @param itemType 对象主键
     * @return
     */
    List<AuthorizeEntity> getListByObjectId(String objectId, String itemType);

    /**
     * 根据对象Id获取列表
     *
     * @param objectType 对象主键
     * @return
     */
    List<AuthorizeEntity> getListByObjectAndItem(String itemId, String objectType);

    /**
     * 根据对象Id获取列表
     *
     * @param itemId 对象主键
     * @param itemType 对象类型
     * @return
     */
    List<AuthorizeEntity> getListByObjectAndItemIdAndType(String itemId, String itemType);

    void savePortalManage(String portalManageId, SaveAuthForm saveAuthForm);

    void getPortal(List<SystemEntity> systemList, List<PortalModel> portalList, Long dateTime, List<String> collect);

    void savePortalAuth(String permissionGroupId, List<String> portalIds);

    byte[] getCondition(AuthorizeConditionModel conditionModel);


    List<ButtonModel> findButton(String objectId);

    List<ColumnModel> findColumn(String objectId);

    List<ResourceModel> findResource(String objectId);

    List<ModuleFormModel> findForms(String objectId);

    List<ButtonModel> findButtonAdmin(Integer mark);

    List<ColumnModel> findColumnAdmin(Integer mark);

    List<ResourceModel> findResourceAdmin(Integer mark);

    List<ModuleFormModel> findFormsAdmin(Integer mark);

    /**
     * 通过Item获取权限列表
     *
     * @param itemType
     * @param itemId
     * @return
     */
    List<AuthorizeEntity> getAuthorizeByItem(String itemType, String itemId);

    AuthorizeVO getAuthorizeByUser(boolean singletonOrg);

    AuthorizeVO getMainSystemAuthorize(List<String> moduleIds, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize, boolean singletonOrg);

    List<AuthorizeEntity> getListByRoleIdsAndItemType(List<String> roleIds, String itemType);

    List<SuperJsonModel> getConditionSql(String moduleId);
}
