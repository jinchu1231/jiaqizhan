package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.annotation.UserPermission;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.SynThirdDingTalkService;
import jnpf.base.service.SynThirdQyService;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.DataException;
import jnpf.exception.ImportException;
import jnpf.message.service.SynThirdInfoService;
import jnpf.model.FlowWorkListVO;
import jnpf.model.FlowWorkModel;
import jnpf.model.tenant.AdminInfoVO;
import jnpf.model.tenant.TenantReSetPasswordForm;
import jnpf.permission.entity.GroupEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.user.ImUserListVo;
import jnpf.permission.model.user.PageUser;
import jnpf.permission.model.user.PaginationUser;
import jnpf.permission.model.user.UserAllVO;
import jnpf.permission.model.user.UserByRoleModel;
import jnpf.permission.model.user.UserByRoleVO;
import jnpf.permission.model.user.UserConditionModel;
import jnpf.permission.model.user.UserCrForm;
import jnpf.permission.model.user.UserExportExceptionVO;
import jnpf.permission.model.user.UserExportVO;
import jnpf.permission.model.user.UserIdListVo;
import jnpf.permission.model.user.UserIdModel;
import jnpf.permission.model.user.UserIdModelByPage;
import jnpf.permission.model.user.UserImportVO;
import jnpf.permission.model.user.UserInfoModel;
import jnpf.permission.model.user.UserInfoVO;
import jnpf.permission.model.user.UserListVO;
import jnpf.permission.model.user.UserModel;
import jnpf.permission.model.user.UserResetPasswordForm;
import jnpf.permission.model.user.UserSelectorModel;
import jnpf.permission.model.user.UserSelectorVO;
import jnpf.permission.model.user.UserUpForm;
import jnpf.permission.model.user.UserUpdateModel;
import jnpf.permission.model.user.UsersByPositionModel;
import jnpf.permission.model.user.WorkHandoverModel;
import jnpf.permission.rest.PullUserUtil;
import jnpf.permission.service.GroupService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PermissionGroupService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.permission.util.PermissionUtil;
import jnpf.service.AuthService;
import jnpf.util.CacheKeyUtil;
import jnpf.util.ExcelUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.Md5Util;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UpUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static jnpf.util.Constants.ADMIN_KEY;

/**
 * 用户管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "用户管理", description = "Users")
@Slf4j
@RestController
@RequestMapping("/api/permission/Users")
public class UserController extends SuperController<UserService, UserEntity> {

    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private SynThirdQyService synThirdQyService;
    @Autowired
    private SynThirdDingTalkService synThirdDingTalkService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;
    @Autowired
    private ConfigValueUtil configValueUtil;

    /*=== the same ===*/

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private RoleService roleService;

    /**
     * 取出线程池
     */
    @Autowired
    private Executor threadPoolExecutor;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private PermissionGroupService permissionGroupService;
    @Autowired
    private AuthService authService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    /**
     * 获取用户列表
     *
     * @param pagination 分页参数
     * @return ignore
     */
    @Operation(summary = "获取用户列表")
    @GetMapping
    public ActionResult<PageListVO<UserListVO>> getList(PaginationUser pagination) {
        List<UserEntity> userList = userService.getList(pagination, pagination.getOrganizeId(), false, true, pagination.getEnabledMark(), pagination.getGender());
        List<UserListVO> list = new ArrayList<>();
        // 得到性别
        List<DictionaryDataEntity> dataServiceList4 = dictionaryDataService.getListByTypeDataCode("sex");
        Map<String, String> dataServiceMap4 = dataServiceList4.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getEnCode, DictionaryDataEntity::getFullName));
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        for (UserEntity userEntity : userList) {
            UserListVO userVO = JsonUtil.getJsonToBean(userEntity, UserListVO.class);
            userVO.setHandoverMark(userEntity.getHandoverMark() == null ? 0 : userEntity.getHandoverMark());
            userVO.setHeadIcon(UploaderUtil.uploaderImg(userVO.getHeadIcon()));
            // 时间小于当前时间则判断已解锁
            if (userVO.getEnabledMark() != null && userVO.getEnabledMark() != 0) {
                if (Objects.nonNull(userEntity.getUnlockTime()) && userEntity.getUnlockTime().getTime() > System.currentTimeMillis()) {
                    userVO.setEnabledMark(2);
                } else if (Objects.nonNull(userEntity.getUnlockTime()) && userEntity.getUnlockTime().getTime() < System.currentTimeMillis()) {
                    userVO.setEnabledMark(1);
                }
            }
            List<UserRelationEntity> orgRelationByUserId = userRelationService.getAllOrgRelationByUserId(userEntity.getId());
            // 储存组织id信息
            StringJoiner stringJoiner = new StringJoiner(",");
            for (UserRelationEntity userRelationEntity : orgRelationByUserId) {
                // 获取组织id详情
                OrganizeEntity entity = organizeService.getInfo(userRelationEntity.getObjectId());
                if (entity != null) {
                    // 获取到组织树
                    String organizeIdTree = entity.getOrganizeIdTree();
                    if (StringUtil.isNotEmpty(organizeIdTree)) {
                        stringJoiner.add(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeIdTree, "/"));
                    }
                }
            }
            userVO.setGender(dataServiceMap4.get(userEntity.getGender()));
            userVO.setOrganize(stringJoiner.toString());
            list.add(userVO);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取用户列表
     *
     * @return ignore
     */
    @Operation(summary = "获取所有用户列表")
    @GetMapping("/All")
    public ActionResult<ListVO<UserAllVO>> getAllUsers(Pagination pagination) {
        List<UserEntity> list = userService.getList(pagination, null, false, false, 0, null);
        List<UserAllVO> user = JsonUtil.getJsonToList(list, UserAllVO.class);
        ListVO<UserAllVO> vo = new ListVO<>();
        vo.setList(user);
        return ActionResult.success(vo);
    }

    /**
     * IM通讯获取用户接口
     *
     * @param pagination 分页参数
     * @return ignore
     */
    @Operation(summary = "IM通讯获取用户")
    @GetMapping("/ImUser")
    public ActionResult<PageListVO<ImUserListVo>> getAllImUserUsers(Pagination pagination) {
        List<UserEntity> data = userService.getList(pagination);
        List<ImUserListVo> list = new ArrayList<>();
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrganizeName(data.stream().map(t -> t.getOrganizeId()).collect(Collectors.toList()), null, false, null);
        for (UserEntity entity : data) {
            ImUserListVo user = JsonUtil.getJsonToBean(entity, ImUserListVo.class);
            OrganizeEntity organize = orgMaps.get(entity.getOrganizeId());
            user.setDepartment(organize != null ? organize.getFullName() : "");
            user.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
            list.add(user);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取用户下拉框列表
     *
     * @return ignore
     */
    @Operation(summary = "获取用户下拉框列表")
    @SaCheckPermission("permission.user")
    @GetMapping("/Selector")
    public ActionResult<ListVO<UserSelectorVO>> selector() {
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, true, null);
        List<OrganizeEntity> organizeData = new ArrayList<>(orgMaps.values());
        List<UserEntity> userData = userService.getList(true);
        List<UserSelectorModel> treeList = JsonUtil.getJsonToList(organizeData, UserSelectorModel.class);
        for (UserSelectorModel entity1 : treeList) {
            if ("department".equals(entity1.getType())) {
                entity1.setIcon("icon-ym icon-ym-tree-department1");
            } else if ("company".equals(entity1.getType())) {
                entity1.setIcon("icon-ym icon-ym-tree-organization3");
            }
        }
        for (UserEntity entity : userData) {
            UserSelectorModel treeModel = new UserSelectorModel();
            treeModel.setId(entity.getId());
            treeModel.setParentId(entity.getOrganizeId());
            treeModel.setFullName(entity.getRealName() + "/" + entity.getAccount());
            treeModel.setType("user");
            treeModel.setIcon("icon-ym icon-ym-tree-user2");
            treeList.add(treeModel);
        }
        List<SumTree<UserSelectorModel>> trees = TreeDotUtils.convertListToTreeDot(treeList);
        List<UserSelectorVO> listvo = JsonUtil.getJsonToList(trees, UserSelectorVO.class);
        List<OrganizeEntity> entities = organizeData.stream().filter(
                t -> "-1".equals(t.getParentId())
        ).collect(Collectors.toList());
        Iterator<UserSelectorVO> iterator = listvo.iterator();
        while (iterator.hasNext()) {
            UserSelectorVO userSelectorVO = iterator.next();
            for (OrganizeEntity entity : entities) {
                if (entity.getId().equals(userSelectorVO.getParentId())) {
                    iterator.remove();//使用迭代器的删除方法删除
                }
            }
        }
        ListVO<UserSelectorVO> vo = new ListVO<>();
        vo.setList(listvo);
        return ActionResult.success(vo);
    }

    /**
     * 通过部门、岗位、用户、角色、分组id获取用户列表
     *
     * @param userConditionModel 用户选择模型
     * @return
     */
    @Operation(summary = "通过部门、岗位、用户、角色、分组id获取用户列表")
    @Parameters({
            @Parameter(name = "userConditionModel", description = "用户选择模型", required = true)
    })
    @PostMapping("/UserCondition")
    public ActionResult<PageListVO<UserIdListVo>> userCondition(@RequestBody UserConditionModel userConditionModel) {
        List<String> list = new ArrayList<>(16);
        if (userConditionModel.getDepartIds() != null) {
            list.addAll(userConditionModel.getDepartIds());
        }
        if (userConditionModel.getRoleIds() != null) {
            list.addAll(userConditionModel.getRoleIds());
        }
        if (userConditionModel.getPositionIds() != null) {
            list.addAll(userConditionModel.getPositionIds());
        }
        if (userConditionModel.getGroupIds() != null) {
            list.addAll(userConditionModel.getGroupIds());
        }
        if (list.size() == 0) {
            list = userRelationService.getListByObjectType(userConditionModel.getType()).stream().map(UserRelationEntity::getObjectId).distinct().collect(Collectors.toList());
            if (PermissionConst.GROUP.equals(userConditionModel.getType())) {
                List<GroupEntity> groupList = groupService.getListByIds(list, true);
                list = groupList.stream().map(GroupEntity::getId).collect(Collectors.toList());
            }
            if (PermissionConst.ORGANIZE.equals(userConditionModel.getType())) {
                List<OrganizeEntity> orgList = organizeService.getOrgEntityList(list, true);
                list = orgList.stream().map(OrganizeEntity::getId).collect(Collectors.toList());
            }
            if (PermissionConst.ROLE.equals(userConditionModel.getType())) {
                List<RoleEntity> roleList = roleService.getListByIds(list, null, false);
                list = roleList.stream().filter(t -> t.getEnabledMark() == 1).map(RoleEntity::getId).collect(Collectors.toList());
            }
            if (PermissionConst.POSITION.equals(userConditionModel.getType())) {
                List<PositionEntity> positionList = positionService.getPosList(list);
                list = positionList.stream().filter(t -> t.getEnabledMark() == 1).map(PositionEntity::getId).collect(Collectors.toList());
            }
        }
        List<String> collect = userRelationService.getListByObjectIdAll(list).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
        if (userConditionModel.getUserIds() != null) {
            collect.addAll(userConditionModel.getUserIds());
        }
        collect = collect.stream().distinct().collect(Collectors.toList());
        List<UserEntity> userName = userService.getUserName(collect, userConditionModel.getPagination());
        List<UserIdListVo> jsonToList = JsonUtil.getJsonToList(userName, UserIdListVo.class);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        jsonToList.forEach(t -> {
            t.setHeadIcon(UploaderUtil.uploaderImg(t.getHeadIcon()));
            t.setFullName(t.getRealName() + "/" + t.getAccount());
            List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(t.getId(), PermissionConst.ORGANIZE);
            List<String> orgId = listByUserId.stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList());
            List<OrganizeEntity> organizeName = new ArrayList<>(organizeService.getOrganizeName(orgId, null, false, null).values());
            StringBuilder stringBuilder = new StringBuilder();
            organizeName.forEach(org -> {
                if (StringUtil.isNotEmpty(org.getOrganizeIdTree())) {
                    String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, org.getOrganizeIdTree(), "/");
                    stringBuilder.append(",");
                    stringBuilder.append(fullNameByOrgIdTree);
                }
            });
            if (stringBuilder.length() > 0) {
                t.setOrganize(stringBuilder.toString().replaceFirst(",", ""));
            }
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(userConditionModel.getPagination(), PaginationVO.class);
        return ActionResult.page(jsonToList, paginationVO);
    }

    /**
     * 获取用户下拉框列表
     *
     * @param organizeIdForm 组织id
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取用户下拉框列表")
    @Parameters({
            @Parameter(name = "organizeId", description = "组织id", required = true),
            @Parameter(name = "pagination", description = "分页模型", required = true)
    })
    @PostMapping("/ImUser/Selector/{organizeId}")
    public ActionResult<?> imUserSelector(@PathVariable("organizeId") String organizeIdForm, @RequestBody Pagination pagination) {
        String organizeId = XSSEscape.escape(organizeIdForm);
        List<UserSelectorVO> jsonToList = new ArrayList<>();
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, true, null);
        //判断是否搜索关键字
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            //通过关键字查询
            List<UserEntity> list = userService.getList(pagination, false);
            //遍历用户给要返回的值插入值
            for (UserEntity entity : list) {
                UserSelectorVO vo = JsonUtil.getJsonToBean(entity, UserSelectorVO.class);
                vo.setParentId(entity.getOrganizeId());
                vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                vo.setType("user");
                vo.setIcon("icon-ym icon-ym-tree-user2");
                vo.setHeadIcon(UploaderUtil.uploaderImg(vo.getHeadIcon()));
                List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(entity.getId()).stream().filter(t -> t != null && PermissionConst.ORGANIZE.equals(t.getObjectType())).collect(Collectors.toList());
                StringJoiner stringJoiner = new StringJoiner(",");
                listByUserId.forEach(t -> {
                    OrganizeEntity organizeEntity = orgMaps.get(t.getObjectId());
                    if (organizeEntity != null) {
                        String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                        if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                            stringJoiner.add(fullNameByOrgIdTree);
                        }
                    }
                });
                vo.setOrganize(stringJoiner.toString());
                vo.setHasChildren(false);
                vo.setIsLeaf(true);
                jsonToList.add(vo);
            }
            PaginationVO jsonToBean = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
            return ActionResult.page(jsonToList, jsonToBean);
        }
        //获取所有组织
        List<OrganizeEntity> collect = new ArrayList<>(orgMaps.values());
        //判断时候传入组织id
        //如果传入组织id，则取出对应的子集
        if (!"0".equals(organizeId)) {
            //通过组织查询部门及人员
            //单个组织
            OrganizeEntity organizeEntity = orgMaps.get(organizeId);
            if (organizeEntity != null) {
                //取出组织下的部门
                List<OrganizeEntity> collect1 = collect.stream().filter(t -> t.getParentId().equals(organizeEntity.getId())).collect(Collectors.toList());
                for (OrganizeEntity entitys : collect1) {
                    UserSelectorVO vo = JsonUtil.getJsonToBean(entitys, UserSelectorVO.class);
                    if ("department".equals(entitys.getCategory())) {
                        vo.setIcon("icon-ym icon-ym-tree-department1");
                    } else if ("company".equals(entitys.getCategory())) {
                        vo.setIcon("icon-ym icon-ym-tree-organization3");
                    }
                    vo.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, entitys.getOrganizeIdTree(), "/"));
                    // 判断组织下是否有人
                    jsonToList.add(vo);
                    vo.setHasChildren(true);
                    vo.setIsLeaf(false);
                }
                //取出组织下的人员
                List<UserEntity> entityList = userService.getListByOrganizeId(organizeId, null);
                for (UserEntity entity : entityList) {
                    if ("0".equals(String.valueOf(entity.getEnabledMark()))) {
                        continue;
                    }
                    UserSelectorVO vo = JsonUtil.getJsonToBean(entity, UserSelectorVO.class);
                    vo.setParentId(organizeId);
                    vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                    vo.setType("user");
                    vo.setIcon("icon-ym icon-ym-tree-user2");
                    List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(entity.getId()).stream().filter(t -> t != null && PermissionConst.ORGANIZE.equals(t.getObjectType())).collect(Collectors.toList());
                    StringBuilder stringBuilder = new StringBuilder();
                    listByUserId.forEach(t -> {
                        OrganizeEntity organizeEntity1 = orgMaps.get(t.getObjectId());
                        if (organizeEntity1 != null) {
                            String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity1.getOrganizeIdTree(), "/");
                            if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                                stringBuilder.append("," + fullNameByOrgIdTree);
                            }
                        }
                    });
                    if (stringBuilder.length() > 0) {
                        vo.setOrganize(stringBuilder.toString().replaceFirst(",", ""));
                    }
                    vo.setHeadIcon(UploaderUtil.uploaderImg(vo.getHeadIcon()));
                    vo.setHasChildren(false);
                    vo.setIsLeaf(true);
                    jsonToList.add(vo);
                }
            }
            ListVO<UserSelectorVO> vo = new ListVO<>();
            vo.setList(jsonToList);
            return ActionResult.success(vo);
        }

        //如果没有组织id，则取出所有组织
        jsonToList = JsonUtil.getJsonToList(collect.stream().filter(t -> "-1".equals(t.getParentId())).collect(Collectors.toList()), UserSelectorVO.class);
        //添加图标
        for (UserSelectorVO userSelectorVO : jsonToList) {
            userSelectorVO.setIcon("icon-ym icon-ym-tree-organization3");
            userSelectorVO.setHasChildren(true);
            userSelectorVO.setIsLeaf(false);
            userSelectorVO.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, orgMaps.get(userSelectorVO.getId()).getOrganizeIdTree(), "/"));
        }
        ListVO<UserSelectorVO> vo = new ListVO<>();
        vo.setList(jsonToList);
        return ActionResult.success(vo);
    }

    /**
     * 获取用户下拉框列表
     *
     * @param organizeId 组织id
     * @param page 关键字
     * @return
     */
    @Operation(summary = "获取用户下拉框列表")
    @Parameters({
            @Parameter(name = "organizeId", description = "组织id", required = true),
            @Parameter(name = "page", description = "关键字", required = true)
    })
    @SaCheckPermission("permission.grade")
    @PostMapping("/GetListByAuthorize/{organizeId}")
    public ActionResult<ListVO<UserByRoleVO>> imUserSelectors(@PathVariable("organizeId") String organizeId, @RequestBody Page page) {
        List<UserByRoleVO> jsonToList = userService.getListByAuthorize(organizeId, page);
        ListVO listVO = new ListVO();
        listVO.setList(jsonToList);
        return ActionResult.success(listVO);
    }

    /**
     * 获取用户信息
     *
     * @param id 用户id
     * @return ignore
     */
    @Operation(summary = "获取用户信息")
    @Parameters({
            @Parameter(name = "id", description = "用户id", required = true)
    })
    @SaCheckPermission("permission.user")
    @GetMapping("/{id}")
    public ActionResult<UserInfoVO> getInfo(@PathVariable("id") String id) throws DataException {
        UserEntity entity = userService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail("用户不存在");
        }
        if (entity.getEnabledMark() != 0) {
            if (Objects.nonNull(entity.getUnlockTime()) && entity.getUnlockTime().getTime() > System.currentTimeMillis()) {
                entity.setEnabledMark(2);
            } else if (Objects.nonNull(entity.getUnlockTime()) && entity.getUnlockTime().getTime() < System.currentTimeMillis()) {
                entity.setEnabledMark(1);
            }
        }

        QueryWrapper<UserRelationEntity> roleQuery = new QueryWrapper<>();
        roleQuery.lambda().eq(UserRelationEntity::getUserId, id);
        roleQuery.lambda().eq(UserRelationEntity::getObjectType, PermissionConst.ROLE);
        List<String> roleIdList = new ArrayList<>();
        for (UserRelationEntity ure : userRelationService.list(roleQuery)) {
            roleIdList.add(ure.getObjectId());
        }

        entity.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
        // 得到组织树
        UserInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, UserInfoVO.class);
        vo.setRoleId(String.join(",", roleIdList));


        // 获取组织id数组
        QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(UserRelationEntity::getUserId, id);
        query.lambda().eq(UserRelationEntity::getObjectType, PermissionConst.ORGANIZE);
        List<String> organizeIds = new ArrayList<>();
        userRelationService.list(query).forEach(u -> {
            organizeIds.add(u.getObjectId());
        });

        // 岗位装配
        QueryWrapper<UserRelationEntity> positionQuery = new QueryWrapper<>();
        positionQuery.lambda().eq(UserRelationEntity::getUserId, id);
        positionQuery.lambda().eq(UserRelationEntity::getObjectType, PermissionConst.POSITION);
        String positionIds = "";
        for (UserRelationEntity ure : userRelationService.list(positionQuery)) {
            PositionEntity info = positionService.getInfo(ure.getObjectId());
            if (info != null) {
                positionIds = positionIds + "," + ure.getObjectId();
            }
        }
        if (positionIds.length() > 0) {
            vo.setPositionId(positionIds.substring(1));
        } else {
            vo.setPositionId(null);
        }
        // 设置分组id
//        List<UserRelationEntity> listByObjectType = userRelationService.getListByObjectType(entity.getId(), PermissionConst.GROUP);
//        StringBuilder groupId = new StringBuilder();
//        listByObjectType.stream().forEach(t->groupId.append("," + t.getObjectId()));
//        if (groupId.length() > 0) {
//            vo.setGroupId(groupId.toString().replaceFirst(",", ""));
//        }
        vo.setOrganizeIdTree(PermissionUtil.getOrgIdsTree(organizeIds, 1, organizeService));
        return ActionResult.success(vo);
    }

    /**
     * 新建用户
     *
     * @param userCrForm 表单参数
     */
    @UserPermission
    @Operation(summary = "新建用户")
    @Parameters({
            @Parameter(name = "userCrForm", description = "表单参数", required = true)
    })
    @SaCheckPermission("permission.user")
    @PostMapping
    public ActionResult<String> create(@RequestBody @Valid UserCrForm userCrForm) throws Exception {
        UserEntity entity = JsonUtil.getJsonToBean(userCrForm, UserEntity.class);
        entity.setPassword("4a7d1ed414474e4033ac29ccb8653d9b");
        if (userService.isExistByAccount(userCrForm.getAccount())) {
            return ActionResult.fail("账户名称不能重复");
        }
        // 如果账号被锁定
        if ("2".equals(String.valueOf(entity.getEnabledMark()))) {
            entity.setUnlockTime(null);
            entity.setLogErrorCount(0);
        }
        userService.create(entity);
        threadPoolExecutor.execute(() -> {
            try {
                //添加用户之后判断是否需要同步到企业微信
                synThirdQyService.createUserSysToQy(false, entity, "");
                //添加用户之后判断是否需要同步到钉钉
                synThirdDingTalkService.createUserSysToDing(false, entity, "");
            } catch (Exception e) {
                log.error("添加用户之后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
            }
        });
        String catchKey = cacheKeyUtil.getAllUser();
        if (redisUtil.exists(catchKey)) {
            redisUtil.remove(catchKey);
        }
        entity.setPassword(Md5Util.getStringMd5("0000"));
        PullUserUtil.syncUser(entity, "create", userProvider.get().getTenantId());
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 修改用户
     *
     * @param userUpForm 表单参数
     * @param id         主键值
     */
    @UserPermission
    @Operation(summary = "修改用户")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "userUpForm", description = "表单参数", required = true)
    })
    @SaCheckPermission("permission.user")
    @PutMapping("/{id}")
    public ActionResult<String> update(@PathVariable("id") String id, @RequestBody @Valid UserUpForm userUpForm) throws Exception {
        UserEntity entity = JsonUtil.getJsonToBean(userUpForm, UserEntity.class);
        //将禁用的id加进数据
        UserEntity originUser = userService.getInfo(id);
        // 如果是管理员的话
        if ("1".equals(String.valueOf(originUser.getIsAdministrator()))) {
            UserInfo operatorUser = userProvider.get();
            // 管理员可以修改自己，但是无法修改其他管理员
            if (operatorUser.getIsAdministrator()) {
                if (originUser.getEnabledMark() != 0 && entity.getEnabledMark() == 0) {
                    return ActionResult.fail("无法禁用管理员用户");
                }
                if (!ADMIN_KEY.equals(userService.getInfo(operatorUser.getUserId()).getAccount())) {
                    if (!operatorUser.getUserId().equals(id)) {
                        return ActionResult.fail("管理员只能修改自己，不能修改其他管理员");
                    }
                }
            } else {
                return ActionResult.fail("无法修改管理员账户");
            }
        }
        //直属主管不能是自己
        if (id.equals(userUpForm.getManagerId())) {
            return ActionResult.fail("直属主管不能是自己");
        }
        if(!originUser.getAccount().equals(entity.getAccount())){
            if (userService.isExistByAccount(entity.getAccount())) {
                return ActionResult.fail("账户名称不能重复");
            }
        }
        // 验证是否有十级,验证是否是自己的下属
        boolean subordinate = userService.isSubordinate(id, userUpForm.getManagerId());
        if (subordinate) {
            return ActionResult.fail("直属主管不能是我的下属用户");
        }
        // 如果账号被锁定
        if ("2".equals(String.valueOf(entity.getEnabledMark()))) {
            entity.setUnlockTime(null);
            entity.setLogErrorCount(0);
        }
        // 如果原来是锁定，现在不锁定，则置空错误次数
        if (originUser.getEnabledMark() == 2 && entity.getEnabledMark() == 1) {
            entity.setUnlockTime(null);
            entity.setLogErrorCount(0);
        }
        //获取头像
        String oldHeadIcon = entity.getHeadIcon();
        if (StringUtil.isEmpty(oldHeadIcon)) {
            entity.setHeadIcon("001.png");
        } else {
            //获取头像
            String[] headIcon = oldHeadIcon.split("/");
            if (headIcon.length > 0) {
                entity.setHeadIcon(headIcon[headIcon.length - 1]);
            }
        }
        boolean flag = userService.update(id, entity);
        threadPoolExecutor.execute(() -> {
            try {
                //修改用户之后判断是否需要同步到企业微信
                synThirdQyService.updateUserSysToQy(false, entity, "");
                //修改用户之后判断是否需要同步到钉钉
                synThirdDingTalkService.updateUserSysToDing(false, entity, "");
            } catch (Exception e) {
                log.error("修改用户之后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
            }
        });
        if (!flag) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        // 删除在线的用户
        userService.delCurUser(null, id);
        PullUserUtil.syncUser(entity, "update", userProvider.get().getTenantId());
        return ActionResult.success(MsgCode.SU004.get());
    }


    /**
     * 删除用户
     *
     * @param id 主键值
     * @return ignore
     */
    @UserPermission
    @Operation(summary = "删除用户")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.user")
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getIsAdministrator()))) {
                return ActionResult.fail("无法删除管理员账户");
            }
            //判断是否是部门主管
            if (organizeService.getList(false).stream().filter(t -> id.equals(t.getManagerId())).collect(Collectors.toList()).size() > 0) {
                return ActionResult.fail("此用户为某部门主管，无法删除");
            }
            // 有下属不允许删除
            if (userService.getListByManagerId(id, null).size() > 0) {
                return ActionResult.fail("此用户有下属，无法删除");
            }
            String tenantId = StringUtil.isEmpty(userProvider.get().getTenantId()) ? "" : userProvider.get().getTenantId();
            String catchKey = tenantId + "allUser";
            if (redisUtil.exists(catchKey)) {
                redisUtil.remove(catchKey);
            }
            userService.delete(entity);
            threadPoolExecutor.execute(() -> {
                try {
                    //删除用户之后判断是否需要同步到企业微信
                    synThirdQyService.deleteUserSysToQy(false, id, "");
                    //删除用户之后判断是否需要同步到钉钉
                    synThirdDingTalkService.deleteUserSysToDing(false, id, "");
                } catch (Exception e) {
                    log.error("删除用户之后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
                }
            });
            userService.delCurUser(null, entity.getId());
            PullUserUtil.syncUser(entity, "delete", userProvider.get().getTenantId());
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }


    /**
     * 修改用户密码
     *
     * @param id 主键
     * @param userResetPasswordForm 修改密码模型
     * @return ignore
     */
    @UserPermission
    @Operation(summary = "修改用户密码")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "userResetPasswordForm", description = "修改密码模型", required = true)
    })
    @SaCheckPermission("permission.user")
    @PostMapping("/{id}/Actions/ResetPassword")
    public ActionResult<String> modifyPassword(@PathVariable("id") String id, @RequestBody @Valid UserResetPasswordForm userResetPasswordForm) {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            entity.setPassword(userResetPasswordForm.getUserPassword());
            userService.updatePassword(entity);
            userService.delCurUser("密码已变更，请重新登录！", entity.getId());
            // 推送时使用表单中的密码
            entity.setPassword(userResetPasswordForm.getUserPassword());
            PullUserUtil.syncUser(entity, "modifyPassword", userProvider.get().getTenantId());
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.success("操作失败,用户不存在");
    }

    /**
     * 更新用户状态
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "更新用户状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.user")
    @PutMapping("/{id}/Actions/State")
    public ActionResult<String> disable(@PathVariable("id") String id) throws Exception {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getIsAdministrator()))) {
                return ActionResult.fail("无法修改管理员账户状态");
            }
            if (entity.getEnabledMark() != null) {
                if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                    entity.setEnabledMark(0);
                    userService.delCurUser(null, entity.getId());
                    userService.update(id, entity);
                } else {
                    entity.setEnabledMark(1);
                    userService.update(id, entity);
                }
            } else {
                entity.setEnabledMark(1);
                userService.update(id, entity);
            }
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.success("操作失败,用户不存在");
    }

    /**
     * 解除锁定
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "解除锁定")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.user")
    @PutMapping("/{id}/Actions/unlock")
    public ActionResult<String> unlock(@PathVariable("id") String id) throws Exception {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            // 状态变成正常
            entity.setEnabledMark(1);
            entity.setUnlockTime(null);
            entity.setLogErrorCount(0);
            entity.setId(id);
            userService.updateById(entity);
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.success("操作失败,用户不存在");
    }

    /**
     * 获取用户基本信息
     *
     * @param userIdModel 用户id
     * @return ignore
     */
    @Operation(summary = "获取用户基本信息")
    @Parameters({
            @Parameter(name = "userIdModel", description = "用户id", required = true)
    })
    @PostMapping("/getUserList")
    public ActionResult<ListVO<UserIdListVo>> getUserList(@RequestBody UserIdModel userIdModel) {
        List<UserEntity> userName = userService.getUserName(userIdModel.getIds(), true);
        List<UserIdListVo> list = JsonUtil.getJsonToList(userName, UserIdListVo.class);
        List<UserRelationEntity> listByUserIds = userRelationService.getRelationByUserIds(list.stream().map(UserIdListVo::getId).collect(Collectors.toList()));
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, true, null);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        for (UserIdListVo entity : list) {
            if (entity == null) {
                break;
            }
            entity.setFullName(entity.getRealName() + "/" + entity.getAccount());
            List<UserRelationEntity> listByUserId = listByUserIds.stream().filter(t -> t.getUserId().equals(entity.getId())).collect(Collectors.toList());
            StringJoiner stringJoiner = new StringJoiner(",");
            listByUserId.forEach(t -> {
                OrganizeEntity organizeEntity = orgMaps.get(t.getObjectId());
                if (organizeEntity != null) {
                    String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                    if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                        stringJoiner.add(fullNameByOrgIdTree);
                    }
                }
            });
            entity.setOrganize(stringJoiner.toString());
            entity.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
        }
        ListVO<UserIdListVo> listVO = new ListVO<>();
        listVO.setList(list);
        return ActionResult.success(listVO);
    }

    /**
     * 获取选中组织、岗位、角色、用户基本信息
     *
     * @param userIdModel 用户id
     * @return ignore
     */
    @Operation(summary = "获取选中组织、岗位、角色、用户基本信息")
    @Parameters({
            @Parameter(name = "userIdModel", description = "用户id", required = true)
    })
    @PostMapping("/getSelectedList")
    public ActionResult<ListVO<UserIdListVo>> getSelectedList(@RequestBody UserIdModel userIdModel) {
        List<String> ids = userIdModel.getIds();
        List<UserIdListVo> list = userService.selectedByIds(ids);
        ListVO<UserIdListVo> listVO = new ListVO<>();
        listVO.setList(list);
        return ActionResult.success(listVO);
    }



    /**
     * 获取用户基本信息
     *
     * @param userIdModel 用户id
     * @return ignore
     */
    @Operation(summary = "获取选中用户基本信息")
    @Parameters({
            @Parameter(name = "userIdModel", description = "用户id", required = true)
    })
    @PostMapping("/getSelectedUserList")
    public ActionResult<PageListVO<UserIdListVo>> getSelectedUserList(@RequestBody UserIdModelByPage userIdModel) {
        List<UserIdListVo> jsonToList = userService.getObjList(userIdModel.getIds(), userIdModel.getPagination(), null);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(userIdModel.getPagination(), PaginationVO.class);
        return ActionResult.page(jsonToList, paginationVO);
    }

    /**
     * 获取组织下的人员
     *
     * @param page 页面信息
     * @return ignore
     */
    @Operation(summary = "获取组织下的人员")
    @GetMapping("/getOrganization")
    public ActionResult<List<UserIdListVo>> getOrganization(PageUser page) {
        String departmentId = page.getOrganizeId();
        // 判断是否获取当前组织下的人员
        if ("0".equals(departmentId)) {
            departmentId = userProvider.get().getDepartmentId();
            // 为空则取组织id
            if (StringUtil.isEmpty(departmentId)) {
                departmentId = userProvider.get().getOrganizeId();
            }
        }
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, true, null);
        List<UserEntity> list = userService.getListByOrganizeId(departmentId, page.getKeyword());
        List<UserIdListVo> jsonToList = JsonUtil.getJsonToList(list, UserIdListVo.class);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        jsonToList.forEach(t -> {
            t.setRealName(t.getRealName() + "/" + t.getAccount());
            t.setFullName(t.getRealName());
            List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(t.getId()).stream().filter(ur -> ur != null && PermissionConst.ORGANIZE.equals(ur.getObjectType())).collect(Collectors.toList());
            StringJoiner stringJoiner = new StringJoiner(",");
            listByUserId.forEach(tt -> {
                OrganizeEntity organizeEntity = orgMaps.get(tt.getObjectId());
                if (organizeEntity != null) {
                    String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                    if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                        stringJoiner.add(fullNameByOrgIdTree);
                    }
                }
            });
            t.setOrganize(stringJoiner.toString());
            t.setHeadIcon(UploaderUtil.uploaderImg(t.getHeadIcon()));
        });
        return ActionResult.success(jsonToList);
    }

    /**
     * 获取岗位人员
     *
     * @param page 页面信息
     * @return ignore
     */
    @Operation(summary = "获取岗位人员")
    @SaCheckPermission("permission.position")
    @GetMapping("/GetUsersByPositionId")
    public ActionResult<List<UserByRoleVO>> getUsersByPositionId(UsersByPositionModel page) {
        List<UserByRoleVO> list = new ArrayList<>(1);
        String keyword = page.getKeyword();
        // 岗位id
        String positionId = page.getPositionId();
        // 得到关联的组织id
        PositionEntity positionEntity = positionService.getInfo(positionId);
        if (positionEntity != null) {
            UserByRoleVO vo = new UserByRoleVO();
            String organizeId = positionEntity.getOrganizeId();
            // 得到组织信息
            OrganizeEntity organizeEntity = organizeService.getInfo(organizeId);
            if (Objects.nonNull(organizeEntity)) {
                vo.setId(organizeEntity.getId());
                vo.setType(organizeEntity.getCategory());
                if ("department".equals(organizeEntity.getCategory())) {
                    vo.setIcon("icon-ym icon-ym-tree-department1");
                } else {
                    vo.setIcon("icon-ym icon-ym-tree-organization3");
                }
                vo.setEnabledMark(organizeEntity.getEnabledMark());
                Map<String, String> orgIdNameMaps = organizeService.getInfoList();
                // 组装组织名称
                String orgName = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                vo.setFullName(orgName);
                // 赋予子集
                List<UserByRoleVO> userByRoleVOS = new ArrayList<>(16);
                List<UserEntity> lists = userService.getListByOrganizeId(organizeEntity.getId(), keyword);
                if (lists.size() > 0) {
                    vo.setHasChildren(true);
                    vo.setIsLeaf(false);
                    lists.stream().forEach(t->{
                        UserByRoleVO userByRoleVO = new UserByRoleVO();
                        userByRoleVO.setParentId(organizeEntity.getId());
                        userByRoleVO.setId(t.getId());
                        userByRoleVO.setFullName(t.getRealName() + "/" + t.getAccount());
                        userByRoleVO.setEnabledMark(t.getEnabledMark());
                        userByRoleVO.setHeadIcon(UploaderUtil.uploaderImg(t.getHeadIcon()));
                        userByRoleVO.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/"));
                        userByRoleVO.setIsLeaf(true);
                        userByRoleVO.setHasChildren(false);
                        userByRoleVO.setIcon("icon-ym icon-ym-tree-user2");
                        userByRoleVO.setType("user");
                        userByRoleVOS.add(userByRoleVO);
                    });
                    vo.setChildren(userByRoleVOS);
                } else {
                    vo.setHasChildren(false);
                    vo.setIsLeaf(true);
                    vo.setChildren(new ArrayList<>());
                }
                list.add(vo);
            }
        }
        return ActionResult.success(list);
    }

    /**
     * 角色成员弹窗
     *
     * @param model 页面信息
     * @return ignore
     */
    @Operation(summary = "角色成员弹窗")
    @SaCheckPermission("permission.role")
    @GetMapping("/GetUsersByRoleOrgId")
    public ActionResult<List<UserByRoleVO>> getUsersByRoleOrgId(UserByRoleModel model) {
        List<UserByRoleVO> jsonToList = new ArrayList<>(16);
        // 得到组织关系
        List<OrganizeRelationEntity> relationListByRoleId = organizeRelationService.getRelationListByRoleId(model.getRoleId());
        // 得到组织信息
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrganizeName(relationListByRoleId.stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList()), null, true, null);
        List<OrganizeEntity> collect = new ArrayList<>(orgMaps.values());
        List<String> orgEntityList = new ArrayList<>(orgMaps.values()).stream().map(OrganizeEntity::getId).collect(Collectors.toList());
        //获取所有组织
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        //判断是否搜索关键字
        if (StringUtil.isNotEmpty(model.getKeyword())) {
            //通过关键字查询
            List<UserEntity> list = userService.getList(orgEntityList, model.getKeyword());
            List<UserRelationEntity> listByUserIds = userRelationService.getRelationByUserIds(list.stream().map(UserEntity::getId).collect(Collectors.toList()));
            //遍历用户给要返回的值插入值
            for (UserEntity entity : list) {
                UserByRoleVO vo = new UserByRoleVO();
                vo.setId(entity.getId());
                vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                vo.setEnabledMark(entity.getEnabledMark());
                vo.setIsLeaf(true);
                vo.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
                List<UserRelationEntity> listByUserId = listByUserIds.stream().filter(t -> t.getUserId().equals(entity.getId())).collect(Collectors.toList());
                StringBuilder stringBuilder = new StringBuilder();
                List<OrganizeEntity> orgEntityLists = organizeService.getOrgEntityList(listByUserId.stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList()), false);
                listByUserId.forEach(t -> {
                    OrganizeEntity orgEntity = orgEntityLists.stream().filter(org -> org.getId().equals(t.getObjectId())).findFirst().orElse(null);
                    if (orgEntity != null) {
                        String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, orgEntity.getOrganizeIdTree(), "/");
                        if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                            stringBuilder.append("," + fullNameByOrgIdTree);
                        }
                    }
                });
                if (stringBuilder.length() > 0) {
                    vo.setOrganize(stringBuilder.toString().replaceFirst(",", ""));
                }
                vo.setHasChildren(false);
                vo.setIcon("icon-ym icon-ym-tree-user2");
                vo.setType("user");
                jsonToList.add(vo);
            }
            return ActionResult.success(jsonToList);
        }
        //判断时候传入组织id
        //如果传入组织id，则取出对应的子集
        if (!"0".equals(model.getOrganizeId())) {
            //通过组织查询部门及人员
            //单个组织
            List<OrganizeEntity> list = collect.stream().filter(t -> model.getOrganizeId().equals(t.getId())).collect(Collectors.toList());
            if (list.size() > 0) {
                //获取组织信息
                OrganizeEntity organizeEntity = list.get(0);
                //取出组织下的部门
                List<OrganizeEntity> collect1 = collect.stream().filter(t -> t.getParentId().equals(organizeEntity.getId())).collect(Collectors.toList());
                // 判断组织关系中是否有子部门id
                List<OrganizeEntity> organizeEntities = new ArrayList<>();
                for (OrganizeEntity entity : collect1) {
                    List<OrganizeRelationEntity> collect2 = relationListByRoleId.stream().filter(t -> entity.getId().equals(t.getOrganizeId())).collect(Collectors.toList());
                    collect2.stream().forEach(t->{
                        if (StringUtil.isNotEmpty(t.getOrganizeId())) {
                            organizeEntities.add(orgMaps.get(t.getOrganizeId()));
                        }
                    });
                }
                // 其他不是子集的直接显示
                List<OrganizeRelationEntity> collect2 = relationListByRoleId.stream()
                        .filter(item -> !organizeEntities.stream().map(e -> e.getId())
                                .collect(Collectors.toList()).contains(item.getOrganizeId()))
                        .collect(Collectors.toList());
                // 移除掉上级不是同一个的
                List<OrganizeRelationEntity> collect3 = collect2.stream().filter(t -> !orgMaps.get(t.getOrganizeId()).getOrganizeIdTree().contains(model.getOrganizeId())).collect(Collectors.toList());
                collect2.removeAll(collect3);
                List<OrganizeRelationEntity> collect4 = collect2.stream().filter(t -> !t.getOrganizeId().equals(model.getOrganizeId())).collect(Collectors.toList());
                List<OrganizeEntity> collect5 = collect.stream().filter(x -> collect4.stream().map(OrganizeRelationEntity::getOrganizeId).collect(Collectors.toList()).contains(x.getId())).collect(Collectors.toList());
                List<OrganizeEntity> organizeEntities1 = new ArrayList<>(collect5);
                // 不是子集的对比子集的
                for (OrganizeEntity entity : collect5) {
                    for (OrganizeEntity organizeEntity1 : organizeEntities) {
                        if (entity.getOrganizeIdTree().contains(organizeEntity1.getId())) {
                            organizeEntities1.remove(entity);
                        }
                    }
                }

                //取出组织下的人员
                List<UserEntity> entityList = userService.getListByOrganizeId(model.getOrganizeId(), null);
                List<UserRelationEntity> listByUserIds = userRelationService.getRelationByUserIds(entityList.stream().map(UserEntity::getId).collect(Collectors.toList()));
                for (UserEntity entity : entityList) {
                    UserByRoleVO vo = new UserByRoleVO();
                    vo.setId(entity.getId());
                    vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                    vo.setEnabledMark(entity.getEnabledMark());
                    vo.setIsLeaf(true);
                    vo.setHasChildren(false);
                    vo.setIcon("icon-ym icon-ym-tree-user2");
                    vo.setType("user");
                    vo.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
                    List<UserRelationEntity> listByUserId = listByUserIds.stream().filter(t -> t.getUserId().equals(entity.getId())).collect(Collectors.toList());
                    StringBuilder stringBuilder = new StringBuilder();
                    List<OrganizeEntity> orgEntityLists = organizeService.getOrgEntityList(listByUserId.stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList()), false);
                    listByUserId.forEach(t -> {
                        OrganizeEntity orgEntity = orgEntityLists.stream().filter(org -> org.getId().equals(t.getObjectId())).findFirst().orElse(null);
                        if (orgEntity != null) {
                            String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, orgEntity.getOrganizeIdTree(), "/");
                            if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                                stringBuilder.append("," + fullNameByOrgIdTree);
                            }
                        }
                    });
                    if (stringBuilder.length() > 0) {
                        vo.setOrganize(stringBuilder.toString().replaceFirst(",", ""));
                    }
                    jsonToList.add(vo);
                }
                // 处理子集断层
                List<OrganizeEntity> organizeEntities2 = new ArrayList<>(collect5);
                for (OrganizeEntity entity : organizeEntities2) {
                    List<OrganizeEntity> collect6 = organizeEntities2.stream().filter(t -> !entity.getId().equals(t.getId()) && t.getOrganizeIdTree().contains(entity.getOrganizeIdTree())).collect(Collectors.toList());
                    organizeEntities1.removeAll(collect6);
                }
                for (OrganizeEntity entity : organizeEntities1) {
                    UserByRoleVO userByRoleVO = new UserByRoleVO();
                    userByRoleVO.setId(entity.getId());
                    userByRoleVO.setType(entity.getCategory());
                    userByRoleVO.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, entity.getOrganizeIdTree(), "/"));
                    if ("department".equals(entity.getCategory())) {
                        userByRoleVO.setIcon("icon-ym icon-ym-tree-department1");
                    } else {
                        userByRoleVO.setIcon("icon-ym icon-ym-tree-organization3");
                    }
                    userByRoleVO.setHasChildren(true);
                    userByRoleVO.setIsLeaf(false);
                    userByRoleVO.setEnabledMark(entity.getEnabledMark());
                    jsonToList.add(userByRoleVO);
                }
                for (OrganizeEntity entitys : organizeEntities) {
                    UserByRoleVO vo = new UserByRoleVO();
                    vo.setId(entitys.getId());
                    vo.setType(entitys.getCategory());
                    vo.setFullName(entitys.getFullName());
                    if ("department".equals(entitys.getCategory())) {
                        vo.setIcon("icon-ym icon-ym-tree-department1");
                    } else {
                        vo.setIcon("icon-ym icon-ym-tree-organization3");
                    }
                    vo.setHasChildren(true);
                    vo.setIsLeaf(false);
                    vo.setEnabledMark(entitys.getEnabledMark());
                    jsonToList.add(vo);
                }
            }
            return ActionResult.success(jsonToList);
        }

        // 判断是否有父级
        Set<OrganizeEntity> set = new HashSet<>(16);
        for (OrganizeEntity entity : collect) {
            List<OrganizeEntity> collect1 = collect.stream().filter(t -> !entity.getId().equals(t.getId()) && entity.getOrganizeIdTree().contains(t.getOrganizeIdTree())).collect(Collectors.toList());
            set.addAll(collect1);
        }
        List<OrganizeEntity> list = new ArrayList<>(set);
        // 从list中一处已经有的
        List<OrganizeEntity> list1 = new ArrayList<>(list);
        for (OrganizeEntity organizeEntity : list) {
            List<OrganizeEntity> collect1 = list.stream().filter(t -> !organizeEntity.getId().equals(t.getId()) && t.getOrganizeIdTree().contains(organizeEntity.getId())).collect(Collectors.toList());
            list1.removeAll(collect1);
        }
        list = list1;
        // 纯断层的
        List<OrganizeEntity> list2 = new ArrayList<>(collect);
        for (OrganizeEntity organizeEntity: collect){
            if (list.stream().filter(t -> organizeEntity.getOrganizeIdTree().contains(t.getId())).count() > 0) {
                list2.remove(organizeEntity);
            }
        }
        list.addAll(list2);
        for (OrganizeEntity organizeEntity : list) {
            if (organizeEntity != null && organizeEntity.getEnabledMark() == 1) {
                UserByRoleVO userByRoleVO = new UserByRoleVO();
                userByRoleVO.setId(organizeEntity.getId());
                userByRoleVO.setType(organizeEntity.getCategory());
                String orgName = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                userByRoleVO.setFullName(orgName);
                if ("department".equals(organizeEntity.getCategory())) {
                    userByRoleVO.setIcon("icon-ym icon-ym-tree-department1");
                } else {
                    userByRoleVO.setIcon("icon-ym icon-ym-tree-organization3");
                }
                userByRoleVO.setHasChildren(true);
                userByRoleVO.setIsLeaf(false);
                userByRoleVO.setEnabledMark(organizeEntity.getEnabledMark());
                jsonToList.add(userByRoleVO);
            }
        }
        return ActionResult.success(jsonToList);
    }

    /**
     * 获取我的下属(不取子集)
     *
     * @param page 页面信息
     * @return ignore
     */
    @Operation(summary = "获取我的下属(不取子集)")
    @Parameters({
            @Parameter(name = "page", description = "关键字", required = true)
    })
    @PostMapping("/getSubordinates")
    public ActionResult<List<UserIdListVo>> getSubordinates(@RequestBody Page page) {
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, false, null);
        List<UserEntity> list = userService.getListByManagerId(userProvider.get().getUserId(), page.getKeyword());
        List<UserIdListVo> jsonToList = JsonUtil.getJsonToList(list, UserIdListVo.class);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        jsonToList.forEach(t -> {
            t.setRealName(t.getRealName() + "/" + t.getAccount());
            t.setFullName(t.getRealName());
            t.setHeadIcon(UploaderUtil.uploaderImg(t.getHeadIcon()));
            List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(t.getId()).stream().filter(ur -> PermissionConst.ORGANIZE.equals(ur.getObjectType())).collect(Collectors.toList());
            StringJoiner stringJoiner = new StringJoiner(",");
            listByUserId.forEach(tt -> {
                OrganizeEntity organizeEntity = orgMaps.get(tt.getObjectId());
                if (organizeEntity != null) {
                    String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                    if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                        stringJoiner.add(fullNameByOrgIdTree);
                    }
                }
            });
            t.setHeadIcon(UploaderUtil.uploaderImg(t.getHeadIcon()));
            t.setOrganize(stringJoiner.toString());
        });
        return ActionResult.success(jsonToList);
    }

    /**
     * 导出excel
     *
     * @param dataType   导出方式
     * @param selectKey 选择列
     * @param pagination 分页
     * @return ignore
     */
    @Operation(summary = "导出excel")
    @SaCheckPermission("permission.user")
    @GetMapping("/ExportData")
    public ActionResult<DownloadVO> Export(String dataType, String selectKey, PaginationUser pagination) {
        // 导出
        DownloadVO vo = userService.exportExcel(dataType, selectKey, pagination);
        return ActionResult.success(vo);
    }

    /**
     * 模板下载
     *
     * @return ignore
     */
    @Operation(summary = "模板下载")
    @SaCheckPermission("permission.user")
    @GetMapping("/TemplateDownload")
    public ActionResult<DownloadVO> TemplateDownload() {
        UserInfo userInfo = userProvider.get();
        DownloadVO vo = DownloadVO.builder().build();
        try {
            vo.setName("用户信息.xlsx");
            vo.setUrl(UploaderUtil.uploaderFile("/api/file/DownloadModel?encryption=", "用户信息" +
                    ".xlsx" + "#" + "Temporary"));
        } catch (Exception e) {
            log.error("信息导出Excel错误:" + e.getMessage());
        }
        return ActionResult.success(vo);
    }



    /**
     * 导入数据
     *
     * @param data 导入模型
     * @return ignore
     */
    @Operation(summary = "导入数据")
    @Parameters({
            @Parameter(name = "data", description = "导入模型", required = true)
    })
    @SaCheckPermission("permission.user")
    @PostMapping("/ImportData")
    public ActionResult<UserImportVO> ImportData(@RequestBody UserExportVO data) {
        List<UserExportVO> dataList = JsonUtil.getJsonToList(data.getList(), UserExportVO.class);
        //导入数据
        UserImportVO result = userService.importData(dataList);
        return ActionResult.success(result);
    }

    /**
     * 导出错误报告
     *
     * @param data 导出模型
     * @return ignore
     */
    @Operation(summary = "导出错误报告")
    @Parameters({
            @Parameter(name = "data", description = "导出模型", required = true)
    })
    @SaCheckPermission("permission.user")
    @PostMapping("/ExportExceptionData")
    public ActionResult<DownloadVO> exportExceptionData(@RequestBody UserExportExceptionVO data) {
        List<UserExportExceptionVO> dataList = JsonUtil.getJsonToList(data.getList(), UserExportExceptionVO.class);
        //生成Excel
        DownloadVO vo = userService.exportExceptionData(dataList);
        return ActionResult.success(vo);
    }

    /*= different =*/


    /**
     * 上传文件(excel)
     *
     * @return ignore
     */
    @Operation(summary = "上传文件")
    @SaCheckPermission("permission.user")
    @PostMapping("/Uploader")
    public ActionResult<Object> Uploader() {
        List<MultipartFile> list = UpUtil.getFileAll();
        MultipartFile file = list.get(0);
        if (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls")) {
            String filePath = XSSEscape.escape(configValueUtil.getTemporaryFilePath());
            String fileName = XSSEscape.escape(RandomUtil.uuId() + "." + UpUtil.getFileType(file));
            // 上传文件
            FileInfo fileInfo = FileUploadUtils.uploadFile(file, filePath, fileName);
            DownloadVO vo = DownloadVO.builder().build();
            vo.setName(fileInfo.getFilename());
            return ActionResult.success(vo);
        } else {
            return ActionResult.fail("选择文件不符合导入");
        }
    }

    /**
     * 导入预览
     *
     * @param fileName 文件名
     * @return
     */
    @Operation(summary = "导入预览")
    @SaCheckPermission("permission.user")
    @GetMapping("/ImportPreview")
    public ActionResult ImportPreview(String fileName) throws ImportException {
        try {
            // 获取文件
            String filePath = configValueUtil.getTemporaryFilePath();
            @Cleanup InputStream inputStream = new ByteArrayInputStream(FileUploadUtils.downloadFileByte(filePath, fileName, false));
            // 得到数据
            List<UserExportVO> personList = ExcelUtil.importExcelByInputStream(inputStream, 0, 1, UserExportVO.class);
            // 预览数据
            Map<String, Object> map = userService.importPreview(personList);
            return ActionResult.success(map);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ImportException(e.getMessage());
        }
    }

    /**
     * 根据角色ID获取所属组织的所有成员
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "根据角色ID获取所有成员")
    @SaCheckPermission("permission.role")
    @GetMapping("/getUsersByRoleId")
    public ActionResult getUsersByRoleId(PaginationUser pagination){
        List<UserEntity> userList = new ArrayList<>();
        if(roleService.getInfo(pagination.getRoleId()).getGlobalMark() == 1){
            userList.addAll(userService.getList(pagination, null, false, false, 0, null));
        } else {
            // 根据roleId获取所有组织
            userService.getListByRoleId(pagination.getRoleId()).forEach(u->{
                userList.add(userService.getInfo(u.getId()));
            });
        }
        // 去重
        List<UserEntity> afterUserList = userList.stream().distinct().collect(Collectors.toList());
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            afterUserList = afterUserList.stream().filter(t -> t.getRealName().contains(pagination.getKeyword()) || t.getAccount().contains(pagination.getKeyword())).collect(Collectors.toList());
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(afterUserList, paginationVO);
    }

    /**
     * 获取默认当前值用户ID
     *
     * @param userConditionModel 参数
     * @return 执行结构
     * @throws DataException ignore
     */
    @Operation(summary = "获取默认当前值用户ID")
    @Parameters({
            @Parameter(name = "userConditionModel", description = "参数", required = true)
    })
    @PostMapping("/getDefaultCurrentValueUserId")
    public ActionResult<?> getDefaultCurrentValueUserId(@RequestBody UserConditionModel userConditionModel) throws DataException {
        String userId = getDefaultCurrentUserId(userConditionModel);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("userId", userId);
        return ActionResult.success("查询成功", dataMap);
    }

    /**
     * 工作交接
     *
     * @param workHandoverModel 模型
     * @return 执行结构
     */
    @Operation(summary = "工作交接")
    @SaCheckPermission("permission.user")
    @Parameters({
            @Parameter(name = "workHandoverModel", description = "模型", required = true)
    })
    @PostMapping("/workHandover")
    public ActionResult<?> workHandover(@RequestBody @Valid WorkHandoverModel workHandoverModel) {
        // 开始交接就禁用用户
        UserEntity entity = userService.getInfo(workHandoverModel.getFromId());
        UserEntity entitys = userService.getInfo(workHandoverModel.getToId());
        if (entity == null || entitys == null) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
//        if (entity.getIsAdministrator() == 1 || entitys.getIsAdministrator() == 1) {
//            return ActionResult.fail("工作交接无法转移给管理员");
//        }
        try {
            boolean flag = flowTaskService.flowWork(workHandoverModel);
            if (!flag) {
                return ActionResult.fail("工作交接失败！");
            }
            permissionGroupService.updateByUser(workHandoverModel.getFromId(), workHandoverModel.getToId(), workHandoverModel.getPermissionList());
            entity.setHandoverMark(1);
            return ActionResult.success("工作交接成功！");
        } finally {
            userService.updateById(entity);
        }
    }

    /**
     * 获取用户工作详情
     *
     * @return 执行结构
     */
    @Operation(summary = "获取用户工作详情")
    @SaCheckPermission("permission.user")
    @Parameters({
            @Parameter(name = "userId", description = "主键", required = true)
    })
    @GetMapping("/getWorkByUser")
    public ActionResult<FlowWorkListVO> getWorkByUser(@RequestParam("fromId") String fromId) {
        FlowWorkListVO flowWorkListVO = flowTaskService.flowWork(fromId);
        if (flowWorkListVO == null) {
            log.error("用户：" + UserProvider.getLoginUserId() + "，待办事宜及负责流程获取失败");
            flowWorkListVO = new FlowWorkListVO();
        }
        List<PermissionGroupEntity> permissionGroupByUserId = permissionGroupService.getPermissionGroupAllByUserId(fromId);
        List<FlowWorkModel> jsonToList = JsonUtil.getJsonToList(permissionGroupByUserId, FlowWorkModel.class);
        jsonToList.forEach(t -> t.setIcon("icon-ym icon-ym-authGroup"));
        flowWorkListVO.setPermission(jsonToList);
        return ActionResult.success(flowWorkListVO);
    }


    // ----------------------------- 多租户调用
    /**
     * 重置管理员密码
     *
     * @param userResetPasswordForm 修改密码模型
     * @return ignore
     */
    @UserPermission
    @Operation(summary = "重置管理员密码")
    @Parameters({
            @Parameter(name = "userResetPasswordForm", description = "修改密码模型", required = true)
    })
    @PutMapping("/Tenant/ResetPassword")
    @NoDataSourceBind
    public ActionResult<String> resetPassword(@RequestBody @Valid TenantReSetPasswordForm userResetPasswordForm) {
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(userResetPasswordForm.getTenantId());
        }
        UserEntity entity = userService.getUserByAccount("admin");
        if (entity != null) {
            entity.setPassword(userResetPasswordForm.getUserPassword());
            userService.updatePassword(entity);
            userService.delCurUser("密码已变更，请重新登录！", entity.getId());
            PullUserUtil.syncUser(entity, "modifyPassword", userResetPasswordForm.getTenantId());
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.fail("操作失败,用户不存在");
    }

    /**
     * 获取用户信息
     *
     * @param tenantId 租户号
     * @return ignore
     */
    @Operation(summary = "获取用户信息")
    @Parameters({
            @Parameter(name = "tenantId", description = "租户号", required = true)
    })
    @NoDataSourceBind
    @GetMapping("/Tenant/AdminInfo")
    public AdminInfoVO adminInfo(@RequestParam("tenantId") String tenantId) throws DataException {
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        UserEntity entity = userService.getUserByAccount("admin");
        AdminInfoVO adminInfoVO = JsonUtil.getJsonToBean(entity, AdminInfoVO.class);
        return adminInfoVO;
    }

    /**
     * 修改管理员信息
     *
     * @param adminInfoVO 模型
     * @return ignore
     */
    @Operation(summary = "修改管理员信息")
    @Parameters({
            @Parameter(name = "adminInfoVO", description = "模型", required = true)
    })
    @NoDataSourceBind
    @PutMapping("/Tenant/UpdateAdminInfo")
    public ActionResult adminInfo(@RequestBody AdminInfoVO adminInfoVO) throws DataException {
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(adminInfoVO.getTenantId());
        }
        UserEntity entity = userService.getUserByAccount("admin");
        if (entity == null) {
            return ActionResult.fail("操作失败,用户不存在");
        }
        entity.setRealName(adminInfoVO.getRealName());
        entity.setMobilePhone(adminInfoVO.getMobilePhone());
        entity.setEmail(adminInfoVO.getEmail());
        userService.updateById(entity);
        threadPoolExecutor.execute(() -> {
            try {
                //修改用户之后判断是否需要同步到企业微信
                synThirdQyService.updateUserSysToQy(false, entity, "");
                //修改用户之后判断是否需要同步到钉钉
                synThirdDingTalkService.updateUserSysToDing(false, entity, "");
            } catch (Exception e) {
                log.error("修改用户之后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
            }
        });
        // 删除在线的用户
        PullUserUtil.syncUser(entity, "update", adminInfoVO.getTenantId());
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 移除租户账号在线用户
     *
     * @param tenantId 租户号
     * @return ignore
     */
    @Operation(summary = "移除租户账号在线用户")
    @Parameters({
            @Parameter(name = "tenantId", description = "租户号", required = true)
    })
    @NoDataSourceBind
    @GetMapping("/Tenant/RemoveOnlineByTenantId")
    public void removeOnlineByTenantId(@RequestParam("tenantId") String tenantId) throws DataException {
        List<String> tokenList = new ArrayList<>();
        List<String> tokens = UserProvider.getLoginUserListToken();
        tokens.forEach(token -> {
            UserInfo userInfo = UserProvider.getUser(token);
            if (tenantId.equals(userInfo.getTenantId())) {
                tokenList.add(token);
            }
        });
        authService.kickoutByToken(Arrays.toString(tokenList.toArray(new String[0])), null, null);
    }


    /**
     * OpenFeign调用 --- 勿删！！！
     */


    
    @PostMapping("/getUserName")
    public List<UserEntity> getUserName(@RequestBody List<String> id) {
        return userService.getUserNameList(id);
    }

    
    @PostMapping("/getUserNamePagination")
    public List<UserEntity> getUserNamePagination(@RequestBody UserModel userModel) {
        return userService.getUserName(userModel.getId(),userModel.getPagination());
    }

    
    @GetMapping("/getListId")
    public List<String> getListId() {
        return userService.getListId();
    }

    
    @NoDataSourceBind
    @PostMapping("/getInfoByAccount")
    public UserEntity getInfoByAccount(@RequestBody UserInfoModel userInfoModel) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(userInfoModel.getTenantId());
        }
        return userService.getUserByAccount(userInfoModel.getUserId());
    }

    
    @NoDataSourceBind
    @PostMapping("/getInfoByUserId")
    public UserEntity getInfoByUserId(UserInfoModel userInfoModel) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(userInfoModel.getTenantId());
        }
        return userService.getInfo(userInfoModel.getUserId());
    }

    
    @PostMapping("/updateById")
    @NoDataSourceBind
    public Boolean updateById(@RequestBody UserUpdateModel userUpdateModel) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(userUpdateModel.getTenantId());
        }
        return userService.updateById(userUpdateModel.getEntity());
    }

    
    @GetMapping("/getInfoByAccount/{account}")
    public UserEntity getInfoByAccount(@PathVariable("account") String account) {
        return userService.getUserByAccount(account);
    }

    
    @GetMapping("/getInfoById/{id}")
    public UserEntity getInfoById(@PathVariable("id") String id) {
        return userService.getInfo(id);
    }

    
    @NoDataSourceBind
    @GetMapping("/getInfoByIdInMessage")
    public UserEntity getInfoByIdInMessage(@RequestParam("id") String id) {
        return userService.getInfo(id);
    }

    
    @GetMapping("/getListByManagerId/{id}")
    public List<UserEntity> getListByManagerId(@PathVariable("id") String id) {
        return userService.getListByManagerId(id, null);
    }

    
    @GetMapping("/getList")
    public List<UserEntity> getList(@RequestParam("enabledMark") boolean enabledMark) {
        return userService.getList(false);
    }

    
    @GetMapping("/getAdminList")
    public List<UserEntity> getAdminList() {
        return userService.getAdminList();
    }

    
    @PostMapping("/setAdminListByIds")
    public Boolean setAdminListByIds(@RequestBody List<String> adminIds) {
        return userService.setAdminListByIds(adminIds);
    }

    
    @GetMapping("/getByRealName/{fullName}")
    public UserEntity getByRealName(@PathVariable("fullName") String fullName){
        return userService.getByRealName(fullName);
    }

    
    @PostMapping("/updateUserById")
    public Boolean updateUserById(@RequestBody UserEntity userEntity){
        return userService.updateById(userEntity);
    }

    
    @PostMapping("/update/{id}")
    public Boolean update(@PathVariable("id") String id, @RequestBody UserEntity entity) throws Exception {
        return userService.update(id, entity);
    }

    
    @GetMapping("/getUserMap")
    public Map<String, Object> getUserMap(@RequestParam ("type") String type) {
        return "id-fullName".equals(type) ?  userService.getUserMap() : userService.getUserNameAndIdMap();
    }

    
    @GetMapping("/getInfoByMobile/{mobile}")
    public UserEntity getInfoByMobile(@PathVariable("mobile") String mobile) {
        return userService.getUserByMobile(mobile);
    }

    
    @PostMapping("/create")
    public Boolean create(@RequestBody UserEntity userEntity) throws Exception {
        return userService.create(userEntity);
    }

    
    @PostMapping("/delete")
    public void delete(@RequestBody UserEntity userEntity) {
        userService.delete(userEntity);
    }

    
    @PostMapping("/getUserByRoleList/{organizeId}")
    public List<UserByRoleVO> getUserByRoleList(@PathVariable("organizeId") String organizeId) {
        List<UserByRoleVO> list = userService.getListByAuthorize(organizeId, new Page());
        return list;
    }

    
    @PostMapping("/getUserIdList")
    public List<String> getUserIdList(@RequestBody List<String> userIds) {
        return userService.getUserIdList(userIds, null);
    }

    
    @PostMapping("/getDefaultCurrentUserId")
    public String getDefaultCurrentUserId(@RequestBody UserConditionModel userConditionModel) throws DataException {
        return userService.getDefaultCurrentUserId(userConditionModel);
    }

    
    @PostMapping("/selectedByIds")
    public List<UserIdListVo> selectedByIds(@RequestBody List<String> ids) {
        return userService.selectedByIds(ids);
    }

    
    @PostMapping("/getUserNameMark")
    public List<UserEntity> getUserNameMark(@RequestBody List<String> id, @RequestParam(name = "getUserNameMark", required = false) boolean filterEnabledMark) {
        return userService.getUserName(id,filterEnabledMark);
    }
}
