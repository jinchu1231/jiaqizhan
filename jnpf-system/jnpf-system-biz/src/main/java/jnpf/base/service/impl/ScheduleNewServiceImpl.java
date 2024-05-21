package jnpf.base.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.UserInfo;
import jnpf.base.entity.ScheduleLogEntity;
import jnpf.base.entity.ScheduleNewEntity;
import jnpf.base.entity.ScheduleNewUserEntity;
import jnpf.base.mapper.ScheduleNewMapper;
import jnpf.base.model.schedule.ScheduleDetailModel;
import jnpf.base.model.schedule.ScheduleJobModel;
import jnpf.base.model.schedule.ScheduleNewTime;
import jnpf.base.service.ScheduleLogService;
import jnpf.base.service.ScheduleNewService;
import jnpf.base.service.ScheduleNewUserService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.util.SentMessageUtil;
import jnpf.base.util.job.ScheduleJobUtil;
import jnpf.message.model.SentMessageForm;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 日程
 *
 * @author JNPF开发平台组
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 */
@Service
public class ScheduleNewServiceImpl extends SuperServiceImpl<ScheduleNewMapper, ScheduleNewEntity> implements ScheduleNewService {

    @Autowired
    private ScheduleLogService scheduleLogService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SentMessageUtil sentMessageUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ScheduleNewUserService scheduleNewUserService;
    @Autowired
    private ScheduleJobUtil scheduleJobUtil;

    @Override
    public List<ScheduleNewEntity> getList(ScheduleNewTime scheduleNewTime) {
        List<String> scheduleId = scheduleNewUserService.getList().stream().map(ScheduleNewUserEntity::getScheduleId).collect(Collectors.toList());
        if (scheduleId.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(ScheduleNewEntity::getId, scheduleId);
        queryWrapper.lambda().orderByDesc(ScheduleNewEntity::getAllDay);
        queryWrapper.lambda().orderByAsc(ScheduleNewEntity::getStartDay);
        queryWrapper.lambda().orderByAsc(ScheduleNewEntity::getEndDay);
        queryWrapper.lambda().orderByDesc(ScheduleNewEntity::getCreatorTime);
        List<ScheduleNewEntity> result = this.list(queryWrapper);
        return result;
    }

    @Override
    public List<ScheduleNewEntity> getList(String groupId, Date date) {
        QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(groupId)) {
            queryWrapper.lambda().eq(ScheduleNewEntity::getGroupId, groupId);
        }
        if (ObjectUtil.isNotEmpty(date)) {
            queryWrapper.lambda().ge(ScheduleNewEntity::getStartDay, date);
        }
        return this.list(queryWrapper);
    }

    @Override
    public List<ScheduleNewEntity> getStartDayList(String groupId, Date date) {
        QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(groupId)) {
            queryWrapper.lambda().eq(ScheduleNewEntity::getGroupId, groupId);
        }
        if (ObjectUtil.isNotEmpty(date)) {
            queryWrapper.lambda().le(ScheduleNewEntity::getStartDay, date);
        }
        queryWrapper.lambda().orderByDesc(ScheduleNewEntity::getStartDay);
        return this.list(queryWrapper);
    }


    @Override
    public List<ScheduleNewEntity> getListAll(Date date) {
        if (date == null) {
            date = new Date();
        }
        QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
        Integer seconds = 10;
        Date end = DateUtil.dateAddSeconds(date, seconds);
        Date start = DateUtil.dateAddSeconds(date, -seconds);
        queryWrapper.lambda().between(ScheduleNewEntity::getPushTime, start, end);
        return this.list(queryWrapper);
    }

    @Override
    public ScheduleNewEntity getInfo(String id) {
        QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ScheduleNewEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ScheduleNewEntity> getGroupList(ScheduleDetailModel detailModel) {
        QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
        String id = detailModel.getId();
        String groupId = detailModel.getGroupId();
        if (StringUtil.isNotEmpty(id)) {
            queryWrapper.lambda().eq(ScheduleNewEntity::getId, id);
        } else {
            queryWrapper.lambda().eq(ScheduleNewEntity::getGroupId, groupId);
        }
        queryWrapper.lambda().orderByAsc(ScheduleNewEntity::getStartDay);
        return this.list(queryWrapper);
    }

    @Override
    @DSTransactional
    public void create(ScheduleNewEntity entity, List<String> toUserIds, String groupId, String operationType, List<String> idList) {
        UserInfo userInfo = userProvider.get();
        entity.setCreatorUserId(userInfo.getUserId());
        boolean isUser = toUserIds.contains(userInfo.getUserId());
        if (!isUser) {
            toUserIds.add(userInfo.getUserId());
        }
        time(entity);
        long time = entity.getEndDay().getTime() - entity.getStartDay().getTime();
        //间隔时间
        List<Date> dataList = new ArrayList<>();
        List<ScheduleNewEntity> listAll = new ArrayList<>();
        DateUtil.getNextDate(0, String.valueOf(entity.getRepetition()), entity.getStartDay(), entity.getRepeatTime(), dataList);
        for (Date date : dataList) {
            ScheduleNewEntity scheduleEntity = JsonUtil.getJsonToBean(entity, ScheduleNewEntity.class);
            scheduleEntity.setStartDay(date);
            scheduleEntity.setEndDay(new Date(date.getTime() + time));
            if (!Objects.equals(scheduleEntity.getReminderTime(), -2)) {
                boolean isAllDay = Objects.equals(scheduleEntity.getAllDay(), 1);
                if (isAllDay) {
                    int oneMinutes = 8 * 60;
                    int twoMinutes = 9 * 60;
                    int threeMinutes = 10 * 60;
                    int day = 1440;
                    Integer reminderTime = scheduleEntity.getReminderTime();
                    switch (reminderTime) {
                        case 4:
                        case 5:
                        case 6:
                            oneMinutes = oneMinutes - day;
                            twoMinutes = twoMinutes - day;
                            threeMinutes = threeMinutes - day;
                            break;
                        case 7:
                        case 8:
                        case 9:
                            oneMinutes = oneMinutes - day * 2;
                            twoMinutes = twoMinutes - day * 2;
                            threeMinutes = threeMinutes - day * 2;
                            break;
                        case 10:
                        case 11:
                        case 12:
                            oneMinutes = oneMinutes - day * 7;
                            twoMinutes = twoMinutes - day * 7;
                            threeMinutes = threeMinutes - day * 7;
                            break;
                        default:
                            break;
                    }
                    List<Integer> oneList = new ArrayList() {{
                        add(1);
                        add(4);
                        add(7);
                        add(10);
                    }};
                    List<Integer> twoList = new ArrayList() {{
                        add(2);
                        add(5);
                        add(8);
                        add(11);
                    }};
                    List<Integer> threeList = new ArrayList() {{
                        add(3);
                        add(6);
                        add(9);
                        add(12);
                    }};
                    Integer pushTime = 0;
                    if (oneList.contains(reminderTime)) {
                        pushTime = oneMinutes;
                    } else if (twoList.contains(reminderTime)) {
                        pushTime = twoMinutes;
                    } else if (threeList.contains(reminderTime)) {
                        pushTime = threeMinutes;
                    }
                    scheduleEntity.setPushTime(DateUtil.dateAddMinutes(scheduleEntity.getStartDay(), pushTime));
                } else {
                    Integer reminderTime = scheduleEntity.getReminderTime() > 0 ? scheduleEntity.getReminderTime() : 0;
                    scheduleEntity.setPushTime(DateUtil.dateAddMinutes(scheduleEntity.getStartDay(), -reminderTime));
                }
            }
            listAll.add(scheduleEntity);
        }
        List<ScheduleJobModel> scheduleJobList = new ArrayList<>();
        String id = "";
        for (int i = 0; i < listAll.size(); i++) {
            String randomId = idList.size() > 0 && idList.size() - 1 >= i ? idList.get(i) : RandomUtil.uuId();
            if (StringUtil.isEmpty(id)) {
                id = randomId;
            }
            ScheduleNewEntity scheduleEntity = listAll.get(i);
            scheduleEntity.setId(randomId);
            scheduleEntity.setCreatorTime(new Date());
            scheduleEntity.setCreatorUserId(userInfo.getUserId());
            scheduleEntity.setGroupId(groupId);
            scheduleEntity.setEnabledMark(1);
            this.save(scheduleEntity);
            for (String toUserId : toUserIds) {
                ScheduleNewUserEntity userEntity = new ScheduleNewUserEntity();
                userEntity.setScheduleId(scheduleEntity.getId());
                userEntity.setToUserId(toUserId);
                userEntity.setEnabledMark(1);
                userEntity.setType(!isUser && userInfo.getUserId().equals(toUserId) ? 1 : 2);
                scheduleNewUserService.create(userEntity);
            }
            boolean isTime = ObjectUtil.isNotEmpty(scheduleEntity.getPushTime()) && scheduleEntity.getPushTime().getTime() >= System.currentTimeMillis();
            ScheduleJobModel jobModel = new ScheduleJobModel();
            jobModel.setId(scheduleEntity.getId());
            jobModel.setScheduleTime(scheduleEntity.getPushTime());
            jobModel.setUserInfo(userInfo);
            jobModel.setUserList(toUserIds);
            if (isTime) {
                scheduleJobList.add(jobModel);
            }
            //操作日志
            ScheduleLogEntity logEntity = JsonUtil.getJsonToBean(entity, ScheduleLogEntity.class);
            logEntity.setOperationType(operationType);
            logEntity.setUserId(JsonUtil.getObjectToString(toUserIds));
            logEntity.setScheduleId(scheduleEntity.getId());
            scheduleLogService.create(logEntity);
        }
        if ("1".equals(operationType)) {
            ScheduleDetailModel model = new ScheduleDetailModel();
            model.setGroupId(groupId);
            model.setId(id);
            model.setType("2");
            msg(toUserIds, userInfo, model, entity, "PZXTRC001", "2");
        }
        //推送任务调度
        job(scheduleJobList);
    }

    @Override
    @DSTransactional
    public boolean update(String id, ScheduleNewEntity entity, List<String> toUserIds, String type) {
        UserInfo userInfo = userProvider.get();
        ScheduleNewEntity info = getInfo(id);
        boolean flag = false;
        String groupId = RandomUtil.uuId();
        if (info != null) {
            //删除一个还是多个
            String delGroupId = info.getGroupId();
            Date startDay = "2".equals(type) ? info.getStartDay() : null;
            List<ScheduleNewEntity> deleteList = "1".equals(type) ? new ArrayList() {{
                add(info);
            }} : getList(delGroupId, startDay);
            repeat(type, info);
            updateStartDay(delGroupId, type, startDay);
            List<String> scheduleIdList = deleteList.stream().map(ScheduleNewEntity::getId).collect(Collectors.toList());
            deleteScheduleList(scheduleIdList);
            create(entity, toUserIds, groupId, "2", scheduleIdList);
            ScheduleDetailModel detailModel = new ScheduleDetailModel();
            detailModel.setGroupId(groupId);
            List<ScheduleNewEntity> groupList = getGroupList(detailModel);
            ScheduleDetailModel model = new ScheduleDetailModel();
            model.setGroupId(groupId);
            model.setId(groupList.size() > 0 ? groupList.get(0).getId() : id);
            model.setType("2");
            entity.setSend("");
            msg(toUserIds, userInfo, model, entity, "PZXTRC002", "2");
            flag = true;
        }
        return flag;
    }


    @Override
    @DSTransactional
    public void deleteScheduleList(List<String> idList) {
        if (idList.size() > 0) {
            QueryWrapper<ScheduleNewEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(ScheduleNewEntity::getId, idList);
            this.remove(queryWrapper);
            scheduleNewUserService.deleteByScheduleId(idList);
            scheduleLogService.delete(idList, "3");
        }
    }

    @Override
    public boolean update(String id, ScheduleNewEntity entity) {
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @DSTransactional
    public void delete(ScheduleNewEntity entity, String type) {
        if (entity != null) {
            UserInfo userInfo = userProvider.get();
            String userId = userInfo.getUserId();
            String groupId = entity.getGroupId();
            String delGroupId = entity.getGroupId();
            Date startDay = "2".equals(type) ? entity.getStartDay() : null;
            List<ScheduleNewEntity> deleteList = "1".equals(type) ? new ArrayList() {{
                add(entity);
            }} : getList(delGroupId, startDay);
            List<String> scheduleIdList = deleteList.stream().map(ScheduleNewEntity::getId).collect(Collectors.toList());
            if (entity.getCreatorUserId().equals(userId)) {
                repeat(type, entity);
                List<String> toUserIds = scheduleNewUserService.getList(entity.getId(), null).stream().map(ScheduleNewUserEntity::getToUserId).collect(Collectors.toList());
                deleteScheduleList(scheduleIdList);
                ScheduleDetailModel model = new ScheduleDetailModel();
                model.setGroupId(groupId);
                model.setId(entity.getId());
                model.setType("3");
                entity.setSend("");
                msg(toUserIds, userInfo, model, entity, "PZXTRC003", "2");
            } else {
                //操作日志
                scheduleLogService.delete(scheduleIdList, "4");
                scheduleNewUserService.deleteByUserId(scheduleIdList);
//                ScheduleDetailModel model = new ScheduleDetailModel();
//                model.setGroupId(groupId);
//                model.setId(entity.getId());
//                model.setType("3");
//                List<String> toUserIds = new ArrayList(){{{{add(userId);}}}};
//                msg(toUserIds, userInfo, model, entity, "PZXTRC003" , "2");
            }
        }
    }

    @Override
    public void scheduleMessage(ScheduleJobModel scheduleModel) {
        ScheduleNewEntity info = getInfo(scheduleModel.getId());
        if (info != null) {
            List<ScheduleNewEntity> listAll = new ArrayList<>();
            listAll.add(info);
            for (ScheduleNewEntity entity : listAll) {
                UserInfo userInfo = scheduleModel.getUserInfo();
                UserEntity userEntity = userService.getInfo(entity.getCreatorUserId());
                List<String> toUserIds = scheduleNewUserService.getList(entity.getId(), null).stream().map(ScheduleNewUserEntity::getToUserId).collect(Collectors.toList());
                ScheduleDetailModel model = new ScheduleDetailModel();
                model.setGroupId(entity.getGroupId());
                model.setId(entity.getId());
                if (userEntity != null) {
                    userInfo.setUserId(userEntity.getId());
                    userInfo.setUserName(userEntity.getRealName());
                }
                model.setType("1");
                msg(toUserIds, userInfo, model, entity, "PZXTRC001", "2");
            }
        }
    }

    private void time(ScheduleNewEntity entity) {
        // 判断是否全天
        if (entity.getAllDay() != 1) {
            String startDate = DateUtil.dateToString(entity.getStartDay(), "yyyy-MM-dd") + " " + entity.getStartTime() + ":00";
            Date star = DateUtil.stringToDate(startDate);
            entity.setStartDay(star);
            if (entity.getDuration() != -1) {
                Date end = DateUtil.dateAddMinutes(entity.getStartDay(), entity.getDuration());
                entity.setEndDay(end);
            } else {
                String endDate = DateUtil.dateToString(entity.getEndDay(), "yyyy-MM-dd") + " " + entity.getEndTime() + ":00";
                Date end = DateUtil.stringToDate(endDate);
                entity.setEndDay(end);
            }
        } else {
            String startDate = DateUtil.dateToString(entity.getStartDay(), "yyyy-MM-dd") + " " + "00:00:00";
            Date star = DateUtil.stringToDate(startDate);
            entity.setStartDay(star);
            entity.setStartTime("00:00");
            String endDate = DateUtil.dateToString(entity.getEndDay(), "yyyy-MM-dd") + " " + "23:59:59";
            Date end = DateUtil.stringToDate(endDate);
            entity.setEndDay(end);
            entity.setEndTime("23:59");
        }
        Date repeatTime = entity.getRepeatTime();
        if (repeatTime != null) {
            String repeat = DateUtil.dateToString(repeatTime, "yyyy-MM-dd") + " " + "23:59:59";
            Date repeatDate = DateUtil.stringToDate(repeat);
            entity.setRepeatTime(repeatDate);
        }
    }

    private void msg(List<String> toUserIds, UserInfo userInfo, ScheduleDetailModel model, ScheduleNewEntity entity, String templateId, String type) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("@Title", entity.getTitle());
        parameterMap.put("@CreatorUserName", userInfo.getUserName()+"/"+userInfo.getUserAccount());
        parameterMap.put("@Content", StringUtil.isNotEmpty(entity.getContent()) ? entity.getContent() : "");
        parameterMap.put("@StartDate", DateUtil.daFormat(entity.getStartDay()));
        parameterMap.put("@StartTime", entity.getStartTime());
        parameterMap.put("@EndDate", DateUtil.daFormat(entity.getEndDay()));
        parameterMap.put("@EndTime", entity.getEndTime());
        parameterMap.put("Title", entity.getTitle());
        parameterMap.put("CreatorUserName", userInfo.getUserName());
        parameterMap.put("Content", StringUtil.isNotEmpty(entity.getContent()) ? entity.getContent() : "");
        parameterMap.put("StartDate", DateUtil.daFormat(entity.getStartDay()));
        parameterMap.put("StartTime", entity.getStartTime());
        parameterMap.put("EndDate", DateUtil.daFormat(entity.getEndDay()));
        parameterMap.put("EndTime", entity.getEndTime());
        SentMessageForm sentMessageForm = new SentMessageForm();
        sentMessageForm.setToUserIds(toUserIds);
        sentMessageForm.setUserInfo(userInfo);
        sentMessageForm.setParameterMap(parameterMap);
        sentMessageForm.setTitle(entity.getTitle());
        sentMessageForm.setTemplateId(StringUtil.isNotEmpty(entity.getSend()) ? entity.getSend() : templateId);
        sentMessageForm.setContent(JsonUtil.getObjectToString(model));
        Map<String, String> contentMsg = JsonUtil.entityToMaps(model);
        sentMessageForm.setContentMsg(contentMsg);
        sentMessageForm.setId(model.getId());
        sentMessageForm.setType(4);
        sentMessageUtil.sendDelegateMsg(sentMessageForm);
    }

    private void job(List<ScheduleJobModel> scheduleJobList) {
        scheduleJobUtil.insertRedis(scheduleJobList, redisUtil);
    }

    private void updateStartDay(String groupId, String type, Date startDay) {
        if ("2".equals(type)) {
            Date startData = DateUtil.stringToDate(DateUtil.dateToString(startDay, "yyyy-MM-dd") + " " + "00:00:00");
            List<ScheduleNewEntity> startDayList = getStartDayList(groupId, startData);
            if (startDayList.size() > 0) {
                Date start = startDayList.get(0).getStartDay();
                for (ScheduleNewEntity entity : startDayList) {
                    Date repeatTime = entity.getRepeatTime();
                    if (repeatTime != null) {
                        String repeat = DateUtil.dateToString(start, "yyyy-MM-dd") + " " + "23:59:59";
                        Date repeatDate = DateUtil.stringToDate(repeat);
                        entity.setRepeatTime(repeatDate);
                        update(entity.getId(), entity);
                    }
                }
            }
        }
    }

    private void repeat(String type, ScheduleNewEntity info) {
        Date repeat = info.getRepeatTime();
        String groupId = info.getGroupId();
        List<String> typeList = new ArrayList() {{
            add("2");
        }};
        if (typeList.contains(type) && ObjectUtil.isNotEmpty(repeat)) {
            List<ScheduleNewEntity> list = getList(groupId, null);
            List<ScheduleNewEntity> collect = list.stream().filter(t -> t.getStartDay().getTime() < info.getStartDay().getTime()).sorted(Comparator.comparing(ScheduleNewEntity::getStartDay).reversed()).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                ScheduleNewEntity scheduleNewEntity = collect.get(i);
                String dateString = DateUtil.getDateString(collect.get(0).getStartDay(), "yyyy-MM-dd") + " 23:59:59";
                Date repeatTime = DateUtil.stringToDate(dateString);
                scheduleNewEntity.setRepeatTime(repeatTime);
                update(scheduleNewEntity.getId(), scheduleNewEntity);
            }
        }
    }
}
