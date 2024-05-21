package jnpf.base.service;

import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.model.datainterface.DataInterfaceActionModel;
import jnpf.base.model.datainterface.DataInterfacePage;
import jnpf.base.model.datainterface.PaginationDataInterface;
import jnpf.exception.DataException;

import java.util.List;
import java.util.Map;

/**
 * 数据接口业务层
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-23
 */
public interface DataInterfaceService extends SuperService<DataInterfaceEntity> {
    /**
     * 获取接口列表(分页)
     *
     * @param pagination 分页参数
     * @return ignore
     */
    List<DataInterfaceEntity> getList(PaginationDataInterface pagination, String dataType, Integer isSelector);

    /**
     * 通过ids获取列表
     *
     * @param ids
     * @return
     */
    List<DataInterfaceEntity> getList(List<String> ids);

    /**
     * 获取接口列表下拉框
     *
     * @return ignore
     * @param filterPage
     */
    List<DataInterfaceEntity> getList(boolean filterPage);

    /**
     * 获取接口数据
     *
     * @param id 主键
     * @return ignore
     */
    DataInterfaceEntity getInfo(String id);

    /**
     * 添加数据接口
     *
     * @param entity 实体
     */
    void create(DataInterfaceEntity entity);

    /**
     * 修改接口
     *
     * @param entity 实体
     * @param id     主键
     * @return 实体
     * @throws DataException ignore
     */
    boolean update(DataInterfaceEntity entity, String id) throws DataException;

    /**
     * 删除接口
     *
     * @param entity 实体
     */
    void delete(DataInterfaceEntity entity);

    /**
     * 判断接口名称是否重复
     *
     * @param fullName 名称
     * @param id       主键
     * @return ignore
     */
    boolean isExistByFullNameOrEnCode(String id, String fullName, String enCode);

    /**
     * 获取接口分页数据
     *
     * @param id   主键
     * @param page 分页参数
     * @return ignore
     */
    ActionResult infoToIdPageList(String id, DataInterfacePage page);

    /**
     * 获取接口详情数据
     *
     * @param id   主键
     * @param page 分页参数
     * @return ignore
     */
    List<Map<String, Object>> infoToInfo(String id, DataInterfacePage page);

    /**
     * 访问接口路径的应用认证
     *
     * @param id       主键
     * @param tenantId 租户encode
     * @param model      需要替换的参数
     * @return ignore
     */
    ActionResult infoToIdNew(String id, String tenantId, DataInterfaceActionModel model);

    /**
     * 检查参数
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/6/14
     */
    DataInterfaceActionModel checkParams(Map<String,String> map);


    /**
     * 访问接口路径
     *
     * @param id       主键
     * @param tenantId 租户encode
     * @param map      需要替换的参数
     * @return ignore
     */
    ActionResult infoToId(String id, String tenantId, Map<String, String> map);

    /**
     * 任务调度使用
     * @param id       主键
     * @param tenantId 租户encode
     * @param map      需要替换的参数
     * @param token    token
     * @return
     */
    ActionResult infoToId(String id, String tenantId, Map<String, String> map, String token, String appId, String invokType, Pagination pagination, Map<String,Object> showMap);

    /**
     *
     * @param interfaceId
     * @param tenantId
     * @return
     */
    DataInterfaceEntity getDataInterfaceInfo(String interfaceId, String tenantId);

    /**
     *
     * @param interfaceId
     * @param param
     * @return
     */
    ActionResult<List<Map<String, Object>>> infoByIds(String interfaceId, DataInterfacePage param);

    /**
     *
     * @param interfaceId
     * @param param
     * @return
     */
    ActionResult infoToIdById(String interfaceId, Map<String, String> param);
}
