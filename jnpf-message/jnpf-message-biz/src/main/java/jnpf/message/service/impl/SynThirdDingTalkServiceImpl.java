package jnpf.message.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.response.OapiV2UserListResponse;
import jnpf.base.UserInfo;
import jnpf.base.entity.SynThirdInfoEntity;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.model.synthirdinfo.DingTalkDeptModel;
import jnpf.base.model.synthirdinfo.DingTalkUserModel;
import jnpf.base.service.SynThirdDingTalkService;
import jnpf.base.service.SysconfigService;
import jnpf.base.util.SynDingTalkUtil;
import jnpf.base.util.SynThirdConsts;
import jnpf.message.service.SynThirdInfoService;
import jnpf.model.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RegexUtils;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * 本系统的公司-部门-用户同步到钉钉的功能代码
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/5/7 8:42
 */
@Service
public class SynThirdDingTalkServiceImpl implements SynThirdDingTalkService {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRelationService userRelationService;

    /**
     * 获取钉钉的配置信息
     * @return
     */
    @Override
    public BaseSystemInfo getDingTalkConfig() {
        Map<String, String> objModel = new HashMap<>();
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        BaseSystemInfo baseSystemInfo = JsonUtil.getJsonToBean(objModel, BaseSystemInfo.class);
        return baseSystemInfo;
    }

    //------------------------------------本系统同步公司、部门到钉钉-------------------------------------

    /**
     * 根据部门的同步表信息判断同步情况
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @param dingDeptList
     * @return
     */
//    public JSONObject checkDepartmentSysToDing(SynThirdInfoEntity synThirdInfoEntity, List<DingTalkDeptModel> dingDeptList) {
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("flag","");
//        retMsg.put("error","");
//
//        if(synThirdInfoEntity!=null){
//            if(StringUtil.isNotEmpty(synThirdInfoEntity.getThirdObjId())) {
//                // 同步表存在钉钉ID,仍需要判断钉钉上有没此部门
//                if(dingDeptList.stream().filter(t -> t.getDeptId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false){
//                    retMsg.put("code",false);
//                    retMsg.put("flag","1");
//                    retMsg.put("error","钉钉不存在同步表对应的部门ID!");
//                }
//            }else{
//                // 同步表的钉钉ID为空
//                retMsg.put("code",false);
//                retMsg.put("flag","2");
//                retMsg.put("error","同步表中部门对应的钉钉ID为空!");
//            }
//        }else{
//            // 上级部门未同步
//            retMsg.put("code",false);
//            retMsg.put("flag","3");
//            retMsg.put("error","部门未同步到钉钉!");
//        }
//
//        return retMsg;
//    }


    /**
     * 根据部门的同步表信息判断同步情况
     * 不带错第三方误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkDepartmentSysToDing(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                // 同步表的钉钉ID为空
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中部门对应的钉钉ID为空!");
            }
        }else{
            // 上级部门未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","部门未同步到钉钉!");
        }

        return retMsg;
    }

    public JSONObject checkDepartmentSysToDing2(List<String> objectIdList) {
        JSONObject retMsg = new JSONObject();
        List<String> thirdIdList = new ArrayList<>();
        retMsg.put("code",true);
        retMsg.put("error","");

        for(String objectId: objectIdList){
            SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,objectId);
            if(synThirdInfoEntity!=null){
                if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                    // 同步表的钉钉ID为空
                    retMsg.put("code",false);
                    retMsg.put("flag","2");
                    retMsg.put("error","同步表中部门对应的钉钉ID为空!");
                    return retMsg;
                }
            }else{
                // 上级部门未同步
                retMsg.put("code",false);
                retMsg.put("flag","3");
                retMsg.put("error","部门未同步到钉钉!");
                return retMsg;
            }
            thirdIdList.add(synThirdInfoEntity.getThirdObjId());
        }
        retMsg.put("flag",thirdIdList.stream().collect(Collectors.joining(",")));
        return retMsg;
    }


    /**
     * 检查部门名称不能含有特殊字符
     * @param deptName
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkDeptName(String deptName, String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                    Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("error","");
        if(deptName.indexOf("-")>-1 || deptName.indexOf(",")>-1 || deptName.indexOf("，")>-1){
            // 同步失败
            Integer synState = SynThirdConsts.SYN_STATE_FAIL;
            String description = deptFlag + "部门名称不能含有,、，、-三种特殊字符";

            // 更新同步表
            saveSynThirdInfoEntity(opType,synThirdInfoEntity,thirdType,dataType,sysObjId,thirdObjId,synState,description);

            retMsg.put("code", false);
            retMsg.put("error", description);
        }
        return retMsg;
    }


    /**
     * 将组织、用户的信息写入同步表
     * @param opType                "add":创建 “upd”:修改
     * @param synThirdInfoEntity    本地同步表信息
     * @param thirdType             第三方类型
     * @param dataType              数据类型
     * @param sysObjId              本地对象ID
     * @param thirdObjId            第三方对象ID
     * @param synState              同步状态(0:未同步;1:同步成功;2:同步失败)
     * @param description
     */
    public void saveSynThirdInfoEntity(String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                       Integer dataType, String sysObjId, String thirdObjId, Integer synState,
                                       String description) {
        UserInfo userInfo = userProvider.get();
        SynThirdInfoEntity entity = new SynThirdInfoEntity();
        String compValue = SynThirdConsts.OBJECT_OP_ADD;
        if(compValue.equals(opType)) {
            entity.setId(RandomUtil.uuId());
            entity.setThirdType(thirdType);
            entity.setDataType(dataType);
            entity.setSysObjId(sysObjId);
            entity.setThirdObjId(thirdObjId);
            entity.setEnabledMark(synState);
            // 备注当作同步失败信息来用
            entity.setDescription(description);
            entity.setCreatorUserId(userInfo.getUserId());
            entity.setCreatorTime(DateUtil.getNowDate());
            entity.setLastModifyUserId(userInfo.getUserId());
            // 修改时间当作最后同步时间来用
            entity.setLastModifyTime(DateUtil.getNowDate());
            synThirdInfoService.create(entity);
        }else{
            entity = synThirdInfoEntity;
            entity.setThirdType(thirdType);
            entity.setDataType(dataType);
            entity.setThirdObjId(thirdObjId);
            entity.setEnabledMark(synState);
            // 备注当作同步失败信息来用
            entity.setDescription(description);
            entity.setLastModifyUserId(userInfo.getUserId());
            // 修改时间当作最后同步时间来用
            entity.setLastModifyTime(DateUtil.getNowDate());
            synThirdInfoService.update(entity.getId(), entity);
        }
    }


    /**
     * 往钉钉创建组织-部门
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity, List<DingTalkDeptModel> dingDeptListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        DingTalkDeptModel deptModel = new DingTalkDeptModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        boolean isDeptDiff = true;
//        String deptFlag = "创建：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "创建：系统未设置单条同步");
//
//        // 支持同步
//        if(dingIsSyn==1){
//            // 获取 access_token 值
//            tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取钉钉上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else {
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                    if (deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                    } else {
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = deptFlag + "获取钉钉的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), deptEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                deptModel.setDeptId(null);
//                deptModel.setName(deptEntity.getFullName());
//                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
//                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
//                    //顶级节点时，钉钉的父节点设置为1
//                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
//                }else{
//                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
//
//                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity,dingDeptList);
//                    isDeptDiff = retMsg.getBoolean("code");
//                    if(isDeptDiff) {
//                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
//                    }
//                }
//                deptModel.setOrder(deptEntity.getSortCode());
//                deptModel.setCreateDeptGroup(false);
//
//                // 创建时：部门名称不能带有特殊字符
//                retMsg = checkDeptName(deptEntity.getFullName(),SynThirdConsts.OBJECT_OP_ADD,null,
//                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                if(isDeptDiff) {
//                    if(dingIsSyn==1) {
//                        // 往钉钉写入公司或部门
//                        retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);
//
//                        // 往同步写入本系统与第三方的对应信息
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功
//                            thirdObjId = retMsg.getString("retDeptId");
//                            retMsg.put("retDeptId", thirdObjId);
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                        } else {
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = deptFlag + retMsg.getString("error");
//                        }
//                    }else{
//                        // 未设置单条同步,归并到未同步状态
//                        // 未同步
//                        synState = SynThirdConsts.SYN_STATE_NO;
//                        description = deptFlag + "系统未设置单条同步";
//
//                        retMsg.put("code", true);
//                        retMsg.put("error", description);
//                        retMsg.put("retDeptId", "0");
//                    }
//                }else{
//                    // 同步失败,上级部门无对应的钉钉ID
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "部门所属的上级部门未同步到钉钉";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                    retMsg.put("retDeptId", "0");
//                }
//
//            }else{
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = deptFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", false);
//                retMsg.put("error", description);
//                retMsg.put("retDeptId", "0");
//            }
//
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉更新组织-部门
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity, List<DingTalkDeptModel> dingDeptListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        DingTalkDeptModel deptModel = new DingTalkDeptModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
//        String opType = "";
//        Integer synState = 0;
//        String description = "";
//        String thirdObjId = "";
//        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
//        boolean isDeptDiff = true;
//        String deptFlag = "更新：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步,设置需要同步到钉钉的对象属性值
//        if(dingIsSyn==1) {
//            // 获取 access_token
//            tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取钉钉上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else {
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                    if (deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                    } else {
//                        if (synThirdInfoEntity != null) {
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        } else {
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = deptFlag + "获取钉钉的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType, synThirdInfoPara, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), deptEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                deptModel.setDeptId(null);
//                deptModel.setName(deptEntity.getFullName());
//                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
//                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
//                    //顶级节点时，钉钉的父节点设置为1
//                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
//                } else {
//                    // 判断上级部门的合法性
//                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
//                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity, dingDeptList);
//                    isDeptDiff = retMsg.getBoolean("code");
//                    if (isDeptDiff) {
//                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
//                    }
//                }
//                deptModel.setOrder(deptEntity.getSortCode());
//
//                // 上级部门检查是否异常
//                if(isDeptDiff){
//                    // 获取同步表信息
//                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//
//                    // 判断当前部门对应的第三方的合法性
//                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity, dingDeptList);
//                    if (!retMsg.getBoolean("code")) {
//                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
//                            // flag:3 未同步，需要创建同步到钉钉、写入同步表
//                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到钉钉、写入同步表
//                            if("1".equals(retMsg.getString("flag"))) {
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            }
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//
//                            // 创建时：部门名称不能带有特殊字符
//                            retMsg = checkDeptName(deptEntity.getFullName(),
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往钉钉写入公司或部门
//                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);
//
//                            // 往同步写入本系统与第三方的对应信息
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = retMsg.getString("retDeptId");
//                                retMsg.put("retDeptId", thirdObjId);
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            }else{
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = deptFlag + retMsg.getString("error");
//                            }
//                        }
//
//                        if ("2".equals(retMsg.getString("flag"))) {
//                            // flag:2 已同步但第三方ID为空，需要创建同步到钉钉、修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = "";
//
//                            // 创建时：部门名称不能带有特殊字符
//                            retMsg = checkDeptName(deptEntity.getFullName(),
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往钉钉写入公司或部门
//                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);
//
//                            // 往同步表更新本系统与第三方的对应信息
//                            if (retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = retMsg.getString("retDeptId");
//                                retMsg.put("retDeptId", thirdObjId);
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            } else {
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = deptFlag + retMsg.getString("error");
//                            }
//                        }
//
//                    } else {
//                        // 更新同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//
//                        // 部门名称不能带有特殊字符
//                        retMsg = checkDeptName(deptEntity.getFullName(),
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 往钉钉写入公司或部门
//                        deptModel.setDeptId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
//
//                        // 设置部门主管：只有在更新时才可以执行
//                        // 初始化时：组织同步=>用户同步=>组织同步(用来更新部门主管的)
//                        if(StringUtil.isNotEmpty(deptEntity.getManager())){
//                            SynThirdInfoEntity userThirdInfo = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,deptEntity.getManager());
//                            if(userThirdInfo!=null){
//                                if(StringUtil.isNotEmpty(userThirdInfo.getThirdObjId())) {
//                                    deptModel.setDeptManagerUseridList(userThirdInfo.getThirdObjId());
//                                }
//                            }
//                        }
//
//                        retMsg = SynDingTalkUtil.updateDepartment(deptModel, access_token);
//
//                        // 往同步表更新本系统与第三方的对应信息
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                            description = "";
//                        } else {
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = deptFlag + retMsg.getString("error");
//                        }
//                    }
//                }else{
//                    // 同步失败,上级部门检查有异常
//                    if(synThirdInfoEntity!=null){
//                        // 修改同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//                    }else{
//                        // 写入同步表
//                        opType = SynThirdConsts.OBJECT_OP_ADD;
//                        synThirdInfoPara = null;
//                        thirdObjId = "";
//                    }
//
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = deptFlag + "上级部门无对应的钉钉ID";
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                }
//
//            }else{
//                // 同步失败
//                if(synThirdInfoEntity!=null){
//                    // 修改同步表
//                    opType = SynThirdConsts.OBJECT_OP_UPD;
//                    synThirdInfoPara = synThirdInfoEntity;
//                    thirdObjId = synThirdInfoEntity.getThirdObjId();
//                }else{
//                    // 写入同步表
//                    opType = SynThirdConsts.OBJECT_OP_ADD;
//                    synThirdInfoPara = null;
//                    thirdObjId = "";
//                }
//
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = deptFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", true);
//                retMsg.put("error", description);
//            }
//
//        }else{
//            // 未设置单条同步,归并到未同步状态
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
//            if(synThirdInfoEntity!=null){
//                // 修改同步表
//                opType = SynThirdConsts.OBJECT_OP_UPD;
//                synThirdInfoPara = synThirdInfoEntity;
//                thirdObjId = synThirdInfoEntity.getThirdObjId();
//            }else{
//                // 写入同步表
//                opType = SynThirdConsts.OBJECT_OP_ADD;
//                synThirdInfoPara = null;
//                thirdObjId = "";
//            }
//
//            synState = SynThirdConsts.SYN_STATE_NO;
//            description = deptFlag + "系统未设置单条同步";
//
//            retMsg.put("code", true);
//            retMsg.put("error", description);
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉删除组织-部门
     * 带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param dingDeptListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject deleteDepartmentSysToDing(boolean isBatch, String id, List<DingTalkDeptModel> dingDeptListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,id);
//        String deptFlag = "删除：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步
//        if(synThirdInfoEntity!=null) {
//            if(dingIsSyn==1){
//                // 获取 access_token
//                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取钉钉上的所有部门列表信息
//                    if(isBatch){
//                        dingDeptList = dingDeptListPara;
//                    }else{
//                        JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID,access_token);
//                        if(deptObject.getBoolean("code")) {
//                            dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                        }else{
//                            // 同步失败,获取部门列表失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "获取企业微信的部门列表信息失败");
//
//                            retMsg.put("code", false);
//                            retMsg.put("error", deptFlag + "获取钉钉的部门列表信息失败");
//                            return retMsg;
//                        }
//                    }
//
//                    // 删除钉钉对应的部门
//                    if (dingDeptList.stream().filter(t -> t.getDeptId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() > 0 ? true : false) {
//                        retMsg = SynDingTalkUtil.deleteDepartment(Long.parseLong(synThirdInfoEntity.getThirdObjId()), access_token);
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功,直接删除同步表记录
//                            synThirdInfoService.delete(synThirdInfoEntity);
//                        } else {
//                            // 同步失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + retMsg.getString("error"));
//                        }
//                    }else{
//                        // 根据钉钉ID找不到相应的信息,直接删除同步表记录
//                        synThirdInfoService.delete(synThirdInfoEntity);
//                    }
//                }else{
//                    // 同步失败
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "access_token值为空,不能同步信息");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", deptFlag + "access_token值为空,不能同步信息！");
//                }
//
//            }else{
//                // 未设置单条同步，归并到未同步状态
//                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, deptFlag + "系统未设置单条同步");
//
//                retMsg.put("code", true);
//                retMsg.put("error", deptFlag + "系统未设置单条同步");
//            }
//        }
//
//        return retMsg;
//    }


    /**
     * 往钉钉创建组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject createDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkDeptModel deptModel = new DingTalkDeptModel();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        boolean isDeptDiff = true;
        String deptFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "创建：系统未设置单条同步");

        // 支持同步
        if(isBatch || dingIsSyn==1){
            // 获取 access_token 值
            if(isBatch) {
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            if (access_token != null && !"".equals(access_token)) {
                deptModel.setDeptId(null);
                deptModel.setName(deptEntity.getFullName());
                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，钉钉的父节点设置为1
                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
                }else{
                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());

                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if(isDeptDiff) {
                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
                    }
                }
                deptModel.setOrder(deptEntity.getSortCode());
                deptModel.setCreateDeptGroup(false);

                // 创建时：部门名称不能带有特殊字符
                retMsg = checkDeptName(deptEntity.getFullName(),SynThirdConsts.OBJECT_OP_ADD,null,
                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                if (!retMsg.getBoolean("code")) {
                    return retMsg;
                }

                if(isDeptDiff) {
                    if(isBatch || dingIsSyn==1) {
                        // 往钉钉写入公司或部门
                        retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);

                        // 往同步写入本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            thirdObjId = retMsg.getString("retDeptId");
                            retMsg.put("retDeptId", thirdObjId);
                            synState = SynThirdConsts.SYN_STATE_OK;
                        } else {
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = deptFlag + retMsg.getString("error");
                        }
                    }else{
                        // 未设置单条同步,归并到未同步状态
                        // 未同步
                        synState = SynThirdConsts.SYN_STATE_NO;
                        description = deptFlag + "系统未设置单条同步";

                        retMsg.put("code", true);
                        retMsg.put("error", description);
                        retMsg.put("retDeptId", "0");
                    }
                }else{
                    // 同步失败,上级部门无对应的钉钉ID
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = deptFlag + "部门所属的上级部门未同步到钉钉";

                    retMsg.put("code", false);
                    retMsg.put("error", description);
                    retMsg.put("retDeptId", "0");
                }

            }else{
                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = deptFlag + "access_token值为空,不能同步信息";

                retMsg.put("code", false);
                retMsg.put("error", description);
                retMsg.put("retDeptId", "0");
            }

        }

        // 更新同步表
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往钉钉更新组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject updateDepartmentSysToDing(boolean isBatch, OrganizeEntity deptEntity,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkDeptModel deptModel = new DingTalkDeptModel();
        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
        String opType = "";
        Integer synState = 0;
        String description = "";
        String thirdObjId = "";
        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
        boolean isDeptDiff = true;
        String deptFlag = "更新：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步,设置需要同步到钉钉的对象属性值
        if(isBatch || dingIsSyn==1) {
            // 获取 access_token
            if(isBatch) {
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

            if (access_token != null && !"".equals(access_token)) {
                deptModel.setDeptId(null);
                deptModel.setName(deptEntity.getFullName());
                // 从本地数据库的同步表获取对应的钉钉ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，钉钉的父节点设置为1
                    deptModel.setParentId(SynThirdConsts.DING_ROOT_DEPT_ID);
                } else {
                    // 判断上级部门的合法性
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());

                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if (isDeptDiff) {
                        deptModel.setParentId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));
                    }
                }
                deptModel.setOrder(deptEntity.getSortCode());

                // 上级部门检查是否异常
                if(isDeptDiff){
                    // 获取同步表信息
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

                    // 判断当前部门对应的第三方的合法性
                    retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
                    if (!retMsg.getBoolean("code")) {
                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
                            // flag:3 未同步，需要创建同步到钉钉、写入同步表
                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到钉钉、写入同步表
                            if("1".equals(retMsg.getString("flag"))) {
                                synThirdInfoService.delete(synThirdInfoEntity);
                            }
                            opType = SynThirdConsts.OBJECT_OP_ADD;
                            synThirdInfoPara = null;
                            thirdObjId = "";

                            // 创建时：部门名称不能带有特殊字符
                            retMsg = checkDeptName(deptEntity.getFullName(),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往钉钉写入公司或部门
                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);

                            // 往同步写入本系统与第三方的对应信息
                            if(retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                retMsg.put("retDeptId", thirdObjId);
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            }else{
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = deptFlag + retMsg.getString("error");
                            }
                        }

                        if ("2".equals(retMsg.getString("flag"))) {
                            // flag:2 已同步但第三方ID为空，需要创建同步到钉钉、修改同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = "";

                            // 创建时：部门名称不能带有特殊字符
                            retMsg = checkDeptName(deptEntity.getFullName(),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往钉钉写入公司或部门
                            retMsg = SynDingTalkUtil.createDepartment(deptModel, access_token);

                            // 往同步表更新本系统与第三方的对应信息
                            if (retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                retMsg.put("retDeptId", thirdObjId);
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            } else {
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = deptFlag + retMsg.getString("error");
                            }
                        }

                    } else {
                        // 更新同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();

                        // 部门名称不能带有特殊字符
                        retMsg = checkDeptName(deptEntity.getFullName(),
                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                        if (!retMsg.getBoolean("code")) {
                            return retMsg;
                        }

                        // 往钉钉写入公司或部门
                        deptModel.setDeptId(Long.parseLong(synThirdInfoEntity.getThirdObjId()));

                        // 设置部门主管：只有在更新时才可以执行
                        // 初始化时：组织同步=>用户同步=>组织同步(用来更新部门主管的)
                        if(StringUtil.isNotEmpty(deptEntity.getManagerId())){
                            SynThirdInfoEntity userThirdInfo = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,deptEntity.getManagerId());
                            if(userThirdInfo!=null){
                                if(StringUtil.isNotEmpty(userThirdInfo.getThirdObjId())) {
                                    deptModel.setDeptManagerUseridList(userThirdInfo.getThirdObjId());
                                }
                            }
                        }

                        retMsg = SynDingTalkUtil.updateDepartment(deptModel, access_token);

                        // 往同步表更新本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            synState = SynThirdConsts.SYN_STATE_OK;
                            description = "";
                        } else {
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = deptFlag + retMsg.getString("error");
                        }
                    }
                }else{
                    // 同步失败,上级部门检查有异常
                    if(synThirdInfoEntity!=null){
                        // 修改同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();
                    }else{
                        // 写入同步表
                        opType = SynThirdConsts.OBJECT_OP_ADD;
                        synThirdInfoPara = null;
                        thirdObjId = "";
                    }

                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = deptFlag + "上级部门无对应的钉钉ID";

                    retMsg.put("code", false);
                    retMsg.put("error", description);
                }

            }else{
                // 同步失败
                if(synThirdInfoEntity!=null){
                    // 修改同步表
                    opType = SynThirdConsts.OBJECT_OP_UPD;
                    synThirdInfoPara = synThirdInfoEntity;
                    thirdObjId = synThirdInfoEntity.getThirdObjId();
                }else{
                    // 写入同步表
                    opType = SynThirdConsts.OBJECT_OP_ADD;
                    synThirdInfoPara = null;
                    thirdObjId = "";
                }

                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = deptFlag + "access_token值为空,不能同步信息";

                retMsg.put("code", true);
                retMsg.put("error", description);
            }

        }else{
            // 未设置单条同步,归并到未同步状态
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
            if(synThirdInfoEntity!=null){
                // 修改同步表
                opType = SynThirdConsts.OBJECT_OP_UPD;
                synThirdInfoPara = synThirdInfoEntity;
                thirdObjId = synThirdInfoEntity.getThirdObjId();
            }else{
                // 写入同步表
                opType = SynThirdConsts.OBJECT_OP_ADD;
                synThirdInfoPara = null;
                thirdObjId = "";
            }

            synState = SynThirdConsts.SYN_STATE_NO;
            description = deptFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往钉钉删除组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id        本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject deleteDepartmentSysToDing(boolean isBatch, String id,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,id);
        String deptFlag = "删除：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            if(isBatch || dingIsSyn==1){
                // 获取 access_token
                if(isBatch) {
                    access_token = accessToken;
                }else{
                    tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                if (access_token != null && !"".equals(access_token)) {
                    // 删除钉钉对应的部门
                    if (!"".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) && !"null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                        retMsg = SynDingTalkUtil.deleteDepartment(Long.parseLong(synThirdInfoEntity.getThirdObjId()), access_token);
                        if (retMsg.getBoolean("code")) {
                            // 同步成功,直接删除同步表记录
                            synThirdInfoService.delete(synThirdInfoEntity);
                        } else {
                            // 同步失败
                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + retMsg.getString("error"));
                        }
                    }else{
                        // 根据钉钉ID找不到相应的信息,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }
                }else{
                    // 同步失败
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, deptFlag + "access_token值为空,不能同步信息");

                    retMsg.put("code", false);
                    retMsg.put("error", deptFlag + "access_token值为空,不能同步信息！");
                }

            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, deptFlag + "系统未设置单条同步");

                retMsg.put("code", true);
                retMsg.put("error", deptFlag + "系统未设置单条同步");
            }
        }

        return retMsg;
    }


    //------------------------------------本系统同步用户到钉钉-------------------------------------

    /**
     * 设置需要提交给钉钉接口的单个成员信息
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param userEntity 本地用户信息
     * @param dingDeptList 钉钉的部门信息
     * @return
     */
//    public JSONObject setDingUserObject(UserEntity userEntity, List<DingTalkDeptModel> dingDeptList) throws ParseException {
//        DingTalkUserModel userModel = new DingTalkUserModel();
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code", true);
//        retMsg.put("error", "");
//
//        // 验证邮箱格式的格式合法性、唯一性
//        if(StringUtil.isNotEmpty(userEntity.getEmail())){
//            if(!RegexUtils.checkEmail(userEntity.getEmail())){
//                retMsg.put("code", false);
//                retMsg.put("error", "邮箱格式不合法！");
//                retMsg.put("dingUserObject", null);
//                return retMsg;
//            }
//        }
//
//        // 判断手机号的合法性
//        if(StringUtil.isNotEmpty(userEntity.getMobilePhone())){
//            if(!RegexUtils.checkMobile(userEntity.getMobilePhone())){
//                retMsg.put("code", false);
//                retMsg.put("error", "手机号不合法！");
//                retMsg.put("dingUserObject", null);
//                return retMsg;
//            }
//        }
//
//        userModel.setUserid(userEntity.getId());
//        userModel.setName(userEntity.getRealName());
//        userModel.setMobile(userEntity.getMobilePhone());
//        userModel.setTelephone(userEntity.getLandline());
//        userModel.setJobNumber(userEntity.getAccount());
//
//        PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId());
//        String jobName = "";
//        if(positionEntity!=null){
//            jobName = positionEntity.getFullName();
//            userModel.setTitle(jobName);
//        }
//
//        userModel.setWorkPlace(userEntity.getPostalAddress());
//
//        if(userEntity.getEntryDate()!= null){
//            String entryDate = DateUtil.daFormat(userEntity.getEntryDate());
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            df.setTimeZone(TimeZone.getTimeZone("GMT"));
//            if(df.parse(entryDate).getTime()>0) {
//                userModel.setHiredDate(df.parse(entryDate).getTime());
//            }
//        }
//
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,userEntity.getOrganizeId());
//        retMsg = checkDepartmentSysToDing(synThirdInfoEntity,dingDeptList);
//        if(retMsg.getBoolean("code")){
//            userModel.setDeptIdList(synThirdInfoEntity.getThirdObjId());
//        }else{
//            retMsg.put("code", false);
//            retMsg.put("error", "部门找不到对应的钉钉ID！");
//            retMsg.put("dingUserObject", null);
//            return retMsg;
//        }
//        userModel.setEmail(userEntity.getEmail());
//
//        retMsg.put("dingUserObject", userModel);
//        return retMsg;
//    }


    /**
     * 设置需要提交给钉钉接口的单个成员信息
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param userEntity 本地用户信息
     * @return
     */
    public JSONObject setDingUserObject(UserEntity userEntity) throws ParseException {
        DingTalkUserModel userModel = new DingTalkUserModel();
        JSONObject retMsg = new JSONObject();
        retMsg.put("code", true);
        retMsg.put("error", "");

        // 验证邮箱格式的格式合法性、唯一性
        if(StringUtil.isNotEmpty(userEntity.getEmail())){
            if(!RegexUtils.checkEmail(userEntity.getEmail())){
                retMsg.put("code", false);
                retMsg.put("error", "邮箱格式不合法！");
                retMsg.put("dingUserObject", null);
                return retMsg;
            }
        }

        // 判断手机号的合法性
        if(StringUtil.isNotEmpty(userEntity.getMobilePhone())){
            if(!RegexUtils.checkMobile(userEntity.getMobilePhone())){
                retMsg.put("code", false);
                retMsg.put("error", "手机号不合法！");
                retMsg.put("dingUserObject", null);
                return retMsg;
            }
        }

        userModel.setUserid(userEntity.getId());
        userModel.setName(userEntity.getRealName());
        userModel.setMobile(userEntity.getMobilePhone());
        userModel.setTelephone(userEntity.getLandline());
        userModel.setJobNumber(userEntity.getAccount());

        PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId());
        String jobName = "";
        if(positionEntity!=null){
            jobName = positionEntity.getFullName();
            userModel.setTitle(jobName);
        }

        userModel.setWorkPlace(userEntity.getPostalAddress());

        if(userEntity.getEntryDate()!= null){
            String entryDate = DateUtil.daFormat(userEntity.getEntryDate());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            if(df.parse(entryDate).getTime()>0) {
                userModel.setHiredDate(df.parse(entryDate).getTime());
            }
        }

        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG,userEntity.getOrganizeId());
        // retMsg = checkDepartmentSysToDing(synThirdInfoEntity,dingDeptList);
//        retMsg = checkDepartmentSysToDing(synThirdInfoEntity);
        List<UserRelationEntity> userRelationList = userRelationService.getListByUserId(userEntity.getId(),"Organize");
        List<String> objectIdList = userRelationList.stream().map(t->t.getObjectId()).collect(Collectors.toList());
        retMsg = checkDepartmentSysToDing2(objectIdList);
        if(retMsg.getBoolean("code")){
//            userModel.setDeptIdList(synThirdInfoEntity.getThirdObjId());
            userModel.setDeptIdList(retMsg.getString("flag"));
        }else{
            retMsg.put("code", false);
            retMsg.put("error", "部门找不到对应的钉钉ID！");
            retMsg.put("dingUserObject", null);
            return retMsg;
        }
        userModel.setEmail(userEntity.getEmail());

        retMsg.put("dingUserObject", userModel);
        return retMsg;
    }


    /**
     * 判断用户的手机号、邮箱是否唯一，钉钉不允许重复
     * @param mobile
     * @param email
     * @param userId
     * @param dingUserList
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkUserMobileEmailRepeat(String mobile, String email, String userId, List<DingTalkUserModel> dingUserList,
                                                 String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                                 Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        boolean isDiff = true;
        String description = "";
        JSONObject retMsg = new JSONObject();

        // 钉钉限制：手机号唯一性
        if(StringUtil.isNotEmpty(mobile)){
            if(StringUtil.isNotEmpty(userId)){
                if(dingUserList.stream().filter(t -> String.valueOf(t.getMobile()).equals(mobile) && !(t.getUserid().equals(userId))).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定手机号:" + mobile;
                }
            }else{
                if(dingUserList.stream().filter(t -> String.valueOf(t.getMobile()).equals(mobile)).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定手机号:" + mobile;
                }
            }
        }

        // 钉钉限制：邮箱地址唯一性
        if(StringUtil.isNotEmpty(email)){
            if(StringUtil.isNotEmpty(userId)){
                if(dingUserList.stream().filter(t -> String.valueOf(t.getEmail()).equals(email) && !(t.getUserid().equals(userId))).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定此邮箱:" + email;
                }
            }else{
                if(dingUserList.stream().filter(t -> String.valueOf(t.getEmail()).equals(email)).count() > 0 ? true : false){
                    isDiff = false;
                    description = deptFlag + "钉钉内已有绑定此邮箱:" + email;
                }
            }
        }

        retMsg.put("code",isDiff);
        retMsg.put("error",description);

        if(!isDiff){
            // 同步失败
            Integer synState = SynThirdConsts.SYN_STATE_FAIL;

            // 更新同步表
            saveSynThirdInfoEntity(opType,synThirdInfoEntity,thirdType,dataType,sysObjId,thirdObjId,synState,description);
        }

        return retMsg;
    }


    /**
     * 根据用户的同步表信息判断同步情况
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @param dingUserList
     * @return
     */
//    public JSONObject checkUserSysToDing(SynThirdInfoEntity synThirdInfoEntity, List<DingTalkUserModel> dingUserList) {
//        JSONObject retMsg = new JSONObject();
//        retMsg.put("code",true);
//        retMsg.put("flag","");
//        retMsg.put("error","");
//
//        if(synThirdInfoEntity!=null){
//            if(StringUtil.isNotEmpty(synThirdInfoEntity.getThirdObjId())) {
//                // 同步表存在钉钉ID,仍需要判断钉钉上有没此用户
//                if(dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false){
//                    retMsg.put("code",false);
//                    retMsg.put("flag","1");
//                    retMsg.put("error","钉钉不存在同步表对应的用户ID!");
//                }
//            }else{
//                // 同步表的企业微信ID为空
//                retMsg.put("code",false);
//                retMsg.put("flag","2");
//                retMsg.put("error","同步表中用户对应的钉钉ID为空!");
//            }
//        }else{
//            // 上级用户未同步
//            retMsg.put("code",false);
//            retMsg.put("flag","3");
//            retMsg.put("error","用户未同步!");
//        }
//
//        return retMsg;
//    }

    /**
     * 根据用户的同步表信息判断同步情况
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkUserSysToDing(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                // 同步表的企业微信ID为空
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中用户对应的钉钉ID为空!");
            }
        }else{
            // 上级用户未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","用户未同步!");
        }

        return retMsg;
    }


    /**
     * 往钉钉创建用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject createUserSysToDing(boolean isBatch, UserEntity userEntity, List<DingTalkDeptModel> dingDeptListPara,
//                                          List<DingTalkUserModel> dingUserListPara) throws ParseException {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//        String access_token = tokenObject.getString("access_token");
//        JSONObject retMsg = new JSONObject();
//        DingTalkUserModel userObjectModel = new DingTalkUserModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        String userFlag = "创建：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", userFlag + "系统未设置单条同步");
//
//        if (dingIsSyn==1){
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取企业微信上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else{
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department",List.class), DingTalkDeptModel.class);
//                    }else{
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取钉钉的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                // 获取钉钉上的所有用户列表信息
//                if(isBatch){
//                    dingUserList = dingUserListPara;
//                }else {
//                    JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList, access_token);
//                    if (userObject.getBoolean("code")) {
//                        dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist", List.class), DingTalkUserModel.class);
//                    } else {
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取钉钉的用户列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                // 判断用户的手机号、邮箱是否唯一,不能重复
//                retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                        SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                if (!retMsg.getBoolean("code")) {
//                    return retMsg;
//                }
//
//                // 要同步到钉钉的对象赋值
//                retMsg = setDingUserObject(userEntity, dingDeptList);
//                if (retMsg.getBoolean("code")) {
//                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);
//
//                    // 往企业微信写入成员
//                    retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
//
//                    // 往同步写入本系统与第三方的对应信息
//                    if (retMsg.getBoolean("code")) {
//                        // 同步成功
//                        thirdObjId = userEntity.getId();
//                        synState = SynThirdConsts.SYN_STATE_OK;
//                    } else {
//                        // 同步失败
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + retMsg.getString("error");
//                    }
//                }else{
//                    // 同步失败,原因：部门找不到对应的第三方ID、邮箱格式不合法
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = userFlag + retMsg.getString("error");
//                }
//
//            }else{
//                // 同步失败
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = userFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", false);
//                retMsg.put("error", description);
//            }
//
//        }else{
//            // 无须同步，未同步状态
//            synState = SynThirdConsts.SYN_STATE_NO;
//            description = userFlag + "系统未设置单条同步";
//
//            retMsg.put("code", true);
//            retMsg.put("error", description);
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉更新用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject updateUserSysToDing(boolean isBatch, UserEntity userEntity, List<DingTalkDeptModel> dingDeptListPara,
//                                          List<DingTalkUserModel> dingUserListPara) throws ParseException {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        DingTalkUserModel userObjectModel = new DingTalkUserModel();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
//        SynThirdInfoEntity entity = new SynThirdInfoEntity();
//        String opType = "";
//        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
//        String thirdObjId = "";
//        Integer synState = 0;
//        String description = "";
//        String userFlag = "更新：";
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", userFlag + "系统未设置单条同步");
//
//        // 支持同步
//        if (dingIsSyn==1){
//            // 获取 access_token
//            tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            access_token = tokenObject.getString("access_token");
//
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
//            if (access_token != null && !"".equals(access_token)) {
//                // 获取企业微信上的所有部门列表信息
//                if(isBatch){
//                    dingDeptList = dingDeptListPara;
//                }else{
//                    JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID,access_token);
//                    if(deptObject.getBoolean("code")) {
//                        dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department",List.class), DingTalkDeptModel.class);
//                    }else{
//                        if(synThirdInfoEntity!=null){
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        }else{
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取企业微信的部门列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//                        return retMsg;
//                    }
//                }
//
//
//                // 获取钉钉上的所有用户列表信息
//                if(isBatch){
//                    dingUserList = dingUserListPara;
//                }else {
//                    JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList, access_token);
//                    if (userObject.getBoolean("code")) {
//                        dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist", List.class), DingTalkUserModel.class);
//                    } else {
//                        if (synThirdInfoEntity != null) {
//                            // 修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = synThirdInfoEntity.getThirdObjId();
//                        } else {
//                            // 写入同步表
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//                        }
//
//                        synState = SynThirdConsts.SYN_STATE_FAIL;
//                        description = userFlag + "获取钉钉的用户列表信息失败";
//
//                        retMsg.put("code", false);
//                        retMsg.put("error", description);
//
//                        // 更新同步表
//                        saveSynThirdInfoEntity(opType, synThirdInfoPara, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);
//
//                        return retMsg;
//                    }
//                }
//
//                // 要同步到企业微信的对象赋值
//                retMsg = setDingUserObject(userEntity,dingDeptList);
//                if (retMsg.getBoolean("code")) {
//                    // 判断当前用户对应的第三方的合法性
//                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);
//                    retMsg = checkUserSysToDing(synThirdInfoEntity, dingUserList);
//                    if (!retMsg.getBoolean("code")) {
//                        if("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))){
//                            // flag:3 未同步，需要创建同步到企业微信、写入同步表
//                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
//                            if("1".equals(retMsg.getString("flag"))) {
//                                synThirdInfoService.delete(synThirdInfoEntity);
//                            }
//                            opType = SynThirdConsts.OBJECT_OP_ADD;
//                            synThirdInfoPara = null;
//                            thirdObjId = "";
//
//                            // 判断用户的手机号、邮箱是否唯一,不能重复
//                            retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往企业微信写入成员
//                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = userEntity.getId();
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            }else{
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = userFlag + retMsg.getString("error");
//                            }
//                        }
//
//                        if("2".equals(retMsg.getString("flag"))){
//                            // 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
//                            opType = SynThirdConsts.OBJECT_OP_UPD;
//                            synThirdInfoPara = synThirdInfoEntity;
//                            thirdObjId = "";
//
//                            // 判断用户的手机号、邮箱是否唯一,不能重复
//                            retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                            if (!retMsg.getBoolean("code")) {
//                                return retMsg;
//                            }
//
//                            // 往企业微信写入成员
//                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
//                            if(retMsg.getBoolean("code")) {
//                                // 同步成功
//                                thirdObjId = userEntity.getId();
//                                synState = SynThirdConsts.SYN_STATE_OK;
//                                description = "";
//                            }else{
//                                // 同步失败
//                                synState = SynThirdConsts.SYN_STATE_FAIL;
//                                description = userFlag + retMsg.getString("error");
//                            }
//                        }
//
//                    }else{
//                        // 更新同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//
//                        // 判断用户的手机号、邮箱是否唯一,不能重复
//                        retMsg = checkUserMobileEmailRepeat(userEntity.getMobilePhone(),userEntity.getEmail(),thirdObjId,dingUserList,
//                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,userFlag);
//                        if (!retMsg.getBoolean("code")) {
//                            return retMsg;
//                        }
//
//                        // 往企业微信更新成员信息
//                        retMsg = SynDingTalkUtil.updateUser(userObjectModel, access_token);
//                        if(retMsg.getBoolean("code")) {
//                            // 同步成功
//                            synState = SynThirdConsts.SYN_STATE_OK;
//                            description = "";
//                        }else{
//                            // 同步失败
//                            synState = SynThirdConsts.SYN_STATE_FAIL;
//                            description = userFlag + retMsg.getString("error");
//                        }
//
//                    }
//
//                }else{
//                    // 同步失败,原因：用户所属部门找不到相应的企业微信ID、邮箱格式不合法
//                    if(synThirdInfoEntity!=null){
//                        // 修改同步表
//                        opType = SynThirdConsts.OBJECT_OP_UPD;
//                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = synThirdInfoEntity.getThirdObjId();
//                    }else{
//                        // 写入同步表
//                        opType = SynThirdConsts.OBJECT_OP_ADD;
//                        synThirdInfoPara = null;
//                        thirdObjId = "";
//                    }
//                    synState = SynThirdConsts.SYN_STATE_FAIL;
//                    description = userFlag + retMsg.getString("error");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", description);
//                }
//
//
//            }else{
//                // 同步失败
//                if(synThirdInfoEntity!=null){
//                    // 修改同步表
//                    opType = SynThirdConsts.OBJECT_OP_UPD;
//                    synThirdInfoPara = synThirdInfoEntity;
//                    thirdObjId = synThirdInfoEntity.getThirdObjId();
//                }else{
//                    // 写入同步表
//                    opType = SynThirdConsts.OBJECT_OP_ADD;
//                    synThirdInfoPara = null;
//                    thirdObjId = "";
//                }
//
//                synState = SynThirdConsts.SYN_STATE_FAIL;
//                description = userFlag + "access_token值为空,不能同步信息";
//
//                retMsg.put("code", true);
//                retMsg.put("error", description);
//            }
//
//        }else{
//            // 未设置单条同步,归并到未同步状态
//            // 获取同步表信息
//            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
//            if(synThirdInfoEntity!=null){
//                // 修改同步表
//                opType = SynThirdConsts.OBJECT_OP_UPD;
//                synThirdInfoPara = synThirdInfoEntity;
//                thirdObjId = synThirdInfoEntity.getThirdObjId();
//            }else{
//                // 写入同步表
//                opType = SynThirdConsts.OBJECT_OP_ADD;
//                synThirdInfoPara = null;
//                thirdObjId = "";
//            }
//
//            synState = SynThirdConsts.SYN_STATE_NO;
//            description = userFlag + "系统未设置单条同步";
//
//            retMsg.put("code", true);
//            retMsg.put("error", description);
//        }
//
//        // 更新同步表
//        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);
//
//        return retMsg;
//    }


    /**
     * 往钉钉删除用户
     * 带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param dingDeptListPara 单条执行时为null
     * @param dingUserListPara 单条执行时为null
     * @return
     */
//    @Override
//    public JSONObject deleteUserSysToDing(boolean isBatch, String id, List<DingTalkDeptModel> dingDeptListPara,
//                                          List<DingTalkUserModel> dingUserListPara) {
//        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        // 单条记录执行时,受开关限制
//        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
//        JSONObject retMsg = new JSONObject();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,id);
//
//        // 返回值初始化
//        retMsg.put("code", true);
//        retMsg.put("error", "系统未设置单条同步");
//
//        // 支持同步
//        if(synThirdInfoEntity!=null) {
//            if(dingIsSyn==1) {
//                // 获取 access_token
//                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//                access_token = tokenObject.getString("access_token");
//
//                if (access_token != null && !"".equals(access_token)) {
//                    // 获取企业微信上的所有部门列表信息
//                    if(isBatch){
//                        dingDeptList = dingDeptListPara;
//                    }else {
//                        JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                        if (deptObject.getBoolean("code")) {
//                            dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                        } else {
//                            // 更新同步表
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "获取钉钉的部门列表信息失败");
//
//                            retMsg.put("code", true);
//                            retMsg.put("error", "获取钉钉的部门列表信息失败");
//                            return retMsg;
//                        }
//                    }
//
//                    // 获取企业微信上的所有成员信息列表
//                    if(isBatch){
//                        dingUserList = dingUserListPara;
//                    }else{
//                        JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList,access_token);
//                        if(userObject.getBoolean("code")) {
//                            dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist",List.class), DingTalkUserModel.class);
//                        }else{
//                            // 同步失败，获取企业微信的成员列表信息失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "获取企业微信的成员列表信息失败");
//
//                            retMsg.put("code", false);
//                            retMsg.put("error", "获取企业微信的成员列表信息失败");
//                            return retMsg;
//                        }
//                    }
//
//                    // 删除企业对应的用户
//                    if(dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() > 0 ? true : false){
//                        retMsg = SynDingTalkUtil.deleteUser(synThirdInfoEntity.getThirdObjId(), access_token);
//                        if (retMsg.getBoolean("code")) {
//                            // 同步成功,直接删除同步表记录
//                            synThirdInfoService.delete(synThirdInfoEntity);
//                        }else{
//                            // 同步失败
//                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
//                        }
//                    }else{
//                        // 根据企业微信ID找不到相应的信息,直接删除同步表记录
//                        synThirdInfoService.delete(synThirdInfoEntity);
//                    }
//                }else{
//                    // 同步失败
//                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "access_token值为空,不能同步信息");
//
//                    retMsg.put("code", false);
//                    retMsg.put("error", "access_token值为空,不能同步信息！");
//                }
//
//            }else{
//                // 未设置单条同步，归并到未同步状态
//                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
//                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, "系统未设置同步");
//
//                retMsg.put("code", true);
//                retMsg.put("error", "系统未设置单条同步");
//            }
//        }
//
//        return retMsg;
//    }



    /**
     * 往钉钉创建用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject createUserSysToDing(boolean isBatch, UserEntity userEntity,String accessToken) throws ParseException {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkUserModel userObjectModel = new DingTalkUserModel();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        String userFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", userFlag + "系统未设置单条同步");

        if (isBatch || dingIsSyn==1){
            if(isBatch){
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            if (access_token != null && !"".equals(access_token)) {
                // 要同步到钉钉的对象赋值
                retMsg = setDingUserObject(userEntity);
                if (retMsg.getBoolean("code")) {
                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);

                    // 往企业微信写入成员
                    retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);

                    // 往同步写入本系统与第三方的对应信息
                    if (retMsg.getBoolean("code")) {
                        // 同步成功
                        thirdObjId = userEntity.getId();
                        synState = SynThirdConsts.SYN_STATE_OK;
                    } else {
                        // 同步失败
                        synState = SynThirdConsts.SYN_STATE_FAIL;
                        description = userFlag + retMsg.getString("error");
                    }
                }else{
                    // 同步失败,原因：部门找不到对应的第三方ID、邮箱格式不合法
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = userFlag + retMsg.getString("error");
                }

            }else{
                // 同步失败
                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = userFlag + "access_token值为空,不能同步信息";

                retMsg.put("code", false);
                retMsg.put("error", description);
            }

        }else{
            // 无须同步，未同步状态
            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), userEntity.getId(), thirdObjId, synState, description);

        return retMsg;
    }


    /**
     * 往钉钉更新用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param userEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject updateUserSysToDing(boolean isBatch, UserEntity userEntity,String accessToken) throws ParseException {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkUserModel userObjectModel = new DingTalkUserModel();
        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
        SynThirdInfoEntity entity = new SynThirdInfoEntity();
        String opType = "";
        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        String userFlag = "更新：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", userFlag + "系统未设置单条同步");

        // 支持同步
        if (isBatch || dingIsSyn==1){
            // 获取 access_token
            if(isBatch){
                access_token = accessToken;
            }else{
                tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
            if (access_token != null && !"".equals(access_token)) {
                // 要同步到企业微信的对象赋值
                retMsg = setDingUserObject(userEntity);
                if (retMsg.getBoolean("code")) {
                    // 判断当前用户对应的第三方的合法性
                    userObjectModel = retMsg.getObject("dingUserObject",DingTalkUserModel.class);
                    retMsg = checkUserSysToDing(synThirdInfoEntity);
                    if (!retMsg.getBoolean("code")) {
                        if("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))){
                            // flag:3 未同步，需要创建同步到企业微信、写入同步表
                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
                            if("1".equals(retMsg.getString("flag"))) {
                                synThirdInfoService.delete(synThirdInfoEntity);
                            }
                            opType = SynThirdConsts.OBJECT_OP_ADD;
                            synThirdInfoPara = null;
                            thirdObjId = "";

                            // 往企业微信写入成员
                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
                            if(retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = userEntity.getId();
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            }else{
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = userFlag + retMsg.getString("error");
                            }
                        }

                        if("2".equals(retMsg.getString("flag"))){
                            // 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = "";

                            // 往企业微信写入成员
                            retMsg = SynDingTalkUtil.createUser(userObjectModel, access_token);
                            if(retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = userEntity.getId();
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            }else{
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = userFlag + retMsg.getString("error");
                            }
                        }

                    }else{
                        // 更新同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();

                        // 往企业微信更新成员信息
                        retMsg = SynDingTalkUtil.updateUser(userObjectModel, access_token);
                        if(retMsg.getBoolean("code")) {
                            // 同步成功
                            synState = SynThirdConsts.SYN_STATE_OK;
                            description = "";
                        }else{
                            // 同步失败
                            synState = SynThirdConsts.SYN_STATE_FAIL;
                            description = userFlag + retMsg.getString("error");
                        }

                    }

                }else{
                    // 同步失败,原因：用户所属部门找不到相应的企业微信ID、邮箱格式不合法
                    if(synThirdInfoEntity!=null){
                        // 修改同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
                        thirdObjId = synThirdInfoEntity.getThirdObjId();
                    }else{
                        // 写入同步表
                        opType = SynThirdConsts.OBJECT_OP_ADD;
                        synThirdInfoPara = null;
                        thirdObjId = "";
                    }
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = userFlag + retMsg.getString("error");

                    retMsg.put("code", false);
                    retMsg.put("error", description);
                }


            }else{
                // 同步失败
                if(synThirdInfoEntity!=null){
                    // 修改同步表
                    opType = SynThirdConsts.OBJECT_OP_UPD;
                    synThirdInfoPara = synThirdInfoEntity;
                    thirdObjId = synThirdInfoEntity.getThirdObjId();
                }else{
                    // 写入同步表
                    opType = SynThirdConsts.OBJECT_OP_ADD;
                    synThirdInfoPara = null;
                    thirdObjId = "";
                }

                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = userFlag + "access_token值为空,不能同步信息";

                retMsg.put("code", true);
                retMsg.put("error", description);
            }

        }else{
            // 未设置单条同步,归并到未同步状态
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,userEntity.getId());
            if(synThirdInfoEntity!=null){
                // 修改同步表
                opType = SynThirdConsts.OBJECT_OP_UPD;
                synThirdInfoPara = synThirdInfoEntity;
                thirdObjId = synThirdInfoEntity.getThirdObjId();
            }else{
                // 写入同步表
                opType = SynThirdConsts.OBJECT_OP_ADD;
                synThirdInfoPara = null;
                thirdObjId = "";
            }

            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),userEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }


    /**
     * 往钉钉删除用户
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param id   本系统的公司或部门ID
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject deleteUserSysToDing(boolean isBatch, String id,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER,id);

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            if(isBatch || dingIsSyn==1) {
                // 获取 access_token
                if(isBatch){
                    access_token = accessToken;
                }else{
                    tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
                    access_token = tokenObject.getString("access_token");
                }

                if (access_token != null && !"".equals(access_token)) {
                    // 删除企业对应的用户
                    if (!"".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) && !"null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                        retMsg = SynDingTalkUtil.deleteUser(synThirdInfoEntity.getThirdObjId(), access_token);
                        if (retMsg.getBoolean("code")) {
                            // 同步成功,直接删除同步表记录
                            synThirdInfoService.delete(synThirdInfoEntity);
                        }else{
                            // 同步失败
                            saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
                        }
                    }else{
                        // 根据企业微信ID找不到相应的信息,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }
                }else{
                    // 同步失败
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_FAIL, "access_token值为空,不能同步信息");

                    retMsg.put("code", false);
                    retMsg.put("error", "access_token值为空,不能同步信息！");
                }

            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), id, synThirdInfoEntity.getThirdObjId(), SynThirdConsts.SYN_STATE_NO, "系统未设置同步");

                retMsg.put("code", true);
                retMsg.put("error", "系统未设置单条同步");
            }
        }

        return retMsg;
    }

    /**
     * 往本地创建组织-部门
     * 钉钉同步单个公司或部门到本地(供调用)
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject createDepartmentDingToSys(boolean isBatch, DingTalkDeptModel deptEntity,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();

        Long dingDeptId = deptEntity.getDeptId();
        String dingDeptName = deptEntity.getName();
        Long dingParentId = deptEntity.getParentId();

        Integer synState = 0;
        String deptFlag = "创建：";
        String description = "";

        JSONObject retMsg = new JSONObject();
        boolean isDeptDiff = true;
        String sysParentId = "";
        String sysObjId = "";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "创建：系统未设置单条同步");

        // 支持同步
        if(isBatch || dingIsSyn==1){
            boolean tag = false;
            if(dingDeptId==1L){
                tag=true;
            }
            SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_ORG,dingParentId+"");


            retMsg = checkDepartmentDingToSys(synThirdInfoEntity);
            isDeptDiff = retMsg.getBoolean("code");
            if(isDeptDiff || tag) {
                sysParentId = tag==true?" -1" : synThirdInfoEntity.getSysObjId();

                if(isBatch || dingIsSyn==1) {
                    // 新增保存组织
                    OrganizeEntity newOrg = new OrganizeEntity();
                    sysObjId = RandomUtil.uuId();
                    newOrg.setId(sysObjId);
                    if(!"1".equals(dingDeptId+"") ){
//                        newOrg.setCategory(SynThirdConsts.OBJECT_TYPE_COMPANY);
//                        newOrg.setParentId("-1");
//                        newOrg.setOrganizeIdTree(sysObjId);

                        Assert.notNull(sysParentId,"父级组织未同步");
                        newOrg.setCategory(SynThirdConsts.OBJECT_TYPE_DEPARTMENT);
                        newOrg.setParentId(sysParentId);
                        // 通过组织id获取父级组织
                        String organizeIdTree = organizeService.getOrganizeIdTree(newOrg);
                        newOrg.setOrganizeIdTree(organizeIdTree+","+sysObjId);

                        newOrg.setEnCode(dingDeptId+"");
                        newOrg.setFullName(dingDeptName);
                        newOrg.setSortCode(deptEntity.getOrder()!=null?deptEntity.getOrder():1L);
                        newOrg.setCategory("company".equals(deptEntity.getSourceIdentifier())?"company":"department");
                        organizeService.save(newOrg);
                    }else{
                        sysObjId = organizeService.getOrganizeByParentId("-1").get(0).getId();
                    }

                    // 中间表
                    retMsg.put("retDeptId", sysObjId);
                    synState = SynThirdConsts.SYN_STATE_OK;
                }else{
                    // 未设置单条同步,归并到未同步状态
                    // 未同步
                    synState = SynThirdConsts.SYN_STATE_NO;
                    description = deptFlag + "系统未设置单条同步";

                    retMsg.put("code", true);
                    retMsg.put("error", description);
                    retMsg.put("retDeptId", "0");
                }
            }else{
                // 同步失败,上级部门无对应的钉钉ID
                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = deptFlag + "部门所属的上级部门未同步到本地";

                retMsg.put("code", false);
                retMsg.put("error", description);
                retMsg.put("retDeptId", "0");
            }
        }

        // 更新同步表
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),sysObjId,dingDeptId+"",synState,description);

        return retMsg;
    }

    /**
     * 往钉钉更新组织-部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     */
    @Override
    public JSONObject updateDepartmentDingToSys(boolean isBatch, DingTalkDeptModel deptEntity,String accessToken) {
        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynOrg();

//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
        JSONObject retMsg = new JSONObject();
        DingTalkDeptModel deptModel = new DingTalkDeptModel();
        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
        String opType = "";
        Integer synState = 0;
        String description = "";
//        String thirdObjId = "";
        String sysObjId = "";
        String sysParentId = "";
        SynThirdInfoEntity synThirdInfoPara = new SynThirdInfoEntity();
        boolean isDeptDiff = true;
        String deptFlag = "更新：";

        Long dingDeptId = deptEntity.getDeptId();
        String dingDeptName = deptEntity.getName();
        Long dingParentId = deptEntity.getParentId();
        OrganizeEntity orgInfo = new OrganizeEntity();

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        if(isBatch || dingIsSyn==1) {
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_ORG,dingParentId+"");
            retMsg = checkDepartmentDingToSys(synThirdInfoEntity);
            isDeptDiff = retMsg.getBoolean("code");
            if(isDeptDiff) {
                sysParentId = synThirdInfoEntity.getSysObjId();
                // 获取同步表信息
                synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_ORG,dingDeptId+"");
                // 判断当前部门对应的第三方的合法性
                retMsg = checkDepartmentDingToSys(synThirdInfoEntity);
                if (!retMsg.getBoolean("code")) {
                    if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
                        // flag:3 未同步，需要创建同步到钉钉、写入同步表
                        // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到钉钉、写入同步表
                        if("1".equals(retMsg.getString("flag"))) {
                            synThirdInfoService.delete(synThirdInfoEntity);
                        }
                        opType = SynThirdConsts.OBJECT_OP_ADD;
                        synThirdInfoPara = null;
//                        thirdObjId = dingDeptId+"";

                        // 新增保存组织
                        orgInfo = new OrganizeEntity();
                        sysObjId = RandomUtil.uuId();
                        orgInfo.setId(sysObjId);
                        if(!"1".equals(dingDeptId+"") ){
//                        newOrg.setCategory(SynThirdConsts.OBJECT_TYPE_COMPANY);
//                        newOrg.setParentId("-1");
//                        newOrg.setOrganizeIdTree(sysObjId);

                            orgInfo.setCategory(SynThirdConsts.OBJECT_TYPE_DEPARTMENT);
                            orgInfo.setParentId(sysParentId);
                            // 通过组织id获取父级组织
                            String organizeIdTree = organizeService.getOrganizeIdTree(orgInfo);
                            orgInfo.setOrganizeIdTree(organizeIdTree+","+sysObjId);

                            orgInfo.setEnCode(dingDeptId+"");
                            orgInfo.setFullName(dingDeptName);
                            orgInfo.setSortCode(deptEntity.getOrder()!=null?deptEntity.getOrder():1L);
//                            orgInfo.setCategory("company".equals(deptEntity.getSourceIdentifier())?"company":"department");
                            organizeService.save(orgInfo);
                        }else{
                            sysObjId = organizeService.getOrganizeByParentId("-1").get(0).getId();
                        }


                        // 同步成功
//                        thirdObjId = retMsg.getString("retDeptId");
                        retMsg.put("retDeptId", sysObjId);
                        synState = SynThirdConsts.SYN_STATE_OK;
                        description = "";

                    }

                    if ("2".equals(retMsg.getString("flag"))) {
                        // flag:2 已同步但第三方ID为空，需要创建同步到钉钉、修改同步表
                        opType = SynThirdConsts.OBJECT_OP_UPD;
                        synThirdInfoPara = synThirdInfoEntity;
//                        thirdObjId = dingDeptId+"";

                        // 新增保存组织
                        orgInfo = new OrganizeEntity();
                        sysObjId = RandomUtil.uuId();
                        orgInfo.setId(sysObjId);
                        if(!"1".equals(dingDeptId+"") ){
//                        newOrg.setCategory(SynThirdConsts.OBJECT_TYPE_COMPANY);
//                        newOrg.setParentId("-1");
//                        newOrg.setOrganizeIdTree(sysObjId);

                            orgInfo.setCategory(SynThirdConsts.OBJECT_TYPE_DEPARTMENT);
                            orgInfo.setParentId(sysParentId);
                            // 通过组织id获取父级组织
                            String organizeIdTree = organizeService.getOrganizeIdTree(orgInfo);
                            orgInfo.setOrganizeIdTree(organizeIdTree+","+sysObjId);

                            orgInfo.setEnCode(dingDeptId+"");
                            orgInfo.setFullName(dingDeptName);
                            orgInfo.setSortCode(deptEntity.getOrder()!=null?deptEntity.getOrder():1L);
//                            orgInfo.setCategory("company".equals(deptEntity.getSourceIdentifier())?"company":"department");
                            organizeService.save(orgInfo);
                        }else{
                            sysObjId = organizeService.getOrganizeByParentId("-1").get(0).getId();
                        }


                        // 同步成功
                        retMsg.put("retDeptId", sysObjId);
                        synState = SynThirdConsts.SYN_STATE_OK;
                        description = "";
                    }
                }else {
                    // 更新同步表
                    opType = SynThirdConsts.OBJECT_OP_UPD;
                    synThirdInfoPara = synThirdInfoEntity;
//                    thirdObjId = synThirdInfoEntity.getThirdObjId();
                    sysObjId = synThirdInfoEntity.getSysObjId();

                    orgInfo = organizeService.getInfo(sysObjId);
                    if(orgInfo!=null){
                        orgInfo.setParentId(dingParentId+"");
                        orgInfo.setFullName(dingDeptName);
                        if(!"1".equals(dingDeptId+"")){
//                            orgInfo.setCategory(SynThirdConsts.OBJECT_TYPE_COMPANY);
//                            orgInfo.setParentId("-1");

//                            orgInfo.setCategory(SynThirdConsts.OBJECT_TYPE_DEPARTMENT);
                            orgInfo.setParentId(sysParentId);
                            orgInfo.setOrganizeIdTree(orgInfo.getOrganizeIdTree().replace(","+orgInfo.getId(),""));
                            organizeService.update(orgInfo.getId(),orgInfo);
                        }


                        // 同步成功
                        synState = SynThirdConsts.SYN_STATE_OK;
                        description = "";
                    }else{
                        // 同步失败
                        synState = SynThirdConsts.SYN_STATE_FAIL;
                        description = deptFlag + "未找到对应的部门";
                    }
                }
            }else{
                synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_ORG,dingDeptId+"");
                // 同步失败,上级部门检查有异常
                if(synThirdInfoEntity!=null){
                    // 修改同步表
                    opType = SynThirdConsts.OBJECT_OP_UPD;
                    synThirdInfoPara = synThirdInfoEntity;
                    sysObjId = synThirdInfoEntity.getSysObjId();
                }else{
                    // 写入同步表
                    opType = SynThirdConsts.OBJECT_OP_ADD;
                    synThirdInfoPara = null;
                    sysObjId = "";
                }

                synState = SynThirdConsts.SYN_STATE_FAIL;
                description = deptFlag + "上级部门无对应的本地ID";

                retMsg.put("code", false);
                retMsg.put("error", description);
            }
        }else{
            // 未设置单条同步,归并到未同步状态
            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_ORG,dingDeptId+"");
            if(synThirdInfoEntity!=null){
                // 修改同步表
                opType = SynThirdConsts.OBJECT_OP_UPD;
                synThirdInfoPara = synThirdInfoEntity;
                sysObjId = synThirdInfoEntity.getSysObjId();
            }else{
                // 写入同步表
                opType = SynThirdConsts.OBJECT_OP_ADD;
                synThirdInfoPara = null;
                sysObjId = "";
            }

            synState = SynThirdConsts.SYN_STATE_NO;
            description = deptFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
        }

        // 更新同步表
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),sysObjId,dingDeptId+"",synState,description);

        return retMsg;
    }

    /**
     * 根据部门的同步表信息判断同步情况
     * 不带错第三方误定位判断的功能代码,只获取调用接口的返回信息 20220331
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkDepartmentDingToSys(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            if("".equals(String.valueOf(synThirdInfoEntity.getSysObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getSysObjId()))) {
                // 同步表的钉钉ID为空
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中部门对应的本地ID为空!");
            }
        }else{
            // 上级部门未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","部门未同步到本地!");
        }

        return retMsg;
    }

    @Override
    public JSONObject deleteUserDingToSys(boolean isBatch, String thirdObjId) throws Exception {
        BaseSystemInfo config = getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
        // 单条记录执行时,受开关限制
        int dingIsSyn = isBatch ? 1 : config.getDingSynIsSynUser();
//        JSONObject tokenObject = new JSONObject();
//        String access_token = "";
        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_USER,thirdObjId);
        String sysObjId = "";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "系统未设置单条同步");

        // 支持同步
        if(synThirdInfoEntity!=null) {
            sysObjId = synThirdInfoEntity.getSysObjId();
            if(isBatch || dingIsSyn==1) {
                // 删除企业对应的用户
                if (!"".equals(String.valueOf(sysObjId)) && !"null".equals(String.valueOf(sysObjId))) {
                    // 获取用户信息
                    UserEntity userEntity = userService.getInfo(sysObjId);
                    if(userEntity!=null){
                        // 删除用户,更新为标记为不可登录
                        // 禁用登录
                        userEntity.setEnabledMark(0);
                        userEntity.setDescription("由于钉钉系统删除了该用户");

                        userService.update(userEntity.getId(), userEntity);

                        // 同步成功,直接删除同步表记录
                        synThirdInfoService.delete(synThirdInfoEntity);
                    }else{
                        // 同步失败
                        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), sysObjId, thirdObjId, SynThirdConsts.SYN_STATE_FAIL, retMsg.getString("error"));
                    }
                }else{
                    // 根据企业微信ID找不到相应的信息,直接删除同步表记录
                    synThirdInfoService.delete(synThirdInfoEntity);
                }

            }else{
                // 未设置单条同步，归并到未同步状态
                saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_UPD, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                        Integer.parseInt(SynThirdConsts.DATA_TYPE_USER), sysObjId, thirdObjId, SynThirdConsts.SYN_STATE_NO, "系统未设置同步");

                retMsg.put("code", true);
                retMsg.put("error", "系统未设置单条同步");
            }
        }

        return retMsg;
    }

    @Override
    public JSONObject createUserDingToSys(boolean isBatch, OapiV2UserListResponse.ListUserResponse dingUserModel, String accessToken) throws Exception {

        String dingUserId = dingUserModel.getUserid();
        String dingUserName = dingUserModel.getName();
        String dingMobile = dingUserModel.getMobile();
        String dingTelephone = dingUserModel.getTelephone();
        // 工号不唯一的情况，不能用于做本系统的账号
        String dingJobNumber = dingUserModel.getJobNumber();
        // 职位：是字符串,手入的
        String title = dingUserModel.getTitle();
        String sysObjId= "";
        BaseSystemInfo config = getDingTalkConfig();
        // 单条记录执行时,受开关限制
        int dingIsSyn = config.getDingSynIsSynUser();
        JSONObject retMsg = new JSONObject();
        String thirdObjId = dingUserId;
        Integer synState = 0;
        String description = "";
        String userFlag = "创建：";
        UserEntity userEntity = new UserEntity();
        String tag= SynThirdConsts.OBJECT_OP_ADD;
        if(isBatch || dingIsSyn==1){
            // 检测账户唯一
            UserEntity userAccount = userService.getUserByMobile(dingMobile);
            if (userAccount!=null) {
                // 查询用户id在不在同步表
                sysObjId= userAccount.getId();
                boolean hasExist = synThirdInfoService.getBySysObjId(sysObjId);
                if (hasExist) {
                    // 说明创建过了，直接跳过
                    tag=SynThirdConsts.OBJECT_OP_UPD;
                    synState=1;
                    description="创建过,更新对象";
                    return retMsg;
                }else{
                    retMsg.put("code", true);
                    description = "账户名重复:线上手机账号"+dingMobile+"自动合并为本地账号";
                    synState = SynThirdConsts.SYN_STATE_OK;
                    retMsg.put("msg", description);
                }
            }else{
                // 判断中间表用户组织是否存在
                List<Long> deptIdList = dingUserModel.getDeptIdList();
                List<String> deptIdStrList = deptIdList.stream().map(t->t+"").collect(Collectors.toList());
                QueryWrapper<SynThirdInfoEntity> wrapper = new QueryWrapper<>();
                wrapper.lambda().in(SynThirdInfoEntity::getThirdObjId,deptIdList);
                wrapper.lambda().eq(SynThirdInfoEntity::getThirdType,SynThirdConsts.THIRD_TYPE_DING_To_Sys);
                List<SynThirdInfoEntity> synThirdInfoEntities = synThirdInfoService.getBaseMapper().selectList(wrapper);
                if(synThirdInfoEntities!=null && synThirdInfoEntities.size()!=0){
                    // 返回值初始化
                    retMsg.put("code", true);
                    retMsg.put("error", userFlag + "系统未设置单条同步");
                    userEntity.setId(RandomUtil.uuId());
                    userEntity.setHeadIcon("001.png");
                    userEntity.setAccount(dingMobile);
                    // 工号
                    userEntity.setDingJobNumber(dingUserModel.getJobNumber());
                    userEntity.setEmail(dingUserModel.getEmail());

                    userEntity.setCertificatesNumber(dingUserModel.getJobNumber());
                    userEntity.setMobilePhone(dingMobile);
//                    userEntity.setGender(2);
                    userEntity.setRealName(dingUserName);
                    userEntity.setEnabledMark(1);
                    if(StringUtil.isBlank(userEntity.getOrganizeId())){
                        String orgId = synThirdInfoService.getSysByThird(String.valueOf(deptIdList.get(0)));
                        userEntity.setOrganizeId(orgId);
                    }
                    userEntity.setPassword("4a7d1ed414474e4033ac29ccb8653d9b");

                    List<String> orgIdList = new ArrayList<>();
                    for (String deptIdStr:deptIdStrList){
                        String orgId = synThirdInfoService.getSysByThird(deptIdStr);
                        orgIdList.add(orgId);
                    }
                    userEntity.setOrganizeId(orgIdList.stream().collect(Collectors.joining(",")));
                    userService.create(userEntity);
                    sysObjId = userEntity.getId();

//                    userRelationService.syncDingUserRelation(sysObjId,deptIdList);
                    // 往同步写入本系统与第三方的对应信息
                    if (retMsg.getBoolean("code")) {
                        // 同步成功
                        synState = SynThirdConsts.SYN_STATE_OK;
                    } else {
                        // 同步失败
                        synState = SynThirdConsts.SYN_STATE_FAIL;
                        description = userFlag + retMsg.getString("error");
                    }

                }else{
                    // 无须同步，未同步状态
                    synState = SynThirdConsts.SYN_STATE_NO;
                    description = userFlag + "用户未同步组织信息";
                    retMsg.put("code", false);
                    retMsg.put("error", description);
                }
            }
        }else{
            // 无须同步，未同步状态
            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";
            retMsg.put("code", true);
            retMsg.put("error", description);
        }
        // 更新同步表
        saveSynThirdInfoEntity(tag, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),sysObjId, thirdObjId, synState, description);
        return retMsg;
    }

    @Override
    public JSONObject updateUserDingToSystem(boolean isBatch, OapiV2UserListResponse.ListUserResponse dingUserModel) throws Exception {
        BaseSystemInfo config = getDingTalkConfig();

        JSONObject retMsg = new JSONObject();
        SynThirdInfoEntity synThirdInfoEntity = new SynThirdInfoEntity();
        String opType = "";
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        String userFlag = "更新：";
        // 处理userEntity
        UserEntity userEntity = null;

        // 赋值第三方id
        thirdObjId = dingUserModel.getUserid();
        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", userFlag + "系统未设置单条同步");

        // 单条记录执行时,受开关限制
        int dingIsSyn = config.getDingSynIsSynUser();
        // 支持同步
        if (isBatch || dingIsSyn==1){
            // 获取同步表信息
            /**
             * 获取指定第三方工具、指定数据类型、本地对象ID的同步信息
             * // 获取方式如果第三方用户id和第三方组织id会一致则须修改
             * thirdType 22 钉钉
             * dataType 2 用户
             * thirdId 第三方id
             */
            synThirdInfoEntity = synThirdInfoService.getInfoByThirdObjId(SynThirdConsts.THIRD_TYPE_DING_To_Sys,SynThirdConsts.DATA_TYPE_USER,thirdObjId);

            if(synThirdInfoEntity!=null && StringUtil.isNoneBlank(synThirdInfoEntity.getSysObjId())){
                opType = SynThirdConsts.OBJECT_OP_UPD;
                String sysObjId = synThirdInfoEntity.getSysObjId();

                UserEntity info = userService.getInfo(sysObjId);
                if(info==null){
                    synState = SynThirdConsts.SYN_STATE_NO;
                    description =   "本地更新记录未找到";
                    retMsg.put("code", false);
                    retMsg.put("error", description);
                    synThirdInfoService.removeById(synThirdInfoEntity.getId());


                }else{
                    String dingUserName = dingUserModel.getName();
                    String dingMobile = dingUserModel.getMobile();

                    // 更新系统用户表
                    List<Long> deptIdList = dingUserModel.getDeptIdList();
                    List<String> deptIdStrList = deptIdList.stream().map(t->t+"").collect(Collectors.toList());
                    info.setMobilePhone(dingMobile);
                    info.setAccount(dingMobile);
                    info.setRealName(dingUserName);
                    List<String> orgIdList = new ArrayList<>();
                    for (String deptIdStr:deptIdStrList){
                        String orgId = synThirdInfoService.getSysByThird(deptIdStr);
                        orgIdList.add(orgId);
                    }
                    info.setOrganizeId(orgIdList.stream().collect(Collectors.joining(",")));
                    userService.update(info.getId(), info);

                    // 检测是否未同步用户组织关联
                    //  userRelationService.syncDingUserRelation(info.getId(),deptIdList);

                    synState=1;
                    // 更新同步表记录
                    description="账号同步更新完成";
                    saveSynThirdInfoEntity(opType, synThirdInfoEntity, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),synThirdInfoEntity.getSysObjId(), thirdObjId, synState, description);

                }

            }else{
                if((synThirdInfoEntity!=null && StringUtil.isBlank(synThirdInfoEntity.getSysObjId() ))){
                    // 删除记录
                    synThirdInfoService.removeById(synThirdInfoEntity.getId());
                }
                opType = SynThirdConsts.OBJECT_OP_ADD;
                thirdObjId = synThirdInfoEntity.getThirdObjId();
                try {
                    this.createUserDingToSys(true,dingUserModel,null);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }else{
            // 未设置单条同步,归并到未同步状态
            synState = SynThirdConsts.SYN_STATE_NO;
            description = userFlag + "系统未设置单条同步";

            retMsg.put("code", true);
            retMsg.put("error", description);
            opType= SynThirdConsts.OBJECT_OP_ADD;

            saveSynThirdInfoEntity(opType, null, Integer.parseInt(SynThirdConsts.THIRD_TYPE_DING_To_Sys),
                    Integer.parseInt(SynThirdConsts.DATA_TYPE_USER),null, thirdObjId, synState, description);
        }

        return retMsg;
    }

}
