package jnpf.onlinedev.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.model.VisualWebTypeEnum;
import jnpf.base.service.ModuleService;
import jnpf.base.service.VisualdevReleaseService;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.VisualFlowFormUtil;
import jnpf.base.util.VisualUtil;
import jnpf.base.util.VisualUtils;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.emnus.ExportModelTypeEnum;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowtemplate.FlowTemplateInfoVO;
import jnpf.engine.service.FlowTaskService;
import jnpf.engine.service.FlowTemplateService;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.integrate.util.IntegrateUtil;
import jnpf.model.flow.DataModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.UploaderTemplateModel;
import jnpf.model.visualJson.config.HeaderModel;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.onlinedev.model.BaseDevModelVO;
import jnpf.onlinedev.model.BatchRemoveIdsVo;
import jnpf.onlinedev.model.ColumnDataInfoVO;
import jnpf.onlinedev.model.DataInfoVO;
import jnpf.onlinedev.model.FormDataInfoVO;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.onlinedev.model.OnlineImport.ExcelImportModel;
import jnpf.onlinedev.model.OnlineImport.ImportExcelFieldModel;
import jnpf.onlinedev.model.OnlineImport.VisualImportModel;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.onlinedev.model.PaginationModelExport;
import jnpf.onlinedev.model.VisualdevModelDataCrForm;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.model.VisualdevModelDataUpForm;
import jnpf.onlinedev.service.VisualDevInfoService;
import jnpf.onlinedev.service.VisualDevListService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.onlinedev.util.AutoFeildsUtil;
import jnpf.onlinedev.util.onlineDevUtil.OnlineDevListUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlineSwapDataUtils;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.service.UserRelationService;
import jnpf.service.FlowFormService;
import jnpf.util.DateUtil;
import jnpf.util.ExcelUtil;
import jnpf.util.FileExport;
import jnpf.util.FileUploadUtils;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UpUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import jnpf.util.context.RequestContext;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 0代码无表开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Tag(name = "0代码无表开发" , description = "OnlineDev" )
@RestController
@RequestMapping("/api/visualdev/OnlineDev")
public class VisualdevModelDataController extends SuperController<VisualdevModelDataService, VisualdevModelDataEntity>{
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTemplateService flowTemplateService;
    @Autowired
    private VisualDevListService visualDevListService;
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private VisualDevInfoService visualDevInfoService;
    @Autowired
    private VisualdevReleaseService visualdevReleaseService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private OnlineSwapDataUtils onlineSwapDataUtils;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private IntegrateUtil integrateUtil;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private VisualFlowFormUtil visualFlowFormUtil;

    @Operation(summary = "获取数据列表" )
    @Parameters({
            @Parameter(name = "modelId",description = "模板id"),
    })
    @PostMapping("/{modelId}/List" )
    public ActionResult list(@PathVariable("modelId" ) String modelId, @RequestBody PaginationModel paginationModel) throws WorkFlowException {
        StpUtil.checkPermission(modelId);

        VisualdevReleaseEntity visualdevEntity = visualdevReleaseService.getById(modelId);
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);

        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }
        ColumnDataModel columnDataModel = visualJsonModel.getColumnData();
        List<Map<String, Object>> realList;
        if (VisualWebTypeEnum.FORM.getType().equals(visualdevEntity.getWebType())) {
            realList = new ArrayList<>();
        } else if (VisualWebTypeEnum.DATA_VIEW.getType().equals(visualdevEntity.getWebType())) {//
            //数据视图的接口数据获取、
            realList = onlineSwapDataUtils.getInterfaceData(visualdevEntity, paginationModel, columnDataModel);
        } else {
            realList = visualDevListService.getDataList(visualJsonModel, paginationModel);
        }

        //判断数据是否分组
        if (OnlineDevData.TYPE_THREE_COLUMNDATA.equals(columnDataModel.getType())) {
            realList = OnlineDevListUtils.groupData(realList, columnDataModel);
        }
        //树形列表
        if (OnlineDevData.TYPE_FIVE_COLUMNDATA.equals(columnDataModel.getType())) {
            realList = OnlineDevListUtils.treeListData(realList, columnDataModel);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationModel, PaginationVO.class);

        return ActionResult.page(realList, paginationVO);
    }


    @Operation(summary = "树形异步查询子列表接口")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "id", description = "数据id"),
    })
    @PostMapping("/{modelId}/List/{id}")
    public ActionResult listTree(@PathVariable("modelId") String modelId, @RequestBody PaginationModel paginationModel, @PathVariable("id") String id) throws WorkFlowException {
        StpUtil.checkPermission(modelId);

        VisualdevReleaseEntity visualdevEntity = visualdevReleaseService.getById(modelId);
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }

        List<Map<String, Object>> realList = visualDevListService.getDataList(visualJsonModel, paginationModel);
        ColumnDataModel columnDataModel = visualJsonModel.getColumnData();
        String parentField = columnDataModel.getParentField() + "_id";

        List<Map<String, Object>> collect = realList.stream().filter(item -> id.equals(item.get(parentField))).collect(Collectors.toList());
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationModel, PaginationVO.class);
        return ActionResult.page(collect, paginationVO);
    }

    @Operation(summary = "获取列表表单配置JSON")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "type", description = "类型0-草稿，1-发布"),
    })
    @GetMapping("/{modelId}/Config")
    public ActionResult getData(@PathVariable("modelId") String modelId, @RequestParam(value = "type", required = false) String type) throws WorkFlowException {
        StpUtil.checkPermissionOr(modelId, "onlineDev.webDesign", "generator.webForm", "generator.flowForm");

        VisualdevEntity entity;
        //线上版本
        if ("0".equals(type)) {
            entity = visualdevService.getInfo(modelId);
        } else {
            VisualdevReleaseEntity releaseEntity = visualdevReleaseService.getById(modelId);
            entity = JsonUtil.getJsonToBean(releaseEntity, VisualdevEntity.class);
        }
        if (entity == null) {
            return ActionResult.fail("该表单已删除");
        }
        String s = VisualUtil.checkPublishVisualModel(entity, "预览");
        if (s != null) {
            return ActionResult.fail(s);
        }

        DataInfoVO vo = JsonUtil.getJsonToBean(entity, DataInfoVO.class);
        if (entity.getEnableFlow() == 1) {
            FlowFormEntity byId = flowFormService.getById(entity.getId());
            FlowTemplateInfoVO templateInfo = flowTemplateService.info(byId.getFlowId());
            if (templateInfo == null) {
                return ActionResult.fail(MsgCode.VS403.get());
            }
            if (Objects.equals(OnlineDevData.STATE_DISABLE, templateInfo.getEnabledMark())) {
                return ActionResult.fail(MsgCode.VS406.get());
            }
            vo.setFlowId(templateInfo.getId());
        }

        //处理默认值
        Map<String, Integer> havaDefaultCurrentValue = new HashMap<String, Integer>();
        UserInfo userInfo = userProvider.get();
        if (StringUtil.isNotEmpty(vo.getFormData())) {
            vo.setFormData(setDefaultCurrentValue(vo.getFormData(), havaDefaultCurrentValue, userInfo));
        }
        if (StringUtil.isNotEmpty(vo.getColumnData())) {
            vo.setColumnData(setDefaultCurrentValue(vo.getColumnData(), havaDefaultCurrentValue, userInfo));
        }
        if (StringUtil.isNotEmpty(vo.getAppColumnData())) {
            vo.setAppColumnData(setDefaultCurrentValue(vo.getAppColumnData(), havaDefaultCurrentValue, userInfo));
        }
        return ActionResult.success(vo);
    }

    //递归处理默认当前配置
    private String setDefaultCurrentValue(String configJson, Map<String, Integer> havaDefaultCurrentValue, UserInfo userInfo) {
        if (StringUtil.isEmpty(configJson)) {
            return configJson;
        }
        Map<String, Object> configJsonMap = JsonUtil.stringToMap(configJson.trim());
        if (configJsonMap == null && configJsonMap.isEmpty()) {
            return configJson;
        }
        int isChange = 0;
        List<String> userId = new ArrayList() {{
            add(userInfo.getUserId());
        }};
        List<UserRelationEntity> userRelationList = userRelationService.getListByUserIdAll(userId);

        //处理字段
        Object fieldsObj = configJsonMap.get("fields");
        List<Map<String, Object>> fieldsList = null;
        if (fieldsObj != null) {
            fieldsList = (List<Map<String, Object>>) fieldsObj;
            if (fieldsList != null && !fieldsList.isEmpty()) {
                setDefaultCurrentValue(userRelationList, fieldsList, userInfo, "add");
                configJsonMap.put("fields", fieldsList);
                isChange = 1;
            }
        }
        //处理查询条件
        Object searchObj = configJsonMap.get("searchList");
        List<Map<String, Object>> searchList = null;
        if (searchObj != null) {
            searchList = (List<Map<String, Object>>) searchObj;
            if (searchList != null && !searchList.isEmpty()) {
                setDefaultCurrentValue(userRelationList, searchList, userInfo, "search");
                configJsonMap.put("searchList", searchList);
                isChange = 1;
            }
        }

        //处理查询条件
        Object columnListObj = configJsonMap.get("columnList");
        List<Map<String, Object>> columnList = null;
        if (columnListObj != null) {
            columnList = (List<Map<String, Object>>) columnListObj;
            if (columnList != null && !columnList.isEmpty()) {
                setDefaultCurrentValue(userRelationList, columnList, userInfo, "add");
                configJsonMap.put("columnList", columnList);
                isChange = 1;
            }
        }

        if (isChange == 1) {
            return JsonUtil.getObjectToString(configJsonMap);
        } else {
            return configJson;
        }
    }

    private void setDefaultCurrentValue(List<UserRelationEntity> userRelationList, List<Map<String, Object>> itemList, UserInfo userInfo, String parseFlag) {
        for (int i = 0, len = itemList.size(); i < len; i++) {
            Map<String, Object> itemMap = itemList.get(i);
            if (itemMap == null || itemMap.isEmpty()) {
                continue;
            }
            Map<String, Object> configMap = (Map<String, Object>) itemMap.get("__config__");
            if (configMap == null || configMap.isEmpty()) {
                continue;
            }
            List<Map<String, Object>> childrenList = (List<Map<String, Object>>) configMap.get("children");
            if (childrenList != null && !childrenList.isEmpty()) {
                setDefaultCurrentValue(userRelationList, childrenList, userInfo, parseFlag);
                configMap = (Map<String, Object>) itemMap.get("__config__");
            }
            String jnpfKey = (String) configMap.get("jnpfKey");
            String defaultCurrent = String.valueOf(configMap.get("defaultCurrent"));
            if ("true".equals(defaultCurrent)) {
                Map<String, List<UserRelationEntity>> relationMap = userRelationList.stream().collect(Collectors.groupingBy(UserRelationEntity::getObjectType));
                Object data = "";
                switch (jnpfKey) {
                    case JnpfKeyConsts.COMSELECT:
                        data = new ArrayList() {{
                            add(userInfo.getOrganizeId());
                        }};
                        break;
                    case JnpfKeyConsts.DEPSELECT:
                        data = userInfo.getDepartmentId();
                        break;
                    case JnpfKeyConsts.POSSELECT:
                        data = userInfo.getPositionIds() != null && userInfo.getPositionIds().length > 0 ? userInfo.getPositionIds()[0] : "";
                        break;
                    case JnpfKeyConsts.USERSELECT:
                    case JnpfKeyConsts.CUSTOMUSERSELECT:
                        data = JnpfKeyConsts.CUSTOMUSERSELECT.equals(jnpfKey) ? userInfo.getUserId() + "--" + PermissionConst.USER : userInfo.getUserId();
                        break;
                    case JnpfKeyConsts.ROLESELECT:
                        List<UserRelationEntity> roleList = relationMap.get(PermissionConst.ROLE) != null ? relationMap.get(PermissionConst.ROLE) : new ArrayList<>();
                        data = roleList.size() > 0 ? roleList.get(0).getObjectId() : "";
                        break;
                    case JnpfKeyConsts.GROUPSELECT:
                        List<UserRelationEntity> groupList = relationMap.get(PermissionConst.GROUP) != null ? relationMap.get(PermissionConst.GROUP) : new ArrayList<>();
                        data = groupList.size() > 0 ? groupList.get(0).getObjectId() : "";
                        break;
                    default:
                        break;
                }
                List<Object> list = new ArrayList<>();
                list.add(data);
                if ("search".equals(parseFlag)) {
                    String searchMultiple = String.valueOf(itemMap.get("searchMultiple"));
                    if ("true".equals(searchMultiple)) {
                        configMap.put("defaultValue", list);
                    } else {
                        configMap.put("defaultValue", data);
                    }
                } else {
                    String multiple = String.valueOf(itemMap.get("multiple"));
                    if ("true".equals(multiple)) {
                        configMap.put("defaultValue", list);
                    } else {
                        configMap.put("defaultValue", data);
                    }
                }
                itemMap.put("__config__", configMap);
                itemList.set(i, itemMap);
            }
        }
    }


    @Operation(summary = "获取列表配置JSON")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
    })
    @GetMapping("/{modelId}/ColumnData")
    public ActionResult getColumnData(@PathVariable("modelId") String modelId) {
        StpUtil.checkPermission(modelId);

        VisualdevEntity entity = visualdevService.getInfo(modelId);
        FormDataInfoVO vo = JsonUtil.getJsonToBean(entity, FormDataInfoVO.class);
        return ActionResult.success(vo);
    }


    @Operation(summary = "获取表单配置JSON")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
    })
    @GetMapping("/{modelId}/FormData")
    public ActionResult<ColumnDataInfoVO> getFormData(@PathVariable("modelId") String modelId) {
        StpUtil.checkPermission(modelId);

        VisualdevEntity entity = visualdevService.getInfo(modelId);
        ColumnDataInfoVO vo = JsonUtil.getJsonToBean(entity, ColumnDataInfoVO.class);
        return ActionResult.success(vo);
    }

    @Operation(summary = "获取数据信息")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
    })
    @GetMapping("/{modelId}/{id}")
    public ActionResult info(@PathVariable("id") String id, @PathVariable("modelId") String modelId) throws DataException {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        //有表
        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            VisualdevModelDataInfoVO editDataInfo = visualDevInfoService.getEditDataInfo(id, visualdevEntity);
            return ActionResult.success(editDataInfo);
        }
        //无表
        VisualdevModelDataEntity entity = visualdevModelDataService.getInfo(id);
        Map<String, Object> formData = JsonUtil.stringToMap(visualdevEntity.getFormData());
        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formData.get("fields").toString(), FieLdsModel.class);
        //去除模板多级控件
        modelList = VisualUtils.deleteMore(modelList);
        String data = AutoFeildsUtil.autoFeilds(modelList, entity.getData());
        entity.setData(data);
        VisualdevModelDataInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, VisualdevModelDataInfoVO.class);
        return ActionResult.success(vo);
    }

    @Operation(summary = "获取数据信息(带转换数据)")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "id", description = "数据id"),
    })
    @GetMapping("/{modelId}/{id}/DataChange")
    public ActionResult infoWithDataChange(@PathVariable("modelId") String modelId, @PathVariable("id") String id) throws DataException, ParseException, IOException, SQLException {
        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        //有表
        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            VisualdevModelDataInfoVO vo = visualDevInfoService.getDetailsDataInfo(id, visualdevEntity);
            return ActionResult.success(vo);
        }
        //无表
        VisualdevModelDataInfoVO vo = visualdevModelDataService.infoDataChange(id, visualdevEntity);
        return ActionResult.success(vo);
    }

    @Operation(summary = "添加数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "visualdevModelDataCrForm", description = "功能数据创建表单"),
    })
    @PostMapping("/{modelId}")
    public ActionResult create(@PathVariable("modelId") String modelId, @RequestBody VisualdevModelDataCrForm visualdevModelDataCrForm) throws Exception {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        Map<String, Object> map = JsonUtil.stringToMap(visualdevModelDataCrForm.getData());
        DataModel dataModel = visualdevModelDataService.visualCreate(visualdevEntity, map);
        List<String> idList = new ArrayList() {{
            add(dataModel.getMainId());
        }};
        integrateUtil.dataAsyncList(modelId, 1, idList, UserProvider.getUser());
        return ActionResult.success(MsgCode.SU001.get());
    }


    @Operation(summary = "修改数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "id", description = "数据id"),
            @Parameter(name = "visualdevModelDataUpForm", description = "功能数据修改表单"),
    })
    @PutMapping("/{modelId}/{id}")
    public ActionResult update(@PathVariable("id") String id, @PathVariable("modelId") String modelId, @RequestBody VisualdevModelDataUpForm visualdevModelDataUpForm) throws Exception {
        StpUtil.checkPermission(modelId);
        Map<String, Object> data = JsonUtil.stringToMap(visualdevModelDataUpForm.getData());
        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        visualdevModelDataService.visualUpdate(visualdevEntity, data, id);
        //todo 调用
        List<String> idList = new ArrayList() {{
            add(id);
        }};
        integrateUtil.dataAsyncList(modelId, 2, idList, UserProvider.getUser());
        return ActionResult.success(MsgCode.SU004.get());
    }


    @Operation(summary = "删除数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "id", description = "数据id"),
    })
    @DeleteMapping("/{modelId}/{id}")
    public ActionResult delete(@PathVariable("id") String id, @PathVariable("modelId") String modelId) throws Exception {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);

        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);

        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }

        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(id);
            if (taskEntity != null && Objects.equals(visualdevEntity.getEnableFlow(),1)) {
                if (!"0".equals(taskEntity.getParentId()) || !(taskEntity.getStatus().equals(0) || taskEntity.getStatus().equals(4))) {
                    return ActionResult.fail(taskEntity.getFullName() + "不能删除");
                }
                if (taskEntity.getStatus().equals(0) || taskEntity.getStatus().equals(4)) {
                    flowTaskService.delete(taskEntity);
                }
            }
            //树形递归删除
            if (OnlineDevData.TYPE_FIVE_COLUMNDATA.equals(visualJsonModel.getColumnData().getType())) {
                try {
                    ActionResult listTreeAction = listTree(modelId, new PaginationModel(), id);
                    if (listTreeAction != null && listTreeAction.getCode() == 200 && listTreeAction.getData() instanceof Object) {
                        Map map = JsonUtil.getJsonToBean(listTreeAction.getData(), Map.class);
                        List<Map<String, Object>> list = JsonUtil.getJsonToListMap(map.get("list").toString());
                        if (list.size() > 0) {
                            for (Map<String, Object> item : list) {
                                this.delete(item.get("id").toString(), modelId);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("子数据删除异常:{}", e.getMessage());
                }
            }
            //todo 调用
            List<String> dataId = new ArrayList() {{
                add(id);
            }};
            List<VisualdevModelDataInfoVO> dataInfoVOList = integrateUtil.dataList(modelId, 3, dataId);
            boolean result = visualdevModelDataService.tableDelete(id, visualJsonModel);
            if (result) {
                integrateUtil.deleteDataList(dataInfoVOList, dataId, UserProvider.getUser());
                return ActionResult.success(MsgCode.SU003.get());
            } else {
                return ActionResult.fail(MsgCode.FA003.get());
            }
        }
        VisualdevModelDataEntity entity = visualdevModelDataService.getInfo(id);
        if (entity != null) {
            visualdevModelDataService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    @Operation(summary = "批量删除数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "idsVo", description = "批量处理参数"),
    })
    @PostMapping("/batchDelete/{modelId}")
    public ActionResult batchDelete(@RequestBody BatchRemoveIdsVo idsVo, @PathVariable("modelId") String modelId) throws Exception {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);

        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);

        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }

        List<String> idsList = new ArrayList<>();
        List<String> idsVoList = Arrays.asList(idsVo.getIds());
        String errMess = "";
        if (visualdevEntity.getEnableFlow() == 1) {
            for (String id : idsVoList) {
                FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(id);
                if (taskEntity != null) {
                    if (taskEntity.getStatus().equals(0) || taskEntity.getStatus().equals(4)) {
                        try {
                            flowTaskService.delete(taskEntity);
                            idsList.add(id);
                        } catch (Exception e) {
                            errMess = e.getMessage();
                        }
                    } else {
                        errMess = "该流程已发起，无法删除";
                    }
                } else {
                    idsList.add(id);
                }
            }
        } else {
            idsList = idsVoList;
        }
        if (idsList.size() == 0) {
            return ActionResult.fail(errMess);
        }
        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            //todo 调用
            List<VisualdevModelDataInfoVO> dataInfoVOList = integrateUtil.dataList(modelId, 3, idsList);
            ActionResult result = visualdevModelDataService.tableDeleteMore(idsList, visualJsonModel);
            integrateUtil.deleteDataList(dataInfoVOList, visualJsonModel.getDataIdList(), UserProvider.getUser());
            return result;
        }
        if (visualdevModelDataService.removeByIds(idsList)) {
            return ActionResult.success(MsgCode.SU003.get());
        } else if (visualdevEntity.getEnableFlow() == 1 && idsList.size() > 0) {
            //分组页面
            return ActionResult.fail("该流程已发起，无法删除");
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }


    @Operation(summary = "导入数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "visualImportModel", description = "导入参数"),
    })
    @PostMapping("{modelId}/ImportData")
    public ActionResult<ExcelImportModel> imports(@PathVariable("modelId") String modelId, @RequestBody VisualImportModel visualImportModel) throws WorkFlowException {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
        FormDataModel formData = visualJsonModel.getFormData();
        List<FieLdsModel> fieldsModelList = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FieLdsModel> allFieLds = new ArrayList<>();
        VisualUtils.recursionFields(fieldsModelList, allFieLds);
        visualJsonModel.setFormListModels(allFieLds);
        visualJsonModel.setFlowId(visualImportModel.getFlowId());
        //复杂表头数据 还原成普通数据
        List<Map<String, Object>> mapList = VisualUtils.complexImportsDataOnline(visualImportModel.getList(), visualdevEntity);
        ExcelImportModel excelData = onlineSwapDataUtils.createExcelData(mapList, visualJsonModel, visualdevEntity);
        List<VisualdevModelDataInfoVO> dataInfoList = excelData.getDataInfoList();
        List<String> addIdList = new ArrayList<>();
        List<String> updateIdList = new ArrayList<>();
        for (VisualdevModelDataInfoVO dataInfoVO : dataInfoList) {
            Integer trigger = StringUtil.isEmpty(dataInfoVO.getIntegrateId()) ? 1 : 2;
            if (Objects.equals(trigger, 1)) {
                addIdList.add(dataInfoVO.getId());
            } else {
                updateIdList.add(dataInfoVO.getId());
            }
        }
        integrateUtil.dataAsyncList(modelId, 1, addIdList, UserProvider.getUser());
        integrateUtil.dataAsyncList(modelId, 2, updateIdList, UserProvider.getUser());
        //复杂表头-表头和数据处理
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
        List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
        if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
            List<Map<String, Object>> mapList1 = VisualUtils.complexHeaderDataHandel(excelData.getFailResult(), complexHeaderList, false);
            excelData.setFailResult(mapList1);
        }
        return ActionResult.success(excelData);
    }

    @Operation(summary = "导入")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
    })
    @PostMapping("/Model/{modelId}/Actions/ImportData")
    public ActionResult imports(@PathVariable("modelId") String modelId) {
        StpUtil.checkPermission(modelId);

        VisualdevModelDataEntity entity = visualdevModelDataService.getInfo(modelId);
        List<MultipartFile> list = UpUtil.getFileAll();
        MultipartFile file = list.get(0);
        if (file.getOriginalFilename().contains(".xlsx")) {
            String filePath = configValueUtil.getTemporaryFilePath();
            String fileName = RandomUtil.uuId() + "." + UpUtil.getFileType(file);
            //保存文件
            FileUtil.upFile(file, filePath, fileName);
            File temporary = new File(XSSEscape.escapePath(filePath + fileName));
            return ActionResult.success(MsgCode.IMP001.get());
        } else {
            return ActionResult.fail("选择文件不符合导入");
        }
    }

    @Operation(summary = "导出")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "paginationModelExport", description = "导出参数"),
    })
    @PostMapping("/{modelId}/Actions/ExportData")
    public ActionResult export(@PathVariable("modelId") String modelId, @RequestBody PaginationModelExport paginationModelExport) throws ParseException, IOException, SQLException, DataException {
        StpUtil.checkPermission(modelId);

        ModuleEntity menuInfo = moduleService.getInfo(paginationModelExport.getMenuId());

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);

        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }

        String[] keys = paginationModelExport.getSelectKey();
        List<String> selectIds = Arrays.asList(paginationModelExport.getSelectIds());
        //关键字过滤
        List<Map<String, Object>> realList;
        DownloadVO vo;
        if (VisualWebTypeEnum.DATA_VIEW.getType().equals(visualdevEntity.getWebType())) {//视图查询数据
            VisualdevReleaseEntity visualdevREntity = JsonUtil.getJsonToBean(visualdevEntity, VisualdevReleaseEntity.class);
            realList = onlineSwapDataUtils.getInterfaceData(visualdevREntity, paginationModelExport, visualJsonModel.getColumnData());
            realList = "2".equals(paginationModelExport.getDataType()) ? realList.stream().filter(t->selectIds.contains(t.get("id"))).collect(Collectors.toList()):realList;
            vo = VisualUtils.createModelExcelServiceData(visualdevEntity, realList, Arrays.asList(keys), "表单信息", menuInfo.getFullName());
        } else {
            realList = visualdevModelDataService.exportData(keys, paginationModelExport, visualJsonModel);
            realList = "2".equals(paginationModelExport.getDataType()) ? realList.stream().filter(t->selectIds.contains(t.get("id"))).collect(Collectors.toList()):realList;
            vo = VisualUtils.createModelExcel(visualdevEntity, realList, Arrays.asList(keys), "表单信息", menuInfo.getFullName());
        }
        return ActionResult.success(vo);
    }

    @Operation(summary = "功能导出")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
    })
    @PostMapping("/{modelId}/Actions/Export")
    @SaCheckPermission("onlineDev.webDesign")
    public ActionResult exportData(@PathVariable("modelId") String modelId) {
        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        BaseDevModelVO vo = JsonUtil.getJsonToBean(visualdevEntity, BaseDevModelVO.class);
        vo.setModelType(ExportModelTypeEnum.Design.getMessage());
        DownloadVO downloadVO = fileExport.exportFile(vo, configValueUtil.getTemporaryFilePath(), visualdevEntity.getFullName(), ModuleTypeEnum.VISUAL_DEV.getTableName());
        return ActionResult.success(downloadVO);
    }

    @Operation(summary = "功能导入")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("onlineDev.webDesign")
    public ActionResult ImportData(@RequestParam("type") Integer type,@RequestPart("file") MultipartFile multipartFile) throws WorkFlowException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.VISUAL_DEV.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        BaseDevModelVO vo = JsonUtil.getJsonToBean(fileContent, BaseDevModelVO.class);
        if (vo.getModelType() == null || !vo.getModelType().equals(ExportModelTypeEnum.Design.getMessage())) {
            return ActionResult.fail("请导入对应功能的json文件");
        }
        VisualdevEntity visualdevEntity = JsonUtil.getJsonToBean(vo, VisualdevEntity.class);

        StringJoiner errList = new StringJoiner("、");
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        if (visualdevService.getInfo(visualdevEntity.getId()) != null) {
            if (Objects.equals(type, 0)) {
                errList.add("ID");
            } else {
                visualdevEntity.setId(RandomUtil.uuId());
            }
        }
        if (visualdevService.getObjByEncode(visualdevEntity.getEnCode(), visualdevEntity.getType()) > 0) {
            if (Objects.equals(type, 0)) {
                errList.add("编码");
            } else {
                visualdevEntity.setEnCode(visualdevEntity.getEnCode() + copyNum);
            }
        }
        if (visualdevService.getCountByName(visualdevEntity.getFullName(), visualdevEntity.getType()) > 0) {
            if (Objects.equals(type, 0)) {
                errList.add("名称");
            } else {
                visualdevEntity.setFullName(visualdevEntity.getFullName() + ".副本" + copyNum);
            }
        }
        if (Objects.equals(type, 0) && errList.length() > 0) {
            return ActionResult.fail(errList + "重复");
        }

        visualdevService.setIgnoreLogicDelete().removeById(visualdevEntity.getId());
        visualdevService.clearIgnoreLogicDelete();
        visualdevEntity.setId(RandomUtil.uuId());
        visualdevEntity.setCreatorTime(DateUtil.getNowDate());
        visualdevEntity.setCreatorUserId(userProvider.get().getUserId());
        visualdevEntity.setLastModifyTime(null);
        visualdevEntity.setLastModifyUserId(null);
        visualdevEntity.setDbLinkId("0");
        visualdevEntity.setState(0);
        visualdevService.save(visualdevEntity);
        // 启用流程 在表单新增一条 提供给流程使用
        if (Objects.equals(OnlineDevData.STATE_ENABLE, visualdevEntity.getEnableFlow()) && visualdevEntity.getType() < 3) {
            visualFlowFormUtil.saveLogicFlowAndForm(visualdevEntity);
        }
        return ActionResult.success(MsgCode.IMP001.get());
    }

    @Operation(summary = "模板下载")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
    })
    @GetMapping("/{modelId}/TemplateDownload")
    public ActionResult<DownloadVO> templateDownload(@PathVariable("modelId") String modelId) {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        FormDataModel formDataModel = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
        UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> allFieLds = new ArrayList<>();
        VisualUtils.recursionFields(fieLdsModels, allFieLds);
        List<String> selectKey = uploaderTemplateModel.getSelectKey();
        Map<String, Object> dataMap = new HashMap<>();
        //子表
        List<FieLdsModel> childFields = allFieLds.stream().filter(f -> f.getConfig().getJnpfKey().equals(JnpfKeyConsts.CHILD_TABLE)).collect(Collectors.toList());
        for (FieLdsModel child : childFields) {
            List<String> childList = selectKey.stream().filter(s -> s.startsWith(child.getVModel())).collect(Collectors.toList());
            childList.stream().forEach(c -> c.replace(child.getVModel() + "-", ""));
            List<FieLdsModel> children = child.getConfig().getChildren();
            List<Map<String, Object>> childData = new ArrayList<>();
            Map<String, Object> childMap = new HashMap<>();
            for (String cl : childList) {
                String substring = cl.substring(cl.indexOf("-") + 1);
                FieLdsModel fieLdsModel = children.stream().filter(c -> c.getVModel().equals(substring)).findFirst().orElse(null);
                childMap.put(substring, VisualUtils.exampleExcelMessage(fieLdsModel));
            }
            childData.add(childMap);
            dataMap.put(child.getVModel(), childData);
        }

        for (String s : selectKey.stream().filter(s -> !s.toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList())) {
            FieLdsModel fieLdsModel = allFieLds.stream().filter(c -> c.getVModel().equals(s)).findFirst().orElse(null);
            dataMap.put(s, VisualUtils.exampleExcelMessage(fieLdsModel));
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(dataMap);
        DownloadVO vo = VisualUtils.createModelExcel(visualdevEntity, dataList, selectKey, visualdevEntity.getFullName() + "模板",visualdevEntity.getFullName() + "模板");
        return ActionResult.success(vo);
    }

    @Operation(summary = "上传文件")
    @PostMapping("/Uploader")
    public ActionResult<Object> Uploader() {
        List<MultipartFile> list = UpUtil.getFileAll();
        MultipartFile file = list.get(0);
        if (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls")) {
            String filePath = XSSEscape.escape(configValueUtil.getTemporaryFilePath());
            String fileName = XSSEscape.escape(RandomUtil.uuId() + "." + UpUtil.getFileType(file));
            //上传文件
            FileInfo fileInfo = FileUploadUtils.uploadFile(file, filePath, fileName);
            DownloadVO vo = DownloadVO.builder().build();
            vo.setName(fileInfo.getFilename());
            return ActionResult.success(vo);
        } else {
            return ActionResult.fail("选择文件不符合导入");
        }
    }

    @Operation(summary = "导入预览")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "fileName", description = "文件名"),
    })
    @GetMapping("/{modelId}/ImportPreview")
    public ActionResult<Map<String, Object>> ImportPreview(@PathVariable("modelId") String modelId, String fileName) throws Exception {
        StpUtil.checkPermission(modelId);

        Map<String, Object> previewMap = null;
        try {
            VisualdevReleaseEntity entity = visualdevReleaseService.getById(modelId);
            ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class);
            FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
            UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
            List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
            List<FieLdsModel> allFields = new ArrayList<>();
            OnlinePublicUtils.recursionFormFields(allFields, fieLdsModels);

            List<String> selectKey = uploaderTemplateModel.getSelectKey();

            //子表tableField
            Set<String> tablefield1 = selectKey.stream().filter(s -> s.toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)).map(s -> s.substring(0, s.indexOf("-"))).collect(Collectors.toSet());

            String filePath = FileUploadUtils.getLocalBasePath() + configValueUtil.getTemporaryFilePath();
            FileUploadUtils.downLocal(configValueUtil.getTemporaryFilePath(), filePath, fileName);
            File temporary = new File(XSSEscape.escapePath(filePath + fileName));
            //判断有无子表
            String tablefield = selectKey.stream().filter(s -> s.toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)).findFirst().orElse(null);
            //有子表需要取第二行的表头
            Integer i = tablefield != null ? 2 : 1;
            //读取excel中数据
            List<Map> excelDataList = ExcelUtil.importExcel(temporary, 0, i, Map.class);
            //todo 备用方案，读取不到时间暂用此方法
            ExcelUtil.imoportExcelToMap(temporary, i, excelDataList);
            //取出导出选项中的子表字段label
            Map<String, Object> valueMap = new HashMap<>();
            for (FieLdsModel fieLdsModel : allFields) {
                String jnpfKey = fieLdsModel.getConfig().getJnpfKey();
                //子表
                if (JnpfKeyConsts.CHILD_TABLE.equals(jnpfKey)) {
                    List<FieLdsModel> children = fieLdsModel.getConfig().getChildren();
                    List<FieLdsModel> collect = children.stream().filter(c -> StringUtil.isNotEmpty(c.getVModel())).collect(Collectors.toList());
                    valueMap.put(fieLdsModel.getVModel(), collect.stream().collect(Collectors.toMap(FieLdsModel::getVModel, c -> c.getConfig().getLabel())));
                } else {
                    valueMap.put(fieLdsModel.getVModel(), fieLdsModel.getConfig().getLabel());
                }
            }
            //列表字段
            List<Map<String, Object>> columns = new ArrayList<>();
            List<ImportExcelFieldModel> chiImList = new ArrayList<>();
            List<ImportExcelFieldModel> allImList = new ArrayList<>();
            selectKey.stream().forEach(s -> {
                ImportExcelFieldModel importExcel = new ImportExcelFieldModel();
                if (s.toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)) {
                    String table = s.substring(0, s.indexOf("-"));
                    String field = s.substring(s.indexOf("-") + 1);
                    importExcel.setField(field);
                    importExcel.setTableField(table);
                    Map map = (Map) valueMap.get(table);
                    importExcel.setFullName(map.get(field).toString());
                    chiImList.add(importExcel);
                } else {
                    importExcel.setField(s);
                    importExcel.setFullName(valueMap.get(s).toString());
                    allImList.add(importExcel);
                }
            });
            Map<String, List<ImportExcelFieldModel>> groups = chiImList.stream().collect(Collectors.groupingBy(ImportExcelFieldModel::getTableField));

            for (Map.Entry<String, List<ImportExcelFieldModel>> entry : groups.entrySet()) {
                ImportExcelFieldModel importExcel = new ImportExcelFieldModel();

                List<ImportExcelFieldModel> value = entry.getValue();
                ImportExcelFieldModel im = value.get(0);
                FieLdsModel fieLdsModel = allFields.stream().filter(f -> entry.getKey().equals(f.getVModel())).findFirst().orElse(null);
                String tableName = fieLdsModel.getConfig().getLabel();
                importExcel.setField(entry.getKey());
                importExcel.setFullName(tableName);
                //            value.stream().forEach(im1->im1.setFullName(im1.getFullName().replace(tableName+"-","")));
                importExcel.setChildren(value);
                allImList.add(importExcel);
            }

            for (ImportExcelFieldModel importExcel : allImList) {
                Map<String, Object> selectMap = new HashMap<>(16);
                selectMap.put("id", importExcel.getField());
                selectMap.put("fullName", importExcel.getFullName());
                if (importExcel.getChildren() != null) {
                    List<ImportExcelFieldModel> children = importExcel.getChildren();
                    List<Map<String, Object>> childMapList = new ArrayList<>();
                    for (ImportExcelFieldModel childIm : children) {
                        Map<String, Object> childMap = new HashMap<>(16);
                        childMap.put("id", childIm.getField());
                        childMap.put("fullName", childIm.getFullName());
                        childMapList.add(childMap);
                    }
                    selectMap.put("children", childMapList);
                }
                columns.add(selectMap);
            }
            List<Map<String, Object>> allDataList = new ArrayList<>();

            for (int z = 0; z < excelDataList.size(); z++) {
                Map<String, Object> dataMap = new HashMap<>(16);
                Map m = excelDataList.get(z);
                List results = new ArrayList<>(m.entrySet());
                //取出的数据最后一行 不带行标签
                int resultsize = z == excelDataList.size() - 1 ? results.size() : m.containsKey("excelRowNum") ? results.size() - 1 : results.size();
                if (resultsize < selectKey.size()) {
                    throw new WorkFlowException(MsgCode.VS407.get());
                }
                for (int e = 0; e < resultsize; e++) {
                    Map.Entry o = (Map.Entry) results.get(e);
                    String entryKey = o.getKey().toString();
                    String substring = entryKey.substring(entryKey.lastIndexOf("(") + 1, entryKey.lastIndexOf(")"));
                    boolean contains = selectKey.contains(substring);
                    if (!contains) {
                        throw new WorkFlowException(MsgCode.VS407.get());
                    }
                    dataMap.put(substring, o.getValue());
                }
                allDataList.add(dataMap);
            }

            //存放在主表数据的下标位置
            List<Map<String, List<Map<String, Object>>>> IndexMap = new ArrayList<>();
//			Map<Integer, Map<String, List<Map<String, Object>>>> IndexMap = new TreeMap<>();
            Map<String, List<Map<String, Object>>> childrenTabMap = new HashMap<>();
            for (String tab : tablefield1) {
                childrenTabMap.put(tab, new ArrayList<>());
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (int t = 0; t < allDataList.size(); t++) {
                boolean isLast = t == allDataList.size() - 1;
                //是否是上条数据的子表
                boolean isTogetherWithUp = false;
                //是否需要合并
                boolean needTogether = true;
                //这条数据是否需要添加
                boolean needAdd = true;
                Map<String, Object> dataMap = allDataList.get(t);
                //首条数据不合并
                if (t > 0) {
                    List<Map.Entry<String, Object>> tablefield2 = dataMap.entrySet().stream().filter(e -> !e.getKey().toLowerCase().startsWith(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList());
                    //如果除子表外都为空 则需要合并
                    Map.Entry<String, Object> entry = tablefield2.stream().filter(ta -> ta.getValue() != null).findFirst().orElse(null);
                    if (entry == null) {
                        isTogetherWithUp = true;
                        needTogether = false;
                        needAdd = false;
                        if (isLast) {
                            needTogether = true;
                        }
                    }
                }

                //合并子表里的字段
                for (String tab : tablefield1) {
                    Map<String, Object> childObjMap = new HashMap<>(16);
                    //该条数据下的子表字段
                    List<Map.Entry<String, Object>> childList = dataMap.entrySet().stream().filter(e -> e.getKey().startsWith(tab)).collect(Collectors.toList());
                    for (Map.Entry<String, Object> entry : childList) {
                        String childFieldName = entry.getKey().replace(tab + "-", "");
                        childObjMap.put(childFieldName, entry.getValue());
                    }
                    List<Map<String, Object>> mapList = childrenTabMap.get(tab);
                    mapList.add(childObjMap);
                }
                if (needTogether && t != 0) {
                    Map<String, List<Map<String, Object>>> c = new HashMap<>(childrenTabMap);
                    Map<String, List<Map<String, Object>>> b = new HashMap<>();

                    for (String tab : tablefield1) {
                        //去掉最后一个 放到下条数据里
                        List<Map<String, Object>> mapList = c.get(tab);
                        Map<String, Object> map = mapList.get(mapList.size() - 1);
                        List<Map<String, Object>> aList = new ArrayList<>();
                        aList.add(map);
                        if (!isLast) {
                            mapList.remove(mapList.size() - 1);
                        }
                        childrenTabMap.put(tab, aList);
                        b.put(tab, mapList);
                    }
                    IndexMap.add(b);
                    if (isLast) {
                        IndexMap.add(childrenTabMap);
                    }
                } else {
                    if (isLast) {
                        IndexMap.add(childrenTabMap);
                    }
                }
                if (needAdd) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                        if (!entry.getKey().contains("-")) {
                            m.put(entry.getKey(), entry.getValue());
                        }
                    }
                    results.add(m);
                }
            }

            //处理结果
            for (int r = 0; r < results.size(); r++) {
                Map<String, List<Map<String, Object>>> entry = IndexMap.get(r);
                Map<String, Object> map = results.get(r);
                for (Map.Entry<String, List<Map<String, Object>>> entry1 : entry.entrySet()) {
                    String tableField = entry1.getKey();
                    Object tableField1 = map.get(tableField);
                    List<Map<String, Object>> value1 = entry1.getValue();
                    if (tableField1 != null) {
                        List<Map<String, Object>> tfMap = (List<Map<String, Object>>) tableField1;
                        value1.addAll(tfMap);
                    }
                    map.put(tableField, value1);
                }
                results.set(r, map);
            }

            previewMap = new HashMap<>();
            //复杂表头-表头和数据处理
            List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
            if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
                results = VisualUtils.complexHeaderDataHandel(results, complexHeaderList);
                columns = VisualUtils.complexHeaderHandelOnline(columns, complexHeaderList);
            }
            previewMap.put("dataRow", results);
            previewMap.put("headerRow", columns);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResult.fail(MsgCode.VS407.get());
        }
        return ActionResult.success(previewMap);
    }

    @Operation(summary = "导出异常报告")
    @Parameters({
            @Parameter(name = "modelId", description = "模板id"),
            @Parameter(name = "visualImportModel", description = "导出参数"),
    })
    @PostMapping("/{modelId}/ImportExceptionData")
    public ActionResult<DownloadVO> ImportExceptionData(@PathVariable("modelId") String modelId, @RequestBody VisualImportModel visualImportModel) {
        StpUtil.checkPermission(modelId);

        VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
        UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
        List<String> selectKey = uploaderTemplateModel.getSelectKey();
        DownloadVO vo = VisualUtils.createModelExcel(visualdevEntity, visualImportModel.getList(), selectKey, "错误报告","错误报告");
        return ActionResult.success(vo);
    }

    //以下是api接口
    /**
     * 获取转换数据的详情
     *
     * @param id
     * @param visualdevId
     * @return VisualdevModelDataInfoVO
     */
    
    @PostMapping("/getDetailsDataInfo/{id}/{visualdevId}" )
    public VisualdevModelDataInfoVO getDetailsDataInfo(@PathVariable("id" ) String id, @PathVariable("visualdevId" ) String visualdevId) {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(visualdevId);
        return visualDevInfoService.getDetailsDataInfo(id, visualdevEntity);
    }

    /**
     * 获取转换数据的详情
     *
     * @param id
     * @param visualdevId
     * @return VisualdevModelDataInfoVO
     */
    
    @PostMapping("/infoDataChange/{id}/{visualdevId}" )
    public VisualdevModelDataInfoVO infoDataChange(@PathVariable("id" ) String id, @PathVariable("visualdevId" ) String visualdevId) throws SQLException, DataException, ParseException, IOException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(visualdevId);
        return visualdevModelDataService.infoDataChange(id, visualdevEntity);
    }

    /**
     * 外链创建数据api
     *
     * @return VisualdevModelDataInfoVO
     */
    @NoDataSourceBind
    
    @PostMapping("/createData/{modelId}" )
    public ActionResult createData(@PathVariable("modelId" ) String modelId,
                                   @RequestParam(value = "tenantId",required = false ) String tenantId,
                                   @RequestBody VisualdevModelDataCrForm visualdevModelDataCrForm) throws WorkFlowException {
        try {
            if (configValueUtil.isMultiTenancy()) {
                if (StringUtil.isNotEmpty(tenantId)) {
                    //切换成租户库
                    TenantDataSourceUtil.switchTenant(tenantId);
                } else {
                    return ActionResult.fail("缺少租户信息" );
                }
            }
            VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
            Map<String, Object> map = JsonUtil.stringToMap(visualdevModelDataCrForm.getData());
            visualdevModelDataService.visualCreate(visualdevEntity, map, true);
        }catch (Exception e){
            throw new WorkFlowException(e.getMessage());
        }
        return ActionResult.success(MsgCode.SU001.get());
    }
}
