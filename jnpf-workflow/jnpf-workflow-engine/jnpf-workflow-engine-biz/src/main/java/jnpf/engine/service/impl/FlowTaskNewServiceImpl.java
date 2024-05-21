package jnpf.engine.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.google.common.collect.ImmutableList;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.engine.entity.FlowDelegateEntity;
import jnpf.engine.entity.FlowEventLogEntity;
import jnpf.engine.entity.FlowOperatorUserEntity;
import jnpf.engine.entity.FlowRejectDataEntity;
import jnpf.engine.entity.FlowTaskCirculateEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.enums.FlowNodeEnum;
import jnpf.engine.enums.FlowRecordEnum;
import jnpf.engine.enums.FlowRecordListEnum;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.engine.enums.FlowTaskOperatorEnum;
import jnpf.engine.enums.FlowTaskStatusEnum;
import jnpf.engine.model.flowbefore.FlowBeforeInfoVO;
import jnpf.engine.model.flowbefore.FlowSummary;
import jnpf.engine.model.flowbefore.FlowTaskModel;
import jnpf.engine.model.flowbefore.FlowTaskNodeModel;
import jnpf.engine.model.flowbefore.FlowTaskOperatorRecordModel;
import jnpf.engine.model.flowbefore.FlowTemplateAllModel;
import jnpf.engine.model.flowbefore.FlowTemplateModel;
import jnpf.engine.model.flowcandidate.FlowCandidateListModel;
import jnpf.engine.model.flowcandidate.FlowCandidateUserModel;
import jnpf.engine.model.flowcandidate.FlowCandidateVO;
import jnpf.engine.model.flowcandidate.FlowRejectVO;
import jnpf.engine.model.flowdelegate.FlowDelegateModel;
import jnpf.engine.model.flowengine.FlowDataModel;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.FlowNextModel;
import jnpf.engine.model.flowengine.FlowOperator;
import jnpf.engine.model.flowengine.FlowOperatordModel;
import jnpf.engine.model.flowengine.FlowTaskNodeRejectModel;
import jnpf.engine.model.flowengine.FlowTaskOperatorRejectModel;
import jnpf.engine.model.flowengine.FlowTaskRejectModel;
import jnpf.engine.model.flowengine.FlowUpModel;
import jnpf.engine.model.flowengine.FlowUpdateNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.LimitModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.Custom;
import jnpf.engine.model.flowmessage.FlowMsgModel;
import jnpf.engine.model.flowmessage.FlowParameterModel;
import jnpf.engine.model.flowtask.FlowAgreeRuleModel;
import jnpf.engine.model.flowtask.FlowApproveModel;
import jnpf.engine.model.flowtask.FlowCountersignModel;
import jnpf.engine.model.flowtask.FlowErrorModel;
import jnpf.engine.model.flowtask.FlowNodeListModel;
import jnpf.engine.model.flowtask.TaskNodeModel;
import jnpf.engine.model.flowtask.WorkTimeoutJobModel;
import jnpf.engine.model.flowtask.method.TaskHandleIdStatus;
import jnpf.engine.model.flowtask.method.TaskOperatoUser;
import jnpf.engine.model.flowtask.method.TaskOperator;
import jnpf.engine.model.flowtasknode.TaskNodeListModel;
import jnpf.engine.service.FlowCandidatesService;
import jnpf.engine.service.FlowDelegateService;
import jnpf.engine.service.FlowEventLogService;
import jnpf.engine.service.FlowOperatorUserService;
import jnpf.engine.service.FlowRejectDataService;
import jnpf.engine.service.FlowTaskCirculateService;
import jnpf.engine.service.FlowTaskNewService;
import jnpf.engine.service.FlowTaskNodeService;
import jnpf.engine.service.FlowTaskOperatorRecordService;
import jnpf.engine.service.FlowTaskOperatorService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowUserService;
import jnpf.engine.util.FlowContextHolder;
import jnpf.engine.util.FlowJsonUtil;
import jnpf.engine.util.FlowMsgUtil;
import jnpf.engine.util.FlowNature;
import jnpf.engine.util.FlowTaskUtil;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.job.WorkTimeoutJobUtil;
import jnpf.model.flow.FlowFormDataModel;
import jnpf.model.form.FlowFormVo;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年4月27日 上午9:18
 */
@Service
@Slf4j
public class FlowTaskNewServiceImpl implements FlowTaskNewService {

    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private FlowUserService flowUserService;
    @Autowired
    private FlowCandidatesService flowCandidatesService;
    @Autowired
    private FlowTaskNodeService flowTaskNodeService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private FlowTaskOperatorRecordService flowTaskOperatorRecordService;
    @Autowired
    private FlowTaskCirculateService flowTaskCirculateService;
    @Autowired
    private FlowRejectDataService flowRejectDataService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTaskUtil flowTaskUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FlowOperatorUserService flowOperatorUserService;
    @Autowired
    private FlowMsgUtil flowMsgUtil;
    @Autowired
    private FlowEventLogService flowEventLogService;
    @Autowired
    private FlowDelegateService flowDelegateService;
    @Autowired
    private WorkTimeoutJobUtil workTimeoutJobUtil;
    @Autowired
    private UserProvider userProvider;


    @Override
    public FlowTaskEntity saveIsAdmin(FlowModel flowModel) throws WorkFlowException {
        FlowTaskEntity entity = save(flowModel);
        return entity;
    }

    @Override
    @DSTransactional
    public FlowTaskEntity save(FlowModel flowModel) throws WorkFlowException {
        String flowId = flowModel.getFlowId();
        UserInfo userInfo = flowModel.getUserInfo();
        flowModel.setStatus(StringUtil.isNotEmpty(flowModel.getStatus()) ? flowModel.getStatus() : FlowStatusEnum.save.getMessage());
        flowModel.setUserId(StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId());
        //流程引擎
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowId);
        FlowTemplateJsonEntity templateJson = templateAllModel.getTemplateJson();
        FlowTemplateEntity template = templateAllModel.getTemplate();
        FlowTaskEntity infoSubmit = flowTaskService.getInfoSubmit(flowModel.getProcessId(),
                FlowTaskEntity::getStatus, FlowTaskEntity::getCreatorUserId, FlowTaskEntity::getIsAsync,
                FlowTaskEntity::getSystemId, FlowTaskEntity::getFullName, FlowTaskEntity::getParentId,
                FlowTaskEntity::getId, FlowTaskEntity::getRejectId
        );
        boolean isTask = infoSubmit != null;
        String fullName = templateJson.getFullName();
        //流程实例
        String flowTitle = StringUtil.isNotEmpty(flowModel.getFlowTitle()) ? flowModel.getFlowTitle() : userInfo.getUserName() + "的" + fullName;
        FlowTaskEntity flowTaskEntity = new FlowTaskEntity();
        flowTaskEntity.setParentId(flowModel.getParentId());
        flowTaskEntity.setIsAsync(FlowNature.ChildSync);
        flowTaskEntity.setFullName(flowTitle);
        flowTaskEntity.setStatus(FlowTaskStatusEnum.Draft.getCode());
        flowTaskEntity.setSystemId(flowModel.getSystemId());
        FlowTaskEntity taskEntity = isTask ? infoSubmit : flowTaskEntity;
        if (isTask) {
            //判断流程是否处于挂起状态
            flowTaskUtil.isSuspend(taskEntity);
            flowModel.setStatus(FlowStatusEnum.save.getMessage().equals(flowModel.getStatus()) ? FlowStatusEnum.none.getMessage() : flowModel.getStatus());
            flowModel.setUserId(taskEntity.getCreatorUserId());
            flowModel.setIsAsync(FlowNature.ChildAsync.equals(taskEntity.getIsAsync()));
            flowModel.setSystemId(taskEntity.getSystemId());
            flowTitle = taskEntity.getFullName();
        }
        if (!FlowNature.ParentId.equals(taskEntity.getParentId())) {
            flowModel.setParentId(taskEntity.getParentId());
            flowTitle = taskEntity.getFullName();
        }
        ChildNode childNodeAll = JsonUtil.getJsonToBean(templateJson.getFlowTemplateJson(), ChildNode.class);
        Properties properties = childNodeAll.getProperties();
        Map<String, Object> data = flowModel.getFormData() != null ? flowModel.getFormData() : new HashMap<>();
        data.put("@flowFullName", fullName);
        data.put("@flowFullCode", template.getEnCode());
        data.put("@launchUserName", userInfo.getUserName());
        data.put("@launchTime", DateUtil.daFormat(new Date()));
        if (!FlowNature.TitleType.equals(properties.getTitleType())) {
            flowTitle = FlowJsonUtil.field(properties.getTitleContent(), data, "1");
            flowTitle += !FlowNature.ParentId.equals(flowModel.getParentId()) ? "(子流程)" : "";
        }
        flowModel.setFlowTitle(flowTitle);
        flowTaskUtil.task(taskEntity, templateAllModel, flowModel);
        //更新流程任务
        if (isTask) {
            flowTaskService.update(taskEntity);
        } else {
            flowTaskService.create(taskEntity);
        }
        return taskEntity;
    }

    @Override
    public void submit(FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        flowModel.setStatus(FlowStatusEnum.submit.getMessage());
        //流程节点
        List<FlowTaskNodeEntity> taskNodeList = new ArrayList<>();
        List<ChildNodeList> nodeListAll = new ArrayList<>();
        //流程经办
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        FlowTaskEntity flowTask = save(flowModel);
        //流程表单Json
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        boolean isRejectId = StringUtil.isEmpty(flowTask.getRejectId());
        if (isRejectId) {
            //发起用户信息
            flowTaskUtil.flowUser(flowTask);
            flowTask.setStartTime(new Date());
            flowModel.setTaskOperatorId(FlowNature.ParentId);
            String formDataJson = templateAllModel.getTemplateJson().getFlowTemplateJson();
            ChildNode childNodeAll = JsonUtil.getJsonToBean(formDataJson, ChildNode.class);
            //获取流程节点
            List<ConditionList> conditionListAll = new ArrayList<>();
            FlowUpdateNode updateNode = FlowUpdateNode.builder().childNodeAll(childNodeAll).nodeListAll(nodeListAll).taskNodeList(taskNodeList).conditionListAll(conditionListAll).flowTask(flowTask).userInfo(userInfo).isSubmit(true).build();
            flowTaskUtil.updateNodeList(updateNode);
            //保存节点数据
            FlowTaskNodeEntity startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
            FlowTaskOperatorEntity operatorEntity = new FlowTaskOperatorEntity();
            operatorEntity.setId(FlowNature.ParentId);
            operatorEntity.setNodeCode(startNode.getNodeCode());
            FlowNodeListModel nodeListModel = new FlowNodeListModel(taskNodeList, flowModel, true, startNode, 1L);
            flowTaskUtil.nodeListAll(nodeListModel);
        } else {
            FlowRejectDataEntity info = flowRejectDataService.getInfo(flowTask.getRejectId());
            List<FlowTaskNodeEntity> list = flowTaskNodeService.getList(flowTask.getId(),
                    FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson
            );
            List<FlowTaskNodeEntity> rejectTaskNodeList = JsonUtil.getJsonToList(info.getTaskNodeJson(), FlowTaskNodeEntity.class);
            for (FlowTaskNodeEntity taskNodeEntity : rejectTaskNodeList) {
                flowTaskNodeService.update(taskNodeEntity);
                for (FlowTaskNodeEntity model : list) {
                    if (model.getId().equals(taskNodeEntity.getId())) {
                        taskNodeEntity.setNodePropertyJson(model.getNodePropertyJson());
                    }
                }
            }
            TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(flowTask.getId()).state(FlowNodeEnum.Process.getCode()).build();
            List<FlowTaskNodeEntity> rejectTaskNode = flowTaskNodeService.getList(taskNodeListModel,
                    FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getNodePropertyJson
            );
            for (FlowTaskNodeEntity taskNodeEntity : rejectTaskNode) {
                nodeListAll.add(JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class));
                taskNodeList.add(taskNodeEntity);
            }
            FlowTaskEntity jsonToBean = JsonUtil.getJsonToBean(info.getTaskJson(), FlowTaskEntity.class);
            flowTask.setStatus(jsonToBean.getStatus());
            flowTask.setThisStep(jsonToBean.getThisStep());
            flowTask.setThisStepId(jsonToBean.getThisStepId());
            flowTask.setCompletion(jsonToBean.getCompletion());
            //还原审批数据状态
            List<FlowTaskOperatorEntity> operatorEntityList = JsonUtil.getJsonToList(info.getTaskOperatorJson(), FlowTaskOperatorEntity.class);
            for (FlowTaskOperatorEntity flowTaskOperatorEntity : operatorEntityList) {
                flowTaskOperatorService.update(flowTaskOperatorEntity);
            }
            //冻结数据传递
            FlowTaskNodeEntity startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
            flowTaskUtil.dataAssignment(startNode.getId(), rejectTaskNodeList, flowModel);
        }
        FlowTaskNodeEntity startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
        ChildNodeList start = JsonUtil.getJsonToBean(startNode.getNodePropertyJson(), ChildNodeList.class);
        //开始表单数据
        FlowDataModel startDataModel = new FlowDataModel(start, taskNodeList, flowModel, true, true);
        flowTaskUtil.createOrUpdate(startDataModel);
        //审批记录
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(flowTask.getId());
        operator.setTaskNodeId(startNode.getId());
        operator.setNodeCode(startNode.getNodeCode());
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        //审批数据赋值
        if (isRejectId) {
            FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.submit.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).build();
            flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskUtil.createRecord(operatorRecord);
        }
        List<String> nodeList = new ArrayList<>();
        nodeList.addAll(isRejectId ? Arrays.asList(startNode.getNodeNext().split(",")) : Arrays.asList(flowTask.getThisStepId().split(",")));
        //获取下一审批人
        List<ChildNodeList> nextOperatorList = nodeListAll.stream().filter(t -> nodeList.contains(t.getCustom().getNodeId())).collect(Collectors.toList());
        Map<String, List<String>> asyncTaskList = new HashMap<>();
        Map<String, List<String>> nodeTaskIdList = new HashMap<>();
        FlowOperator flowOperator = FlowOperator.builder().operatorListAll(operatorList).flowModel(flowModel).flowTask(flowTask).nodeList(nextOperatorList).taskNodeListAll(taskNodeList).userInfo(userInfo).asyncTaskList(asyncTaskList).nodeTaskIdList(nodeTaskIdList).build();
        flowTaskUtil.nextOperator(flowOperator);
        Map<String, List<FlowTaskOperatorEntity>> operatorMap = operatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId));
        for (ChildNodeList childNodeList : nextOperatorList) {
            Properties properties = childNodeList.getProperties();
            boolean counterSign = FlowNature.ImproperApprover.equals(properties.getCounterSign());
            if (counterSign && StringUtil.isEmpty(flowModel.getFreeApproverUserId())) {
                List<FlowTaskOperatorEntity> listAll = operatorMap.get(childNodeList.getTaskNodeId()) != null ? operatorMap.get(childNodeList.getTaskNodeId()) : new ArrayList<>();
                flowOperatorUserService.updateReject(flowTask.getId(), new HashSet() {{
                    add(childNodeList.getTaskNodeId());
                }});
                flowOperatorUserService.create(JsonUtil.getJsonToList(listAll, FlowOperatorUserEntity.class));
            }
            FlowDataModel flowDataModel = new FlowDataModel(childNodeList, taskNodeList, flowModel, true, true);
            flowTaskUtil.createOrUpdate(flowDataModel);
        }
        //过滤依次审批人
        flowTaskUtil.improperApproverUser(operatorList, taskNodeList, start, null);
        //定时器
        FlowTaskOperatorEntity startOperator = new FlowTaskOperatorEntity();
        startOperator.setTaskId(start.getTaskId());
        startOperator.setTaskNodeId(start.getTaskNodeId());
        if (isRejectId) {
            flowTaskUtil.timer(startOperator, taskNodeList, operatorList);
        }
        flowTaskOperatorService.create(operatorList);
        //更新关联同步子流程id
        for (String nodeId : nodeTaskIdList.keySet()) {
            FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(nodeId)).findFirst().orElse(null);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.getCustom().setTaskId(nodeTaskIdList.get(nodeId));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.updateTaskIdList(entity);
            }
        }
        //更新关联异步子流程id
        for (String nodeId : asyncTaskList.keySet()) {
            FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(nodeId)).findFirst().orElse(null);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.getCustom().setAsyncTaskList(asyncTaskList.get(nodeId));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.updateTaskIdList(entity);
            }
        }
        List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
        //更新流程节点
        if (isRejectId) {
            //修改选择分支没有走过的节点
            List<String> nodeCodeList = nextOperatorList.stream().map(t -> t.getCustom().getNodeId()).collect(Collectors.toList());
            flowTaskUtil.branchTaskNode(nodeCodeList, taskNodeList, operatorList);
            if (StringUtil.isEmpty(flowTask.getThisStepId())) {
                flowTaskUtil.getNextStepId(nextOperatorList, flowTask, flowModel, taskNodeList);
            }
            boolean isEnd = nodeList.contains(FlowNature.NodeEnd);
            if (isEnd) {
                flowTaskUtil.endround(flowTask, nodeListAll.get(0), flowModel);
            }
            //保存节点数据
            if (Objects.equals(FlowNature.ResurgenceThis, start.getProperties().getResurgenceDataRule())) {
                startNode.setDraftData(flowTask.getFlowFormContentJson());
            }
            flowTaskNodeService.update(startNode);
            //获取抄送人
            flowTaskUtil.circulateList(start, taskNodeList, circulateList, flowModel, flowTask);
            flowTaskCirculateService.create(circulateList);
        }
        flowTask.setRejectId(null);
        flowTaskService.update(flowTask);
        //委托消息
        boolean approve = StringUtil.isNotEmpty(flowTask.getDelegateUser());
        if (approve) {
            UserInfo approverUserInfo = JsonUtil.getJsonToBean(userInfo, UserInfo.class);
            FlowDelegateModel delegate = new FlowDelegateModel();
            delegate.setToUserIds(ImmutableList.of(flowTask.getCreatorUserId()));
            UserEntity userEntity = serviceUtil.getUserInfo(flowTask.getDelegateUser());
            approverUserInfo.setUserName(userEntity != null ? userEntity.getRealName() : "");
            delegate.setDelegate(false);
            delegate.setUserInfo(approverUserInfo);
            delegate.setFlowTask(flowTask);
            delegate.setTemplateAllModel(templateAllModel);
            delegate.setApprove(approve);
            flowMsgUtil.delegateMsg(delegate);
        }
        if (isRejectId) {
            //开始事件
            flowMsgUtil.event(1, start, operatorRecord, flowModel);
            //自动审批
            FlowApproveModel approveModel = FlowApproveModel.builder().operatorList(operatorList).taskNodeList(taskNodeList).flowTask(flowTask).flowModel(flowModel).isSubmit(true).build();
            flowTaskUtil.approve(approveModel);
            //发送消息
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setOperatorList(operatorList);
            flowMsgModel.setData(flowModel.getFormData());
            flowMsgModel.setFlowModel(flowModel);
            flowMsgModel.setTaskEntity(flowTask);
            flowMsgModel.setCopy(true);
            flowMsgModel.setFlowTemplateAllModel(templateAllModel);
            boolean isEnd = nodeList.contains(FlowNature.NodeEnd);
            if (isEnd && isRejectId) {
                flowMsgModel.setTaskNodeEntity(startNode);
            }
            flowMsgUtil.message(flowMsgModel);
            //超时
            insTimeOutRedis(flowModel, operatorList, userInfo, flowTask, taskNodeList);
        }
    }

    @Override
    @DSTransactional
    public void submitAll(FlowModel flowModel) throws WorkFlowException {
        try {
            submit(flowModel);
            //表单数据
            Map<String, Map<String, Object>> childAllData = FlowContextHolder.getChildAllData();
            Map<String, List<Map<String, Object>>> childAllOperates = FlowContextHolder.getFormOperates();
            for (String idAll : childAllData.keySet()) {
                String[] idList = idAll.split("_jnpf_");
                Map<String, Object> formData = childAllData.get(idAll);
                List<Map<String, Object>> formOperates = childAllOperates.get(idAll);
                FlowFormDataModel model = FlowFormDataModel.builder().formId(idList[1]).id(idList[0]).map(formData).formOperates(formOperates).build();
                serviceUtil.createOrUpdate(model);
            }
        } finally {
            List<FlowParameterModel> allEvent = FlowContextHolder.getAllEvent();
            if (allEvent.size() > 0) {
                FlowModel cancelModel = new FlowModel();
                cancelModel.setUserInfo(flowModel.getUserInfo());
                flowTaskUtil.taskCancel(ImmutableList.of(flowModel.getProcessId()), cancelModel);
            }
            FlowContextHolder.clearAll();
        }
    }

    @Override
    public void audit(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        //判断是否审批过
        if (!FlowNature.ProcessCompletion.equals(operator.getCompletion())) {
            throw new WorkFlowException("已审核完成");
        }
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        //主流程冻结,子流程结束报错
        if (StringUtil.isNotEmpty(flowTask.getRejectId()) && StringUtil.isEmpty(operator.getReject())) {
            throw new WorkFlowException("冻结不能操作！");
        }
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        boolean before = FlowNature.Before.equals(flowModel.getFreeApproverType());
        //更新表单数据
        boolean isOperator = StringUtil.isNotEmpty(operator.getId());
        boolean isUser = StringUtil.isNotEmpty(flowModel.getFreeApproverUserId());
        boolean isReject = StringUtil.isEmpty(flowTask.getRejectId());
        flowModel.setTaskOperatorId(operator.getId());
        flowModel.setParentId(isUser ? operator.getId() : flowModel.getParentId());
        flowModel.setRollbackId(before ? operator.getId() : operator.getRollbackId());
        flowModel.setRejectUser(StringUtil.isNotEmpty(flowTask.getRejectId()) ? true : false);
        FlowTaskNodeEntity flowTaskNode = flowTaskNodeService.getInfo(operator.getTaskNodeId(),
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getFormId, FlowTaskNodeEntity::getNodeType,
                FlowTaskNodeEntity::getNodePropertyJson, FlowTaskNodeEntity::getSortCode, FlowTaskNodeEntity::getNodeCode,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getCandidates
        );
        //自动审批获取最新表单数据
        Map<String, Object> formData = flowModel.getVoluntarily() ? flowTaskUtil.infoData(flowTaskNode.getFormId(), flowTask.getId()) : flowModel.getFormData();
        if (!FlowNature.NodeSubFlow.equals(flowTaskNode.getNodeType())) {
            FlowContextHolder.addData(flowTaskNode.getFormId(), formData);
            FlowContextHolder.addChildData(flowTask.getId(), flowTaskNode.getFormId(), formData);
            ChildNodeList nodeModel = JsonUtil.getJsonToBean(flowTaskNode.getNodePropertyJson(), ChildNodeList.class);
            List<Map<String, Object>> formOperates = flowTaskUtil.formOperates(nodeModel);
            FlowContextHolder.addFormOperates(flowTask.getId(), flowTaskNode.getFormId(), formOperates);
        }
        //todo
        if (!flowModel.getIsAsync() && isReject) {
            flowTaskUtil.auditTaskNode(templateAllModel, flowTask, flowTaskNode, flowModel);
        }
        //流程所有节点
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(flowTask.getId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getFormId, FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getSortCode,
                FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getNodeName, FlowTaskNodeEntity::getCompletion,
                FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getState, FlowTaskNodeEntity::getCandidates,
                FlowTaskNodeEntity::getNodeUp
        );
        //当前节点
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(m -> m.getId().equals(operator.getTaskNodeId())).findFirst().orElse(null);
        if (taskNode == null) {
            throw new WorkFlowException(MsgCode.COD001.get());
        }
        //加签回流
        if (!isUser) {
            String rollbackId = StringUtil.isNotEmpty(operator.getRollbackId()) ? operator.getRollbackId() : RandomUtil.uuId();
            FlowTaskOperatorEntity operatorInfo = flowTaskOperatorService.getOperatorInfo(rollbackId);
            if (operatorInfo != null) {
                FlowModel rollbackModel = new FlowModel();
                rollbackModel.setUserInfo(flowModel.getUserInfo());
                rollbackModel.setTaskOperatorId(flowModel.getTaskOperatorId());
                rollbackModel.setParentId(flowModel.getTaskOperatorId());
                rollbackModel.setCopyIds(flowModel.getCopyIds());
                rollbackModel.setFileList(flowModel.getFileList());
                rollbackModel.setHandleOpinion(flowModel.getHandleOpinion());
                rollbackModel.setSignImg(flowModel.getSignImg());
                rollbackModel.setRejectUser(flowModel.getRejectUser());
                rollbackModel.setFormData(flowModel.getFormData());
                flowModel = JsonUtil.getJsonToBean(rollbackModel, FlowModel.class);
                flowModel.setFreeApproverUserId(operatorInfo.getHandleId());
                flowModel.setRollbackId(operatorInfo.getRollbackId());
                flowModel.setFreeApproverType(FlowNature.Reflux);
                flowModel.setTaskOperatorId(FlowNature.ParentId.equals(operatorInfo.getParentId()) ? FlowNature.ParentId : flowModel.getTaskOperatorId());
            }
        }
        String userId = StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId();
        //当前节点属性
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        //修改或签、会签经办数据
        TaskHandleIdStatus handleIdStatus = new TaskHandleIdStatus();
        handleIdStatus.setStatus(1);
        handleIdStatus.setNodeModel(nodeModel);
        handleIdStatus.setUserInfo(userInfo);
        handleIdStatus.setTaskNodeList(taskNodeList);
        handleIdStatus.setFlowModel(flowModel);
        flowTaskUtil.handleIdStatus(operator, handleIdStatus);
        //更新流当前程经办状态
        if (isOperator) {
            operator.setState(StringUtil.isNotEmpty(flowModel.getFreeApproverUserId()) ? FlowNodeEnum.FreeApprover.getCode() : operator.getState());
            flowTaskOperatorService.update(operator);
        }
        //更新下一节点
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        //获取下一审批人
        List<FlowTaskNodeEntity> nextNode = taskNodeList.stream().filter(t -> taskNode.getNodeNext().contains(t.getNodeCode())).collect(Collectors.toList());
        //判断转向节点是否错误
        String msg = "";
        List<FlowTaskNodeEntity> flowTaskNodeEntities = JsonUtil.getJsonToList(taskNodeList, FlowTaskNodeEntity.class);
        flowTaskUtil.nodeList(flowTaskNodeEntities);
        List<String> nextNodeCode = nextNode.stream().map(FlowTaskNodeEntity::getNodeCode).collect(Collectors.toList());
        boolean isNotNode = flowTaskNodeEntities.stream().filter(t -> nextNodeCode.contains(t.getNodeCode())).count() == 0;
        if (isNotNode) {
            nextNodeCode.add(taskNode.getNodeNext());
            TaskNodeListModel taskNodeModel = TaskNodeListModel.builder().id(flowTask.getId()).build();
            List<FlowTaskNodeEntity> nodeList = flowTaskNodeService.getList(taskNodeModel, FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodeCode);
            boolean isErrorNode = nodeList.stream().filter(t -> nextNodeCode.contains(t.getNodeCode())).count() == 0;
            msg = isErrorNode ? "转向节点不存在或配置错误" : "转向失败，转向节点未审批";
            nextNode.add(taskNode);
        }
        List<ChildNodeList> nextOperatorList = new ArrayList<>();
        FlowNextModel flowNextModel = FlowNextModel.builder().nodeListAll(taskNodeList).nextNodeEntity(nextNode).taskNode(taskNode).flowModel(flowModel).isCountersign(false).build();
        List<FlowTaskNodeEntity> result = flowTaskUtil.isNextAll(flowNextModel);
        flowTaskUtil.candidateList(flowModel, taskNodeList, operator);
        if (result.size() > 0) {
            if (StringUtil.isNotEmpty(msg)) {
                throw new WorkFlowException(msg);
            }
            boolean freeApproverUserId = StringUtil.isEmpty(flowModel.getFreeApproverUserId());
            if (freeApproverUserId && !isReject) {
                FlowRejectDataEntity info = flowRejectDataService.getInfo(flowTask.getRejectId());
                FlowTaskEntity jsonToBean = JsonUtil.getJsonToBean(info.getTaskJson(), FlowTaskEntity.class);
                jsonToBean.setRejectId(null);
                flowTask.setRejectId(null);
                flowModel.setRejectUser(false);
                flowTaskService.update(jsonToBean);
                Set<String> rejectNodeList = new HashSet<>();
                List<String> rejectList = new ArrayList() {{
                    add(operator.getTaskNodeId());
                }};
                List<String> thisStepId = Arrays.asList(jsonToBean.getThisStepId().split(","));
                List<FlowTaskNodeEntity> rejectTaskNodeList = JsonUtil.getJsonToList(info.getTaskNodeJson(), FlowTaskNodeEntity.class);
                flowTaskUtil.upAll(rejectNodeList, rejectList, rejectTaskNodeList);
                for (FlowTaskNodeEntity taskNodeEntity : rejectTaskNodeList) {
                    FlowTaskNodeEntity node = taskNodeList.stream().filter(t -> t.getId().equals(taskNodeEntity.getId())).findFirst().orElse(null);
                    taskNodeEntity.setDraftData(node != null ? JsonUtil.getObjectToString(flowModel.getFormData()) : taskNodeEntity.getDraftData());
                    flowTaskNodeService.update(taskNodeEntity);
                    taskNodeEntity.setNodePropertyJson(node != null ? node.getNodePropertyJson() : "{}");
                }
                //获取
                List<FlowTaskNodeEntity> rejectNodeAll = rejectTaskNodeList.stream().filter(t -> rejectNodeList.contains(t.getId())).collect(Collectors.toList());
                for (FlowTaskNodeEntity taskNodeEntity : rejectNodeAll) {
                    if (thisStepId.contains(taskNodeEntity.getNodeCode())) {
                        nextOperatorList.add(JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class));
                    }
                }
                //还原审批数据状态
                List<FlowTaskOperatorEntity> operatorEntityList = JsonUtil.getJsonToList(info.getTaskOperatorJson(), FlowTaskOperatorEntity.class);
                for (FlowTaskOperatorEntity flowTaskOperatorEntity : operatorEntityList) {
                    flowTaskOperatorService.update(flowTaskOperatorEntity);
                }
                //冻结数据传递
                flowTaskUtil.dataAssignment(operator.getTaskNodeId(), rejectTaskNodeList, flowModel);
            } else {
                //分支的下个节点
                List<FlowTaskNodeEntity> nodeCandidateList = result.stream().filter(t -> StringUtil.isNotEmpty(t.getCandidates())).collect(Collectors.toList());
                for (FlowTaskNodeEntity entity : nodeCandidateList) {
                    ChildNodeList node = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                    nextOperatorList.add(node);
                }
                if (nextOperatorList.size() == 0) {
                    for (FlowTaskNodeEntity entity : result) {
                        ChildNodeList node = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                        nextOperatorList.add(node);
                    }
                }
            }
        }
        boolean isStart = nextNode.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).count() > 0;
        boolean isCompletion = nextNode.stream().filter(t -> FlowNature.AuditCompletion.equals(t.getCompletion())).count() > 0;
        boolean isSwerve = flowNextModel.getIsCountersign() && isCompletion;
        //同意记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(isSwerve ? FlowRecordEnum.swerve.getCode() : FlowRecordEnum.audit.getCode()).flowModel(flowModel).userId(userId).operator(operator).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        //子流程不新增流转记录
        if (!flowModel.getIsAsync() && !before) {
            flowTaskUtil.createRecord(operatorRecord);
        }
        if (isReject) {
            //更新流程节点
            if (isSwerve) {
                FlowTaskNodeEntity serverTaskNode = new FlowTaskNodeEntity();
                serverTaskNode.setNodeUp(taskNode.getNodeNext());
                serverTaskNode.setTaskId(serverTaskNode.getTaskId());
                FlowUpModel flowUpModel = FlowUpModel.builder().flowTask(flowTask).taskNode(serverTaskNode).taskNodeList(taskNodeList)
                        .isReject(isSwerve).rejectType(true).flowModel(flowModel).isAudit(true).build();
                flowTaskUtil.flowUp(flowUpModel);
            } else {
                flowTaskUtil.getNextStepId(nextOperatorList, flowTask, flowModel, taskNodeList);
                flowTask.setCompletion(FlowNature.NodeEnd.equals(flowTask.getThisStepId()) ? FlowNature.Progress : flowTask.getCompletion());
            }
            if (StringUtil.isNotEmpty(flowTask.getThisStepId())) {
                FlowTaskEntity updateFlowTask = new FlowTaskEntity();
                updateFlowTask.setTaskNodeId(null);
                updateFlowTask.setCompletion(flowTask.getCompletion());
                updateFlowTask.setThisStepId(flowTask.getThisStepId());
                updateFlowTask.setStatus(flowTask.getStatus());
                updateFlowTask.setThisStep(flowTask.getThisStep());
                updateFlowTask.setId(flowTask.getId());
                flowTaskService.update(updateFlowTask);
            }
        }
        //下个节点
        Map<String, List<String>> asyncTaskList = new HashMap<>();
        Map<String, List<String>> nodeTaskIdList = new HashMap<>();
        FlowOperator flowOperator = FlowOperator.builder().operatorListAll(operatorList).flowModel(flowModel).flowTask(flowTask).nodeList(nextOperatorList).taskNodeListAll(taskNodeList).userInfo(userInfo).asyncTaskList(asyncTaskList).nodeTaskIdList(nodeTaskIdList).build();
        if (!isStart) {
            flowTaskUtil.nextOperator(flowOperator);
            Map<String, List<FlowTaskOperatorEntity>> operatorMap = operatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId));
            for (ChildNodeList childNodeList : nextOperatorList) {
                Properties properties = childNodeList.getProperties();
                boolean counterSign = FlowNature.ImproperApprover.equals(properties.getCounterSign());
                if (counterSign && StringUtil.isEmpty(flowModel.getFreeApproverUserId())) {
                    List<FlowTaskOperatorEntity> listAll = operatorMap.get(childNodeList.getTaskNodeId()) != null ? operatorMap.get(childNodeList.getTaskNodeId()) : new ArrayList<>();
                    flowOperatorUserService.create(JsonUtil.getJsonToList(listAll, FlowOperatorUserEntity.class));
                }
                FlowDataModel flowDataModel = new FlowDataModel(childNodeList, taskNodeList, flowModel, true, true);
                flowTaskUtil.createOrUpdate(flowDataModel);
            }
            //过滤依次审批人
            if (StringUtil.isEmpty(flowModel.getFreeApproverUserId())) {
                flowTaskUtil.improperApproverUser(operatorList, taskNodeList, nodeModel, operator);
            }
            flowTaskUtil.timer(operator, taskNodeList, operatorList);
            flowTaskOperatorService.create(operatorList);
            //修改选择分支没有走过的节点
            if (flowModel.getBranchList().size() > 0) {
                List<String> nodeCodeList = result.stream().map(FlowTaskNodeEntity::getNodeCode).collect(Collectors.toList());
                flowTaskUtil.branchTaskNode(nodeCodeList, taskNodeList, operatorList);
            }
        }
        //更新关联同步子流程id
        for (String nodeId : nodeTaskIdList.keySet()) {
            FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(nodeId)).findFirst().orElse(null);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.getCustom().setTaskId(nodeTaskIdList.get(nodeId));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.updateTaskIdList(entity);
            }
        }
        //更新关联异步子流程id
        for (String nodeId : asyncTaskList.keySet()) {
            FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(nodeId)).findFirst().orElse(null);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.getCustom().setAsyncTaskList(asyncTaskList.get(nodeId));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.updateTaskIdList(entity);
            }
        }
        //获取抄送人
        List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
        if (isReject) {
            flowTaskUtil.circulateList(nodeModel, taskNodeList, circulateList, flowModel, flowTask);
            flowTaskCirculateService.create(circulateList);
        }
        //更新节点接收时间
        flowTaskUtil.taskCreatTime(operatorList);
        //委托消息
        boolean approve = StringUtil.isNotEmpty(flowTask.getDelegateUser());
        if (approve) {
            FlowDelegateModel delegate = new FlowDelegateModel();
            delegate.setToUserIds(ImmutableList.of(operator.getHandleId()));
            delegate.setDelegate(false);
            delegate.setType(FlowNature.ApproveMsg);
            delegate.setUserInfo(userInfo);
            delegate.setFlowTask(flowTask);
            delegate.setTemplateAllModel(templateAllModel);
            delegate.setApprove(approve);
            flowMsgUtil.delegateMsg(delegate);
        }
        if (isReject) {
            //节点事件
            flowMsgUtil.event(4, nodeModel, operatorRecord, flowModel);
            //发送消息
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setApprove(FlowNature.AuditCompletion.equals(taskNode.getCompletion()));
            flowMsgModel.setCopy(true);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setOperatorList(operatorList);
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setData(flowModel.getFormData());
            flowMsgModel.setTaskNodeEntity(taskNode);
            flowMsgModel.setTaskEntity(flowTask);
            flowMsgModel.setFlowTemplateAllModel(templateAllModel);
            FlowTaskOperatorRecordEntity taskOperatorRecord = new FlowTaskOperatorRecordEntity();
            taskOperatorRecord.setHandleId(userInfo.getUserId());
            flowMsgModel.setFlowModel(flowModel);
            flowMsgUtil.message(flowMsgModel);
            //超时
            insTimeOutRedis(flowModel, operatorList, userInfo, flowTask, taskNodeList);
            //自动审批
            if (!isSwerve) {
                FlowApproveModel approveModel = FlowApproveModel.builder().operatorList(operatorList).taskNodeList(taskNodeList).flowTask(flowTask).flowModel(flowModel).build();
                flowTaskUtil.approve(approveModel);
                //查询代办用户是否通过
                flowTaskUtil.approverPass(flowTask, taskNodeList, flowModel, operator);
            }
        }
    }

    @Override
    public void auditAll(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException {
        try {
            audit(flowTask, operator, flowModel);
            //表单数据
            Map<String, Map<String, Object>> childAllData = FlowContextHolder.getChildAllData();
            Map<String, List<Map<String, Object>>> childAllOperates = FlowContextHolder.getFormOperates();
            for (String idAll : childAllData.keySet()) {
                String[] idList = idAll.split("_jnpf_");
                Map<String, Object> formData = childAllData.get(idAll);
                List<Map<String, Object>> formOperates = childAllOperates.get(idAll);
                FlowFormDataModel model = FlowFormDataModel.builder().formId(idList[1]).id(idList[0]).map(formData).formOperates(formOperates).build();
                serviceUtil.createOrUpdate(model);
            }
        } finally {
            List<FlowParameterModel> allEvent = FlowContextHolder.getAllEvent();
            if (allEvent.size() > 0) {
                FlowModel cancelModel = new FlowModel();
                cancelModel.setUserInfo(flowModel.getUserInfo());
                flowTaskUtil.taskCancel(Collections.singletonList(flowModel.getId()), cancelModel);
            }
            FlowContextHolder.clearAll();
        }
    }

    @Override
    @DSTransactional
    public void audit(FlowModel flowModel) throws WorkFlowException {
        String id = flowModel.getId();
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        UserInfo userInfo = userProvider.get();
        flowModel.setUserInfo(userInfo);
        FlowTaskEntity flowTask = flowTaskService.getInfo(operator.getTaskId(),
                FlowTaskEntity::getStatus, FlowTaskEntity::getTemplateId, FlowTaskEntity::getFlowId,
                FlowTaskEntity::getId, FlowTaskEntity::getFullName, FlowTaskEntity::getCreatorUserId,
                FlowTaskEntity::getParentId, FlowTaskEntity::getThisStepId, FlowTaskEntity::getThisStep,
                FlowTaskEntity::getRejectId, FlowTaskEntity::getCompletion
        );
        permissions(operator.getHandleId(), flowTask, operator, "", flowModel);
        if (!FlowNature.ProcessCompletion.equals(operator.getCompletion())) {
            throw new WorkFlowException("已审核完成");
        }
        if (FlowNature.ProcessCompletion.equals(operator.getCompletion())) {
            auditAll(flowTask, operator, flowModel);
        }
    }

    @Override
    @DSTransactional
    public void reject(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        String userId = StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId();
        List<String> rejectThisStepId = Arrays.asList(flowTask.getThisStepId().split(","));
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        //流程所有节点
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(flowTask.getId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodePropertyJson,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getNodeName,
                FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getFormId, FlowTaskNodeEntity::getState,
                FlowTaskNodeEntity::getCompletion
        );
        List<FlowTaskOperatorEntity> operatorEntityList = flowTaskOperatorService.getList(flowTask.getId()).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState()) && !rejectThisStepId.contains(t.getNodeCode())).collect(Collectors.toList());
        //保存当前节点和任务
        String taskJson = JsonUtil.getObjectToString(JsonUtil.getJsonToBean(flowTask, FlowTaskRejectModel.class));
        String taskNodeJson = JsonUtil.getObjectToString(JsonUtil.getJsonToList(taskNodeList, FlowTaskNodeRejectModel.class));
        //当前节点
        Optional<FlowTaskNodeEntity> first = taskNodeList.stream().filter(m -> m.getId().equals(operator.getTaskNodeId())).findFirst();
        if (!first.isPresent()) {
            throw new WorkFlowException(MsgCode.COD001.get());
        }
        FlowTaskNodeEntity taskNode = first.get();
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        flowTaskUtil.candidateList(flowModel, taskNodeList, operator);
        //当前节点属性
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        boolean rejectType = FlowNature.PresentType.equals(flowModel.getRejectType());
        taskNode.setNodeUp(flowModel.getRejectStep());
        //驳回记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.reject.getCode()).flowModel(flowModel).userId(userId).operator(operator).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskUtil.createRecord(operatorRecord);
        //修改或签、会签经办数据
        TaskHandleIdStatus handleIdStatus = new TaskHandleIdStatus();
        handleIdStatus.setStatus(0);
        handleIdStatus.setNodeModel(nodeModel);
        handleIdStatus.setUserInfo(userInfo);
        handleIdStatus.setTaskNodeList(taskNodeList);
        flowTaskUtil.handleIdStatus(operator, handleIdStatus);
        //更新流当前程经办状态
        flowTaskOperatorService.update(operator);
        //判断是否是会签
        List<FlowTaskOperatorEntity> rejectOperatorList = flowTaskOperatorService.getList(taskNode.getTaskId()).stream().filter(t -> t.getTaskNodeId().equals(taskNode.getId()) && FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        FlowCountersignModel countersign = new FlowCountersignModel();
        countersign.setTaskNode(taskNode);
        countersign.setOperatorList(rejectOperatorList);
        boolean isReject = flowTaskUtil.isRejectCountersign(countersign);
        //更新驳回节点
        //todo 123456789
        FlowUpModel flowUpModel = FlowUpModel.builder().flowTask(flowTask).taskNode(taskNode).taskNodeList(taskNodeList)
                .isReject(isReject).rejectType(!rejectType).flowModel(flowModel).isAudit(false).build();
        List<FlowTaskNodeEntity> upAll = flowTaskUtil.flowUp(flowUpModel);
        boolean isStart = upAll.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).count() > 0;
        List<ChildNodeList> nextOperatorList = new ArrayList<>();
        for (FlowTaskNodeEntity entity : upAll) {
            ChildNodeList node = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
            nextOperatorList.add(node);
        }
        //驳回节点
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        if (!isStart) {
            //赋值数据
            flowModel.setProcessId(flowTask.getId());
            flowModel.setId(flowTask.getId());
            Map<String, Object> data = serviceUtil.infoData(taskNode.getFormId(), taskNode.getTaskId());
            flowModel.setFormData(data);
            FlowOperator flowOperator = FlowOperator.builder().operatorListAll(operatorList).flowModel(flowModel).flowTask(flowTask).nodeList(nextOperatorList).taskNodeListAll(taskNodeList).reject(true).asyncTaskList(new HashMap<>()).build();
            flowTaskUtil.nextOperator(flowOperator);
        }
        //保存冻结的数据
        if (rejectType && isReject) {
            String operatorJson = JsonUtil.getObjectToString(JsonUtil.getJsonToList(operatorEntityList, FlowTaskOperatorRejectModel.class));
            FlowRejectDataEntity rejectEntity = new FlowRejectDataEntity();
            rejectEntity.setTaskNodeJson(taskNodeJson);
            rejectEntity.setTaskOperatorJson(operatorJson);
            rejectEntity.setTaskJson(taskJson);
            rejectEntity.setId(flowTask.getId());
            flowTask.setRejectId(flowTask.getId());
            flowRejectDataService.createOrUpdate(rejectEntity);
        }
        //更新流程节点
        flowTask.setTaskNodeId(null);
        flowTaskService.update(flowTask);
        //显示当前的驳回记录
        flowTaskOperatorRecordService.update(operatorRecord.getId(), operatorRecord);
        for (FlowTaskOperatorEntity operatorEntity : operatorList) {
            operatorEntity.setReject(rejectType ? "1" : "");
        }
        //创建审批人
        Map<String, List<FlowTaskOperatorEntity>> operatorMap = operatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId));
        for (ChildNodeList childNodeList : nextOperatorList) {
            Properties properties = childNodeList.getProperties();
            boolean counterSign = FlowNature.ImproperApprover.equals(properties.getCounterSign());
            if (counterSign && StringUtil.isEmpty(flowModel.getFreeApproverUserId())) {
                List<FlowTaskOperatorEntity> listAll = operatorMap.get(childNodeList.getTaskNodeId()) != null ? operatorMap.get(childNodeList.getTaskNodeId()) : new ArrayList<>();
                flowOperatorUserService.create(JsonUtil.getJsonToList(listAll, FlowOperatorUserEntity.class));
            }
        }
        //过滤依次审批人
        flowTaskUtil.improperApproverUser(operatorList, taskNodeList, nodeModel, operator);
        flowTaskOperatorService.create(operatorList);
        //更新节点接收时间
        flowTaskUtil.taskCreatTime(operatorList);
        //获取抄送人
        List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
        flowTaskUtil.circulateList(nodeModel, taskNodeList, circulateList, flowModel, flowTask);
        flowTaskCirculateService.create(circulateList);
        //节点事件
        flowMsgUtil.event(5, nodeModel, operatorRecord, flowModel);
        List<FlowParameterModel> parameterModels = FlowContextHolder.getAllEvent();
        if (parameterModels.size() == 0) {
            //发送消息
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setOperatorList(operatorList);
            flowMsgModel.setReject(true);
            flowMsgModel.setCopy(true);
            flowMsgModel.setWait(isStart ? false : true);
            flowMsgModel.setStart(isStart);
            flowMsgModel.setData(JsonUtil.stringToMap(flowTask.getFlowFormContentJson()));
            flowMsgModel.setTaskNodeEntity(taskNode);
            flowMsgModel.setTaskEntity(flowTask);
            flowMsgModel.setFlowTemplateAllModel(templateAllModel);
            FlowTaskOperatorRecordEntity taskOperatorRecord = new FlowTaskOperatorRecordEntity();
            taskOperatorRecord.setHandleId(userInfo.getUserId());
            flowMsgModel.setFlowModel(flowModel);
            flowMsgUtil.message(flowMsgModel);
            //超时
            insTimeOutRedis(flowModel, operatorList, userInfo, flowTask, taskNodeList);
            //自动审批
            FlowApproveModel approveModel = FlowApproveModel.builder().operatorList(operatorList).taskNodeList(taskNodeList).flowTask(flowTask).flowModel(flowModel).build();
            flowTaskUtil.approve(approveModel);
        }
    }

    @Override
    public void rejectAll(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException {
        try {
            if (StringUtil.isNotEmpty(flowTask.getRejectId())) {
                throw new WorkFlowException("退回至您的审批，不能再发起退回");
            }
            reject(flowTask, operator, flowModel);
        } finally {
            List<FlowParameterModel> allEvent = FlowContextHolder.getAllEvent();
            if (allEvent.size() > 0) {
                FlowModel cancelModel = new FlowModel();
                cancelModel.setUserInfo(flowModel.getUserInfo());
                flowTaskUtil.taskCancel(Collections.singletonList(flowTask.getId()), cancelModel);
            }
            FlowContextHolder.clearAll();
        }
    }

    @Override
    @DSTransactional
    public void reject(FlowModel flowModel) throws WorkFlowException {
        String id = flowModel.getId();
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        FlowTaskEntity flowTask = flowTaskService.getInfo(operator.getTaskId(),
                FlowTaskEntity::getId, FlowTaskEntity::getStatus, FlowTaskEntity::getFlowId,
                FlowTaskEntity::getThisStep, FlowTaskEntity::getThisStepId,
                FlowTaskEntity::getCreatorUserId, FlowTaskEntity::getFullName,
                FlowTaskEntity::getCompletion
        );
        UserInfo userInfo = userProvider.get();
        flowModel.setUserInfo(userInfo);
        permissions(operator.getHandleId(), flowTask, operator, "", flowModel);
        if (!FlowNature.ProcessCompletion.equals(operator.getCompletion())) {
            throw new WorkFlowException("已审核完成");
        }
        if (FlowNature.ProcessCompletion.equals(operator.getCompletion())) {
            rejectAll(flowTask, operator, flowModel);
        }
    }

    @Override
    @DSTransactional
    public void recall(String id, FlowTaskOperatorRecordEntity operatorRecord, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        //撤回经办
        FlowTaskOperatorEntity operatorEntity = flowTaskOperatorService.getInfo(operatorRecord.getTaskOperatorId());
        if (FlowNodeEnum.Futility.getCode().equals(operatorEntity.getState())) {
            throw new WorkFlowException("流程已处理，无法撤回");
        }
        FlowTaskOperatorEntity rollbackOperator = flowTaskOperatorService.getOperatorInfo(StringUtil.isNotEmpty(operatorEntity.getRollbackId()) ? operatorEntity.getRollbackId() : RandomUtil.uuId());
        boolean isParentId = (FlowNature.ParentId.equals(operatorEntity.getParentId()) || (rollbackOperator != null && FlowNature.ParentId.equals(rollbackOperator.getParentId())));
        //撤回任务
        FlowTaskEntity flowTask = flowTaskService.getInfo(operatorRecord.getTaskId(),
                FlowTaskEntity::getId, FlowTaskEntity::getRejectId, FlowTaskEntity::getStatus,
                FlowTaskEntity::getThisStepId
        );
        if (StringUtil.isNotEmpty(flowTask.getRejectId())) {
            throw new WorkFlowException("退回至您的审批,不能再退回审批.");
        }
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        //所有节点
        TaskNodeListModel nodeListModel = TaskNodeListModel.builder().id(operatorRecord.getTaskId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> list = flowTaskNodeService.getList(nodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getCandidates, FlowTaskNodeEntity::getNodeCode,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getNodeName, FlowTaskNodeEntity::getNodePropertyJson,
                FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getCompletion, FlowTaskNodeEntity::getFormId,
                FlowTaskNodeEntity::getTaskId
        );
        //撤回节点
        FlowTaskNodeEntity recallTaskNode = list.stream().filter(t -> t.getId().equals(operatorRecord.getTaskNodeId())).findFirst().orElse(null);
        //所有经办
        List<FlowTaskOperatorEntity> flowTaskOperatorEntityList = flowTaskOperatorService.getList(operatorRecord.getTaskId()).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        //撤回节点属性
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(recallTaskNode.getNodePropertyJson(), ChildNodeList.class);
        //拒绝不撤回
        if (FlowNature.ProcessCompletion.equals(operatorEntity.getHandleStatus())) {
            throw new WorkFlowException("流程已处理，无法撤回");
        }
        //任务待审状态才能撤回
        if (!FlowTaskStatusEnum.Handle.getCode().equals(flowTask.getStatus())) {
            throw new WorkFlowException("流程已处理，无法撤回");
        }
        //撤回节点下一节点已操作
        List<FlowTaskOperatorEntity> recallNextOperatorList = flowTaskOperatorEntityList.stream().filter(x -> recallTaskNode.getNodeNext().contains(x.getNodeCode())).collect(Collectors.toList());
        boolean isRecall = recallNextOperatorList.stream().filter(t -> FlowNature.AuditCompletion.equals(t.getCompletion()) && FlowNodeEnum.Process.getCode().equals(t.getState())).count() > 0;
        if (isRecall) {
            throw new WorkFlowException("流程已处理，无法撤回");
        }
        List<String> childIdList = new ArrayList<>();
        for (FlowTaskNodeEntity taskNodeEntity : list) {
            ChildNodeList childNodeList = JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class);
            childIdList.addAll(childNodeList.getCustom().getAsyncTaskList());
            childIdList.addAll(childNodeList.getCustom().getTaskId());
        }
        List<FlowTaskEntity> childList = flowTaskService.getOrderStaList(childIdList, FlowTaskEntity::getId, FlowTaskEntity::getStatus).stream().filter(t -> !FlowTaskStatusEnum.Cancel.getCode().equals(t.getStatus())).collect(Collectors.toList());
        boolean isNext = childList.size() > 0;
        if (isNext) {
            throw new WorkFlowException("当前流程包含子流程，无法撤回");
        }
        //加签人
        Set<FlowTaskOperatorEntity> operatorList = new HashSet<>();
        flowTaskUtil.getOperator(operatorEntity.getId(), operatorList);
        operatorEntity.setHandleStatus(null);
        operatorEntity.setHandleTime(null);
        operatorEntity.setCreatorTime(new Date());
        operatorEntity.setCompletion(FlowNature.ProcessCompletion);
        operatorEntity.setState(FlowNodeEnum.Process.getCode());
        operatorList.add(operatorEntity);
        List<String> delOperatorRecordIds = new ArrayList<>();
        for (FlowTaskOperatorEntity item : operatorList) {
            FlowTaskOperatorRecordEntity record = flowTaskOperatorRecordService.getInfo(item.getTaskId(), item.getTaskNodeId(), item.getId());
            if (record != null) {
                delOperatorRecordIds.add(record.getId());
            }
        }
        Set<String> rejectNodeList = new HashSet<>();
        List<String> rejectList = new ArrayList() {{
            add(recallTaskNode.getId());
        }};
        flowTaskUtil.upAll(rejectNodeList, rejectList, list);
        //撤回节点是否完成
        if (FlowNature.AuditCompletion.equals(recallTaskNode.getCompletion())) {
            //撤回节点下一节点经办删除
            List<String> idAll = recallNextOperatorList.stream().map(FlowTaskOperatorEntity::getId).collect(Collectors.toList());
            flowTaskOperatorService.updateTaskOperatorState(idAll);
            List<FlowTaskOperatorEntity> hanleOperatorList = flowTaskOperatorEntityList.stream().filter(x -> x.getTaskNodeId().equals(operatorRecord.getTaskNodeId()) && Objects.isNull(x.getHandleStatus()) && Objects.isNull(x.getHandleTime()) && Objects.isNull(x.getParentId())).collect(Collectors.toList());
            for (FlowTaskOperatorEntity taskOperator : hanleOperatorList) {
                taskOperator.setCompletion(FlowNature.ProcessCompletion);
            }
            operatorList.addAll(hanleOperatorList);
            flowTaskNodeService.updateCompletion(new ArrayList<>(rejectNodeList), FlowNature.ProcessCompletion);
            //更新任务流程
            List<String> stepIdList = new ArrayList<>(Arrays.asList(flowTask.getThisStepId().split(",")));
            List<String> stepNameList = new ArrayList<>();
            List<Integer> progressList = new ArrayList<>();
            List<String> rejectNodeCode = list.stream().filter(t -> rejectNodeList.contains(t.getId())).map(FlowTaskNodeEntity::getNodeCode).collect(Collectors.toList());
            stepIdList.removeAll(rejectNodeCode);
            stepIdList.add(recallTaskNode.getNodeCode());
            List<FlowTaskNodeEntity> recallNodeList = list.stream().filter(x -> stepIdList.contains(x.getNodeCode())).collect(Collectors.toList());
            for (FlowTaskNodeEntity taskNodeEntity : recallNodeList) {
                ChildNodeList childNode = JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class);
                Properties properties = childNode.getProperties();
                Integer progress = properties.getProgress();
                if (ObjectUtil.isNotEmpty(progress)) {
                    progressList.add(progress);
                }
                stepIdList.add(taskNodeEntity.getNodeCode());
                stepNameList.add(taskNodeEntity.getNodeName());
            }
            if (progressList.size() == 0) {
                progressList.add(FlowNature.ProcessCompletion);
            }
            //更新当前节点
            flowTask.setCompletion(progressList.get(0));
            flowTask.setThisStepId(String.join(",", stepIdList));
            flowTask.setThisStep(String.join(",", stepNameList));
            flowTask.setStatus(FlowTaskStatusEnum.Handle.getCode());
            flowTaskService.update(flowTask);
        }
        for (FlowTaskOperatorEntity taskOperator : operatorList) {
            flowTaskOperatorService.update(taskOperator);
        }
        if (isParentId) {
            FlowTaskNodeEntity node = list.stream().filter(t -> t.getId().equals(operatorEntity.getTaskNodeId())).findFirst().orElse(null);
            List<String> nextNode = Arrays.asList(node.getNodeNext().split(","));
            List<FlowTaskNodeEntity> taskNodeList = list.stream().filter(t -> nextNode.contains(t.getNodeCode())).collect(Collectors.toList());
            int num = 0;
            for (FlowTaskNodeEntity taskNode : taskNodeList) {
                int candidateList = StringUtil.isNotEmpty(taskNode.getCandidates()) ? 1 : 0;
                num += candidateList > 0 ? 1 : 0;
            }
            if (num > 0) {
                //删除异常人
                List<String> candidateList = list.stream().map(FlowTaskNodeEntity::getId).collect(Collectors.toList());
                flowCandidatesService.deleteTaskNodeId(candidateList, FlowNature.Candidates);
            }
            rejectNodeList.removeAll(rejectList);
            flowTaskNodeService.updateTaskNodeCandidates(new ArrayList<>(rejectNodeList), "");
            //删除异常人
            flowCandidatesService.deleteTaskNodeId(new ArrayList<>(rejectNodeList), FlowNature.CandidatesError);
            //删除依次审批人
            FlowOperatorUserEntity nextOperatorUser = flowOperatorUserService.getList(flowTask.getId()).stream().filter(t -> operatorEntity.getSortCode().equals(t.getSortCode() - 1)).findFirst().orElse(null);
            if (nextOperatorUser != null) {
                boolean count = flowTaskOperatorEntityList.stream().filter(t -> t.getId().equals(nextOperatorUser.getId()) && FlowNature.AuditCompletion.equals(t.getCompletion())).count() > 0;
                if (count) {
                    throw new WorkFlowException("流程已处理，无法撤回");
                } else {
                    flowTaskOperatorService.deleteList(Collections.singletonList(nextOperatorUser.getId()));
                    flowOperatorUserService.updateReject(flowTask.getId(), rejectNodeList);
                }
            }
        }
        List<String> taskOperatorId = operatorList.stream().map(FlowTaskOperatorEntity::getId).collect(Collectors.toList());
        flowCandidatesService.delete(taskOperatorId);
        //删除经办记录
        delOperatorRecordIds.add(operatorRecord.getId());
        flowTaskOperatorRecordService.updateStatus(delOperatorRecordIds);
        //撤回记录
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setNodeCode(operatorRecord.getNodeCode());
        operator.setNodeName(operatorRecord.getNodeName());
        operator.setTaskId(operatorRecord.getTaskId());
        operator.setTaskNodeId(operatorRecord.getTaskNodeId());
        operator.setId(operatorRecord.getTaskOperatorId());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.revoke.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        Map<String, Object> objectMap = serviceUtil.infoData(recallTaskNode.getFormId(), recallTaskNode.getTaskId());
        flowModel.setFormData(objectMap);
        //节点事件
        flowMsgUtil.event(6, nodeModel, operatorRecord, flowModel);
        List<FlowParameterModel> allEvent = FlowContextHolder.getAllEvent();
        if (allEvent.size() > 0) {
            FlowModel cancelModel = new FlowModel();
            cancelModel.setUserInfo(flowModel.getUserInfo());
            flowTaskUtil.taskCancel(Collections.singletonList(flowTask.getId()), cancelModel);
        } else {
            //超时
            insTimeOutRedis(flowModel, operatorList, userInfo, flowTask, list);
        }
    }

    @Override
    @DSTransactional
    public void revoke(FlowTaskEntity flowTask, FlowModel flowModel, boolean isParentId) throws WorkFlowException {
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        UserInfo userInfo = flowModel.getUserInfo();
        TaskNodeListModel nodeListModel = TaskNodeListModel.builder().id(flowTask.getId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(nodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson, FlowTaskNodeEntity::getFormId,
                FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getNodeCode
        );
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        FlowTemplateJsonEntity templateJson = templateAllModel.getTemplateJson();
        ChildNodeList childNode = JsonUtil.getJsonToBean(templateJson.getFlowTemplateJson(), ChildNodeList.class);
        childNode.setTaskId(flowTask.getId());
        FlowTaskNodeEntity startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().orElse(null);
        if (startNode != null) {
            childNode = JsonUtil.getJsonToBean(startNode.getNodePropertyJson(), ChildNodeList.class);
        }
        Properties properties = childNode.getProperties();
        boolean revokeRule = FlowNature.RevokeTerminate.equals(properties.getRevokeRule());
        Map<String, Object> objectMap = serviceUtil.infoData(properties.getFormId(), flowTask.getId());
        flowModel.setFormData(objectMap);
        //删除节点
        flowTaskNodeService.update(flowTask.getId());
        //删除经办
        flowTaskOperatorService.update(flowTask.getId());
        //删除候选人
        flowCandidatesService.deleteByTaskId(flowTask.getId());
        //删除发起用户信息
        flowUserService.deleteByTaskId(flowTask.getId());
        //更新当前节点
        flowTask.setThisStep("开始");
        flowTask.setCompletion(FlowNature.ProcessCompletion);
        flowTask.setStatus(revokeRule || !isParentId ? FlowTaskStatusEnum.Revoke.getCode() : FlowTaskStatusEnum.Resubmit.getCode());
        flowTaskService.update(flowTask);
        //撤回记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(childNode.getTaskId());
        operator.setTaskNodeId(childNode.getTaskNodeId());
        operator.setNodeName(properties.getTitle());
        operator.setNodeCode(properties.getNodeId());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.revoke.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //撤回事件
        operatorRecord.setHandleStatus(FlowTaskStatusEnum.Revoke.getCode());
        flowMsgUtil.event(3, childNode, operatorRecord, flowModel);
        List<FlowParameterModel> parameterModels = FlowContextHolder.getAllEvent();
        if (parameterModels.size() > 0) {
            FlowModel cancelModel = new FlowModel();
            cancelModel.setUserInfo(flowModel.getUserInfo());
            cancel(Collections.singletonList(flowTask.getId()), cancelModel);
        } else {
            List<String> childAllList = flowTaskService.getChildAllList(flowTask.getId());
            childAllList.remove(flowTask.getId());
            FlowModel cancelModel = new FlowModel();
            cancelModel.setUserInfo(flowModel.getUserInfo());
            revoke(childAllList, cancelModel, false);
        }
    }


    @Override
    @DSTransactional
    public void revoke(List<String> id, FlowModel flowModel, boolean isParentId) throws WorkFlowException {
        List<FlowTaskEntity> taskList = flowTaskService.getOrderStaList(id, FlowTaskEntity::getId,
                FlowTaskEntity::getParentId, FlowTaskEntity::getFlowType, FlowTaskEntity::getFullName,
                FlowTaskEntity::getStatus, FlowTaskEntity::getFlowId
        );
        if (isParentId) {
            long count = taskList.stream().filter(t -> StringUtil.isNotEmpty(t.getParentId()) && !FlowNature.ParentId.equals(t.getParentId())).count();
            if (count > 0) {
                throw new WorkFlowException("子流程无法撤回");
            }
        }
        for (FlowTaskEntity entity : taskList) {
            FlowModel model = JsonUtil.getJsonToBean(flowModel, FlowModel.class);
            revoke(entity, model, isParentId);
        }
    }

    @Override
    @DSTransactional
    public void cancel(FlowTaskEntity flowTask, FlowModel flowModel) throws WorkFlowException {
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        UserInfo userInfo = flowModel.getUserInfo();
        //终止记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(flowTask.getId());
        operator.setNodeCode(flowTask.getThisStepId());
        operator.setNodeName(flowTask.getThisStep());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.cancel.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //更新实例
        flowTask.setStatus(FlowTaskStatusEnum.Cancel.getCode());
        flowTask.setEndTime(new Date());
        flowTaskService.update(flowTask);
        //发送消息
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(flowTask.getId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getId,
                FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodeType,
                FlowTaskNodeEntity::getNodePropertyJson
        );
        FlowTaskNodeEntity start = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().orElse(null);
        if (start != null) {
            FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setEnd(true);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setTaskEntity(flowTask);
            flowMsgModel.setTaskNodeEntity(start);
            flowMsgModel.setFlowTemplateAllModel(templateAllModel);
            ChildNode childNode = JsonUtil.getJsonToBean(templateAllModel.getTemplateJson().getFlowTemplateJson(), ChildNode.class);
            String formId = childNode.getProperties().getFormId();
            Map<String, Object> data = serviceUtil.infoData(formId, flowTask.getId());
            flowMsgModel.setData(data);
            flowMsgModel.setFlowModel(flowModel);
            flowMsgUtil.message(flowMsgModel);
            //递归查询子流程，并终止
            List<String> childAllList = flowTaskService.getChildAllList(flowTask.getId());
            childAllList.remove(flowTask.getId());
            FlowModel cancelModel = new FlowModel();
            cancelModel.setUserInfo(flowModel.getUserInfo());
            cancel(childAllList, cancelModel);
        }
    }

    @Override
    @DSTransactional
    public void cancel(List<String> id, FlowModel flowModel) throws WorkFlowException {
        List<FlowTaskEntity> orderStaList = flowTaskService.getOrderStaList(id,
                FlowTaskEntity::getStatus, FlowTaskEntity::getId, FlowTaskEntity::getThisStepId,
                FlowTaskEntity::getThisStep, FlowTaskEntity::getFlowId
        );
        for (FlowTaskEntity entity : orderStaList) {
            FlowModel model = JsonUtil.getJsonToBean(flowModel, FlowModel.class);
            cancel(entity, model);
        }
    }

    @Override
    @DSTransactional
    public void assign(String id, FlowModel flowModel) throws WorkFlowException {
        FlowTaskEntity flowTask = flowTaskService.getInfo(id);
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        List<FlowTaskOperatorEntity> list = flowTaskOperatorService.getList(id).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState()) && flowModel.getNodeCode().equals(t.getNodeCode())).collect(Collectors.toList());
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(flowTask.getId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson, FlowTaskNodeEntity::getNodeCode,
                FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodeName, FlowTaskNodeEntity::getNodeType
        );
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(t -> flowModel.getNodeCode().equals(t.getNodeCode())).findFirst().orElse(null);
        ChildNodeList childNode = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        boolean isRejct = list.stream().filter(t -> StringUtil.isNotEmpty(t.getReject())).count() > 0;
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        TaskOperatoUser taskOperatoUser = new TaskOperatoUser();
        taskOperatoUser.setDate(new Date());
        taskOperatoUser.setChildNode(childNode);
        taskOperatoUser.setAutomation("");
        taskOperatoUser.setId(FlowNature.ParentId);
        taskOperatoUser.setHandLeId(flowModel.getFreeApproverUserId());
        taskOperatoUser.setSortCode(1);
        taskOperatoUser.setRejectUser(isRejct);
        flowTaskUtil.operatorUser(operatorList, taskOperatoUser);
        List<String> idAll = list.stream().map(FlowTaskOperatorEntity::getId).collect(Collectors.toList());
        flowTaskOperatorService.deleteList(idAll);
        flowTaskOperatorService.create(operatorList);
        Set<String> taskNodeId = new HashSet() {{
            add(taskNode.getId());
        }};
        flowOperatorUserService.updateReject(id, taskNodeId);
        //指派记录
        UserInfo userInfo = flowModel.getUserInfo();
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(taskNode.getTaskId());
        operator.setNodeCode(taskNode.getNodeCode());
        operator.setNodeName(taskNode.getNodeName());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.assign.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).operatorId(flowModel.getFreeApproverUserId()).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //发送消息
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        FlowMsgModel flowMsgModel = new FlowMsgModel();
        flowMsgModel.setData(JsonUtil.stringToMap(flowTask.getFlowFormContentJson()));
        flowMsgModel.setNodeList(taskNodeList);
        flowMsgModel.setOperatorList(operatorList);
        flowMsgModel.setTaskNodeEntity(taskNode);
        flowMsgModel.setTaskEntity(flowTask);
        flowMsgModel.setFlowTemplateAllModel(templateAllModel);
        flowMsgModel.setFlowModel(flowModel);
        flowMsgUtil.message(flowMsgModel);
        //超时
        insTimeOutRedis(flowModel, operatorList, userInfo, flowTask, taskNodeList);
    }

    @Override
    public void transfer(FlowTaskOperatorEntity taskOperator, FlowModel flowModel) throws WorkFlowException {
        flowTaskOperatorService.update(taskOperator);
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(taskOperator.getTaskId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson, FlowTaskNodeEntity::getNodeCode,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodeType
        );
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(t -> t.getId().equals(taskOperator.getTaskNodeId())).findFirst().orElse(null);
        ChildNodeList childNode = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        FlowTaskEntity flowTask = flowTaskService.getInfoSubmit(taskNode.getTaskId(), FlowTaskEntity::getId, FlowTaskEntity::getFlowId, FlowTaskEntity::getFullName, FlowTaskEntity::getCreatorUserId, FlowTaskEntity::getStatus, FlowTaskEntity::getFlowFormContentJson);
        //判断流程是否处于挂起状态
        flowTaskUtil.isSuspend(flowTask);
        //转办记录
        UserInfo userInfo = flowModel.getUserInfo();
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(taskOperator.getTaskId());
        operator.setNodeCode(taskOperator.getNodeCode());
        operator.setTaskNodeId(taskOperator.getTaskNodeId());
        operator.setNodeName(taskOperator.getNodeName());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(FlowRecordEnum.transfer.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).operatorId(taskOperator.getHandleId()).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //自动审批
        List<FlowTaskOperatorEntity> operatorListAll = new ArrayList<>();
        List<UserEntity> userName = new ArrayList() {{
            UserEntity user = new UserEntity();
            user.setId(taskOperator.getHandleId());
            add(user);
        }};
        TaskOperatoUser taskOperatoUser = new TaskOperatoUser();
        taskOperatoUser.setDate(new Date());
        taskOperatoUser.setId(FlowNature.ParentId);
        taskOperatoUser.setHandLeId(operator.getHandleId());
        taskOperatoUser.setChildNode(childNode);
        FlowAgreeRuleModel ruleModel = FlowAgreeRuleModel.builder().operatorListAll(operatorListAll).taskOperatoUser(taskOperatoUser).flowTask(flowTask).userName(userName).childNode(childNode).taskNodeList(taskNodeList).reject(false).build();
        flowTaskUtil.flowAgreeRule(ruleModel);
        operatorListAll.stream().forEach(t -> t.setId(taskOperator.getId()));
        //发送消息
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        List<FlowTaskOperatorEntity> operatorList = new ArrayList() {{
            FlowTaskOperatorEntity operatorEntity = new FlowTaskOperatorEntity();
            operatorEntity.setId(taskOperator.getId());
            operatorEntity.setTaskId(operatorRecord.getTaskId());
            operatorEntity.setHandleId(taskOperator.getHandleId());
            operatorEntity.setTaskNodeId(operatorRecord.getTaskNodeId());
            add(operatorEntity);
        }};
        FlowMsgModel flowMsgModel = new FlowMsgModel();
        flowMsgModel.setNodeList(taskNodeList);
        flowMsgModel.setOperatorList(operatorList);
        flowMsgModel.setData(JsonUtil.stringToMap(flowTask.getFlowFormContentJson()));
        flowMsgModel.setTaskNodeEntity(taskNode);
        flowMsgModel.setTaskEntity(flowTask);
        flowMsgModel.setFlowTemplateAllModel(templateAllModel);
        flowMsgModel.setFlowModel(flowModel);
        flowMsgUtil.message(flowMsgModel);
        //自动审批
        FlowApproveModel approveModel = FlowApproveModel.builder().operatorList(operatorListAll).taskNodeList(taskNodeList).flowTask(flowTask).flowModel(flowModel).build();
        flowTaskUtil.approve(approveModel);
        //超时
        insTimeOutRedis(flowModel, operatorListAll, userInfo, flowTask, taskNodeList);
    }

    @Override
    @DSTransactional
    public void transfer(FlowModel flowModel) throws WorkFlowException {
        String id = flowModel.getId();
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        FlowTaskEntity flowTask = flowTaskService.getInfo(operator.getTaskId());
        flowModel.setUserInfo(userProvider.get());
        permissions(operator.getHandleId(), flowTask, operator, "", flowModel);
        operator.setHandleId(flowModel.getFreeApproverUserId());
        operator.setCreatorTime(new Date());
        transfer(operator, flowModel);
    }


    @Override
    public FlowBeforeInfoVO getBeforeInfo(FlowModel flowModel) throws WorkFlowException {
        FlowBeforeInfoVO vo = new FlowBeforeInfoVO();
        String operatorId = flowModel.getTaskOperatorId();
        String flowId = flowModel.getFlowId();
        boolean parentId = FlowNature.ParentId.equals(flowModel.getId());
        FlowTaskEntity taskEntity = parentId ? null : flowTaskService.getInfoSubmit(flowModel.getId(),
                FlowTaskEntity::getFlowId, FlowTaskEntity::getId, FlowTaskEntity::getRejectId,
                FlowTaskEntity::getFrestore, FlowTaskEntity::getThisStepId, FlowTaskEntity::getCreatorUserId,
                FlowTaskEntity::getTaskNodeId, FlowTaskEntity::getThisStepId, FlowTaskEntity::getThisStep,
                FlowTaskEntity::getFlowCode, FlowTaskEntity::getFullName, FlowTaskEntity::getFlowType,
                FlowTaskEntity::getFlowCategory, FlowTaskEntity::getEnabledMark, FlowTaskEntity::getCompletion,
                FlowTaskEntity::getFlowName, FlowTaskEntity::getFlowUrgent, FlowTaskEntity::getFlowVersion,
                FlowTaskEntity::getStatus, FlowTaskEntity::getTaskNodeId
        );
        FlowTemplateAllModel templateAllModel = taskEntity != null ? flowTaskUtil.templateJson(taskEntity.getFlowId()) : flowTaskUtil.templateJson(flowId);
        FlowTemplateJsonEntity templateJson = templateAllModel.getTemplateJson();
        FlowTemplateEntity template = templateAllModel.getTemplate();
        ChildNodeList childNodeAll = JsonUtil.getJsonToBean(templateJson.getFlowTemplateJson(), ChildNodeList.class);
        String formId = childNodeAll.getProperties().getFormId();
        int num = 0;
        UserInfo userInfo = userProvider.get();
        if (taskEntity != null) {
            boolean isRejectId = StringUtil.isEmpty(taskEntity.getRejectId());
            TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(taskEntity.getId()).state(FlowNodeEnum.Process.getCode()).build();
            List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                    FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getCompletion, FlowTaskNodeEntity::getState, FlowTaskNodeEntity::getTaskId,
                    FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getNodePropertyJson,
                    FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getNodeName, FlowTaskNodeEntity::getFormId,
                    FlowTaskNodeEntity::getCreatorTime, FlowTaskNodeEntity::getCandidates, FlowTaskNodeEntity::getDraftData
            );
            //排除分支没有走过的节点
            flowTaskUtil.nodeList(taskNodeList);
            List<Integer> state = ImmutableList.of(FlowNodeEnum.Process.getCode(), FlowNodeEnum.FreeApprover.getCode());
            List<FlowTaskOperatorEntity> taskOperatorList = flowTaskOperatorService.getList(taskEntity.getId()).stream().filter(t -> state.contains(t.getState()) && FlowNature.ParentId.equals(t.getParentId())).collect(Collectors.toList());
            Map<String, List<FlowTaskOperatorEntity>> nodeIdList = taskOperatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId));
            List<FlowTaskOperatorRecordEntity> operatorRecordList = flowTaskOperatorRecordService.getList(taskEntity.getId());
            List<FlowOperatorUserEntity> operatorUserList = flowOperatorUserService.getList(taskEntity.getId());
            Map<String, List<FlowOperatorUserEntity>> operatorNodeIdList = operatorUserList.stream().collect(Collectors.groupingBy(FlowOperatorUserEntity::getTaskNodeId));
            //流程任务
            FlowTaskModel flowTaskModel = JsonUtil.getJsonToBean(taskEntity, FlowTaskModel.class);
            flowTaskModel.setType(template.getType());
            flowTaskModel.setFlowId(template.getId());
            flowTaskModel.setRejectDataId(taskEntity.getRejectId());
            flowTaskModel.setSuspend(Objects.equals(taskEntity.getFrestore(), 1));
            vo.setFlowTaskInfo(flowTaskModel);
            //已办人员
            List<FlowTaskOperatorRecordModel> recordList = new ArrayList<>();
            List<String> userIdAll = new ArrayList<>();
            operatorRecordList.stream().forEach(t -> {
                userIdAll.add(t.getHandleId());
                if (StringUtil.isNotEmpty(t.getOperatorId())) {
                    userIdAll.add(t.getOperatorId());
                }
            });
            userIdAll.addAll(taskOperatorList.stream().map(FlowTaskOperatorEntity::getHandleId).collect(Collectors.toList()));
            userIdAll.addAll(operatorUserList.stream().map(FlowOperatorUserEntity::getHandleId).collect(Collectors.toList()));
            userIdAll.add(taskEntity.getCreatorUserId());
            List<UserEntity> userList = serviceUtil.getUserName(userIdAll);
            for (FlowTaskOperatorRecordEntity entity : operatorRecordList) {
                List<FlowEventLogEntity> list = new ArrayList<>();
                if(StringUtil.isNotEmpty(entity.getTaskNodeId())) {
                    list.addAll(flowEventLogService.getList(ImmutableList.of(entity.getTaskNodeId())));
                }
                FlowTaskOperatorRecordModel infoModel = JsonUtil.getJsonToBean(entity, FlowTaskOperatorRecordModel.class);
                UserEntity operatorName = userList.stream().filter(t -> t.getId().equals(entity.getOperatorId())).findFirst().orElse(null);
                infoModel.setOperatorId(operatorName != null ? operatorName.getRealName() + "/" + operatorName.getAccount() : "");
                UserEntity userName = userList.stream().filter(t -> t.getId().equals(entity.getHandleId())).findFirst().orElse(null);
                infoModel.setUserName(userName != null ? userName.getRealName() + "/" + userName.getAccount() : "");
                FlowTaskOperatorEntity operatorEntity = taskOperatorList.stream().filter(t -> t.getId().equals(entity.getTaskOperatorId())).findFirst().orElse(null);
                int status = 0;
                if (operatorEntity != null) {
                    status += FlowNature.ParentId.equals(operatorEntity.getParentId()) ? 0 : 1;
                    infoModel.setCreatorTime(operatorEntity.getCreatorTime().getTime());
                } else {
                    infoModel.setCreatorTime(entity.getHandleTime().getTime());
                }
                infoModel.setStatus(status);
                infoModel.setIsLog(list.size() > 0);
                recordList.add(infoModel);
            }
            vo.setFlowTaskOperatorRecordList(recordList);
            //流程节点
            String[] tepId = taskEntity.getThisStepId() != null ? taskEntity.getThisStepId().split(",") : new String[]{};
            List<String> tepIdAll = Arrays.asList(tepId);
            List<FlowTaskNodeModel> flowTaskNodeListAll = JsonUtil.getJsonToList(taskNodeList, FlowTaskNodeModel.class);
            for (FlowTaskNodeModel model : flowTaskNodeListAll) {
                if (FlowNature.AuditCompletion.equals(model.getCompletion())) {
                    model.setType("0");
                }
                if (tepIdAll.contains(model.getNodeCode())) {
                    model.setType("1");
                    if (FlowNature.NodeEnd.equals(model.getNodeCode())) {
                        model.setType("0");
                    }
                }
                //查询审批人
                ChildNodeList childNode = JsonUtil.getJsonToBean(model.getNodePropertyJson(), ChildNodeList.class);
                Custom custom = childNode.getCustom();
                Properties properties = childNode.getProperties();
                Integer type = properties.getAssigneeType();
                List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
                TaskOperator taskOperator = new TaskOperator();
                taskOperator.setChildNode(childNode);
                taskOperator.setTaskEntity(taskEntity);
                taskOperator.setFlowModel(flowModel);
                taskOperator.setDetails(false);
                taskOperator.setVerify(false);
                taskOperator.setExtraRule(true);
                taskOperator.setTaskNodeList(taskNodeList);
                List<String> userName = new ArrayList<>();
                if (FlowNature.NodeStart.equals(custom.getType())) {
                    UserEntity startUser = userList.stream().filter(t -> t.getId().equals(taskEntity.getCreatorUserId())).findFirst().orElse(null);
                    userName.add(startUser != null ? startUser.getRealName() + "/" + startUser.getAccount() : "");
                } else if (FlowNature.NodeSubFlow.equals(custom.getType())) {
                    List<UserEntity> list = new ArrayList<>();
                    List<String> taskListAll = new ArrayList() {{
                        addAll(childNode.getCustom().getAsyncTaskList());
                        addAll(childNode.getCustom().getTaskId());
                    }};
                    if (taskListAll.size() > 0) {
                        //子流程人员
                        List<String> childList = flowTaskService.getOrderStaList(taskListAll, FlowTaskEntity::getCreatorUserId).stream().map(FlowTaskEntity::getCreatorUserId).collect(Collectors.toList());
                        List<UserEntity> subFlowUserList = serviceUtil.getUserName(childList);
                        list.addAll(subFlowUserList);
                    } else {
                        TaskOperator taskChild = new TaskOperator();
                        taskChild.setChildNode(childNode);
                        taskChild.setTaskEntity(taskEntity);
                        taskChild.setFlowModel(flowModel);
                        taskChild.setTaskNodeList(taskNodeList);
                        taskChild.setVerify(false);
                        list.addAll(flowTaskUtil.childSaveList(taskChild));
                    }
                    List<String> nameList = new ArrayList<>();
                    for (UserEntity entity : list) {
                        nameList.add(entity.getRealName() + "/" + entity.getAccount());
                    }
                    userName.addAll(nameList);
                } else if (!FlowNature.NodeEnd.equals(custom.getNodeId())) {
                    List<UserEntity> operatorUser = new ArrayList<>();
                    List<String> list = nodeIdList.get(model.getId()) != null ? nodeIdList.get(model.getId()).stream().map(FlowTaskOperatorEntity::getHandleId).collect(Collectors.toList()) : new ArrayList<>();
                    List<String> operatorUserIdList = operatorNodeIdList.get(model.getId()) != null ? operatorNodeIdList.get(model.getId()).stream().map(FlowOperatorUserEntity::getHandleId).collect(Collectors.toList()) : new ArrayList<>();
                    Set<String> userAll = new LinkedHashSet() {{
                        addAll(operatorUserIdList.size() > 0 ? operatorUserIdList : list);
                    }};
                    for (String userId : userAll) {
                        operatorUser.addAll(userList.stream().filter(t -> t.getId().equals(userId)).collect(Collectors.toList()));
                    }
                    boolean isShow = true;
                    //环节还没有经过和当前不显示审批人
                    if (isRejectId) {
                        if (FlowTaskOperatorEnum.Tache.getCode().equals(type) || FlowTaskOperatorEnum.Candidate.getCode().equals(type)) {
                            List<String> typeList = ImmutableList.of("0", "1");
                            boolean completion = typeList.contains(model.getType());
                            if (!completion) {
                                isShow = false;
                            }
                        }
                    }
                    if (isShow) {
                        if (userAll.size() == 0) {
                            flowTaskUtil.operator(operatorList, taskOperator);
                            List<String> handleId = operatorList.stream().map(FlowTaskOperatorEntity::getHandleId).collect(Collectors.toList());
                            List<UserEntity> operator = serviceUtil.getUserName(handleId);
                            operatorUser.addAll(operator);
                        }
                        List<String> nameList = new ArrayList<>();
                        for (UserEntity operator : operatorUser) {
                            nameList.add(operator.getRealName() + "/" + operator.getAccount());
                        }
                        userName.addAll(nameList);
                    }
                }
                model.setUserName(String.join(",", userName));
            }
            vo.setFlowTaskNodeList(flowTaskNodeListAll);
            String taskNodeId = flowModel.getTaskNodeId();
            boolean isRestore = StringUtil.isNotEmpty(operatorId) || StringUtil.isNotEmpty(taskNodeId);
            if (isRestore) {
                //草稿数据、复活数据
                FlowTaskOperatorEntity operator = taskOperatorList.stream().filter(t -> t.getId().equals(operatorId)).findFirst().orElse(null);
                boolean isOperator = operator != null;
                String draftTaskNodeId = isOperator ? operator.getTaskNodeId() : taskNodeId;
                if (isOperator) {
                    vo.getFlowTaskInfo().setThisStep(operator.getNodeName());
                }
                FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(t -> t.getId().equals(draftTaskNodeId)).findFirst().orElse(null);
                List<String> versionList = ImmutableList.of(TableFeildsEnum.VERSION.getField());
                Map<String, Object> draftDataAll = new HashMap<>();
                if (taskNode != null) {
                    Map<String, Object> objectMap = flowTaskUtil.infoData(taskNode.getFormId(), taskEntity.getId());
                    //草稿数据
                    Map<String, Object> operatorData = isOperator && StringUtil.isNotEmpty(operator.getDraftData()) ? JsonUtil.stringToMap(operator.getDraftData()) : null;
                    //复活数据
                    Map<String, Object> taskNodeData = StringUtil.isNotEmpty(taskNode.getDraftData()) && StringUtil.isNotEmpty(taskEntity.getTaskNodeId()) ? JsonUtil.stringToMap(taskNode.getDraftData()) : null;
                    Map<String, Object> draftData = operatorData != null ? operatorData : taskNodeData;
                    if (draftData != null) {
                        draftData.keySet().removeIf(key -> versionList.contains(key.toLowerCase()));
                        objectMap.putAll(draftData);
                        draftDataAll.putAll(objectMap);
                        vo.setDraftData(draftDataAll);
                        num++;
                    }
                    childNodeAll = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
                    List<Map<String, Object>> formOperates = flowTaskUtil.formOperates(childNodeAll);
                    childNodeAll.getProperties().setFormOperates(formOperates);
                    formId = taskNode.getFormId();
                }
            }
            //流程监控权限
            if (!userInfo.getIsAdministrator()) {
                List<String> organizeUserList = serviceUtil.getOrganizeUserList("edit");
                vo.setNoOperateAuth(!organizeUserList.contains(taskEntity.getCreatorUserId()));
            }
        }
        FlowFormEntity form = serviceUtil.getForm(formId);
        vo.setFlowFormInfo(JsonUtil.getJsonToBean(form, FlowFormVo.class));
        Properties approversProperties = childNodeAll.getProperties();
        vo.setApproversProperties(approversProperties);
        vo.setFormOperates(approversProperties.getFormOperates());
        FlowTemplateModel templateModel = JsonUtil.getJsonToBean(templateJson, FlowTemplateModel.class);
        templateModel.setType(template.getType());
        templateModel.setFullName(template.getFullName());
        vo.setFlowTemplateInfo(templateModel);
        if (!parentId && num == 0) {
            vo.setFormData(serviceUtil.infoData(formId, flowModel.getId()));
        }
        return vo;
    }

    @Override
    public List<FlowSummary> recordList(String id, String category, String type) {
        //审批汇总
        List<Integer> handleStatus = new ArrayList<>();
        if (!"0".equals(type)) {
            handleStatus.add(0);
            handleStatus.add(1);
        }
        List<FlowTaskOperatorRecordEntity> recordListAll = flowTaskOperatorRecordService.getRecordList(id, handleStatus);
        List<String> userIdAll = new ArrayList<>();
        List<String> userIdList = recordListAll.stream().map(FlowTaskOperatorRecordEntity::getHandleId).collect(Collectors.toList());
        List<String> operatorId = recordListAll.stream().filter(t -> StringUtil.isNotEmpty(t.getOperatorId())).map(FlowTaskOperatorRecordEntity::getOperatorId).collect(Collectors.toList());
        userIdAll.addAll(userIdList);
        userIdAll.addAll(operatorId);
        List<UserEntity> userList = serviceUtil.getUserName(userIdAll);
        List<FlowSummary> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Map<String, List<FlowTaskOperatorRecordEntity>> operatorAll = new HashMap<>();
        if (FlowRecordListEnum.position.getCode().equals(category)) {
            List<String> userId = userList.stream().map(UserEntity::getId).collect(Collectors.toList());
            List<UserRelationEntity> relationList = serviceUtil.getListByUserIdAll(userId);
            List<String> objectId = relationList.stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList());
            List<PositionEntity> positionListAll = serviceUtil.getPositionName(objectId);
            for (PositionEntity entity : positionListAll) {
                map.put(entity.getId(), entity.getFullName());
                List<String> userAll = relationList.stream().filter(t -> t.getObjectId().equals(entity.getId())).map(UserRelationEntity::getUserId).collect(Collectors.toList());
                List<FlowTaskOperatorRecordEntity> operator = new ArrayList<>();
                for (FlowTaskOperatorRecordEntity recordEntity : recordListAll) {
                    if (userAll.contains(recordEntity.getHandleId())) {
                        operator.add(recordEntity);
                    }
                }
                operatorAll.put(entity.getId(), operator);
            }
        } else if (FlowRecordListEnum.role.getCode().equals(category)) {
            List<String> userId = userList.stream().map(UserEntity::getId).collect(Collectors.toList());
            List<UserRelationEntity> relationList = serviceUtil.getListByUserIdAll(userId);
            List<String> objectId = relationList.stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList());
            List<RoleEntity> roleListAll = serviceUtil.getListByIds(objectId);
            for (RoleEntity entity : roleListAll) {
                map.put(entity.getId(), entity.getFullName());
                List<String> userAll = relationList.stream().filter(t -> t.getObjectId().equals(entity.getId())).map(UserRelationEntity::getUserId).collect(Collectors.toList());
                List<FlowTaskOperatorRecordEntity> operator = new ArrayList<>();
                for (FlowTaskOperatorRecordEntity recordEntity : recordListAll) {
                    if (userAll.contains(recordEntity.getHandleId())) {
                        operator.add(recordEntity);
                    }
                }
                operatorAll.put(entity.getId(), operator);
            }
        } else if (FlowRecordListEnum.department.getCode().equals(category)) {
            List<String> organizeList = userList.stream().map(UserEntity::getOrganizeId).collect(Collectors.toList());
            List<OrganizeEntity> organizeListAll = serviceUtil.getOrganizeName(organizeList);
            for (OrganizeEntity entity : organizeListAll) {
                map.put(entity.getId(), entity.getFullName());
                List<String> userAll = userList.stream().filter(t -> t.getOrganizeId().equals(entity.getId())).map(UserEntity::getId).collect(Collectors.toList());
                List<FlowTaskOperatorRecordEntity> operator = new ArrayList<>();
                for (FlowTaskOperatorRecordEntity recordEntity : recordListAll) {
                    if (userAll.contains(recordEntity.getHandleId())) {
                        operator.add(recordEntity);
                    }
                }
                operatorAll.put(entity.getId(), operator);
            }
        }
        for (String key : map.keySet()) {
            String fullName = map.get(key);
            FlowSummary summary = new FlowSummary();
            summary.setId(key);
            summary.setFullName(fullName);
            List<FlowTaskOperatorRecordEntity> recordList = operatorAll.get(key);
            List<FlowSummary> childList = new ArrayList<>();
            for (FlowTaskOperatorRecordEntity entity : recordList) {
                FlowSummary childSummary = JsonUtil.getJsonToBean(entity, FlowSummary.class);
                UserEntity user = userList.stream().filter(t -> t.getId().equals(entity.getHandleId())).findFirst().orElse(null);
                childSummary.setUserName(user != null ? user.getRealName() + "/" + user.getAccount() : "");
                UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(entity.getOperatorId())).findFirst().orElse(null);
                childSummary.setOperatorId(userEntity != null ? userEntity.getRealName() + "/" + userEntity.getAccount() : "");
                childSummary.setHeadIcon(UploaderUtil.uploaderImg(user.getHeadIcon()));
                childList.add(childSummary);
            }
            summary.setList(childList);
            list.add(summary);
        }
        return list;
    }

    @Override
    public boolean press(String id, FlowModel flowModel) throws WorkFlowException {
        FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(id,
                FlowTaskEntity::getId, FlowTaskEntity::getFullName, FlowTaskEntity::getStatus,
                FlowTaskEntity::getCreatorUserId, FlowTaskEntity::getFlowId
        );
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTaskEntity.getFlowId());
        Map<String, Object> data = flowTaskUtil.startData(templateAllModel, flowTaskEntity);
        List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.press(id);
        boolean flag = operatorList.size() > 0;
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(id).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson,
                FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getNodeName,
                FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getFormId
        );
        //发送消息
        FlowMsgModel flowMsgModel = new FlowMsgModel();
        flowMsgModel.setNodeList(taskNodeList);
        flowMsgModel.setOperatorList(operatorList);
        flowMsgModel.setTaskEntity(flowTaskEntity);
        flowMsgModel.setData(data);
        flowMsgModel.setFlowTemplateAllModel(templateAllModel);
        flowMsgModel.setFlowModel(flowModel);
        flowMsgUtil.message(flowMsgModel);
        return flag;
    }

    @Override
    public FlowCandidateVO candidates(String id, FlowModel flowModel, boolean batch) throws WorkFlowException {
        boolean parentId = FlowNature.ParentId.equals(id);
        FlowTaskOperatorEntity operator = parentId ? null : flowTaskOperatorService.getOperatorInfo(id);
        String flowTaskId = operator != null ? operator.getTaskId() : flowModel.getId();
        FlowTaskEntity flowTask = StringUtil.isNotEmpty(flowTaskId) ? flowTaskService.getInfoSubmit(flowTaskId, FlowTaskEntity::getRejectId) : null;
        List<ChildNodeList> childNodeListAll = flowTaskUtil.childNodeListAll(operator, flowModel);
        List<FlowCandidateListModel> listVO = new ArrayList<>();
        FlowCandidateVO vo = new FlowCandidateVO();
        boolean branchFlow = childNodeListAll.stream().filter(t -> t.getCustom().getBranchFlow()).count() > 0;
        int candidates = 0;
        for (ChildNodeList childNodeList : childNodeListAll) {
            Properties properties = childNodeList.getProperties();
            Custom custom = childNodeList.getCustom();
            String nodeId = custom.getNodeId();
            String nodeName = properties.getTitle();
            Integer assigneeType = properties.getAssigneeType();
            Integer initiateType = properties.getInitiateType();
            FlowCandidateListModel candidateVO = new FlowCandidateListModel();
            candidateVO.setNodeName(nodeName);
            candidateVO.setNodeId(nodeId);
            boolean isCandidates = (FlowTaskOperatorEnum.Candidate.getCode().equals(initiateType) || FlowTaskOperatorEnum.Candidate.getCode().equals(assigneeType));
            candidateVO.setIsCandidates(isCandidates);
            boolean isBranchFlow = custom.getBranchFlow();
            candidateVO.setIsBranchFlow(isBranchFlow);
            if (isCandidates) {
                List<String> list = new ArrayList<>();
                list.addAll(properties.getApprovers());
                list.addAll(properties.getInitiator());
                List<String> userId = serviceUtil.getUserListAll(list);
                Pagination pagination = JsonUtil.getJsonToBean(flowModel, Pagination.class);
                List<UserEntity> userName = serviceUtil.getUserName(userId, pagination);
                candidateVO.setHasCandidates(userName.size() > 0);
            }
            candidates += isCandidates ? 1 : 0;
            listVO.add(candidateVO);
        }
        if (batch && branchFlow) {
            throw new WorkFlowException("下一节点为选择分支无法批量审批!");
        }
        int flowType = branchFlow ? 1 : candidates > 0 ? 2 : 3;
        if (operator != null) {
            if (branchFlow) {
                FlowTaskNodeEntity taskNode = flowTaskNodeService.getInfo(operator.getTaskNodeId(), FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodePropertyJson);
                List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(operator.getTaskId()).stream().filter(t -> t.getTaskNodeId().equals(operator.getTaskNodeId()) && FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
                operatorList.stream().forEach(t -> {
                    if (t.getId().equals(id)) {
                        t.setCompletion(FlowNature.AuditCompletion);
                    }
                    if (StringUtil.isEmpty(t.getRollbackId()) && !FlowNature.ParentId.equals(t.getParentId())) {
                        t.setCompletion(FlowNature.AuditCompletion);
                    }
                });
                FlowCountersignModel countersign = new FlowCountersignModel();
                countersign.setTaskNode(taskNode);
                countersign.setOperatorList(operatorList);
                boolean isCountersign = flowTaskUtil.isCountersign(countersign);
                flowType = isCountersign ? flowType : branchFlow ? 3 : candidates > 0 ? 2 : 3;
            }
            if (!FlowNature.ParentId.equals(operator.getParentId())) {
                flowType = 3;
            }
        }
        if (flowTask != null && StringUtil.isNotEmpty(flowTask.getRejectId())) {
            flowType = 3;
        }
        vo.setType(flowType);
        vo.setList(listVO);
        return vo;
    }

    @Override
    public List<FlowCandidateUserModel> candidateUser(String id, FlowModel flowModel) throws WorkFlowException {
        List<FlowCandidateUserModel> dataList = new ArrayList<>();
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getOperatorInfo(id);
        List<ChildNodeList> childNodeListAll = flowTaskUtil.childNodeListAll(operator, flowModel);
        for (ChildNodeList childNodeList : childNodeListAll) {
            Properties properties = childNodeList.getProperties();
            List<String> list = new ArrayList<>();
            list.addAll(properties.getApprovers());
            list.addAll(properties.getInitiator());
            List<String> userId = serviceUtil.getUserListAll(list);
            Pagination pagination = JsonUtil.getJsonToBean(flowModel, Pagination.class);
            List<UserEntity> userName = serviceUtil.getUserName(userId, pagination);
            flowModel.setTotal(pagination.getTotal());
            List<String> userIdAll = userName.stream().map(UserEntity::getId).collect(Collectors.toList());
            Map<String, List<UserRelationEntity>> userMap = serviceUtil.getListByUserIdAll(userIdAll).stream().filter(t -> PermissionConst.ORGANIZE.equals(t.getObjectType())).collect(Collectors.groupingBy(UserRelationEntity::getUserId));
            for (UserEntity entity : userName) {
                List<UserRelationEntity> listByUserId = userMap.get(entity.getId()) != null ? userMap.get(entity.getId()) : new ArrayList<>();
                StringJoiner joiner = new StringJoiner(",");
                for (UserRelationEntity relation : listByUserId) {
                    List<OrganizeEntity> organizeId = serviceUtil.getOrganizeId(relation.getObjectId());
                    if (organizeId.size() > 0) {
                        String organizeName = organizeId.stream().map(OrganizeEntity::getFullName).collect(Collectors.joining("/"));
                        joiner.add(organizeName);
                    }
                }
                FlowCandidateUserModel userModel = JsonUtil.getJsonToBean(entity, FlowCandidateUserModel.class);
                userModel.setFullName(entity.getRealName() + "/" + entity.getAccount());
                userModel.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
                userModel.setOrganize(joiner.toString());
                dataList.add(userModel);
            }
        }
        return dataList;
    }

    @Override
    @DSTransactional
    public void batch(FlowModel flowModel) throws WorkFlowException {
        List<String> idList = flowModel.getIds();
        Integer batchType = flowModel.getBatchType();
        for (String id : idList) {
            FlowModel model = JsonUtil.getJsonToBean(flowModel, FlowModel.class);
            model.setId(id);
            switch (batchType) {
                case 0:
                    model.setVoluntarily(true);
                    audit(model);
                    break;
                case 1:
                    reject(model);
                    break;
                case 2:
                    transfer(model);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public FlowCandidateVO batchCandidates(String flowId, String taskOperatorId, FlowModel flowModel) throws WorkFlowException {
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowId);
        FlowTemplateJsonEntity templateJson = templateAllModel.getTemplateJson();
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(taskOperatorId);
        FlowTaskNodeEntity taskNode = flowTaskNodeService.getInfo(operator.getTaskNodeId(), FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getId);
        ChildNode childNodeAll = JsonUtil.getJsonToBean(templateJson.getFlowTemplateJson(), ChildNode.class);
        //获取流程节点
        List<ChildNodeList> nodeListAll = new ArrayList<>();
        List<ConditionList> conditionListAll = new ArrayList<>();
        //递归获取条件数据和节点数据
        FlowJsonUtil.getTemplateAll(childNodeAll, nodeListAll, conditionListAll);
        //判断节点是否有在条件中
        boolean isCondition = conditionListAll.stream().filter(t -> operator.getNodeCode().equals(t.getPrevId())).count() > 0;
        List<ChildNodeList> freeApprover = new ArrayList<>();
        List<ChildNodeList> branchFlow = new ArrayList<>();
        if (isCondition) {
            List<String> nodeNext = StringUtil.isNotEmpty(taskNode.getNodeNext()) ? Arrays.asList(taskNode.getNodeNext().split(",")) : new ArrayList<>();
            List<ChildNodeList> nextList = nodeListAll.stream().filter(t -> nodeNext.contains(t.getCustom().getNodeId())).collect(Collectors.toList());
            nextList.stream().forEach(t -> {
                if (FlowTaskOperatorEnum.Candidate.getCode().equals(t.getProperties().getAssigneeType())) {
                    freeApprover.add(t);
                }
                if (t.getCustom().getBranchFlow()) {
                    branchFlow.add(t);
                }
            });
        }
        if (freeApprover.size() > 0) {
            throw new WorkFlowException("条件流程包含候选人无法批量通过！");
        }
        if (branchFlow.size() > 0) {
            throw new WorkFlowException("下一节点为选择分支无法批量审批!");
        }
        return candidates(taskOperatorId, flowModel, true);
    }

    @Override
    public void permissions(String userId, FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, String msg, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        if (operator == null || FlowNodeEnum.Futility.getCode().equals(operator.getState())) {
            throw new WorkFlowException(StringUtil.isEmpty(msg) ? MsgCode.WF122.get() : msg);
        }
        List<String> flowDelegateList = flowDelegateService.getUser(userId, flowTask.getTemplateId(), userInfo.getUserId()).stream().map(FlowDelegateEntity::getToUserId).collect(Collectors.toList());
        flowDelegateList.add(userId);
        if (!flowDelegateList.contains(userInfo.getUserId())) {
            throw new WorkFlowException("没有权限操作");
        }
        if (FlowTaskStatusEnum.Cancel.getCode().equals(flowTask.getStatus())) {
            throw new WorkFlowException("该流程工单已终止");
        }
        if (FlowTaskStatusEnum.Revoke.getCode().equals(flowTask.getStatus())) {
            throw new WorkFlowException("该流程工单已撤回");
        }
    }

    @Override
    @DSTransactional
    public void change(FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = flowModel.getUserInfo();
        FlowTaskEntity flowTask = flowTaskService.getInfo(flowModel.getTaskId());
        List<String> thisStepId = Arrays.asList(flowTask.getThisStepId().split(","));
        //获取节点
        TaskNodeListModel nodeListModel = TaskNodeListModel.builder().id(flowModel.getTaskId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(nodeListModel,
                FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getFormId,
                FlowTaskNodeEntity::getNodeNext, FlowTaskNodeEntity::getNodeType, FlowTaskNodeEntity::getDraftData,
                FlowTaskNodeEntity::getNodePropertyJson, FlowTaskNodeEntity::getNodeName
        );
        FlowTaskNodeEntity taskNodeEntity = taskNodeList.stream().filter(t -> t.getId().equals(flowModel.getTaskNodeId())).findFirst().orElse(null);
        FlowTemplateAllModel templateAllModel = flowTaskUtil.templateJson(flowTask.getFlowId());
        boolean resurgence = flowModel.getResurgence();
        ChildNodeList childNode = JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class);
        if (resurgence) {
            flowTask.setTaskNodeId(flowModel.getTaskNodeId());
            flowTask.setParentId(FlowNature.ParentId);
            flowTask.setStatus(FlowNature.NodeStart.equals(taskNodeEntity.getNodeType()) ? FlowTaskStatusEnum.Draft.getCode() : FlowTaskStatusEnum.Handle.getCode());
            if (Objects.equals(FlowNature.ResurgenceThis, childNode.getProperties().getResurgenceDataRule())) {
                if (StringUtil.isEmpty(taskNodeEntity.getDraftData())) {
                    throw new WorkFlowException("该节点没有数据,无法复活");
                }
            } else {
                Map<String, Object> objectMap = serviceUtil.infoData(taskNodeEntity.getFormId(), flowTask.getId());
                if (ObjectUtil.isEmpty(objectMap)) {
                    throw new WorkFlowException("该节点没有数据,无法复活");
                }
            }
        }
        List<Integer> handleStatus = ImmutableList.of(FlowRecordEnum.audit.getCode(), FlowRecordEnum.submit.getCode());
        FlowTaskOperatorRecordEntity record = flowTaskOperatorRecordService.getList(flowTask.getId()).stream().filter(t -> handleStatus.contains(t.getHandleStatus())).sorted(Comparator.comparing(FlowTaskOperatorRecordEntity::getHandleTime).reversed()).findFirst().orElse(null);
        List<FlowTaskNodeEntity> nodeListAll = taskNodeList.stream().filter(t -> t.getNodeCode().equals(record.getNodeCode())).collect(Collectors.toList());
        for (FlowTaskNodeEntity nodeEntity : nodeListAll) {
            String formId = nodeEntity.getFormId();
            //验证变更是否同表单
            if (!resurgence && !taskNodeEntity.getFormId().equals(formId)) {
                throw new WorkFlowException("此流程不支持变更");
            }
        }
        //当前节点是子流程不能变更
        List<FlowTaskNodeEntity> thisTaskNodeList = taskNodeList.stream().filter(t -> thisStepId.contains(t.getNodeCode())).collect(Collectors.toList());
        boolean isChild = thisTaskNodeList.stream().filter(t -> FlowNature.NodeSubFlow.equals(t.getNodeType())).count() > 0;
        if (isChild) {
            throw new WorkFlowException("当前节点有子流程无法变更");
        }
        flowTaskNodeService.update(taskNodeEntity);
        String nodeId = childNode.getCustom().getNodeId();
        Properties properties = childNode.getProperties();
        Integer progress = properties.getProgress();
        String title = properties.getTitle();
        flowTask.setThisStepId(taskNodeEntity.getNodeCode());
        flowTask.setThisStep(taskNodeEntity.getNodeName());
        flowTask.setRejectId(null);
        flowTask.setCompletion(progress);
        flowTaskService.update(flowTask);
        //获取节点
        List<String> id = taskNodeList.stream().map(FlowTaskNodeEntity::getId).collect(Collectors.toList());
        flowCandidatesService.deleteTaskNodeId(id);
        flowTaskNodeService.updateCompletion(id, FlowNature.AuditCompletion);
        List<FlowTaskNodeEntity> nextTaskNodeList = taskNodeList.stream().filter(t -> thisStepId.contains(t.getNodeCode())).collect(Collectors.toList());
        for (FlowTaskNodeEntity nodeEntity : nextTaskNodeList) {
            flowTaskUtil.change(taskNodeList, nodeEntity, false, FlowNature.AuditCompletion, flowModel);
            if (FlowNature.NodeSubFlow.equals(nodeEntity.getNodeType())) {
                ChildNodeList nodeModel = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), ChildNodeList.class);
                List<String> idAll = nodeModel.getCustom().getTaskId();
                flowTaskService.deleteChildAll(idAll);
            }
        }
        if (taskNodeEntity != null) {
            flowTaskUtil.change(taskNodeList, taskNodeEntity, resurgence, FlowNature.ProcessCompletion, flowModel);
            taskNodeEntity.setCompletion(FlowNature.ProcessCompletion);
            flowTaskNodeService.update(taskNodeEntity);
        }
        Set<String> rejectNodeList = taskNodeList.stream().map(FlowTaskNodeEntity::getId).collect(Collectors.toSet());
        flowTaskOperatorService.updateReject(flowModel.getTaskId(), rejectNodeList);
        flowOperatorUserService.updateReject(flowModel.getTaskId(), rejectNodeList);
        Set<String> recordNodeList = new HashSet<>();
        List<String> delTaskNode = ImmutableList.of(flowModel.getTaskNodeId());
        flowTaskUtil.upAll(recordNodeList, delTaskNode, taskNodeList);
        flowTaskOperatorRecordService.updateStatus(recordNodeList, flowTask.getId());
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        List<ChildNodeList> nextOperatorList = new ArrayList<>();
        nextOperatorList.add(childNode);
        flowModel.setFormData(JsonUtil.stringToMap(flowTask.getFlowFormContentJson()));
        flowModel.setHandleOpinion(flowModel.getHandleOpinion());
        //查询审批人
        TaskOperator taskOperator = new TaskOperator();
        taskOperator.setChildNode(childNode);
        taskOperator.setTaskNodeList(taskNodeList);
        taskOperator.setTaskEntity(flowTask);
        taskOperator.setFlowModel(flowModel);
        taskOperator.setExtraRule(true);
        //插入新的候选人
        List<String> userIdAll = flowTaskUtil.userListAll(taskOperator);
        Map<String, List<String>> candidateErrorList = flowModel.getErrorRuleUserList();
        for (String key : candidateErrorList.keySet()) {
            userIdAll.addAll(candidateErrorList.get(key));
        }
        List<UserEntity> userList = serviceUtil.getUserName(userIdAll, true);
        if (userList.size() == 0) {
            List<FlowErrorModel> errorList = new ArrayList() {{
                FlowErrorModel errorModel = new FlowErrorModel();
                errorModel.setNodeId(nodeId);
                errorModel.setNodeName(title);
                add(errorModel);
            }};
            throw new WorkFlowException(200, JsonUtil.getObjectToString(errorList));
        }
        for (int i = 0; i < userList.size(); i++) {
            UserEntity userEntity = userList.get(i);
            TaskOperatoUser taskOperatoUser = new TaskOperatoUser();
            taskOperatoUser.setDate(new Date());
            taskOperatoUser.setChildNode(childNode);
            taskOperatoUser.setAutomation("");
            taskOperatoUser.setId(FlowNature.ParentId);
            taskOperatoUser.setHandLeId(userEntity.getId());
            taskOperatoUser.setSortCode(i + 1);
            flowTaskUtil.operatorUser(operatorList, taskOperatoUser);
        }
        Map<String, List<FlowTaskOperatorEntity>> operatorMap = operatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId));
        for (ChildNodeList childNodeList : nextOperatorList) {
            boolean counterSign = FlowNature.ImproperApprover.equals(childNodeList.getProperties().getCounterSign());
            if (counterSign && StringUtil.isEmpty(flowModel.getFreeApproverUserId())) {
                List<FlowTaskOperatorEntity> listAll = operatorMap.get(childNodeList.getTaskNodeId()) != null ? operatorMap.get(childNodeList.getTaskNodeId()) : new ArrayList<>();
                flowOperatorUserService.create(JsonUtil.getJsonToList(listAll, FlowOperatorUserEntity.class));
            }
        }
        //过滤依次审批人
        flowTaskUtil.improperApproverUser(operatorList, taskNodeList, childNode, null);
        flowTaskOperatorService.create(operatorList);
        //修改节点的选择分支数据
        recordNodeList.removeAll(delTaskNode);
        flowTaskNodeService.updateTaskNodeCandidates(new ArrayList<>(recordNodeList), "");
        //自动审批
        FlowApproveModel approveModel = FlowApproveModel.builder().operatorList(operatorList).taskNodeList(taskNodeList).flowTask(flowTask).flowModel(flowModel).build();
        flowTaskUtil.approve(approveModel);
        //审批数据赋值
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(flowTask.getId());
        operator.setHandleId(userInfo.getUserId());
        operator.setNodeCode(taskNodeEntity.getNodeCode());
        operator.setNodeName(taskNodeEntity.getNodeName());
        operator.setTaskNodeId(taskNodeEntity.getId());
        FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(resurgence ? FlowRecordEnum.resurrection.getCode() : FlowRecordEnum.change.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).operatorId(operator.getHandleId()).build();
        flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //发送消息
        FlowMsgModel flowMsgModel = new FlowMsgModel();
        flowMsgModel.setNodeList(taskNodeList);
        flowMsgModel.setOperatorList(operatorList);
        flowMsgModel.setData(flowModel.getFormData());
        flowMsgModel.setTaskNodeEntity(null);
        flowMsgModel.setTaskEntity(flowTask);
        flowMsgModel.setFlowModel(flowModel);
        flowMsgModel.setFlowTemplateAllModel(templateAllModel);
        flowMsgUtil.message(flowMsgModel);
        //超时
        insTimeOutRedis(flowModel, operatorList, userInfo, flowTask, taskNodeList);
    }

    @Override
    public FlowRejectVO rejectList(String id, boolean batch) throws WorkFlowException {
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(operator.getTaskId()).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        TaskNodeListModel taskNodeListModel = TaskNodeListModel.builder().id(operator.getTaskId()).state(FlowNodeEnum.Process.getCode()).build();
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeService.getList(taskNodeListModel,
                FlowTaskNodeEntity::getId, FlowTaskNodeEntity::getNodePropertyJson,
                FlowTaskNodeEntity::getTaskId, FlowTaskNodeEntity::getNodeType,
                FlowTaskNodeEntity::getCompletion, FlowTaskNodeEntity::getCandidates,
                FlowTaskNodeEntity::getNodeCode, FlowTaskNodeEntity::getNodeNext,
                FlowTaskNodeEntity::getNodeName
        );
        //排除分支没有走过的节点
        FlowTaskNodeEntity nodeEntity = taskNodeList.stream().filter(t -> t.getId().equals(operator.getTaskNodeId())).findFirst().orElse(null);
        flowTaskUtil.nodeList(taskNodeList);
        ChildNodeList childNodeList = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), ChildNodeList.class);
        String rejectStep = childNodeList.getProperties().getRejectStep();
        List<FlowTaskNodeEntity> rejectNode = taskNodeList.stream().filter(t -> t.getNodeCode().equals(rejectStep)).collect(Collectors.toList());
        boolean up = FlowNature.UP.equals(rejectStep);
        boolean reject = FlowNature.Reject.equals(rejectStep);
        boolean start = FlowNature.START.equals(rejectStep);
        List<String> nodetype = ImmutableList.of(FlowNature.NodeSubFlow);
        if (rejectNode.size() == 0) {
            List<FlowTaskNodeEntity> taskNodeEntity = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).collect(Collectors.toList());
            if (up) {
                List<FlowTaskNodeEntity> collect = taskNodeList.stream().filter(t -> t.getNodeNext().contains(nodeEntity.getNodeCode())).collect(Collectors.toList());
                rejectNode.addAll(collect);
                List<FlowTaskNodeEntity> subFlowList = collect.stream().filter(t -> nodetype.contains(t.getNodeType())).collect(Collectors.toList());
                if (subFlowList.size() > 0) {
                    throw new WorkFlowException("退回节点包含子流程，退回失败");
                }
            } else if (reject) {
                Set<FlowTaskNodeEntity> rejectNodeList = new HashSet<>();
                List<String> nodeIdList = ImmutableList.of(operator.getTaskNodeId());
                flowTaskUtil.upNodeList(taskNodeList, nodeIdList, rejectNodeList, null);
                rejectNode.addAll(rejectNodeList.stream().filter(t -> !nodetype.contains(t.getNodeType())).collect(Collectors.toSet()));
            } else if (start) {
                rejectNode.addAll(taskNodeEntity);
            }
        }
        if (rejectNode.size() == 0) {
            throw new WorkFlowException("当前节点未审批，不能退回");
        }
        Set<FlowTaskNodeEntity> rejectNodeAll = new HashSet<>();
        for (int i = 0; i < rejectNode.size(); i++) {
            FlowTaskNodeEntity taskNodeEntity = rejectNode.get(i);
            boolean add = operatorList.stream().filter(t -> t.getTaskNodeId().equals(taskNodeEntity.getId())).count() > 0;
            if (add || !reject) {
                rejectNodeAll.add(taskNodeEntity);
            }
        }
        if (reject) {
            List<FlowTaskNodeEntity> startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).collect(Collectors.toList());
            rejectNodeAll.addAll(startNode);
        }
        Set<FlowTaskNodeEntity> rejectNodeList = new HashSet<>();
        if (up) {
            FlowTaskNodeEntity upNode = new FlowTaskNodeEntity();
            List<String> node = rejectNode.stream().map(FlowTaskNodeEntity::getNodeCode).collect(Collectors.toList());
            upNode.setNodeCode(String.join(",", node));
            upNode.setNodeName("上一审批");
            rejectNodeList.add(upNode);
        } else {
            rejectNodeList.addAll(rejectNodeAll);
        }
        List<TaskNodeModel> nodeList = JsonUtil.getJsonToList(rejectNodeList, TaskNodeModel.class);
        List<FlowTaskOperatorEntity> rejectOperatorList = operatorList.stream().filter(t -> t.getTaskNodeId().equals(operator.getTaskNodeId())).collect(Collectors.toList());
        rejectOperatorList.stream().forEach(t -> {
            if (t.getId().equals(id)) {
                t.setCompletion(FlowNature.RejectCompletion);
            }
        });
        FlowCountersignModel countersign = new FlowCountersignModel();
        countersign.setTaskNode(nodeEntity);
        countersign.setOperatorList(rejectOperatorList);
        boolean isCountersign = flowTaskUtil.isRejectCountersign(countersign);
        FlowRejectVO vo = new FlowRejectVO();
        vo.setList(nodeList);
        boolean appro = reject ? isCountersign ? true : false : true;
        vo.setIsLastAppro(appro);
        return vo;
    }

    @Override
    @DSTransactional
    public void suspend(String id, FlowModel flowModel, boolean isSuspend) {
        UserInfo userInfo = flowModel.getUserInfo();
        List<String> idList = new ArrayList() {{
            add(id);
        }};
        if (!isSuspend) {
            List<FlowTaskEntity> childList = flowTaskService.getChildList(id, FlowTaskEntity::getId, FlowTaskEntity::getFrestore);
            boolean suspend = childList.stream().filter(t -> ObjectUtil.isNotEmpty(t.getFrestore()) && t.getFrestore() == 1).count() == 0;
            flowModel.setSuspend(suspend);
        }
        flowTaskService.getChildList(id, flowModel.getSuspend(), idList);
        List<FlowTaskOperatorEntity> operatorList = flowTaskUtil.suspend(idList, isSuspend, id);
        List<FlowTaskEntity> orderStaList = flowTaskService.getOrderStaList(idList);
        for (FlowTaskEntity flowTask : orderStaList) {
            //恢复、挂起记录
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
            operator.setTaskId(flowTask.getId());
            operator.setNodeCode(flowTask.getThisStepId());
            operator.setNodeName(flowTask.getThisStep());
            //审批数据赋值
            FlowOperatordModel flowOperatordModel = FlowOperatordModel.builder().status(isSuspend ? FlowRecordEnum.suspend.getCode() : FlowRecordEnum.restore.getCode()).flowModel(flowModel).userId(userInfo.getUserId()).operator(operator).build();
            flowTaskUtil.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskOperatorRecordService.create(operatorRecord);
        }
        //启动、停止定时器
        workTimeoutJobUtil.suspendFuture(operatorList, isSuspend);
    }

    private void insTimeOutRedis(FlowModel flowModel, Collection<FlowTaskOperatorEntity> operatorList, UserInfo userInfo, FlowTaskEntity flowTask, List<FlowTaskNodeEntity> nodeList) {
        for (FlowTaskOperatorEntity operatorOne : operatorList) {
            FlowTaskNodeEntity taskNodeEntity = nodeList.stream().filter(t -> t.getId().equals(operatorOne.getTaskNodeId())).findFirst().orElse(null);
            boolean flag = ifInsRedis(taskNodeEntity, nodeList);
            if (flag) {
                WorkTimeoutJobModel workTimeoutJobModel = WorkTimeoutJobModel.builder().flowModel(flowModel)
                        .taskId(flowTask.getId()).taskNodeId(operatorOne.getTaskNodeId()).taskNodeOperatorId(operatorOne.getId()).operatorEntity(operatorOne)
                        .tenantId(userInfo.getTenantId()).build();
                workTimeoutJobUtil.insertRedis(workTimeoutJobModel, redisUtil);
            }
        }
    }

    /**
     * 封装超时消息
     *
     * @return
     */
    public boolean ifInsRedis(FlowTaskNodeEntity taskNodeEntity, List<FlowTaskNodeEntity> nodeList) {
        ChildNodeList childNode = JsonUtil.getJsonToBean(taskNodeEntity.getNodePropertyJson(), ChildNodeList.class);
        LimitModel timeLimitConfig = childNode.getProperties().getTimeLimitConfig();
        FlowTaskNodeEntity startNode = nodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().orElse(null);
        if (timeLimitConfig.getOn() == 0) {
            return false;
        } else if (timeLimitConfig.getOn() == 2) {
            ChildNodeList childNodeStart = JsonUtil.getJsonToBean(startNode.getNodePropertyJson(), ChildNodeList.class);
            if (childNodeStart.getProperties().getTimeLimitConfig().getOn() == 0) {
                return false;
            }
        }
        return true;
    }

}
