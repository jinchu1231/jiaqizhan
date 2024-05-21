package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jnpf.base.controller.SuperController;
import cn.hutool.core.util.ArrayUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jnpf.annotation.UserPermission;
import jnpf.base.ActionResult;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.permission.PermissionModel;
import jnpf.permission.model.userrelation.UserRelationForm;
import jnpf.permission.model.userrelation.UserRelationIdsVO;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户关系
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "用户关系", description = "UserRelation")
@RestController
@RequestMapping("/api/permission/UserRelation")
public class UserRelationController extends SuperController<UserRelationService, UserRelationEntity> {

    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserService userService;

    /**
     * 列表
     *
     * @param objectId 对象主键
     * @return
     */
    @Operation(summary = "获取岗位/角色/门户成员列表ids")
    @Parameters({
            @Parameter(name = "objectId", description = "对象主键", required = true)
    })
    @SaCheckPermission(value = {"permission.authorize", "permission.position", "permission.role"}, mode = SaMode.OR)
    @GetMapping("/{objectId}")
    public ActionResult<UserRelationIdsVO> listTree(@PathVariable("objectId") String objectId) {
        List<UserRelationEntity> data = userRelationService.getListByObjectId(objectId);
        List<String> ids = new ArrayList<>();
        for (UserRelationEntity entity : data) {
            ids.add(entity.getUserId());
        }
        UserRelationIdsVO vo = new UserRelationIdsVO();
        vo.setIds(ids);
        return ActionResult.success(vo);
    }

    /**
     * 保存
     *
     * @param objectId 对象主键
     * @param userRelationForm 页面数据
     * @return
     */
    @UserPermission
    @Operation(summary = "添加岗位或角色成员")
    @Parameters({
            @Parameter(name = "objectId", description = "对象主键", required = true),
            @Parameter(name = "userRelationForm", description = "页面数据", required = true)
    })
    @SaCheckPermission(value = {"permission.authorize", "permission.position", "permission.role"}, mode = SaMode.OR)
    @PostMapping("/{objectId}")
    public ActionResult save(@PathVariable("objectId") String objectId, @RequestBody UserRelationForm userRelationForm) {
        List<String> userIds = new ArrayList<>();
        if(userRelationForm.getObjectType().equals(PermissionConst.ROLE)){
            // 得到禁用的id
            List<UserRelationEntity> listByObjectId = userRelationService.getListByObjectId(objectId, PermissionConst.ROLE);
            List<String> collect = listByObjectId.stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
            List<String> collect1 = collect.stream().filter(t -> !userRelationForm.getUserIds().contains(t)).collect(Collectors.toList());
            userIds.addAll(collect1);
            Set<String> set = new HashSet<>(userRelationForm.getUserIds());
            set.addAll(userService.getUserList(collect).stream().map(UserEntity::getId).collect(Collectors.toList()));
            List<String> list = new ArrayList<>(set);
            userRelationService.roleSaveByUserIds(objectId, list);
        } else {
            // 得到禁用的id
            List<UserRelationEntity> listByObjectId = userRelationService.getListByObjectId(objectId, PermissionConst.POSITION);
            List<String> collect = listByObjectId.stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
            List<String> collect1 = collect.stream().filter(t -> !userRelationForm.getUserIds().contains(t)).collect(Collectors.toList());
            userIds.addAll(collect1);
            Set<String> set = new HashSet<>(userRelationForm.getUserIds());
            set.addAll(userService.getUserList(collect).stream().map(UserEntity::getId).collect(Collectors.toList()));
            List<String> list = new ArrayList<>(set);
            userRelationForm.setUserIds(list);
            userRelationService.saveObjectId(objectId,userRelationForm);
        }

        userService.delCurUser(null, ArrayUtil.toArray(userIds, String.class));
        return ActionResult.success(MsgCode.SU002.get());
    }

    
    @GetMapping("/getList/{userId}")
    public List<UserRelationEntity> getList(@PathVariable("userId") String userId) {
        return userRelationService.getListByUserId(userId);
    }

    
    @GetMapping("/getList")
    public List<UserRelationEntity> getList(@RequestParam("userId") String userId, @RequestParam("objectType") String objectType) {
        return userRelationService.getListByUserId(userId, objectType);
    }

    
    @PostMapping("/getListByUserIdAll")
    public List<UserRelationEntity> getListByUserIdAll(@RequestBody List<String> id) {
        return userRelationService.getListByUserIdAll(id);
    }

    
    @PostMapping("/getListByObjectIdAll")
    public List<UserRelationEntity> getListByObjectIdAll(@RequestBody List<String> id) {
        return userRelationService.getListByObjectIdAll(id);
    }

    
    @GetMapping("/getObjectVoList/{objectType}")
    public List<PermissionModel> getObjectVoList(@PathVariable("objectType")String objectType){
        return userRelationService.getObjectVoList(objectType);
    }

}
