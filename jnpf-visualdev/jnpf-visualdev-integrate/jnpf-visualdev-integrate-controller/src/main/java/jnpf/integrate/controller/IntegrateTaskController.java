package jnpf.integrate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.integrate.entity.IntegrateNodeEntity;
import jnpf.integrate.entity.IntegrateQueueEntity;
import jnpf.integrate.entity.IntegrateTaskEntity;
import jnpf.integrate.model.integrate.IntegratePageModel;
import jnpf.integrate.model.integratetask.IntegrateQueueListVO;
import jnpf.integrate.model.integratetask.IntegrateTaskInfo;
import jnpf.integrate.model.integratetask.IntegrateTaskListVO;
import jnpf.integrate.model.integratetask.IntegrateTaskModel;
import jnpf.integrate.service.IntegrateNodeService;
import jnpf.integrate.service.IntegrateQueueService;
import jnpf.integrate.service.IntegrateTaskService;
import jnpf.integrate.util.IntegrateUtil;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "集成助手日志", description = "IntegrateTask" )
@RestController
@RequestMapping("/api/visualdev/IntegrateTask" )
public class IntegrateTaskController extends SuperController<IntegrateTaskService, IntegrateTaskEntity> {

    @Autowired
    private IntegrateTaskService integrateTaskService;
    @Autowired
    private IntegrateNodeService integrateNodeService;
    @Autowired
    private IntegrateUtil integrateUtil;
    @Autowired
    private IntegrateQueueService integrateQueueService;

    /**
     * 列表
     *
     * @return
     */
    @Operation(summary = "日志列表" )
    @GetMapping
    public ActionResult<PageListVO<IntegrateTaskListVO>> list(IntegratePageModel pagination) {
        List<IntegrateTaskEntity> data = integrateTaskService.getList(pagination);
        List<IntegrateTaskListVO> list = JsonUtil.getJsonToList(data, IntegrateTaskListVO.class);
        for (IntegrateTaskListVO taskListVO : list) {
            taskListVO.setIsRetry("0".equals(taskListVO.getParentId()) ? 0 : 1);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除" )
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @DeleteMapping("/{id}" )
    public ActionResult delete(@PathVariable("id" ) String id) {
        IntegrateTaskEntity entity = integrateTaskService.getInfo(id);
        if (entity != null) {
            integrateTaskService.delete(entity);
            return ActionResult.success("删除成功" );
        }
        return ActionResult.fail("删除失败，数据不存在" );
    }

    /**
     * 日志列表
     *
     * @return
     */
    @Operation(summary = "执行列表" )
    @GetMapping("/queueList" )
    public ActionResult<List<IntegrateQueueListVO>> queueList() {
        List<IntegrateQueueEntity> list = integrateQueueService.getList();
        List<IntegrateQueueListVO> listVO = JsonUtil.getJsonToList(list, IntegrateQueueListVO.class);
        return ActionResult.success(listVO);
    }

    /**
     * 日志列表
     *
     * @return
     */
    @Operation(summary = "日志详情" )
    @GetMapping("/{id}" )
    public ActionResult<IntegrateTaskInfo> list(@PathVariable("id" ) String id) {
        IntegrateTaskEntity taskEntity = integrateTaskService.getInfo(id);
        List<IntegrateNodeEntity> nodeList = integrateNodeService.getList(new ArrayList() {{
            add(id);
        }}, null);
        List<IntegrateTaskModel> list = JsonUtil.getJsonToList(nodeList, IntegrateTaskModel.class);
        for (IntegrateTaskModel taskModel : list) {
            boolean isType = "0".equals(taskModel.getParentId());
            taskModel.setType(isType ? 1 : 0);
        }
        IntegrateTaskInfo info = new IntegrateTaskInfo();
        info.setList(list);
        info.setData(taskEntity.getData());
        return ActionResult.success(info);
    }

    /**
     * 节点重试
     *
     * @return
     */
    @Operation(summary = "节点重试" )
    @GetMapping(value = "/{id}/nodeRetry" )
    public ActionResult taskNode(@PathVariable("id" ) String id, String nodeId) {
        IntegrateTaskEntity taskEntity = integrateTaskService.getInfo(id);
        if (taskEntity != null) {
            integrateUtil.integrate(id, "0", nodeId);
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.fail(MsgCode.FA007.get());
    }

    /**
     * 重试
     *
     * @return
     */
    @Operation(summary = "重试" )
    @PutMapping(value = "/{id}/retry" )
    public ActionResult ImportData(@PathVariable("id" ) String id) {
        IntegrateTaskEntity taskEntity = integrateTaskService.getInfo(id);
        if (taskEntity != null) {
            integrateUtil.integrate(id, id, "0" );
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.fail(MsgCode.FA007.get());
    }

}
