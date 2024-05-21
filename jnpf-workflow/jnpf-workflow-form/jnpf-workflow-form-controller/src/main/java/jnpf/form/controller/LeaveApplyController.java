package jnpf.form.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.constant.MsgCode;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.form.entity.LeaveApplyEntity;
import jnpf.form.model.leaveapply.LeaveApplyForm;
import jnpf.form.model.leaveapply.LeaveApplyInfoVO;
import jnpf.form.service.LeaveApplyService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请假申请
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "请假申请", description = "LeaveApply")
@RestController
@RequestMapping("/api/workflow/Form/LeaveApply")
public class LeaveApplyController extends SuperController<LeaveApplyService, LeaveApplyEntity> {

    @Autowired
    private LeaveApplyService leaveApplyService;

    /**
     * 获取请假申请信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取请假申请信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<LeaveApplyInfoVO> info(@PathVariable("id") String id) {
        LeaveApplyEntity entity = leaveApplyService.getInfo(id);
        LeaveApplyInfoVO vo = JsonUtil.getJsonToBean(entity, LeaveApplyInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建请假申请
     *
     * @param leaveApplyForm 表单对象
     * @return
     */
    @Operation(summary = "新建请假申请")
    @PostMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "leaveApplyForm", description = "请假模型", required = true),
    })
    public ActionResult create(@RequestBody LeaveApplyForm leaveApplyForm, @PathVariable("id") String id) {
        LeaveApplyEntity entity = JsonUtil.getJsonToBean(leaveApplyForm, LeaveApplyEntity.class);
        if (FlowStatusEnum.save.getMessage().equals(leaveApplyForm.getStatus())) {
            leaveApplyService.save(id, entity, leaveApplyForm);
            return ActionResult.success(MsgCode.SU002.get());
        }
        leaveApplyService.submit(id, entity, leaveApplyForm);
        return ActionResult.success(MsgCode.SU006.get());
    }

    /**
     * 修改请假申请
     *
     * @param leaveApplyForm 表单对象
     * @param id             主键
     * @return
     */
    @Operation(summary = "修改请假申请")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "leaveApplyForm", description = "请假模型", required = true),
    })
    public ActionResult update(@RequestBody LeaveApplyForm leaveApplyForm, @PathVariable("id") String id) {
        LeaveApplyEntity entity = JsonUtil.getJsonToBean(leaveApplyForm, LeaveApplyEntity.class);
        entity.setId(id);
        if (FlowStatusEnum.save.getMessage().equals(leaveApplyForm.getStatus())) {
            leaveApplyService.save(id, entity, leaveApplyForm);
            return ActionResult.success(MsgCode.SU002.get());
        }
        leaveApplyService.submit(id, entity, leaveApplyForm);
        return ActionResult.success(MsgCode.SU006.get());
    }
}
