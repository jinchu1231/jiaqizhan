package jnpf.message.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse;
import jnpf.base.UserInfo;
import jnpf.base.entity.SynThirdInfoEntity;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.mapper.SynThirdInfoMapper;
import jnpf.base.model.synthirdinfo.DingTalkDeptModel;
import jnpf.base.model.synthirdinfo.SynThirdTotal;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.SynThirdDingTalkService;
import jnpf.base.service.SysconfigService;
import jnpf.base.util.SynDingTalkUtil;
import jnpf.base.util.SynQyWebChatUtil;
import jnpf.base.util.SynThirdConsts;
import jnpf.exception.WxErrorException;
import jnpf.message.service.SynThirdInfoService;
import jnpf.model.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/23 17:33
 */
@Service
public class SynThirdInfoServiceImpl extends SuperServiceImpl<SynThirdInfoMapper, SynThirdInfoEntity> implements SynThirdInfoService {
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserService userService;
    @Autowired
    SynThirdDingTalkService synThirdDingTalkService;

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;


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

    @Override
    public List<SynThirdInfoEntity> getList(String thirdType, String dataType) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getThirdType, Integer.valueOf(thirdType)));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getDataType, Integer.valueOf(dataType)));
        queryWrapper.lambda().orderByAsc(SynThirdInfoEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public SynThirdInfoEntity getInfo(String id) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SynThirdInfoEntity::getId,id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(SynThirdInfoEntity entity) {
        this.save(entity);
    }

    @Override
    public boolean update(String id, SynThirdInfoEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(SynThirdInfoEntity entity) {
        if(entity!=null){
            this.removeById(entity.getId());
        }
    }

    @Override
    public SynThirdInfoEntity getInfoBySysObjId(String thirdType,String dataType,String id) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getThirdType,thirdType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getDataType,dataType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getSysObjId,id));
        return this.getOne(queryWrapper);
    }

    @Override
    public SynThirdTotal getSynTotal(String thirdType, String dataType) {
        String synType = dataType.equals(SynThirdConsts.DATA_TYPE_ORG) ? "组织" : "用户";
        Integer recordTotal = 0;
        Long synSuccessCount = 0L;
        Long synFailCount = 0L;
        Long unSynCount = 0L;
        Date synDate = null;

        // 获取列表数据
        List<SynThirdInfoEntity> synList = getList(thirdType, dataType).stream().filter(t->t.getLastModifyTime() != null).collect(Collectors.toList());
        if(synList!=null && synList.size()>0){
            synSuccessCount = synList.stream().filter(t -> t.getEnabledMark().equals(SynThirdConsts.SYN_STATE_OK)).count();
            synFailCount = synList.stream().filter(t -> t.getEnabledMark().equals(SynThirdConsts.SYN_STATE_FAIL)).count();
            unSynCount = synList.stream().filter(t -> t.getEnabledMark().equals(SynThirdConsts.SYN_STATE_NO)).count();
            synDate = synList.stream().max(Comparator.comparing(u -> u.getLastModifyTime())).isPresent() ?
                    synList.stream().max(Comparator.comparing(u -> u.getLastModifyTime())).get().getLastModifyTime() :
                    null;
        }

        // 获取本系统的组织、用户表的记录数
        if(dataType.equals(SynThirdConsts.DATA_TYPE_ORG)){
            // 获取组织(公司和部门)的记录数
            recordTotal = organizeService.getList(false).size();
        }else{
            // 获取用户的记录数
            recordTotal = userService.getList(false).size();
        }

        // 写入同步统计模型对象
        SynThirdTotal synThirdTotal = new SynThirdTotal();
        synThirdTotal.setSynType(synType);
        synThirdTotal.setRecordTotal(recordTotal);
        synThirdTotal.setSynSuccessCount(synSuccessCount);
        synThirdTotal.setSynFailCount(synFailCount);
        synThirdTotal.setUnSynCount(unSynCount);
        synThirdTotal.setSynDate(synDate);

        return synThirdTotal;
    }

    @Override
    public List<SynThirdInfoEntity> syncThirdInfoByType(String thirdToSysType, String dataTypeOrg, String SysToThirdType) {

        HashMap<String,String> typeMap = new HashMap<>();
        typeMap.put(SysToThirdType,thirdToSysType);
        typeMap.put(thirdToSysType,SysToThirdType);

        List<SynThirdInfoEntity> synThirdInfoList = this.getList(thirdToSysType, dataTypeOrg);
        List<SynThirdInfoEntity> synThirdInfoDingList = this.getList( typeMap.get(thirdToSysType), dataTypeOrg);


        List<String> collectSource = synThirdInfoList.stream().filter(t -> StringUtil.isBlank(t.getThirdObjId()) || StringUtil.isBlank(t.getSysObjId())).map(t->t.getId()).collect(Collectors.toList());
        List<String> collectTarget = synThirdInfoDingList.stream().filter(t -> StringUtil.isBlank(t.getThirdObjId()) || StringUtil.isBlank(t.getSysObjId())).map(t->t.getId()).collect(Collectors.toList());
        List<String> deleteList = new ArrayList<>();
        deleteList.addAll(collectSource);
        deleteList.addAll(collectTarget);
//        List<String> fails = this.selectAllFail();
//        deleteList.addAll(fails);
        if(!deleteList.isEmpty()){

            this.getBaseMapper().deleteBatchIds(deleteList);
        }


        synThirdInfoList = this.getList(thirdToSysType, dataTypeOrg);
        synThirdInfoDingList = this.getList( typeMap.get(thirdToSysType), dataTypeOrg);
        // 记录已经存在的组合
        HashMap<String,Boolean> existingMap =  new HashMap<>();
        synThirdInfoList.forEach(k->{
            String tag = k.getThirdType() + "-" + k.getDataType() + "-" +k.getSysObjId() +"-"+k.getThirdObjId();
            existingMap.put(tag,true);
        });
        synThirdInfoDingList.forEach(k->{
            String tag = k.getThirdType() + "-" + k.getDataType() + "-" +k.getSysObjId() +"-"+k.getThirdObjId();
            existingMap.put(tag,true);
        });


        HashMap<String, SynThirdInfoEntity> mapSource = new HashMap<>();
        HashMap<String, SynThirdInfoEntity> mapTarget = new HashMap<>();
        String tag = "";
        for(SynThirdInfoEntity entity :synThirdInfoList){
//            if(collectSource.size()>0 && !collectSource.contains(entity.getId())){
            tag =entity.getSysObjId() +"-" + entity.getThirdObjId();
            mapSource.put(tag,entity);
//            }
        }
        for(SynThirdInfoEntity entity :synThirdInfoDingList){
//            if(collectTarget.size()>0 && !collectTarget.contains(entity.getId())){
            tag =entity.getSysObjId() +"-" + entity.getThirdObjId();
            mapTarget.put(tag,entity);
//            }
        }

        // 同步记录
        List<SynThirdInfoEntity> synThirdInfoAddList = new ArrayList<>();
        SynThirdInfoEntity addEntity = null;
        if(mapSource.size()==0 && mapTarget.size()==0){
            return new ArrayList<>();
        }else if (mapSource.size()>0 && mapTarget.size()==0){
            for(String key : mapSource.keySet()){
                SynThirdInfoEntity synThirdInfoEntity = mapSource.get(key);
                addEntity = JsonUtil.getJsonToBean(synThirdInfoEntity,SynThirdInfoEntity.class);
                addEntity.setId(RandomUtil.uuId());
                addEntity.setThirdType(Integer.valueOf(typeMap.get(thirdToSysType)));
                synThirdInfoAddList.add(addEntity);
            }

        }else if (mapSource.size()==0 && mapTarget.size()>0){
            for(String key : mapTarget.keySet()){
                SynThirdInfoEntity synThirdInfoEntity = mapTarget.get(key);
                addEntity = JsonUtil.getJsonToBean(synThirdInfoEntity,SynThirdInfoEntity.class);
                addEntity.setId(RandomUtil.uuId());
                addEntity.setThirdType(Integer.valueOf(thirdToSysType));
                synThirdInfoAddList.add(addEntity);
            }
        }else{
            for(String key : mapSource.keySet()){
                if(!mapTarget.containsKey(key)){
                    SynThirdInfoEntity synThirdInfoEntity = mapSource.get(key);
                    addEntity = JsonUtil.getJsonToBean(synThirdInfoEntity,SynThirdInfoEntity.class);
                    addEntity.setId(RandomUtil.uuId());
                    addEntity.setThirdType(Integer.valueOf(typeMap.get(thirdToSysType)));
                    synThirdInfoAddList.add(addEntity);
                }
            }
            for(String key : mapTarget.keySet()){
                if(!mapSource.containsKey(key)){
                    SynThirdInfoEntity synThirdInfoEntity = mapTarget.get(key);
                    addEntity = JsonUtil.getJsonToBean(synThirdInfoEntity,SynThirdInfoEntity.class);
                    addEntity.setId(RandomUtil.uuId());
                    addEntity.setThirdType(Integer.valueOf(thirdToSysType));
                    synThirdInfoAddList.add(addEntity);
                }
            }

        }

        ArrayList<SynThirdInfoEntity> addList = new ArrayList<>();
        if(synThirdInfoAddList.size() > 0 ){
            // 过滤
            synThirdInfoAddList.forEach(k->{
                String addTag = k.getThirdType() + "-" + k.getDataType() + "-" +k.getSysObjId() +"-"+k.getThirdObjId();
                if (existingMap.get(addTag)==null) {
                    addList.add(k);
                }
            });
            this.saveBatch(addList);
        }
        // 查找对应的数据
        synThirdInfoList = this.getList(thirdToSysType, dataTypeOrg);
        return synThirdInfoList;
    }

    private List<String> selectAllFail() {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getEnabledMark,"2"));
        List<SynThirdInfoEntity> lists = this.getBaseMapper().selectList(queryWrapper);
        return lists.stream().map(t->t.getId()).collect(Collectors.toList());
    }

    @Override
    public void initBaseDept(Long dingRootDeptId, String access_token, String thirdType) {
        final String sysByThird = this.getSysByThird("1");
        // 判断是否在中间表存在
        JSONObject retMsg = new JSONObject();

        if(StringUtil.isBlank(sysByThird)){
            if("22".equals(thirdType)){
                retMsg = SynDingTalkUtil.getDepartmentInfo(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
                OapiV2DepartmentGetResponse.DeptGetResponse departmentInfo = (OapiV2DepartmentGetResponse.DeptGetResponse) retMsg.get("departmentInfo");
                DingTalkDeptModel model = JsonUtil.getJsonToBean(departmentInfo, DingTalkDeptModel.class);
                retMsg = synThirdDingTalkService.createDepartmentDingToSys(true, model, access_token);
            }
//            if("11".equals(thirdType)){
//                retMsg = SynDingTalkUtil.getDepartmentInfo(SynThirdConsts.QY_ROOT_DEPT_ID, access_token);
//                OapiV2DepartmentGetResponse.DeptGetResponse departmentInfo = (OapiV2DepartmentGetResponse.DeptGetResponse) retMsg.get("departmentInfo");
//                DingTalkDeptModel model = JsonUtil.getJsonToBean(departmentInfo, DingTalkDeptModel.class);
//                retMsg = synThirdDingTalkService.createDepartmentDingToSys(true, model, access_token);
//            }
        }
    }

    @Override
    public boolean getBySysObjId(String id) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SynThirdInfoEntity::getEnabledMark,"1");
        queryWrapper.lambda().eq(SynThirdInfoEntity::getSysObjId,id);
        List<SynThirdInfoEntity> list = this.getBaseMapper().selectList(queryWrapper);
        if(list!=null && list.size() > 0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public String getSysByThird(String valueOf) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().isNotNull(SynThirdInfoEntity::getSysObjId);
        queryWrapper.lambda().eq(SynThirdInfoEntity::getThirdObjId,valueOf);
        List<SynThirdInfoEntity> list = this.getBaseMapper().selectList(queryWrapper);
        if(list!=null && !list.isEmpty()){
            return list.get(0).getSysObjId();
        }
        return null;
    }

    @Override
    public SynThirdInfoEntity getInfoByThirdObjId(String thirdType,String dataType,String thirdObjId) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getThirdType,thirdType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getDataType,dataType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getThirdObjId,thirdObjId));
        return this.getOne(queryWrapper);
    }
    /**
     * 获取企业微信的配置信息
     * @return
     */
    @Override
    public BaseSystemInfo getQyhConfig() {
        Map<String, String> objModel = new HashMap<>();
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        BaseSystemInfo baseSystemInfo = JsonUtil.getJsonToBean(objModel, BaseSystemInfo.class);
        return baseSystemInfo;
    }

    /**
     * 根据部门的同步表信息判断同步情况
     * 不带第三方错误定位判断的功能代码 20210604
     * @param synThirdInfoEntity
     * @return
     */
    public JSONObject checkDepartmentSysToQy(SynThirdInfoEntity synThirdInfoEntity) {
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("flag","");
        retMsg.put("error","");

        if(synThirdInfoEntity!=null){
            // 同步表的企业微信ID为空
            if("".equals(String.valueOf(synThirdInfoEntity.getThirdObjId())) || "null".equals(String.valueOf(synThirdInfoEntity.getThirdObjId()))) {
                retMsg.put("code",false);
                retMsg.put("flag","2");
                retMsg.put("error","同步表中部门对应的企业微信ID为空!");
            }

        }else{
            // 上级部门未同步
            retMsg.put("code",false);
            retMsg.put("flag","3");
            retMsg.put("error","部门未同步到企业微信!");
        }

        return retMsg;
    }

    /**
     * 检查部门中文名称与英文名称是否相同
     * @param cnName
     * @param EnName
     * @param opType
     * @param synThirdInfoEntity
     * @param thirdType
     * @param dataType
     * @param sysObjId
     * @param thirdObjId
     * @param deptFlag
     * @return
     */
    public JSONObject checkCnEnName(String cnName, String EnName,
                                    String opType, SynThirdInfoEntity synThirdInfoEntity, Integer thirdType,
                                    Integer dataType, String sysObjId, String thirdObjId, String deptFlag){
        JSONObject retMsg = new JSONObject();
        retMsg.put("code",true);
        retMsg.put("error","");
        if(cnName.equals(EnName)){
            // 同步失败
            Integer synState = SynThirdConsts.SYN_STATE_FAIL;
            String description = deptFlag + "部门中文名称与英文名称不能相同";

            // 更新同步表
            saveSynThirdInfoEntity(opType,synThirdInfoEntity,thirdType,dataType,sysObjId,thirdObjId,synState,description);

            retMsg.put("code", false);
            retMsg.put("error", description);
        }
        return retMsg;
    }

    /**
     * 往企业微信更新部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject updateDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity, String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        // 向企业微信插入数据需要另外token（凭证密钥）
        String corpSecret = config.getQyhAgentSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        JSONObject object = new JSONObject();
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

        // 支持同步,设置需要同步到企业微信的对象属性值
        if(isBatch || qyhIsSyn==1) {
            if(isBatch){
                access_token = accessToken;
            }else{
                // 获取 access_token
                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            // 获取同步表信息
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

            if (access_token != null && !"".equals(access_token)) {
                object.put("id", null);
                object.put("name", deptEntity.getFullName());
                object.put("name_en", deptEntity.getEnCode());
                // 从本地数据库的同步表获取对应的企业微信ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，企业微信的父节点设置为1
                    object.put("parentid", 1);
                } else {
                    // 判断上级部门的合法性
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());
                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if (isDeptDiff) {
                        object.put("parentid", synThirdInfoEntity.getThirdObjId());
                    }
                }
                object.put("order", deptEntity.getSortCode());

                // 上级部门检查是否异常
                if(isDeptDiff){
                    // 获取同步表信息
                    synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());

                    // 判断当前部门对应的第三方的合法性
                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
                    if (!retMsg.getBoolean("code")) {
                        if ("3".equals(retMsg.getString("flag")) || "1".equals(retMsg.getString("flag"))) {
                            // flag:3 未同步，需要创建同步到企业微信、写入同步表
                            // flag:1 已同步但第三方上没对应的ID，需要删除原来的同步信息，再创建同步到企业微信、写入同步表
                            if("1".equals(retMsg.getString("flag"))) {
                                synThirdInfoService.delete(synThirdInfoEntity);
                            }
                            opType = SynThirdConsts.OBJECT_OP_ADD;
                            synThirdInfoPara = null;
                            thirdObjId = "";

                            // 部门中文名称与英文名称不能相同
                            retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往企业微信写入公司或部门
                            retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);

                            // 往同步写入本系统与第三方的对应信息
                            if(retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
                                synState = SynThirdConsts.SYN_STATE_OK;
                                description = "";
                            }else{
                                // 同步失败
                                synState = SynThirdConsts.SYN_STATE_FAIL;
                                description = deptFlag + retMsg.getString("error");
                            }
                        }

                        if ("2".equals(retMsg.getString("flag"))) {
                            // flag:2 已同步但第三方ID为空，需要创建同步到企业微信、修改同步表
                            opType = SynThirdConsts.OBJECT_OP_UPD;
                            synThirdInfoPara = synThirdInfoEntity;
                            thirdObjId = "";

                            // 部门中文名称与英文名称不能相同
                            retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
                                    opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                    Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                            if (!retMsg.getBoolean("code")) {
                                return retMsg;
                            }

                            // 往企业微信写入公司或部门
                            retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);

                            // 往同步表更新本系统与第三方的对应信息
                            if (retMsg.getBoolean("code")) {
                                // 同步成功
                                thirdObjId = retMsg.getString("retDeptId");
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

                        // 部门中文名称与英文名称不能相同
                        retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),
                                opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                        if (!retMsg.getBoolean("code")) {
                            return retMsg;
                        }

                        // 往企业微信写入公司或部门
                        object.put("id", synThirdInfoEntity.getThirdObjId());
                        retMsg = SynQyWebChatUtil.updateDepartment(object.toJSONString(), access_token);

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
                    description = deptFlag + "上级部门无对应的企业微信ID";

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
            synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getId());
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
        saveSynThirdInfoEntity(opType,synThirdInfoPara,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }

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
     * 往企业微信创建部门
     * 不带错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     * @param isBatch   是否批量(批量不受开关限制)
     * @param deptEntity
     * @param accessToken (单条调用时为空)
     * @return
     * @throws WxErrorException
     */
    @Override
    public JSONObject createDepartmentSysToQy(boolean isBatch, OrganizeEntity deptEntity,String accessToken) throws WxErrorException {
        BaseSystemInfo config = getQyhConfig();
        String corpId = config.getQyhCorpId();
        // 向企业微信插入数据需要另外token（凭证密钥）
        String corpSecret = config.getQyhAgentSecret();
        String compValue = SynThirdConsts.OBJECT_TYPE_COMPANY;
        // 单条记录执行时,受开关限制
        int qyhIsSyn = isBatch ? 1 : config.getQyhIsSynOrg();
        JSONObject tokenObject = new JSONObject();
        String access_token = "";
        JSONObject retMsg = new JSONObject();
        JSONObject object = new JSONObject();
        String thirdObjId = "";
        Integer synState = 0;
        String description = "";
        boolean isDeptDiff = true;
        String deptFlag = "创建：";

        // 返回值初始化
        retMsg.put("code", true);
        retMsg.put("error", "创建：系统未设置单条同步");

        // 支持同步
        if(isBatch || qyhIsSyn==1){
            if(isBatch){
                access_token = accessToken;
            }else{
                // 获取 access_token 值
                tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
                access_token = tokenObject.getString("access_token");
            }

            if (access_token != null && !"".equals(access_token)) {
                object.put("id", null);
                // name:必填项,同一个层级的部门名称不能重复
                // name_en:必填项,同一个层级的部门名称不能重复
                // name与name_en的值不能相同，否则会报错, 20210429
                object.put("name", deptEntity.getFullName());
                object.put("name_en", deptEntity.getEnCode());
                // 从本地数据库的同步表获取对应的企业微信ID，为空报异常，不为空再验证所获取接口部门列表是否当前ID 未处理
                if(compValue.equals(deptEntity.getCategory()) && "-1".equals(deptEntity.getParentId())){
                    //顶级节点时，企业微信的父节点设置为1
                    thirdObjId = "1";
                    synState = 1;
                    saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                            Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);
                    return null;
                }else{
                    SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG,deptEntity.getParentId());

                    retMsg = checkDepartmentSysToQy(synThirdInfoEntity);
                    isDeptDiff = retMsg.getBoolean("code");
                    if(isDeptDiff) {
                        object.put("parentid", synThirdInfoEntity.getThirdObjId());
                    }
                }
                object.put("order", deptEntity.getSortCode());

                // 创建时：部门中文名称与英文名称不能相同
                retMsg = checkCnEnName(object.getString("name"),object.getString("name_en"),SynThirdConsts.OBJECT_OP_ADD,null,
                        Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,deptFlag);
                if (!retMsg.getBoolean("code")) {
                    return retMsg;
                }

                if(isDeptDiff) {
                    if(qyhIsSyn==1) {
                        // 往企业微信写入公司或部门
                        retMsg = SynQyWebChatUtil.createDepartment(object.toJSONString(), access_token);

                        // 往同步写入本系统与第三方的对应信息
                        if (retMsg.getBoolean("code")) {
                            // 同步成功
                            thirdObjId = retMsg.getString("retDeptId");
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
                    // 同步失败,上级部门无对应的企业微信ID
                    synState = SynThirdConsts.SYN_STATE_FAIL;
                    description = deptFlag + "部门所属的上级部门未同步到企业微信";

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
        saveSynThirdInfoEntity(SynThirdConsts.OBJECT_OP_ADD,null,Integer.parseInt(SynThirdConsts.THIRD_TYPE_QY),
                Integer.parseInt(SynThirdConsts.DATA_TYPE_ORG),deptEntity.getId(),thirdObjId,synState,description);

        return retMsg;
    }
}
