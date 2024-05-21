package jnpf.base.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.entity.ModuleButtonEntity;
import jnpf.base.model.button.ButtonListVO;
import jnpf.base.model.button.ButtonTreeListModel;
import jnpf.base.model.button.ButtonTreeListVO;
import jnpf.base.model.button.ModuleButtonCrForm;
import jnpf.base.model.button.ModuleButtonInfoVO;
import jnpf.base.model.button.ModuleButtonUpForm;
import jnpf.base.service.ModuleButtonService;
import jnpf.base.vo.ListVO;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.util.JsonUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 按钮权限
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "按钮权限", description = "ModuleButton")
@RestController
@RequestMapping("/api/system/ModuleButton")
public class ModuleButtonController extends SuperController<ModuleButtonService, ModuleButtonEntity> {

    @Autowired
    private ModuleButtonService moduleButtonService;

    /**
     * 按钮按钮权限列表
     *
     * @param menuId 功能主键
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取按钮权限列表")
    @Parameters({
            @Parameter(name = "menuId", description = "功能主键", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/{menuId}/List")
    public ActionResult list(@PathVariable("menuId") String menuId, Pagination pagination) {
        List<ModuleButtonEntity> data = moduleButtonService.getListByModuleIds(menuId, pagination);
        List<ButtonTreeListModel> treeList = JsonUtil.getJsonToList(data, ButtonTreeListModel.class);
        List<SumTree<ButtonTreeListModel>> sumTrees = TreeDotUtils.convertListToTreeDot(treeList);
        if (data.size() > sumTrees.size()) {
            List<ButtonTreeListVO> list = JsonUtil.getJsonToList(sumTrees, ButtonTreeListVO.class);
            ListVO<ButtonTreeListVO> treeVo = new ListVO<>();
            treeVo.setList(list);
            return ActionResult.success(treeVo);
        }
        List<ButtonListVO> list = JsonUtil.getJsonToList(treeList, ButtonListVO.class);
        ListVO<ButtonListVO> treeVo1 = new ListVO<>();
        treeVo1.setList(list);
        return ActionResult.success(treeVo1);
    }


    /**
     * 按钮按钮权限列表
     *
     * @param menuId 功能主键
     * @return
     */
    @Operation(summary = "获取按钮权限下拉框")
    @Parameters({
            @Parameter(name = "menuId", description = "功能主键", required = true)
    })
    @GetMapping("/api/system/{menuId}/Selector")
    public ActionResult<ListVO<ButtonTreeListVO>> selectList(@PathVariable("menuId") String menuId) {
        List<ModuleButtonEntity> data = moduleButtonService.getListByModuleIds(menuId);
        List<ButtonTreeListModel> treeList = JsonUtil.getJsonToList(data, ButtonTreeListModel.class);
        List<SumTree<ButtonTreeListModel>> sumTrees = TreeDotUtils.convertListToTreeDot(treeList);
        List<ButtonTreeListVO> list = JsonUtil.getJsonToList(sumTrees, ButtonTreeListVO.class);
        ListVO<ButtonTreeListVO> treeVo = new ListVO<>();
        treeVo.setList(list);
        return ActionResult.success(treeVo);
    }


    /**
     * 获取按钮权限信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取按钮权限信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/{id}")
    public ActionResult<ModuleButtonInfoVO> info(@PathVariable("id") String id)throws DataException {
        ModuleButtonEntity entity = moduleButtonService.getInfo(id);
        ModuleButtonInfoVO vo = JsonUtil.getJsonToBeanEx(entity, ModuleButtonInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建按钮权限
     *
     * @param moduleButtonCrForm 实体对象
     * @return
     */
    @Operation(summary = "新建按钮权限")
    @Parameters({
            @Parameter(name = "moduleButtonCrForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.menu")
    @PostMapping
    public ActionResult create(@RequestBody ModuleButtonCrForm moduleButtonCrForm) {
        ModuleButtonEntity entity = JsonUtil.getJsonToBean(moduleButtonCrForm, ModuleButtonEntity.class);
        if (moduleButtonService.isExistByEnCode(entity.getModuleId(), entity.getEnCode(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        moduleButtonService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新按钮权限
     *
     * @param id 主键值
     * @param moduleButtonUpForm 实体对象
     * @return
     */
    @Operation(summary = "更新按钮权限")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "moduleButtonUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.menu")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody ModuleButtonUpForm moduleButtonUpForm) {
        ModuleButtonEntity entity = JsonUtil.getJsonToBean(moduleButtonUpForm, ModuleButtonEntity.class);
        if (moduleButtonService.isExistByEnCode(entity.getModuleId(), entity.getEnCode(), id)) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        boolean flag = moduleButtonService.update(id, entity);
        if (flag == false) {
            return ActionResult.success(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除按钮权限
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除按钮权限")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.menu")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ModuleButtonEntity entity = moduleButtonService.getInfo(id);
        if (entity != null) {
            moduleButtonService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
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
    @SaCheckPermission("system.menu")
    @PutMapping("/{id}/Actions/State")
    public ActionResult upState(@PathVariable("id") String id) {
        ModuleButtonEntity entity = moduleButtonService.getInfo(id);
        if (entity.getEnabledMark() == null || entity.getEnabledMark() == 1) {
            entity.setEnabledMark(0);
        } else {
            entity.setEnabledMark(1);
        }
       boolean flag= moduleButtonService.update(id, entity);
        if(flag==false){
            return ActionResult.success("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    @GetMapping("/getList")
    public List<ModuleButtonEntity> getList() {
        return moduleButtonService.getListByModuleIds();
    }


    @PostMapping("/getListByModuleId")
    public List<ModuleButtonEntity> getListByModuleIds(@RequestBody List<String> ids) {
        return moduleButtonService.getListByModuleIds(ids);
    }


    @PostMapping("/getListByIds")
    public List<ModuleButtonEntity> getListByIds(@RequestBody List<String> ids) {
        return moduleButtonService.getListByIds(ids);
    }
}
