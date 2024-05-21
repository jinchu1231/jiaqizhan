package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.dingtalk.api.response.OapiV2UserListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.SynThirdInfoEntity;
import jnpf.base.model.synthirdinfo.DingTalkDeptModel;
import jnpf.base.model.synthirdinfo.QyWebChatDeptModel;
import jnpf.base.model.synthirdinfo.QyWebChatUserModel;
import jnpf.base.model.synthirdinfo.SynThirdInfoCrForm;
import jnpf.base.model.synthirdinfo.SynThirdInfoUpForm;
import jnpf.base.model.synthirdinfo.SynThirdTotal;
import jnpf.base.service.SynThirdDingTalkService;
import jnpf.base.service.SynThirdQyService;
import jnpf.base.util.SynDingTalkUtil;
import jnpf.base.util.SynQyWebChatUtil;
import jnpf.base.util.SynThirdConsts;
import jnpf.config.ConfigValueUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.exception.WxErrorException;
import jnpf.message.service.MessageService;
import jnpf.message.service.SynThirdInfoService;
import jnpf.model.BaseSystemInfo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.SynOrganizeDeleteModel;
import jnpf.permission.model.SynOrganizeModel;
import jnpf.permission.model.SynThirdQyModel;
import jnpf.permission.model.SysThirdDeleteModel;
import jnpf.permission.model.organize.OrganizeListVO;
import jnpf.permission.model.organize.OrganizeModel;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/25 9:30
 */
@Tag(name = "第三方信息同步",description = "SynThirdInfo")
@RestController
@RequestMapping("/api/system/SynThirdInfo")
@Slf4j
public class SynThirdInfoController extends SuperController<SynThirdInfoService, SynThirdInfoEntity> {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SynThirdInfoService synThirdInfoService;
    @Autowired
    private SynThirdQyService synThirdQyService;
    @Autowired
    private SynThirdDingTalkService synThirdDingTalkService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Executor executor;
    @Autowired
    private MessageService messageService;



    /**
     * 新增同步表信息
     *
     * @param synThirdInfoCrForm 同步信息模型
     * @return
     */
    @Operation(summary = "新增同步表信息")
    @Parameters({
            @Parameter(name = "synThirdInfoCrForm", description = "同步信息模型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @PostMapping
    @Transactional
    public ActionResult create(@RequestBody @Valid SynThirdInfoCrForm synThirdInfoCrForm) throws DataException {
        UserInfo userInfo = userProvider.get();
        SynThirdInfoEntity entity= JsonUtil.getJsonToBean(synThirdInfoCrForm, SynThirdInfoEntity.class);
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(DateUtil.getNowDate());
        entity.setId(RandomUtil.uuId());
        synThirdInfoService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 获取同步表信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取同步表信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/{id}")
    public SynThirdInfoEntity getInfo(@PathVariable("id") String id){
        SynThirdInfoEntity entity= synThirdInfoService.getInfo(id);
        return entity;
    }

    /**
     * 获取指定类型的同步对象
     * @param thirdType 1:企业微信 2:钉钉
     * @param dataType  1:公司 2:部门 3：用户
     * @param id        dataType对应的对象ID
     * @return
     */
    @NoDataSourceBind
    @GetMapping("/getInfoBySysObjId/{thirdType}/{dataType}/{id}")
    public SynThirdInfoEntity getInfoBySysObjId(@PathVariable("thirdType") String thirdType,@PathVariable("dataType") String dataType,@PathVariable("id") String id, @RequestParam("tenantId") String tenantId) {
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        SynThirdInfoEntity entity= synThirdInfoService.getInfoBySysObjId(thirdType,dataType,id);
        return entity;
    }


    /**
     * 更新同步表信息
     *
     * @param id 主键
     * @param synThirdInfoUpForm 同步模型
     * @return
     */
    @Operation(summary = "更新同步表信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "synThirdInfoUpForm", description = "同步模型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @PutMapping("/{id}")
    @Transactional
    public ActionResult update(@PathVariable("id") String id,@RequestBody @Valid SynThirdInfoUpForm synThirdInfoUpForm) throws DataException {
        SynThirdInfoEntity entity = synThirdInfoService.getInfo(id);
        UserInfo userInfo = userProvider.get();
        if(entity!=null){
            SynThirdInfoEntity entityUpd = JsonUtil.getJsonToBean(synThirdInfoUpForm,SynThirdInfoEntity.class);
            entityUpd.setCreatorUserId(entity.getCreatorUserId());
            entityUpd.setCreatorTime(entity.getCreatorTime());
            entityUpd.setLastModifyUserId(userInfo.getUserId());
            entityUpd.setLastModifyTime(DateUtil.getNowDate());
            synThirdInfoService.update(id,entityUpd);

            return ActionResult.success("更新成功");
        }else{
            return ActionResult.fail("更新失败，数据不存在");
        }
    }


    /**
     * 删除同步表信息
     *
     * @param id
     * @return
     */
    @Operation(summary = "删除同步表信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @DeleteMapping("/{id}")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id){
        SynThirdInfoEntity entity = synThirdInfoService.getInfo(id);
        if(entity!=null){
            synThirdInfoService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }




    /**
     * 获取第三方(如：企业微信、钉钉)的组织与用户同步统计信息
     * @param thirdType 第三方类型(1:企业微信;2:钉钉)
     * @return
     */
    @Operation(summary = "获取第三方(如：企业微信、钉钉)的组织与用户同步统计信息")
    @Parameters({
            @Parameter(name = "thirdType", description = "第三方类型(1:企业微信;2:钉钉)", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/getSynThirdTotal/{thirdType}")
    public ActionResult<List<SynThirdTotal>> getSynThirdTotal(@PathVariable("thirdType") String thirdType){
        List<SynThirdTotal> synTotalList = new ArrayList<>();
        synTotalList.add(synThirdInfoService.getSynTotal(thirdType, SynThirdConsts.DATA_TYPE_ORG));
        synTotalList.add(synThirdInfoService.getSynTotal(thirdType,SynThirdConsts.DATA_TYPE_USER));
        return ActionResult.success(synTotalList);
    }

    /**
     * 获取第三方(如：企业微信、钉钉)的组织或用户同步统计信息
     * @param thirdType 第三方类型(1:企业微信;2:钉钉)
     * @param dataType  数据类型(1:组织(公司与部门);2:用户)
     * @return
     */
    @Operation(summary = "获取第三方(如：企业微信、钉钉)的组织或用户同步统计信息")
    @Parameters({
            @Parameter(name = "thirdType", description = "第三方类型(1:企业微信;2:钉钉)", required = true),
            @Parameter(name = "dataType", description = "数据类型(1:组织(公司与部门);2:用户)", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/getSynThirdTotal/{thirdType}/{dataType}")
    public SynThirdTotal getSynThirdTotal(@PathVariable("thirdType") String thirdType,@PathVariable("dataType") String dataType){
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(thirdType,dataType);
        return synThirdTotal;
    }


    //==================================本系统的公司-部门-用户批量同步到企业微信==================================

    /**
     * 本地所有组织信息(包含公司和部门)同步到企业微信
     * 有带第三方错误定位判断的功能代码 20210604
     * @throws WxErrorException
     */
//    @Operation(summary = "本地所有组织信息(包含公司和部门)同步到企业微信")
//    @GetMapping("/synAllOrganizeSysToQy")
//    public ActionResult synAllOrganizeSysToQy() throws WxErrorException {
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdQyService.getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        List<QyWebChatDeptModel> qyDeptList = new ArrayList<>();
//
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.fail("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取企业微信上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject deptObject = SynQyWebChatUtil.getDepartmentList(SynThirdConsts.QY_ROOT_DEPT_ID, access_token);
//                if (deptObject.getBoolean("code")) {
//                    qyDeptList = JsonUtil.getJsonToList(deptObject.getString("department"), QyWebChatDeptModel.class);
//                } else {
//                    return ActionResult.fail("组织同步失败:获取企业微信的部门信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("组织同步失败:获取企业微信access_token失败");
//            }
//
//            // 获取同步表、部门表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
//            List<OrganizeEntity> organizeList = organizeService.getList();
//
//            // 部门进行树结构化,固化上下层级序列化
//            List<OraganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList, OraganizeModel.class);
//            List<SumTree<OraganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
//            List<OraganizeListVO> listVO = JsonUtil.getJsonToList(trees, OraganizeListVO.class);
//
//            // 转化成为按上下层级顺序排序的列表数据
//            List<OrganizeEntity> listByOrder = new ArrayList<>();
//            for (OraganizeListVO organizeVo : listVO) {
//                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeVo.getId())).findFirst().orElse(null);
//                listByOrder.add(entity);
//                SynQyWebChatUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
//            }
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (organizeList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteDepartmentSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//                if (qyDeptList.stream().filter(t -> t.getId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteDepartmentSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//            }
//
//            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (OrganizeEntity organizeEntity : listByOrder) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
//                    // 执行更新功能
//                    retMsg = synThirdQyService.updateDepartmentSysToQy(true, organizeEntity);
//                } else {
//                    // 执行创建功能
//                    retMsg = synThirdQyService.createDepartmentSysToQy(true, organizeEntity);
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_ORG);
//        return ActionResult.success(synThirdTotal);
//    }


    /**
     * 本地所有用户信息同步到企业微信
     * 有带第三方错误定位判断的功能代码 20210604
     * @throws WxErrorException
     */
//    @Operation(summary = "本地所有用户信息同步到企业微信")
//    @GetMapping("/synAllUserSysToQy")
//    public ActionResult synAllUserSysToQy() throws WxErrorException {
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdQyService.getQyhConfig();
//        String corpId = config.getQyhCorpId();
//        String corpSecret = config.getQyhCorpSecret();
//        List<QyWebChatUserModel> qyUserList = new ArrayList<>();
//
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.fail("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取企业微信上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject userObject = SynQyWebChatUtil.getUserList(SynThirdConsts.QY_ROOT_DEPT_ID, "1", access_token);
//                if (userObject.getBoolean("code")) {
//                    qyUserList = JsonUtil.getJsonToList(userObject.getString("userlist"), QyWebChatUserModel.class);
//                } else {
//                    return ActionResult.fail("用户同步失败:获取企业微信的成员信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("用户同步失败:获取企业微信access_token失败");
//            }
//
//            // 获取同步表、用户表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_USER);
//            List<UserEntity> userList = userService.getList();
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteUserSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//                if (qyUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdQyService.deleteUserSysToQy(true, synThirdInfoEntity.getSysObjId());
//                }
//            }
//
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (UserEntity userEntity : userList) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
//                    // 执行创建功能
//                    retMsg = synThirdQyService.createUserSysToQy(true, userEntity);
//                } else {
//                    // 执行更新功能
//                    retMsg = synThirdQyService.updateUserSysToQy(true, userEntity);
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY,SynThirdConsts.DATA_TYPE_USER);
//        return ActionResult.success(synThirdTotal);
//    }


    /**
     * 本地所有组织信息(包含公司和部门)同步到企业微信
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     *
     * @param type 类型
     * @return ignore
     * @throws WxErrorException ignore
     */
    @Operation(summary = "本地所有组织信息(包含公司和部门)同步到企业微信")
    @Parameters({
            @Parameter(name = "type", description = "类型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllOrganizeSysToQy")
    public ActionResult synAllOrganizeSysToQy(@RequestParam(value = "type", required = false) String type) throws WxErrorException {
        if("1".equals(type)){
            //类型为1走企业微信组织信息同步到本地
            ActionResult  actionResult = this.synAllOrganizeQyToSys();
            return actionResult;
        }
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdQyService.getQyhConfig();
        String corpId = config.getQyhCorpId();
        // 向企业微信插入数据需要另外token（凭证密钥）
        String corpSecret = config.getQyhAgentSecret();
        String access_token = "";
        try {
            // 获取Token值
            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
            if (!tokenObject.getBoolean("code")) {
                return ActionResult.fail("获取企业微信access_token失败");
            }
            access_token = tokenObject.getString("access_token");

            // 获取同步表、部门表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
            Map<String, OrganizeEntity> organizeList = organizeService.getOrgMapsAll();

            // 部门进行树结构化,固化上下层级序列化
            List<OrganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList.values(), OrganizeModel.class);
            List<SumTree<OrganizeModel>> trees = jnpf.util.treeutil.newtreeutil.TreeDotUtils.convertListToTreeDot(organizeModelList);
            List<OrganizeListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeListVO.class);

            // 转化成为按上下层级顺序排序的列表数据
            List<OrganizeEntity> listByOrder = new ArrayList<>();
            for (OrganizeListVO organizeVo : listVO) {
                OrganizeEntity entity = organizeList.get(organizeVo.getId());
                listByOrder.add(entity);
                SynQyWebChatUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
            }

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (organizeList.get(synThirdInfoEntity.getSysObjId()) == null) {
                    //执行删除操作
//                    retMsg = synThirdQyService.deleteDepartmentSysToQy(true, synThirdInfoEntity.getSysObjId(), access_token);
                }
            }

            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (OrganizeEntity organizeEntity : listByOrder) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
                    // 执行更新功能
                    retMsg = synThirdQyService.updateDepartmentSysToQy(true, organizeEntity, access_token);
                } else {
                    // 执行创建功能
                    retMsg = synThirdQyService.createDepartmentSysToQy(true, organizeEntity, access_token);
                }
            }
        } catch (Exception e) {
            ActionResult.fail(e.toString());
        }
        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_ORG);
        return ActionResult.success(synThirdTotal);
    }


    /**
     * 本地所有用户信息同步到企业微信
     * 不带第三方错误定位判断的功能代码,只获取调用接口的返回信息 20210604
     *
     * @param type 类型
     * @return ignore
     * @throws WxErrorException ignore
     */
    @Operation(summary = "本地所有用户信息同步到企业微信")
    @Parameters({
            @Parameter(name = "type", description = "类型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllUserSysToQy")
    public ActionResult synAllUserSysToQy(@RequestParam(value = "type", required = false) String type) throws WxErrorException {
        if("1".equals(type)){
            //类型为1走企业微信用户同步到本地
            ActionResult  actionResult = this.synAllUserQyToSys();
            return actionResult;
        }
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdQyService.getQyhConfig();
        String corpId = config.getQyhCorpId();
        // 向企业微信插入数据需要另外token（凭证密钥）
        String corpSecret = config.getQyhAgentSecret();
        String access_token = "";

        try {
            // 获取Token值
            JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(corpId, corpSecret);
            if (!tokenObject.getBoolean("code")) {
                return ActionResult.fail("获取企业微信access_token失败");
            }
            access_token = tokenObject.getString("access_token");

            // 获取同步表、用户表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_USER);
            List<UserEntity> userList = userService.getList(false);

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
                    //执行删除操作
                    retMsg = synThirdQyService.deleteUserSysToQy(true, synThirdInfoEntity.getSysObjId(), access_token);
                }
            }

            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (UserEntity userEntity : userList) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
                    // 执行创建功能
                    retMsg = synThirdQyService.createUserSysToQy(true, userEntity, access_token);
                } else {
                    // 执行更新功能
                    retMsg = synThirdQyService.updateUserSysToQy(true, userEntity, access_token);
                }
            }
        } catch (Exception e) {
            ActionResult.fail(e.toString());
        }

        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_QY, SynThirdConsts.DATA_TYPE_USER);
        return ActionResult.success(synThirdTotal);
    }


    //==================================本系统的公司-部门-用户批量同步到钉钉==================================

    /**
     * 本地所有组织信息(包含公司和部门)同步到钉钉
     * 有带第三方错误定位判断的功能代码 20210604
     */
//    @Operation(summary = "本地所有组织信息(包含公司和部门)同步到钉钉")
//    @GetMapping("/synAllOrganizeSysToDing")
//    public ActionResult synAllOrganizeSysToDing(){
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.fail("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取钉钉上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                if (deptObject.getBoolean("code")) {
//                    dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                } else {
//                    return ActionResult.fail("组织同步失败:获取钉钉的部门信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("组织同步失败:获取钉钉的access_token失败");
//            }
//
//            // 获取同步表、部门表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
//            List<OrganizeEntity> organizeList = organizeService.getList();
//
//            // 部门进行树结构化,固化上下层级序列化
//            List<OraganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList, OraganizeModel.class);
//            List<SumTree<OraganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
//            List<OraganizeListVO> listVO = JsonUtil.getJsonToList(trees, OraganizeListVO.class);
//
//            // 转化成为按上下层级顺序排序的列表数据
//            List<OrganizeEntity> listByOrder = new ArrayList<>();
//            for (OraganizeListVO organizeVo : listVO) {
//                OrganizeEntity entity = organizeList.stream().filter(t -> t.getId().equals(organizeVo.getId())).findFirst().orElse(null);
//                listByOrder.add(entity);
//                SynDingTalkUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
//            }
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (organizeList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteDepartmentSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList);
//                }
//                if (dingDeptList.stream().filter(t -> t.getDeptId().toString().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteDepartmentSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList);
//                }
//                // 手工清除(到钉钉取需要递归取很慢)
//                if(retMsg.getBoolean("code") && !"".equals(synThirdInfoEntity.getThirdObjId()) && !"null".equals(synThirdInfoEntity.getThirdObjId())) {
//                    List<DingTalkDeptModel> deleteDeptList = dingDeptList.stream().filter(t->t.getDeptId().equals(synThirdInfoEntity.getThirdObjId())).collect(Collectors.toList());
//                    if(deleteDeptList!=null){
//                        dingDeptList.removeAll(deleteDeptList);
//                    }
//                }
//            }
//
//            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (OrganizeEntity organizeEntity : listByOrder) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
//                    // 执行更新功能
//                    retMsg = synThirdDingTalkService.updateDepartmentSysToDing(true, organizeEntity, dingDeptList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        String dingDeptId=retMsg.getString("retDeptId");
//                        if(dingDeptList.stream().filter(t->t.getDeptId().equals(dingDeptId)).count()==0?true:false) {
//                            DingTalkDeptModel dingDept = new DingTalkDeptModel();
//                            dingDept.setDeptId(Long.parseLong(dingDeptId));
//                            dingDeptList.add(dingDept);
//                        }
//                    }
//                } else {
//                    // 执行创建功能
//                    retMsg = synThirdDingTalkService.createDepartmentSysToDing(true, organizeEntity, dingDeptList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        DingTalkDeptModel dingDept = new DingTalkDeptModel();
//                        dingDept.setDeptId(Long.parseLong(retMsg.getString("retDeptId")));
//                        dingDeptList.add(dingDept);
//                    }
//
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_ORG);
//        return ActionResult.success(synThirdTotal);
//    }


    /**
     * 本地所有用户信息同步到钉钉
     * 有带第三方错误定位判断的功能代码 20210604
     */
//    @Operation(summary = "本地所有用户信息同步到钉钉")
//    @GetMapping("/synAllUserSysToDing")
//    public ActionResult synAllUserSysToDing() throws ParseException {
//        JSONObject retMsg = new JSONObject();
//        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
//        String corpId = config.getDingSynAppKey();
//        String corpSecret = config.getDingSynAppSecret();
//        List<DingTalkUserModel> dingUserList = new ArrayList<>();
//        List<DingTalkDeptModel> dingDeptList = new ArrayList<>();
//        try {
//            // 获取Token值
//            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
//            if(!tokenObject.getBoolean("code")){
//                return ActionResult.success("获取企业微信access_token失败");
//            }
//            String access_token = tokenObject.getString("access_token");
//
//            // 获取钉钉上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject deptObject = SynDingTalkUtil.getDepartmentList(SynThirdConsts.DING_ROOT_DEPT_ID, access_token);
//                if (deptObject.getBoolean("code")) {
//                    dingDeptList = JsonUtil.getJsonToList(deptObject.getObject("department", List.class), DingTalkDeptModel.class);
//                } else {
//                    return ActionResult.fail("组织同步失败:获取钉钉的部门信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("组织同步失败:获取钉钉的access_token失败");
//            }
//
//            // 获取钉钉上的所有部门列表信息
//            if (access_token != null && !"".equals(access_token)) {
//                JSONObject userObject = SynDingTalkUtil.getUserList(dingDeptList, access_token);
//                if (userObject.getBoolean("code")) {
//                    dingUserList = JsonUtil.getJsonToList(userObject.getObject("userlist", List.class), DingTalkUserModel.class);
//                } else {
//                    return ActionResult.fail("用户同步失败:获取钉钉的成员信息列表失败");
//                }
//            } else {
//                return ActionResult.fail("用户同步失败:获取钉钉的access_token失败");
//            }
//
//            // 获取同步表、用户表的信息
//            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_USER);
//            List<UserEntity> userList = userService.getList();
//
//            // 根据同步表、公司表进行比较，判断不存的执行删除
//            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
//                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteUserSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList, dingUserList);
//                }
//                if (dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0 ? true : false) {
//                    //执行删除操作
//                    retMsg = synThirdDingTalkService.deleteUserSysToDing(true, synThirdInfoEntity.getSysObjId(), dingDeptList, dingUserList);
//                }
//                // 手工清除(到钉钉取需要递归取很慢)
//                if(retMsg.getBoolean("code") && !"".equals(synThirdInfoEntity.getThirdObjId()) && !"null".equals(synThirdInfoEntity.getThirdObjId())) {
//                    List<DingTalkUserModel> deleteUserList = dingUserList.stream().filter(t->t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).collect(Collectors.toList());
//                    if(deleteUserList!=null){
//                        dingUserList.removeAll(deleteUserList);
//                    }
//                }
//            }
//
//            // 根据公司表、同步表进行比较，决定执行创建、还是更新
//            for (UserEntity userEntity : userList) {
//                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
//                    // 执行创建功能
//                    retMsg = synThirdDingTalkService.createUserSysToDing(true, userEntity, dingDeptList, dingUserList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        DingTalkUserModel dingUser = new DingTalkUserModel();
//                        dingUser.setUserid(userEntity.getId());
//                        dingUser.setMobile(userEntity.getMobilePhone());
//                        dingUser.setEmail(userEntity.getEmail());
//                        dingUserList.add(dingUser);
//                    }
//                } else {
//                    // 执行更新功能
//                    retMsg = synThirdDingTalkService.updateUserSysToDing(true, userEntity, dingDeptList, dingUserList);
//                    // 手工添加(到钉钉取需要递归取很慢)
//                    if(retMsg.getBoolean("code")) {
//                        String dingUserId=userEntity.getId();
//                        if(dingUserList.stream().filter(t->t.getUserid().equals(dingUserId)).count()==0?true:false) {
//                            DingTalkUserModel dingUser = new DingTalkUserModel();
//                            dingUser.setUserid(dingUserId);
//                            dingUser.setMobile(userEntity.getMobilePhone());
//                            dingUser.setEmail(userEntity.getEmail());
//                            dingUserList.add(dingUser);
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
//            ActionResult.fail(e.toString());
//        }
//
//        //获取结果
//        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING,SynThirdConsts.DATA_TYPE_USER);
//        return ActionResult.success(synThirdTotal);
//    }



    /**
     * 本地所有组织信息(包含公司和部门)同步到钉钉
     * 不带第三方错误定位判断的功能代码 20210604
     *
     * @param type 类型
     * @return ignore
     */
    @Operation(summary = "本地所有组织信息(包含公司和部门)同步到钉钉")
    @Parameters({
            @Parameter(name = "type", description = "类型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllOrganizeSysToDing")
    public ActionResult synAllOrganizeSysToDing(@RequestParam(value = "type", required = false) String type) {
        if("1".equals(type)){
            //类型为1走钉钉组织部门信息同步到本地
            ActionResult  actionResult = this.synAllOrganizeDingToSys();
            return actionResult;
        }
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();

        try {
            // 获取Token值
            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
            if (!tokenObject.getBoolean("code")) {
                return ActionResult.fail("获取钉钉的access_token失败");
            }
            String access_token = tokenObject.getString("access_token");

            // 获取同步表、部门表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
            Map<String, OrganizeEntity> organizeList = organizeService.getOrgMapsAll();

            // 部门进行树结构化,固化上下层级序列化
            List<OrganizeModel> organizeModelList = JsonUtil.getJsonToList(organizeList.values(), OrganizeModel.class);
            List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeModelList);
            List<OrganizeListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeListVO.class);

            // 转化成为按上下层级顺序排序的列表数据
            List<OrganizeEntity> listByOrder = new ArrayList<>();
            for (OrganizeListVO organizeVo : listVO) {
                OrganizeEntity entity = organizeList.get(organizeVo.getId());
                listByOrder.add(entity);
                SynDingTalkUtil.getOrganizeTreeToList(organizeVo, organizeList, listByOrder);
            }

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (organizeList.get(synThirdInfoEntity.getSysObjId()) == null) {
                    //执行删除操作
                    retMsg = synThirdDingTalkService.deleteDepartmentSysToDing(true, synThirdInfoEntity.getSysObjId(), access_token);
                }
            }

            synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (OrganizeEntity organizeEntity : listByOrder) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(organizeEntity.getId())).count() > 0 ? true : false) {
                    // 执行更新功能
                    retMsg = synThirdDingTalkService.updateDepartmentSysToDing(true, organizeEntity, access_token);
                } else {
                    // 执行创建功能
                    retMsg = synThirdDingTalkService.createDepartmentSysToDing(true, organizeEntity, access_token);
                }
            }
        } catch (Exception e) {
            ActionResult.fail(e.toString());
        }

        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_ORG);
        return ActionResult.success(synThirdTotal);
    }


    /**
     * 本地所有用户信息同步到钉钉
     * 不带第三方错误定位判断的功能代码 20210604
     *
     * @param type 类型
     * @return ignore
     */
    @Operation(summary = "本地所有用户信息同步到钉钉")
    @Parameters({
            @Parameter(name = "type", description = "类型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllUserSysToDing")
    public ActionResult synAllUserSysToDing(@RequestParam(value = "type", required = false) String type) throws ParseException {
        if("1".equals(type)){
            //类型为1走钉钉用户信息同步到本地
            ActionResult  actionResult = this.synAllUserDingToSys();
            return actionResult;
        }
        JSONObject retMsg = new JSONObject();
        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
        String corpId = config.getDingSynAppKey();
        String corpSecret = config.getDingSynAppSecret();

        try {
            // 获取Token值
            JSONObject tokenObject = SynDingTalkUtil.getAccessToken(corpId, corpSecret);
            if (!tokenObject.getBoolean("code")) {
                return ActionResult.success("获取钉钉的access_token失败");
            }
            String access_token = tokenObject.getString("access_token");

            // 获取同步表、用户表的信息
            List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_USER);
            List<UserEntity> userList = userService.getList(false);

            // 根据同步表、公司表进行比较，判断不存的执行删除
            for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                if (userList.stream().filter(t -> t.getId().equals(synThirdInfoEntity.getSysObjId())).count() == 0 ? true : false) {
                    // 执行删除操作
                    retMsg = synThirdDingTalkService.deleteUserSysToDing(true, synThirdInfoEntity.getSysObjId(), access_token);
                }
            }

            // 根据公司表、同步表进行比较，决定执行创建、还是更新
            for (UserEntity userEntity : userList) {
                if (synThirdInfoList.stream().filter(t -> t.getSysObjId().equals(userEntity.getId())).count() == 0 ? true : false) {
                    // 执行创建功能
                    retMsg = synThirdDingTalkService.createUserSysToDing(true, userEntity, access_token);
                } else {
                    // 执行更新功能
                    retMsg = synThirdDingTalkService.updateUserSysToDing(true, userEntity, access_token);
                }
            }
        } catch (Exception e) {
            ActionResult.fail(e.toString());
        }

        //获取结果
        SynThirdTotal synThirdTotal = synThirdInfoService.getSynTotal(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_USER);
        return ActionResult.success(synThirdTotal);
    }

    //==================================钉钉的公司-部门-用户批量同步到本系统20220330==================================

    /**
     * 钉钉所有组织信息(包含公司和部门)同步到本系统
     * 不带第三方错误定位判断的功能代码 20220330
     *
     * @return ignore
     */
    @Operation(summary = "钉钉所有组织信息(包含公司和部门)同步到本系统")
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllOrganizeDingToSys")
    public ActionResult synAllOrganizeDingToSys() {
        // 设置redis的key
        String synDing = "";
        UserInfo userInfo = userProvider.get();
        if (configValueUtil.isMultiTenancy()) {
            synDing = userInfo.getTenantId() + "_" + userInfo.getUserId() + "_synAllOrganizeDingToSys";
        } else {
            synDing = userInfo.getUserId() + "_synAllOrganizeDingToSys";
        }
        // 如果redis中存在key说明同步正在进行
        if (redisUtil.exists(synDing)) {
            return ActionResult.fail("正在进行同步，请稍后再试");
        }
        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();

        // 获取Token值
        JSONObject tokenObject = SynDingTalkUtil.getAccessToken(config.getDingSynAppKey(), config.getDingSynAppSecret());
        if (!tokenObject.getBoolean("code")) {
            return ActionResult.fail("获取钉钉的access_token失败");
        }

        // 异步执行
        String finalSynDing = synDing;
        executor.execute(() -> {
            String userId = userInfo.getUserId();
            try {
                redisUtil.insert(finalSynDing, "true");

                List<OapiV2DepartmentListsubResponse.DeptBaseResponse> DingDeptList = new ArrayList<>();

                List<DingTalkDeptModel> DingDeptAllList = new ArrayList<>();
                String access_token = tokenObject.getString("access_token");

                // 获取同步表的信息
                List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.syncThirdInfoByType(SynThirdConsts.THIRD_TYPE_DING_To_Sys, SynThirdConsts.DATA_TYPE_ORG, SynThirdConsts.THIRD_TYPE_DING);

                // 获取钉钉上的根目录部门(本系统的组织)
                long departId = SynThirdConsts.DING_ROOT_DEPT_ID;
                if (StringUtil.isNoneBlank(config.getDingDepartment())) {
                    departId = Long.parseLong(config.getDingDepartment());
                }
                synThirdInfoService.initBaseDept(departId, access_token,SynThirdConsts.THIRD_TYPE_DING_To_Sys);

                //  获取钉钉上的部门列表
                JSONObject retMsg = SynDingTalkUtil.getDepartmentList(departId, access_token);
                DingDeptList = (List<OapiV2DepartmentListsubResponse.DeptBaseResponse>) retMsg.get("department");
                List<DingTalkDeptModel> dingDeptListVo = JsonUtil.getJsonToList(DingDeptList, DingTalkDeptModel.class);
                DingDeptAllList.addAll(dingDeptListVo);


                // 根据同步表、公司表进行比较，判断不存的执行删除
                for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                    if (DingDeptAllList.stream().filter(t -> String.valueOf(t.getDeptId()).equals(synThirdInfoEntity.getThirdObjId())).count() == 0) {
//                        // 执行删除操作
//                        retMsg = synThirdDingTalkService.deleteDepartmentDingToSys(true, synThirdInfoEntity.getThirdObjId());
                    }
                }

                // 删除后需要重新获取数据
                synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING_To_Sys, SynThirdConsts.DATA_TYPE_ORG);

                // 根据公司表、同步表进行比较，决定执行创建、还是更新
                for (DingTalkDeptModel dingDeptEntity : DingDeptAllList) {
                    if (synThirdInfoList.stream().filter(t -> t.getThirdObjId().equals(String.valueOf(dingDeptEntity.getDeptId()))).count() > 0) {
                        // 执行本地更新功能
                        synThirdDingTalkService.updateDepartmentDingToSys(true, dingDeptEntity, access_token);
                    } else {
                        // 执行本的创建功能
                        synThirdDingTalkService.createDepartmentDingToSys(true, dingDeptEntity, access_token);
                    }
                }
            } catch (Exception e) {
                log.error(finalSynDing + "，钉钉所有组织信息同步到本系统失败：" + e.getMessage());
            } finally {
                redisUtil.remove(finalSynDing);
                List<String> toUserId = new ArrayList<>(1);
                toUserId.add(userId);
                organizeService.removeOrganizeInfoList();
                messageService.sentMessage(toUserId, "钉钉所有组织信息同步到本系统", "同步完成", 1,1,userInfo);
            }
        });
        return ActionResult.success("正在进行同步,请稍等");
    }


    /**
     * 钉钉所有用户信息同步到本系统
     * 不带第三方错误定位判断的功能代码 20210604
     *
     * @return ignore
     */
    @Operation(summary = "钉钉所有用户信息同步到本系统")
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllUserDingToSys")
    @Transactional
    public ActionResult synAllUserDingToSys() {
        // 设置redis的key
        String synDing = "";
        UserInfo userInfo = userProvider.get();
        if (configValueUtil.isMultiTenancy()) {
            synDing = userInfo.getTenantId() + "_" + userInfo.getUserId() + "_synAllUserDingToSys";
        } else {
            synDing = userInfo.getUserId() + "_synAllUserDingToSys";
        }
        // 如果redis中存在key说明同步正在进行
        if (redisUtil.exists(synDing)) {
            return ActionResult.fail("正在进行同步，请稍后再试");
        }
        // 获取已同步的部门信息
        List<SynThirdInfoEntity> synThirdOrgList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_DING_To_Sys, SynThirdConsts.DATA_TYPE_ORG);
        List<String> dingDeptIdList = new ArrayList<>();
        if (synThirdOrgList != null && synThirdOrgList.size() > 0) {
            dingDeptIdList = synThirdOrgList.stream().map(SynThirdInfoEntity::getThirdObjId).distinct().collect(Collectors.toList());
        } else {
            return ActionResult.fail("请先从钉钉同步部门到本地");
        }

        // 获取Token值
        BaseSystemInfo config = synThirdDingTalkService.getDingTalkConfig();
        JSONObject tokenObject = SynDingTalkUtil.getAccessToken(config.getDingSynAppKey(), config.getDingSynAppSecret());
        if (!tokenObject.getBoolean("code")) {
            return ActionResult.fail("获取钉钉的access_token失败");
        }
        // 异步执行
        List<String> finalDingDeptIdList = dingDeptIdList;
        String finalSynDing = synDing;
        executor.execute(() -> {
            String userId = userInfo.getUserId();
            try {
                redisUtil.insert(finalSynDing, "true");
                List<OapiV2UserListResponse.ListUserResponse> dingUserList = new ArrayList<>();
                String access_token = tokenObject.getString("access_token");

                // 获取钉钉的用户列表
                JSONObject retMsg = SynDingTalkUtil.getUserDingList(finalDingDeptIdList, access_token);
                dingUserList = (List<OapiV2UserListResponse.ListUserResponse>) retMsg.get("userlist");

                // 获取同步表、用户表的信息
                List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.syncThirdInfoByType(SynThirdConsts.THIRD_TYPE_DING_To_Sys, SynThirdConsts.DATA_TYPE_USER, SynThirdConsts.THIRD_TYPE_DING);

                // 根据同步表、公司表进行比较，判断不存的执行删除
                for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                    // 线上不包含中间表的这条记录
                    if (dingUserList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0) {
                        // 执行删除此条中间表记录
                        synThirdDingTalkService.deleteUserDingToSys(true, synThirdInfoEntity.getThirdObjId());
                    }
                }
                // 得到推送钉钉信息
                List<SynThirdInfoEntity> synThirdInfoLists = synThirdInfoService.syncThirdInfoByType(SynThirdConsts.THIRD_TYPE_DING, SynThirdConsts.DATA_TYPE_USER, SynThirdConsts.THIRD_TYPE_DING);
                // 根据公司表、同步表进行比较，决定执行创建、还是更新
                for (OapiV2UserListResponse.ListUserResponse dingUserModel : dingUserList) {
                    if (synThirdInfoList.stream().filter(t -> t.getThirdObjId().equals(dingUserModel.getUserid())).count() == 0
                            && synThirdInfoLists.stream().filter(t -> t.getThirdObjId().equals(dingUserModel.getUserid())).count() == 0) {
                        // 执行创建功能
                        synThirdDingTalkService.createUserDingToSys(true, dingUserModel, access_token);
                    } else {
                        // 执行更新功能
                        synThirdDingTalkService.updateUserDingToSystem(true, dingUserModel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(finalSynDing + "，钉钉所有用户信息同步到本系统失败：" + e.getMessage());
            } finally {
                redisUtil.remove(finalSynDing);
                List<String> toUserId = new ArrayList<>(1);
                toUserId.add(userId);
                messageService.sentMessage(toUserId, "钉钉所有用户信息同步到本系统", "同步完成", 1,1,userInfo);
            }
        });
        return ActionResult.success("正在进行同步,请稍等");
    }

    //==================================企业微信的公司-部门-用户批量同步到本系统20220609==================================

    /**
     * 企业微信所有组织信息(包含公司和部门)同步到本系统
     *
     * @return ignore
     */
    @Operation(summary = "企业微信所有组织信息(包含公司和部门)同步到本系统")
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllOrganizeQyToSys")
    public ActionResult synAllOrganizeQyToSys() {
        // 设置redis的key
        String synDing = "";
        UserInfo userInfo = userProvider.get();
        if (configValueUtil.isMultiTenancy()) {
            synDing = userInfo.getTenantId() + "_" + userInfo.getUserId() + "_synAllOrganizeQyToSys";
        } else {
            synDing = userInfo.getUserId() + "_synAllOrganizeQyToSys";
        }
        // 如果redis中存在key说明同步正在进行
        if (redisUtil.exists(synDing)) {
            return ActionResult.fail("正在进行同步，请稍后再试");
        }
        BaseSystemInfo config = synThirdQyService.getQyhConfig();

        // 获取Token值
        JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(config.getQyhCorpId(), config.getQyhCorpSecret());
        if (!tokenObject.getBoolean("code")) {
            return ActionResult.fail("获取企业微信的access_token失败");
        }

        // 异步执行
        String finalSynDing = synDing;
        executor.execute(() -> {
            String userId = userInfo.getUserId();
            try {
                redisUtil.insert(finalSynDing, "true");
                String access_token = tokenObject.getString("access_token");

                List<OapiV2DepartmentListsubResponse.DeptBaseResponse> DingDeptList = new ArrayList<>();

                List<QyWebChatDeptModel> QyDeptAllList = new ArrayList<>();

                // 获取同步表的信息
                List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.syncThirdInfoByType(SynThirdConsts.THIRD_TYPE_QY_To_Sys, SynThirdConsts.DATA_TYPE_ORG, SynThirdConsts.THIRD_TYPE_QY);

                // 获取企业微信上的根目录部门(本系统的组织)
                String departId = SynThirdConsts.QY_ROOT_DEPT_ID;

                //  获取企业微信上的部门列表
                JSONObject retMsg = SynQyWebChatUtil.getDepartmentList(departId, access_token);
                StringBuilder department = new StringBuilder(retMsg.get("department").toString());
                QyDeptAllList = JsonUtil.getJsonToList(department.toString(),QyWebChatDeptModel.class);


                // 根据同步表、公司表进行比较，判断不存的执行删除
                for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                    if (QyDeptAllList.stream().filter(t -> String.valueOf(t.getParentid()).equals(synThirdInfoEntity.getThirdObjId())).count() == 0) {
//                        // 执行删除操作
//                        retMsg = synThirdDingTalkService.deleteDepartmentDingToSys(true, synThirdInfoEntity.getThirdObjId());
                    }
                }

                // 删除后需要重新获取数据
                synThirdInfoList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY_To_Sys, SynThirdConsts.DATA_TYPE_ORG);

                // 根据公司表、同步表进行比较，决定执行创建、还是更新
                for (QyWebChatDeptModel qyWebChatDeptModel : QyDeptAllList) {
                    if (synThirdInfoList.stream().filter(t -> t.getThirdObjId().equals(String.valueOf(qyWebChatDeptModel.getId()))).count() > 0) {
                        // 执行本地更新功能
                        synThirdQyService.updateDepartmentQyToSys(true, qyWebChatDeptModel, access_token);
                    } else {
                        // 执行本的创建功能
                        synThirdQyService.createDepartmentQyToSys(true, qyWebChatDeptModel, access_token);
                    }
                }
            } catch (Exception e) {
                log.error(finalSynDing + "，企业微信所有组织信息同步到本系统失败：" + e.getMessage());
            } finally {
                redisUtil.remove(finalSynDing);
                List<String> toUserId = new ArrayList<>(1);
                toUserId.add(userId);
                organizeService.removeOrganizeInfoList();
                messageService.sentMessage(toUserId, "企业微信所有组织信息同步到本系统", "同步完成", 1,1,userInfo);
            }
        });
        return ActionResult.success("正在进行同步,请稍等");
    }


    /**
     * 企业微信所有用户信息同步到本系统
     *
     * @return ignore
     */
    @Operation(summary = "企业微信所有用户信息同步到本系统")
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/synAllUserQyToSys")
    @Transactional
    public ActionResult synAllUserQyToSys() {
        // 设置redis的key
        String synDing = "";
        UserInfo userInfo = userProvider.get();
        if (configValueUtil.isMultiTenancy()) {
            synDing = userInfo.getTenantId() + "_" + userInfo.getUserId() + "_synAllUserQyToSys";
        } else {
            synDing = userInfo.getUserId() + "_synAllUserQyToSys";
        }
        // 如果redis中存在key说明同步正在进行
        if (redisUtil.exists(synDing)) {
            return ActionResult.fail("正在进行同步，请稍后再试");
        }
        // 获取已同步的部门信息
        List<SynThirdInfoEntity> synThirdOrgList = synThirdInfoService.getList(SynThirdConsts.THIRD_TYPE_QY_To_Sys, SynThirdConsts.DATA_TYPE_ORG);
        List<String> dingDeptIdList = new ArrayList<>();
        if (synThirdOrgList != null && synThirdOrgList.size() > 0) {
            dingDeptIdList = synThirdOrgList.stream().map(SynThirdInfoEntity::getThirdObjId).distinct().collect(Collectors.toList());
        } else {
            return ActionResult.fail("请先从企业微信同步部门到本地");
        }

        // 获取Token值
        BaseSystemInfo config = synThirdQyService.getQyhConfig();
        JSONObject tokenObject = SynQyWebChatUtil.getAccessToken(config.getQyhCorpId(), config.getQyhCorpSecret());
        if (!tokenObject.getBoolean("code")) {
            return ActionResult.fail("获取企业微信的access_token失败");
        }
        // 异步执行
        List<String> finalDingDeptIdList = dingDeptIdList;
        String finalSynDing = synDing;
        executor.execute(() -> {
            String userId = userInfo.getUserId();
            try {
                redisUtil.insert(finalSynDing, "true");
                List<OapiV2UserListResponse.ListUserResponse> dingUserList = new ArrayList<>();
                List<QyWebChatUserModel> qyUserAllList = new ArrayList<>();
                String access_token = tokenObject.getString("access_token");

                // 获取企业微信的用户列表
                JSONObject retMsg = SynQyWebChatUtil.getUserList("1", "1",access_token);
                StringBuilder department = new StringBuilder(retMsg.get("userlist").toString());
                qyUserAllList = JsonUtil.getJsonToList(JsonUtil.getJsonToListMap((String) retMsg.get("userlist")),QyWebChatUserModel.class);

                // 获取同步表、用户表的信息
                List<SynThirdInfoEntity> synThirdInfoList = synThirdInfoService.syncThirdInfoByType(SynThirdConsts.THIRD_TYPE_QY_To_Sys, SynThirdConsts.DATA_TYPE_USER, SynThirdConsts.THIRD_TYPE_QY);

                // 根据同步表、公司表进行比较，判断不存的执行删除
                for (SynThirdInfoEntity synThirdInfoEntity : synThirdInfoList) {
                    // 线上不包含中间表的这条记录
                    if (qyUserAllList.stream().filter(t -> t.getUserid().equals(synThirdInfoEntity.getThirdObjId())).count() == 0) {
                        // 执行删除此条中间表记录
                        synThirdDingTalkService.deleteUserDingToSys(true, synThirdInfoEntity.getThirdObjId());
                    }
                }
                // 得到企业微信信息
                List<SynThirdInfoEntity> synThirdInfoLists = synThirdInfoService.syncThirdInfoByType(SynThirdConsts.THIRD_TYPE_QY_To_Sys, SynThirdConsts.DATA_TYPE_USER, SynThirdConsts.THIRD_TYPE_QY);
                // 根据公司表、同步表进行比较，决定执行创建、还是更新
                for (QyWebChatUserModel qyWebChatUserModel : qyUserAllList) {
                    if (synThirdInfoList.stream().filter(t -> t.getThirdObjId().equals(qyWebChatUserModel.getUserid())).count() == 0
                            && synThirdInfoLists.stream().filter(t -> t.getThirdObjId().equals(qyWebChatUserModel.getUserid())).count() == 0) {
                        // 执行创建功能
                        synThirdQyService.createUserQyToSys(true, qyWebChatUserModel,access_token);
                    } else {
                        // 执行更新功能
                        synThirdQyService.updateUserQyToSystem(true, qyWebChatUserModel,access_token);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(finalSynDing + "，企业微信所有用户信息同步到本系统失败：" + e.getMessage());
            } finally {
                redisUtil.remove(finalSynDing);
                List<String> toUserId = new ArrayList<>(1);
                toUserId.add(userId);
                messageService.sentMessage(toUserId, "企业微信所有用户信息同步到本系统", "同步完成", 1,1,userInfo);
            }
        });
        return ActionResult.success("正在进行同步,请稍等");
    }

    /**
     * 新建用户后同步用户到企业微信
     * @param synThirdQyModel
     * @return
     */
    @PostMapping("/createUserSysToQy")
    public void createUserSysToQy(@RequestBody SynThirdQyModel synThirdQyModel) {
        try {
            synThirdQyService.createUserSysToQy(synThirdQyModel.getIsBatch(),synThirdQyModel.getUserEntity(),synThirdQyModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新建用户后同步用户到钉钉
     * @param synThirdQyModel
     * @return
     */
    @PostMapping("/createUserSysToDing")
    public void createUserSysToDing(@RequestBody SynThirdQyModel synThirdQyModel) {
        try {
            synThirdDingTalkService.createUserSysToDing(synThirdQyModel.getIsBatch(),synThirdQyModel.getUserEntity(),synThirdQyModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改用户后同步用户到企业微信
     * @param synThirdQyModel
     * @return
     */
    @PostMapping("/updateUserSysToQy")
    public void updateUserSysToQy(@RequestBody SynThirdQyModel synThirdQyModel) {
        try {
            synThirdQyService.updateUserSysToQy(synThirdQyModel.getIsBatch(),synThirdQyModel.getUserEntity(),synThirdQyModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改用户后同步用户到钉钉
     * @param synThirdQyModel
     * @return
     */
    @PostMapping("/updateUserSysToDing")
    public void updateUserSysToDing(@RequestBody SynThirdQyModel synThirdQyModel) {
        try {
            synThirdDingTalkService.updateUserSysToDing(synThirdQyModel.getIsBatch(),synThirdQyModel.getUserEntity(),synThirdQyModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除用户后同步用户到企业微信
     * @param sysThirdDeleteModel
     * @return
     */
    @PostMapping("/deleteUserSysToQy")
    public void deleteUserSysToQy(@RequestBody SysThirdDeleteModel sysThirdDeleteModel) {
        try {
            synThirdQyService.deleteUserSysToQy(sysThirdDeleteModel.getIsBatch(),sysThirdDeleteModel.getUserId(),sysThirdDeleteModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除用户后同步用户到钉钉
     * @param sysThirdDeleteModel
     * @return
     */
    @PostMapping("/deleteUserSysToDing")
    public void deleteUserSysToDing(@RequestBody SysThirdDeleteModel sysThirdDeleteModel) {
        try {
            synThirdDingTalkService.deleteUserSysToDing(sysThirdDeleteModel.getIsBatch(),sysThirdDeleteModel.getUserId(),sysThirdDeleteModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新建组织或部门后同步用户到企业微信
     * @param synOrganizeModel
     * @return
     */
    @PostMapping("/createDepartmentSysToQy")
    public void createDepartmentSysToQy(@RequestBody SynOrganizeModel synOrganizeModel) {
        try {
            synThirdQyService.createDepartmentSysToQy(synOrganizeModel.getIsBatch(),synOrganizeModel.getDeptEntity(),synOrganizeModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新建组织或部门后同步用户到钉钉
     * @param synOrganizeModel
     * @return
     */
    @PostMapping("/createDepartmentSysToDing")
    public void createDepartmentSysToDing(@RequestBody SynOrganizeModel synOrganizeModel) {
        try {
            synThirdDingTalkService.createDepartmentSysToDing(synOrganizeModel.getIsBatch(),synOrganizeModel.getDeptEntity(),synOrganizeModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改组织或部门后同步用户到企业微信
     * @param synOrganizeModel
     * @return
     */
    @PostMapping("/updateDepartmentSysToQy")
    public void updateDepartmentSysToQy(@RequestBody SynOrganizeModel synOrganizeModel) {
        try {
            synThirdQyService.updateDepartmentSysToQy(synOrganizeModel.getIsBatch(),synOrganizeModel.getDeptEntity(),synOrganizeModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改组织或部门后同步用户到钉钉
     * @param synOrganizeModel
     * @return
     */
    @PostMapping("/updateDepartmentSysToDing")
    public void updateDepartmentSysToDing(@RequestBody SynOrganizeModel synOrganizeModel) {
        try {
            synThirdDingTalkService.updateDepartmentSysToDing(synOrganizeModel.getIsBatch(),synOrganizeModel.getDeptEntity(),synOrganizeModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除组织或部门后同步用户到企业微信
     * @param synOrganizeDeleteModel
     * @return
     */
    @PostMapping("/deleteDepartmentSysToQy")
    public void deleteDepartmentSysToQy(@RequestBody SynOrganizeDeleteModel synOrganizeDeleteModel) {
        try {
            synThirdQyService.deleteDepartmentSysToQy(synOrganizeDeleteModel.getIsBatch(),synOrganizeDeleteModel.getOrganizeId(),synOrganizeDeleteModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除组织或部门后同步用户到钉钉
     * @param synOrganizeDeleteModel
     * @return
     */
    @PostMapping("/deleteDepartmentSysToDing")
    public void deleteDepartmentSysToDing(@RequestBody SynOrganizeDeleteModel synOrganizeDeleteModel) {
        try {
            synThirdDingTalkService.deleteDepartmentSysToDing(synOrganizeDeleteModel.getIsBatch(),synOrganizeDeleteModel.getOrganizeId(),synOrganizeDeleteModel.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
