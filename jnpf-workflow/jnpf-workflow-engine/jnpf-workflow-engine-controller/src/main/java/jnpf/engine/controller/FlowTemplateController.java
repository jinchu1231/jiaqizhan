package jnpf.engine.controller;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.entity.FlowTemplateJsonEntity;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.engine.model.flowengine.PaginationFlowEngine;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowtask.FlowAssistModel;
import jnpf.engine.model.flowtemplate.FlowExportModel;
import jnpf.engine.model.flowtemplate.FlowPageListVO;
import jnpf.engine.model.flowtemplate.FlowSelectVO;
import jnpf.engine.model.flowtemplate.FlowTemplatUprForm;
import jnpf.engine.model.flowtemplate.FlowTemplateCrForm;
import jnpf.engine.model.flowtemplate.FlowTemplateInfoVO;
import jnpf.engine.model.flowtemplate.FlowTemplateListVO;
import jnpf.engine.model.flowtemplate.FlowTemplateVO;
import jnpf.engine.model.flowtemplatejson.FlowTemplateJsonListVO;
import jnpf.engine.model.flowtemplatejson.FlowTemplateJsonPage;
import jnpf.engine.service.FlowEngineVisibleService;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateJsonService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.model.form.FlowFormVo;
import jnpf.permission.entity.UserEntity;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "流程模板", description = "template")
@RestController
@RequestMapping("/api/workflow/Engine/flowTemplate")
public class FlowTemplateController extends SuperController<FlowTemplateService, FlowTemplateEntity> {

    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private FlowTemplateJsonService flowTemplateJsonService;
    @Autowired
    private FlowEngineVisibleService flowEngineVisibleService;
    @Autowired
    private ServiceAllUtil serviceUtil;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private UserProvider userProvider;

    /**
     * 获取流程引擎列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取流程引擎列表")
    @GetMapping
    public ActionResult<PageListVO<FlowPageListVO>> list(FlowPagination pagination) {
        List<FlowTemplateEntity> list = flowTemplateService.getPageList(pagination);
        List<DictionaryDataEntity> dictionList = serviceUtil.getDictionName(list.stream().map(FlowTemplateEntity::getCategory).collect(Collectors.toList()));
        List<UserEntity> userList = serviceUtil.getUserName(list.stream().map(FlowTemplateEntity::getCreatorUserId).collect(Collectors.toList()));
        List<FlowPageListVO> listVO = new ArrayList<>();
        for (FlowTemplateEntity entity : list) {
            FlowPageListVO vo = JsonUtil.getJsonToBean(entity, FlowPageListVO.class);
            DictionaryDataEntity dataEntity = dictionList.stream().filter(t -> t.getId().equals(entity.getCategory())).findFirst().orElse(null);
            vo.setCategory(dataEntity != null ? dataEntity.getFullName() : "");
            UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(entity.getCreatorUserId())).findFirst().orElse(null);
            vo.setCreatorUser(userEntity != null ? userEntity.getRealName() + "/" + userEntity.getAccount() : "");
            listVO.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 获取流程设计列表
     *
     * @return
     */
    @Operation(summary = "流程引擎下拉框")
    @GetMapping("/Selector")
    public ActionResult<ListVO<FlowTemplateListVO>> listSelect() {
        PaginationFlowEngine pagination = new PaginationFlowEngine();
        pagination.setEnabledMark(1);
        pagination.setType(0);
        List<FlowTemplateListVO> treeList = flowTemplateService.getTreeList(pagination, true);
        ListVO vo = new ListVO();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 可见引擎下拉框
     *
     * @return
     */
    @Operation(summary = "可见引擎下拉框")
    @GetMapping("/ListAll")
    public ActionResult<ListVO<FlowTemplateListVO>> listAll() {
        PaginationFlowEngine pagination = new PaginationFlowEngine();
        List<FlowTemplateListVO> treeList = flowTemplateService.getTreeList(pagination, false);
        ListVO vo = new ListVO();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 可见的流程引擎列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "可见的流程引擎列表")
    @GetMapping("/PageListAll")
    public ActionResult<PageListVO<FlowPageListVO>> listAll(FlowPagination pagination) {
        List<FlowTemplateEntity> list = flowTemplateService.getListAll(pagination, true);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        List<FlowPageListVO> listVO = JsonUtil.getJsonToList(list, FlowPageListVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 可见的流程引擎列表
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "可见的子流程流程引擎列表")
    @GetMapping("/PageChildListAll")
    public ActionResult<PageListVO<FlowSelectVO>> childListAll(FlowPagination pagination) {
        List<FlowSelectVO> list = flowTemplateJsonService.getChildListPage(pagination);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取流程引擎信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取流程引擎信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<FlowTemplateInfoVO> info(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateInfoVO vo = flowTemplateService.info(id);
        return ActionResult.success(vo);
    }

    /**
     * 新建流程设计
     *
     * @param form 流程模型
     * @return
     */
    @Operation(summary = "新建流程引擎")
    @PostMapping
    @Parameters({
            @Parameter(name = "form", description = "流程模型", required = true),
    })
    public ActionResult create(@RequestBody @Valid FlowTemplateCrForm form) throws WorkFlowException {
        FlowTemplateEntity entity = JsonUtil.getJsonToBean(form, FlowTemplateEntity.class);
        String json = StringUtil.isNotEmpty(form.getFlowTemplateJson()) ? form.getFlowTemplateJson() : "[]";
        List<FlowTemplateJsonEntity> templatejson = JsonUtil.getJsonToList(json, FlowTemplateJsonEntity.class);
        flowTemplateService.create(entity, templatejson);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新流程设计
     *
     * @param id   主键
     * @param form 流程模型
     * @return
     */
    @Operation(summary = "更新流程引擎")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "form", description = "流程模型", required = true),
    })
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid FlowTemplatUprForm form) throws WorkFlowException {
        FlowTemplateEntity entity = JsonUtil.getJsonToBean(form, FlowTemplateEntity.class);
        String json = StringUtil.isNotEmpty(form.getFlowTemplateJson()) ? form.getFlowTemplateJson() : "[]";
        List<FlowTemplateJsonEntity> templateJsonList = JsonUtil.getJsonToList(json, FlowTemplateJsonEntity.class);
        FlowTemplateVO vo = flowTemplateService.updateVisible(id, entity, templateJsonList);
        return ActionResult.success(MsgCode.SU004.get(), vo);
    }

    /**
     * 删除流程设计
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除流程引擎")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult delete(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateEntity entity = flowTemplateService.getInfo(id);
        flowTemplateService.delete(entity);
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 复制流程表单
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "复制流程表单")
    @PostMapping("/{id}/Actions/Copy")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult copy(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateEntity flowtemplate = flowTemplateService.getInfo(id);
        if (flowtemplate != null) {
            if (flowtemplate.getType() == 1) {
                throw new WorkFlowException(MsgCode.WF127.get());
            }
            List<FlowTemplateJsonEntity> templateJson = flowTemplateJsonService.getMainList(ImmutableList.of(id));
            flowTemplateService.copy(flowtemplate, templateJson);
            return ActionResult.success(MsgCode.SU007.get());
        }
        return ActionResult.fail(MsgCode.FA004.get());
    }

    /**
     * 流程表单状态
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "更新流程表单状态")
    @PutMapping("/{id}/Actions/State")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult state(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateEntity entity = flowTemplateService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark("1".equals(String.valueOf(entity.getEnabledMark())) ? 0 : 1);
            flowTemplateService.update(id, entity);
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail(MsgCode.FA002.get());
    }

    /**
     * 发布流程引擎
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "发布流程设计")
    @PostMapping("/Release/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult release(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateEntity entity = flowTemplateService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark(1);
            List<FlowTemplateJsonEntity> templateJson = flowTemplateJsonService.getMainList(ImmutableList.of(id));
            if (templateJson.size() == 0) {
                return ActionResult.fail("启用失败，流程未设计！");
            }
            flowTemplateService.update(id, entity);
            return ActionResult.success(MsgCode.WF131.get());
        }
        return ActionResult.fail(MsgCode.FA011.get());
    }

    /**
     * 停止流程引擎
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "停止流程设计")
    @PostMapping("/Stop/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult stop(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateEntity entity = flowTemplateService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark(0);
            flowTemplateService.update(id, entity);
            return ActionResult.success(MsgCode.WF130.get());
        }
        return ActionResult.fail(MsgCode.FA008.get());
    }

    /**
     * 工作流导出
     *
     * @param id 主键
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "工作流导出")
    @GetMapping("/{id}/Actions/Export")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<DownloadVO> exportData(@PathVariable("id") String id) throws WorkFlowException {
        FlowExportModel model = flowTemplateService.exportData(id);
        DownloadVO downloadVO = serviceUtil.exportData(model);
        return ActionResult.success(downloadVO);
    }

    /**
     * 工作流导入
     *
     * @param multipartFile 文件
     * @return
     * @throws WorkFlowException
     */
    @Operation(summary = "工作流导入")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult ImportData(@RequestPart("file") MultipartFile multipartFile, @RequestPart("type") String type) throws WorkFlowException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.FLOW_FLOWENGINE.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        FlowExportModel flowExportModel = JsonUtil.getJsonToBean(fileContent, FlowExportModel.class);
        if (ObjectUtil.isEmpty(flowExportModel.getFlowTemplate())) {
            return ActionResult.fail("导入数据格式不正确");
        }
        flowTemplateService.ImportData(flowExportModel, type);
        return ActionResult.success(MsgCode.IMP001.get());
    }

    /**
     * 流程版本列表
     *
     * @param templateId 主键
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "流程版本列表")
    @GetMapping("{templateId}/FlowJsonList")
    @Parameters({
            @Parameter(name = "templateId", description = "主键", required = true),
    })
    public ActionResult<PageListVO<FlowTemplateJsonListVO>> list(@PathVariable("templateId") String templateId, FlowTemplateJsonPage pagination) {
        List<FlowTemplateJsonEntity> list = flowTemplateJsonService.getListPage(pagination, true);
        List<String> createId = list.stream().map(FlowTemplateJsonEntity::getCreatorUserId).collect(Collectors.toList());
        List<UserEntity> userName = serviceUtil.getUserName(createId);
        List<FlowTemplateJsonListVO> listVO = JsonUtil.getJsonToList(list, FlowTemplateJsonListVO.class);
        for (FlowTemplateJsonListVO templateJson : listVO) {
            UserEntity entity = userName.stream().filter(t -> t.getId().equals(templateJson.getCreatorUserId())).findFirst().orElse(null);
            templateJson.setCreatorUser(entity != null ? entity.getRealName() + "/" + entity.getAccount() : "");
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 设置主版本
     *
     * @param ids 主键
     * @return
     */
    @Operation(summary = "设置主版本")
    @PostMapping("{ids}/MainVersion")
    @Parameters({
            @Parameter(name = "ids", description = "主键", required = true),
    })
    public ActionResult mainVersion(@PathVariable("ids") String ids) throws WorkFlowException {
        flowTemplateJsonService.templateJsonMajor(ids);
        return ActionResult.success("修改成功");
    }

    /**
     * 删除版本
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除版本")
    @DeleteMapping("{id}/FlowJson")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult flowJson(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateJsonEntity entity = flowTemplateJsonService.getInfo(id);
        List<FlowTaskEntity> flowTaskList = flowTaskService.getFlowList(entity.getId());
        if (flowTaskList.size() > 0) {
            throw new WorkFlowException("该版本内有工单任务流转，无法删除！");
        }
        flowTemplateJsonService.deleteFormFlowId(entity);
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 流程类型下拉
     *
     * @param id   主键
     * @param type 类型
     * @return
     */
    @Operation(summary = "流程类型下拉")
    @GetMapping("/FlowJsonList/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<List<FlowSelectVO>> flowJsonList(@PathVariable("id") String id, String type) {
        UserInfo userInfo = userProvider.get();
        List<FlowTemplateJsonEntity> list = flowTemplateJsonService.getMainList(ImmutableList.of(id));
        List<FlowSelectVO> listVO = new ArrayList<>();
        if (StringUtil.isNotEmpty(type) && !userInfo.getIsAdministrator()) {
            List<FlowEngineVisibleEntity> visibleFlowList = flowEngineVisibleService.getVisibleFlowList(userInfo.getUserId());
            for (FlowTemplateJsonEntity entity : list) {
                boolean count = visibleFlowList.stream().filter(t -> t.getFlowId().equals(entity.getId())).count() > 0;
                if ((entity.getVisibleType() == 1 && count) || entity.getVisibleType() == 0) {
                    FlowSelectVO vo = JsonUtil.getJsonToBean(entity, FlowSelectVO.class);
                    listVO.add(vo);
                }
            }
            if (listVO.size() == 0) {
                return ActionResult.fail("您没有发起该流程的权限");
            }
        } else {
            listVO.addAll(JsonUtil.getJsonToList(list, FlowSelectVO.class));
        }
        return ActionResult.success(listVO);
    }

    /**
     * 子流程表单信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "子流程表单信息")
    @GetMapping("/{id}/FormInfo")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<FlowFormVo> formInfo(@PathVariable("id") String id) throws WorkFlowException {
        FlowTemplateJsonEntity info = flowTemplateJsonService.getInfo(id);
        ChildNode childNode = JsonUtil.getJsonToBean(info.getFlowTemplateJson(), ChildNode.class);
        String formId = childNode.getProperties().getFormId();
        FlowFormEntity entity = serviceUtil.getForm(formId);
        if (entity == null) {
            throw new WorkFlowException("表单未找到");
        }
        FlowFormVo vo = JsonUtil.getJsonToBean(entity, FlowFormVo.class);
        return ActionResult.success(vo);
    }

    /**
     * 流程协管
     *
     * @param assistModel 协管模型
     * @return
     */
    @Operation(summary = "流程协管")
    @PostMapping("/assist")
    @Parameters({
            @Parameter(name = "assistModel", description = "协管模型", required = true),
    })
    public ActionResult assist(@RequestBody FlowAssistModel assistModel) {
        flowEngineVisibleService.assistList(assistModel);
        return ActionResult.success("保存成功");
    }

    /**
     * 委托可选全部流程
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "委托可选全部流程")
    @GetMapping("/getflowAll")
    public ActionResult<PageListVO<FlowPageListVO>> getflowAll(FlowPagination pagination) {
        List<FlowTemplateEntity> listByFlowIds = flowTemplateService.getListAll(pagination, true);
        List<FlowPageListVO> listVO = JsonUtil.getJsonToList(listByFlowIds, FlowPageListVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 委托流程选择展示
     *
     * @param templateIds 委托流程
     * @return
     */
    @Operation(summary = "委托流程选择展示")
    @PostMapping("/getflowList")
    @Parameters({
            @Parameter(name = "templateIds", description = "委托流程", required = true),
    })
    public ActionResult<List<FlowPageListVO>> getflowList(@RequestBody List<String> templateIds) {
        FlowPagination pagination = new FlowPagination();
        pagination.setTemplateIdList(templateIds);
        List<FlowTemplateEntity> listByFlowIds = flowTemplateService.getListAll(pagination, false);
        List<FlowPageListVO> listVO = JsonUtil.getJsonToList(listByFlowIds, FlowPageListVO.class);
        return ActionResult.success("获取成功", listVO);
    }

    /**
     * 委托流程选择展示
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取协管")
    @GetMapping("/{id}/assistList")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<ListVO<String>> getAssistList(@PathVariable("id") String id) {
        List<FlowEngineVisibleEntity> assistListAll = flowEngineVisibleService.getList(ImmutableList.of(id));
        List<String> assistList = new ArrayList<>();
        for (FlowEngineVisibleEntity entity : assistListAll) {
            assistList.add(entity.getOperatorId() + "--" + entity.getOperatorType());
        }
        ListVO vo = new ListVO();
        vo.setList(assistList);
        return ActionResult.success(vo);
    }

    /**
     * 获取引擎id
     *
     * @param code 编码
     * @return
     */
    @Operation(summary = "获取引擎id")
    @GetMapping("/getFlowIdByCode/{code}")
    @Parameters({
            @Parameter(name = "code", description = "编码", required = true),
    })
    public ActionResult getFlowIdByCode(@PathVariable("code") String code) throws WorkFlowException {
        FlowTemplateEntity entity = flowTemplateService.getFlowIdByCode(code);
        return ActionResult.success("获取成功", entity.getId());
    }

}
