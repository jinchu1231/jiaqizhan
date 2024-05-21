package jnpf.engine.controller;

import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.engine.entity.FlowDelegateEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.engine.model.flowtask.FlowTaskListModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.model.flowtemplate.FlowTemplateCrForm;
import jnpf.engine.model.flowtemplate.FlowTemplateInfoVO;
import jnpf.engine.service.FlowDelegateService;
import jnpf.engine.service.FlowDynamicService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateJsonService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.exception.WorkFlowException;
import jnpf.model.FlowWorkListVO;
import jnpf.permission.model.user.WorkHandoverModel;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
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
@RequestMapping("/api/workflow/Engine/flowTemplateJson")
public class FlowTempJsonComtroller {


    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowDelegateService flowDelegateService;
    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private FlowTemplateJsonService flowTemplateJsonService;
    @Autowired
    private FlowDynamicService flowDynamicService;


    //———————————————流程任务数据——————————

    /**
     * 列表（待我审批）
     *
     * @return
     */
    
    @PostMapping("/GetWaitList")
    public PageListVO getWaitList(@RequestBody PaginationFlowTask pagination) {
        List<FlowTaskListModel> result = flowTaskService.getWaitList(pagination);
        PageListVO pageListVO = new PageListVO();
        pageListVO.setList(result);
        pageListVO.setPagination(JsonUtil.getJsonToBean(pagination,PaginationVO.class));
        return pageListVO;
    }

    /**
     * 列表（我已审批）
     *
     * @return
     */
    
    @PostMapping("/GetTrialList")
    public PageListVO getTrialList(@RequestBody PaginationFlowTask pagination) {
        List<FlowTaskListModel> result = flowTaskService.getTrialList(pagination);
        PageListVO pageListVO = new PageListVO();
        pageListVO.setList(result);
        pageListVO.setPagination(JsonUtil.getJsonToBean(pagination,PaginationVO.class));
        return pageListVO;
    }

    /**
     * 列表（订单状态）
     *
     * @return
     */
    
    @PostMapping("/GetOrderStaList")
    public List<FlowTaskEntity> getOrderStaList(@RequestBody List<String> id) {
        return flowTaskService.getOrderStaList(id);
    }

    
    @PostMapping("/GetTaskList")
    public List<FlowTaskEntity> getTaskList(String[] ids) {
        return flowTaskService.getInfosSubmit(ids, FlowTaskEntity::getStatus, FlowTaskEntity::getId, FlowTaskEntity::getProcessId);
    }

    
    @GetMapping("/getDelegateList")
    public List<FlowDelegateEntity> getDelegateList() {
        return flowDelegateService.getList();
    }

    
    @PostMapping("/getCirculateList")
    public PageListVO getCirculateList(@RequestBody PaginationFlowTask pagination) {
        List<FlowTaskListModel> result = flowTaskService.getCirculateList(pagination);
        PageListVO pageListVO = new PageListVO();
        pageListVO.setList(result);
        pageListVO.setPagination(JsonUtil.getJsonToBean(pagination,PaginationVO.class));
        return pageListVO;
    }

    //———————————————流程引擎数据——————————

    
    @PostMapping(value = "/getListAll")
    public PageListVO getListAll(@RequestBody FlowPagination pagination) {
        List<FlowTemplateEntity> pageList = flowTemplateService.getListAll(pagination, true);
        PageListVO pageListVO = new PageListVO();
        pageListVO.setList(pageList);
        pageListVO.setPagination(JsonUtil.getJsonToBean(pagination,PaginationVO.class));
        return pageListVO;
    }

    
    @PostMapping("/getTemplateList")
    public List<FlowTemplateEntity> getTemplateList(@RequestBody List<String> templaIdList) {
        List<FlowTemplateEntity> list = flowTemplateService.getTemplateList(templaIdList);
        return list;
    }

    
    @GetMapping("/getFlowTemplateJsonEntity")
    public FlowTemplateJsonEntity getFlowTemplateJsonEntity(@RequestParam("id") String id) throws WorkFlowException {
        return flowTemplateJsonService.getInfo(id);
    }

    
    @GetMapping("/getInfoSubmit")
    public ActionResult<FlowTaskEntity> getInfoSubmit(@RequestParam("id") String id) {
        ActionResult result = new ActionResult();
        try {
            FlowTaskEntity info = flowTaskService.getInfo(id);
            result.setData(info);
        } catch (Exception e) {
        }
        return result;
    }

    
    @PostMapping("/deleteFlowTask")
    public ActionResult deleteFlowTask(@RequestBody FlowTaskEntity taskEntity) {
        try{
            flowTaskService.delete(taskEntity);
            return  ActionResult.success();
        }catch (Exception e){
            return  ActionResult.fail(e.getMessage());
        }
    }

    
    @PostMapping("/createTemplate")
    public ActionResult createTemplate(@RequestBody FlowTemplateCrForm form) {
        String msg = "";
        try {
            if (flowTemplateService.isExistByFullName(form.getFullName(), form.getId())) {
                return ActionResult.fail("流程名称不能重复");
            }
            if (flowTemplateService.isExistByEnCode(form.getEnCode(), form.getId())) {
                return ActionResult.fail("流程编码不能重复");
            }
            FlowTemplateEntity entity = JsonUtil.getJsonToBean(form, FlowTemplateEntity.class);
            flowTemplateService.create(entity);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        return StringUtil.isNotEmpty(msg) ? ActionResult.fail(msg) : ActionResult.success();
    }

    
    @PostMapping("/updateTemplate/{id}")
    public ActionResult updateTemplate(@PathVariable("id") String id, @RequestBody FlowTemplateCrForm form) {
        String msg = "";
        try {
            if (flowTemplateService.isExistByFullName(form.getFullName(), id)) {
                ActionResult.success("流程名称不能重复");
            }
            if (flowTemplateService.isExistByEnCode(form.getEnCode(),id)) {
                ActionResult.success("流程编码不能重复");
            }
            FlowTemplateEntity entity = JsonUtil.getJsonToBean(form, FlowTemplateEntity.class);
            flowTemplateService.update(id, entity);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        return StringUtil.isNotEmpty(msg) ? ActionResult.fail(msg) : ActionResult.success();
    }

    
    @GetMapping("/getTemplateInfo/{id}")
    public FlowTemplateInfoVO getTemplateInfo(@PathVariable("id") String id) {
        FlowTemplateInfoVO vo = new FlowTemplateInfoVO();
        try {
            vo = flowTemplateService.info(id);
        } catch (Exception e) {
            vo = null;
        }
        return vo;
    }

    
    @GetMapping("/deleteTemplateInfo/{id}")
    public ActionResult deleteTemplateInfo(@PathVariable("id") String id) {
        String msg = "";
        try {
            FlowTemplateEntity entity = flowTemplateService.getInfo(id);
            flowTemplateService.delete(entity);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        return StringUtil.isNotEmpty(msg) ? ActionResult.fail(msg) : ActionResult.success();
    }

    /**
     * 逻辑删除还原数据
     * @param id
     */
    
    @GetMapping("/saveLogicFlowAndForm" )
    public void saveLogicFlowAndForm(@RequestParam("id" ) String id) {
        flowTemplateService.saveLogicFlowAndForm(id);
    }

    /**
     * 获取待办及负责流程
     */
    
    @GetMapping("/flowWork" )
    public FlowWorkListVO flowWork(@RequestParam("fromId") String fromId) {
        return flowTaskService.flowWork(fromId);
    }

    /**
     * 获取待办及负责流程
     */
    
    @PostMapping("/saveFlowWork" )
    public boolean saveFlowWork(@RequestBody WorkHandoverModel workHandoverModel) {
        return flowTaskService.flowWork(workHandoverModel);
    }

    
    @PostMapping("/batchCreateOrUpdate")
    public ActionResult batchCreateOrUpdate(@RequestBody FlowModel flowModel)  {
        ActionResult result = new ActionResult();
        try {
            flowDynamicService.batchCreateOrUpdate(flowModel);
        }catch (Exception e){
            result.setCode(400);
            result.setMsg(e.getMessage());
        }
        return result;
    }

}
