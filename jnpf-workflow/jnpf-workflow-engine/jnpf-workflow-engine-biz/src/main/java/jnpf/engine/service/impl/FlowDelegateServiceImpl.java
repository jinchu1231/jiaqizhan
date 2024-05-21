package jnpf.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.ImmutableList;
import jnpf.base.UserInfo;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.vo.ListVO;
import jnpf.constant.PermissionConst;
import jnpf.engine.entity.FlowDelegateEntity;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.mapper.FlowDelegateMapper;
import jnpf.engine.model.flowcandidate.FlowCandidateUserModel;
import jnpf.engine.model.flowdelegate.FlowDelegateCrForm;
import jnpf.engine.model.flowdelegate.FlowDelegateModel;
import jnpf.engine.model.flowdelegate.FlowDelegatePagination;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.engine.model.flowtemplate.FlowPageListVO;
import jnpf.engine.service.FlowDelegateService;
import jnpf.engine.service.FlowEngineVisibleService;
import jnpf.engine.service.FlowTemplateJsonService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.engine.util.FlowMsgUtil;
import jnpf.engine.util.FlowNature;
import jnpf.exception.WorkFlowException;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 流程委托
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class FlowDelegateServiceImpl extends SuperServiceImpl<FlowDelegateMapper, FlowDelegateEntity> implements FlowDelegateService {

    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private FlowMsgUtil flowMsgUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private FlowTemplateJsonService flowTemplateJsonService;
    @Autowired
    private FlowEngineVisibleService flowEngineVisibleService;

    @Override
    public List<FlowDelegateEntity> getList(FlowDelegatePagination pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        String userId = userProvider.get().getUserId();
        QueryWrapper<FlowDelegateEntity> queryWrapper = new QueryWrapper<>();
        if ("2".equals(pagination.getMyOrDelagateToMe())) {//MyOrDelagateToMe=2查询委托给我的列表
            queryWrapper.lambda().eq(FlowDelegateEntity::getToUserId, userId);
        } else {
//            if (!userProvider.get().getIsAdministrator()) {//非超管
//                List<UserByRoleVO> list = serviceUtil.getListByAuthorize("0");
//                if (CollectionUtils.isNotEmpty(list)) {//是分管
//                    List<String> userIds = new ArrayList<>();
//                    for (UserByRoleVO item : list) {
//                        List<UserByRoleVO> listByAuthorize = serviceUtil.getListByAuthorize(item.getId());
//                        userIds.addAll(listByAuthorize.stream().map(UserByRoleVO::getId).collect(Collectors.toList()));
//                    }
//                    queryWrapper.lambda().and(
//                                    t -> t.eq(FlowDelegateEntity::getUserId, userId))
//                            .or().in(FlowDelegateEntity::getUserId, userIds);
//                } else {
//                    queryWrapper.lambda().eq(FlowDelegateEntity::getUserId, userId);
//                }
//            }
            queryWrapper.lambda().eq(FlowDelegateEntity::getUserId, userId);
        }

        if (!StringUtils.isEmpty(pagination.getKeyword())) {
            flag = true;
            if ("1".equals(pagination.getMyOrDelagateToMe())) {
                queryWrapper.lambda().and(
                        t -> t.like(FlowDelegateEntity::getFlowName, pagination.getKeyword())
                                .or().like(FlowDelegateEntity::getToUserName, pagination.getKeyword())
                );
            } else {
                queryWrapper.lambda().and(
                        t -> t.like(FlowDelegateEntity::getFlowName, pagination.getKeyword())
                                .or().like(FlowDelegateEntity::getUserName, pagination.getKeyword())
                );
            }
        }
        //排序
        queryWrapper.lambda().orderByAsc(FlowDelegateEntity::getSortCode).orderByDesc(FlowDelegateEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(FlowDelegateEntity::getLastModifyTime);
        }
        Page page = new Page(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<FlowDelegateEntity> flowDelegateEntityPage = this.page(page, queryWrapper);
        return pagination.setData(flowDelegateEntityPage.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowDelegateEntity> getList() {
        String userId = userProvider.get().getUserId();
        QueryWrapper<FlowDelegateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(
                t -> t.eq(FlowDelegateEntity::getCreatorUserId, userId)
                        .or().eq(FlowDelegateEntity::getUserId, userId));
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public FlowDelegateEntity getInfo(String id) {
        QueryWrapper<FlowDelegateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowDelegateEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void delete(FlowDelegateEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public void create(FlowDelegateEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(RandomUtil.uuId());
        entity.setSortCode(RandomUtil.parses());
        entity.setCreatorUserId(userInfo.getUserId());
        FlowDelegateModel delegate = new FlowDelegateModel();
        delegate.setToUserIds(ImmutableList.of(entity.getToUserId()));
        delegate.setType(entity.getType());
        delegate.setUserInfo(userInfo);
        flowMsgUtil.delegateMsg(delegate);
        this.save(entity);
    }

    @Override
    public List<FlowDelegateEntity> getUser(String touserId) {
        return getUser(null, null, touserId);
    }

    @Override
    public List<FlowDelegateEntity> getUser(String userId, String flowId, String touserId) {
        Date thisTime = DateUtil.getNowDate();
        QueryWrapper<FlowDelegateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowDelegateEntity::getType, 1);
        queryWrapper.lambda().le(FlowDelegateEntity::getStartTime, thisTime).ge(FlowDelegateEntity::getEndTime, thisTime);
        if (StringUtil.isNotEmpty(userId)) {
            queryWrapper.lambda().eq(FlowDelegateEntity::getUserId, userId);
        }
        if (StringUtil.isNotEmpty(touserId)) {
            queryWrapper.lambda().eq(FlowDelegateEntity::getToUserId, touserId);
        }
        List<FlowDelegateEntity> list = this.list(queryWrapper);
        List<FlowDelegateEntity> listRes = new ArrayList<>();
        if (StringUtil.isNotEmpty(flowId)) {
            for (FlowDelegateEntity item : list) {
                if (StringUtil.isNotEmpty(item.getFlowId())) {
                    String[] split = item.getFlowId().split(",");
                    if (Arrays.asList(split).contains(flowId)) {
                        listRes.add(item);
                    }
                } else {//为空是全部流程
                    listRes.add(item);
                }
            }
        } else {
            listRes = list;
        }
        return listRes;
    }

    @Override
    public boolean update(String id, FlowDelegateEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userInfo.getUserId());
        FlowDelegateModel delegate = new FlowDelegateModel();
        delegate.setToUserIds(ImmutableList.of(entity.getToUserId()));
        delegate.setType(entity.getType());
        delegate.setUserInfo(userInfo);
        flowMsgUtil.delegateMsg(delegate);
        return this.updateById(entity);
    }

    @Override
    public boolean updateStop(String id, FlowDelegateEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userInfo.getUserId());
        FlowDelegateModel delegate = new FlowDelegateModel();
        delegate.setToUserIds(ImmutableList.of(entity.getToUserId()));
        delegate.setType(FlowNature.EndMsg);
        delegate.setUserInfo(userInfo);
        flowMsgUtil.delegateMsg(delegate);
        return this.updateById(entity);
    }

    @Override
    public List<FlowPageListVO> getflow(FlowPagination pagination) {
        List<FlowDelegateEntity> list = getLaunchDelagateList();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
//        List<String> listFlow = new ArrayList<>();
//        for (FlowDelegateEntity item : list) {
//            if (StringUtil.isNotEmpty(item.getFlowId())) {
//                listFlow.addAll(
//                        flowTemplateService.getListByFlowIds(pagination, Arrays.asList(item.getFlowId().split(",")), false, false, item.getUserId())
//                                .stream().map(FlowTemplateEntity::getId).collect(Collectors.toList()));
//            } else {
//                listFlow.addAll(flowTemplateService.getListByFlowIds(pagination, null, true, false, item.getUserId())
//                        .stream().map(FlowTemplateEntity::getId).collect(Collectors.toList()));
//            }
//        }
//        List<FlowTemplateEntity> listReslut = flowTemplateService.getListByFlowIds(pagination, listFlow, false, false, null);
        List<String> templateIdList = new ArrayList<>();
        int num = 0;
        for (FlowDelegateEntity item : list) {
            if (StringUtil.isNotEmpty(item.getFlowId())) {
                templateIdList.addAll(Arrays.asList(item.getFlowId().split(",")));
            }
            num += StringUtil.isEmpty(item.getFlowId()) ? 1 : 0;
        }
        if (num == 0) {
            pagination.setTemplateIdList(templateIdList);
        }
        List<FlowTemplateEntity> dataList = flowTemplateService.getListAll(pagination, true);
        List<FlowPageListVO> listVO = JsonUtil.getJsonToList(dataList, FlowPageListVO.class);
        return listVO;
    }

    @Override
    public ListVO<FlowCandidateUserModel> getUserListByFlowId(String flowId) throws WorkFlowException {
        FlowTemplateJsonEntity info = flowTemplateJsonService.getInfo(flowId);
        String templateId = info.getTemplateId();
        List<FlowDelegateEntity> list = getLaunchDelagateList();
        Set<UserEntity> userName = new HashSet<>();
        for (FlowDelegateEntity item : list) {
            //用户可见列表
            UserEntity userInfo = serviceUtil.getUserInfo(item.getUserId());
            boolean visibleType = "1".equals(userInfo.getIsAdministrator());
            List<String> listVisible = null;
            if (!visibleType) {
                List<String> id = flowEngineVisibleService.getVisibleFlowList(userInfo.getId()).stream().map(FlowEngineVisibleEntity::getFlowId).collect(Collectors.toList());
                //可见列表
                listVisible = flowTemplateJsonService.getListAll(id).stream().map(FlowTemplateJsonEntity::getTemplateId).collect(Collectors.toList());
            }
            //判断委托人是否有发起权限
            if (StringUtil.isNotEmpty(item.getFlowId())) {
                List<String> strings = Arrays.asList(item.getFlowId().split(","));
                if (strings.contains(templateId) && listVisible.contains(templateId)) {
                    userName.add(userInfo);
                }
            } else {
                if (listVisible.contains(templateId)) {
                    userName.add(userInfo);
                }
            }
        }
        List<String> userIdAll = userName.stream().map(UserEntity::getId).collect(Collectors.toList());
        Map<String, List<UserRelationEntity>> userMap = serviceUtil.getListByUserIdAll(userIdAll).stream().filter(t -> PermissionConst.ORGANIZE.equals(t.getObjectType())).collect(Collectors.groupingBy(UserRelationEntity::getUserId));
        List<FlowCandidateUserModel> jsonToList = new ArrayList<>();
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
            FlowCandidateUserModel vo = JsonUtil.getJsonToBean(entity, FlowCandidateUserModel.class);
            vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
            vo.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
            vo.setOrganize(joiner.toString());
            jsonToList.add(vo);
        }
        if (jsonToList.size() == 0) {
            throw new WorkFlowException("您没有发起该流程的权限");
        }
        ListVO<FlowCandidateUserModel> vo = new ListVO<>();
        vo.setList(jsonToList);
        return vo;
    }

    /**
     * 获取当前用户所有发起委托列表
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2022/10/17
     */
    private List<FlowDelegateEntity> getLaunchDelagateList() {
        String userId = userProvider.get().getUserId();
        Date thisTime = DateUtil.getNowDate();
        QueryWrapper<FlowDelegateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowDelegateEntity::getToUserId, userId);
        queryWrapper.lambda().le(FlowDelegateEntity::getStartTime, thisTime).ge(FlowDelegateEntity::getEndTime, thisTime);
        queryWrapper.lambda().like(FlowDelegateEntity::getType, 0);
        List<FlowDelegateEntity> list = this.baseMapper.selectList(queryWrapper);//全部发起委托
        return list;
    }

    @Override
    public List<FlowDelegateEntity> selectSameParamAboutDelaget(FlowDelegateCrForm model) {
        QueryWrapper<FlowDelegateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowDelegateEntity::getUserId, model.getUserId());
        queryWrapper.lambda().eq(FlowDelegateEntity::getToUserId, model.getToUserId());
        queryWrapper.lambda().eq(FlowDelegateEntity::getType, model.getType());
        queryWrapper.lambda().gt(FlowDelegateEntity::getEndTime, new Date());
        List<FlowDelegateEntity> list = this.list(queryWrapper);
        return list;
    }
}
