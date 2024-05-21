package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.SystemEntity;
import jnpf.base.model.base.SystemApiListModel;
import jnpf.base.model.base.SystemApiModel;
import jnpf.base.model.base.SystemCrModel;
import jnpf.base.model.base.SystemListVO;
import jnpf.base.model.base.SystemPageVO;
import jnpf.base.model.base.SystemServiceByIdsModel;
import jnpf.base.model.base.SystemUpModel;
import jnpf.base.model.base.SystemVO;
import jnpf.base.service.CommonWordsService;
import jnpf.base.service.SystemService;
import jnpf.base.vo.ListVO;
import jnpf.constant.JnpfConst;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.message.util.OnlineUserModel;
import jnpf.message.util.OnlineUserProvider;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统控制器
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/21 15:33
 */
@Tag(name = "系统", description = "system")
@RestController
@RequestMapping("/api/system/System")
public class SystemController extends SuperController<SystemService, SystemEntity> {

    @Autowired
    private SystemService systemService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private CommonWordsService commonWordsService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrganizeAdministratorService organizeAdminTratorService;

    /**
     * 获取系统列表
     *
     * @param page 关键字
     * @return ignore
     */
    @Operation(summary = "获取系统列表")
    @SaCheckPermission("system.menu")
    @GetMapping
    public ActionResult<ListVO<SystemListVO>> list(SystemPageVO page) {
        Boolean enabledMark = false;
        if (ObjectUtil.equal(page.getEnabledMark(), "0")) {
            enabledMark = null;
        }
        if (ObjectUtil.equal(page.getEnabledMark(), "1")) {
            enabledMark = true;
        }
        List<SystemEntity> list = systemService.getList(page.getKeyword(), enabledMark, true, page.getSelector(), true, new ArrayList<>());
        List<SystemListVO> jsonToList = JsonUtil.getJsonToList(list, SystemListVO.class);
        return ActionResult.success(new ListVO<>(jsonToList));
    }

    /**
     * 获取系统详情
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "获取系统详情")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/{id}")
    public ActionResult<SystemVO> info(@PathVariable("id") String id) {
        SystemEntity entity = systemService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        SystemVO jsonToBean = JsonUtil.getJsonToBean(entity, SystemVO.class);
        return ActionResult.success(jsonToBean);
    }

    /**
     * 获取系统详情
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "获取系统详情")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/getPermission/{id}")
    public ActionResult<SystemVO> getPermission(@PathVariable("id") String id) {
        SystemEntity entity = systemService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        SystemVO jsonToBean = JsonUtil.getJsonToBean(entity, SystemVO.class);
        return ActionResult.success(jsonToBean);
    }

    /**
     * 新建系统
     *
     * @param systemCrModel 新建模型
     * @return ignore
     */
    @Operation(summary = "新建系统")
    @Parameters({
            @Parameter(name = "systemCrModel", description = "新建模型", required = true)
    })
    @SaCheckPermission("system.menu")
    @PostMapping
    public ActionResult create(@RequestBody SystemCrModel systemCrModel) {
        SystemEntity entity = JsonUtil.getJsonToBean(systemCrModel, SystemEntity.class);
        if (systemService.isExistFullName(entity.getId(), entity.getFullName())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (systemService.isExistEnCode(entity.getId(), entity.getEnCode())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        systemService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 修改系统
     *
     * @param id            主键
     * @param systemUpModel 修改模型
     * @return ignore
     */
    @Operation(summary = "修改系统")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "systemCrModel", description = "修改模型", required = true)
    })
    @SaCheckPermission("system.menu")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody SystemUpModel systemUpModel) {
        SystemEntity systemEntity = systemService.getInfo(id);
        if (systemEntity == null) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        // 主系统不允许禁用
        if (systemEntity.getIsMain() != null && systemEntity.getIsMain() == 1) {
            if (systemUpModel.getEnabledMark() == 0) {
                return ActionResult.fail("更新失败，主系统不允许禁用");
            }
            if (!systemEntity.getEnCode().equals(systemUpModel.getEnCode())) {
                return ActionResult.fail("更新失败，主系统不允许修改编码");
            }
        }
        SystemEntity entity = JsonUtil.getJsonToBean(systemUpModel, SystemEntity.class);
        entity.setIsMain(systemEntity.getIsMain() != null ? systemEntity.getIsMain() : 0);
        if (systemService.isExistFullName(id, entity.getFullName())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (systemService.isExistEnCode(id, entity.getEnCode())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        systemService.update(id, entity);
        // 如果禁用了系统，则需要将系统
        if (systemEntity.getEnabledMark() == 1 && entity.getEnabledMark() == 0) {
            sentMessage("应用已被禁用，正为您切换应用", systemEntity);
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除系统
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "删除系统")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("system.menu")
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        SystemEntity entity = systemService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA003.get());
        }
        if (ObjectUtil.equal(entity.getIsMain(), 1)) {
            return ActionResult.fail("主系统不允许删除");
        }
        // 系统绑定审批常用语时不允许被删除
        if (commonWordsService.existSystem(id)) {
            return ActionResult.fail("系统在审批常用语中被使用，不允许删除");
        } else {
            systemService.delete(id);
            // 通知下线
            sentMessage("应用已被删除，正为您切换应用", entity);
        }
        return ActionResult.success(MsgCode.SU003.get());
    }

//    
//    @GetMapping("/getMainSystem")
//    public SystemEntity getMainSystem() {
//        return systemService.getMainSystem();
//    }

    
    @PostMapping("/getList")
    public List<SystemEntity> getList(@RequestBody SystemApiListModel model) {
        return systemService.getList(model.getKeyword(), model.getFilterEnableMark(), model.getVerifyAuth(), model.getFilterMain(), model.getIsList(), model.getModuleAuthorize());
    }

    
    @PostMapping("/getListByIds")
    public List<SystemEntity> getListByIds(@RequestBody SystemServiceByIdsModel model) {
        return systemService.getListByIds(model.getIds(), model.getModuleAuthorize());
    }

    
    @GetMapping("/getInfoById")
    public SystemEntity getInfoById(@RequestParam("systemId") String systemId) {
        return systemService.getInfo(systemId);
    }

    
    @GetMapping("/getInfoByEnCode")
    public SystemEntity getInfoByEnCode(@RequestParam("enCode") String enCode) {
        return systemService.getInfoByEnCode(enCode);
    }

//    
//    @PostMapping("/getMainSys")
//    public List<SystemEntity> getMainSys(@RequestBody List<String> systemIds) {
//        return systemService.getMainSys(systemIds);
//    }

//    
//    @PostMapping("/getCurrentUserSystem")
//    public List<String> getCurrentUserSystem(@RequestBody UserInfo userInfo) {
//        userInfo = UserProvider.getUser();
//        return XSSEscape.escapeObj(systemService.getCurrentUserSystem(userInfo));
//    }

    
    @PostMapping("/findSystemAdmin")
    public List<SystemEntity> findSystemAdmin(@RequestBody SystemApiModel model) {
        return systemService.findSystemAdmin(model.getMark(), model.getMainSystemCode(), model.getModuleAuthorize());
    }
    private void sentMessage(String message, SystemEntity entity) {
        // 如果禁用了系统，则需要将系统
        List<OnlineUserModel> onlineUserList = OnlineUserProvider.getOnlineUserList();
        SystemEntity mainSystem = systemService.getInfoByEnCode(JnpfConst.MAIN_SYSTEM_CODE);
        // 所有在线用户信息
        Map<String, UserEntity> userEntityMap = userService.getUserName(onlineUserList.stream().map(OnlineUserModel::getUserId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        for (OnlineUserModel item : onlineUserList) {
            UserEntity userEntity = userEntityMap.get(item.getUserId());
            String systemId = userEntity.getSystemId();
            if (item.getIsMobileDevice()) {
                systemId = userEntity.getAppSystemId();
            }
            if (userEntity == null ||
                    ((Objects.equals(userEntity.getIsAdministrator(), 1)
                            || organizeAdminTratorService.getOrganizeAdministratorEntity(item.getUserId(), PermissionConst.SYSTEM, false).size() > 0
                            || organizeAdminTratorService.getOrganizeAdministratorEntity(item.getUserId(), PermissionConst.MODULE, false).size()> 0)
                            && systemId.equals(mainSystem.getId())
                    )
                    || (!userProvider.get(item.getToken()).getSystemIds().contains(entity.getId()))
            ) {
                continue;
            }
            if (item.getWebSocket().isOpen()) {
                Map<String, String> maps = new HashMap<>(1);
                maps.put("msg", message);
                if (item.getIsMobileDevice()) {
                    maps.put("method", "logout");
                    maps.put("msg", message.contains("禁用") ? "应用已被禁用，正在退出！" : "应用已被删除，正在退出！");
                } else {
                    maps.put("method", "refresh");
                }
                if (StringUtil.isNotEmpty(userProvider.get().getTenantId())) {
                    if (userProvider.get().getTenantId().equals(item.getTenantId())) {
                        item.getWebSocket().getAsyncRemote().sendText(JsonUtil.getObjectToString(maps));
                    }
                } else {
                    item.getWebSocket().getAsyncRemote().sendText(JsonUtil.getObjectToString(maps));
                }
            }
        }
    }
}
