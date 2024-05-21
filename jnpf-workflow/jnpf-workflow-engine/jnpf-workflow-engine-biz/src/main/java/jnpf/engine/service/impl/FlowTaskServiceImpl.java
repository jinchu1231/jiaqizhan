package jnpf.engine.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.ImmutableList;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.engine.entity.FlowAuthorizeEntity;
import jnpf.engine.entity.FlowCandidatesEntity;
import jnpf.engine.entity.FlowCommentEntity;
import jnpf.engine.entity.FlowDelegateEntity;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.entity.FlowOperatorUserEntity;
import jnpf.engine.entity.FlowRejectDataEntity;
import jnpf.engine.entity.FlowTaskCirculateEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.entity.FlowUserEntity;
import jnpf.engine.enums.FlowNodeEnum;
import jnpf.engine.enums.FlowTaskStatusEnum;
import jnpf.engine.mapper.FlowTaskMapper;
import jnpf.engine.model.flowbefore.FlowBatchModel;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowtask.FlowTaskListModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.model.flowtemplatejson.FlowTemplateJsonPage;
import jnpf.engine.service.FlowAuthorizeService;
import jnpf.engine.service.FlowCandidatesService;
import jnpf.engine.service.FlowCommentService;
import jnpf.engine.service.FlowDelegateService;
import jnpf.engine.service.FlowDynamicService;
import jnpf.engine.service.FlowEngineVisibleService;
import jnpf.engine.service.FlowOperatorUserService;
import jnpf.engine.service.FlowRejectDataService;
import jnpf.engine.service.FlowTaskCirculateService;
import jnpf.engine.service.FlowTaskNodeService;
import jnpf.engine.service.FlowTaskOperatorRecordService;
import jnpf.engine.service.FlowTaskOperatorService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateJsonService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.engine.service.FlowUserService;
import jnpf.engine.util.FlowNature;
import jnpf.exception.WorkFlowException;
import jnpf.model.FlowWorkListVO;
import jnpf.model.FlowWorkModel;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.user.WorkHandoverModel;
import jnpf.util.JsonUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流程任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Service
public class FlowTaskServiceImpl extends SuperServiceImpl<FlowTaskMapper, FlowTaskEntity> implements FlowTaskService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private FlowTemplateJsonService flowTemplateJsonService;
    @Autowired
    private FlowDelegateService flowDelegateService;
    @Autowired
    private FlowTaskNodeService flowTaskNodeService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private FlowOperatorUserService flowOperatorUserService;
    @Autowired
    private FlowRejectDataService flowRejectDataService;
    @Autowired
    private FlowTaskOperatorRecordService flowTaskOperatorRecordService;
    @Autowired
    private FlowTaskCirculateService flowTaskCirculateService;
    @Autowired
    private FlowCandidatesService flowCandidatesService;
    @Autowired
    private FlowCommentService flowCommentService;
    @Autowired
    private FlowAuthorizeService flowAuthorizeService;
    @Autowired
    private FlowUserService flowUserService;
    @Autowired
    private FlowEngineVisibleService flowEngineVisibleService;
    @Autowired
    private FlowDynamicService flowDynamicService;

    @Override
    public List<FlowTaskEntity> getMonitorList(PaginationFlowTask pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        UserInfo userInfo = userProvider.get();
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(FlowTaskEntity::getId);
        queryWrapper.lambda().gt(FlowTaskEntity::getStatus, FlowTaskStatusEnum.Draft.getCode());
        queryWrapper.lambda().and(t -> t.isNull(FlowTaskEntity::getSuspend).or().ne(FlowTaskEntity::getSuspend, FlowTaskStatusEnum.Draft.getCode()));
        if (!userInfo.getIsAdministrator()) {
            List<String> userList = serviceUtil.getOrganizeUserList("select");
            if (userList.size() == 0) {
                return new ArrayList<>();
            }
            queryWrapper.lambda().in(FlowTaskEntity::getCreatorUserId, userList);
        }
        //关键字（流程名称、流程编码）
        String keyWord = pagination.getKeyword();
        if (ObjectUtil.isNotEmpty(keyWord)) {
            flag = true;
            queryWrapper.lambda().and(t -> t.like(FlowTaskEntity::getEnCode, keyWord).or().like(FlowTaskEntity::getFullName, keyWord));
        }
        //日期范围（近7天、近1月、近3月、自定义）
        if (ObjectUtil.isNotEmpty(pagination.getStartTime()) && ObjectUtil.isNotEmpty(pagination.getEndTime())) {
            queryWrapper.lambda().between(FlowTaskEntity::getStartTime, new Date(pagination.getStartTime()), new Date(pagination.getEndTime()));
        }
        //流程状态
        Integer status = pagination.getStatus();
        if (ObjectUtil.isNotEmpty(status)) {
            flag = true;
            List<Integer> statusList = new ArrayList() {{
                add(status);
            }};
            if (Objects.equals(status, FlowTaskStatusEnum.Revoke.getCode())) {
                statusList.add(FlowTaskStatusEnum.Resubmit.getCode());
            }
            queryWrapper.lambda().in(FlowTaskEntity::getStatus, statusList);
        }
        //所属流程
        String templateId = pagination.getTemplateId();
        if (ObjectUtil.isNotEmpty(templateId)) {
            flag = true;
            queryWrapper.lambda().eq(FlowTaskEntity::getTemplateId, templateId);
        }
        //所属名称
        String flowId = pagination.getFlowId();
        if (ObjectUtil.isNotEmpty(flowId)) {
            flag = true;
            FlowTemplateJsonPage page = new FlowTemplateJsonPage();
            page.setTemplateId(templateId);
            page.setFlowId(flowId);
            List<String> flowList = flowTemplateJsonService.getListPage(page, false).stream().map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
            if (flowList.size() == 0) {
                return new ArrayList<>();
            }
            queryWrapper.lambda().in(FlowTaskEntity::getFlowId, flowList);
        }
        //所属分类
        String flowCategory = pagination.getFlowCategory();
        if (ObjectUtil.isNotEmpty(flowCategory)) {
            flag = true;
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowCategory, flowCategory);
        }
        //发起人员
        String creatorUserId = pagination.getCreatorUserId();
        if (ObjectUtil.isNotEmpty(creatorUserId)) {
            flag = true;
            queryWrapper.lambda().eq(FlowTaskEntity::getCreatorUserId, creatorUserId);
        }
        //紧急程度
        Integer flowUrgent = pagination.getFlowUrgent();
        if (ObjectUtil.isNotEmpty(flowUrgent)) {
            flag = true;
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowUrgent, flowUrgent);
        }
        //排序
//        if ("desc".equals(pagination.getSort().toLowerCase())) {
//            queryWrapper.lambda().orderByDesc(FlowTaskEntity::getCreatorTime);
//        } else {
        queryWrapper.lambda().orderByAsc(FlowTaskEntity::getSortCode).orderByDesc(FlowTaskEntity::getCreatorTime);
//        }
        if (flag) {
            queryWrapper.lambda().orderByDesc(FlowTaskEntity::getLastModifyTime);
        }
        Page<FlowTaskEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<FlowTaskEntity> flowTaskEntityPage = this.page(page, queryWrapper);
        if (!flowTaskEntityPage.getRecords().isEmpty()) {
            List<String> ids = flowTaskEntityPage.getRecords().stream().map(FlowTaskEntity::getId).collect(Collectors.toList());
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(FlowTaskEntity::getId, ids);
            //排序
            queryWrapper.lambda().orderByAsc(FlowTaskEntity::getSortCode).orderByDesc(FlowTaskEntity::getCreatorTime);
            if (flag) {
                queryWrapper.lambda().orderByDesc(FlowTaskEntity::getLastModifyTime);
            }
            flowTaskEntityPage.setRecords(this.list(queryWrapper));
        }
        return pagination.setData(flowTaskEntityPage.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTaskEntity> getLaunchList(PaginationFlowTask pagination) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        String userId = userProvider.get().getUserId();
        if (pagination.getDelegateType()) {
            queryWrapper.lambda().select(FlowTaskEntity::getId).eq(FlowTaskEntity::getDelegateUser, userId);
        } else {
            queryWrapper.lambda().select(FlowTaskEntity::getId).eq(FlowTaskEntity::getCreatorUserId, userId);
        }
        //关键字（流程名称、流程编码）
        String keyWord = pagination.getKeyword();
        if (ObjectUtil.isNotEmpty(keyWord)) {
            queryWrapper.lambda().and(t -> t.like(FlowTaskEntity::getEnCode, keyWord).or().like(FlowTaskEntity::getFullName, keyWord));
        }
        //日期范围（近7天、近1月、近3月、自定义）
        if (ObjectUtil.isNotEmpty(pagination.getStartTime()) && ObjectUtil.isNotEmpty(pagination.getEndTime())) {
            queryWrapper.lambda().between(FlowTaskEntity::getStartTime, new Date(pagination.getStartTime()), new Date(pagination.getEndTime()));
        }
        //所属流程
        String templateId = pagination.getTemplateId();
        if (ObjectUtil.isNotEmpty(templateId)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getTemplateId, templateId);
        }
        //所属名称
        String flowId = pagination.getFlowId();
        if (ObjectUtil.isNotEmpty(flowId)) {
            FlowTemplateJsonPage page = new FlowTemplateJsonPage();
            page.setTemplateId(templateId);
            page.setFlowId(flowId);
            List<String> flowList = flowTemplateJsonService.getListPage(page, false).stream().map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
            if (flowList.size() == 0) {
                return new ArrayList<>();
            }
            queryWrapper.lambda().in(FlowTaskEntity::getFlowId, flowList);
        }
        //流程状态
        Integer status = pagination.getStatus();
        if (ObjectUtil.isNotEmpty(status)) {
            List<Integer> statusList = new ArrayList() {{
                add(status);
            }};
            if (Objects.equals(status, FlowTaskStatusEnum.Revoke.getCode())) {
                statusList.add(FlowTaskStatusEnum.Resubmit.getCode());
            }
            queryWrapper.lambda().in(FlowTaskEntity::getStatus, statusList);
        }
        //紧急程度
        Integer flowUrgent = pagination.getFlowUrgent();
        if (ObjectUtil.isNotEmpty(flowUrgent)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowUrgent, flowUrgent);
        }
        //所属分类
        String flowCategory = pagination.getFlowCategory();
        if (ObjectUtil.isNotEmpty(flowCategory)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowCategory, flowCategory);
        }
        //排序
        queryWrapper.lambda().orderByAsc(FlowTaskEntity::getStatus).orderByDesc(FlowTaskEntity::getStartTime);
        Page<FlowTaskEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<FlowTaskEntity> flowTaskEntityPage = this.page(page, queryWrapper);
        if (!flowTaskEntityPage.getRecords().isEmpty()) {
            List<String> ids = flowTaskEntityPage.getRecords().stream().map(FlowTaskEntity::getId).collect(Collectors.toList());
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(FlowTaskEntity::getId, ids);
            //排序
            queryWrapper.lambda().orderByAsc(FlowTaskEntity::getStatus).orderByDesc(FlowTaskEntity::getStartTime);
            flowTaskEntityPage.setRecords(this.list(queryWrapper));
        }
        return pagination.setData(flowTaskEntityPage.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTaskListModel> getWaitList(PaginationFlowTask pagination) {
        String userId = StringUtil.isNotEmpty(pagination.getUserId()) ? pagination.getUserId() : userProvider.get().getUserId();
        MPJLambdaWrapper<FlowTaskEntity> wrapper = JoinWrappers.lambda(FlowTaskEntity.class)
                .selectAll(FlowTaskEntity.class)
                .leftJoin(FlowTaskOperatorEntity.class, FlowTaskOperatorEntity::getTaskId, FlowTaskEntity::getId)
                .selectAs(FlowTaskOperatorEntity::getId, FlowTaskListModel::getId)
                .selectAs(FlowTaskOperatorEntity::getNodeName, FlowTaskListModel::getThisStep)
                .selectAs(FlowTaskOperatorEntity::getNodeName, FlowTaskListModel::getNodeName)
                .selectAs(FlowTaskOperatorEntity::getTaskNodeId, FlowTaskListModel::getThisStepId)
                .selectAs(FlowTaskOperatorEntity::getHandleId, FlowTaskListModel::getHandleId)
                .selectAs(FlowTaskOperatorEntity::getCreatorTime, FlowTaskListModel::getCreatorTime)
                .eq(FlowTaskOperatorEntity::getCompletion, FlowNature.ProcessCompletion)
                .eq(FlowTaskOperatorEntity::getState, FlowNodeEnum.Process.getCode())
                .eq(FlowTaskEntity::getStatus, FlowTaskStatusEnum.Handle.getCode());
        boolean isDelegateType = pagination.getDelegateType();
        List<String> handleId = new ArrayList<>();
        handleId.add(userId);
        Map<String, String[]> delegateListAll = new HashMap<>();
        //是否委托
        if (isDelegateType) {
            List<FlowDelegateEntity> delegateList = flowDelegateService.getUser(userId);
            for (FlowDelegateEntity entity : delegateList) {
                if (StringUtil.isNotEmpty(entity.getFlowId())) {
                    String[] flowIdAll = entity.getFlowId().split(",");
                    delegateListAll.put(entity.getUserId(), flowIdAll);
                } else {
                    handleId.add(entity.getUserId());
                }
            }
        }
        //代办人
        wrapper.and(t -> {
            t.in(FlowTaskOperatorEntity::getHandleId, handleId);
            if (isDelegateType) {
                for (String key : delegateListAll.keySet()) {
                    t.or(tw -> tw.in(FlowTaskEntity::getTemplateId, delegateListAll.get(key)).eq(FlowTaskOperatorEntity::getHandleId, key));
                }
            }
        });
        //关键字（流程名称、流程编码）
        String keyWord = pagination.getKeyword();
        if (ObjectUtil.isNotEmpty(keyWord)) {
            wrapper.and(t -> t.like(FlowTaskEntity::getEnCode, keyWord).or().like(FlowTaskEntity::getFullName, keyWord));
        }
        //日期范围（近7天、近1月、近3月、自定义）
        if (ObjectUtil.isNotEmpty(pagination.getStartTime()) && ObjectUtil.isNotEmpty(pagination.getEndTime())) {
            wrapper.between(FlowTaskEntity::getStartTime, new Date(pagination.getStartTime()), new Date(pagination.getEndTime()));
        }
        //所属流程
        String templateId = pagination.getTemplateId();
        if (ObjectUtil.isNotEmpty(templateId)) {
            wrapper.eq(FlowTaskEntity::getTemplateId, templateId);
        }
        //是否批量
        Integer isBatch = pagination.getIsBatch();
        if (ObjectUtil.isNotEmpty(isBatch)) {
            wrapper.eq(FlowTaskEntity::getIsBatch, isBatch);
        }
        //所属名称
        String flowId = pagination.getFlowId();
        if (ObjectUtil.isNotEmpty(flowId)) {
            List<String> flowList = new ArrayList<>();
            if (ObjectUtil.isEmpty(isBatch)) {
                FlowTemplateJsonPage page = new FlowTemplateJsonPage();
                page.setTemplateId(templateId);
                page.setFlowId(flowId);
                flowList.addAll(flowTemplateJsonService.getListPage(page, false).stream().map(FlowTemplateJsonEntity::getId).collect(Collectors.toList()));
                if (flowList.size() == 0) {
                    return new ArrayList<>();
                }
            } else {
                flowList.add(flowId);
            }
            wrapper.in(FlowTaskEntity::getFlowId, flowList);
        }
        //所属分类
        String category = pagination.getFlowCategory();
        if (ObjectUtil.isNotEmpty(category)) {
            wrapper.in(FlowTaskEntity::getFlowCategory, category.split(","));
        }
        //发起人员
        String creatorUserId = pagination.getCreatorUserId();
        if (ObjectUtil.isNotEmpty(creatorUserId)) {
            wrapper.eq(FlowTaskEntity::getCreatorUserId, creatorUserId);
        }
        //节点编码
        String nodeCode = pagination.getNodeCode();
        if (ObjectUtil.isNotEmpty(nodeCode)) {
            wrapper.eq(FlowTaskOperatorEntity::getNodeCode, nodeCode);
        }
        //紧急程度
        Integer flowUrgent = pagination.getFlowUrgent();
        if (ObjectUtil.isNotEmpty(flowUrgent)) {
            wrapper.eq(FlowTaskEntity::getFlowUrgent, flowUrgent);
        }
        wrapper.orderByDesc(FlowTaskOperatorEntity::getCreatorTime);
        Page<FlowTaskListModel> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        Page<FlowTaskListModel> data = this.selectJoinListPage(page, FlowTaskListModel.class, wrapper);
        for (FlowTaskListModel entity : data.getRecords()) {
            boolean isuser = entity.getHandleId().equals(userId);
            entity.setDelegateUser(!isuser ? entity.getCreatorUserId() : null);
        }
        return pagination.setData(data.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTaskListModel> getCirculateList(PaginationFlowTask pagination) {
        String userId = StringUtil.isNotEmpty(pagination.getUserId()) ? pagination.getUserId() : userProvider.get().getUserId();
        MPJLambdaWrapper<FlowTaskEntity> wrapper = JoinWrappers.lambda(FlowTaskEntity.class)
                .selectAll(FlowTaskEntity.class)
                .leftJoin(FlowTaskCirculateEntity.class, FlowTaskCirculateEntity::getTaskId, FlowTaskEntity::getId)
                .selectAs(FlowTaskCirculateEntity::getNodeName, FlowTaskListModel::getThisStep)
                .selectAs(FlowTaskCirculateEntity::getTaskNodeId, FlowTaskListModel::getThisStepId)
                .selectAs(FlowTaskCirculateEntity::getId, FlowTaskListModel::getCirculateId)
                .selectAs(FlowTaskCirculateEntity::getCreatorTime, FlowTaskListModel::getCreatorTime)
                .eq(FlowTaskCirculateEntity::getObjectId, userId);
        //关键字（流程名称、流程编码）
        String keyWord = pagination.getKeyword();
        if (ObjectUtil.isNotEmpty(keyWord)) {
            wrapper.and(t -> t.like(FlowTaskEntity::getEnCode, keyWord).or().like(FlowTaskEntity::getFullName, keyWord));
        }
        //日期范围（近7天、近1月、近3月、自定义）
        if (ObjectUtil.isNotEmpty(pagination.getStartTime()) && ObjectUtil.isNotEmpty(pagination.getEndTime())) {
            wrapper.between(FlowTaskEntity::getStartTime, new Date(pagination.getStartTime()), new Date(pagination.getEndTime()));
        }
        //所属流程
        String templateId = pagination.getTemplateId();
        if (ObjectUtil.isNotEmpty(templateId)) {
            wrapper.eq(FlowTaskEntity::getTemplateId, templateId);
        }
        //所属名称
        String flowId = pagination.getFlowId();
        if (ObjectUtil.isNotEmpty(flowId)) {
            FlowTemplateJsonPage page = new FlowTemplateJsonPage();
            page.setTemplateId(templateId);
            page.setFlowId(flowId);
            List<String> flowList = flowTemplateJsonService.getListPage(page, false).stream().map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
            if (flowList.size() == 0) {
                return new ArrayList<>();
            }
            wrapper.in(FlowTaskEntity::getFlowId, flowList);
        }
        //所属分类
        String category = pagination.getFlowCategory();
        if (ObjectUtil.isNotEmpty(category)) {
            wrapper.in(FlowTaskEntity::getFlowCategory, category.split(","));
        }
        //发起人员
        String creatorUserId = pagination.getCreatorUserId();
        if (ObjectUtil.isNotEmpty(creatorUserId)) {
            wrapper.in(FlowTaskEntity::getCreatorUserId, creatorUserId);
        }
        //紧急程度
        Integer flowUrgent = pagination.getFlowUrgent();
        if (ObjectUtil.isNotEmpty(flowUrgent)) {
            wrapper.in(FlowTaskEntity::getFlowUrgent, flowUrgent);
        }
        wrapper.orderByDesc(FlowTaskCirculateEntity::getCreatorTime);
        Page<FlowTaskListModel> page = new Page(pagination.getCurrentPage(), pagination.getPageSize());
        Page<FlowTaskListModel> data = this.selectJoinListPage(page, FlowTaskListModel.class, wrapper);
        return pagination.setData(data.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTaskListModel> getTrialList(PaginationFlowTask pagination) {
        String userId = StringUtil.isNotEmpty(pagination.getUserId()) ? pagination.getUserId() : userProvider.get().getUserId();
        Integer handleStatus[] = new Integer[]{0, 1, 10, 13};
        MPJLambdaWrapper<FlowTaskOperatorRecordEntity> recordWrapper = JoinWrappers.lambda(FlowTaskOperatorRecordEntity.class)
                .select(FlowTaskOperatorRecordEntity::getHandleId)
                .select(FlowTaskOperatorRecordEntity::getTaskNodeId)
                .select(FlowTaskOperatorRecordEntity::getTaskId)
                .selectMax(FlowTaskOperatorRecordEntity::getHandleTime)
                .in(FlowTaskOperatorRecordEntity::getHandleStatus, handleStatus)
                .eq(FlowTaskOperatorRecordEntity::getHandleId, userId)
                .isNotNull(FlowTaskOperatorRecordEntity::getTaskOperatorId)
                .groupBy(FlowTaskOperatorRecordEntity::getTaskId, FlowTaskOperatorRecordEntity::getTaskNodeId, FlowTaskOperatorRecordEntity::getHandleId);
        List<FlowTaskOperatorRecordEntity> recordList = flowTaskOperatorRecordService.selectJoinList(FlowTaskOperatorRecordEntity.class, recordWrapper);
        if (recordList.size() > 0) {
            List<String> taskId = recordList.stream().map(FlowTaskOperatorRecordEntity::getTaskId).collect(Collectors.toList());
            List<String> taskNodeId = recordList.stream().map(FlowTaskOperatorRecordEntity::getTaskNodeId).collect(Collectors.toList());
            List<Date> handleTime = recordList.stream().map(FlowTaskOperatorRecordEntity::getHandleTime).collect(Collectors.toList());
            MPJLambdaWrapper<FlowTaskEntity> wrapper = JoinWrappers.lambda(FlowTaskEntity.class)
                    .leftJoin(FlowTaskOperatorRecordEntity.class, FlowTaskOperatorRecordEntity::getTaskId, FlowTaskEntity::getId)
                    .leftJoin(FlowTaskOperatorEntity.class, FlowTaskOperatorEntity::getId, FlowTaskOperatorRecordEntity::getTaskOperatorId)
                    .selectAll(FlowTaskEntity.class)
                    .selectAs(FlowTaskOperatorRecordEntity::getId, FlowTaskListModel::getId)
                    .selectAs(FlowTaskOperatorRecordEntity::getNodeName, FlowTaskListModel::getThisStep)
                    .selectAs(FlowTaskOperatorRecordEntity::getTaskNodeId, FlowTaskListModel::getThisStepId)
                    .selectAs(FlowTaskOperatorRecordEntity::getHandleStatus, FlowTaskListModel::getStatus)
                    .selectAs(FlowTaskOperatorRecordEntity::getHandleId, FlowTaskListModel::getHandleId)
                    .selectAs(FlowTaskOperatorRecordEntity::getHandleTime, FlowTaskListModel::getCreatorTime)
                    .selectAs(FlowTaskOperatorEntity::getHandleId, FlowTaskListModel::getDelegateUser)
                    .in(FlowTaskOperatorRecordEntity::getTaskId, taskId)
                    .in(FlowTaskOperatorRecordEntity::getTaskNodeId, taskNodeId)
                    .and(t -> {
                        for (Date date : handleTime) {
                            t.or().eq(FlowTaskOperatorRecordEntity::getHandleTime, date);
                        }
                    });
            //关键字（流程名称、流程编码）
            String keyWord = pagination.getKeyword();
            if (ObjectUtil.isNotEmpty(keyWord)) {
                wrapper.and(t -> t.like(FlowTaskEntity::getEnCode, keyWord).or().like(FlowTaskEntity::getFullName, keyWord));
            }
            //日期范围（近7天、近1月、近3月、自定义）
            if (ObjectUtil.isNotEmpty(pagination.getStartTime()) && ObjectUtil.isNotEmpty(pagination.getEndTime())) {
                wrapper.between(FlowTaskEntity::getStartTime, new Date(pagination.getStartTime()), new Date(pagination.getEndTime()));
            }
            //所属流程
            String templateId = pagination.getTemplateId();
            if (ObjectUtil.isNotEmpty(templateId)) {
                wrapper.eq(FlowTaskEntity::getTemplateId, templateId);
            }
            //所属名称
            String flowId = pagination.getFlowId();
            if (ObjectUtil.isNotEmpty(flowId)) {
                FlowTemplateJsonPage page = new FlowTemplateJsonPage();
                page.setTemplateId(templateId);
                page.setFlowId(flowId);
                List<String> flowList = flowTemplateJsonService.getListPage(page, false).stream().map(FlowTemplateJsonEntity::getId).collect(Collectors.toList());
                if (flowList.size() == 0) {
                    return new ArrayList<>();
                }
                wrapper.in(FlowTaskEntity::getFlowId, flowList);
            }
            //所属分类
            String category = pagination.getFlowCategory();
            if (ObjectUtil.isNotEmpty(category)) {
                wrapper.in(FlowTaskEntity::getFlowCategory, category.split(","));
            }
            //发起人员
            String creatorUserId = pagination.getCreatorUserId();
            if (ObjectUtil.isNotEmpty(creatorUserId)) {
                wrapper.in(FlowTaskEntity::getCreatorUserId, creatorUserId);
            }
            //紧急程度
            Integer flowUrgent = pagination.getFlowUrgent();
            if (ObjectUtil.isNotEmpty(flowUrgent)) {
                wrapper.in(FlowTaskEntity::getFlowUrgent, flowUrgent);
            }
            wrapper.orderByDesc(FlowTaskOperatorRecordEntity::getHandleTime);
            Page<FlowTaskListModel> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
            Page<FlowTaskListModel> data = this.selectJoinListPage(page, FlowTaskListModel.class, wrapper);
            for (FlowTaskListModel entity : data.getRecords()) {
                boolean isuser = entity.getHandleId().equals(entity.getDelegateUser());
                entity.setDelegateUser(!isuser ? entity.getCreatorUserId() : null);
            }
            return pagination.setData(data.getRecords(), page.getTotal());
        }
        return new ArrayList<>();
    }

    @Override
    public FlowTaskEntity getInfo(String id, SFunction<FlowTaskEntity, ?>... columns) throws WorkFlowException {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskEntity::getId, id);
        queryWrapper.lambda().select(columns);
        FlowTaskEntity entity = this.getOne(queryWrapper);
        if (entity == null) {
            throw new WorkFlowException(MsgCode.WF115.get());
        }
        return entity;
    }

    @Override
    public void update(FlowTaskEntity entity) {
        this.updateById(entity);
    }

    @Override
    public void create(FlowTaskEntity entity) {
        this.save(entity);
    }

    @Override
    public void createOrUpdate(FlowTaskEntity entity) {
        this.saveOrUpdate(entity);
    }

    @Override
    public FlowTaskEntity getInfoSubmit(String id, SFunction<FlowTaskEntity, ?>... columns) {
        List<FlowTaskEntity> list = getInfosSubmit(new String[]{id}, columns);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<FlowTaskEntity> getInfosSubmit(String[] ids, SFunction<FlowTaskEntity, ?>... columns) {
        List<FlowTaskEntity> resultList = Collections.emptyList();
        if (ids == null || ids.length == 0) {
            return resultList;
        }
        LambdaQueryWrapper<FlowTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (ids.length == 1) {
            queryWrapper.select(columns).and(
                    t -> t.eq(FlowTaskEntity::getId, ids[0])
            );
            resultList = this.list(queryWrapper);
            if (resultList.isEmpty()) {
                queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(columns).and(
                        t -> t.eq(FlowTaskEntity::getProcessId, ids[0])
                );
                resultList = this.list(queryWrapper);
            }
        } else {
            queryWrapper.select(FlowTaskEntity::getId).and(t -> {
                t.in(FlowTaskEntity::getId, ids).or().in(FlowTaskEntity::getProcessId, ids);
            });
            List<String> resultIds = this.listObjs(queryWrapper, t -> t.toString());
            if (!resultIds.isEmpty()) {
                queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(columns).in(FlowTaskEntity::getId, resultIds);
                resultList = this.list(queryWrapper);
            }
        }
        return resultList;
    }

    @Override
    public void delete(FlowTaskEntity entity) throws WorkFlowException {
        if (FlowTaskStatusEnum.Suspend.getCode().equals(entity.getStatus())) {
            throw new WorkFlowException("流程处于挂起状态，不可操作");
        }
        if (!FlowNature.ParentId.equals(entity.getParentId()) && StringUtil.isNotEmpty(entity.getParentId())) {
            throw new WorkFlowException(entity.getFullName() + "不能删除");
        }
        if (!checkStatus(entity.getStatus())) {
            throw new WorkFlowException(MsgCode.WF116.get());
        } else {
            List<String> idList = ImmutableList.of(entity.getId());
            this.deleteAll(idList, true, true);
        }
    }

    @Override
    public void deleteChildAll(List<String> idAll) {
        this.deleteAll(idAll, true, true);
    }

    @Override
    public void delete(String[] ids) throws WorkFlowException {
        if (ids.length > 0) {
            List<String> idList = Arrays.asList(ids);
            List<FlowTaskEntity> flowTaskList = getOrderStaList(idList);
            List<FlowTaskEntity> del = flowTaskList.stream().filter(t -> t.getFlowType() == 1).collect(Collectors.toList());
            if (del.size() > 0) {
                throw new WorkFlowException(del.get(0).getFullName() + MsgCode.WF117.get());
            }
            List<FlowTaskEntity> child = flowTaskList.stream().filter(t -> !FlowNature.ParentId.equals(t.getParentId()) && StringUtil.isNotEmpty(t.getParentId())).collect(Collectors.toList());
            if (child.size() > 0) {
                throw new WorkFlowException(child.get(0).getFullName() + MsgCode.WF118.get());
            }
            List<FlowTaskEntity> taskStatusList = new ArrayList<>();
            for (String id : ids) {
                List<String> childAllList = getChildAllList(id);
                taskStatusList.addAll(getOrderStaList(childAllList));
            }
            List<FlowTaskEntity> taskStatus = taskStatusList.stream().filter(t -> FlowTaskStatusEnum.Suspend.getCode().equals(t.getStatus())).collect(Collectors.toList());
            if (taskStatus.size() > 0) {
                throw new WorkFlowException(taskStatus.get(0).getFullName() + "已被挂起不能删除");
            }
            UserInfo userInfo = userProvider.get();
            if (!userInfo.getIsAdministrator()) {
                List<String> organizeUserList = serviceUtil.getOrganizeUserList("delete");
                List<FlowTaskEntity> taskOrganize = taskStatusList.stream().filter(t -> !organizeUserList.contains(t.getCreatorUserId())).collect(Collectors.toList());
                if (taskOrganize.size() > 0) {
                    throw new WorkFlowException(taskOrganize.get(0).getFullName() + "没有删除权限");
                }
            }
            this.deleteAll(idList, true, true);
        }
    }

    @Override
    public List<FlowTaskEntity> getOrderStaList(List<String> id, SFunction<FlowTaskEntity, ?>... columns) {
        List<FlowTaskEntity> list = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(FlowTaskEntity::getId, id);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public List<FlowTaskEntity> getChildList(String id, SFunction<FlowTaskEntity, ?>... columns) {
        return getChildList(ImmutableList.of(id), columns);
    }

    @Override
    public List<FlowTaskEntity> getChildList(List<String> id, SFunction<FlowTaskEntity, ?>... columns) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(columns).in(FlowTaskEntity::getParentId, id);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowTaskEntity> getTemplateIdList(String tempId) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskEntity::getTemplateId, tempId);
        queryWrapper.lambda().select(FlowTaskEntity::getId, FlowTaskEntity::getFlowId);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowTaskEntity> getFlowList(String flowId) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskEntity::getFlowId, flowId);
        queryWrapper.lambda().select(FlowTaskEntity::getId);
        return list(queryWrapper);
    }

    @Override
    public List<FlowBatchModel> batchFlowSelector() {
        List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getBatchList();
        List<String> taskIdList = operatorList.stream().map(FlowTaskOperatorEntity::getTaskId).collect(Collectors.toList());
        List<FlowTaskEntity> taskList = getOrderStaList(taskIdList);
        Map<String, List<FlowTaskEntity>> flowList = taskList.stream().filter(t -> ObjectUtil.isNotEmpty(t.getIsBatch()) && t.getIsBatch() == 1).collect(Collectors.groupingBy(FlowTaskEntity::getTemplateId));
        List<FlowBatchModel> batchFlowList = new ArrayList<>();
        for (String key : flowList.keySet()) {
            List<FlowTaskEntity> flowTaskList = flowList.get(key);
            List<String> flowTask = flowTaskList.stream().map(FlowTaskEntity::getId).collect(Collectors.toList());
            List<FlowTemplateJsonEntity> templateJsonList = flowTemplateJsonService.getTemplateJsonList(flowTaskList.stream().map(FlowTaskEntity::getFlowId).collect(Collectors.toList()));
            if (flowTaskList.size() > 0) {
                String flowName = flowTaskList.stream().map(FlowTaskEntity::getFlowName).distinct().collect(Collectors.joining(","));
                String flowId = templateJsonList.stream().map(FlowTemplateJsonEntity::getTemplateId).distinct().collect(Collectors.joining(","));
                Long count = operatorList.stream().filter(t -> flowTask.contains(t.getTaskId())).count();
                FlowBatchModel batchModel = new FlowBatchModel();
                batchModel.setNum(count);
                batchModel.setId(flowId);
                batchModel.setFullName(flowName + "(" + count + ")");
                batchFlowList.add(batchModel);
            }
        }
        batchFlowList = batchFlowList.stream().sorted(Comparator.comparing(FlowBatchModel::getNum).reversed()).collect(Collectors.toList());
        return batchFlowList;
    }

    @Override
    public List<String> getChildAllList(String id) {
        List<String> idAll = new ArrayList<>();
        List<String> idList = ImmutableList.of(id);
        this.deleTaskAll(idList, idAll);
        return idAll;
    }

    @Override
    public void deleTaskAll(List<String> idList, List<String> idAll) {
        idAll.addAll(idList);
        for (String id : idList) {
            List<FlowTaskEntity> taskAll = this.getChildList(id, FlowTaskEntity::getId);
            List<String> list = taskAll.stream().map(FlowTaskEntity::getId).collect(Collectors.toList());
            this.deleTaskAll(list, idAll);
        }
    }

    @Override
    public void getChildList(String id, boolean suspend, List<String> list) {
        List<FlowTaskEntity> taskAll = this.getChildList(id, FlowTaskEntity::getId, FlowTaskEntity::getIsAsync);
        if (suspend) {
            taskAll = taskAll.stream().filter(t -> FlowNature.ChildSync.equals(t.getIsAsync())).collect(Collectors.toList());
        }
        for (FlowTaskEntity entity : taskAll) {
            list.add(entity.getId());
            this.getChildList(entity.getId(), suspend, list);
        }
    }

    @Override
    public FlowWorkListVO flowWork(String fromId) {
        FlowWorkListVO vo = new FlowWorkListVO();
        //代办
        QueryWrapper<FlowTaskOperatorEntity> operatorWrapper = new QueryWrapper<>();
        operatorWrapper.lambda().eq(FlowTaskOperatorEntity::getHandleId, fromId);
        List<FlowTaskOperatorEntity> list = flowTaskOperatorService.list(operatorWrapper);
        List<String> taskIdList = list.stream().map(FlowTaskOperatorEntity::getTaskId).collect(Collectors.toList());
        List<FlowTaskEntity> taskList = getOrderStaList(taskIdList);
        List<FlowTemplateEntity> waitTemplateList = flowTemplateService.getTemplateList(taskList.stream().map(FlowTaskEntity::getTemplateId).collect(Collectors.toList()));
        List<FlowWorkModel> waitList = new ArrayList<>();
        for (FlowTaskOperatorEntity entity : list) {
            FlowWorkModel workModel = JsonUtil.getJsonToBean(entity, FlowWorkModel.class);
            FlowTaskEntity taskEntity = taskList.stream().filter(t -> t.getId().equals(entity.getTaskId())).findFirst().orElse(null);
            if (taskEntity != null) {
                workModel.setFullName(taskEntity.getFullName());
                FlowTemplateEntity templateEntity = waitTemplateList.stream().filter(t -> t.getId().equals(taskEntity.getTemplateId())).findFirst().orElse(null);
                if (templateEntity != null) {
                    workModel.setIcon(templateEntity.getIcon());
                }
            }
            waitList.add(workModel);
        }
        vo.setWait(waitList);

        //流程
        QueryWrapper<FlowTemplateJsonEntity> queryWrapper = new QueryWrapper<>();
        List<FlowTemplateJsonEntity> templateJsonList = flowTemplateJsonService.list(queryWrapper).stream().filter(t -> t.getFlowTemplateJson().contains(fromId)).collect(Collectors.toList());
        List<String> templateIdList = templateJsonList.stream().map(FlowTemplateJsonEntity::getTemplateId).collect(Collectors.toList());
        List<FlowTemplateEntity> templateList = flowTemplateService.getTemplateList(templateIdList);
        List<FlowWorkModel> flowList = new ArrayList<>();
        for (FlowTemplateJsonEntity entity : templateJsonList) {
            FlowWorkModel workModel = JsonUtil.getJsonToBean(entity, FlowWorkModel.class);
            FlowTemplateEntity templateEntity = templateList.stream().filter(t -> t.getId().equals(entity.getTemplateId())).findFirst().orElse(null);
            if (templateEntity != null) {
                workModel.setFullName(templateEntity.getFullName() + "_" + entity.getFullName() + "(V" + entity.getVersion() + ")");
                workModel.setIcon(templateEntity.getIcon());
            }
            flowList.add(workModel);
        }
        vo.setFlow(flowList);
        return vo;
    }

    @Override
    @Transactional
    public boolean flowWork(WorkHandoverModel workHandoverModel) {
        String fromId = workHandoverModel.getFromId();
        String toId = workHandoverModel.getToId();
        List<String> waitList = workHandoverModel.getWaitList();
        //待办事宜
        if (waitList.size() > 0) {
            UpdateWrapper<FlowTaskOperatorEntity> operator = new UpdateWrapper<>();
            operator.lambda().in(FlowTaskOperatorEntity::getId, waitList);
            operator.lambda().eq(FlowTaskOperatorEntity::getHandleId, fromId);
            operator.lambda().set(FlowTaskOperatorEntity::getHandleId, toId);
            flowTaskOperatorService.update(operator);
            UpdateWrapper<FlowOperatorUserEntity> userOperator = new UpdateWrapper<>();
            userOperator.lambda().in(FlowOperatorUserEntity::getId, waitList);
            userOperator.lambda().eq(FlowOperatorUserEntity::getHandleId, fromId);
            userOperator.lambda().set(FlowOperatorUserEntity::getHandleId, toId);
            flowOperatorUserService.update(userOperator);
            UpdateWrapper<FlowCandidatesEntity> candidates = new UpdateWrapper<>();
            candidates.lambda().in(FlowCandidatesEntity::getId, waitList);
            candidates.lambda().eq(FlowCandidatesEntity::getHandleId, fromId);
            candidates.lambda().set(FlowCandidatesEntity::getHandleId, toId);
            flowCandidatesService.update(candidates);
        }
        //负责流程
        List<String> flowList = workHandoverModel.getFlowList();
        if (flowList.size() > 0) {
            UserEntity toUser = serviceUtil.getUserInfo(toId);
            String toUserName = toUser != null ? toUser.getRealName() + "/" + toUser.getAccount() : "";
            UserEntity fromUser = serviceUtil.getUserInfo(fromId);
            String fromUserName = fromUser != null ? fromUser.getRealName() + "/" + fromUser.getAccount() : "";
            //json
            List<FlowTemplateJsonEntity> templateJsonList = flowTemplateJsonService.getTemplateJsonList(flowList);
            for (FlowTemplateJsonEntity entity : templateJsonList) {
                String json = entity.getFlowTemplateJson().replaceAll(fromId, toId).replaceAll(fromUserName, toUserName);
                entity.setFlowTemplateJson(json);
                flowTemplateJsonService.update(entity.getId(), entity);
            }
            //任务
            QueryWrapper<FlowTaskEntity> taskQueryWrapper = new QueryWrapper<>();
            taskQueryWrapper.lambda().in(FlowTaskEntity::getFlowId, flowList);
            List<FlowTaskEntity> list = this.list(taskQueryWrapper);
            for (FlowTaskEntity entity : list) {
                update(entity);
                QueryWrapper<FlowTaskNodeEntity> nodeQueryWrapper = new QueryWrapper<>();
                nodeQueryWrapper.lambda().eq(FlowTaskNodeEntity::getTaskId, entity.getId());
                List<FlowTaskNodeEntity> nodeList = flowTaskNodeService.list(nodeQueryWrapper);
                for (FlowTaskNodeEntity taskNodeEntity : nodeList) {
                    taskNodeEntity.setNodePropertyJson(taskNodeEntity.getNodePropertyJson().replaceAll(fromId, toId).replaceAll(fromUserName, toUserName));
                    flowTaskNodeService.update(taskNodeEntity);
                }
            }
            //流程可见
            List<String> templateIdList = templateJsonList.stream().map(FlowTemplateJsonEntity::getTemplateId).collect(Collectors.toList());
            if (templateIdList.size() > 0) {
                UpdateWrapper<FlowEngineVisibleEntity> visibleWrapper = new UpdateWrapper<>();
                visibleWrapper.lambda().eq(FlowEngineVisibleEntity::getOperatorId, fromId);
                visibleWrapper.lambda().in(FlowEngineVisibleEntity::getFlowId, templateIdList);
                visibleWrapper.lambda().set(FlowEngineVisibleEntity::getOperatorId, toId);
                flowEngineVisibleService.update(visibleWrapper);
            }
        }
        return true;
    }

    @Override
    public ActionResult batchCreateOrUpdate(FlowModel flowModel)  {
        ActionResult result = new ActionResult();
        try {
            flowDynamicService.batchCreateOrUpdate(flowModel);
        }catch (Exception e){
            result.setCode(400);
            result.setMsg(e.getMessage());
        }
        return result;
    }

    @Override
    public FlowTemplateJsonEntity getFlowTemplateJsonEntity(String id) throws WorkFlowException {
        return flowTemplateJsonService.getInfo(id);
    }

    /**
     * 验证有效状态
     *
     * @param status 状态编码
     * @return
     */
    private boolean checkStatus(int status) {
        List<Integer> statusList = ImmutableList.of(FlowTaskStatusEnum.Draft.getCode(),
                FlowTaskStatusEnum.Reject.getCode(), FlowTaskStatusEnum.Revoke.getCode());
        if (statusList.contains(status)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 递归删除所有子节点
     *
     * @param idList
     * @param isRecord
     * @param isCirculate
     */
    private void deleteAll(List<String> idList, boolean isRecord, boolean isCirculate) {
        List<String> idAll = new ArrayList<>();
        this.deleTaskAll(idList, idAll);
        if (idAll.size() > 0) {
            QueryWrapper<FlowTaskEntity> task = new QueryWrapper<>();
            task.lambda().in(FlowTaskEntity::getId, idAll);
            this.remove(task);
            QueryWrapper<FlowCandidatesEntity> candidates = new QueryWrapper<>();
            candidates.lambda().in(FlowCandidatesEntity::getTaskId, idAll);
            flowCandidatesService.remove(candidates);
            QueryWrapper<FlowCommentEntity> comment = new QueryWrapper<>();
            comment.lambda().in(FlowCommentEntity::getTaskId, idAll);
            flowCommentService.remove(comment);
            QueryWrapper<FlowAuthorizeEntity> authorize = new QueryWrapper<>();
            authorize.lambda().in(FlowAuthorizeEntity::getTaskId, idAll);
            flowAuthorizeService.remove(authorize);
            QueryWrapper<FlowUserEntity> user = new QueryWrapper<>();
            user.lambda().in(FlowUserEntity::getTaskId, idAll);
            flowUserService.remove(user);
            QueryWrapper<FlowTaskNodeEntity> node = new QueryWrapper<>();
            node.lambda().in(FlowTaskNodeEntity::getTaskId, idAll);
            flowTaskNodeService.remove(node);
            QueryWrapper<FlowTaskOperatorEntity> operator = new QueryWrapper<>();
            operator.lambda().in(FlowTaskOperatorEntity::getTaskId, idAll);
            flowTaskOperatorService.remove(operator);
            QueryWrapper<FlowOperatorUserEntity> operatorUser = new QueryWrapper<>();
            operatorUser.lambda().in(FlowOperatorUserEntity::getTaskId, idAll);
            flowOperatorUserService.remove(operatorUser);
            QueryWrapper<FlowRejectDataEntity> rejectData = new QueryWrapper<>();
            rejectData.lambda().in(FlowRejectDataEntity::getId, idAll);
            flowRejectDataService.remove(rejectData);
            QueryWrapper<FlowTaskOperatorRecordEntity> record = new QueryWrapper<>();
            record.lambda().in(FlowTaskOperatorRecordEntity::getTaskId, idAll);
            flowTaskOperatorRecordService.remove(record);
            QueryWrapper<FlowTaskCirculateEntity> circulate = new QueryWrapper<>();
            circulate.lambda().in(FlowTaskCirculateEntity::getTaskId, idAll);
            flowTaskCirculateService.remove(circulate);
        }
    }

}
