package jnpf.permission.service;

import jnpf.base.Page;
import jnpf.base.Pagination;
import jnpf.base.service.SuperService;
import jnpf.base.vo.DownloadVO;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.user.PaginationUser;
import jnpf.permission.model.user.UserByRoleVO;
import jnpf.permission.model.user.UserConditionModel;
import jnpf.permission.model.user.UserExportExceptionVO;
import jnpf.permission.model.user.UserExportVO;
import jnpf.permission.model.user.UserIdListVo;
import jnpf.permission.model.user.UserImportVO;
import jnpf.permission.model.user.UserUpdateModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface UserService extends SuperService<UserEntity> {

    /**
     * 列表
     *
     * @param pagination 条件
     * @param enabledMark
     * @param gender
     * @return
     */
    List<UserEntity>  getList(Pagination pagination, String organizeId, Boolean flag, Boolean filter, Integer enabledMark, String gender);

    /**
     * 列表
     *
     * @param pagination 条件
     * @return
     */
    List<UserEntity>  getList(Pagination pagination);

    /**
     * 通过关键字查询
     *
     * @param keyword
     * @return
     */
    List<UserEntity> getList(String keyword);

    /**
     * 通过组织id获取用户列表
     *
     * @param organizeId 组织id
     * @param keyword    关键字
     * @return
     */
    List<UserEntity> getListByOrganizeId(String organizeId, String keyword);

    /**
     * 列表
     *
     * @return
     * @param enabledMark
     */
    List<UserEntity> getList(boolean enabledMark);

    /**
     * 用户名列表（在线开发）
     *
     * @param idList
     * @return
     */
    List<UserEntity> getUserNameList(List<String> idList);

    /**
     * 用户名列表（在线开发）
     *
     * @param idList
     * @return
     */
    List<UserEntity> getUserNameList(Set<String> idList);

    Map<String,Object> getUserMap();
    Map<String,Object> getUserMap(String type);

    /**
     * （ name/account: id）
     * @return
     */
    Map<String,Object> getUserNameAndIdMap();

    /**
     * 通过名称查询id
     *
     * @return
     */
    UserEntity getByRealName(String realName);

    /**
     * 列表
     *
     * @param managerId 主管Id
     * @param keyword   关键字
     * @return
     */
    List<UserEntity> getListByManagerId(String managerId, String keyword);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    UserEntity getInfo(String id);

    /**
     * 信息
     *
     * @param account 账户
     * @return
     */
    UserEntity getUserByAccount(String account);

    /**
     * 信息
     *
     * @param mobile 手机号码
     * @return
     */
    UserEntity getUserByMobile(String mobile);

    /**
     * 验证账户
     *
     * @param account 账户
     * @return
     */
    boolean isExistByAccount(String account);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    Boolean create(UserEntity entity) throws Exception;

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    Boolean update(String id, UserEntity entity) throws Exception;

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(UserEntity entity);

    /**
     * 修改密码
     *
     * @param entity 实体对象
     */
    void updatePassword(UserEntity entity);

    /**
     * 查询用户名称
     *
     * @param id 主键值
     * @return
     */
    List<UserEntity> getUserName(List<String> id);


    /**
     * 查询用户名称
     *
     * @param id 主键值
     * @return
     */
    List<UserEntity> getUserName(List<String> id, boolean filterEnabledMark);

    /**
     * 候选人分页查询
     *
     * @param id
     * @param pagination
     * @param flag 是否过滤自己
     * @return
     */
    List<UserEntity> getUserNames(List<String> id, Pagination pagination, Boolean flag, Boolean enabledMark);

    /**
     * 查询出分页被禁用的账号
     *
     * @param id 主键值
     * @return
     */
    List<UserEntity> getUserList(List<String> id);

    /**
     * 通过account返回user实体
     *
     * @param account 账户
     * @return
     */
    UserEntity getUserEntity(String account);

    /**
     * 获取用户id
     *
     * @return
     */
    List<String> getListId();

    /**
     * 添加岗位或角色成员
     *
     * @param entity
     */
    void update(UserEntity entity, String type);

    /**
     * 添加岗位或角色成员
     *
     * @param entity
     */
    void updateLastTime(UserEntity entity, String type);

    /**
     * 判断是否为自己的下属
     *
     * @param id
     * @param managerId
     * @return
     */
    boolean isSubordinate(String id, String managerId);

    /**
     * 导出Excel
     *
     * @param dataType
     * @param selectKey
     * @param pagination
     * @return
     */
    DownloadVO exportExcel(String dataType, String selectKey, PaginationUser pagination);

    /**
     * 导入预览
     *
     * @param personList
     * @return
     */
    Map<String, Object> importPreview(List<UserExportVO> personList);

    /**
     * 导入数据
     *
     * @param dataList 数据源
     */
    UserImportVO importData(List<UserExportVO> dataList);

    /**
     * 通过组织id获取上级id集合
     *
     * @param organizeId
     * @param organizeParentIdList
     */
    void getOrganizeIdTree(String organizeId, StringBuffer organizeParentIdList);

    /**
     * 导出错误报告
     *
     * @param dataList
     * @return
     */
    DownloadVO exportExceptionData(List<UserExportExceptionVO> dataList);

    /**
     * 候选人分页查询
     *
     * @param id
     * @param pagination
     * @return
     */
    List<UserEntity> getUserName(List<String> id, Pagination pagination);

    /**
     * 根据角色ID获取所在组织下的所有成员
     * @param roleId 角色ID
     * @return
     */
    List<UserEntity> getListByRoleId(String roleId);

    List<UserEntity> getListByRoleIds(List<String> roleIds);

    /**
     * 删除在线的角色用户
     */
    Boolean delCurRoleUser(List<String> objectIdAll);

    Boolean delCurUser(String message, String... userIds);

    /**
     * 获取系统管理员
     * @return
     */
    List<UserEntity> getAdminList();

    /**
     * 设置系统管理员
     *
     * @param adminIds
     * @return
     */
    Boolean setAdminListByIds(List<String> adminIds);

    /**
     * 获取用户信息
     *
     *
     * @param orgIdList
     * @param keyword
     * @return
     */
    List<UserEntity> getList(List<String> orgIdList, String keyword);

    /**
     * 列表
     *
     * @param pagination 条件
     * @param filterCurrentUser
     * @return
     */
    List<UserEntity>  getList(Pagination pagination, Boolean filterCurrentUser);

    /**
     * 获取用户下拉框列表
     */
    List<UserByRoleVO> getListByAuthorize(String organizeId, Page page);

    /**
     * 得到用户关系
     *
     * @param userIds
     * @param type
     * @return
     */
    List<String> getUserIdList(List<String> userIds, String type);

    /**
     * 得到用户关系
     *
     * @param userIds
     * @return
     */
    List<UserIdListVo> getObjList(List<String> userIds, Pagination pagination, String type);


    String getDefaultCurrentUserId(UserConditionModel userConditionModel) throws DataException;

    /**
     * 通过ids返回相应的数据
     *
     * @param ids
     * @return
     */
    List<UserIdListVo> selectedByIds(List<String> ids);

    /**
     * 通过ids转换数据
     *
     * @param ids
     * @return
     */
    List<String> getFullNameByIds(List<String> ids);

    /**
     *
     * @param userUpdateModel
     */
    Boolean updateByIdAndTenantId(UserUpdateModel userUpdateModel);

    /**
     * 获取租户用户信息
     * @param userAccount
     * @param tenantId
     * @return
     */
    UserEntity getInfoByAccount(String userAccount, String tenantId);
}
