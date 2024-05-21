package jnpf.visualdata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.util.JsonUtil;
import jnpf.visualdata.entity.VisualGlobEntity;
import jnpf.visualdata.model.VisualPageVO;
import jnpf.visualdata.model.visualglob.*;
import jnpf.visualdata.service.VisualGlobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 全局变量
 *
 * @author JNPF开发平台组
 * @version V3.5.0
 * @copyright 引迈信息技术有限公司
 * @date 2023年7月7日
 */
@RestController
@Tag(name = "全局变量", description = "glob")
@RequestMapping("/api/blade-visual/visual-global")
public class VisualGlobController extends SuperController<VisualGlobService, VisualGlobEntity> {

    @Autowired
    private VisualGlobService gloService;

    /**
     * 分页
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "分页")
    @GetMapping("/list")
    public ActionResult<VisualPageVO<VisualGlobListVO>> list(VisualGlobPaginationModel pagination) {
        List<VisualGlobEntity> data = gloService.getList(pagination);
        List<VisualGlobListVO> list = JsonUtil.getJsonToList(data, VisualGlobListVO.class);
        VisualPageVO paginationVO = JsonUtil.getJsonToBean(pagination, VisualPageVO.class);
        paginationVO.setRecords(list);
        return ActionResult.success(paginationVO);
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
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult<VisualGlobInfoVO> info(@RequestParam("id")String id) {
        VisualGlobEntity entity = gloService.getInfo(id);
        VisualGlobInfoVO vo = JsonUtil.getJsonToBean(entity, VisualGlobInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 新增
     *
     * @param recordCrForm 数据模型
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping("/save")
    @Parameters({
            @Parameter(name = "recordCrForm", description = "数据模型",required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult create(@RequestBody VisualGlobCrForm recordCrForm) {
        VisualGlobEntity entity = JsonUtil.getJsonToBean(recordCrForm, VisualGlobEntity.class);
        gloService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param recordUpForm 数据模型
     * @return
     */
    @Operation(summary = "修改")
    @PostMapping("/update")
    @Parameters({
            @Parameter(name = "recordUpForm", description = "数据模型",required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult update(@RequestBody VisualGlobUpForm recordUpForm) {
        VisualGlobEntity entity = JsonUtil.getJsonToBean(recordUpForm, VisualGlobEntity.class);
        gloService.update(entity.getId(), entity);
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
        VisualGlobEntity entity = gloService.getInfo(ids);
        if (entity != null) {
            gloService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }


}
