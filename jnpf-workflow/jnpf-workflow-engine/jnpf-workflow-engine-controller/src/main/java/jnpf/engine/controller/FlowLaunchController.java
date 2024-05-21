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
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowlaunch.FlowLaunchListVO;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.service.FlowTaskNewService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.util.FlowNature;
import jnpf.exception.WorkFlowException;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 流程发起
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "流程发起", description = "FlowLaunch")
@RestController
@RequestMapping("/api/workflow/Engine/FlowLaunch")
public class FlowLaunchController {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FlowTaskNewService flowTaskNewService;

    /**
     * 获取流程发起列表
     *
     * @param paginationFlowTask 分页模型
     * @return
     */
    @Operation(summary = "获取流程发起列表(带分页)")
    @GetMapping
    public ActionResult<PageListVO<FlowLaunchListVO>> list(PaginationFlowTask paginationFlowTask) {
        List<FlowTaskEntity> list = flowTaskService.getLaunchList(paginationFlowTask);
        List<FlowLaunchListVO> listVO = JsonUtil.getJsonToList(list, FlowLaunchListVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationFlowTask, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 删除流程发起
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除流程发起")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult delete(@PathVariable("id") String id) throws WorkFlowException {
        FlowTaskEntity entity = flowTaskService.getInfo(id, FlowTaskEntity::getId,
                FlowTaskEntity::getParentId, FlowTaskEntity::getFlowType, FlowTaskEntity::getFullName,
                FlowTaskEntity::getStatus
        );
        if (entity != null) {
            if (Objects.equals(entity.getFlowType(), 1)) {
                return ActionResult.fail("功能流程不能删除");
            }
            if (!FlowNature.ParentId.equals(entity.getParentId()) && StringUtil.isNotEmpty(entity.getParentId())) {
                return ActionResult.fail(entity.getFullName() + "不能删除");
            }
            flowTaskService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 待我审核催办
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "发起催办")
    @PostMapping("/Press/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult press(@PathVariable("id") String id) throws WorkFlowException {
        FlowModel flowModel = new FlowModel();
        UserInfo userInfo = userProvider.get();
        flowModel.setUserInfo(userInfo);
        boolean flag = flowTaskNewService.press(id, flowModel);
        if (flag) {
            return ActionResult.success("催办成功");
        }
        return ActionResult.fail("未找到催办人");
    }

    /**
     * 撤回流程发起
     * 注意：在撤销流程时要保证你的下一节点没有处理这条记录；如已处理则无法撤销流程。
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "撤回流程发起")
    @PutMapping("/{id}/Actions/Withdraw")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult revoke(@PathVariable("id") String id, @RequestBody FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = userProvider.get();
        flowModel.setUserInfo(userInfo);
        flowTaskNewService.revoke(ImmutableList.of(id), flowModel, true);
        return ActionResult.success("撤回成功");
    }
}
