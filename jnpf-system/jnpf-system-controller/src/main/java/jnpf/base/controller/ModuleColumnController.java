package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.entity.ModuleColumnEntity;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.Template6.ColumnListField;
import jnpf.base.model.column.ColumnBatchForm;
import jnpf.base.model.column.ColumnListVO;
import jnpf.base.model.column.ModuleColumnCrForm;
import jnpf.base.model.column.ModuleColumnInfoVO;
import jnpf.base.model.column.ModuleColumnUpForm;
import jnpf.base.model.module.PropertyJsonModel;
import jnpf.base.service.ModuleColumnService;
import jnpf.base.service.ModuleService;
import jnpf.base.service.VisualdevService;
import jnpf.base.vo.ListVO;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 列表权限
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "列表权限", description = "ModuleColumn")
@Validated
@RestController
@RequestMapping("/api/system/ModuleColumn")
public class  ModuleColumnController extends SuperController<ModuleColumnService, ModuleColumnEntity> {
    @Autowired
    private ModuleColumnService moduleColumnService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private VisualdevService visualdevService;

    /**
     * 获取列表权限信息列表
     *
     * @param moduleId   功能主键
     * @param pagination 分页参数
     * @return ignore
     */
    @Operation(summary = "获取列表权限列表")
    @Parameters({
            @Parameter(name = "moduleId", description = "功能主键", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/{moduleId}/Fields")
    public ActionResult<ListVO<ColumnListVO>> getList(@PathVariable("moduleId") String moduleId, Pagination pagination) {
        List<ModuleColumnEntity> list = moduleColumnService.getList(moduleId, pagination);
        List<ColumnListVO> voList = JsonUtil.getJsonToList(list, ColumnListVO.class);
        voList.stream().forEach(t-> {
            String enCode = t.getEnCode();
            if (StringUtil.isNotEmpty(enCode)) {
                if (enCode.contains("-")){
                    enCode = enCode.substring(enCode.indexOf("-")+1);
                }
                t.setEnCode(enCode.replace("jnpf_" + t.getBindTable() + "_jnpf_", ""));
            }
        });
        ListVO<ColumnListVO> vo = new ListVO<>();
        vo.setList(voList);
        return ActionResult.success(vo);
    }

    /**
     * 菜单列表权限
     *
     * @param moduleId 功能主键
     * @return ignore
     */
    @Operation(summary = "菜单列表权限")
    @Parameters({
            @Parameter(name = "moduleId", description = "功能主键", required = true)
    })
    @SaCheckPermission("system.menu")
    @GetMapping("/{moduleId}/FieldList")
    public ActionResult<List<Map<String, String>>> fieldList(@PathVariable("moduleId") String moduleId) {
        List<Map<String, String>> list = new ArrayList<>();
        // 得到菜单id
        ModuleEntity entity = moduleService.getInfo(moduleId);
        if (entity != null) {
            PropertyJsonModel model = JsonUtil.getJsonToBean(entity.getPropertyJson(), PropertyJsonModel.class);
            if (model == null) {
                model = new PropertyJsonModel();
            }
            VisualdevEntity info = visualdevService.getInfo(model.getModuleId());
            boolean isPc = entity.getCategory().equalsIgnoreCase("web");
            if (info!=null){
                Object columnData = isPc ? info.getColumnData() : info.getAppColumnData() ;
                if (Objects.nonNull(columnData)){
                    ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnData.toString(), ColumnDataModel.class);
                    List<ColumnListField> columnListFields = JsonUtil.getJsonToList(columnDataModel.getDefaultColumnList(), ColumnListField.class);
                    if (Objects.nonNull(columnListFields)) {
                        columnListFields.stream().forEach(col -> {
                            Map<String, String> dataMap = new HashMap<>();
                            dataMap.put("field", col.getProp());
                            dataMap.put("fieldName", col.getLabel());
                            list.add(dataMap);
                        });
                    }
                }
            }
        }
        return ActionResult.success(list);
    }

    /**
     * 获取列表权限信息
     *
     * @param id 主键值
     * @return ignore
     * @throws DataException ignore
     */
    @Operation(summary = "获取列表权限信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @GetMapping("/{id}")
    public ActionResult<ModuleColumnInfoVO> info(@PathVariable("id") String id) throws DataException {
        ModuleColumnEntity entity = moduleColumnService.getInfo(id);
            String enCode = entity.getEnCode();
            if (StringUtil.isNotEmpty(enCode)) {
                if (enCode.contains("-") && entity.getFieldRule()==2){
                    enCode = enCode.substring(enCode.indexOf("-")+1);
                    entity.setEnCode(enCode);
                }
                if (Objects.equals(entity.getFieldRule(),1) && entity.getBindTable()!=null){
                    entity.setEnCode(enCode.replace("jnpf_" + entity.getBindTable() + "_jnpf_", ""));
                }
            }
        ModuleColumnInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, ModuleColumnInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建列表权限
     *
     * @param moduleColumnCrForm 实体对象
     * @return ignore
     */
    @Operation(summary = "新建列表权限")
    @Parameters({
            @Parameter(name = "moduleColumnCrForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.menu")
    @PostMapping
    public ActionResult create(@RequestBody @Valid ModuleColumnCrForm moduleColumnCrForm) {
        ModuleEntity moduleEntity = moduleService.getInfo(moduleColumnCrForm.getModuleId());
        ModuleColumnEntity entity = JsonUtil.getJsonToBean(moduleColumnCrForm, ModuleColumnEntity.class);

        if (moduleEntity != null){
            PropertyJsonModel model = JsonUtil.getJsonToBean(moduleEntity.getPropertyJson(), PropertyJsonModel.class);
            if (model == null) {
                model = new PropertyJsonModel();
            }
            if (entity.getFieldRule() == 1 && StringUtil.isNotEmpty(moduleColumnCrForm.getBindTable())) {
                String enCode = "jnpf_" + moduleColumnCrForm.getBindTable() + "_jnpf_" + entity.getEnCode();
                entity.setEnCode(enCode);
            }

            if (entity.getFieldRule() == 2 && StringUtil.isNotEmpty(moduleColumnCrForm.getChildTableKey())) {
                // 得到bean
//                Object bean = SpringContext.getBean("visualdevServiceImpl");
//                Object method = ReflectionUtil.invokeMethod(bean, "getTableNameToKey", new Class[]{String.class}, new Object[]{model.getModuleId()});
//                Map<String, Object> map = JsonUtil.entityToMap(method);
//
//                String enCode = map.get(moduleColumnCrForm.getBindTable().toLowerCase()) + "-" + entity.getEnCode();
                String enCode = moduleColumnCrForm.getChildTableKey() + "-" + entity.getEnCode();
                entity.setEnCode(enCode);
            }
        }
        if (moduleColumnService.isExistByEnCode(entity.getModuleId(), entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("字段名称不能重复");
        }
        moduleColumnService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新列表权限
     *
     * @param id 主键值
     * @param moduleColumnUpForm 实体对象
     * @return ignore
     */
    @Operation(summary = "更新列表权限")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "moduleColumnUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.menu")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ModuleColumnUpForm moduleColumnUpForm) {
        ModuleEntity moduleEntity = moduleService.getInfo(moduleColumnUpForm.getModuleId());
        ModuleColumnEntity entity = JsonUtil.getJsonToBean(moduleColumnUpForm, ModuleColumnEntity.class);
        if (moduleEntity != null){
            PropertyJsonModel model = JsonUtil.getJsonToBean(moduleEntity.getPropertyJson(), PropertyJsonModel.class);
            if (model == null) {
                model = new PropertyJsonModel();
            }
            if (entity.getFieldRule() == 1 && StringUtil.isNotEmpty(moduleColumnUpForm.getBindTable())) {
                String enCode = "jnpf_" + moduleColumnUpForm.getBindTable() + "_jnpf_" + entity.getEnCode();
                entity.setEnCode(enCode);
            }

            if (entity.getFieldRule() == 2 && StringUtil.isNotEmpty(moduleColumnUpForm.getChildTableKey())) {
//                // 得到bean
//                Object bean = SpringContext.getBean("visualdevServiceImpl");
//                Object method = ReflectionUtil.invokeMethod(bean, "getTableNameToKey", new Class[]{String.class}, new Object[]{model.getModuleId()});
//                Map<String, Object> map = JsonUtil.entityToMap(method);

//                String enCode = map.get(moduleColumnUpForm.getBindTable().toLowerCase()) + "-" + entity.getEnCode();
                String enCode = moduleColumnUpForm.getChildTableKey() + "-" + entity.getEnCode();
                entity.setEnCode(enCode);
            }
        }
        if (moduleColumnService.isExistByEnCode(entity.getModuleId(), entity.getEnCode(), id)) {
            return ActionResult.fail("字段名称不能重复");
        }
        boolean flag = moduleColumnService.update(id, entity);
        if (!flag) {
            return ActionResult.success(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除列表权限
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "删除列表权限")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.menu")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ModuleColumnEntity entity = moduleColumnService.getInfo(id);
        if (entity != null) {
            moduleColumnService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 更新列表权限状态
     *
     * @param id 主键值
     * @return ignore
     */
    @Operation(summary = "更新列表权限状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.menu")
    @PutMapping("/{id}/Actions/State")
    public ActionResult upState(@PathVariable("id") String id) {
        ModuleColumnEntity entity = moduleColumnService.getInfo(id);
        if (entity.getEnabledMark() == null || "1".equals(String.valueOf(entity.getEnabledMark()))) {
            entity.setEnabledMark(0);
        } else {
            entity.setEnabledMark(1);
        }
        boolean flag = moduleColumnService.update(id, entity);
        if (!flag) {
            return ActionResult.success(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 批量新建
     *
     * @param columnBatchForm 权限模型
     * @return ignore
     */
    @Operation(summary = "批量新建列表权限")
    @Parameters({
            @Parameter(name = "columnBatchForm", description = "权限模型", required = true)
    })
    @SaCheckPermission("system.menu")
    @PostMapping("/Actions/Batch")
    public ActionResult batchCreate(@RequestBody @Valid ColumnBatchForm columnBatchForm) {
        List<ModuleColumnEntity> entitys = columnBatchForm.getColumnJson() != null ? JsonUtil.getJsonToList(columnBatchForm.getColumnJson(), ModuleColumnEntity.class) : new ArrayList<>();
        List<String> name = new ArrayList<>();
        for (ModuleColumnEntity entity : entitys) {
            entity.setModuleId(columnBatchForm.getModuleId());
            if (entity.getFieldRule() == 1 ) {
                String enCode = "jnpf_" + entity.getBindTable() + "_jnpf_" + entity.getEnCode();
                entity.setEnCode(enCode);
            }
            if (entity.getFieldRule() == 2 ) {
                String enCode = entity.getChildTableKey() + "-" + entity.getEnCode();
                entity.setEnCode(enCode);
            }
            if (moduleColumnService.isExistByEnCode(entity.getModuleId(), entity.getEnCode(), null)) {
                return ActionResult.fail(MsgCode.EXIST002.get());
            }
            if (name.contains(entity.getEnCode())) {
                return ActionResult.fail(MsgCode.EXIST002.get());
            }
            name.add(entity.getEnCode());
        }
        moduleColumnService.create(entitys);
        return ActionResult.success(MsgCode.SU001.get());
    }

    @GetMapping("/getList")
    public List<ModuleColumnEntity> getList() {
        return moduleColumnService.getList();
    }

    @PostMapping("/getListByModuleId")
    public List<ModuleColumnEntity> getListByModuleId(@RequestBody List<String> ids) {
        return moduleColumnService.getListByModuleId(ids);
    }

    @PostMapping("/getListByIds")
    public List<ModuleColumnEntity> getListByIds(@RequestBody List<String> ids) {
        return moduleColumnService.getListByIds(ids);
    }
}
