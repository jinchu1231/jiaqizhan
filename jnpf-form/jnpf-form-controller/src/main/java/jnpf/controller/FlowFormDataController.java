package jnpf.controller;

import jnpf.base.ActionResult;
import jnpf.constant.MsgCode;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.FlowFormDataModel;
import jnpf.service.FlowFormRelationService;
import jnpf.service.FlowFormService;
import jnpf.service.FormDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flowForm/FormData")
public class FlowFormDataController {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private FlowFormRelationService flowFormRelationService;



    @PostMapping("/saveOrUpdate")
    public ActionResult saveOrUpdate(@RequestBody FlowFormDataModel flowFormDataModel) {
        ActionResult result = ActionResult.success();
        try {
            formDataService.saveOrUpdate(flowFormDataModel);
        }catch (WorkFlowException e){
            result.setCode(400);
            result.setMsg(e.getMessage());
        }
        return result;
    }


    @PostMapping("/info")
    public ActionResult info(@RequestBody FlowFormDataModel flowFormDataModel) {
        return formDataService.info(flowFormDataModel.getFormId(),flowFormDataModel.getId());
    }

    /**
     * 流程引用表单保存
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/2/17
     */

    @PostMapping("/saveFlowIdByFormIds/{flowId}" )
    public ActionResult saveFlowIdByFormIds(@PathVariable("flowId" ) String flowId, @RequestBody List<String> formIds) {
        flowFormRelationService.saveFlowIdByFormIds(flowId, formIds);
        return ActionResult.success();
    }

    /**
     * 根据流程id获取表单信息
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/2/17
     */

    @GetMapping("/getFlowIdList/{flowId}" )
    public List<FlowFormEntity> getFlowIdList(@PathVariable("flowId" ) String flowId) {
        return flowFormService.getFlowIdList(flowId);
    }

    /**
     * 更新功能表单关联流程id
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/2/17
     */

    @PostMapping("/updateForm" )
    public void updateForm(@RequestBody FlowFormEntity entity) {
        flowFormService.updateForm(entity);
    }

    /**
     * 获取表单详情
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/2/17
     */

    @GetMapping("/getById/{formId}" )
    public FlowFormEntity getById(@PathVariable("formId" ) String formId) {
        return flowFormService.getById(formId);
    }

    /**
     * 修改或保存
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/2/17
     */

    @PostMapping("/form/saveOrUpdate" )
    public ActionResult saveOrUpdate(@RequestBody FlowFormEntity flowFormEntity) throws WorkFlowException {
        //判断名称是否重复
        if (flowFormService.isExistByFullName(flowFormEntity.getFullName(), flowFormEntity.getId())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        //判断编码是否重复
        if (flowFormService.isExistByEnCode(flowFormEntity.getEnCode(), flowFormEntity.getId())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        flowFormService.saveOrUpdate(flowFormEntity);
        return ActionResult.success();
    }

    /**
     * 删除
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/2/17
     */

    @GetMapping("/remobeById" )
    public void remobeById(@RequestParam("id" ) String id) {
        flowFormService.removeById(id);
    }

    /**
     * 逻辑删除还原数据
     * @param id
     */

    @GetMapping("/saveLogicFlowAndForm" )
    public void saveLogicFlowAndForm(@RequestParam("id" ) String id) {
        flowFormService.saveLogicFlowAndForm(id);
    }
}
