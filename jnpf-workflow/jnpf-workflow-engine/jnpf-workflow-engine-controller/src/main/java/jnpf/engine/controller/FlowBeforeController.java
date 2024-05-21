package jnpf.engine.controller;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.enums.FlowNodeEnum;
import jnpf.engine.enums.FlowRecordEnum;
import jnpf.engine.enums.FlowTaskStatusEnum;
import jnpf.engine.model.flowbefore.FlowBatchModel;
import jnpf.engine.model.flowbefore.FlowBeforeInfoVO;
import jnpf.engine.model.flowbefore.FlowBeforeListVO;
import jnpf.engine.model.flowbefore.FlowSummary;
import jnpf.engine.model.flowbefore.FlowTemplateAllModel;
import jnpf.engine.model.flowcandidate.FlowCandidateUserModel;
import jnpf.engine.model.flowcandidate.FlowCandidateVO;
import jnpf.engine.model.flowcandidate.FlowRejectVO;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.engine.model.flowtask.FlowTaskListModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.model.flowtask.TaskNodeModel;
import jnpf.engine.model.flowtasknode.TaskNodeListModel;
import jnpf.engine.service.FlowTaskNewService;
import jnpf.engine.service.FlowTaskNodeService;
import jnpf.engine.service.FlowTaskOperatorRecordService;
import jnpf.engine.service.FlowTaskOperatorService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateJsonService;
import jnpf.engine.util.FlowJsonUtil;
import jnpf.engine.util.FlowNature;
import jnpf.engine.util.FlowTaskUtil;
import jnpf.exception.WorkFlowException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.util.JsonUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.visiual.JnpfKeyConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 待我审核
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "待我审核", description = "FlowBefore")
@RestController
@RequestMapping("/api/workflow/Engine/FlowBefore")
public class FlowBeforeController {


    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FlowTaskUtil flowTaskUtil;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTemplateJsonService flowTemplateJsonService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private FlowTaskOperatorRecordService flowTaskOperatorRecordService;
    @Autowired
    private FlowTaskNodeService flowTaskNodeService;
    @Autowired
    private FlowTaskNewService flowTaskNewService;

    /**
     * 获取待我审核列表
     *
     * @param category           分类
     * @param paginationFlowTask 分页模型
     * @return
     */
    @Operation(summary = "获取待我审核列表(有带分页)，1-待办事宜，2-已办事宜，3-抄送事宜,4-批量审批")
    @GetMapping("/List/{category}")
    @Parameters({
            @Parameter(name = "category", description = "分类", required = true),
    })
    public ActionResult<PageListVO<FlowBeforeListVO>> list(@PathVariable("category") String category, PaginationFlowTask paginationFlowTask) {
        List<FlowTaskListModel> data = new ArrayList<>();
        paginationFlowTask.setDelegateType(true);
        if (FlowNature.WAIT.equals(category)) {
            data.addAll(flowTaskService.getWaitList(paginationFlowTask));
        } else if (FlowNature.TRIAL.equals(category)) {
            data.addAll(flowTaskService.getTrialList(paginationFlowTask));
        } else if (FlowNature.CIRCULATE.equals(category)) {
            data.addAll(flowTaskService.getCirculateList(paginationFlowTask));
        } else if (FlowNature.BATCH.equals(category)) {
            paginationFlowTask.setIsBatch(1);
            data.addAll(flowTaskService.getWaitList(paginationFlowTask));
        }
        List<FlowBeforeListVO> listVO = new LinkedList<>();
        List<UserEntity> userList = serviceUtil.getUserName(data.stream().map(FlowTaskListModel::getCreatorUserId).collect(Collectors.toList()));
        boolean isBatch = FlowNature.BATCH.equals(category);
        List<FlowTaskNodeEntity> taskNodeList = new ArrayList<>();
        List<String> taskNodeIdList = data.stream().map(FlowTaskListModel::getThisStepId).collect(Collectors.toList());
        if (isBatch) {
            taskNodeList.addAll(flowTaskNodeService.getList(taskNodeIdList, FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson));
        }
        for (FlowTaskListModel task : data) {
            FlowBeforeListVO vo = JsonUtil.getJsonToBean(task, FlowBeforeListVO.class);
            //用户名称赋值
            UserEntity user = userList.stream().filter(t -> t.getId().equals(vo.getCreatorUserId())).findFirst().orElse(null);
            vo.setUserName(user != null ? user.getRealName() + "/" + user.getAccount() : "");
            FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(t -> t.getId().equals(task.getThisStepId())).findFirst().orElse(null);
            if (isBatch && taskNode != null) {
                ChildNodeList childNode = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
                vo.setApproversProperties(JsonUtil.getObjectToString(childNode.getProperties()));
            }
            vo.setFlowVersion(StringUtil.isEmpty(vo.getFlowVersion()) ? "" : vo.getFlowVersion());
            listVO.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationFlowTask, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 获取待我审批信息
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "获取待我审批信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<FlowBeforeInfoVO> info(@PathVariable("id") String id, FlowModel flowModel) throws WorkFlowException {
        flowModel.setId(id);
        FlowBeforeInfoVO vo = flowTaskNewService.getBeforeInfo(flowModel);
        //处理当前默认值
        if (vo != null && vo.getFlowFormInfo() != null && StringUtil.isNotEmpty(vo.getFlowFormInfo().getPropertyJson()) && vo.getFlowFormInfo().getFormType() == 2) {
            UserInfo userInfo = userProvider.get();
            Map<String, Integer> havaDefaultCurrentValue = new HashMap<>();
            vo.getFlowFormInfo().setPropertyJson(setDefaultCurrentValue(vo.getFlowFormInfo().getPropertyJson(), havaDefaultCurrentValue, userInfo));
        }
        return ActionResult.success(vo);
    }

    /**
     * 待我审核审核
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "待我审核审核")
    @PostMapping("/Audit/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult audit(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setId(id);
        flowTaskNewService.audit(flowModel);
        return ActionResult.success("审核成功");
    }

    /**
     * 保存草稿
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "保存草稿")
    @PostMapping("/SaveAudit/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult saveAudit(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        FlowTaskOperatorEntity flowTaskOperatorEntity = flowTaskOperatorService.getInfo(id);
        if (flowTaskOperatorEntity != null) {
            Map<String, Object> formDataAll = flowModel.getFormData();
            flowTaskOperatorEntity.setDraftData(JsonUtil.getObjectToString(formDataAll));
            flowTaskOperatorService.update(flowTaskOperatorEntity);
            return ActionResult.success("保存成功");
        }
        return ActionResult.fail(MsgCode.FA001.get());
    }

    /**
     * 审批汇总
     *
     * @param id       主键
     * @param category 类型
     * @param type     类型
     * @return
     */
    @Operation(summary = "审批汇总")
    @GetMapping("/RecordList/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<List<FlowSummary>> recordList(@PathVariable("id") String id, String category, String type) {
        List<FlowSummary> flowSummaries = flowTaskNewService.recordList(id, category, type);
        return ActionResult.success(flowSummaries);
    }

    /**
     * 待我审核驳回
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "待我审核驳回")
    @PostMapping("/Reject/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult reject(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setId(id);
        flowTaskNewService.reject(flowModel);
        return ActionResult.success("退回成功");
    }

    /**
     * 待我审核转办
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "待我审核转办")
    @PostMapping("/Transfer/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult transfer(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setId(id);
        flowTaskNewService.transfer(flowModel);
        return ActionResult.success("转办成功");
    }

    /**
     * 待我审核转办
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "待我审核加签")
    @PostMapping("/freeApprover/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult freeApprover(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setId(id);
        flowTaskNewService.audit(flowModel);
        return ActionResult.success("加签成功");
    }

    /**
     * 待我审核撤回审核
     * 注意：在撤销流程时要保证你的下一节点没有处理这条记录；如已处理则无法撤销流程。
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "待我审核撤回审核")
    @PostMapping("/Recall/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult recall(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        FlowTaskOperatorRecordEntity operatorRecord = flowTaskOperatorRecordService.getInfo(id);
        FlowTaskNodeEntity taskNode = flowTaskNodeService.getInfo(operatorRecord.getTaskNodeId(), FlowTaskNodeEntity::getId);
        //拒绝不撤回
        if (FlowNature.ProcessCompletion.equals(operatorRecord.getHandleStatus())) {
            throw new WorkFlowException("当前流程被退回，无法撤回流程");
        }
        if (taskNode == null) {
            return ActionResult.fail("流程已撤回，不能重复操作！");
        }
        if (FlowRecordEnum.swerve.getCode().equals(operatorRecord.getHandleStatus())) {
            return ActionResult.fail("撤回失败,转向数据无法撤回");
        }
        if (FlowRecordEnum.revoke.getCode().equals(operatorRecord.getStatus())) {
            return ActionResult.fail("流程已撤回，不能重复操作！");
        }
        if (taskNode != null && !FlowRecordEnum.revoke.getCode().equals(operatorRecord.getStatus())) {
            flowModel.setUserInfo(userProvider.get());
            flowTaskNewService.recall(id, operatorRecord, flowModel);
        }
        return ActionResult.success("撤回成功");
    }

    /**
     * 待我审核终止审核
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "待我审核终止审核")
    @PostMapping("/Cancel/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult cancel(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        FlowTaskEntity entity = flowTaskService.getInfo(id, FlowTaskEntity::getFlowType);
        if (entity != null) {
            if (Objects.equals(entity.getFlowType(), 1)) {
                return ActionResult.fail("功能流程不能终止");
            }
            flowModel.setUserInfo(userProvider.get());
            List<String> idList = ImmutableList.of(id);
            flowTaskNewService.cancel(idList, flowModel);
            return ActionResult.success("操作成功");
        }
        return ActionResult.fail(MsgCode.FA009.get());
    }

    /**
     * 指派人
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "指派人")
    @PostMapping("/Assign/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult assign(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setUserInfo(userProvider.get());
        flowTaskNewService.assign(id, flowModel);
        return ActionResult.success("指派成功");
    }

    /**
     * 获取候选人
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "获取候选人节点")
    @PostMapping("/Candidates/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult<FlowCandidateVO> candidates(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setUserInfo(userProvider.get());
        FlowCandidateVO candidate = flowTaskNewService.candidates(id, flowModel, false);
        return ActionResult.success(candidate);
    }

    /**
     * 获取候选人
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "获取候选人")
    @PostMapping("/CandidateUser/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult<PageListVO<FlowCandidateUserModel>> candidateUser(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setUserInfo(userProvider.get());
        List<FlowCandidateUserModel> candidate = flowTaskNewService.candidateUser(id, flowModel);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(flowModel, PaginationVO.class);
        return ActionResult.page(candidate, paginationVO);
    }

    /**
     * 批量审批引擎
     *
     * @return
     */
    @Operation(summary = "批量审批引擎")
    @GetMapping("/BatchFlowSelector")
    public ActionResult<List<FlowBatchModel>> batchFlowSelector() {
        List<FlowBatchModel> batchFlowList = flowTaskService.batchFlowSelector();
        return ActionResult.success(batchFlowList);
    }

    /**
     * 拒绝下拉框
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "拒绝下拉框")
    @GetMapping("/RejectList/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<FlowRejectVO> rejectList(@PathVariable("id") String id) throws WorkFlowException {
        FlowRejectVO vo = flowTaskNewService.rejectList(id, false);
        return ActionResult.success(vo);
    }

    /**
     * 引擎节点
     *
     * @param id 主键
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "引擎节点")
    @GetMapping("/NodeSelector/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<List<FlowBatchModel>> nodeSelector(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateAllModel template = flowTaskUtil.templateJson(id);
        String templateJson = template.getTemplateJson().getFlowTemplateJson();
        List<FlowBatchModel> batchList = new ArrayList<>();
        ChildNode childNodeAll = JsonUtil.getJsonToBean(templateJson, ChildNode.class);
        //获取流程节点
        List<ChildNodeList> nodeListAll = new ArrayList<>();
        List<ConditionList> conditionListAll = new ArrayList<>();
        //递归获取条件数据和节点数据
        FlowJsonUtil.getTemplateAll(childNodeAll, nodeListAll, conditionListAll);
        List<String> type = ImmutableList.of(FlowNature.NodeSubFlow, FlowNature.NodeStart);
        for (ChildNodeList childNodeList : nodeListAll) {
            if (!type.contains(childNodeList.getCustom().getType())) {
                FlowBatchModel batchModel = new FlowBatchModel();
                batchModel.setFullName(childNodeList.getProperties().getTitle());
                batchModel.setId(childNodeList.getCustom().getNodeId());
                batchList.add(batchModel);
            }
        }
        return ActionResult.success(batchList);
    }

    /**
     * 流程批量类型下拉
     *
     * @param id 主键
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "流程批量类型下拉")
    @GetMapping("/BatchFlowJsonList/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<List<FlowBatchModel>> batchFlowJsonList(@PathVariable("id") String id) {
        List<String> taskIdList = flowTaskOperatorService.getBatchList().stream().map(FlowTaskOperatorEntity::getTaskId).collect(Collectors.toList());
        List<FlowTaskEntity> taskListAll = flowTaskService.getOrderStaList(taskIdList);
        List<String> flowIdList = taskListAll.stream().filter(t -> t.getTemplateId().equals(id)).map(FlowTaskEntity::getFlowId).collect(Collectors.toList());
        List<FlowTemplateJsonEntity> templateJsonList = flowTemplateJsonService.getTemplateJsonList(flowIdList);
        List<FlowBatchModel> listVO = new ArrayList<>();
        for (FlowTemplateJsonEntity entity : templateJsonList) {
            FlowBatchModel vo = JsonUtil.getJsonToBean(entity, FlowBatchModel.class);
            vo.setFullName(vo.getFullName() + "(v" + entity.getVersion() + ")");
            listVO.add(vo);
        }
        return ActionResult.success(listVO);
    }

    /**
     * 批量审批
     *
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "批量审批")
    @PostMapping("/BatchOperation")
    @Parameters({
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult batchOperation(@RequestBody FlowModel flowModel) throws WorkFlowException {
        flowModel.setUserInfo(userProvider.get());
        flowTaskNewService.batch(flowModel);
        return ActionResult.success("批量操作完成");
    }

    /**
     * 批量获取候选人
     *
     * @param flowId         流程主键
     * @param taskOperatorId 代办主键
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "批量获取候选人")
    @GetMapping("/BatchCandidate")
    public ActionResult<FlowCandidateVO> batchCandidate(String flowId, String taskOperatorId) throws WorkFlowException {
        FlowModel flowModel = new FlowModel();
        flowModel.setUserInfo(userProvider.get());
        flowModel.setFlowId(flowId);
        FlowCandidateVO candidate = flowTaskNewService.batchCandidates(flowId, taskOperatorId, flowModel);
        return ActionResult.success(candidate);
    }

    /**
     * 消息跳转工作流
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "消息跳转工作流")
    @GetMapping("/{id}/Info")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult taskOperatorId(@PathVariable("id") String id) throws WorkFlowException {
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        FlowTaskEntity flowTask = flowTaskService.getInfo(operator.getTaskId());
        FlowModel flowModel = new FlowModel();
        flowModel.setUserInfo(userProvider.get());
        flowTaskNewService.permissions(operator.getHandleId(), flowTask, operator, "", flowModel);
        Map<String, Object> map = new HashMap<>();
        if (!FlowNature.ProcessCompletion.equals(operator.getCompletion())) {
            map.put("isCheck", true);
        } else {
            map.put("isCheck", false);
        }
        return ActionResult.success(map);
    }

    /**
     * 节点下拉框
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "节点下拉框")
    @GetMapping("/Selector/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<List<TaskNodeModel>> selector(@PathVariable("id") String id) {
        List<String> nodetype = ImmutableList.of(FlowNature.NodeStart, FlowNature.NodeSubFlow, FlowNature.EndRound);
        TaskNodeListModel nodeListModel = TaskNodeListModel.builder().id(id).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> list = flowTaskNodeService.getList(nodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getCandidates,
                FlowTaskNodeEntity::getCompletion, FlowTaskNodeEntity::getNodeType,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getNodeName,
                FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getNodePropertyJson
        );
        flowTaskUtil.nodeList(list);
        list = list.stream().filter(t -> !nodetype.contains(t.getNodeType())).collect(Collectors.toList());
        List<TaskNodeModel> nodeList = JsonUtil.getJsonToList(list, TaskNodeModel.class);
        return ActionResult.success(nodeList);
    }

    /**
     * 变更或者复活
     *
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "变更或者复活")
    @PostMapping("/Change")
    @Parameters({
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult change(@RequestBody FlowModel flowModel) throws WorkFlowException {
        FlowTaskEntity info = flowTaskService.getInfo(flowModel.getTaskId());
        if (FlowTaskStatusEnum.Revoke.getCode().equals(info.getStatus()) || FlowTaskStatusEnum.Cancel.getCode().equals(info.getStatus()) || FlowTaskStatusEnum.Draft.getCode().equals(info.getStatus())) {
            throw new WorkFlowException("该流程不能操作");
        }
        flowModel.setUserInfo(userProvider.get());
        flowTaskNewService.change(flowModel);
        String msg = flowModel.getResurgence() ? "复活成功" : "变更成功";
        return ActionResult.success(msg);
    }

    /**
     * 子流程数据
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "子流程数据")
    @GetMapping("/SubFlowInfo/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<List<FlowBeforeInfoVO>> subFlowInfo(@PathVariable("id") String id) throws WorkFlowException {
        FlowTaskNodeEntity taskNode = flowTaskNodeService.getInfo(id, FlowTaskNodeEntity::getNodePropertyJson);
        List<FlowBeforeInfoVO> listVO = new ArrayList<>();
        if (taskNode != null) {
            ChildNodeList childNodeList = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
            List<String> flowTaskIdList = new ArrayList<>();
            flowTaskIdList.addAll(childNodeList.getCustom().getAsyncTaskList());
            flowTaskIdList.addAll(childNodeList.getCustom().getTaskId());
            for (String taskId : flowTaskIdList) {
                FlowModel flowModel = new FlowModel();
                flowModel.setId(taskId);
                FlowBeforeInfoVO vo = flowTaskNewService.getBeforeInfo(flowModel);
                listVO.add(vo);
            }
        }
        return ActionResult.success(listVO);
    }

    /**
     * 流程类型下拉
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "流程类型下拉")
    @GetMapping("/Suspend/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult suspend(@PathVariable("id") String id) {
        List<FlowTaskEntity> childList = flowTaskService.getChildList(id, FlowTaskEntity::getId, FlowTaskEntity::getIsAsync);
        boolean isAsync = childList.stream().filter(t -> FlowNature.ChildAsync.equals(t.getIsAsync())).count() > 0;
        return ActionResult.success(isAsync);
    }

    /**
     * 流程挂起
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "流程挂起")
    @PostMapping("/Suspend/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult suspend(@PathVariable("id") String id, @RequestBody FlowModel flowModel) {
        flowModel.setUserInfo(userProvider.get());
        flowTaskNewService.suspend(id, flowModel, true);
        return ActionResult.success("挂起成功");
    }

    /**
     * 流程恢复
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "流程恢复")
    @PostMapping("/Restore/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult restore(@PathVariable("id") String id, @RequestBody FlowModel flowModel) {
        flowModel.setUserInfo(userProvider.get());
        flowModel.setSuspend(false);
        flowTaskNewService.suspend(id, flowModel, false);
        return ActionResult.success("恢复成功");
    }

    //递归处理默认当前配置
    private String setDefaultCurrentValue(String configJson, Map<String, Integer> havaDefaultCurrentValue, UserInfo userInfo) {
        if (StringUtil.isEmpty(configJson)) {
            return configJson;
        }
        Map<String, Object> configJsonMap = JsonUtil.stringToMap(configJson.trim());
        if (configJsonMap == null && configJsonMap.isEmpty()) {
            return configJson;
        }
        List<UserRelationEntity> userRelationList = serviceUtil.getListByUserIdAll(ImmutableList.of(userInfo.getUserId()));

        int isChange = 0;
        //处理字段
        Object fieldsObj = configJsonMap.get("fields");
        List<Map<String, Object>> fieldsList = null;
        if (fieldsObj != null) {
            fieldsList = (List<Map<String, Object>>) fieldsObj;
            if (fieldsList != null && !fieldsList.isEmpty()) {
                setDefaultCurrentValue(userRelationList, fieldsList, userInfo, "add");
                configJsonMap.put("fields", fieldsList);
                isChange = 1;
            }
        }

        if (isChange == 1) {
            return JsonUtil.getObjectToString(configJsonMap);
        } else {
            return configJson;
        }
    }

    private void setDefaultCurrentValue(List<UserRelationEntity> userRelationList, List<Map<String, Object>> itemList, UserInfo userInfo, String parseFlag) {
        for (int i = 0, len = itemList.size(); i < len; i++) {
            Map<String, Object> itemMap = itemList.get(i);
            if (itemMap == null || itemMap.isEmpty()) {
                continue;
            }
            Map<String, Object> configMap = (Map<String, Object>) itemMap.get("__config__");
            if (configMap == null || configMap.isEmpty()) {
                continue;
            }
            List<Map<String, Object>> childrenList = (List<Map<String, Object>>) configMap.get("children");
            if (childrenList != null && !childrenList.isEmpty()) {
                setDefaultCurrentValue(userRelationList, childrenList, userInfo, parseFlag);
                configMap = (Map<String, Object>) itemMap.get("__config__");
            }
            String jnpfKey = (String) configMap.get("jnpfKey");
            String defaultCurrent = String.valueOf(configMap.get("defaultCurrent"));
            if ("true".equals(defaultCurrent)) {
                Map<String, List<UserRelationEntity>> relationMap = userRelationList.stream().collect(Collectors.groupingBy(UserRelationEntity::getObjectType));
                Object data = "";
                switch (jnpfKey) {
                    case JnpfKeyConsts.COMSELECT:
                        data = new ArrayList() {{
                            add(userInfo.getOrganizeId());
                        }};
                        break;
                    case JnpfKeyConsts.DEPSELECT:
                        data = userInfo.getDepartmentId();
                        break;
                    case JnpfKeyConsts.POSSELECT:
                        data = userInfo.getPositionIds().length > 0 ? userInfo.getPositionIds()[0] : "";
                        break;
                    case JnpfKeyConsts.USERSELECT:
                    case JnpfKeyConsts.CUSTOMUSERSELECT:
                        data = JnpfKeyConsts.CUSTOMUSERSELECT.equals(jnpfKey) ? userInfo.getUserId() + "--" + PermissionConst.USER : userInfo.getUserId();
                        break;
                    case JnpfKeyConsts.ROLESELECT:
                        List<UserRelationEntity> roleList = relationMap.get(PermissionConst.ROLE) != null ? relationMap.get(PermissionConst.ROLE) : new ArrayList<>();
                        data = roleList.size() > 0 ? roleList.get(0).getObjectId() : "";
                        break;
                    case JnpfKeyConsts.GROUPSELECT:
                        List<UserRelationEntity> groupList = relationMap.get(PermissionConst.GROUP) != null ? relationMap.get(PermissionConst.GROUP) : new ArrayList<>();
                        data = groupList.size() > 0 ? groupList.get(0).getObjectId() : "";
                        break;
                    default:
                        break;
                }
                List<Object> list = new ArrayList<>();
                list.add(data);
                if ("search".equals(parseFlag)) {
                    String searchMultiple = String.valueOf(itemMap.get("searchMultiple"));
                    if ("true".equals(searchMultiple)) {
                        configMap.put("defaultValue", list);
                    } else {
                        configMap.put("defaultValue", data);
                    }
                } else {
                    String multiple = String.valueOf(itemMap.get("multiple"));
                    if ("true".equals(multiple)) {
                        configMap.put("defaultValue", list);
                    } else {
                        configMap.put("defaultValue", data);
                    }
                }
                itemMap.put("__config__", configMap);
                itemList.set(i, itemMap);
            }
        }
    }

}
