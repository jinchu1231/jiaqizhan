package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jnpf.base.controller.SuperController;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jnpf.annotation.RolePermission;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.JnpfConst;
import jnpf.constant.MsgCode;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.constant.PermissionConst;
import jnpf.permission.entity.*;
import jnpf.permission.model.position.PosOrgConditionModel;
import jnpf.permission.model.position.PositionSelectorVO;
import jnpf.permission.model.role.*;
import jnpf.permission.model.user.UserIdModel;
import jnpf.permission.service.*;
import jnpf.permission.util.PermissionUtil;
import jnpf.util.*;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "角色管理", description = "Role")
@RestController
@RequestMapping("/api/permission/Role")
public class RoleController extends SuperController<RoleService, RoleEntity> {

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private OrganizeAdministratorService organizeAdministratorService;

    /**
     * 获取角色列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取角色列表")
    @SaCheckPermission("permission.role")
    @GetMapping
    public ActionResult<PageListVO<RoleListVO>> list(RolePagination pagination) {
        // null:全部显示 0：全局
        int globalMark;
        if (pagination.getType() != null) {
            globalMark = pagination.getType();
        } else {
            if ("0".equals(pagination.getOrganizeId())) {
                // 显示全局
                globalMark = 1;
            } else if ("".equals(pagination.getOrganizeId())) {
                // 全部显示
                globalMark = -1;
            } else {
                // 显示组织内
                globalMark = 0;
            }
        }
        List<RoleEntity> list = roleService.getList(pagination , globalMark);
        List<RoleListVO> listVO = new ArrayList<>();
        for (RoleEntity entity : list) {
            // 角色类型展示
            RoleListVO vo = JsonUtil.getJsonToBean(entity, RoleListVO.class);
            if(entity.getGlobalMark() != null && entity.getGlobalMark().equals(1)){
                vo.setType("全局");
            }else {
                vo.setType("组织");
                QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
                query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
                query.lambda().eq(OrganizeRelationEntity::getObjectId, entity.getId());
                List<String> ids = new ArrayList<>();
                for(OrganizeRelationEntity relation : organizeRelationService.list(query)) {
                    ids.add(relation.getOrganizeId());
                }
                String orgInfos = PermissionUtil.getLinkInfoByOrgId(ids, organizeService);
                if(orgInfos.length() > 0){
                    vo.setOrganizeInfo(orgInfos.substring(0, orgInfos.length() -1));
                }else {
                    vo.setOrganizeInfo("");
                }

            }
            listVO.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO,paginationVO);
    }

    /**
     * 角色下拉框列表
     *
     * @return
     */
    @Operation(summary = "角色下拉框列表")
    @GetMapping("/Selector")
    public ActionResult<ListVO<RoleSelectorVO>> listAll() {
        List<RoleEntity> list1 = roleService.getList(true);
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, false, null);

        List<RoleModel> roleModels = new ArrayList<>();
        List<RoleSelectorVO> modelList = new ArrayList<>();

        // 全局展示
        RoleSelectorVO globalParent = new RoleSelectorVO();
        String globalParentId = "1";
        globalParent.setFullName("全局");
        globalParent.setId("1");
        globalParent.setId(globalParentId);
        globalParent.setOnlyId("organizeList_0");
        globalParent.setIcon("icon-ym icon-ym-global-role");
        List<RoleSelectorVO> globalModelList = JsonUtil.getJsonToList(roleService.getGlobalList(), RoleSelectorVO.class);
        globalModelList.forEach(g-> {
            g.setType("role");
            g.setOnlyId(UUID.randomUUID().toString());
            g.setParentId(globalParentId);
            g.setIcon("icon-ym icon-ym-generator-role");
        });
        globalParent.setHasChildren(globalModelList.size() > 0);
        globalParent.setChildren(globalModelList);
        globalParent.setIsLeaf(false);
        globalParent.setIcon("icon-ym icon-ym-global-role");

        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        List<OrganizeRelationEntity> relationListByObjectIdAndType = organizeRelationService.getRelationListByType(PermissionConst.ROLE);
        for (RoleEntity roleEntity : list1) {
            if (roleEntity.getEnabledMark() == 0) {
                continue;
            }
            List<OrganizeRelationEntity> relationListByObjectIdAndTypes = relationListByObjectIdAndType.stream().filter(t -> roleEntity.getId().equals(t.getObjectId())).collect(Collectors.toList());
            for (OrganizeRelationEntity entity : relationListByObjectIdAndTypes) {
                RoleModel roleVo = JsonUtil.getJsonToBean(roleEntity, RoleModel.class);
                // 必须加这个标识，不然前端会报错
                roleVo.setType("role");
                roleVo.setOnlyId(UUID.randomUUID().toString());
                String organizeId = entity.getOrganizeId();
                roleVo.setParentId(organizeId);
                roleVo.setIcon("icon-ym icon-ym-generator-role");
                OrganizeEntity organizeEntity = orgMaps.get(organizeId);
                if (organizeEntity != null) {
                    roleVo.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/"));
                    roleVo.setOrganizeIds(organizeService.getOrgIdTree(organizeEntity));
                } else {
                    roleVo.setOrganizeIds(new ArrayList<>());
                }
                roleModels.add(roleVo);
            }
        }
        List<RoleModel> orgList = new ArrayList<>(16);
        orgMaps.values().stream().forEach(org -> {
            RoleModel orgVo = JsonUtil.getJsonToBean(org, RoleModel.class);
            orgVo.setType(org.getCategory());
            orgVo.setIcon(StringUtil.isNotEmpty(org.getCategory()) ? "company".equals(org.getCategory()) ? "icon-ym icon-ym-tree-organization3" : "icon-ym icon-ym-tree-department1" : "");
            orgVo.setOnlyId(UUID.randomUUID().toString());
            OrganizeEntity organizeEntity = orgMaps.get(orgVo.getId());
            if (organizeEntity != null) {
                orgVo.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/"));
                orgVo.setOrganizeIds(organizeService.getOrgIdTree(organizeEntity));
            } else {
                orgVo.setOrganizeIds(new ArrayList<>());
            }
            orgList.add(orgVo);
        });

        JSONArray objects = ListToTreeUtil.treeWhere(roleModels, orgList);
        List<RoleModel> jsonToList = JsonUtil.getJsonToList(objects, RoleModel.class);

        List<RoleModel> list = new ArrayList<>(16);
        // 得到角色的值
        List<RoleModel> collect = jsonToList.stream().filter(t -> "role".equals(t.getType())).sorted(Comparator.comparing(RoleModel::getSortCode)).collect(Collectors.toList());
        list.addAll(collect);
        jsonToList.removeAll(collect);
        List<RoleModel> collect1 = jsonToList.stream().sorted(Comparator.comparing(RoleModel::getSortCode).thenComparing(RoleModel::getCreatorTime, Comparator.reverseOrder())).collect(Collectors.toList());
        list.addAll(collect1);

        List<SumTree<RoleModel>> trees = TreeDotUtils2.convertListToTreeDot(list);
        modelList.addAll(JsonUtil.getJsonToList(trees, RoleSelectorVO.class));
        modelList.add(globalParent);
        ListVO vo = new ListVO();
        vo.setList(modelList);
        return ActionResult.success(vo);
    }

    /**
     * 分级管理下角色下拉框列表
     *
     * @return
     */
    @Operation(summary = "分级管理下角色下拉框列表")
    @GetMapping("/SelectorByPermission")
    public ActionResult<ListVO<RoleSelectorVO>> roleListAll() {
        boolean isAdministrator = userProvider.get().getIsAdministrator();
        List<RoleEntity> list1;
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, false, null);

        List<RoleModel> roleModels = new ArrayList<>();
        List<RoleSelectorVO> modelList = new ArrayList<>();

        Collection<OrganizeEntity> orgList = new ArrayList<>();
        if (!isAdministrator) {
            Set<String> set = new HashSet<>(16);
            // 获取用户分级管理的权限
            List<OrganizeAdministratorEntity> organizeAdministratorEntity = organizeAdministratorService.getOrganizeAdministratorEntity(userProvider.get().getUserId());
            organizeAdministratorEntity.stream().forEach(t->{
                // 如果有本层编辑权限
                if ("1".equals(String.valueOf(t.getThisLayerEdit()))) {
                    set.add(t.getOrganizeId());
                }
                if ("1".equals(String.valueOf(t.getSubLayerEdit()))) {
                    // 如果有子层编辑权限
                    List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                    set.addAll(underOrganizations);
                }
            });
            Collection<OrganizeEntity> finalOrganizeEntities = orgList;
            set.forEach(t->{
                OrganizeEntity entity = orgMaps.get(t);
                if (entity != null) {
                    finalOrganizeEntities.add(entity);
                }
            });
            List<OrganizeRelationEntity> relationListByOrganizeId = organizeRelationService.getRelationListByOrganizeId(orgList.stream().map(OrganizeEntity::getId).collect(Collectors.toList()));
            list1 = roleService.getListByIds(relationListByOrganizeId.stream().filter(t->PermissionConst.ROLE.equals(t.getObjectType())).map(OrganizeRelationEntity::getObjectId).collect(Collectors.toList()), null, false);
        } else {
            orgList = orgMaps.values();
            list1 = roleService.getList(true);
        }
        List<OrganizeRelationEntity> relationListByObjectIdAndType = organizeRelationService.getRelationListByType(PermissionConst.ROLE);
        for (RoleEntity roleEntity : list1) {
            relationListByObjectIdAndType = relationListByObjectIdAndType.stream().filter(t -> roleEntity.getId().equals(t.getObjectId())).collect(Collectors.toList());
            for (OrganizeRelationEntity entity : relationListByObjectIdAndType) {
                if (!orgMaps.containsKey(entity.getOrganizeId())) {
                    continue;
                }
                RoleModel roleVo = JsonUtil.getJsonToBean(roleEntity, RoleModel.class);
                // 必须加这个标识，不然前端会报错
                roleVo.setType("role");
                roleVo.setOnlyId(UUID.randomUUID().toString());
                roleVo.setParentId(entity.getOrganizeId());
                roleModels.add(roleVo);
            }
        }
        orgList.stream().forEach(org -> {
            RoleModel orgVo = JsonUtil.getJsonToBean(org, RoleModel.class);
            orgVo.setType(org.getCategory());
            orgVo.setIcon(StringUtil.isNotEmpty(org.getCategory()) ? "company".equals(org.getCategory()) ? "icon-ym icon-ym-tree-organization3" : "icon-ym icon-ym-tree-department1" : "");
            orgVo.setOnlyId(UUID.randomUUID().toString());
            roleModels.add(orgVo);
        });
        List<SumTree<RoleModel>> sumTrees = TreeDotUtils.convertListToTreeDot(roleModels);
        List<RoleSelectorVO> jsonToList = JsonUtil.getJsonToList(sumTrees, RoleSelectorVO.class);

        modelList.addAll(jsonToList);

        // 如果是管理员需要展示全局角色
        if (isAdministrator) {
            // 全局展示
            RoleSelectorVO globalParent = new RoleSelectorVO();
            String globalParentId = "1";
            globalParent.setFullName("全局");
            globalParent.setId("1");
            globalParent.setId(globalParentId);
            globalParent.setOnlyId("organizeList_0");
            List<RoleSelectorVO> globalModelList = JsonUtil.getJsonToList(roleService.getGlobalList(), RoleSelectorVO.class);
            globalModelList.forEach(g-> {
                g.setType("role");
                g.setOnlyId(UUID.randomUUID().toString());
                g.setParentId(globalParentId);
                g.setIcon("icon-ym icon-ym-generator-role");
            });
            globalParent.setHasChildren(globalModelList.size() > 0);
            globalParent.setChildren(globalModelList);
            globalParent.setIsLeaf(false);
            globalParent.setIcon("icon-ym icon-ym-global-role");
            modelList.add(globalParent);
        }
        ListVO vo = new ListVO();
        vo.setList(modelList);
        return ActionResult.success(vo);
    }

    /**
     * 获取角色信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取角色信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.role")
    @GetMapping("/{id}")
    public ActionResult<RoleInfoVO> getInfo(@PathVariable("id") String id) throws DataException {
        RoleEntity entity = roleService.getInfo(id);
        RoleInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, RoleInfoVO.class);
        // 通过组织角色关联表获取组织
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
        query.lambda().eq(OrganizeRelationEntity::getObjectId, id);

        List<String> ids = new ArrayList<>();
        organizeRelationService.list(query).forEach(relation->{
            ids.add(relation.getOrganizeId());
        });
        vo.setOrganizeIdsTree(PermissionUtil.getOrgIdsTree(ids, 1, organizeService));
        return ActionResult.success(vo);
    }



    /**
     * 新建角色
     *
     * @param roleCrForm 角色模型
     * @return
     */
    @RolePermission
    @Operation(summary = "新建角色")
    @Parameters({
            @Parameter(name = "roleCrForm", description = "角色模型", required = true)
    })
    @SaCheckPermission("permission.role")
    @PostMapping
    @Transactional
    public ActionResult<String> create(@RequestBody @Valid RoleCrForm roleCrForm) {
        RoleEntity entity = JsonUtil.getJsonToBean(roleCrForm, RoleEntity.class);
        if (roleService.isExistByFullName(roleCrForm.getFullName(), entity.getId(), roleCrForm.getGlobalMark())) {
            return ActionResult.fail("角色名称不能重复");
        }
        if (roleService.isExistByEnCode(roleCrForm.getEnCode(), entity.getId())) {
            return ActionResult.fail("角色编码不能重复");
        }

        // 角色的类型是啥，放organizeId，全局角色设置
        String roleId = RandomUtil.uuId();
        entity.setId(roleId);
        createOrganizeRoleRelation(roleCrForm.getOrganizeIdsTree(), roleId, roleCrForm.getGlobalMark());
        roleService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新角色
     *
     * @param id         主键值
     * @param roleUpForm roleUpForm
     * @return
     */
    @RolePermission
    @Operation(summary = "更新角色")
    @Parameters({
            @Parameter(name = "roleUpForm", description = "角色模型", required = true),
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.role")
    @PutMapping("/{id}")
    @Transactional
    public ActionResult<String> update(@RequestBody @Valid RoleUpForm roleUpForm, @PathVariable("id") String id) throws DataException {
        // 如果角色下面有用户则不允许修改角色类型和所属组织
        RoleEntity entity0 = roleService.getInfo(id);
        // 组织关系
        List<OrganizeRelationEntity> relationListByRoleId = organizeRelationService.getRelationListByRoleId(id);
        // 得到组织id
        if (entity0 != null) {
            List<UserRelationEntity> bingUserByRoleList = userRelationService.getListByObjectId(id, PermissionConst.ROLE);
            if (bingUserByRoleList.size() > 0) {
                // 修改角色类型
                if (!entity0.getGlobalMark().equals(roleUpForm.getGlobalMark())) {
                    return ActionResult.fail(MsgCode.FA023.get());
                }
                // 如果是修改状态的话
                if (!entity0.getEnabledMark().equals(roleUpForm.getEnabledMark())) {
                    return ActionResult.fail("更新失败！已绑定用户，无法修改状态");
                }

            }
        }

        if (roleUpForm.getGlobalMark() != 1) {
            List<String> beforeOrgIds = relationListByRoleId.stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList());
            List<String> updateOrgIds = PermissionUtil.getOrgIdsByFormTree(organizeService, roleUpForm.getOrganizeIdsTree());
            // 并集：所有未修改的ID
            List<String> unUpdateOrgIds = beforeOrgIds.stream().filter(updateOrgIds::contains).collect(Collectors.toList());
            // 差集：减少的ID
            beforeOrgIds.removeAll(unUpdateOrgIds);

            // 当角色绑定用户不让其更改角色所属组织
            String info = roleService.getBindInfo(id, beforeOrgIds);
            if(info != null){
                return ActionResult.fail(MsgCode.FA023.get());
            }
        }

        RoleEntity entity = JsonUtil.getJsonToBean(roleUpForm, RoleEntity.class);
        if (roleService.isExistByFullName(roleUpForm.getFullName(), id, roleUpForm.getGlobalMark())) {
            return ActionResult.fail("角色名称不能重复");
        }
        if (roleService.isExistByEnCode(roleUpForm.getEnCode(), id)) {
            return ActionResult.fail("角色编码不能重复");
        }
        boolean flag = roleService.update(id, entity);
        if (!flag) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        createOrganizeRoleRelation(roleUpForm.getOrganizeIdsTree(), id, roleUpForm.getGlobalMark());
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 添加组织角色关联关系
     */
    private Boolean createOrganizeRoleRelation(List<List<String>> organizeIdsTree, String roleId, Integer globalMark) {
        // 清除之前的关联关系
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
        query.lambda().eq(OrganizeRelationEntity::getObjectId, roleId);
        organizeRelationService.remove(query);
        // globalMark等于0时，为组织角色
        if(globalMark.equals(0)) {
            List<OrganizeRelationEntity> relationList = new ArrayList<>();
            for (List<String> organizeIds : organizeIdsTree) {
                // 组织id数组树最后一个数组最后一个id，是需要储存的id
                String organizeId = organizeIds.get(organizeIds.size() - 1);
                // 添加与组织的关联关系
                OrganizeRelationEntity organizeRelationEntity = new OrganizeRelationEntity();
                organizeRelationEntity.setId(RandomUtil.uuId());
                organizeRelationEntity.setOrganizeId(organizeId);
                organizeRelationEntity.setObjectType(PermissionConst.ROLE);
                organizeRelationEntity.setObjectId(roleId);
                relationList.add(organizeRelationEntity);
            }
            organizeRelationService.saveBatch(relationList);
        }
        return true;
    }

    /**
     * 删除角色
     *
     * @param id 主键值
     * @return
     */
    @RolePermission
    @Operation(summary = "删除角色")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.role")
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        // 当角色绑定用户不让其删除
        if(userRelationService.existByObj(PermissionConst.ROLE, id)){
            return ActionResult.fail(MsgCode.FA024.get());
        }
        RoleEntity entity = roleService.getInfo(id);
        if (entity != null) {
            List<UserRelationEntity> userRelList = userRelationService.getListByObjectId(id);
            if (userRelList.size() > 0) {
                return ActionResult.fail("该角色下有数据权限");
            }
            for (UserRelationEntity entity1 : userRelList) {
                UserEntity entity2 = userService.getById(entity1.getUserId());
                if (entity2 != null) {
                    String newRoleId = entity2.getRoleId().replace(id, "");
                    if (entity2.getRoleId().contains(id)) {
                        if (newRoleId.length() != 0 && newRoleId.substring(0, 1) == ",") {
                            entity2.setRoleId(newRoleId.substring(1));
                        } else if (newRoleId.length() != 0) {
                            entity2.setRoleId(newRoleId.replace(",,", ","));
                        }
                    }
                }
            }
            // 删除所有用户角色关联
            userRelationService.deleteAllByObjId(id);
            // 删除所有组织角色关联
            organizeRelationService.deleteAllByRoleId(id);
            roleService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 更新角色状态
     *
     * @param id 主键值
     * @return
     */
    @RolePermission
    @Operation(summary = "更新角色状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.role")
    @PutMapping("/{id}/Actions/State")
    public ActionResult<String> disable(@PathVariable("id") String id) {
        RoleEntity entity = roleService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            roleService.update(id, entity);
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.fail(MsgCode.FA007.get());
    }

    /**
     * 通过组织id获取岗位列表
     *
     * @param organizeIds 组织id数组
     * @return 角色列表
     */
    @Operation(summary = "获取角色列表通过组织id数组")
    @Parameters({
            @Parameter(name = "organizeIds", description = "组织id数组", required = true)
    })
    @SaCheckPermission("permission.user")
    @PostMapping("/getListByOrgIds")
    public ActionResult<ListVO<RoleModel>> getListByOrganizeIds(@RequestBody @Valid Map<String,List<String>> organizeIds) {
        List<RoleModel> modelAll = new ArrayList<>();

        for(String organizeId : organizeIds.get("organizeIds")){
            OrganizeEntity organizeEntity = organizeService.getInfo(organizeId);
            if(organizeEntity != null){
                RoleModel organizeModel = JsonUtil.getJsonToBean(organizeEntity, RoleModel.class);
                List<OrganizeRelationEntity> roleRelations = organizeRelationService.getListByTypeAndOrgId(PermissionConst.ROLE, organizeId);
                List<SumTree<RoleModel>> roleList = new ArrayList<>();
                for(OrganizeRelationEntity roleRelation : roleRelations){
                    RoleEntity roleEntity = roleService.getInfo(roleRelation.getObjectId());
                    // 非全局
                    if(roleEntity.getGlobalMark() == 0 && roleEntity.getEnabledMark() == 1){
                        RoleModel roleModel = JsonUtil.getJsonToBean(roleEntity, RoleModel.class);
                        roleModel.setHasChildren(false);
                        roleList.add(roleModel);
                    }
                }
                organizeModel.setHasChildren(true);
                organizeModel.setChildren(roleList);
                modelAll.add(organizeModel);
            }
        }

        // 获取全局角色
        List<SumTree<RoleModel>> models = new ArrayList<>();
        roleService.getGlobalList().forEach(r->{
            models.add(JsonUtil.getJsonToBean(r, RoleModel.class));
        });
        RoleModel modelGlobal = new RoleModel();
        modelGlobal.setFullName("全局");
        modelGlobal.setHasChildren(true);
        modelGlobal.setId("0");
        modelGlobal.setChildren(models);
        modelAll.add(modelGlobal);

        ListVO<RoleModel> vo = new ListVO<>();
        vo.setList(modelAll);
        return ActionResult.success(vo);
    }

    /**
     * 获取角色下拉框
     *
     * @param idModel 岗位选择模型
     * @return
     */
    @Operation(summary = "获取角色下拉框")
    @Parameters({
            @Parameter(name = "idModel", description = "ids", required = true)
    })
    @PostMapping("/RoleCondition")
    public ActionResult<ListVO<PositionSelectorVO>> roleCondition(@RequestBody UserIdModel idModel) {
        // 所有组织跟角色关联关系
        List<OrganizeRelationEntity> relationListByType = organizeRelationService.getRelationListByType(PermissionConst.ROLE);

        // 定义返回对象
        List<PositionSelectorVO> modelList = new ArrayList<>();
        List<String> roleIds = idModel.getIds();
        List<String> lists = organizeRelationService.getOrgIds(roleIds, PermissionConst.ROLE);
        List<String> list = new ArrayList<>(lists);
        List<String> roleIdList = new ArrayList<>();
        lists.forEach(t -> {
            List<OrganizeRelationEntity> collect = relationListByType.stream().filter(relationEntity -> relationEntity.getOrganizeId().equals(t)).collect(Collectors.toList());
            collect.forEach(relationEntity -> roleIdList.add(relationEntity.getObjectId()));
            List<OrganizeRelationEntity> collect1 = relationListByType.stream().filter(relationEntity -> relationEntity.getObjectId().equals(t)).collect(Collectors.toList());
            list.addAll(collect1.stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList()));
        });
        roleIds.addAll(roleIdList);

        // 全局
        if (UserProvider.getUser().getIsAdministrator() && (idModel.getIds().contains(JnpfConst.CURRENT_GRADE) || idModel.getIds().contains(JnpfConst.CURRENT_GRADE_TYPE))) {
            roleIds.addAll(roleService.getGlobalList().stream().map(RoleEntity::getId).collect(Collectors.toList()));
        }

        roleIds = roleIds.stream().distinct().collect(Collectors.toList());
        List<RoleEntity> roleEntityList = roleService.getListByIds(roleIds, null, true);
        List<PosOrgConditionModel> posOrgModels = new ArrayList<>(16);
        roleEntityList.forEach(t -> {
            if (t.getGlobalMark() != null && t.getGlobalMark() == 1) {
                PosOrgConditionModel posOrgModel = JsonUtil.getJsonToBean(t, PosOrgConditionModel.class);
                posOrgModel.setType("role");
                posOrgModel.setIcon("icon-ym icon-ym-generator-role");
                posOrgModel.setParentId("1");
                posOrgModel.setOnlyId(RandomUtil.uuId());
                posOrgModels.add(posOrgModel);
            } else {
                // 得到所有的组织id
                List<String> orgIds = relationListByType.stream().filter(relationEntity -> relationEntity.getObjectId().equals(t.getId()))
                        .map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList());
                orgIds.forEach(orgId -> {
                    if (orgIds.size() > 1) {
                        if (!list.contains(orgId)) {
                            return;
                        }
                    }
                    PosOrgConditionModel posOrgModel = JsonUtil.getJsonToBean(t, PosOrgConditionModel.class);
                    posOrgModel.setType("role");
                    posOrgModel.setIcon("icon-ym icon-ym-generator-role");
                    posOrgModel.setParentId(orgId);
                    posOrgModel.setOrganizeId(orgId);
                    posOrgModel.setOnlyId(RandomUtil.uuId());
                    posOrgModels.add(posOrgModel);
                });
            }
        });
        //获取所有组织
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        // 取出所有所属组织
        List<String> orgIds = posOrgModels.stream().filter(t -> StringUtil.isNotEmpty(t.getOrganizeId())).map(PosOrgConditionModel::getOrganizeId).collect(Collectors.toList());
        List<OrganizeEntity> orgEntityList = organizeService.getOrgEntityList(orgIds, true);
        orgEntityList.forEach(orgEntity -> {
            PosOrgConditionModel posOrgModel = JsonUtil.getJsonToBean(orgEntity, PosOrgConditionModel.class);
            if ("department".equals(orgEntity.getCategory())) {
                posOrgModel.setIcon("icon-ym icon-ym-tree-department1");
            } else if ("company".equals(orgEntity.getCategory())) {
                posOrgModel.setIcon("icon-ym icon-ym-tree-organization3");
            }
            // 处理断层
            if (StringUtil.isNotEmpty(orgEntity.getOrganizeIdTree())) {
                List<String> list1 = new ArrayList<>();
                String[] split = orgEntity.getOrganizeIdTree().split(",");
                list1 = Arrays.asList(split);
                Collections.reverse(list1);
                for (String orgId : list1) {
                    OrganizeEntity organizeEntity1 = orgEntityList.stream().filter(organizeEntity -> organizeEntity.getId().equals(orgId)).findFirst().orElse(null);
                    if (organizeEntity1 != null && !organizeEntity1.getId().equals(posOrgModel.getId())) {
                        posOrgModel.setParentId(organizeEntity1.getId());
                        String[] split1 = orgEntity.getOrganizeIdTree().split(organizeEntity1.getId());
                        if (split1.length > 1) {
                            posOrgModel.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                        }
                        break;
                    }
                }
            }
            posOrgModel.setOnlyId(RandomUtil.uuId());
            posOrgModels.add(posOrgModel);
        });

        if (posOrgModels.stream().anyMatch(posOrgConditionModel -> "1".equals(posOrgConditionModel.getParentId()))) {
            PosOrgConditionModel globalModel = new PosOrgConditionModel();
            globalModel.setId("1");
            globalModel.setParentId("-1");
            globalModel.setFullName("全局");
            globalModel.setIcon("icon-ym icon-ym-global-role");
            globalModel.setOnlyId("organizeList_0");
            posOrgModels.add(globalModel);
        }
        List<SumTree<PosOrgConditionModel>> trees = TreeDotUtils2.convertListToTreeDot(posOrgModels);
        List<PositionSelectorVO> positionSelectorVO = JsonUtil.getJsonToList(trees, PositionSelectorVO.class);
        // 处理数据
        positionSelectorVO.forEach(t -> {
            if (!"0".equals(t.getId()) && !"1".equals(t.getId()) && !"role".equals(t.getType())) {
                t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
            }
        });
        modelList.addAll(positionSelectorVO);

        ListVO vo = new ListVO();
        vo.setList(modelList);
        return ActionResult.success(vo);
    }

    
    @PostMapping("/getListByUserId")
    @NoDataSourceBind
    public List<RoleEntity> getListByUserId(@RequestBody RoleInfoModel roleInfoModel) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(roleInfoModel.getTenantId());
        }
        return roleService.getListByUserId(roleInfoModel.getId());
    }

    
    @PostMapping("/getByUserId")
    public List<RoleEntity> getByUserId(@RequestParam("userId") String userId) {
        return roleService.getListByUserId(userId);
    }

    
    @GetMapping("/getInfoById/{id}")
    public RoleEntity getInfoById(@PathVariable("id") String id) {
        return roleService.getInfo(id);
    }

    
    @GetMapping("/getRoleIdsByCurrentUser")
    public List<String> getRoleIdsByCurrentUser() {
        return roleService.getRoleIdsByCurrentUser();
    }

    
    @PostMapping("/getListByIds")
    public List<RoleEntity> getListByIds(@RequestBody List<String> roleIds) {
        return roleService.getListByIds(roleIds, null, false);
    }

    
    @GetMapping("/getAllRoleIdsByUserIdAndOrgId")
    public List<String> getAllRoleIdsByUserIdAndOrgId(@RequestParam("userId")String userId, @RequestParam("orgId")String orgId) {
        return roleService.getAllRoleIdsByUserIdAndOrgId(userId,orgId);
    }

    
    @GetMapping("/getListAll")
    public List<RoleEntity> getListAll() {
        return roleService.getList(false);
    }

    
    @GetMapping("/getRoleMap")
    public Map<String, Object> getRoleMap(@RequestParam ("type")String type) {
        if ("id-fullName".equals(type)){
            return roleService.getRoleMap();
        } else {
            return roleService.getRoleNameAndIdMap();
        }
    }
}
