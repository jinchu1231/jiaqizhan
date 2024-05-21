package jnpf.base.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ScheduleNewEntity;
import jnpf.base.entity.ScheduleNewUserEntity;
import jnpf.base.model.schedule.ScheduleDetailModel;
import jnpf.base.model.schedule.ScheduleNewAppListVO;
import jnpf.base.model.schedule.ScheduleNewCrForm;
import jnpf.base.model.schedule.ScheduleNewDetailInfoVO;
import jnpf.base.model.schedule.ScheduleNewInfoVO;
import jnpf.base.model.schedule.ScheduleNewListVO;
import jnpf.base.model.schedule.ScheduleNewTime;
import jnpf.base.model.schedule.ScheduleNewUpForm;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.ScheduleNewService;
import jnpf.base.service.ScheduleNewUserService;
import jnpf.base.vo.ListVO;
import jnpf.message.entity.SendMessageConfigEntity;
import jnpf.message.service.SendMessageConfigService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 日程
 *
 * @author JNPF开发平台组
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 */
@Tag(name = "日程", description = "Schedule")
@RestController
@RequestMapping("/api/system/Schedule")
public class ScheduleNewController extends SuperController<ScheduleNewService, ScheduleNewEntity> {


    @Autowired
    private UserService userService;
    @Autowired
    private SendMessageConfigService sendMessageConfigService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private ScheduleNewService scheduleNewService;
    @Autowired
    private ScheduleNewUserService scheduleNewUserService;

    /**
     * 获取日程安排列表
     *
     * @param scheduleNewTime 分页模型
     * @return
     */
    @Operation(summary = "获取日程安排列表")
    @GetMapping
    public ActionResult<ListVO<ScheduleNewListVO>> list(ScheduleNewTime scheduleNewTime) {
        List<ScheduleNewEntity> list = scheduleNewService.getList(scheduleNewTime);
        Date start = DateUtil.stringToDates(scheduleNewTime.getStartTime());
        Date end = DateUtil.stringToDates(scheduleNewTime.getEndTime());
        List<Date> dataAll = DateUtil.getAllDays(start, end);
        List<ScheduleNewEntity> result = new ArrayList<>();
        if (list.size() > 0) {
            for (Date date : dataAll) {
                for (ScheduleNewEntity entity : list) {
                    Date startDay = DateUtil.stringToDates(DateUtil.daFormat(entity.getStartDay()));
                    Date endDay = DateUtil.stringToDates(DateUtil.daFormat(entity.getEndDay()));
                    if(DateUtil.isEffectiveDate(date,startDay,endDay)){
                        result.add(entity);
                    }
                }
            }
        }
        for (ScheduleNewEntity entity : result) {
            if (entity.getAllDay() == 1) {
                entity.setEndDay(DateUtil.dateAddSeconds(entity.getEndDay(), 1));
            }
        }
        List<ScheduleNewListVO> vo = JsonUtil.getJsonToList(result, ScheduleNewListVO.class);
        ListVO listVO = new ListVO();
        listVO.setList(vo);
        return ActionResult.success(listVO);
    }

    /**
     * 获取日程安排列表
     *
     * @param scheduleNewTime 分页模型
     * @return
     */
    @Operation(summary = "获取日程安排列表")
    @GetMapping("/AppList")
    public ActionResult<ScheduleNewAppListVO> selectList(ScheduleNewTime scheduleNewTime) {
        Map<String, Object> signMap = new HashMap<>(16);
        List<ScheduleNewEntity> list = scheduleNewService.getList(scheduleNewTime);
        Date start = DateUtil.stringToDates(scheduleNewTime.getStartTime());
        Date end = DateUtil.stringToDates(scheduleNewTime.getEndTime());
        List<Date> dateList = new ArrayList() {{
            add(start);
            add(end);
        }};
        if(StringUtils.isNotEmpty(scheduleNewTime.getDateTime())){
            dateList.add(DateUtil.strToDate(scheduleNewTime.getDateTime()));
        }
        Date minDate = dateList.stream().min(Date::compareTo).get();
        Date maxDate = dateList.stream().max(Date::compareTo).get();
        List<Date> dataAll = DateUtil.getAllDays(minDate, maxDate);
        ScheduleNewAppListVO vo = new ScheduleNewAppListVO();
        String pattern = "yyyyMMdd";
        String dateTime = StringUtils.isEmpty(scheduleNewTime.getDateTime()) ? DateUtil.dateNow(pattern) : scheduleNewTime.getDateTime().replaceAll("-", "");
        List<ScheduleNewEntity> todayList = new ArrayList<>();
        for (Date date : dataAll) {
            String time = DateUtil.dateToString(date, pattern);
            List<ScheduleNewEntity> result = new ArrayList<>();
            for (ScheduleNewEntity entity : list) {
                Date startDay = DateUtil.stringToDates(DateUtil.daFormat(entity.getStartDay()));
                Date endDay = DateUtil.stringToDates(DateUtil.daFormat(entity.getEndDay()));
                if(DateUtil.isEffectiveDate(date,startDay,endDay)){
                    result.add(entity);
                }
            }
            signMap.put(time, result.size());
            if(time.equals(dateTime)){
                todayList.addAll(result);
            }
        }
        vo.setSignList(signMap);
        vo.setTodayList(JsonUtil.getJsonToList(todayList, ScheduleNewListVO.class));
        return ActionResult.success(vo);
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取日程安排信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<ScheduleNewInfoVO> info(@PathVariable("id") String id) {
        ScheduleNewEntity entity = scheduleNewService.getInfo(id);
        ScheduleNewInfoVO vo = JsonUtil.getJsonToBean(entity, ScheduleNewInfoVO.class);
        if (vo != null) {
            SendMessageConfigEntity config = sendMessageConfigService.getInfo(vo.getSend());
            vo.setSendName(config!=null?config.getFullName():"");
            List<String> toUserIds = scheduleNewUserService.getList(entity.getId(),2).stream().map(ScheduleNewUserEntity::getToUserId).collect(Collectors.toList());
            vo.setToUserIds(toUserIds);
            return ActionResult.success(vo);
        }
        return ActionResult.fail("数据不存在");
    }

    /**
     * 信息
     *
     * @param detailModel 查询模型
     * @return
     */
    @Operation(summary = "获取日程安排信息")
    @GetMapping("/detail")
    public ActionResult<ScheduleNewDetailInfoVO> detail(ScheduleDetailModel detailModel) {
        List<ScheduleNewEntity> groupList = scheduleNewService.getGroupList(detailModel);
        ScheduleNewEntity entity = groupList.size() > 0 ? groupList.get(0) : null;
        boolean isVO = entity != null;
        if (isVO) {
            ScheduleNewDetailInfoVO vo = JsonUtil.getJsonToBean(entity, ScheduleNewDetailInfoVO.class);
            DictionaryDataEntity info = dictionaryDataService.getInfo(entity.getCategory());
            vo.setCategory(info != null ? info.getFullName() : "");
            vo.setUrgent("1".equals(vo.getUrgent()) ? "普通" : "2".equals(vo.getUrgent()) ? "重要" : "紧急");
            UserEntity infoById = userService.getInfo(vo.getCreatorUserId());
            vo.setCreatorUserId(infoById != null ? infoById.getRealName() + "/" + infoById.getAccount() : "");
            List<String> toUserIds = scheduleNewUserService.getList(entity.getId(),2).stream().map(ScheduleNewUserEntity::getToUserId).collect(Collectors.toList());
            List<UserEntity> userName = userService.getUserName(toUserIds);
            StringJoiner joiner = new StringJoiner(",");
            for (UserEntity userEntity : userName) {
                joiner.add(userEntity.getRealName() + "/" + userEntity.getAccount());
            }
            vo.setToUserIds(joiner.toString());
            return ActionResult.success(vo);
        }
        return ActionResult.fail("该日程已被删除");
    }

    /**
     * 新建
     *
     * @param scheduleCrForm 日程模型
     * @return
     */
    @Operation(summary = "新建日程安排")
    @PostMapping
    @Parameters({
            @Parameter(name = "scheduleCrForm", description = "日程模型",required = true),
    })
    public ActionResult create(@RequestBody @Valid ScheduleNewCrForm scheduleCrForm) {
        ScheduleNewEntity entity = JsonUtil.getJsonToBean(scheduleCrForm, ScheduleNewEntity.class);
        scheduleNewService.create(entity, scheduleCrForm.getToUserIds(), RandomUtil.uuId(),"1",new ArrayList<>());
        return ActionResult.success("新建成功");
    }

    /**
     * 更新
     *
     * @param id             主键
     * @param scheduleUpForm 日程模型
     * @param type           1.此日程 2.此日程及后续 3.所有日程
     * @return
     */
    @Operation(summary = "更新日程安排")
    @PutMapping("/{id}/{type}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "scheduleUpForm", description = "日程模型", required = true),
            @Parameter(name = "type", description = "类型", required = true),
    })
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ScheduleNewUpForm scheduleUpForm, @PathVariable("type") String type) {
        if("1".equals(type)){
            scheduleUpForm.setRepeatTime(null);
            scheduleUpForm.setRepetition(1);
        }
        ScheduleNewEntity entity = JsonUtil.getJsonToBean(scheduleUpForm, ScheduleNewEntity.class);
        boolean flag = scheduleNewService.update(id, entity, scheduleUpForm.getToUserIds(), type);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param id   主键
     * @param type           1.此日程 2.此日程及后续 3.所有日程
     * @return
     */
    @Operation(summary = "删除日程安排")
    @DeleteMapping("/{id}/{type}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "type", description = "类型", required = true),
    })
    public ActionResult delete(@PathVariable("id") String id, @PathVariable("type") String type) {
        ScheduleNewEntity entity = scheduleNewService.getInfo(id);
        if (entity != null) {
            scheduleNewService.delete(entity, type);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

}
