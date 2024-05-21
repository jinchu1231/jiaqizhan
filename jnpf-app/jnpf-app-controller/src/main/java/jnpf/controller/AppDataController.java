package jnpf.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.entity.AppDataEntity;
import jnpf.model.AppDataCrForm;
import jnpf.model.AppDataListAllVO;
import jnpf.model.AppDataListVO;
import jnpf.model.AppFlowListAllVO;
import jnpf.service.AppDataService;
import jnpf.util.JsonUtil;
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
import java.util.List;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-08
 */
@Tag(name = "app常用数据", description = "data")
@RestController
@RequestMapping("/api/app/Data")
public class AppDataController extends SuperController<AppDataService, AppDataEntity> {

    @Autowired
    private AppDataService appDataService;

    /**
     * 常用数据
     *
     * @param type 类型
     * @return
     */
    @Operation(summary = "常用数据")
    @GetMapping
    @Parameters({
            @Parameter(name = "type", description = "类型"),
    })
    public ActionResult<ListVO<AppDataListVO>> list(@RequestParam("type") String type) {
        List<AppDataEntity> list = appDataService.getList(type);
        List<AppDataListVO> data = JsonUtil.getJsonToList(list, AppDataListVO.class);
        ListVO listVO = new ListVO();
        listVO.setList(data);
        return ActionResult.success(listVO);
    }

    /**
     * 新建
     *
     * @param appDataCrForm 新建模型
     * @return
     */
    @PostMapping
    @Operation(summary = "新建")
    @Parameters({
            @Parameter(name = "appDataCrForm", description = "常用模型",required = true),
    })
    public ActionResult create(@RequestBody @Valid AppDataCrForm appDataCrForm) {
        AppDataEntity entity = JsonUtil.getJsonToBean(appDataCrForm, AppDataEntity.class);
        if (appDataService.isExistByObjectId(entity.getObjectId(), appDataCrForm.getSystemId()))
            return ActionResult.fail("常用数据已存在");
        appDataService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 删除
     *
     * @param objectId 主键
     * @return
     */
    @Operation(summary = "删除")
    @DeleteMapping("/{objectId}")
    @Parameters({
            @Parameter(name = "objectId", description = "主键", required = true),
    })
    public ActionResult create(@PathVariable("objectId") String objectId) {
        AppDataEntity entity = appDataService.getInfo(objectId);
        if (entity != null) {
            appDataService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 所有流程
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "所有流程")
    @GetMapping("/getFlowList")
    public ActionResult<PageListVO<AppFlowListAllVO>> getFlowList(FlowPagination pagination) {
        List<AppFlowListAllVO> list = appDataService.getFlowList(pagination);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 所有应用
     *
     * @return
     */
    @Operation(summary = "所有应用")
    @GetMapping("/getDataList")
    public ActionResult<ListVO<AppDataListAllVO>> getAllList() {
        List<AppDataListAllVO> result = appDataService.getDataList("2");
        ListVO listVO = new ListVO();
        listVO.setList(result);
        return ActionResult.success(listVO);
    }

    /**
     * 删除app常用数据
     *
     * @param id 主键
     */
    @GetMapping("/deleObject/{id}")
    public void deleObject(@PathVariable("id") String id) {
        appDataService.delete(id);
    }

    /**
     * 删除app常用数据
     *
     * @param id 主键
     */
    @GetMapping("/getInfo")
    public AppDataEntity getData(@RequestParam("id") String id) {
        return appDataService.getInfo(id);
    }

    /**
     * 修改常用菜单
     *
     * @param appDataEntity 修改常用菜单实体
     */
    @PutMapping("/updateData")
    public void updateData(@RequestBody AppDataEntity appDataEntity) {
        appDataService.updateById(appDataEntity);
    }
}
