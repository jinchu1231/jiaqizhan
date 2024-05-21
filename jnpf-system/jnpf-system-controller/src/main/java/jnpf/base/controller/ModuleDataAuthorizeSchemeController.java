package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;
import jnpf.base.model.moduledataauthorizescheme.DataAuthorizeSchemeCrForm;
import jnpf.base.model.moduledataauthorizescheme.DataAuthorizeSchemeInfoVO;
import jnpf.base.model.moduledataauthorizescheme.DataAuthorizeSchemeListVO;
import jnpf.base.model.moduledataauthorizescheme.DataAuthorizeSchemeUpForm;
import jnpf.base.service.ModuleDataAuthorizeSchemeService;
import jnpf.base.vo.ListVO;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
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
import java.util.List;

/**
 * 数据权限方案
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "数据权限方案", description = "ModuleDataAuthorizeScheme")
@RestController
@RequestMapping("/api/system/ModuleDataAuthorizeScheme")
public class ModuleDataAuthorizeSchemeController extends SuperController<ModuleDataAuthorizeSchemeService, ModuleDataAuthorizeSchemeEntity> {

    @Autowired
    private ModuleDataAuthorizeSchemeService schemeService;

    /**
     * 列表
     *
     * @param moduleId 功能主键
     * @return ignore
     */
    @Operation(summary = "方案列表")
    @Parameters({
            @Parameter(name = "moduleId", description = "功能主键", required = true)
    })
    @GetMapping("/{moduleId}/List")
    public ActionResult<ListVO<DataAuthorizeSchemeListVO>> list(@PathVariable("moduleId") String moduleId) {
        List<ModuleDataAuthorizeSchemeEntity> data = schemeService.getList(moduleId);
        List<DataAuthorizeSchemeListVO> list = JsonUtil.getJsonToList(data, DataAuthorizeSchemeListVO.class);
        ListVO<DataAuthorizeSchemeListVO> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     * @throws DataException ignore
     */
    @Operation(summary = "获取方案信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/{id}")
    public ActionResult<DataAuthorizeSchemeInfoVO> info(@PathVariable("id") String id) throws DataException {
        ModuleDataAuthorizeSchemeEntity entity = schemeService.getInfo(id);
        DataAuthorizeSchemeInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, DataAuthorizeSchemeInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param dataAuthorizeSchemeCrForm 实体对象
     * @return ignore
     */
    @Operation(summary = "新建方案")
    @Parameters({
            @Parameter(name = "dataAuthorizeSchemeCrForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.menu")
    @PostMapping
    public ActionResult create(@RequestBody @Valid DataAuthorizeSchemeCrForm dataAuthorizeSchemeCrForm) {
        ModuleDataAuthorizeSchemeEntity entity = JsonUtil.getJsonToBean(dataAuthorizeSchemeCrForm, ModuleDataAuthorizeSchemeEntity.class);
        // 判断fullName是否重复
        if (schemeService.isExistByFullName(entity.getId(), entity.getFullName(), entity.getModuleId())) {
            return ActionResult.fail("已存在相同名称");
        }
        // 判断encode是否重复
        if (schemeService.isExistByEnCode(entity.getId(), entity.getEnCode(), entity.getModuleId())) {
            return ActionResult.fail("已存在相同编码");
        }
        schemeService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新
     *
     * @param id                        主键值
     * @param dataAuthorizeSchemeUpForm 实体对象
     * @return ignore
     */
    @Operation(summary = "更新方案")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "dataAuthorizeSchemeUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.menu")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid DataAuthorizeSchemeUpForm dataAuthorizeSchemeUpForm) {
        ModuleDataAuthorizeSchemeEntity entity = JsonUtil.getJsonToBean(dataAuthorizeSchemeUpForm, ModuleDataAuthorizeSchemeEntity.class);
        // 判断encode是否重复
        if ("jnpf_alldata".equals(entity.getEnCode())) {
            return ActionResult.fail("修改失败，该方案不允许编辑");
        }
        // 判断fullName是否重复
        if (schemeService.isExistByFullName(id, entity.getFullName(), entity.getModuleId())) {
            return ActionResult.fail("已存在相同名称");
        }
        // 判断encode是否重复
        if (schemeService.isExistByEnCode(id, entity.getEnCode(), entity.getModuleId())) {
            return ActionResult.fail("已存在相同编码");
        }
        boolean flag = schemeService.update(id, entity);
        if (!flag) {
            return ActionResult.success(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "删除方案")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.menu")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ModuleDataAuthorizeSchemeEntity entity = schemeService.getInfo(id);
        if (entity != null) {
            schemeService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }


    @GetMapping("/getList")
    public List<ModuleDataAuthorizeSchemeEntity> getList() {
        return schemeService.getList();
    }

    
    @PostMapping("/getListByModuleId")
    public List<ModuleDataAuthorizeSchemeEntity> getListByModuleId(@RequestBody List<String> ids) {
        return schemeService.getListByModuleId(ids);
    }

    
    @PostMapping("/getListByIds")
    public List<ModuleDataAuthorizeSchemeEntity> getListByIds(@RequestBody List<String> ids) {
        return schemeService.getListByIds(ids);
    }
}
