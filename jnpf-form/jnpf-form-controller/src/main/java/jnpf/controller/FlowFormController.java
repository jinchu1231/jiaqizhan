package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.entity.FlowFormEntity;
import jnpf.entity.FlowFormRelationEntity;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.FlowTempInfoModel;
import jnpf.model.form.FlowFieldModel;
import jnpf.model.form.FlowFormModel;
import jnpf.model.form.FlowFormPage;
import jnpf.model.form.FlowFormVo;
import jnpf.model.form.FlowSelectVo;
import jnpf.model.form.FormDraftJsonModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormCloumnUtil;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.FlowFormRelationService;
import jnpf.service.FlowFormService;
import jnpf.util.DataFileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 18:03
 */
@Tag(name = "流程表单控制器" , description = "FlowForm" )
@RestController
@RequestMapping("/api/flowForm/Form")
public class FlowFormController extends SuperController<FlowFormService, FlowFormEntity>{
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserService userService;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @Autowired
    private FlowFormRelationService flowFormRelationService;

    @Operation(summary = "表单列表" )
    @GetMapping
    @SaCheckPermission("formDesign" )
    public ActionResult getList(FlowFormPage flowFormPage) {
        List<FlowFormEntity> list = flowFormService.getList(flowFormPage);
        List<FlowFormVo> listVo = JsonUtil.getJsonToList(list, FlowFormVo.class);
        listVo.stream().forEach(item -> {
            if (StringUtil.isNotEmpty(item.getCreatorUserId())) {
                UserEntity info = userService.getInfo(item.getCreatorUserId());
                if (info != null) item.setCreatorUser(info.getRealName() + "/" + info.getAccount());
            }
            if (StringUtil.isNotEmpty(item.getLastModifyUserId())) {
                UserEntity info = userService.getInfo(item.getLastModifyUserId());
                if (info != null) item.setLastModifyUser(info.getRealName() + "/" + info.getAccount());
            }
            item.setIsRelease(item.getState());
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(flowFormPage, PaginationVO.class);
        return ActionResult.page(listVo, paginationVO);
    }

    @Operation(summary = "表单下拉列表" )
    @GetMapping("/select" )
    @SaCheckPermission("formDesign" )
    public ActionResult getListForSelect(FlowFormPage flowFormPage) {
        if(Objects.equals(1,flowFormPage.getFlowType())){
            flowFormPage.setFormType(1);
        }
        List<FlowFormEntity> list = flowFormService.getListForSelect(flowFormPage);
        List<FlowSelectVo> listVo = new ArrayList<>();
        for (FlowFormEntity entity : list) {
            FlowSelectVo flowSelectVo = JsonUtil.getJsonToBean(entity, FlowSelectVo.class);
            flowSelectVo.setIsQuote(StringUtil.isNotEmpty(entity.getFlowId()));
            listVo.add(flowSelectVo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(flowFormPage, PaginationVO.class);
        return ActionResult.page(listVo, paginationVO);
    }

    @Operation(summary = "查看" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @GetMapping("/{id}" )
    @SaCheckPermission(value = {"formDesign" , "onlineDev.webDesign","generator.webForm","generator.flowForm"}, mode = SaMode.OR)
    public ActionResult getInfo(@PathVariable("id" ) String id) {
        FlowFormEntity entity = flowFormService.getById(id);
        FlowFormVo vo = JsonUtil.getJsonToBean(entity, FlowFormVo.class);
        if (ObjectUtil.isNotEmpty(entity.getDraftJson())) {
            FormDraftJsonModel formDraft = JsonUtil.getJsonToBean(entity.getDraftJson(), FormDraftJsonModel.class);
            vo.setDraftJson(Optional.ofNullable(formDraft.getDraftJson()).orElse(null));
            vo.setTableJson(formDraft.getTableJson());
        }
        return ActionResult.success(vo);
    }

    @Operation(summary = "保存表单" )
    @PostMapping
    @SaCheckPermission("formDesign" )
    public ActionResult save(@RequestBody FlowFormModel formModel) throws WorkFlowException {
        FlowFormEntity entity = JsonUtil.getJsonToBean(formModel, FlowFormEntity.class);
        //判断子表是否复用
        if (formModel.getFormType() == 2 && entity.getDraftJson() != null) {
            RecursionForm recursionForm = new RecursionForm();
            FormDataModel formData = JsonUtil.getJsonToBean(entity.getDraftJson(), FormDataModel.class);
            List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
            recursionForm.setList(list);
            List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getTableJson(), TableModel.class);
            recursionForm.setTableModelList(tableModelList);
            if (FormCloumnUtil.repetition(recursionForm, new ArrayList<>())) {
                return ActionResult.fail("子表重复" );
            }
        }
        //判断名称是否重复
        if (flowFormService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        //判断编码是否重复
        if (flowFormService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setCreatorTime(new Date());
        entity.setState(0);
        entity.setEnabledMark(0);//首次创建为未发布
        flowFormService.create(entity);
        return ActionResult.success(MsgCode.SU002.get());
    }

    @Operation(summary = "修改表单" )
    @PutMapping
    @SaCheckPermission("formDesign" )
    public ActionResult update(@RequestBody FlowFormModel formModel) throws Exception {
        FlowFormEntity entity = JsonUtil.getJsonToBean(formModel, FlowFormEntity.class);
        if (flowFormService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (flowFormService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(new Date());
        //判断子表是否复用
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getTableJson(), TableModel.class);
        //在已发布的状态下 删表动作禁用
        if (formModel.getFormType() == 2 && entity.getEnabledMark() == 1 && tableModelList.size() == 0) {
            return ActionResult.fail(MsgCode.VS408.get());
        }
        if (formModel.getFormType() == 2 && tableModelList.size() > 0) {
            RecursionForm recursionForm = new RecursionForm();
            if(StringUtil.isNotEmpty(entity.getDraftJson())) {
                FormDataModel formData = JsonUtil.getJsonToBean(entity.getDraftJson(), FormDataModel.class);
                List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
                recursionForm.setList(list);
                recursionForm.setTableModelList(tableModelList);
                if (FormCloumnUtil.repetition(recursionForm, new ArrayList<>())) {
                    return ActionResult.fail("子表重复");
                }
            }
        }
        FlowFormEntity info = flowFormService.getById(formModel.getId());
//        boolean json = !Objects.equals(info.getPropertyJson(),formModel.getDraftJson()) || !Objects.equals(info.getTableJson(),formModel.getTableJson());
        if(info!=null && Objects.equals(info.getState(),1)){
            entity.setState(2);
        }
        if(info!=null && StringUtil.isNotEmpty(info.getFlowId())){
            entity.setFlowId(info.getFlowId());
        }
        boolean b = flowFormService.update(entity);
        if (b) {
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail(MsgCode.FA002.get());
    }

    @Operation(summary = "发布/回滚" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true),
            @Parameter(name = "isRelease" , description = "是否发布：是否发布：1-发布 0-回滚" )
    })
    @PostMapping("/Release/{id}" )
    @SaCheckPermission("formDesign" )
    public ActionResult release(@PathVariable("id" ) String id, @RequestParam("isRelease" ) Integer isRelease) throws WorkFlowException {
        return flowFormService.release(id, isRelease);
    }

    @Operation(summary = "复制表单" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @GetMapping("/{id}/Actions/Copy" )
    @SaCheckPermission("formDesign" )
    public ActionResult copyForm(@PathVariable("id" ) String id) {
        try {
            boolean b = flowFormService.copyForm(id);
            if (b) {
                return ActionResult.success(MsgCode.SU007.get());
            }
        } catch (Exception e) {
            return ActionResult.fail("已到达该模板复制上限，请复制源模板!" );
        }

        return ActionResult.fail("复制失败" );
    }

    @Operation(summary = "删除表单" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @DeleteMapping("/{id}" )
    @SaCheckPermission("formDesign" )
    public ActionResult delete(@PathVariable("id" ) String id) {
        //todo 该表单已被流程引用，无法删除 -完成
        List<FlowFormRelationEntity> listByFormId = flowFormRelationService.getListByFormId(id);
        if (CollectionUtils.isNotEmpty(listByFormId)) {
            return ActionResult.fail("该表单已被流程引用，无法删除！" );
        }
        boolean b = flowFormService.removeById(id);
        if (b) {
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }


    @Operation(summary = "工作流表单导出" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @GetMapping("/{id}/Actions/Export" )
    @SaCheckPermission("formDesign" )
    public ActionResult exportData(@PathVariable("id" ) String id) throws WorkFlowException {
        FlowFormEntity entity = flowFormService.getById(id);
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.FLOW_FLOWDFORM.getTableName());
        return ActionResult.success(downloadVO);
    }


    @Operation(summary = "工作流表单导入" )
    @PostMapping(value = "/Actions/Import" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("formDesign" )
    public ActionResult ImportData(@RequestPart("file" ) MultipartFile multipartFile,@RequestPart("type" ) String type) throws WorkFlowException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.FLOW_FLOWDFORM.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        FlowFormEntity entity = JsonUtil.getJsonToBean(fileContent, FlowFormEntity.class);
        return flowFormService.ImportData(entity,type);
    }

    @Operation(summary = "获取表单字段列表" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @GetMapping(value = "/{id}/getField" )
    @SaCheckPermission("formDesign" )
    public ActionResult getField(@PathVariable("id" ) String id) {
        FlowFormEntity entity = flowFormService.getById(id);
        if (entity == null || entity.getEnabledMark() != 1) return ActionResult.fail("表单不存在或者未发布！" );
        FlowFormVo vo = JsonUtil.getJsonToBean(entity, FlowFormVo.class);
        List<FlowFieldModel> list = new ArrayList<>();
        if (vo.getFormType() == 0) {//0系统表单
            list = JsonUtil.getJsonToList(vo.getPropertyJson(), FlowFieldModel.class);
        } else {
            JSONObject objects = JSONObject.parseObject(vo.getPropertyJson());
            JSONArray arr = objects.getJSONArray("fields" );
            for (Object obj : arr) {
                JSONObject object = (JSONObject) obj;
                FlowFieldModel flowFieldModel = new FlowFieldModel();
                JSONObject config = object.getJSONObject("__config__" );
                flowFieldModel.setFiledId(object.get("__vModel__" ).toString())
                        .setFiledName(config.get("label" ).toString())
                        .setJnpfKey(config.get("jnpfKey" ).toString())
                        .setRequired(config.get("required" ).toString());
                list.add(flowFieldModel);
            }
        }
        return ActionResult.success(list);
    }


    @Operation(summary = "获取引擎id" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" , required = true)
    })
    @GetMapping("/getFormById/{id}" )
//    @SaCheckPermission("formDesign")
    public ActionResult getFormById(@PathVariable("id" ) String id) throws WorkFlowException {
        FlowTempInfoModel model = flowFormService.getFormById(id);
        return ActionResult.success("获取成功" , model);
    }


}
