package jnpf.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.controller.SuperController;
import jnpf.constant.MsgCode;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.service.FlowDynamicService;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.util.UserProvider;
import jnpf.util.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "流程引擎", description = "FlowTask")
@RestController
@RequestMapping("/api/workflow/Engine/FlowTask")
public class FlowTaskController extends SuperController<FlowTaskService, FlowTaskEntity> {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FlowDynamicService flowDynamicService;

    /**
     * 保存
     *
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "保存")
    @PostMapping
    @Parameters({
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult save(@RequestBody FlowModel flowModel) throws WorkFlowException {
        boolean isApp = !RequestContext.isOrignPc();
        UserInfo userInfo = userProvider.get();
        flowModel.setUserInfo(userInfo);
        flowModel.setSystemId(isApp ? userInfo.getSystemId() : userInfo.getAppSystemId());
        flowDynamicService.batchCreateOrUpdate(flowModel);
        String msg = FlowStatusEnum.save.getMessage().equals(flowModel.getStatus()) ? MsgCode.SU002.get() : MsgCode.SU006.get();
        return ActionResult.success(msg);
    }

    /**
     * 提交
     *
     * @param id        主键
     * @param flowModel 流程模型
     * @return
     */
    @Operation(summary = "提交")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "flowModel", description = "流程模型", required = true),
    })
    public ActionResult submit(@RequestBody FlowModel flowModel, @PathVariable("id") String id) throws WorkFlowException {
        boolean isApp = !RequestContext.isOrignPc();
        UserInfo userInfo = userProvider.get();
        flowModel.setId(id);
        flowModel.setUserInfo(userInfo);
        flowModel.setSystemId(isApp ? userInfo.getSystemId() : userInfo.getAppSystemId());
        flowDynamicService.batchCreateOrUpdate(flowModel);
        String msg = FlowStatusEnum.save.getMessage().equals(flowModel.getStatus()) ? MsgCode.SU002.get() : MsgCode.SU006.get();
        return ActionResult.success(msg);
    }

}
