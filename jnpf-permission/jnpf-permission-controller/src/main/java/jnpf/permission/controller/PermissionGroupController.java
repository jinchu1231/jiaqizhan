package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.ModuleButtonEntity;
import jnpf.base.entity.ModuleColumnEntity;
import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.ModuleFormEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.model.base.SystemServiceByIdsModel;
import jnpf.base.service.ModuleButtonService;
import jnpf.base.service.ModuleColumnService;
import jnpf.base.service.ModuleDataAuthorizeSchemeService;
import jnpf.base.service.ModuleFormService;
import jnpf.base.service.ModuleService;
import jnpf.base.service.SystemService;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.model.FlowWorkModel;
import jnpf.permission.constant.AuthorizeConst;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.entity.GroupEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.permissiongroup.PaginationPermissionGroup;
import jnpf.permission.model.permissiongroup.PermissionGroupListVO;
import jnpf.permission.model.permissiongroup.PermissionGroupModel;
import jnpf.permission.model.permissiongroup.ViewPermissionsModel;
import jnpf.permission.model.permissiongroup.ViewPermissionsTreeModel;
import jnpf.permission.model.permissiongroup.ViewPermissionsVO;
import jnpf.permission.model.user.UserIdListVo;
import jnpf.permission.model.user.UserIdModel;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.GroupService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PermissionGroupService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserService;
import jnpf.portal.model.PortalModel;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Tag(name = "权限组控制器", description = "PermissionGroup")
@RequestMapping("/api/permission/PermissionGroup")
public class PermissionGroupController extends SuperController<PermissionGroupService, PermissionGroupEntity> {

    @Autowired
    private PermissionGroupService permissionGroupService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private ModuleButtonService buttonService;
    @Autowired
    private ModuleColumnService columnService;
    @Autowired
    private ModuleFormService formService;
    @Autowired
    private ModuleDataAuthorizeSchemeService schemeService;

    /**
     * 列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "列表")
    @SaCheckPermission("permission.authorize")
    @GetMapping
    public ActionResult<PageListVO<PermissionGroupListVO>> list(PaginationPermissionGroup pagination) {
        List<PermissionGroupEntity> data = permissionGroupService.list(pagination);
        List<PermissionGroupListVO> list = JsonUtil.getJsonToList(data, PermissionGroupListVO.class);
        list.forEach(t -> {
            String permissionMember = t.getPermissionMember();
            if (StringUtil.isEmpty(permissionMember)) {
                t.setPermissionMember("");
                return;
            }
            List<String> fullNameByIds = userService.getFullNameByIds(Arrays.asList(permissionMember.split(",")));
            StringJoiner stringJoiner = new StringJoiner(",");
            fullNameByIds.forEach(stringJoiner::add);
            t.setPermissionMember(stringJoiner.toString());
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 下拉选择
     *
     * @return
     */
    @Operation(summary = "下拉框")
    @SaCheckPermission("permission.authorize")
    @GetMapping("/Selector")
    public ActionResult<ListVO<FlowWorkModel>> list() {
        List<PermissionGroupEntity> data = permissionGroupService.list(true, null);
        List<FlowWorkModel> list = JsonUtil.getJsonToList(data, FlowWorkModel.class);
        list.forEach(t -> t.setIcon("icon-ym icon-ym-authGroup"));
        ListVO<FlowWorkModel> listVO = new ListVO<>();
        listVO.setList(list);
        return ActionResult.success(listVO);
    }

    /**
     * 查看权限成员
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "权限成员")
    @SaCheckPermission("permission.authorize")
    @Parameter(name = "id", description = "主键", required = true)
    @GetMapping("/PermissionMember/{id}")
    public ActionResult<ListVO<UserIdListVo>> permissionMember(@PathVariable("id") String id) {
        PermissionGroupEntity entity = permissionGroupService.permissionMember(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA003.get());
        }
        ListVO<UserIdListVo> listVO = new ListVO<>();
        List<UserIdListVo> list = new ArrayList<>();
        if (StringUtil.isEmpty(entity.getPermissionMember())) {
            listVO.setList(list);
            return ActionResult.success(listVO);
        }
        List<String> ids = Arrays.asList(entity.getPermissionMember().split(","));
        list = userService.selectedByIds(ids);
        listVO.setList(list);
        return ActionResult.success(listVO);
    }

    /**
     * 保存权限成员
     *
     * @param id          主键
     * @param userIdModel 用户id模型
     * @return
     */
    @Operation(summary = "保存权限成员")
    @SaCheckPermission("permission.authorize")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "userIdModel", description = "用户id模型", required = true)
    })
    @PostMapping("/PermissionMember/{id}")
    public ActionResult<ListVO<UserIdListVo>> savePermissionMember(@PathVariable("id") String id, @RequestBody UserIdModel userIdModel) {
        PermissionGroupEntity entity = permissionGroupService.info(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA003.get());
        }
        StringJoiner stringJoiner = new StringJoiner(",");
        List<String> userId = userIdModel.getIds();
        userId.forEach(t -> {
            stringJoiner.add(t);
        });
        entity.setPermissionMember(stringJoiner.toString());
        // 修改前的用户
        List<String> member = permissionGroupService.list(Collections.singletonList(id))
                .stream().filter(t -> StringUtil.isNotEmpty(t.getPermissionMember())).map(PermissionGroupEntity::getPermissionMember).collect(Collectors.toList());
        // 新的用户
        member.addAll(userId);
        member = member.stream().distinct().collect(Collectors.toList());
        List<String> userIdList = userService.getUserIdList(member, null);
        permissionGroupService.update(id, entity);
        userService.delCurUser(null, userIdList.stream().toArray(String[]::new));
        return ActionResult.success(MsgCode.SU002.get());
    }

    /**
     * 详情
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "详情")
    @SaCheckPermission("permission.authorize")
    @Parameter(name = "id", description = "主键", required = true)
    @GetMapping("/{id}")
    public ActionResult<PermissionGroupModel> info(@PathVariable("id") String id) {
        PermissionGroupEntity entity = permissionGroupService.info(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA003.get());
        }
        PermissionGroupModel model = JsonUtil.getJsonToBean(entity, PermissionGroupModel.class);
        return ActionResult.success(model);
    }

    /**
     * 新建
     *
     * @param model 模型
     * @return
     */
    @Operation(summary = "新建")
    @SaCheckPermission("permission.authorize")
    @Parameter(name = "id", description = "模型", required = true)
    @PostMapping
    public ActionResult<String> crete(@RequestBody PermissionGroupModel model) {
        PermissionGroupEntity entity = JsonUtil.getJsonToBean(model, PermissionGroupEntity.class);
        if (permissionGroupService.isExistByFullName(entity.getId(), entity)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (permissionGroupService.isExistByEnCode(entity.getId(), entity)) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        permissionGroupService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 修改
     *
     * @param id    主键
     * @param model 模型
     * @return
     */
    @Operation(summary = "修改")
    @SaCheckPermission("permission.authorize")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "model", description = "模型", required = true)
    })
    @PutMapping("/{id}")
    public ActionResult<String> update(@PathVariable("id") String id, @RequestBody PermissionGroupModel model) {
        PermissionGroupEntity permissionGroupEntity = permissionGroupService.info(id);
        PermissionGroupEntity entity = JsonUtil.getJsonToBean(model, PermissionGroupEntity.class);
        if (permissionGroupService.isExistByFullName(id, entity)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (permissionGroupService.isExistByEnCode(id, entity)) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        if (permissionGroupEntity.getEnabledMark() == 1 && entity.getEnabledMark() == 0) {
            userService.delCurRoleUser(Collections.singletonList(id));
        }
        permissionGroupService.update(id, entity);
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除")
    @SaCheckPermission("permission.authorize")
    @Parameter(name = "id", description = "主键", required = true)
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        PermissionGroupEntity entity = permissionGroupService.info(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA003.get());
        }
        userService.delCurRoleUser(Collections.singletonList(id));
        permissionGroupService.delete(entity);
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 复制
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "复制")
    @SaCheckPermission("permission.authorize")
    @Parameter(name = "id", description = "主键", required = true)
    @PostMapping("/{id}/Actions/Copy")
    @Transactional
    public ActionResult<String> copy(@PathVariable("id") String id) {
        PermissionGroupEntity entity = permissionGroupService.info(id);
        if (entity == null) {
            return ActionResult.fail("复制失败，数据不存在");
        }
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        entity.setFullName(entity.getFullName() + ".副本" + copyNum);
        if (entity.getFullName().length() > 50) return ActionResult.fail(MsgCode.COPY001.get());
        entity.setEnCode(entity.getEnCode() + "." + copyNum);
        entity.setId(RandomUtil.uuId());
        entity.setEnabledMark(0);
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(UserProvider.getLoginUserId());
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        permissionGroupService.save(entity);
        // 赋值权限表
        List<AuthorizeEntity> listByObjectId = authorizeService.getListByObjectId(Collections.singletonList(id));
        listByObjectId.forEach(t -> {
            t.setId(RandomUtil.uuId());
            t.setObjectId(entity.getId());
        });
        authorizeService.saveBatch(listByObjectId);
        return ActionResult.success(MsgCode.SU007.get());
    }/**
     * 获取菜单权限返回权限组
     *
     * @param model       模型
     * @return ignore
     */
    @Operation(summary = "获取菜单权限返回权限组")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/getPermissionGroup")
    public ActionResult<Map<String, Object>> getPermissionGroup(ViewPermissionsModel model) {
        String objectType = model.getObjectType();
        String id = model.getId();
        if (checkDataById(id, objectType)) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        Map<String, Object> map = new HashMap<>(2);
        int type = 0; // 0未开启权限，1有
        List<FlowWorkModel> list = new ArrayList<>();
        List<PermissionGroupEntity> permissionGroupByUserId = permissionGroupService.getPermissionGroupByObjectId(id, objectType);
        list = JsonUtil.getJsonToList(permissionGroupByUserId, FlowWorkModel.class);
        list.forEach(t -> t.setIcon("icon-ym icon-ym-authGroup"));
        if (list.size() > 0) {
            type = 1;
        } else {
            type = 2;
        }
        map.put("list", list);
        map.put("type", type);
        return ActionResult.success(map);
    }

    /**
     * 通过权限组id获取相关权限
     *
     * @param model       模型
     * @return ignore
     */
    @Operation(summary = "通过权限组id获取相关权限")
    @Parameters({
            @Parameter(name = "id", description = "权限组id", required = true)
    })
    @GetMapping("/getPermission")
    public ActionResult<List<ViewPermissionsVO>> getPermission(ViewPermissionsModel model) {
        String objectType = model.getObjectType();
        String id = model.getId();
        String permissionId = model.getPermissionId();
        if (StringUtil.isEmpty(permissionId)) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        // 获取当前菜单开启了哪些权限
        if (checkDataById(id, objectType)) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        PermissionGroupEntity permissionGroupEntity = permissionGroupService.info(permissionId);
        if (permissionGroupEntity == null) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        String itemType = model.getItemType();
        // 权限组的权限
        List<AuthorizeEntity> authList = authorizeService.getListByObjectId(Collections.singletonList(permissionId));
        List<ViewPermissionsTreeModel> list = new ArrayList<>();
        if (AuthorizeConst.SYSTEM.equals(itemType)) {
            list = this.system(authList, itemType);
        } else if (AuthorizeConst.MODULE.equals(itemType)) {
            list = this.module(authList, itemType);
        } else if (AuthorizeConst.BUTTON.equals(itemType)) {
            list = this.button(authList, itemType);
        } else if (AuthorizeConst.COLUMN.equals(itemType)) {
            list = this.column(authList, itemType);
        } else if (AuthorizeConst.FROM.equals(itemType)) {
            list = this.form(authList, itemType);
        } else if (AuthorizeConst.RESOURCE.equals(itemType)) {
            list = this.resources(authList, itemType);
        } else if (AuthorizeConst.AUTHORIZE_PORTAL_MANAGE.equals(itemType)) {
            list = this.portal(authList, AuthorizeConst.AUTHORIZE_PORTAL_MANAGE);
        }
        list = list.stream().sorted(Comparator.comparing(ViewPermissionsTreeModel::getSortCode, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(ViewPermissionsTreeModel::getCreatorTime, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
        List<SumTree<ViewPermissionsTreeModel>> sumTrees = TreeDotUtils.convertListToTreeDot(list);
        List<ViewPermissionsVO> jsonToList = JsonUtil.getJsonToList(sumTrees, ViewPermissionsVO.class);
        return ActionResult.success(jsonToList);
    }

    /**
     * 返回所有系统信息
     * @param authList
     * @param itemType
     * @return
     */
    private List<ViewPermissionsTreeModel> system(List<AuthorizeEntity> authList, String itemType) {
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        SystemServiceByIdsModel model = SystemServiceByIdsModel.builder().ids(ids).build();
        return JsonUtil.getJsonToList(
                systemService.getListByIds(model.getIds(), model.getModuleAuthorize()),
                ViewPermissionsTreeModel.class
        );
    }

    /**
     * 返回所有菜单信息
     * @param authList
     * @param itemType
     * @return
     */
    private List<ViewPermissionsTreeModel> module(List<AuthorizeEntity> authList, String itemType) {
        List<ViewPermissionsTreeModel> systemList = this.system(authList, AuthorizeConst.SYSTEM);
        systemList.forEach(systemEntity -> systemEntity.setParentId("-1"));
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        List<ModuleEntity> moduleByIds = moduleService.getModuleByIds(ids, null, null, false);
        Map<String, List<ModuleEntity>> systemGroupMap = moduleByIds.stream().collect(Collectors.groupingBy(ModuleEntity::getSystemId));
        List<ModuleEntity> categoryList = new ArrayList<>();
        Date datetime = new Date();
        if (systemGroupMap != null) {
            ids.forEach(systemId -> {
                List<ModuleEntity> moduleEntities = systemGroupMap.get(systemId);
                if (moduleEntities != null && moduleEntities.size() > 0) {
                    Map<String, List<ModuleEntity>> categoryMap = moduleEntities.stream().collect(Collectors.groupingBy(ModuleEntity::getCategory));
                    if (categoryMap != null) {
                        List<ModuleEntity> webModuleList = categoryMap.get("Web");
                        if (webModuleList != null && webModuleList.size() > 0) {
                            ModuleEntity entity = new ModuleEntity();
                            entity.setParentId(webModuleList.get(0).getSystemId());
                            entity.setId(webModuleList.get(0).getSystemId() + "1");
                            entity.setFullName("WEB菜单");
                            entity.setIcon("icon-ym icon-ym-pc");
                            entity.setSortCode(-1L);
                            entity.setCreatorTime(datetime);
                            categoryList.add(entity);
                        }
                        List<ModuleEntity> appModuleList = categoryMap.get("App");
                        if (appModuleList != null && appModuleList.size() > 0) {
                            ModuleEntity entity = new ModuleEntity();
                            entity.setParentId(webModuleList.get(0).getSystemId());
                            entity.setId(webModuleList.get(0).getSystemId() + "2");
                            entity.setFullName("APP菜单");
                            entity.setIcon("icon-ym icon-ym-mobile");
                            entity.setSortCode(0L);
                            entity.setCreatorTime(datetime);
                            categoryList.add(entity);
                        }
                    }
                }
            });
        }
        moduleByIds.addAll(categoryList);
        moduleByIds.forEach(t -> {
            if ("-1".equals(t.getParentId())) {
                if ("Web".equals(t.getCategory())) {
                    t.setParentId(t.getSystemId() + "1");
                } else {
                    t.setParentId(t.getSystemId() + "2");
                }
            }
        });
        List<ViewPermissionsTreeModel> moduleList = JsonUtil.getJsonToList(moduleByIds, ViewPermissionsTreeModel.class);
        List<String> systemId = moduleByIds.stream().map(ModuleEntity::getSystemId).distinct().collect(Collectors.toList());
        List<ViewPermissionsTreeModel> collect = systemList.stream().filter(t -> systemId.contains(t.getId())).collect(Collectors.toList());
        moduleList.addAll(collect);
        return moduleList;
    }

    /**
     * 返回所有按钮权限信息
     * @param authList
     * @param itemType
     * @return
     */
    private List<ViewPermissionsTreeModel> button(List<AuthorizeEntity> authList, String itemType) {
        List<ViewPermissionsTreeModel> module = this.module(authList, AuthorizeConst.MODULE);
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        List<ModuleButtonEntity> listByIds = buttonService.getListByIds(ids);
        listByIds.forEach(t -> t.setParentId(t.getModuleId()));
        List<ViewPermissionsTreeModel> moduleList = JsonUtil.getJsonToList(listByIds, ViewPermissionsTreeModel.class);
        Map<String, ViewPermissionsTreeModel> moduleModel = module.stream().collect(Collectors.toMap(ViewPermissionsTreeModel::getId, Function.identity()));
        // 上级菜单id
        List<String> moduleIds = listByIds.stream().map(ModuleButtonEntity::getModuleId).distinct().collect(Collectors.toList());
        moduleIds.forEach(t -> {
            ViewPermissionsTreeModel viewPermissionsTreeModel = moduleModel.get(t);
            moduleList.add(viewPermissionsTreeModel);
            getParentModule(moduleModel, viewPermissionsTreeModel.getParentId(), moduleList);
        });
        return moduleList;
    }

    /**
     * 返回所有列表权限信息
     * @param authList
     * @param itemType
     * @return
     */
    private List<ViewPermissionsTreeModel> column(List<AuthorizeEntity> authList, String itemType) {
        List<ViewPermissionsTreeModel> module = this.module(authList, AuthorizeConst.MODULE);
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        List<ModuleColumnEntity> listByIds = columnService.getListByIds(ids);
        listByIds.forEach(t -> t.setParentId(t.getModuleId()));
        List<ViewPermissionsTreeModel> moduleList = JsonUtil.getJsonToList(listByIds, ViewPermissionsTreeModel.class);
        Map<String, ViewPermissionsTreeModel> moduleModel = module.stream().collect(Collectors.toMap(ViewPermissionsTreeModel::getId, Function.identity()));
        // 上级菜单id
        List<String> moduleIds = listByIds.stream().map(ModuleColumnEntity::getModuleId).distinct().collect(Collectors.toList());
        moduleIds.forEach(t -> {
            ViewPermissionsTreeModel viewPermissionsTreeModel = moduleModel.get(t);
            moduleList.add(viewPermissionsTreeModel);
            getParentModule(moduleModel, viewPermissionsTreeModel.getParentId(), moduleList);
        });
        return moduleList;
    }

    /**
     * 返回所有表单权限信息
     * @param authList
     * @param itemType
     * @return
     */
    private List<ViewPermissionsTreeModel> form(List<AuthorizeEntity> authList, String itemType) {
        List<ViewPermissionsTreeModel> module = this.module(authList, AuthorizeConst.MODULE);
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        List<ModuleFormEntity> listByIds = formService.getListByIds(ids);
        listByIds.forEach(t -> t.setParentId(t.getModuleId()));
        List<ViewPermissionsTreeModel> moduleList = JsonUtil.getJsonToList(listByIds, ViewPermissionsTreeModel.class);
        Map<String, ViewPermissionsTreeModel> moduleModel = module.stream().collect(Collectors.toMap(ViewPermissionsTreeModel::getId, Function.identity()));
        // 上级菜单id
        List<String> moduleIds = listByIds.stream().map(ModuleFormEntity::getModuleId).distinct().collect(Collectors.toList());
        moduleIds.forEach(t -> {
            ViewPermissionsTreeModel viewPermissionsTreeModel = moduleModel.get(t);
            moduleList.add(viewPermissionsTreeModel);
            getParentModule(moduleModel, viewPermissionsTreeModel.getParentId(), moduleList);
        });
        return moduleList;
    }

    /**
     * 返回所有数据权限信息
     * @param authList
     * @param itemType
     * @return
     */
    private List<ViewPermissionsTreeModel> resources(List<AuthorizeEntity> authList, String itemType) {
        List<ViewPermissionsTreeModel> module = this.module(authList, AuthorizeConst.MODULE);
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        List<ModuleDataAuthorizeSchemeEntity> listByIds = schemeService.getListByIds(ids);
        List<ViewPermissionsTreeModel> moduleList = JsonUtil.getJsonToList(listByIds, ViewPermissionsTreeModel.class);
        moduleList.forEach(t -> t.setParentId(t.getModuleId()));
        Map<String, ViewPermissionsTreeModel> moduleModel = module.stream().collect(Collectors.toMap(ViewPermissionsTreeModel::getId, Function.identity()));
        // 上级菜单id
        List<String> moduleIds = listByIds.stream().map(ModuleDataAuthorizeSchemeEntity::getModuleId).distinct().collect(Collectors.toList());
        moduleIds.forEach(t -> {
            ViewPermissionsTreeModel viewPermissionsTreeModel = moduleModel.get(t);
            moduleList.add(viewPermissionsTreeModel);
            getParentModule(moduleModel, viewPermissionsTreeModel.getParentId(), moduleList);
        });
        return moduleList;
    }

    private List<ViewPermissionsTreeModel> portal(List<AuthorizeEntity> authList, String itemType) {
        List<String> ids = authList.stream().filter(t -> itemType.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        List<PortalModel> myPortalList = new ArrayList<>();
        List<SystemEntity> mySystemList = systemService.getListByIds(ids, null);
        List<String> collect = authList.stream().filter(t -> AuthorizeConst.AUTHORIZE_PORTAL_MANAGE.equals(t.getItemType())).map(AuthorizeEntity::getItemId).collect(Collectors.toList());
        authorizeService.getPortal(mySystemList, myPortalList, System.currentTimeMillis(), collect);
        return JsonUtil.getJsonToList(myPortalList.stream().sorted(Comparator.comparing(PortalModel::getSortCode).thenComparing(PortalModel::getCreatorTime).reversed()).collect(Collectors.toList()), ViewPermissionsTreeModel.class);
    }

    /**
     * 获取上级菜单
     *
     * @param moduleModel
     * @param parentId
     * @param moduleList
     */
    private void getParentModule(Map<String, ViewPermissionsTreeModel> moduleModel, String parentId, List<ViewPermissionsTreeModel> moduleList) {
        if (!"-1".equals(parentId)) {
            if (moduleModel.get(parentId) != null) {
                moduleList.add(moduleModel.get(parentId));
                this.getParentModule(moduleModel, moduleModel.get(parentId).getParentId(), moduleList);
            }
        }
    }

    /**
     * 验证对象数据是否存在
     *
     * @param id
     * @param objectType
     * @return
     */
    private boolean checkDataById(String id, String objectType) {
        if (PermissionConst.COMPANY.equals(objectType) || PermissionConst.DEPARTMENT.equals(objectType)) {
            // 获取当前菜单开启了哪些权限
            OrganizeEntity entity = organizeService.getInfo(id);
            if (entity == null) {
                return true;
            }
        } else if ("position".equals(objectType)) {
            PositionEntity entity = positionService.getInfo(id);
            if (entity == null) {
                return true;
            }
        } else if ("user".equals(objectType)) {
            UserEntity entity = userService.getInfo(id);
            if (entity == null) {
                return true;
            }
        } else if ("role".equals(objectType)) {
            RoleEntity entity = roleService.getInfo(id);
            if (entity == null) {
                return true;
            }
        } else if ("group".equals(objectType)) {
            GroupEntity entity = groupService.getInfo(id);
            if (entity == null) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    
    @GetMapping("/getPermissionGroupByUserId")
    public List<PermissionGroupEntity> getPermissionGroupByUserId(@RequestParam("userId") String userId) {
        return permissionGroupService.getPermissionGroupByUserId(userId, null, false, null);
    }

    
    @NoDataSourceBind
    @GetMapping("/getPermissionGroupByUserIdAndTenantId")
    public List<PermissionGroupEntity> getPermissionGroupByUserIdAndTenantId(@RequestParam(value = "userId", required = false) String userId,
                                                                             @RequestParam(value = "tenantId", required = false) String tenantId,
                                                                             @RequestParam(value = "systemId", required = false) String systemId) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        return permissionGroupService.getPermissionGroupByUserId(userId, null, false, null);
    }

    
    @NoDataSourceBind
    @GetMapping("/getOrganizeIdByUserIdAndTenantId")
    public String getOrganizeIdByUserIdAndTenantId(@RequestParam(value = "userId", required = false) String userId,
                                                                             @RequestParam(value = "tenantId", required = false) String tenantId) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        return permissionGroupService.getPermissionGroupByUserId(userId);
    }

    
    @GetMapping("/getPermissionGroupByModuleId")
    public List<PermissionGroupEntity> getPermissionGroupByModuleId(@RequestParam("moduleId") String moduleId) {
        return permissionGroupService.getPermissionGroupByModuleId(moduleId);
    }

    
    @GetMapping("/getInfoById")
    public PermissionGroupEntity getInfoById(@RequestParam("id") String id) {
        return permissionGroupService.info(id);
    }

    
    @PostMapping("/getListByIds")
    public List<PermissionGroupEntity> getListByIds(@RequestBody List<String> ids) {
        return permissionGroupService.list(ids);
    }

}
