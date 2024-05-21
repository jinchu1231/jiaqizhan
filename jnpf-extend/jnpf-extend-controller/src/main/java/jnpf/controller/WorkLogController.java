package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.entity.WorkLogEntity;
import jnpf.exception.DataException;
import jnpf.model.worklog.WorkLogCrForm;
import jnpf.model.worklog.WorkLogInfoVO;
import jnpf.model.worklog.WorkLogListVO;
import jnpf.model.worklog.WorkLogUpForm;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.WorkLogService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
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
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * 工作日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "app工作日志", description = "WorkLog")
@RestController
@RequestMapping("/api/extend/WorkLog")
public class WorkLogController extends SuperController<WorkLogService, WorkLogEntity> {

    @Autowired
    private WorkLogService workLogService;
    @Autowired
    private UserService usersService;

    /**
     * 列表(我发出的)
     *
     * @param pageModel 分页模型
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping("/Send")
    @SaCheckPermission("reportinglog")
    public ActionResult<PageListVO<WorkLogListVO>> getSendList(Pagination pageModel) {
        List<WorkLogEntity> data = workLogService.getSendList(pageModel);
        List<WorkLogListVO> list = JsonUtil.getJsonToList(data, WorkLogListVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pageModel, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 列表(我收到的)
     *
     * @param pageModel 分页模型
     * @return
     */
    @GetMapping("/Receive")
    @SaCheckPermission("reportinglog")
    public ActionResult<PageListVO<WorkLogListVO>> getReceiveList(Pagination pageModel) {
        List<WorkLogEntity> data = workLogService.getReceiveList(pageModel);
        List<WorkLogListVO> list = JsonUtil.getJsonToList(data, WorkLogListVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pageModel, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("reportinglog")
    public ActionResult<WorkLogInfoVO> info(@PathVariable("id") String id) throws DataException {
        WorkLogEntity entity = workLogService.getInfo(id);
        StringJoiner userName = new StringJoiner(",");
        StringJoiner userIds = new StringJoiner(",");
        List<String> userId = Arrays.asList(entity.getToUserId().split(","));
        List<UserEntity> userList = usersService.getUserName(userId);
        for (UserEntity user : userList) {
            userIds.add(user.getId());
            userName.add(user.getRealName() + "/" + user.getAccount());
        }
        entity.setToUserId(userName.toString());
        WorkLogInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, WorkLogInfoVO.class);
        vo.setUserIds(userIds.toString());
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param workLogCrForm 日志模型
     * @return
     */
    @Operation(summary = "新建")
    @PostMapping
    @Parameters({
            @Parameter(name = "workLogCrForm", description = "日志模型",required = true),
    })
    @SaCheckPermission("reportinglog")
    public ActionResult create(@RequestBody @Valid WorkLogCrForm workLogCrForm) {
        WorkLogEntity entity = JsonUtil.getJsonToBean(workLogCrForm, WorkLogEntity.class);
        workLogService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 更新
     *
     * @param id            主键
     * @param workLogUpForm 日志模型
     * @return
     */
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "workLogUpForm", description = "日志模型",required = true),
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("reportinglog")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid WorkLogUpForm workLogUpForm) {
        WorkLogEntity entity = JsonUtil.getJsonToBean(workLogUpForm, WorkLogEntity.class);
        boolean flag = workLogService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("reportinglog")
    public ActionResult delete(@PathVariable("id") String id) {
        WorkLogEntity entity = workLogService.getInfo(id);
        if (entity != null) {
            workLogService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }
}

