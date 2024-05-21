package jnpf.visualdata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.util.JsonUtil;
import jnpf.visualdata.entity.VisualCategoryEntity;
import jnpf.visualdata.model.VisualPageVO;
import jnpf.visualdata.model.VisualPagination;
import jnpf.visualdata.model.visualcategory.VisualCategoryCrForm;
import jnpf.visualdata.model.visualcategory.VisualCategoryInfoVO;
import jnpf.visualdata.model.visualcategory.VisualCategoryListVO;
import jnpf.visualdata.model.visualcategory.VisualCategoryUpForm;
import jnpf.visualdata.service.VisualCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 大屏分类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@RestController
@Tag(name = "大屏分类", description = "category")
@RequestMapping("/api/blade-visual/category")
public class VisualCategoryController extends SuperController<VisualCategoryService, VisualCategoryEntity> {

    @Autowired
    private VisualCategoryService categoryService;

    /**
     * 列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping("/page")
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult<VisualPageVO<VisualCategoryListVO>> list(VisualPagination pagination) {
        List<VisualCategoryEntity> data = categoryService.getList(pagination);
        List<VisualCategoryListVO> list = JsonUtil.getJsonToList(data, VisualCategoryListVO.class);
        VisualPageVO paginationVO = JsonUtil.getJsonToBean(pagination, VisualPageVO.class);
        paginationVO.setRecords(list);
        return ActionResult.success(paginationVO);
    }

    /**
     * 列表
     *
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping("/list")
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult<List<VisualCategoryListVO>> list() {
        List<VisualCategoryEntity> data = categoryService.getList();
        List<VisualCategoryListVO> list = JsonUtil.getJsonToList(data, VisualCategoryListVO.class);
        return ActionResult.success(list);
    }

    /**
     * 详情
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "详情")
    @GetMapping("/detail")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<VisualCategoryInfoVO> info(@RequestParam("id") String id) {
        VisualCategoryEntity entity = categoryService.getInfo(id);
        VisualCategoryInfoVO vo = JsonUtil.getJsonToBean(entity, VisualCategoryInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新增
     *
     * @param categoryCrForm 大屏分类模型
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping("/save")
    @Parameters({
            @Parameter(name = "categoryCrForm", description = "大屏分类模型",required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult create(@RequestBody @Valid VisualCategoryCrForm categoryCrForm) {
        VisualCategoryEntity entity = JsonUtil.getJsonToBean(categoryCrForm, VisualCategoryEntity.class);
        if (categoryService.isExistByValue(entity.getCategoryvalue(), entity.getId())) {
            return ActionResult.fail("模块键值已存在");
        }
        categoryService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param categoryUpForm 大屏分类模型
     * @return
     */
    @Operation(summary = "修改")
    @PostMapping("/update")
    @Parameters({
            @Parameter(name = "categoryUpForm", description = "大屏分类模型",required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult update(@RequestBody VisualCategoryUpForm categoryUpForm) {
        VisualCategoryEntity entity = JsonUtil.getJsonToBean(categoryUpForm, VisualCategoryEntity.class);
        if (categoryService.isExistByValue(entity.getCategoryvalue(), entity.getId())) {
            return ActionResult.fail("模块键值已存在");
        }
        boolean flag = categoryService.update(categoryUpForm.getId(), entity);
        if (!flag) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param ids 主键
     * @return
     */
    @Operation(summary = "删除")
    @PostMapping("/remove")
    @Parameters({
            @Parameter(name = "ids", description = "主键", required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult delete(String ids) {
        VisualCategoryEntity entity = categoryService.getInfo(ids);
        if (entity != null) {
            categoryService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

}
