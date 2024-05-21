package jnpf.message.service;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.entity.SynThirdInfoEntity;
import jnpf.base.model.synthirdinfo.SynThirdTotal;
import jnpf.base.service.SuperService;
import jnpf.exception.WxErrorException;
import jnpf.model.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;

import java.util.List;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/23 17:29
 */
public interface SynThirdInfoService extends SuperService<SynThirdInfoEntity> {

    /**
     * 获取钉钉的配置信息
     * @return
     */
    BaseSystemInfo getDingTalkConfig();

    /**
     * 获取指定第三方工具、指定数据类型的数据列表
     * @param thirdType
     * @param dataType
     * @return
     */
    List<SynThirdInfoEntity> getList(String thirdType,String dataType);

    /**
     * 获取同步的详细信息
     * @param id
     * @return
     */
    SynThirdInfoEntity getInfo(String id);

    void create(SynThirdInfoEntity entity);

    boolean update(String id,SynThirdInfoEntity entity);

    void delete(SynThirdInfoEntity entity);

    /**
     * 获取指定第三方工具、指定数据类型、对象ID的同步信息
     * @param thirdType
     * @param dataType
     * @param id
     * @return
     */
    SynThirdInfoEntity getInfoBySysObjId(String thirdType,String dataType,String id);

    /**
     * 获取指定第三方工具、指定数据类型的同步统计信息
     * @param thirdType
     * @param dataType
     * @return
     */
    SynThirdTotal getSynTotal(String thirdType,String dataType);

    /**
     *
     * @param thirdToSysType
     * @param dataTypeOrg
     * @param SysToThirdType
     * @return
     */
    List<SynThirdInfoEntity> syncThirdInfoByType(String thirdToSysType, String dataTypeOrg, String SysToThirdType);

    boolean getBySysObjId(String id);

    String getSysByThird(String valueOf);

    void initBaseDept(Long dingRootDeptId, String access_token, String thirdType);

    /**
     * 获取指定第三方工具、指定数据类型、第三方对象ID的同步信息 20220331
     * @param thirdType
     * @param dataType
     * @param thirdObjId
     * @return
     */
    SynThirdInfoEntity getInfoByThirdObjId(String thirdType,String dataType,String thirdObjId);

    /**
     * 获取企业微信的配置信息
     * @return
     */
    BaseSystemInfo getQyhConfig();

    /**
     * 本地更新单个公司或部门到企业微信(供调用)
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    JSONObject updateDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity, String accessToken) throws WxErrorException;

    /**
     * 本地同步单个公司或部门到钉钉(供调用)
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken);

    /**
     * 本地更新单个公司或部门到钉钉(供调用)
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken);

    /**
     * 本地同步单个公司或部门到企业微信(供调用)
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    JSONObject createDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity,String accessToken) throws WxErrorException;
}
