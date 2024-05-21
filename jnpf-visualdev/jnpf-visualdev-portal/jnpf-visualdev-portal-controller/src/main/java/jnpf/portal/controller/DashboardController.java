package jnpf.portal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.engine.model.flowtask.FlowTaskListModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.service.FlowDelegateService;
import jnpf.engine.service.FlowTaskService;
import jnpf.message.model.NoticeModel;
import jnpf.message.model.NoticeVO;
import jnpf.message.service.MessageService;
import jnpf.portal.model.EmailVO;
import jnpf.portal.model.FlowTodo;
import jnpf.portal.model.FlowTodoCountVO;
import jnpf.portal.model.FlowTodoVO;
import jnpf.portal.model.MyFlowTodoVO;
import jnpf.service.EmailReceiveService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主页控制器
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "主页控制器", description = "Home")
@RestController
@RequestMapping("api/visualdev/Dashboard")
public class DashboardController {
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowDelegateService flowDelegateService;
    @Autowired
    private EmailReceiveService emailReceiveService;
    @Autowired
    private MessageService messageService;





    /**
     * 获取我的待办
     *
     * @return
     */
    @Operation(summary = "获取我的待办")
    @PostMapping("/FlowTodoCount")
    public ActionResult getFlowTodoCount(@RequestBody FlowTodo flowTodo) {
        FlowTodoCountVO vo = new FlowTodoCountVO();
        PaginationFlowTask pagination = new PaginationFlowTask();
        pagination.setIsPage(false);
        List<FlowTaskListModel> toBeReviewedList = flowTaskService.getWaitList(pagination);
        if (flowTodo.getToBeReviewedType().size() > 0) {
            toBeReviewedList = toBeReviewedList.stream().filter(t -> flowTodo.getToBeReviewedType().contains(t.getFlowCategory())).collect(Collectors.toList());
        }
        vo.setToBeReviewed(toBeReviewedList.size());
        vo.setEntrust(flowDelegateService.getList().size());
        List<FlowTaskListModel> flowDoneList = flowTaskService.getTrialList(pagination);
        if (flowTodo.getFlowDoneType().size() > 0) {
            flowDoneList = flowDoneList.stream().filter(t -> flowTodo.getFlowDoneType().contains(t.getFlowCategory())).collect(Collectors.toList());
        }
        vo.setFlowDone(flowDoneList.size());
        List<FlowTaskListModel> flowCirculate = flowTaskService.getCirculateList(pagination);
        if (flowTodo.getFlowCirculateType().size() > 0) {
            flowCirculate = flowCirculate.stream().filter(t -> flowTodo.getFlowCirculateType().contains(t.getFlowCategory())).collect(Collectors.toList());
        }
        vo.setFlowCirculate(flowCirculate.size());
        return ActionResult.success(vo);
    }

    /**
     * 获取通知公告
     *
     * @return
     */
    @Operation(summary = "获取通知公告")
    @PostMapping("/Notice")
    public ActionResult getNotice(@RequestBody NoticeModel noticeModel) {
        List<NoticeVO> list = JsonUtil.getJsonToList(messageService.getNoticeList(noticeModel.getTypeList()), NoticeVO.class);
        ListVO<NoticeVO> voList = new ListVO();
        voList.setList(list);
        return ActionResult.success(voList);
    }

    /**
     * 获取未读邮件
     *
     * @return
     */
    @Operation(summary = "获取未读邮件")
    @GetMapping("/Email")
    public ActionResult getEmail() {
        List<EmailVO> list = JsonUtil.getJsonToList(emailReceiveService.getDashboardReceiveList(), EmailVO.class);
        ListVO<EmailVO> voList = new ListVO<>();
        voList.setList(list);
        return ActionResult.success(voList);
    }

    /**
     * 获取待办事项
     *
     * @return
     */
    @Operation(summary = "获取待办事项")
    @GetMapping("/FlowTodo")
    public ActionResult getFlowTodo() {
        PaginationFlowTask pagination = new PaginationFlowTask();
        pagination.setPageSize(20L);
        pagination.setCurrentPage(1L);
        pagination.setDelegateType(false);
        List<FlowTaskListModel> taskList = flowTaskService.getWaitList(pagination);
        List<FlowTodoVO> list = new LinkedList<>();
        for (FlowTaskListModel taskEntity : taskList) {
            FlowTodoVO vo = JsonUtil.getJsonToBean(taskEntity, FlowTodoVO.class);
            vo.setTaskNodeId(taskEntity.getThisStepId());
            vo.setTaskOperatorId(taskEntity.getId());
            vo.setType(2);
            list.add(vo);
        }
        ListVO voList = new ListVO<>();
        voList.setList(list);
        return ActionResult.success(voList);
    }

    /**
     * 获取我的待办事项
     *
     * @return
     */
    @Operation(summary = "获取我的待办事项")
    @GetMapping("/MyFlowTodo")
    public ActionResult getMyFlowTodo() {
        PaginationFlowTask pagination = new PaginationFlowTask();
        pagination.setIsPage(false);
        List<MyFlowTodoVO> list = JsonUtil.getJsonToList(flowTaskService.getWaitList(pagination), MyFlowTodoVO.class);
        ListVO<MyFlowTodoVO> voList = new ListVO<>();
        voList.setList(list);
        return ActionResult.success(voList);
    }
}
