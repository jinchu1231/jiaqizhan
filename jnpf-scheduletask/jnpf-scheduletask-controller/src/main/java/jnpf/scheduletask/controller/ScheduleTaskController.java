package jnpf.scheduletask.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.scheduletask.entity.HandlerNameEntity;
import jnpf.scheduletask.entity.TimeTaskEntity;
import jnpf.scheduletask.model.TaskCrForm;
import jnpf.scheduletask.model.TaskInfoVO;
import jnpf.scheduletask.model.TaskMethodsVO;
import jnpf.scheduletask.model.TaskPage;
import jnpf.scheduletask.model.TaskUpForm;
import jnpf.scheduletask.model.UpdateTaskModel;
import jnpf.scheduletask.rest.RestScheduleTaskUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.UserProvider;
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
import java.util.List;

/**
 * 任务调度控制器
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/3/23 9:49
 */
@Tag(name = "任务调度", description = "TimeTask")
@RestController
@RequestMapping("/api/scheduletask")
public class ScheduleTaskController {

    /**
     * 获取任务调度列表
     *
     * @param pagination
     * @return
     */
    @Operation(summary = "获取任务调度列表")
    @GetMapping
    public JSONObject list(Pagination pagination) {
        UserInfo userInfo = UserProvider.getUser();
//        List<TimeTaskEntity> data = RestScheduleTaskUtil.getList(pagination, userInfo);
//        List<TaskVO> list = JsonUtil.getJsonToList(data, TaskVO.class);
//        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return RestScheduleTaskUtil.getList(pagination, userInfo);
    }

    /**
     * 获取本地任务列表
     *
     * @return
     */
    @Operation(summary = "获取任务调度列表")
    @GetMapping("/TaskMethods")
    public ActionResult<List<TaskMethodsVO>> taskMethods() {
        List<TaskMethodsVO> list = new ArrayList<>(16);
        // 获取所有handlerName
        List<HandlerNameEntity> handlerNameEntities = RestScheduleTaskUtil.getHandlerList();
        for (HandlerNameEntity entity : handlerNameEntities) {
            TaskMethodsVO taskMethodsVO = new TaskMethodsVO();
            taskMethodsVO.setId(entity.getId());
            taskMethodsVO.setFullName(entity.getHandlerName());
            list.add(taskMethodsVO);
        }
        return ActionResult.success(list);
    }

    /**
     * 获取任务调度日志列表
     *
     * @param pagination
     * @param taskId     任务Id
     * @return
     */
    @Operation(summary = "获取任务调度日志列表")
    @GetMapping("/{id}/TaskLog")
    public JSONObject list(@PathVariable("id") String taskId, TaskPage pagination) {
//        // 得到任务后通过任务id去获取日志
//        XxlJobInfo xxlJobInfo = RestScheduleTaskUtil.getInfoByTaskId(taskId);
//        if (xxlJobInfo == null) {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("list", new ArrayList<>());
//            jsonObject.put("pagination", JsonUtil.getJsonToBean(pagination, PaginationVO.class));
//            return jsonObject;
//        }
//        List<XxlJobLog> list = RestScheduleTaskUtil.getLogList(xxlJobInfo.getId(), pagination);
//        List<TaskLogVO> voList = new ArrayList<>(16);
//        for (XxlJobLog xxlJobLog : list) {
//            TaskLogVO taskLogVO = new TaskLogVO();
//            taskLogVO.setId(String.valueOf(xxlJobLog.getId()));
//            taskLogVO.setRunTime(xxlJobLog.getTriggerTime().getTime());
//            taskLogVO.setDescription(xxlJobLog.getTriggerMsg());
//            taskLogVO.setRunResult(xxlJobLog.getHandleCode() == 200 ? 0 : 1);
//            voList.add(taskLogVO);
//        }
//        PaginationVO pageModel = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return RestScheduleTaskUtil.getLogList(taskId, UserProvider.getUser(), pagination);
    }

    /**
     * 获取任务调度信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取任务调度信息")
    @GetMapping("/Info/{id}")
    public ActionResult<TaskInfoVO> info(@PathVariable("id") String id) throws DataException {
        TimeTaskEntity entity = RestScheduleTaskUtil.getInfo(id, UserProvider.getUser());
        TaskInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, TaskInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建任务调度
     *
     * @param taskCrForm
     * @return
     */
    @Operation(summary = "新建任务调度")
    @PostMapping
    public ActionResult create(@RequestBody @Valid TaskCrForm taskCrForm) {
        taskCrForm.setUserInfo(UserProvider.getUser());
        JSONObject jsonObject = RestScheduleTaskUtil.create(taskCrForm);
        return JsonUtil.getJsonToBean(jsonObject, ActionResult.class);
    }

    /**
     * 修改任务调度
     *
     * @param id         主键值
     * @param taskUpForm
     * @return
     */
    @Operation(summary = "修改任务调度")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid TaskUpForm taskUpForm) {
        taskUpForm.setUserInfo(UserProvider.getUser());
        JSONObject jsonObject = RestScheduleTaskUtil.update(id, taskUpForm);
        return JsonUtil.getJsonToBean(jsonObject, ActionResult.class);
    }

    /**
     * 删除任务
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        JSONObject jsonObject = RestScheduleTaskUtil.delete(id, UserProvider.getUser());
        return JsonUtil.getJsonToBean(jsonObject, ActionResult.class);
    }

    /**
     * 停止任务调度
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "停止任务调度")
    @PutMapping("/{id}/Actions/Stop")
    public ActionResult stop(@PathVariable("id") String id) {
        UpdateTaskModel updateTaskModel = new UpdateTaskModel();
        TimeTaskEntity entity = RestScheduleTaskUtil.getInfo(id, UserProvider.getUser());
        if (entity != null) {
            entity.setEnabledMark(0);
            entity.setRunCount(entity.getRunCount());
            updateTaskModel.setEntity(entity);
            updateTaskModel.setUserInfo(UserProvider.getUser());
            RestScheduleTaskUtil.updateTask(updateTaskModel);
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.fail("操作失败，任务不存在");
    }

    /**
     * 启动任务调度
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "启动任务调度")
    @PutMapping("/{id}/Actions/Enable")
    public ActionResult enable(@PathVariable("id") String id) {
        UpdateTaskModel updateTaskModel = new UpdateTaskModel();
        TimeTaskEntity entity = RestScheduleTaskUtil.getInfo(id, UserProvider.getUser());
        if (entity != null) {
            entity.setEnabledMark(1);
            updateTaskModel.setEntity(entity);
            updateTaskModel.setUserInfo(UserProvider.getUser());
            RestScheduleTaskUtil.updateTask(updateTaskModel);
            return ActionResult.success(MsgCode.SU005.get());
        }
        return ActionResult.fail("操作失败，任务不存在");
    }


    
    @PostMapping("/schedule")
    public void schedule(@RequestBody TaskCrForm taskCrForm) {
        JSONObject jsonObject = RestScheduleTaskUtil.schedule(taskCrForm);
    }
}
