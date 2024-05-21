package jnpf.permission.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.model.base.SystemBaeModel;
import jnpf.base.model.base.SystemServiceByIdsModel;
import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.form.ModuleFormModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.model.resource.ResourceModel;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.SysconfigService;
import jnpf.base.service.SystemService;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.entity.LogEntity;
import jnpf.model.PaginationLogModel;
import jnpf.permission.constant.AuthorizeConst;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PermissionEntityBase;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.SignEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserOldPasswordEntity;
import jnpf.permission.model.authorize.AuthorizeModel;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.model.permission.PermissionModel;
import jnpf.permission.model.sign.SignForm;
import jnpf.permission.model.sign.SignListVO;
import jnpf.permission.model.user.UserAppDataForm;
import jnpf.permission.model.user.UserAuthorizeModel;
import jnpf.permission.model.user.UserAuthorizeVO;
import jnpf.permission.model.user.UserBaseInfoVO;
import jnpf.permission.model.user.UserInfoForm;
import jnpf.permission.model.user.UserLanguageForm;
import jnpf.permission.model.user.UserLogVO;
import jnpf.permission.model.user.UserModifyPasswordForm;
import jnpf.permission.model.user.UserSettingForm;
import jnpf.permission.model.user.UserSubordinateVO;
import jnpf.permission.model.user.UserThemeForm;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PermissionGroupService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.SignService;
import jnpf.permission.service.UserOldPasswordService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.permission.util.PermissionUtil;
import jnpf.portal.model.PortalModel;
import jnpf.service.LogService;
import jnpf.util.CacheKeyUtil;
import jnpf.util.IpUtil;
import jnpf.util.JsonUtil;
import jnpf.util.Md5Util;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人资料
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "个人资料", description = "CurrentUsersInfo")
@RestController
@RequestMapping("/api/permission/Users/Current")
@Slf4j
public class UserSettingController {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private UserOldPasswordService userOldPasswordService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private LogService logService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private SysconfigService sysConfigService;
    @Autowired
    private SignService signService;
    @Autowired
    private PermissionGroupService permissionGroupService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private SystemService systemService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    /**
     * 我的信息
     *
     * @return
     */
    @Operation(summary = "个人资料")
    @GetMapping("/BaseInfo")
    public ActionResult<UserBaseInfoVO> get() {
        UserInfo userInfo = userProvider.get();
        UserEntity userEntity = userService.getInfo(userInfo.getUserId());

        String catchKey = cacheKeyUtil.getAllUser();
        if (redisUtil.exists(catchKey)) {
            redisUtil.remove(catchKey);
        }

        UserBaseInfoVO vo = JsonUtil.getJsonToBean(userEntity, UserBaseInfoVO.class);

        List<DictionaryDataEntity> dataServiceList4 = dictionaryDataService.getListByTypeDataCode("sex");
        Map<String, String> dataServiceMap4 = dataServiceList4.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getEnCode, DictionaryDataEntity::getFullName));
        vo.setGender(dataServiceMap4.get(vo.getGender()));

        if (StringUtil.isNotEmpty(userEntity.getManagerId())) {
            UserEntity menager = userService.getInfo(userEntity.getManagerId());
            vo.setManager(menager != null ? menager.getRealName() + "/" + menager.getAccount() : "");
        }

        //设置语言和主题
        vo.setLanguage(userEntity.getLanguage() != null ? userEntity.getLanguage() : "zh-CN");
        vo.setTheme(userEntity.getTheme() != null ? userEntity.getTheme() : "W-001");

        // 获取组织
        vo.setOrganize(PermissionUtil.getLinkInfoByOrgId(userInfo.getOrganizeId(), organizeService, false));

        // 获取角色
        if (StringUtil.isNotEmpty(userInfo.getOrganizeId())) {
            vo.setRoleId(roleService.getCurRolesByOrgId(userInfo.getOrganizeId()).stream()
                    .map(PermissionEntityBase::getFullName).collect(Collectors.joining(",")));
        }

        // 获取主要岗位
        List<PositionEntity> positionEntityList = positionService.getListByOrgIdAndUserId(userInfo.getOrganizeId(), userEntity.getId());
        if (positionEntityList.size() > 0) {
            List<String> fullNames = positionEntityList.stream().map(PositionEntity::getFullName).collect(Collectors.toList());
            vo.setPosition(String.join(",", fullNames));
        }

        // 获取用户
        if (StringUtil.isNotEmpty(userInfo.getTenantId())) {
            vo.setAccount(userInfo.getTenantId() + "@" + vo.getAccount());
        }

        // 获取用户头像
        if (!StringUtil.isEmpty(userInfo.getUserIcon())) {
            vo.setAvatar(UploaderUtil.uploaderImg(userInfo.getUserIcon()));
        }
        vo.setBirthday(userEntity.getBirthday() != null ? userEntity.getBirthday().getTime() : null);
        DictionaryDataEntity dictionaryDataEntity3 = dictionaryDataService.getInfo(userEntity.getRanks());
        vo.setRanks(dictionaryDataEntity3 != null && ObjectUtil.equal(dictionaryDataEntity3.getEnabledMark(), 1) ? dictionaryDataEntity3.getFullName() : null);
        // 多租户
        String tenantId = userProvider.get().getTenantId();
        Map<String, String> headers = Collections.EMPTY_MAP;
        try{
            String ip = IpUtil.getIpAddr();
            if(StringUtil.isNotEmpty(ip) && !Objects.equals("127.0.0.1", ip)) {
                headers = ImmutableMap.of("X-Forwarded-For", ip);
            }
        }catch (Exception e){}
        if (StringUtil.isNotEmpty(tenantId)) {
            vo.setIsTenant(true);
            try (HttpResponse execute = HttpRequest.get(configValueUtil.getMultiTenancyUrl() + "GetTenantInfo/" + tenantId)
                    .addHeaders(headers)
                    .execute()) {
                vo.setCurrentTenantInfo(JSON.parseObject(execute.body()));
            }catch (Exception e){
                log.error("获取远端多租户信息失败", e);
            }
        }
        return ActionResult.success(vo);
    }

    @GetMapping("/ReportUserInfo")
    public Map<String, String> reportUserInfo() {
        UserInfo userInfo = userProvider.get();
        Map<String, String> map = new HashMap<>();
        map.put("userId", userInfo.getUserId());
        map.put("departmentId", userInfo.getDepartmentId());
        map.put("organizeId", userInfo.getOrganizeId());
        map.put("positionId", userInfo.getPositionIds().length>0?userInfo.getPositionIds()[0]:"");
        map.put("roleId", userInfo.getRoleIds().size()>0?userInfo.getRoleIds().get(0):"");
        map.put("managerId", userInfo.getManagerId());
        return map;
    }



    /**
     * 递归找他的上级
     */
    public void getOrganizeName(List<OrganizeEntity> OrganizeList, String OrganizeId, StringBuilder organizeName) {
        List<OrganizeEntity> OrganizeList2 = OrganizeList.stream().filter(t -> t.getId().equals(OrganizeId)).collect(Collectors.toList());
        if (OrganizeList2.size() > 0) {
            for (OrganizeEntity organizeEntity : OrganizeList2) {
                if ("-1".equals(organizeEntity.getParentId())) {
                    //父级为-1时候退出
                    organizeName.append(organizeEntity.getFullName());
                } else {
                    organizeName.append(organizeEntity.getFullName() + " / ");
                }
            }
            for (OrganizeEntity orgSub : OrganizeList2) {
                getOrganizeName(OrganizeList, orgSub.getParentId(), organizeName);
            }
        }
    }


    /**
     * 我的权限
     *
     * @return
     */
    @Operation(summary = "系统权限")
    @GetMapping("/Authorize")
    public ActionResult<UserAuthorizeVO> getList() {
        List<AuthorizeEntity> authorizeList = new ArrayList<>();
        //系统权限
        AuthorizeVO authorizeModel = authorizeService.getAuthorizeByUser(false);
        //赋值图标
        Map<String, ModuleModel> moduleMap = this.moduleList(authorizeModel.getModuleList());
        UserInfo userInfo = userProvider.get();
        if (StringUtil.isEmpty(userInfo.getSystemId())) {
            return ActionResult.success(new UserAuthorizeVO());
        }
        List<ModuleModel> moduleList = authorizeModel.getModuleList();
        moduleList = moduleList.stream().filter(t -> t != null && StringUtil.isNotEmpty(t.getSystemId()) && t.getSystemId().equals(userInfo.getSystemId())).collect(Collectors.toList());
        moduleList.forEach(t -> {
            if (t.getParentId().equals(t.getSystemId())) {
                t.setParentId("-1");
            }
        });
        UserAuthorizeVO vo = UserAuthorizeVO.builder()
                .button(this.moduleButton(moduleList, authorizeModel.getButtonList(), authorizeList, moduleMap))
                .column(this.moduleColumn(moduleList, authorizeModel.getColumnList(), authorizeList, moduleMap))
                .form(this.moduleForm(moduleList, authorizeModel.getFormsList(), authorizeList, moduleMap))
                .resource(this.resourceData(moduleList, authorizeModel.getResourceList(), authorizeList, moduleMap))
                .module(this.module(moduleList, authorizeList))
                .portal(this.portal(authorizeModel.getSystemList())).build();
        return ActionResult.success(vo);
    }

    /**
     * 系统日志
     *
     * @param pagination 页面参数
     * @return
     */
    @Operation(summary = "系统日志")
    @GetMapping("/SystemLog")
    public ActionResult<PageListVO<UserLogVO>> getLogList(PaginationLogModel pagination) {

        PageListVO<LogEntity> pageListVO = new PageListVO();
        List<LogEntity> data = logService.getList(pagination.getCategory(), pagination);
        List<UserLogVO> loginLogVOList = JsonUtil.getJsonToList(data, UserLogVO.class);
        for (int i = 0; i < loginLogVOList.size(); i++) {
            loginLogVOList.get(i).setAbstracts(data.get(i).getDescription());
        }
        PaginationVO paginationVO = pageListVO.getPagination();
        return ActionResult.page(loginLogVOList, paginationVO);
    }

    /**
     * 修改用户资料
     *
     * @param userInfoForm 页面参数
     * @return
     */
    @Operation(summary = "修改用户资料")
    @Parameters({
            @Parameter(name = "userInfoForm", description = "页面参数", required = true)
    })
    @PutMapping("/BaseInfo")
    public ActionResult updateInfo(@RequestBody UserInfoForm userInfoForm) throws Exception {
        UserEntity userEntity = userService.getInfo(UserProvider.getLoginUserId());
        userEntity.setBirthday(userInfoForm.getBirthday() == null ? null : new Date(userInfoForm.getBirthday()));
        userEntity.setCertificatesNumber(userInfoForm.getCertificatesNumber());
        userEntity.setCertificatesType(userInfoForm.getCertificatesType());
        userEntity.setEducation(userInfoForm.getEducation());
        userEntity.setEmail(userInfoForm.getEmail());
        userEntity.setGender(userInfoForm.getGender());
        userEntity.setLandline(userInfoForm.getLandline());
        userEntity.setMobilePhone(userInfoForm.getMobilePhone());
        userEntity.setNation(userInfoForm.getNation());
        userEntity.setNativePlace(userInfoForm.getNativePlace());
        userEntity.setPostalAddress(userInfoForm.getPostalAddress());
        userEntity.setRealName(userInfoForm.getRealName());
        userEntity.setSignature(userInfoForm.getSignature());
        userEntity.setTelePhone(userInfoForm.getTelePhone());
        userEntity.setUrgentContacts(userInfoForm.getUrgentContacts());
        userEntity.setUrgentTelePhone(userInfoForm.getUrgentTelePhone());
        userService.updateById(userEntity);
        return ActionResult.success(MsgCode.SU002.get());
    }

    /**
     * 修改用户密码
     *
     * @param userModifyPasswordForm 用户修改密码表单
     * @return
     */
    @Operation(summary = "修改用户密码")
    @Parameters({
            @Parameter(name = "userModifyPasswordForm", description = "用户修改密码表单", required = true)
    })
    @PostMapping("/Actions/ModifyPassword")
    public ActionResult modifyPassword(@RequestBody @Valid UserModifyPasswordForm userModifyPasswordForm) {
        UserEntity userEntity = userService.getInfo(UserProvider.getLoginUserId());
        if (userEntity != null) {
//            if ("1".equals(String.valueOf(userEntity.getIsAdministrator()))) {
//                return ActionResult.fail("无法修改管理员账户");
//            }
            String timestamp = String.valueOf(redisUtil.getString(userModifyPasswordForm.getTimestamp()));
            if (!userModifyPasswordForm.getCode().equalsIgnoreCase(timestamp)) {
                return ActionResult.fail(MsgCode.LOG104.get());
            }
            if (!Md5Util.getStringMd5((userModifyPasswordForm.getOldPassword().toLowerCase() + userEntity.getSecretkey().toLowerCase())).equals(userEntity.getPassword())) {
                return ActionResult.fail(MsgCode.LOG201.get());
            }
            //禁用旧密码
            String disableOldPassword = sysConfigService.getValueByKey("disableOldPassword");
            if(disableOldPassword.equals("1")){
                String disableTheNumberOfOldPasswords = sysConfigService.getValueByKey("disableTheNumberOfOldPasswords");
                List<UserOldPasswordEntity> userOldPasswordList = userOldPasswordService.getList(UserProvider.getLoginUserId());
                userOldPasswordList = userOldPasswordList.stream().limit(Long.valueOf(disableTheNumberOfOldPasswords)).collect(Collectors.toList());
                for (UserOldPasswordEntity userOldPassword : userOldPasswordList) {
                    String newPassword = Md5Util.getStringMd5(userModifyPasswordForm.getPassword().toLowerCase() + userOldPassword.getSecretkey().toLowerCase());
                    if(userOldPassword.getOldPassword().equals(newPassword)){
                        return ActionResult.fail(MsgCode.LOG204.get());
                    }
                }
            }
            userEntity.setPassword(userModifyPasswordForm.getPassword());
            userService.updatePassword(userEntity);
            UserProvider.logoutByUserId(userEntity.getId());
            return ActionResult.success(MsgCode.LOG202.get());
        }
        return ActionResult.fail(MsgCode.LOG203.get());

    }

    /**
     * 我的下属
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "我的下属")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/Subordinate/{id}")
    public ActionResult<List<UserSubordinateVO>> getSubordinate(@PathVariable("id") String id) {
        List<UserEntity> userName = new ArrayList<>(16);
        List<UserSubordinateVO> list = new ArrayList<>();
        if ("0".equals(id)) {
            if (Objects.isNull(UserProvider.getLoginUserId())) {
                return ActionResult.success(list);
            }
            userName.add(userService.getInfo(UserProvider.getLoginUserId()));
        } else {
            userName = new ArrayList<>(userService.getListByManagerId(id, null));
        }
        List<String> department = userName.stream().map(t -> t.getOrganizeId()).collect(Collectors.toList());
        List<OrganizeEntity> departmentList = organizeService.getOrganizeName(department);
        for (UserEntity user : userName) {
            String departName = departmentList.stream().filter(
                    t -> String.valueOf(user.getOrganizeId()).equals(String.valueOf(t.getId()))
            ).findFirst().orElse(new OrganizeEntity()).getFullName();
            PositionEntity entity = null;
            if (StringUtil.isNotEmpty(user.getPositionId())) {
                String[] split = user.getPositionId().split(",");
                for (String positionId : split) {
                    entity = positionService.getInfo(positionId);
                    if (Objects.nonNull(entity)) {
                        break;
                    }
                }
            }
            UserSubordinateVO subordinateVO = UserSubordinateVO.builder()
                    .id(user.getId())
                    .avatar(UploaderUtil.uploaderImg(user.getHeadIcon()))
                    .department(departName)
                    .userName(user.getRealName() + "/" + user.getAccount())
                    .position(entity != null ? entity.getFullName() : null)
                    .isLeaf(false).build();
            list.add(subordinateVO);
        }
        return ActionResult.success(list);
    }

    /**
     * 修改系统主题
     *
     * @param userThemeForm 主题模板
     * @return
     */
    @Operation(summary = "修改系统主题")
    @Parameters({
            @Parameter(name = "userThemeForm", description = "主题模板", required = true)
    })
    @PutMapping("/SystemTheme")
    public ActionResult updateTheme(@RequestBody @Valid UserThemeForm userThemeForm) {
        UserEntity entity = JsonUtil.getJsonToBean(userThemeForm, UserEntity.class);
        entity.setId(UserProvider.getLoginUserId());
        userService.updateById(entity);
        return ActionResult.success(MsgCode.SU016.get());
    }

    /**
     * 修改头像
     *
     * @param name 名称
     * @return
     */
    @Operation(summary = "修改头像")
    @Parameters({
            @Parameter(name = "name", description = "名称", required = true)
    })
    @PutMapping("/Avatar/{name}")
    public ActionResult updateAvatar(@PathVariable("name") String name) throws Exception {
        UserInfo userInfo = userProvider.get();
        UserEntity userEntity = userService.getInfo(userInfo.getUserId());
        userEntity.setHeadIcon(name);
        userService.update(userEntity.getId(), userEntity);
        if (!StringUtil.isEmpty(userInfo.getId())) {
            userInfo.setUserIcon(name);
            //redisUtil.insert(userInfo.getId(), userInfo, DateUtil.getTime(userInfo.getOverdueTime()) - DateUtil.getTime(new Date()));
            UserProvider.setLoginUser(userInfo);
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 修改系统语言
     *
     * @param userLanguageForm 修改语言模型
     * @return
     */
    @Operation(summary = "修改系统语言")
    @Parameters({
            @Parameter(name = "userLanguageForm", description = "修改语言模型", required = true)
    })
    @PutMapping("/SystemLanguage")
    public ActionResult updateLanguage(@RequestBody @Valid UserLanguageForm userLanguageForm) {
        UserEntity userEntity = userService.getInfo(UserProvider.getLoginUserId());
        userEntity.setLanguage(userLanguageForm.getLanguage());
        userService.updateById(userEntity);
        return ActionResult.success(MsgCode.SU016.get());
    }


    /**
     * 赋值图标
     *
     * @param moduleList
     * @return
     */
    private Map<String, ModuleModel> moduleList(List<ModuleModel> moduleList) {
        Map<String, ModuleModel> auth = new HashMap<>(16);
        for (ModuleModel module : moduleList) {
            auth.put(module.getId(), module);
            module.setIcon(module.getIcon());
        }
        return auth;
    }

    /**
     * 功能权限
     *
     * @param moduleList    功能
     * @param authorizeLiat 权限集合
     * @return
     */
    private List<UserAuthorizeModel> module(List<ModuleModel> moduleList, List<AuthorizeEntity> authorizeLiat) {
        List<String> appId = moduleList.stream().filter(t -> "App".equals(t.getCategory())).map(t -> t.getId()).collect(Collectors.toList());
        List<AuthorizeModel> treeList = JsonUtil.getJsonToList(moduleList, AuthorizeModel.class);
        List<SumTree<AuthorizeModel>> trees = TreeDotUtils.convertListToTreeDot(treeList, "-1");
        List<UserAuthorizeModel> vo = JsonUtil.getJsonToList(trees, UserAuthorizeModel.class);
        List<UserAuthorizeModel> dataList = new LinkedList<>();
        List<UserAuthorizeModel> webChildList = new LinkedList<>();
        List<UserAuthorizeModel> appChildList = new LinkedList<>();
        for (UserAuthorizeModel model : vo) {
            if (appId.contains(model.getId())) {
                appChildList.add(model);
            } else {
                webChildList.add(model);
            }
        }
        if (webChildList.size() > 0) {
            UserAuthorizeModel webData = new UserAuthorizeModel();
            webData.setId("1");
            webData.setFullName("WEB菜单");
            webData.setIcon("icon-ym icon-ym-pc");
            webData.setChildren(webChildList);
            dataList.add(webData);
        }
        if (appChildList.size() > 0) {
            UserAuthorizeModel appData = new UserAuthorizeModel();
            appData.setId("2");
            appData.setFullName("APP菜单");
            appData.setIcon("icon-ym icon-ym-mobile");
            appData.setChildren(appChildList);
            dataList.add(appData);
        }
        return dataList;
    }

    /**
     * 按钮权限
     *
     * @param moduleList       功能
     * @param moduleButtonList 按钮
     * @param authorizeLiat    权限集合
     * @return
     */
    private List<UserAuthorizeModel> moduleButton(List<ModuleModel> moduleList, List<ButtonModel> moduleButtonList, List<AuthorizeEntity> authorizeLiat, Map<String, ModuleModel> moduleMap) {
        List<AuthorizeModel> treeList = new ArrayList<>();
        Set<String> moduleModeId = new HashSet<>();
        //获取按钮的菜单id
        for (ButtonModel buttonModel : moduleButtonList) {
            moduleModeId.add(buttonModel.getModuleId());
            AuthorizeModel treeModel = new AuthorizeModel();
            treeModel.setId(buttonModel.getId());
            treeModel.setFullName(buttonModel.getFullName());
            treeModel.setParentId(buttonModel.getModuleId());
            treeModel.setIcon(buttonModel.getIcon());
            treeList.add(treeModel);
        }
        List<ModuleModel> buttonList = moduleList.stream().filter(t -> moduleModeId.contains(t.getId())).collect(Collectors.toList());
        List<AuthorizeModel> moduleListAll = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(buttonList, moduleList), AuthorizeModel.class);
        treeList.addAll(moduleListAll);
        treeList = treeList.stream().sorted(Comparator.comparing(AuthorizeModel::getSortCode)).collect(Collectors.toList());
        List<SumTree<AuthorizeModel>> trees = TreeDotUtils.convertListToTreeDot(treeList, "-1");
        //组装菜单树
        List<String> appId = moduleList.stream().filter(t -> "App".equals(t.getCategory())).map(t -> t.getId()).collect(Collectors.toList());
        List<UserAuthorizeModel> data = JsonUtil.getJsonToList(trees, UserAuthorizeModel.class);
        List<UserAuthorizeModel> dataList = new LinkedList<>();
        List<UserAuthorizeModel> webChildList = new LinkedList<>();
        List<UserAuthorizeModel> appChildList = new LinkedList<>();
        for (UserAuthorizeModel model : data) {
            if (appId.contains(model.getId())) {
                appChildList.add(model);
            } else {
                webChildList.add(model);
            }
        }
        if (webChildList.size() > 0) {
            UserAuthorizeModel webData = new UserAuthorizeModel();
            webData.setId("1");
            webData.setFullName("WEB菜单");
            webData.setIcon("icon-ym icon-ym-pc");
            webData.setChildren(webChildList);
            dataList.add(webData);
        }
        if (appChildList.size() > 0) {
            UserAuthorizeModel appData = new UserAuthorizeModel();
            appData.setId("2");
            appData.setFullName("APP菜单");
            appData.setIcon("icon-ym icon-ym-mobile");
            appData.setChildren(appChildList);
            dataList.add(appData);
        }
        return dataList;
    }

    /**
     * 列表权限
     *
     * @param moduleList       功能
     * @param moduleColumnList 列表
     * @param authorizeLiat    权限集合
     * @return
     */
    private List<UserAuthorizeModel> moduleColumn(List<ModuleModel> moduleList, List<ColumnModel> moduleColumnList, List<AuthorizeEntity> authorizeLiat, Map<String, ModuleModel> moduleMap) {
        List<AuthorizeModel> treeList = new ArrayList<>();
        List<String> moduleModeId = new ArrayList<>();
        //获取按钮的菜单id
        for (ColumnModel columnModel : moduleColumnList) {
            moduleModeId.add(columnModel.getModuleId());
            AuthorizeModel treeModel = new AuthorizeModel();
            treeModel.setId(columnModel.getId());
            treeModel.setFullName(columnModel.getFullName());
            treeModel.setParentId(columnModel.getModuleId());
            treeModel.setIcon("fa fa-tags column");
            treeList.add(treeModel);
        }
        List<ModuleModel> buttonList = moduleList.stream().filter(t -> moduleModeId.contains(t.getId())).collect(Collectors.toList());
        List<AuthorizeModel> moduleListAll = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(buttonList, moduleList), AuthorizeModel.class);
        treeList.addAll(moduleListAll);
        treeList = treeList.stream().sorted(Comparator.comparing(AuthorizeModel::getSortCode)).collect(Collectors.toList());
        List<SumTree<AuthorizeModel>> trees = TreeDotUtils.convertListToTreeDot(treeList, "-1");
        //组装菜单树
        List<String> appId = moduleList.stream().filter(t -> "App".equals(t.getCategory())).map(t -> t.getId()).collect(Collectors.toList());
        List<UserAuthorizeModel> data = JsonUtil.getJsonToList(trees, UserAuthorizeModel.class);
        List<UserAuthorizeModel> dataList = new LinkedList<>();
        List<UserAuthorizeModel> webChildList = new LinkedList<>();
        List<UserAuthorizeModel> appChildList = new LinkedList<>();
        for (UserAuthorizeModel model : data) {
            if (appId.contains(model.getId())) {
                appChildList.add(model);
            } else {
                webChildList.add(model);
            }
        }
        if (webChildList.size() > 0) {
            UserAuthorizeModel webData = new UserAuthorizeModel();
            webData.setId("1");
            webData.setFullName("WEB菜单");
            webData.setIcon("icon-ym icon-ym-pc");
            webData.setChildren(webChildList);
            dataList.add(webData);
        }
        if (appChildList.size() > 0) {
            UserAuthorizeModel appData = new UserAuthorizeModel();
            appData.setId("2");
            appData.setFullName("APP菜单");
            appData.setIcon("icon-ym icon-ym-mobile");
            appData.setChildren(appChildList);
            dataList.add(appData);
        }
        return dataList;
    }

    /**
     * 表单权限
     *
     * @param moduleList     功能
     * @param moduleFormList 表单
     * @param authorizeLiat  权限集合
     * @return ignore
     */
    private List<UserAuthorizeModel> moduleForm(List<ModuleModel> moduleList, List<ModuleFormModel> moduleFormList, List<AuthorizeEntity> authorizeLiat, Map<String, ModuleModel> moduleMap) {
        List<AuthorizeModel> treeList = new ArrayList<>();
        List<String> moduleModeId = new ArrayList<>();
        //获取按钮的菜单id
        for (ModuleFormModel formModel : moduleFormList) {
            moduleModeId.add(formModel.getModuleId());
            AuthorizeModel treeModel = new AuthorizeModel();
            treeModel.setId(formModel.getId());
            treeModel.setFullName(formModel.getFullName());
            treeModel.setParentId(formModel.getModuleId());
            treeModel.setIcon("fa fa-binoculars resource");
            treeList.add(treeModel);
        }
        List<ModuleModel> buttonList = moduleList.stream().filter(t -> moduleModeId.contains(t.getId())).collect(Collectors.toList());
        List<AuthorizeModel> moduleListAll = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(buttonList, moduleList), AuthorizeModel.class);
        treeList.addAll(moduleListAll);
        treeList = treeList.stream().sorted(Comparator.comparing(AuthorizeModel::getSortCode)).collect(Collectors.toList());
        List<SumTree<AuthorizeModel>> trees = TreeDotUtils.convertListToTreeDot(treeList, "-1");
        //组装菜单树
        List<String> appId = moduleList.stream().filter(t -> "App".equals(t.getCategory())).map(t -> t.getId()).collect(Collectors.toList());
        List<UserAuthorizeModel> data = JsonUtil.getJsonToList(trees, UserAuthorizeModel.class);
        List<UserAuthorizeModel> dataList = new LinkedList<>();
        List<UserAuthorizeModel> webChildList = new LinkedList<>();
        List<UserAuthorizeModel> appChildList = new LinkedList<>();
        for (UserAuthorizeModel model : data) {
            if (appId.contains(model.getId())) {
                appChildList.add(model);
            } else {
                webChildList.add(model);
            }
        }
        if (webChildList.size() > 0) {
            UserAuthorizeModel webData = new UserAuthorizeModel();
            webData.setId("1");
            webData.setFullName("WEB菜单");
            webData.setIcon("icon-ym icon-ym-pc");
            webData.setChildren(webChildList);
            dataList.add(webData);
        }
        if (appChildList.size() > 0) {
            UserAuthorizeModel appData = new UserAuthorizeModel();
            appData.setId("2");
            appData.setFullName("APP菜单");
            appData.setIcon("icon-ym icon-ym-mobile");
            appData.setChildren(appChildList);
            dataList.add(appData);
        }
        return dataList;
    }

    /**
     * 数据权限
     *
     * @param moduleList         功能
     * @param moduleResourceList 资源
     * @param authorizeLiat      权限集合
     * @return ignore
     */
    private List<UserAuthorizeModel> resourceData(List<ModuleModel> moduleList, List<ResourceModel> moduleResourceList, List<AuthorizeEntity> authorizeLiat, Map<String, ModuleModel> moduleMap) {
        List<AuthorizeModel> treeList = new ArrayList<>();
        List<String> moduleModeId = new ArrayList<>();
        //获取按钮的菜单id
        for (ResourceModel resourceModel : moduleResourceList) {
            moduleModeId.add(resourceModel.getModuleId());
            AuthorizeModel treeModel = new AuthorizeModel();
            treeModel.setId(resourceModel.getId());
            treeModel.setFullName(resourceModel.getFullName());
            treeModel.setParentId(resourceModel.getModuleId());
            treeModel.setIcon("fa fa-binoculars resource");
            treeList.add(treeModel);
        }
        List<ModuleModel> buttonList = moduleList.stream().filter(t -> moduleModeId.contains(t.getId())).collect(Collectors.toList());
        List<AuthorizeModel> moduleListAll = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(buttonList, moduleList), AuthorizeModel.class);
        treeList.addAll(moduleListAll);
        treeList = treeList.stream().sorted(Comparator.comparing(AuthorizeModel::getSortCode)).collect(Collectors.toList());
        List<SumTree<AuthorizeModel>> trees = TreeDotUtils.convertListToTreeDot(treeList, "-1");
        //组装菜单树
        List<String> appId = moduleList.stream().filter(t -> "App".equals(t.getCategory())).map(t -> t.getId()).collect(Collectors.toList());
        List<UserAuthorizeModel> data = JsonUtil.getJsonToList(trees, UserAuthorizeModel.class);
        List<UserAuthorizeModel> dataList = new LinkedList<>();
        List<UserAuthorizeModel> webChildList = new LinkedList<>();
        List<UserAuthorizeModel> appChildList = new LinkedList<>();
        for (UserAuthorizeModel model : data) {
            if (appId.contains(model.getId())) {
                appChildList.add(model);
            } else {
                webChildList.add(model);
            }
        }
        if (webChildList.size() > 0) {
            UserAuthorizeModel webData = new UserAuthorizeModel();
            webData.setId("1");
            webData.setFullName("WEB菜单");
            webData.setIcon("icon-ym icon-ym-pc");
            webData.setChildren(webChildList);
            dataList.add(webData);
        }
        if (appChildList.size() > 0) {
            UserAuthorizeModel appData = new UserAuthorizeModel();
            appData.setId("2");
            appData.setFullName("APP菜单");
            appData.setIcon("icon-ym icon-ym-mobile");
            appData.setChildren(appChildList);
            dataList.add(appData);
        }
        return dataList;
    }

    /**
     * 门户权限
     *
     * @param systemBaeModelList
     * @return
     */
    private List<UserAuthorizeModel> portal(List<SystemBaeModel> systemBaeModelList) {
        List<PortalModel> myPortalList = new ArrayList<>();
        List<SystemEntity> mySystemList = JsonUtil.getJsonToList(systemBaeModelList, SystemEntity.class);
        SystemEntity systemEntity = mySystemList.stream().filter(t -> t.getId().equals(UserProvider.getUser().getSystemId())).findFirst().orElse(null);
        List<String> roleIdList = new ArrayList<>();
        permissionGroupService.getPermissionGroupByUserId(UserProvider.getLoginUserId(), null, true, null).forEach(t -> {
            roleIdList.add(t.getId());
        });
        List<String> collect = authorizeService.getListByRoleIdsAndItemType(roleIdList, AuthorizeConst.AUTHORIZE_PORTAL_MANAGE).stream().map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        authorizeService.getPortal(systemEntity == null ? new ArrayList<>() : Collections.singletonList(systemEntity), myPortalList, System.currentTimeMillis(), collect);
        myPortalList.remove(JsonUtil.getJsonToBean(systemEntity, PortalModel.class));
        List<SumTree<PortalModel>> trees = TreeDotUtils.convertListToTreeDot(myPortalList);
        trees.forEach(t -> {
            if (t.getParentId().startsWith(systemEntity.getId())) {
                t.setParentId("-1");
            }
        });
        return JsonUtil.getJsonToList(trees, UserAuthorizeModel.class);
    }

    /**
     * 设置主要组织、主要岗位（角色当前不做）
     *
     * @param userSettingForm 页面参数
     * @return
     */
    @Operation(summary = "设置主要组织、主要岗位（角色当前不做）")
    @Parameters({
            @Parameter(name = "userSettingForm", description = "页面参数", required = true)
    })
    @PutMapping("/major")
    public ActionResult<String> defaultOrganize(@RequestBody UserSettingForm userSettingForm){
        UserInfo userInfo = userProvider.get();
        UserEntity userEntity = userService.getInfo(userInfo.getUserId());
        if (userEntity == null) {
            return ActionResult.fail(ActionResultCode.SessionOverdue.getCode(), ActionResultCode.SessionOverdue.getMessage());
        }
        UserEntity updateUser = new UserEntity();
        switch (userSettingForm.getMajorType()) {
            case PermissionConst.ORGANIZE:
                String orgId = userSettingForm.getMajorId();
                // 对角色权限进行验证
                List<PermissionGroupEntity> permissionGroupEntities = organizeRelationService.checkBasePermission(userEntity.getId(), orgId, null);
                if (organizeRelationService.checkBasePermission(userEntity.getId(), orgId, null).size() == 0) {
                    return ActionResult.fail(MsgCode.FA025.get());
                }
                updateUser.setOrganizeId(orgId);
                // 只取菜单和系统
                List<AuthorizeEntity> listByObjectId = authorizeService.getListByObjectId(permissionGroupEntities.stream().map(PermissionGroupEntity::getId).collect(Collectors.toList()));
                listByObjectId = listByObjectId.stream().filter(t -> AuthorizeConst.SYSTEM.equals(t.getItemType()) || AuthorizeConst.MODULE.equals(t.getItemType())).collect(Collectors.toList());
                SystemServiceByIdsModel model = new SystemServiceByIdsModel(listByObjectId.stream().map(AuthorizeEntity::getItemId).collect(Collectors.toList()));
                List<SystemEntity> listByIds = systemService.getListByIds(model.getIds(), model.getModuleAuthorize());
                // 判断systemCode是否未空
                if (StringUtil.isNotEmpty(userInfo.getSystemCode()) && listByIds.stream().map(SystemEntity::getEnCode).noneMatch(t -> t.equals(userInfo.getSystemCode()))) {
                    return ActionResult.fail(MsgCode.FA025.get());
                }
                // 组织的权限没有当前系统
                if (listByIds.size() > 0) {
                    if (userSettingForm.getMenuType() != null && userSettingForm.getMenuType() == 1) {
                        if (!listByIds.contains(userEntity.getAppSystemId())) {
                            updateUser.setAppSystemId(listByIds.get(0).getId());
                        }
                    } else {
                        if (!listByIds.contains(userEntity.getSystemId()))
                            updateUser.setSystemId(listByIds.get(0).getId());
                    }
                }
                // 岗位自动切换
                updateUser.setPositionId(organizeRelationService.autoGetMajorPositionId(userEntity.getId(), orgId, userEntity.getPortalId()));
                break;
            case PermissionConst.POSITION:
                updateUser.setPositionId(userSettingForm.getMajorId());
                break;
            case PermissionConst.SYSTEM:
                SystemEntity systemEntity = systemService.getInfo(userSettingForm.getMajorId());
                if (systemEntity == null) {
                    return ActionResult.fail("该应用已删除");
                }
                if (systemEntity.getEnabledMark() == 0) {
                    return ActionResult.fail("该应用已禁用");
                }
                // 获取的时候判断
                List<ModuleModel> moduleList = authorizeService.getAuthorizeByUser(false).getModuleList()
                        .stream().filter(t -> StringUtil.isNotEmpty(t.getSystemId()) && t.getSystemId().equals(userSettingForm.getMajorId())).collect(Collectors.toList());
                Map<String, List<ModuleModel>> map = moduleList.stream().collect(Collectors.groupingBy(t -> {
                    if ("Web".equals(t.getCategory())) {
                        return "Web";
                    } else {
                        return "App";
                    }
                }));
                List<ModuleModel> webModule = map.containsKey("Web") ? map.get("Web") : new ArrayList<>();
                List<ModuleModel> appModule = map.containsKey("App") ? map.get("App") : new ArrayList<>();
                boolean workFlowEnabled = systemEntity.getWorkflowEnabled() != null && systemEntity.getWorkflowEnabled() == 0;
                if (Objects.equals(userSettingForm.getMenuType(), 1)) {
                    if (appModule.size() == 0 && workFlowEnabled) {
                        return ActionResult.fail(MsgCode.FA027.get());
                    }
                } else if (webModule.size() == 0 && workFlowEnabled){
                    return ActionResult.fail(MsgCode.FA027.get());
                }
                if (userSettingForm.getMenuType() != null && userSettingForm.getMenuType() == 1) {
                    updateUser.setAppSystemId(userSettingForm.getMajorId());
                } else {
                    updateUser.setSystemId(userSettingForm.getMajorId());
                }
                updateUser.setId(userEntity.getId());
                // 切换组织
                String orgIdByUserIdAndSystemId = permissionGroupService.getOrgIdByUserIdAndSystemId(userEntity.getId(), userSettingForm.getMajorId());
                if (StringUtil.isNotEmpty(orgIdByUserIdAndSystemId)) {
                    updateUser.setOrganizeId(orgIdByUserIdAndSystemId);
                }
                userService.updateById(updateUser);
                return ActionResult.success("切换成功");
            default:
                break;
        }
        updateUser.setId(userEntity.getId());
        userService.updateById(updateUser);
        return ActionResult.success(MsgCode.SU016.get());
    }

    /**
     * 获取当前用户所有组织
     *
     * @return
     */
    @Operation(summary = "获取当前用户所有组织")
    @GetMapping("/getUserOrganizes")
    public ActionResult<List<PermissionModel>> getUserOrganizes(){
        return ActionResult.success(userRelationService.getObjectVoList(PermissionConst.ORGANIZE));
    }

    /**
     * 获取当前用户当前组织底下所有岗位
     * @return
     */
    @Operation(summary = "获取当前用户当前组织底下所有岗位")
    @GetMapping("/getUserPositions")
    public ActionResult<List<PermissionModel>> getUserPositions(){
        return ActionResult.success(userRelationService.getObjectVoList(PermissionConst.POSITION));
    }


    /**
     * 获取当前用户所有角色
     * @return
     */
    @Operation(summary = "获取当前用户所有角色")
    @GetMapping("/getUserRoles")
    public ActionResult<List<PermissionModel>> getUserRoles(){
        return ActionResult.success(userRelationService.getObjectVoList(PermissionConst.ROLE));
    }

    /*= different =*/

    /**
     * 修改app常用
     *
     * @param userAppDataForm 页面参数
     * @return
     */
    @Operation(summary = "修改app常用数据")
    @Parameter(name = "userAppDataForm", description = "页面参数", required = true)
    @PutMapping("/SystemAppData")
    public ActionResult updateAppData(@RequestBody @Valid UserAppDataForm userAppDataForm) {
        UserEntity entity = userService.getInfo(UserProvider.getLoginUserId());
        entity.setPropertyJson(userAppDataForm.getData());
        userService.updateById(entity);
        return ActionResult.success(MsgCode.SU016.get());
    }


    /**
     * 获取个性签名列表
     *
     * @return ignore
     */
    @Operation(summary = "获取个性签名列表")
    @GetMapping("/SignImg")
    public ActionResult getListSignImg() {
        List<SignEntity> list = signService.getList();
        List<SignListVO> data = JsonUtil.getJsonToList(list, SignListVO.class);
        return  ActionResult.success(data);
    }


    /**
     * 新建
     *
     * @param signForm 实体对象
     * @return ignore
     */
    @Operation(summary = "添加个性签名")
    @Parameter(name = "signForm", description = "实体对象", required = true)
    @PostMapping("/SignImg")
    public ActionResult create(@RequestBody @Valid SignForm signForm) {
        SignEntity entity = JsonUtil.getJsonToBean(signForm, SignEntity.class);
        boolean b = signService.create(entity);
        if(b){
            return ActionResult.success(MsgCode.SU001.get());
        }
        return ActionResult.fail(MsgCode.SU001.get());
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "删除个性签名")
    @Parameter(name = "id", description = "主键值", required = true)
    @DeleteMapping("/{id}/SignImg")
    public ActionResult delete(@PathVariable("id") String id) {
        boolean delete = signService.delete(id);
        if(delete){
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.SU003.get());
    }

    /**
     * 设置默认
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "设置默认")
    @Parameter(name = "id", description = "主键值", required = true)
    @PutMapping("/{id}/SignImg")
    public ActionResult uptateDefault(@PathVariable("id") String id) {
        boolean b = signService.updateDefault(id);
        if(b){
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail(MsgCode.SU004.get());
    }

    
    @GetMapping("/getAuthorize")
    public AuthorizeVO getAuthorize() {
        return authorizeService.getAuthorizeByUser(false);
    }
}
