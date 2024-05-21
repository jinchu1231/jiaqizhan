package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.annotation.PositionPermission;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.exception.DataException;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.permission.PermissionModel;
import jnpf.permission.model.position.PaginationPosition;
import jnpf.permission.model.position.PosOrgConditionModel;
import jnpf.permission.model.position.PosOrgModel;
import jnpf.permission.model.position.PositionCrForm;
import jnpf.permission.model.position.PositionInfoVO;
import jnpf.permission.model.position.PositionListAllVO;
import jnpf.permission.model.position.PositionListVO;
import jnpf.permission.model.position.PositionSelectorVO;
import jnpf.permission.model.position.PositionUpForm;
import jnpf.permission.model.position.PositionVo;
import jnpf.permission.model.user.UserIdModel;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "岗位管理", description = "Position")
@RestController
@RequestMapping("/api/permission/Position")
public class PositionController extends SuperController<PositionService, PositionEntity> {
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private OrganizeRelationService organizeRelationService;

    /**
     * 获取岗位管理信息列表
     *
     * @param paginationPosition 分页模型
     * @return
     */
    @Operation(summary = "获取岗位列表（分页）")
    @SaCheckPermission("permission.position")
    @GetMapping
    public ActionResult<PageListVO<PositionListVO>> list(PaginationPosition paginationPosition) {
        List<DictionaryDataEntity> dictionaryDataEntities = dictionaryDataService.getListByTypeDataCode("PositionType");
        if (StringUtil.isNotEmpty(paginationPosition.getType())) {
            DictionaryDataEntity dictionaryDataEntity = dictionaryDataEntities.stream().filter(t -> paginationPosition.getType().equals(t.getId())).findFirst().orElse(null);
            if (dictionaryDataEntity != null) {
                paginationPosition.setEnCode(dictionaryDataEntity.getEnCode());
            }
        }
        List<PositionEntity> data = positionService.getList(paginationPosition);
        //添加部门信息，部门映射到organizeId
        List<PositionListVO> voList = JsonUtil.getJsonToList(data, PositionListVO.class);
        List<String> collect = data.stream().map(PositionEntity::getOrganizeId).collect(Collectors.toList());
        List<OrganizeEntity> list = organizeService.getOrgEntityList(collect, true);
        //添加部门信息
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        for (PositionListVO entity1 : voList) {
            OrganizeEntity entity = list.stream().filter(t -> t.getId().equals(entity1.getOrganizeId())).findFirst().orElse(new OrganizeEntity());
            if (entity1.getOrganizeId().equals(entity.getId())) {
                entity1.setDepartment(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, entity.getOrganizeIdTree(), "/"));
            }
        }
        //将type成中文名
        for (PositionListVO entity1 : voList) {
            dictionaryDataEntities.stream().filter(t -> t.getEnCode().equals(entity1.getType())).findFirst().ifPresent(entity -> entity1.setType(entity.getFullName()));
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationPosition, PaginationVO.class);
        return ActionResult.page(voList, paginationVO);
    }

    /**
     * 列表
     *
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping("/All")
    public ActionResult<ListVO<PositionListAllVO>> listAll() {
        List<PositionEntity> list = positionService.getList(true);
        List<PositionListAllVO> vos = JsonUtil.getJsonToList(list, PositionListAllVO.class);
        ListVO<PositionListAllVO> vo = new ListVO<>();
        vo.setList(vos);
        return ActionResult.success(vo);
    }

    /**
     * 树形（机构+岗位）
     *
     * @return
     */
    @Operation(summary = "获取岗位下拉列表（公司+部门+岗位）")
    @GetMapping("/Selector")
    public ActionResult<ListVO<PositionSelectorVO>> selector() {
        List<PositionEntity> list1 = positionService.getList(true);
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, false, null);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        List<OrganizeEntity> list2 = new ArrayList<>(orgMaps.values());;
        List<PosOrgModel> posList = new ArrayList<>();
        for (PositionEntity entity : list1) {
            PosOrgModel posOrgModel = JsonUtil.getJsonToBean(entity, PosOrgModel.class);
            String organizeId = entity.getOrganizeId();
            posOrgModel.setParentId(organizeId);
            posOrgModel.setType("position");
            posOrgModel.setIcon("icon-ym icon-ym-tree-position1");
            OrganizeEntity organizeEntity = orgMaps.get(organizeId);
            if (organizeEntity != null) {
                posOrgModel.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/"));
                posOrgModel.setOrganizeIds(organizeService.getOrgIdTree(organizeEntity));
            } else {
                posOrgModel.setOrganizeIds(new ArrayList<>());
            }
            posList.add(posOrgModel);
        }
        List<PosOrgModel> orgList = JsonUtil.getJsonToList(list2, PosOrgModel.class);
        for (PosOrgModel entity1 : orgList) {
            if ("department".equals(entity1.getType())) {
                entity1.setIcon("icon-ym icon-ym-tree-department1");
            } else if ("company".equals(entity1.getType())) {
                entity1.setIcon("icon-ym icon-ym-tree-organization3");
            }
            OrganizeEntity organizeEntity = orgMaps.get(entity1.getId());
            if (organizeEntity != null) {
                entity1.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/"));
                entity1.setOrganizeIds(organizeService.getOrgIdTree(organizeEntity));
            } else {
                entity1.setOrganizeIds(new ArrayList<>());
            }
            entity1.setOrganizeIds(new ArrayList<>());
        }
        JSONArray objects = ListToTreeUtil.treeWhere(posList, orgList);
        List<PosOrgModel> jsonToList = JsonUtil.getJsonToList(objects, PosOrgModel.class);

        List<PosOrgModel> list = new ArrayList<>(16);
        // 得到角色的值
        List<PosOrgModel> collect = jsonToList.stream().filter(t -> "position".equals(t.getType())).sorted(Comparator.comparing(PosOrgModel::getSortCode)).collect(Collectors.toList());
        list.addAll(collect);
        jsonToList.removeAll(collect);
        List<PosOrgModel> collect1 = jsonToList.stream().sorted(Comparator.comparing(PosOrgModel::getSortCode).thenComparing(PosOrgModel::getCreatorTime, Comparator.reverseOrder())).collect(Collectors.toList());
        list.addAll(collect1);

        List<SumTree<PosOrgModel>> trees = TreeDotUtils.convertListToTreeDot(list);
        List<PositionSelectorVO> jsonToList1 = JsonUtil.getJsonToList(trees, PositionSelectorVO.class);
        ListVO vo = new ListVO();
        vo.setList(jsonToList1);
        return ActionResult.success(vo);
    }

    /**
     * 通过部门、岗位获取岗位下拉框
     *
     * @param idModel 岗位选择模型
     * @return
     */
    @Operation(summary = "通过部门、岗位获取岗位下拉框")
    @Parameters({
            @Parameter(name = "positionConditionModel", description = "岗位选择模型", required = true)
    })
    @PostMapping("/PositionCondition")
    public ActionResult<ListVO<PositionSelectorVO>> positionCondition(@RequestBody UserIdModel idModel) {
        // 定义返回对象
        List<PositionSelectorVO> modelList = new ArrayList<>();

        List<String> list = organizeRelationService.getOrgIds(idModel.getIds(), null);
        List<String> lists = new ArrayList<>();
        list.forEach(t -> lists.add(t.split("--")[0]));
        list = lists;
        List<String> collect = positionService.getListByOrganizeId(list, false).stream().map(PositionEntity::getId).collect(Collectors.toList());
        collect.addAll(list);
        List<PositionEntity> positionName = positionService.getPositionName(collect,  null);
        positionName = positionName.stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());

        Map<String, OrganizeEntity> orgMaps = organizeService.getOrganizeName(positionName.stream().map(PositionEntity::getOrganizeId).collect(Collectors.toList()), null, false, null);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();

        List<PosOrgConditionModel> posOrgModels = new ArrayList<>(16);
        positionName.forEach(t -> {
            PosOrgConditionModel posOrgModel = JsonUtil.getJsonToBean(t, PosOrgConditionModel.class);
            OrganizeEntity entity = orgMaps.get(t.getOrganizeId());
            if (entity != null) {
                posOrgModel.setOrganizeId(entity.getId());
                posOrgModel.setParentId(entity.getId());
                if (StringUtil.isNotEmpty(entity.getOrganizeIdTree())) {
                    posOrgModel.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, entity.getOrganizeIdTree(), "/"));
                }
            }
            posOrgModel.setType("position");
            posOrgModel.setIcon("icon-ym icon-ym-tree-position1");
            posOrgModels.add(posOrgModel);
        });

        // 处理组织
        orgMaps.values().forEach(org -> {
            PosOrgConditionModel orgVo = JsonUtil.getJsonToBean(org, PosOrgConditionModel.class);
            if ("department".equals(orgVo.getType())) {
                orgVo.setIcon("icon-ym icon-ym-tree-department1");
            } else if ("company".equals(orgVo.getType())) {
                orgVo.setIcon("icon-ym icon-ym-tree-organization3");
            }
            // 处理断层
            if (StringUtil.isNotEmpty(org.getOrganizeIdTree())) {
                List<String> list1 = new ArrayList<>();
                String[] split = org.getOrganizeIdTree().split(",");
                list1 = Arrays.asList(split);
                Collections.reverse(list1);
                for (String orgId : list1) {
                    OrganizeEntity organizeEntity1 = orgMaps.get(orgId);
                    if (organizeEntity1 != null && !organizeEntity1.getId().equals(orgVo.getId())) {
                        orgVo.setParentId(organizeEntity1.getId());
                        String[] split1 = org.getOrganizeIdTree().split(organizeEntity1.getId());
                        if (split1.length > 1) {
                            orgVo.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                        }
                        break;
                    }
                }
            }
            posOrgModels.add(orgVo);
        });

        List<SumTree<PosOrgConditionModel>> trees = TreeDotUtils.convertListToTreeDot(posOrgModels);
        List<PositionSelectorVO> positionSelectorVO = JsonUtil.getJsonToList(trees, PositionSelectorVO.class);
        // 处理数据
        positionSelectorVO.forEach(t -> {
            if (!"position".equals(t.getType())) {
                t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
            }
        });
        modelList.addAll(positionSelectorVO);
        ListVO vo = new ListVO();
        vo.setList(modelList);
        return ActionResult.success(vo);
    }

    /**
     * 通过组织id获取岗位列表
     *
     * @param organizeId 主键值
     * @return
     */
    @Operation(summary = "通过组织id获取岗位列表")
    @Parameters({
            @Parameter(name = "organizeId", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.position")
    @GetMapping("/getList/{organizeId}")
    public ActionResult<List<PositionVo>> getListByOrganizeId(@PathVariable("organizeId") String organizeId) {
        List<PositionEntity> list = positionService.getListByOrganizeId(Collections.singletonList(organizeId), false);
        List<PositionVo> jsonToList = JsonUtil.getJsonToList(list, PositionVo.class);
        return ActionResult.success(jsonToList);
    }

    /**
     * 获取岗位管理信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取岗位管理信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.position")
    @GetMapping("/{id}")
    public ActionResult<PositionInfoVO> getInfo(@PathVariable("id") String id) throws DataException {
        PositionEntity entity = positionService.getInfo(id);
        PositionInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, PositionInfoVO.class);
        String organizeId = entity.getOrganizeId();
        OrganizeEntity organizeEntity = organizeService.getInfo(organizeId);
        vo.setOrganizeIdTree(StringUtil.isNotEmpty(organizeEntity.getOrganizeIdTree()) ? Arrays.asList(organizeEntity.getOrganizeIdTree().split(",")) : new ArrayList<>());
        return ActionResult.success(vo);
    }


    /**
     * 新建岗位管理
     *
     * @param positionCrForm 实体对象
     * @return
     */
    @PositionPermission
    @Operation(summary = "新建岗位管理")
    @Parameters({
            @Parameter(name = "positionCrForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("permission.position")
    @PostMapping
    public ActionResult create(@RequestBody @Valid PositionCrForm positionCrForm) {
        PositionEntity entity = JsonUtil.getJsonToBean(positionCrForm, PositionEntity.class);
        if (positionService.isExistByFullName(entity, false)) {
            return ActionResult.fail("岗位名称不能重复");
        }
        if (positionService.isExistByEnCode(entity, false)) {
            return ActionResult.fail("岗位编码不能重复");
        }
        // 设置岗位id
        entity.setId(RandomUtil.uuId());
//        createOrganizeRoleRelation(entity.getOrganizeId(), entity.getId());
        positionService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新岗位管理
     *
     * @param id             主键值
     * @param positionUpForm 实体对象
     * @return
     */
    @PositionPermission
    @Operation(summary = "更新岗位管理")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "positionUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("permission.position")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid PositionUpForm positionUpForm) {
        // 当岗位绑定用户不让其更改
        if(userRelationService.existByObj(PermissionConst.POSITION, id)){
            if(!positionService.getInfo(id).getOrganizeId().equals(positionUpForm.getOrganizeId())){
                return ActionResult.fail(MsgCode.FA023.get());
            }
        }
        PositionEntity entity = JsonUtil.getJsonToBean(positionUpForm, PositionEntity.class);
        entity.setId(id);
        if (positionService.isExistByFullName(entity, true)) {
            return ActionResult.fail("岗位名称不能重复");
        }
        if (positionService.isExistByEnCode(entity, true)) {
            return ActionResult.fail("岗位编码不能重复");
        }
//        createOrganizeRoleRelation(entity.getOrganizeId(), id);
        boolean flag = positionService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除岗位管理
     *
     * @param id 主键值
     * @return
     */
    @PositionPermission
    @Operation(summary = "删除岗位管理")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.position")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        // 当岗位绑定用户不让其更改
        if(userRelationService.existByObj(PermissionConst.POSITION, id)){
            return ActionResult.fail(MsgCode.FA024.get());
        }
        PositionEntity entity = positionService.getInfo(id);
        if (entity != null) {
            List<UserRelationEntity> userRelList = userRelationService.getListByObjectId(id);
            if(userRelList.size()>0){
                return ActionResult.
                        fail("该岗位下有用户");
            }
            for (UserRelationEntity entity1 : userRelList) {
                UserEntity entity2 = userService.getById(entity1.getUserId());
                if (entity2 != null) {
                    String newPositionId = entity2.getPositionId().replace(id, "");
                    if (entity2.getPositionId().contains(id)) {
                        if (newPositionId.length() != 0 && newPositionId.substring(0, 1) == ",") {
                            entity2.setPositionId(newPositionId.substring(1));
                        } else if (newPositionId.length() != 0) {
                            entity2.setPositionId(newPositionId.replace(",,", ","));
                        }
                    }
                }
            }
            userRelationService.deleteAllByObjId(id);

            // 删除岗位与组织之间的关联数据 @TODO 无实际意义
            QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
            query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.POSITION);
            query.lambda().eq(OrganizeRelationEntity::getObjectId, id);
            organizeRelationService.remove(query);

            positionService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 更新菜单状态
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "更新菜单状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("permission.position")
    @PutMapping("/{id}/Actions/State")
    public ActionResult upState(@PathVariable("id") String id) {
        PositionEntity entity = positionService.getInfo(id);
        if (entity != null) {
            if (entity.getEnabledMark() == null ||"1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            positionService.update(id, entity);
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail("MsgCode.UPDATE_FAIL_NOT_EXISTS");
    }

    /**
     * 通过组织id获取岗位列表
     *
     * @param organizeIds 组织id数组
     * @return 岗位列表
     */
    @Operation(summary = "获取岗位列表通过组织id数组")
    @Parameters({
            @Parameter(name = "organizeIds", description = "组织id数组", required = true)
    })
    @SaCheckPermission("permission.user")
    @PostMapping("/getListByOrgIds")
    public ActionResult<ListVO<PermissionModel>> getListByOrganizeIds(@RequestBody @Valid Map<String,List<String>> organizeIds) {
        List<PermissionModel> PositionModelAll = new LinkedList<>();
        for(String organizeId : organizeIds.get("organizeIds")){
            OrganizeEntity info = organizeService.getInfo(organizeId);
            if(info != null){
                PermissionModel parentModel = new PermissionModel();
                List<PositionEntity> list = positionService.getListByOrganizeId(Collections.singletonList(organizeId), true);
                List<PermissionModel> positionModels = JsonUtil.getJsonToList(list, PermissionModel.class);
                parentModel.setHasChildren(true);
                parentModel.setFullName(info.getFullName());
                parentModel.setId(info.getId());
                parentModel.setChildren(positionModels);
                PositionModelAll.add(parentModel);
            }
        }
        ListVO vo = new ListVO();
        vo.setList(PositionModelAll);
        return ActionResult.success(vo);
    }

    /**
     * 添加组织角色关联关系
     *
     * @param organizeId    组织id
     * @param positionId    岗位id
     */
    private void createOrganizeRoleRelation(String organizeId, String positionId) {
        // 清除之前的关联关系
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.POSITION);
        query.lambda().eq(OrganizeRelationEntity::getObjectId, positionId);
        organizeRelationService.remove(query);
        // 添加与组织的关联关系
        OrganizeRelationEntity organizeRelationEntity = new OrganizeRelationEntity();
        organizeRelationEntity.setId(RandomUtil.uuId());
        organizeRelationEntity.setOrganizeId(organizeId);
        organizeRelationEntity.setObjectType(PermissionConst.POSITION);
        organizeRelationEntity.setObjectId(positionId);
        organizeRelationService.save(organizeRelationEntity);
    }


    
    @GetMapping("/queryInfoById/{id}")
    public PositionEntity queryInfoById(@PathVariable("id") String id) {
        return positionService.getInfo(id);
    }

    
    @PostMapping("/getPositionName")
    public List<PositionEntity> getPositionName(@RequestBody List<String> posiList, @RequestParam("filterEnabledMark") Boolean filterEnabledMark) {
        return positionService.getPositionName(posiList, false);
    }

    
    @GetMapping("/getByFullName/{fullName}")
    public PositionEntity getByFullName(@PathVariable("fullName") String fullName){
        return positionService.getByFullName(fullName);
    }

    
    @GetMapping("/getPosMap")
    public Map<String, Object> getPosMap(@RequestParam ("type") String type) {
        return "id-fullName".equals(type) ?  positionService.getPosMap() : positionService.getPosEncodeAndName();
    }

    
    @PostMapping("/getListByOrganizeId")
    public List<PositionEntity> getListByOrganizeId(@RequestBody List<String> ableDepIds) {
        return positionService.getListByOrganizeId(ableDepIds, false);
    }
}
