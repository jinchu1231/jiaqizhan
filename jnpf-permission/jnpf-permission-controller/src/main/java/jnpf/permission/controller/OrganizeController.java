package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.annotation.OrganizePermission;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.ListVO;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.exception.DataException;
import jnpf.message.service.SynThirdInfoService;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.model.organize.OrganizeByAuthModel;
import jnpf.permission.model.organize.OrganizeConditionModel;
import jnpf.permission.model.organize.OrganizeCrForm;
import jnpf.permission.model.organize.OrganizeDepartCrForm;
import jnpf.permission.model.organize.OrganizeDepartInfoVO;
import jnpf.permission.model.organize.OrganizeDepartSelectorListVO;
import jnpf.permission.model.organize.OrganizeDepartUpForm;
import jnpf.permission.model.organize.OrganizeInfoVO;
import jnpf.permission.model.organize.OrganizeListVO;
import jnpf.permission.model.organize.OrganizeModel;
import jnpf.permission.model.organize.OrganizeSelectorByAuthVO;
import jnpf.permission.model.organize.OrganizeSelectorVO;
import jnpf.permission.model.organize.OrganizeTreeVO;
import jnpf.permission.model.organize.OrganizeUpForm;
import jnpf.permission.model.organize.PaginationOrganize;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import jnpf.util.CacheKeyUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 组织机构
 * 组织架构：公司》部门》岗位》用户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "组织管理", description = "Organize")
@RestController
@RequestMapping("/api/permission/Organize")
@Slf4j
public class OrganizeController extends SuperController<OrganizeService, OrganizeEntity> {

    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;
    /**
     * 取出线程池
     */
    @Autowired
    private Executor threadPoolExecutor;
    @Autowired
    private OrganizeAdministratorService organizeAdministratorService;
    @Autowired
    private OrganizeRelationService organizeRelationService;

    //---------------------------组织管理--------------------------------------------

    /**
     * 获取组织列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取组织列表")
    @SaCheckPermission(value = {"permission.organize", "permission.position", "permission.user", "permission.role"}, mode = SaMode.OR)
    @GetMapping
    public ActionResult<ListVO<OrganizeListVO>> getList(PaginationOrganize pagination) {
        // 获取所有组织
        Map<String, OrganizeEntity> orgMaps;
        if (!UserProvider.getUser().getIsAdministrator()) {
            // 通过权限转树
            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(UserProvider.getLoginUserId());
            Set<String> orgIds = new HashSet<>(16);
            // 判断自己是哪些组织的管理员
            listss.stream().forEach(t-> {
                if (t != null) {
                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgIds.add(t.getOrganizeId());
                    }
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), Objects.equals(pagination.getEnabledMark(), 1));
                        orgIds.addAll(underOrganizations);
                    }
                }
            });
            List<String> list1 = new ArrayList<>(orgIds);
            // 得到所有有权限的组织
            orgMaps = organizeService.getOrganizeName(list1, pagination.getKeyword(), Objects.equals(pagination.getEnabledMark(), 1), pagination.getType());
        }else{
            orgMaps = organizeService.getOrgMaps(pagination.getKeyword(), Objects.equals(pagination.getEnabledMark(), 1), pagination.getType());
        }
        Map<String, OrganizeModel> orgMapsModel = JSONObject.parseObject(JSONObject.toJSONString(orgMaps), new TypeReference<LinkedHashMap<String, OrganizeModel>>() {}, new Feature[0]);;

        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        orgMapsModel.values().forEach(t -> {
            if (PermissionConst.COMPANY.equals(t.getType())) {
                t.setIcon("icon-ym icon-ym-tree-organization3");
            } else {
                t.setIcon("icon-ym icon-ym-tree-department1");
            }
            // 处理断层
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                String[] split = t.getOrganizeIdTree().split(",");
                List<String> list1 = Arrays.asList(split);
                Collections.reverse(list1);
                for (String orgId : list1) {
                    if(!orgId.equals(t.getId())) {
                        OrganizeModel organizeEntity1 = orgMapsModel.get(orgId);
                        if (organizeEntity1 != null) {
                            t.setParentId(organizeEntity1.getId());
                            String[] split1 = t.getOrganizeIdTree().split(organizeEntity1.getId());
                            if (split1.length > 1) {
                                t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                            }
                            break;
                        }
                    }
                }
            }
        });
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertMapsToTreeDot(orgMapsModel);
        List<OrganizeListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeListVO.class);
        listVO.forEach(t -> {
            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
        });
        ListVO<OrganizeListVO> vo = new ListVO<>();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 获取组织下拉框列表
     *
     * @param pagination 分页模型
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取组织下拉框列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/Selector/{id}")
    public ActionResult<ListVO<OrganizeSelectorVO>> getSelector(Pagination pagination, @PathVariable("id") String id) {
        List<OrganizeEntity> allList = new LinkedList<>(organizeService.getOrgMaps(pagination.getKeyword(), true, null).values());
        if (!"0".equals(id)) {
            allList.remove(organizeService.getInfo(id));
        }
        List<OrganizeEntity> dataAll = allList;
        List<OrganizeEntity> list = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(allList, dataAll), OrganizeEntity.class);
        list = list.stream().filter(t -> "company".equals(t.getCategory())).collect(Collectors.toList());
        List<OrganizeModel> models = JsonUtil.getJsonToList(list, OrganizeModel.class);
        for (OrganizeModel model : models) {
            model.setIcon("icon-ym icon-ym-tree-organization3");
        }
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        models.forEach(t -> {
            t.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                String[] split = t.getOrganizeIdTree().split(",");
                if (split.length > 0) {
                    t.setOrganizeIds(Arrays.asList(split));
                } else {
                    t.setOrganizeIds(new ArrayList<>());
                }
            }
        });

        List<OrganizeModel> modelAll = new ArrayList<>();
        modelAll.addAll(models);
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDotFilter(modelAll);
        List<OrganizeSelectorVO> listVO = JsonUtil.getJsonToList(trees, OrganizeSelectorVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }


    /**
     * 获取组织下拉框列表
     *
     * @param pagination 分页模型
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取组织下拉框列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/SelectorByAuth/{id}")
    public ActionResult<ListVO<OrganizeSelectorByAuthVO>> getSelectorByAuth(Pagination pagination, @PathVariable("id") String id) {
        List<OrganizeEntity> allList = new LinkedList<>(organizeService.getOrgMaps(pagination.getKeyword(), true, null).values());
        allList = allList.stream().filter(t -> "company".equals(t.getCategory())).collect(Collectors.toList());
        OrganizeEntity entity = organizeService.getInfo(id);
        List<OrganizeEntity> dataAll = allList;

        List<OrganizeEntity> list = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(allList, dataAll), OrganizeEntity.class);

        List<OrganizeByAuthModel> models = JsonUtil.getJsonToList(list, OrganizeByAuthModel.class);

        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        if (!UserProvider.getUser().getIsAdministrator()) {
            // 通过权限转树
            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(UserProvider.getLoginUserId());
            Set<String> orgIds = new HashSet<>(16);
            // 判断自己是哪些组织的管理员
            listss.stream().forEach(t-> {
                if (t != null) {
                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgIds.add(t.getOrganizeId());
                    }
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                        orgIds.addAll(underOrganizations);
                    }
                }
            });
            List<String> list1 = new ArrayList<>(orgIds);
            // 得到所有有权限的组织
            List<OrganizeEntity> organizeName = organizeService.getOrganizeName(list1);
            organizeName = organizeName.stream().filter(t->PermissionConst.COMPANY.equals(t.getCategory())).collect(Collectors.toList());
            models = JsonUtil.getJsonToList(organizeName, OrganizeByAuthModel.class);
        }

        List<OrganizeByAuthModel> finalModels = models;
        models.forEach(t -> {
            if (PermissionConst.COMPANY.equals(t.getType())) {
                t.setIcon("icon-ym icon-ym-tree-organization3");
            } else {
                t.setIcon("icon-ym icon-ym-tree-department1");
            }
            // 处理断层
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                String[] split = t.getOrganizeIdTree().split(",");
                List<String> list1 = Arrays.asList(split);
                t.setOrganizeIds(list1);
                t.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
                List<String> list2 = new ArrayList<>(list1);
                Collections.reverse(list2);
                for (String orgId : list2) {
                    OrganizeModel organizeEntity1 = finalModels.stream().filter(organizeEntity -> organizeEntity.getId().equals(orgId)).findFirst().orElse(null);
                    if (organizeEntity1 != null && !organizeEntity1.getId().equals(t.getId())) {
                        t.setParentId(organizeEntity1.getId());
                        String[] split1 = t.getOrganizeIdTree().split(organizeEntity1.getId());
                        if (split1.length > 1) {
                            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                        }
                        break;
                    }
                }
            }
        });
        List<SumTree<OrganizeByAuthModel>> trees = TreeDotUtils.convertListToTreeDot(models);
        List<OrganizeSelectorByAuthVO> listVO = JsonUtil.getJsonToList(trees, OrganizeSelectorByAuthVO.class);
        listVO.forEach(t -> {
            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
        });
        ListVO<OrganizeSelectorByAuthVO> vo = new ListVO<>();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 通过部门id获取部门下拉框下拉框
     *
     * @param organizeConditionModel 组织条件模型
     * @return
     */
    @Operation(summary = "通过部门id获取部门下拉框")
    @Parameters({
            @Parameter(name = "organizeConditionModel", description = "组织id模型", required = true)
    })
    @PostMapping("/OrganizeCondition")
    public ActionResult<ListVO<OrganizeListVO>> organizeCondition(@RequestBody OrganizeConditionModel organizeConditionModel) {
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        List<OrganizeModel> organizeList = organizeRelationService.getOrgIdsList(organizeConditionModel);
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(organizeList);
        List<OrganizeListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeListVO.class);
        listVO.forEach(t -> {
            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
        });
        ListVO<OrganizeListVO> vo = new ListVO<>();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 组织树形
     *
     * @return
     */
    @Operation(summary = "获取组织/公司树形")
    @GetMapping("/Tree")
    public ActionResult<ListVO<OrganizeTreeVO>> tree() {
        List<OrganizeEntity> list = new LinkedList<>(organizeService.getOrgMaps(null, true, null).values());
        list = list.stream().filter(t -> "company".equals(t.getCategory())).collect(Collectors.toList());
        List<OrganizeModel> models = JsonUtil.getJsonToList(list, OrganizeModel.class);
        for (OrganizeModel model : models) {
            model.setIcon("icon-ym icon-ym-tree-organization3");
        }
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(models);
        List<OrganizeTreeVO> listVO = JsonUtil.getJsonToList(trees, OrganizeTreeVO.class);
        //将子节点全部删除
        Iterator<OrganizeTreeVO> iterator = listVO.iterator();
        while (iterator.hasNext()) {
            OrganizeTreeVO orananizeTreeVO = iterator.next();
            if (!"-1".equals(orananizeTreeVO.getParentId())) {
                iterator.remove();
            }
        }
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 获取组织信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取组织信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @GetMapping("/{id}")
    public ActionResult<OrganizeInfoVO> info(@PathVariable("id") String id) throws DataException {
        OrganizeEntity entity = organizeService.getInfo(id);
        OrganizeInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, OrganizeInfoVO.class);
        if (StringUtil.isNotEmpty(entity.getOrganizeIdTree())) {
            String replace = entity.getOrganizeIdTree().replace(entity.getId(), "");
            if (StringUtil.isNotEmpty(replace) && !",".equals(replace)) {
                vo.setOrganizeIdTree(Arrays.asList(replace.split(",")));
            } else {
                vo.setOrganizeIdTree(Arrays.asList(new String[]{"-1"}));
            }
        }
        return ActionResult.success(vo);
    }


    /**
     * 新建组织
     *
     * @param organizeCrForm 新建模型
     * @return
     */
    @OrganizePermission
    @Operation(summary = "新建组织")
    @Parameters({
            @Parameter(name = "organizeCrForm", description = "新建模型", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @PostMapping
    public ActionResult create(@RequestBody @Valid OrganizeCrForm organizeCrForm) {
        OrganizeEntity entity = JsonUtil.getJsonToBean(organizeCrForm, OrganizeEntity.class);
        entity.setCategory("company");
        if (organizeService.isExistByFullName(entity, false, false)) {
            return ActionResult.fail("公司名称不能重复");
        }
        if (organizeService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("公司编码不能重复");
        }

        // 通过组织id获取父级组织
        String organizeIdTree = getOrganizeIdTree(entity);
        entity.setOrganizeIdTree(organizeIdTree);

        organizeService.create(entity);
        threadPoolExecutor.execute(() -> {
            try{
                //创建组织后判断是否需要同步到企业微信
                synThirdInfoService.updateDepartmentSysToQy(false, entity, "");
                //创建组织后判断是否需要同步到钉钉
                synThirdInfoService.createDepartmentSysToDing(false, entity, "");
            } catch (Exception e) {
                log.error("创建组织后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
            }
        });
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新组织
     *
     * @param id              主键值
     * @param organizeUpForm 实体对象
     * @return
     */
    @OrganizePermission
    @Operation(summary = "更新组织")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "organizeUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid OrganizeUpForm organizeUpForm) {
        List<OrganizeEntity> synList = new ArrayList<>();
        OrganizeEntity entity = JsonUtil.getJsonToBean(organizeUpForm, OrganizeEntity.class);
        OrganizeEntity info = organizeService.getInfo(organizeUpForm.getParentId());
        if (id.equals(entity.getParentId()) || (info != null && info.getOrganizeIdTree() != null && info.getOrganizeIdTree().contains(id))) {
            return ActionResult.fail("当前机构Id不能与父机构Id相同");
        }
        entity.setId(id);
        entity.setCategory("company");
        if (organizeService.isExistByFullName(entity, false, true)) {
            return ActionResult.fail("公司名称不能重复");
        }
        if (organizeService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("公司编码不能重复");
        }
        // 通过组织id获取父级组织
        String organizeIdTree = getOrganizeIdTree(entity);
        entity.setOrganizeIdTree(organizeIdTree);

        boolean flag = organizeService.update(id, entity);
        synList.add(entity);

        // 得到所有子组织或部门id
        if (info != null && info.getParentId() != null && !entity.getParentId().equals(info.getParentId())) {
            List<String> underOrganizations = organizeService.getUnderOrganizations(id, false);
            underOrganizations.forEach(t -> {
                OrganizeEntity info1 = organizeService.getInfo(t);
                if (StringUtil.isNotEmpty(info1.getOrganizeIdTree())) {
                    String organizeIdTrees = getOrganizeIdTree(info1);
                    info1.setOrganizeIdTree(organizeIdTrees);
                    organizeService.update(info1.getId(), info1);
                    synList.add(info1);
                }
            });
        }
        threadPoolExecutor.execute(() -> {
            synList.forEach(t -> {
                try{
                    //修改组织后判断是否需要同步到企业微信
                    synThirdInfoService.updateDepartmentSysToQy(false, t, "");
                    //修改组织后判断是否需要同步到钉钉
                    synThirdInfoService.updateDepartmentSysToDing(false, t, "");
                } catch (Exception e) {
                    log.error("修改组织后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
                }
            });
        });
        if (!flag) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除组织
     *
     * @param id 组织主键
     * @return
     */
    @OrganizePermission
    @Operation(summary = "删除组织")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        return organizeService.delete(id);
    }

    /**
     * 删除部门
     *
     * @param id 部门主键
     * @return
     */
    @OrganizePermission
    @Operation(summary = "删除部门")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @DeleteMapping("/Department/{id}")
    public ActionResult<String> deleteDepartment(@PathVariable("id") String id) {
        return organizeService.delete(id);
    }

    /**
     * 更新组织状态
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "更新组织状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        OrganizeEntity organizeEntity = organizeService.getInfo(id);
        if (organizeEntity != null) {
            if ("1".equals(String.valueOf(organizeEntity.getEnabledMark()))) {
                organizeEntity.setEnabledMark(0);
            } else {
                organizeEntity.setEnabledMark(1);
            }
            organizeService.update(organizeEntity.getId(), organizeEntity);
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.success(MsgCode.FA002.get());
    }


    //---------------------------部门管理--------------------------------------------

//    /**
//     * 获取部门列表
//     *
//     * @param companyId 组织id
//     * @param pagination 分页模型
//     * @return
//     */
//    @Operation(summary = "获取部门列表")
//    @Parameters({
//            @Parameter(name = "companyId", description = "组织id", required = true)
//    })
//    @SaCheckPermission(value = {"permission.organize"})
//    @GetMapping("/{companyId}/Department")
//    public ActionResult<ListVO<OrganizeDepartListVO>> getListDepartment(@PathVariable("companyId") String companyId, Pagination pagination) {
//        List<OrganizeEntity> dataAll = organizeService.getParentIdList(companyId);
//        List<String> childId = dataAll.stream().map(t -> t.getId()).collect(Collectors.toList());
//        List<OrganizeEntity> data = organizeService.getListAll(childId, pagination.getKeyword());
//        //正序显示
//        data = data.stream().sorted(Comparator.comparing(OrganizeEntity::getSortCode)).collect(Collectors.toList());
//        List<OrganizeModel> models = JsonUtil.getJsonToList(data, OrganizeModel.class);
//        if (!UserProvider.getUser().getIsAdministrator()) {
//            // 通过权限转树
//            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(UserProvider.getLoginUserId());
//            Set<String> orgIds = new HashSet<>(16);
//            // 判断自己是哪些组织的管理员
//            listss.stream().forEach(t-> {
//                if (t != null) {
//                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
//                        orgIds.add(t.getOrganizeId());
//                    }
//                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
//                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
//                        orgIds.addAll(underOrganizations);
//                    }
//                }
//            });
//            List<String> list1 = new ArrayList<>(orgIds);
//            List<OrganizeModel> organizeModels = new ArrayList<>(16);
//
//            models.forEach(t -> {
//                list1.forEach(tt -> {
//                    if (t.getId() != null && t.getId().equals(tt)) {
//                        organizeModels.add(t);
//                    }
//                });
//            });
//            models = organizeModels;
//        }
//        // 给部门经理赋值
//        for (OrganizeModel model : models) {
//            if (!StringUtil.isEmpty(model.getManager())) {
//                UserEntity entity = userService.getById(model.getManager());
//                model.setManager(entity != null ? entity.getRealName() + "/" + entity.getAccount() : null);
//            }
//        }
//        List<OrganizeDepartListVO> listvo = JsonUtil.getJsonToList(models, OrganizeDepartListVO.class);
//        ListVO vo = new ListVO();
//        vo.setList(listvo);
//        return ActionResult.success(vo);
//    }



    /**
     * 获取部门下拉框列表
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取部门下拉框列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/Department/Selector/{id}")
    public ActionResult<ListVO<OrganizeDepartSelectorListVO>> getListDepartment(@PathVariable("id") String id) {
        List<OrganizeEntity> data = new LinkedList<>(organizeService.getOrgMaps(null, true, null).values());
        if (!"0".equals(id)) {
            data.remove(organizeService.getInfo(id));
        }
        List<OrganizeModel> models = JsonUtil.getJsonToList(data, OrganizeModel.class);
        for (OrganizeModel model : models) {
            if ("department".equals(model.getType())) {
                model.setIcon("icon-ym icon-ym-tree-department1");
            } else if ("company".equals(model.getType())) {
                model.setIcon("icon-ym icon-ym-tree-organization3");
            }
        }
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        models.forEach(t -> {
            t.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                String[] split = t.getOrganizeIdTree().split(",");
                if (split.length > 0) {
                    t.setOrganizeIds(Arrays.asList(split));
                } else {
                    t.setOrganizeIds(new ArrayList<>());
                }
            }
        });

        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDotFilter(models);
        List<OrganizeDepartSelectorListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeDepartSelectorListVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 获取部门下拉框列表
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取部门下拉框列表")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/Department/SelectorByAuth/{id}")
    public ActionResult<ListVO<OrganizeSelectorByAuthVO>> getDepartmentSelectorByAuth(@PathVariable("id") String id) {
        Map<String, OrganizeEntity> orgMaps;
        OrganizeEntity entity = organizeService.getInfo(id);

        if (!UserProvider.getUser().getIsAdministrator()) {
            // 通过权限转树
            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(UserProvider.getUser().getUserId());
            Set<String> orgIds = new HashSet<>(16);
            // 判断自己是哪些组织的管理员
            listss.stream().forEach(t-> {
                if (t != null) {
                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgIds.add(t.getOrganizeId());
                    }
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                        orgIds.addAll(underOrganizations);
                    }
                }
            });
            List<String> list1 = new ArrayList<>(orgIds);
            orgMaps = organizeService.getOrganizeName(list1, null, true, null);
        } else {
            orgMaps = organizeService.getOrgMaps(null, true, null);
        }
        Map<String, OrganizeByAuthModel> orgMapsModel = JSONObject.parseObject(JSONObject.toJSONString(orgMaps), new TypeReference<LinkedHashMap<String, OrganizeByAuthModel>>() {}, new Feature[0]);;
        if (!"0".equals(id)) {
            orgMapsModel.remove(id);
        }
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        // 判断当前编辑的权限时候是否有上级
        if (entity != null) {
            if (orgMapsModel.values().stream().filter(t -> t.getId().equals(entity.getParentId())).findFirst().orElse(null) == null) {
                OrganizeEntity info = organizeService.getInfo(entity.getParentId());
                if (info != null) {
                    OrganizeByAuthModel jsonToBean = JsonUtil.getJsonToBean(info, OrganizeByAuthModel.class);
                    jsonToBean.setDisabled(true);
                    orgMapsModel.put(info.getId(), jsonToBean);
                }
            }
        }
        orgMapsModel.values().forEach(t -> {
            if (PermissionConst.COMPANY.equals(t.getType())) {
                t.setIcon("icon-ym icon-ym-tree-organization3");
            } else {
                t.setIcon("icon-ym icon-ym-tree-department1");
            }
            // 处理断层
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                List<String> list1 = new ArrayList<>();
                String[] split = t.getOrganizeIdTree().split(",");
                list1 = Arrays.asList(split);
                List<String> list = new ArrayList<>(16);
                list1.forEach(orgId -> {
                    if (StringUtil.isNotEmpty(orgId)) {
                        list.add(orgId);
                    }
                });
                t.setOrganizeIds(list);
                t.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
                Collections.reverse(list1);
                for (String orgId : list1) {
                    OrganizeModel organizeEntity1 = orgMapsModel.get(orgId);
                    if (organizeEntity1 != null && !organizeEntity1.getId().equals(t.getId())) {
                        t.setParentId(organizeEntity1.getId());
                        String[] split1 = t.getOrganizeIdTree().split(organizeEntity1.getId());
                        if (split1.length > 1) {
                            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                        }
                        break;
                    }
                }
            }
        });
        List<SumTree<OrganizeByAuthModel>> trees = TreeDotUtils.convertMapsToTreeDot(orgMapsModel);
        List<OrganizeSelectorByAuthVO> listVO = JsonUtil.getJsonToList(trees, OrganizeSelectorByAuthVO.class);
        listVO.forEach(t -> {
            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
        });
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }


    /**
     * 新建部门
     *
     * @param organizeDepartCrForm 新建模型
     * @return
     */
    @OrganizePermission
    @Operation(summary = "新建部门")
    @Parameters({
            @Parameter(name = "organizeDepartCrForm", description = "新建模型", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @PostMapping("/Department")
    public ActionResult createDepartment(@RequestBody @Valid OrganizeDepartCrForm organizeDepartCrForm) {
        OrganizeEntity entity = JsonUtil.getJsonToBean(organizeDepartCrForm, OrganizeEntity.class);
        entity.setCategory("department");
        //判断同一个父级下是否含有同一个名称
        if (organizeService.isExistByFullName(entity, false, false)) {
            return ActionResult.fail("部门名称不能重复");
        }
        //判断同一个父级下是否含有同一个编码
        if (organizeService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("部门编码不能重复");
        }

        // 通过组织id获取父级组织
        String organizeIdTree = getOrganizeIdTree(entity);
        entity.setOrganizeIdTree(organizeIdTree);

        organizeService.create(entity);
        threadPoolExecutor.execute(() -> {
            try{
                //创建部门后判断是否需要同步到企业微信
                synThirdInfoService.createDepartmentSysToQy(false, entity, "");
                //创建部门后判断是否需要同步到钉钉
                synThirdInfoService.createDepartmentSysToDing(false, entity, "");
            } catch (Exception e) {
                log.error("创建部门后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
            }
        });
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新部门
     *
     * @param id                    主键值
     * @param oraganizeDepartUpForm 修改模型
     * @return
     */
    @OrganizePermission
    @Operation(summary = "更新部门")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "oraganizeDepartUpForm", description = "修改模型", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @PutMapping("/Department/{id}")
    public ActionResult updateDepartment(@PathVariable("id") String id, @RequestBody @Valid OrganizeDepartUpForm oraganizeDepartUpForm) {
        List<OrganizeEntity> synList = new ArrayList<>();
        OrganizeEntity entity = JsonUtil.getJsonToBean(oraganizeDepartUpForm, OrganizeEntity.class);
        OrganizeEntity info = organizeService.getInfo(oraganizeDepartUpForm.getParentId());
        if (id.equals(entity.getParentId()) || (info != null && info.getOrganizeIdTree() != null && info.getOrganizeIdTree().contains(id))) {
            return ActionResult.fail("当前机构Id不能与父机构Id相同");
        }
        entity.setId(id);
        entity.setCategory("department");
        //判断同一个父级下是否含有同一个名称
        if (organizeService.isExistByFullName(entity, false, true)) {
            return ActionResult.fail("部门名称不能重复");
        }
        //判断同一个父级下是否含有同一个编码
        if (organizeService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("部门编码不能重复");
        }

        // 通过组织id获取父级组织
        String organizeIdTree = getOrganizeIdTree(entity);
        entity.setOrganizeIdTree(organizeIdTree);

        boolean flag = organizeService.update(id, entity);
        synList.add(entity);

        // 得到所有子组织或部门id
        if (info.getParentId() != null && !entity.getParentId().equals(info.getParentId())) {
            List<String> underOrganizations = organizeService.getUnderOrganizations(id, false);
            underOrganizations.forEach(t -> {
                OrganizeEntity info1 = organizeService.getInfo(t);
                if (StringUtil.isNotEmpty(info1.getOrganizeIdTree())) {
                    String organizeIdTrees = getOrganizeIdTree(info1);
                    info1.setOrganizeIdTree(organizeIdTrees);
                    organizeService.update(info1.getId(), info1);
                    synList.add(info1);
                }
            });
        }

        threadPoolExecutor.execute(() -> {
            synList.forEach(t -> {
                try{
                    //修改部门后判断是否需要同步到企业微信
                    synThirdInfoService.updateDepartmentSysToQy(false, t, "");                    //修改部门后判断是否需要同步到钉钉
                    synThirdInfoService.updateDepartmentSysToDing(false, t, "");
                } catch (Exception e) {
                    log.error("修改部门后同步失败到企业微信或钉钉失败，异常：" + e.getMessage());
                }
            });
        });
        if (!flag) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }



    /**
     * 更新部门状态
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "更新部门状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @PutMapping("/Department/{id}/Actions/State")
    public ActionResult updateDepartment(@PathVariable("id") String id) {
        OrganizeEntity organizeEntity = organizeService.getInfo(id);
        if (organizeEntity != null) {
            if ("1".equals(String.valueOf(organizeEntity.getEnabledMark()))) {
                organizeEntity.setEnabledMark(0);
            } else {
                organizeEntity.setEnabledMark(1);
            }
            organizeService.update(organizeEntity.getId(), organizeEntity);
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail(MsgCode.FA002.get());
    }

    /**
     * 获取部门信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取部门信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission(value = {"permission.organize"})
    @GetMapping("/Department/{id}")
    public ActionResult<OrganizeDepartInfoVO> infoDepartment(@PathVariable("id") String id) throws DataException {
        OrganizeEntity entity = organizeService.getInfo(id);
        OrganizeDepartInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, OrganizeDepartInfoVO.class);
        List<String> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(entity.getOrganizeIdTree())) {
            String[] split = entity.getOrganizeIdTree().split(",");
            if (split.length > 1) {
                for (int i = 0; i < split.length - 1; i++) {
                    list.add(split[i]);
                }
            }
        }
        vo.setOrganizeIdTree(list);
        return ActionResult.success(vo);
    }

    /**
     * 获取父级组织id
     *
     * @param entity
     * @return
     */
//    private String getOrganizeIdTree(OrganizeEntity entity) {
//        List<String> list = new ArrayList<>();
//        organizeService.getOrganizeIdTree(entity.getParentId(), list);
//        // 倒叙排放
//        Collections.reverse(list);
//        StringBuffer organizeIdTree = new StringBuffer();
//        for (String organizeParentId : list) {
//            organizeIdTree.append("," + organizeParentId);
//        }
//        String organizeParentIdTree = organizeIdTree.toString();
//        if (StringUtil.isNotEmpty(organizeParentIdTree)) {
//            organizeParentIdTree = organizeParentIdTree.replaceFirst(",", "");
//        }
//        return organizeParentIdTree;
//    }

    /**
     * 获取父级组织id
     *
     * @return
     */
    
    @PostMapping("/getOrganizeIdTree")
    public String getOrganizeIdTree(@RequestBody OrganizeEntity entity) {
        return organizeService.getOrganizeIdTree(entity);
    }

    
    @GetMapping("/getInfoById/{organizeId}")
    public OrganizeEntity getInfoById(@PathVariable("organizeId") String organizeId) {
        return Optional.ofNullable(organizeService.getInfo(organizeId)).orElse(new OrganizeEntity());
    }

    
    @GetMapping("/getList")
    public List<OrganizeEntity> getList() {
        return organizeService.getList(false);
    }

    
    @PostMapping("/getOrganizeName")
    public List<OrganizeEntity> getOrganizeName(@RequestBody List<String> id){
        return organizeService.getOrganizeName(id);
    }

    
    @GetMapping("/getByFullName/{fullName}")
    public OrganizeEntity getByFullName(@PathVariable("fullName") String fullName){
        return organizeService.getByFullName(fullName);
    }

    
    @GetMapping("/getOrganizeId/{organizeId}")
    public List<OrganizeEntity> getOrganizeId(@PathVariable("organizeId") String organizeId){
        List<OrganizeEntity> organizeList = new ArrayList<>();
        organizeService.getOrganizeId(organizeId, organizeList);
        return organizeList;
    }

    
    @GetMapping("/organizeIdTree")
    public String getFullNameByOrgIdTree(@RequestParam("organizeIdTree") String organizeIdTree) {
        return organizeService.getFullNameByOrgIdTree(organizeService.getInfoList(), organizeIdTree, "/");
    }

    
    @GetMapping("/getOrgMap")
    public Map<String, Object> getOrgMap(@RequestParam("type") String type, @RequestParam(value = "category",required = false) String category) {
        if (type.equals("id-fullName")){
            return organizeService.getOrgMap();
        } else if (type.equals("fullName-id")){
            return organizeService.getOrgNameAndId(category);
        } else {
            return organizeService.getOrgEncodeAndName(category);
        }
    }

    
    @PostMapping("/save")
    public void save(@RequestBody OrganizeEntity entity) {
        organizeService.save(entity);
    }

    
    @GetMapping("/getOrganizeByParentId")
    public List<OrganizeEntity> getOrganizeByParentId(){
        return organizeService.getOrganizeByParentId("-1");
    }

    
    @PutMapping("/updateOrganizeEntity/{organizeId}")
    public void updateOrganizeEntity(@PathVariable("organizeId") String organizeId,@RequestBody OrganizeEntity organizeEntity){
        organizeService.update(organizeId, organizeEntity);
    }

    
    @GetMapping("/getOrganizeDepartmentAll/{organize}")
    public List<OrganizeEntity> getOrganizeDepartmentAll(@PathVariable("organize") String organize) {
        List<OrganizeEntity> list = new ArrayList<>();
        list.addAll(organizeService.getDepartmentAll(organize));
        return list;
    }

    
    @GetMapping("/getUnderOrganizations/{organizeId}")
    public List<String> getUnderOrganizations(@PathVariable("organizeId")String organizeId) {
        return organizeService.getUnderOrganizations(organizeId, false);
    }

    
    @GetMapping("/upWardRecursion")
    public List<String> upWardRecursion(String organizeId) {
        List<String> allUpOrgIDs = new ArrayList<>();
        organizeService.upWardRecursion(allUpOrgIDs,organizeId);
        return allUpOrgIDs;
    }

    /**
     * 获取默认当前值部门ID
     *
     * @param organizeConditionModel 参数
     * @return 执行结构
     * @throws DataException ignore
     */
    @Operation(summary = "获取默认当前值部门ID")
    @Parameters({
            @Parameter(name = "organizeConditionModel", description = "参数", required = true)
    })
    @PostMapping("/getDefaultCurrentValueDepartmentId")
    public ActionResult<?> getDefaultCurrentValueDepartmentId(@RequestBody OrganizeConditionModel organizeConditionModel) throws DataException {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String departmentId = getDefaultCurrentDepartmentId(organizeConditionModel);
        dataMap.put("departmentId", departmentId);
        return ActionResult.success("查询成功", dataMap);
    }


    // -----临时调用
    /**
     * 获取组织列表
     *
     * @param id 主键
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取组织列表")
    @SaCheckPermission(value = {"permission.organize", "permission.position", "permission.user", "permission.role"}, mode = SaMode.OR)
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/AsyncList/{id}")
    public ActionResult<ListVO<OrganizeListVO>> getList(@PathVariable("id") String id, Pagination pagination) {
        ListVO<OrganizeListVO> vo = new ListVO<>();
        // 获取所有组织
        Map<String, OrganizeEntity> orgMaps;
        if (!UserProvider.getUser().getIsAdministrator()) {
            // 通过权限转树
            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(UserProvider.getUser().getUserId());
            Set<String> orgIds = new HashSet<>(16);
            // 判断自己是哪些组织的管理员
            listss.stream().forEach(t-> {
                if (t != null) {
                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgIds.add(t.getOrganizeId());
                    }
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                        orgIds.addAll(underOrganizations);
                    }
                }
            });
            List<String> list1 = new ArrayList<>(orgIds);
            // 得到所有有权限的组织
            orgMaps = organizeService.getOrganizeName(list1, null, false, null);
        } else {
            orgMaps = organizeService.getOrgMaps(null, false, null);
        }
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        List<OrganizeEntity> organizeEntityList = new ArrayList<>();
        OrganizeEntity parentEntity = null;
        if ("-1".equals(id)) {
            parentEntity = organizeService.getInfoByParentId(id);
            OrganizeEntity organizeEntity = orgMaps.get(parentEntity.getId());
            if (organizeEntity != null) {
                organizeEntityList.add(organizeEntity);
            }
        } else {
            parentEntity = organizeService.getInfo(id);
        }
        // 判断是否有顶级组织权限
        if (organizeEntityList.size() == 0) {
            List<OrganizeEntity> temOrganizeEntityList = new ArrayList<>();
            temOrganizeEntityList.add(parentEntity);
            getParentEntity(orgMaps, temOrganizeEntityList, organizeEntityList);
        }
        if (organizeEntityList .size() == 0) {
            vo.setList(new ArrayList<>());
            return ActionResult.success(vo);
        }
        List<OrganizeListVO> voList = JsonUtil.getJsonToList(organizeEntityList, OrganizeListVO.class);
        voList.forEach(t -> {
            if (PermissionConst.COMPANY.equals(t.getType())) {
                t.setIcon("icon-ym icon-ym-tree-organization3");
            } else {
                t.setIcon("icon-ym icon-ym-tree-department1");
            }
            t.setHasChildren(true);
            // 处理断层
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                String[] split = t.getOrganizeIdTree().split(",");
                List<String> list1 = Arrays.asList(split);
                Collections.reverse(list1);
                for (String orgId : list1) {
                    if(!orgId.equals(t.getId())) {
                        OrganizeEntity organizeEntity1 = orgMaps.get(orgId);
                        if (organizeEntity1 != null) {
                            t.setParentId(organizeEntity1.getId());
                            String[] split1 = t.getOrganizeIdTree().split(organizeEntity1.getId());
                            if (split1.length > 1) {
                                t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                            }
                            break;
                        }
                    }
                }
            }
        });
        vo.setList(voList);
        return ActionResult.success(vo);
    }

    private void getParentEntity(Map<String, OrganizeEntity> orgMaps,
                                 List<OrganizeEntity> temOrganizeEntityList,
                                 List<OrganizeEntity> organizeEntityList) {
        List<OrganizeEntity> temOrganizeEntityList1 = new ArrayList<>();
        // 判断是否有顶级组织权限
        if (organizeEntityList.size() == 0) {
            temOrganizeEntityList.forEach(t -> {
                List<OrganizeEntity> organizeByParentId = organizeService.getOrganizeByParentId(t.getId());
                temOrganizeEntityList1.addAll(organizeByParentId);
                organizeByParentId.forEach(organizeEntity -> {
                    OrganizeEntity organizeEntity1 = orgMaps.get(organizeEntity.getId());
                    if (organizeEntity1 != null) {
                        organizeEntityList.add(organizeEntity1);
                    }
                });
            });
        }
        if (organizeEntityList.size() == 0 && temOrganizeEntityList1.size() > 0) {
            getParentEntity(orgMaps, temOrganizeEntityList1, organizeEntityList);
        }
    }

    
    @PostMapping("/getDefaultCurrentDepartmentId")
    public String getDefaultCurrentDepartmentId(@RequestBody OrganizeConditionModel organizeConditionModel) throws DataException {
       return organizeService.getDefaultCurrentDepartmentId(organizeConditionModel);
    }

    
    @PostMapping("/getOrganizeChildList")
    public List<OrganizeEntity> getOrganizeChildList(@RequestBody List<String> list) {
        List<OrganizeEntity> organizeList = organizeService.getOrganizeChildList(list);
        return organizeList;
    }

    
    @GetMapping("/getOrgMapsAll")
    public Map<String, OrganizeEntity> getOrgMapsAll() {
        return organizeService.getOrgMapsAll();
    }

    
    @GetMapping("/removeOrganizeInfoList")
    public void removeOrganizeInfoList() {
        redisUtil.remove(cacheKeyUtil.getOrganizeInfoList());
    }

    /**
     * 获取名称及id组成map
     *
     * @return
     */
    
    @GetMapping("/getInfoList")
    public Map<String, String> getInfoList() {
        return organizeService.getInfoList();
    }

    /**
     * 获取所有组织全路径名称
     *
     * @return
     */
    
    @GetMapping("/getAllOrgsTreeName")
    public Map<String, Object> getAllOrgsTreeName() {
        return organizeService.getAllOrgsTreeName();
    }
}
