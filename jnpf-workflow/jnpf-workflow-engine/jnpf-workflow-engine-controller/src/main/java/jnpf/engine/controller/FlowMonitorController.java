package jnpf.engine.controller;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.engine.entity.FlowEventLogEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowmonitor.FlowEventLogListVO;
import jnpf.engine.model.flowmonitor.FlowMonitorListVO;
import jnpf.engine.model.flowtask.FlowAssistModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.service.FlowEventLogService;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.permission.entity.UserEntity;
import jnpf.util.JsonUtil;
import jnpf.util.ServiceAllUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程监控
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "流程监控", description = "FlowMonitor")
@RestController
@RequestMapping("/api/workflow/Engine/FlowMonitor")
public class FlowMonitorController {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private FlowEventLogService flowEventLogService;

    /**
     * 获取流程监控列表
     *
     * @param paginationFlowTask 分页模型
     * @return
     */
    @Operation(summary = "获取流程监控列表")
    @GetMapping
    public ActionResult<PageListVO<FlowMonitorListVO>> list(PaginationFlowTask paginationFlowTask) {
        List<FlowTaskEntity> list = flowTaskService.getMonitorList(paginationFlowTask);
        List<UserEntity> userList = serviceUtil.getUserName(list.stream().map(t -> t.getCreatorUserId()).collect(Collectors.toList()));
        List<FlowMonitorListVO> listVO = new LinkedList<>();
        for (FlowTaskEntity taskEntity : list) {
            //用户名称赋值
            FlowMonitorListVO vo = JsonUtil.getJsonToBean(taskEntity, FlowMonitorListVO.class);
            UserEntity user = userList.stream().filter(t -> t.getId().equals(taskEntity.getCreatorUserId())).findFirst().orElse(null);
            vo.setUserName(user != null ? user.getRealName() + "/" + user.getAccount() : "");
            listVO.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationFlowTask, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 批量删除流程监控
     *
     * @param assistModel 流程删除模型
     * @return
     */
    @Operation(summary = "批量删除流程监控")
    @DeleteMapping
    @Parameters({
            @Parameter(name = "assistModel", description = "流程删除模型", required = true),
    })
    public ActionResult delete(@RequestBody FlowAssistModel assistModel) throws WorkFlowException {
        String[] taskId = assistModel.getIds().split(",");
        flowTaskService.delete(taskId);
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 获取事件日志列表
     *
     * @return
     */
    @Operation(summary = "获取事件日志列表")
    @GetMapping("/{id}/EventLog")
    public ActionResult getEventLog(@PathVariable("id") String id) {
        List<FlowEventLogEntity> logList = flowEventLogService.getList(ImmutableList.of(id));
        List<String> interfaceIdList = logList.stream().map(FlowEventLogEntity::getInterfaceId).collect(Collectors.toList());
        List<DataInterfaceEntity> interfaceList = serviceUtil.getInterfaceList(interfaceIdList);
        List<FlowEventLogListVO> list = new ArrayList<>();
        for (FlowEventLogEntity logEntity : logList) {
            FlowEventLogListVO listVO = JsonUtil.getJsonToBean(logEntity, FlowEventLogListVO.class);
            DataInterfaceEntity dataInterface = interfaceList.stream().filter(t -> t.getId().equals(listVO.getInterfaceId())).findFirst().orElse(null);
            if (dataInterface != null) {
                listVO.setInterfaceCode(dataInterface.getEnCode());
                listVO.setInterfaceName(dataInterface.getFullName());
            }
            list.add(listVO);
        }
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

}
