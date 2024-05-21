package jnpf.visualdata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.util.JsonUtil;
import jnpf.visualdata.entity.VisualMapEntity;
import jnpf.visualdata.model.VisualPageVO;
import jnpf.visualdata.model.VisualPagination;
import jnpf.visualdata.model.visualmap.VisualMapCrForm;
import jnpf.visualdata.model.visualmap.VisualMapInfoVO;
import jnpf.visualdata.model.visualmap.VisualMapListVO;
import jnpf.visualdata.model.visualmap.VisualMapUpForm;
import jnpf.visualdata.service.VisualMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 大屏地图
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@RestController
@Tag(name = "大屏地图", description = "map")
@RequestMapping("/api/blade-visual/map")
public class VisualMapController extends SuperController<VisualMapService, VisualMapEntity> {

    @Autowired
    private VisualMapService mapService;

    /**
     * 分页
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "分页")
    @GetMapping("/list")
    public ActionResult<VisualPageVO<VisualMapListVO>> list(VisualPagination pagination) {
        List<VisualMapEntity> data = mapService.getListWithColnums(pagination, VisualMapEntity::getId, VisualMapEntity::getName);
        List<VisualMapListVO> list = JsonUtil.getJsonToList(data, VisualMapListVO.class);
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
    public ActionResult<VisualMapInfoVO> info(@RequestParam("id") String id) {
        VisualMapEntity entity = mapService.getInfo(id);
        VisualMapInfoVO vo = JsonUtil.getJsonToBean(entity, VisualMapInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新增
     *
     * @param mapCrForm 地图模型
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping("/save")
    @Parameters({
            @Parameter(name = "mapCrForm", description = "地图模型", required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult create(@RequestBody VisualMapCrForm mapCrForm) {
        VisualMapEntity entity = JsonUtil.getJsonToBean(mapCrForm, VisualMapEntity.class);
        mapService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param mapUpForm 地图模型
     * @return
     */
    @Operation(summary = "修改")
    @PostMapping("/update")
    @Parameters({
            @Parameter(name = "mapUpForm", description = "地图模型", required = true),
    })
    @SaCheckPermission("onlineDev.dataScreen")
    public ActionResult update(@RequestBody VisualMapUpForm mapUpForm) {
        VisualMapEntity entity = JsonUtil.getJsonToBean(mapUpForm, VisualMapEntity.class);
        boolean flag = mapService.update(mapUpForm.getId(), entity);
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
    public ActionResult delete(@RequestParam("ids") String ids) {
        VisualMapEntity entity = mapService.getInfo(ids);
        if (entity != null) {
            mapService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 数据详情
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "数据详情")
    @GetMapping("/data")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public Map<String, Object> dataInfo(@RequestParam("id") String id) {
        VisualMapEntity entity = mapService.getInfo(id);
        Map<String, Object> data = JsonUtil.stringToMap(entity.getData());
        return data;
    }

}
