package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.InterfaceOauthEntity;
import jnpf.base.model.datainterface.DataConfigJsonModel;
import jnpf.base.model.datainterface.DataInterfaceActionModel;
import jnpf.base.model.datainterface.DataInterfaceCrForm;
import jnpf.base.model.datainterface.DataInterfaceGetListVO;
import jnpf.base.model.datainterface.DataInterfaceInvokeModel;
import jnpf.base.model.datainterface.DataInterfaceListVO;
import jnpf.base.model.datainterface.DataInterfaceModel;
import jnpf.base.model.datainterface.DataInterfacePage;
import jnpf.base.model.datainterface.DataInterfaceParamModel;
import jnpf.base.model.datainterface.DataInterfaceTreeModel;
import jnpf.base.model.datainterface.DataInterfaceTreeVO;
import jnpf.base.model.datainterface.DataInterfaceUpForm;
import jnpf.base.model.datainterface.DataInterfaceVo;
import jnpf.base.model.datainterface.PaginationDataInterface;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.service.InterfaceOauthService;
import jnpf.base.util.interfaceUtil.InterfaceUtil;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.DbSensitiveConstant;
import jnpf.constant.MsgCode;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.DataException;
import jnpf.util.DataFileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.NoDataSourceBind;
import jnpf.util.ParameterUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.enums.DictionaryDataEnum;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.TreeDotUtils;
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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * 数据接口
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-15 10:29
 */
@Tag(name = "数据接口", description = "DataInterface")
@RestController
@RequestMapping(value = "/api/system/DataInterface")
public class DataInterfaceController extends SuperController<DataInterfaceService, DataInterfaceEntity>  {
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private InterfaceOauthService interfaceOauthService;

    @Autowired
    private DataFileExport fileExport;

    /**
     * 获取接口列表(分页)
     *
     * @param pagination 分页模型
     * @return
     */
    @Operation(summary = "获取接口列表(分页)")
    @SaCheckPermission("systemData.dataInterface")
    @GetMapping
    public ActionResult<PageListVO<DataInterfaceListVO>> getList(PaginationDataInterface pagination) {
        List<DataInterfaceEntity> data = dataInterfaceService.getList(pagination, pagination.getType(), 0);
        List<DataInterfaceListVO> list = JsonUtil.getJsonToList(data, DataInterfaceListVO.class);
        // 添加tenantId字段
        for (DataInterfaceListVO vo : list) {
            // 类别转换
            if ("1".equals(vo.getType())) {
                vo.setType("SQL操作");
            } else if ("2".equals(vo.getType())) {
                vo.setType("静态数据");
            } else if ("3".equals(vo.getType())) {
                vo.setType("Service数据");
            }
            if (StringUtil.isNotEmpty(userProvider.get().getTenantId())) {
                vo.setTenantId(userProvider.get().getTenantId());
            }
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取接口列表(工作流选择时调用)
     *
     * @param pagination 分页参数
     * @return ignore
     */
    @Operation(summary = "获取接口列表(工作流选择时调用)")
    @GetMapping("/getList")
    public ActionResult<PageListVO<DataInterfaceGetListVO>> getLists(PaginationDataInterface pagination) {
        List<DataInterfaceEntity> data = dataInterfaceService.getList(pagination, pagination.getType(), 1);
        List<DataInterfaceGetListVO> list = JsonUtil.getJsonToList(data, DataInterfaceGetListVO.class);
        for (DataInterfaceGetListVO vo : list) {
            // 类别转换
            if ("1".equals(vo.getType())) {
                vo.setType("SQL操作");
            } else if ("2".equals(vo.getType())) {
                vo.setType("静态数据");
            } else if ("3".equals(vo.getType())) {
                vo.setType("Service数据");
            }
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取接口列表下拉框
     *
     * @return
     */
    @Operation(summary = "获取接口列表下拉框")
    @GetMapping("/Selector")
    public ActionResult<List<DataInterfaceTreeVO>> getSelector() {
        List<DataInterfaceTreeModel> tree = new ArrayList<>();
        List<DataInterfaceEntity> data = dataInterfaceService.getList(false);
        List<DictionaryDataEntity> dataEntityList = dictionaryDataService.getList(dictionaryTypeService.getInfoByEnCode(DictionaryDataEnum.SYSTEM_DATAINTERFACE.getDictionaryTypeId()).getId());
        // 获取数据接口外层菜单
        for (DictionaryDataEntity dictionaryDataEntity : dataEntityList) {
            DataInterfaceTreeModel firstModel = JsonUtil.getJsonToBean(dictionaryDataEntity, DataInterfaceTreeModel.class);
            firstModel.setId(dictionaryDataEntity.getId());
            firstModel.setCategory("0");
            long num = data.stream().filter(t -> t.getCategory().equals(dictionaryDataEntity.getId())).count();
            if (num > 0) {
                tree.add(firstModel);
            }
        }
        for (DataInterfaceEntity entity : data) {
            DataInterfaceTreeModel treeModel = JsonUtil.getJsonToBean(entity, DataInterfaceTreeModel.class);
            treeModel.setCategory("1");
            treeModel.setParentId(entity.getCategory());
            treeModel.setId(entity.getId());
            DictionaryDataEntity dataEntity = dictionaryDataService.getInfo(entity.getCategory());
            if (dataEntity != null) {
                tree.add(treeModel);
            }
        }
        List<SumTree<DataInterfaceTreeModel>> sumTrees = TreeDotUtils.convertListToTreeDot(tree);
        List<DataInterfaceTreeVO> list = JsonUtil.getJsonToList(sumTrees, DataInterfaceTreeVO.class);
        ListVO<DataInterfaceTreeVO> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(list);
    }

    /**
     * 获取接口参数列表下拉框
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "获取接口参数列表下拉框")
    @Parameter(name = "id", description = "主键", required = true)
    @SaCheckPermission("systemData.dataInterface")
    @GetMapping("/GetParam/{id}")
    public ActionResult<List<DataInterfaceModel>> getSelector(@PathVariable("id") String id) {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        if (entity!=null) {
            String parameterJson = entity.getParameterJson();
            List<DataInterfaceModel> jsonToList = JsonUtil.getJsonToList(parameterJson, DataInterfaceModel.class);
            return ActionResult.success(jsonToList == null ? new ArrayList<>() : jsonToList);
        }
        return ActionResult.fail("数据不存在");
    }

    /**
     * 获取接口数据
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取接口数据")
    @Parameter(name = "id", description = "主键", required = true)
    @SaCheckPermission("systemData.dataInterface")
    @GetMapping("/{id}")
    public ActionResult<DataInterfaceVo> getInfo(@PathVariable("id") String id) throws DataException {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        DataInterfaceVo vo = JsonUtil.getJsonToBean(entity, DataInterfaceVo.class);
        return ActionResult.success(vo);
    }

    /**
     * 添加接口
     *
     * @param dataInterfaceCrForm 实体模型
     * @return
     */
    @Operation(summary = "添加接口")
    @Parameter(name = "dataInterfaceCrForm", description = "实体模型", required = true)
    @SaCheckPermission("systemData.dataInterface")
    @PostMapping
    public ActionResult create(@RequestBody @Valid DataInterfaceCrForm dataInterfaceCrForm) throws DataException {
        DataInterfaceEntity entity = JsonUtil.getJsonToBean(dataInterfaceCrForm, DataInterfaceEntity.class);
        // 判断是否有敏感字
        String containsSensitive = containsSensitive(entity);
        if (StringUtil.isNotEmpty(containsSensitive)) {
            return ActionResult.fail("当前SQL含有敏感字：" + containsSensitive);
        }
        if (dataInterfaceService.isExistByFullNameOrEnCode(entity.getId(), entity.getFullName(), null)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (dataInterfaceService.isExistByFullNameOrEnCode(entity.getId(), null, entity.getEnCode())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        dataInterfaceService.create(entity);
        return ActionResult.success("接口创建成功");
    }

    /**
     * 判断是否有敏感字
     *
     * @param entity
     * @return
     */
    private String containsSensitive(DataInterfaceEntity entity) {
        // 判断是否有敏感字
        if (entity.getType() == 1 && (entity.getAction() != null && entity.getAction() == 3)) {
            DataConfigJsonModel dataConfigJsonModel = JsonUtil.getJsonToBean(entity.getDataConfigJson(), DataConfigJsonModel.class);
            String sql = dataConfigJsonModel.getSqlData().getSql();
            if (StringUtil.isNotEmpty(sql)) {
                return ParameterUtil.checkContainsSensitive(sql, DbSensitiveConstant.SENSITIVE);
            }
        }
        return "";
    }

    /**
     * 修改接口
     *
     * @param dataInterfaceUpForm 实体模型
     * @param id 主键
     * @return
     */
    @Operation(summary = "修改接口")
    @Parameters({
            @Parameter(name = "dataInterfaceUpForm", description = "实体模型", required = true),
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.dataInterface")
    @PutMapping("/{id}")
    public ActionResult update(@RequestBody @Valid DataInterfaceUpForm dataInterfaceUpForm, @PathVariable("id") String id) throws DataException {
        DataInterfaceEntity entity = JsonUtilEx.getJsonToBeanEx(dataInterfaceUpForm, DataInterfaceEntity.class);
        // 判断是否有敏感字
        String containsSensitive = containsSensitive(entity);
        if (StringUtil.isNotEmpty(containsSensitive)) {
            return ActionResult.fail("当前SQL含有敏感字：" + containsSensitive);
        }
        if (dataInterfaceService.isExistByFullNameOrEnCode(id, entity.getFullName(), null)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (dataInterfaceService.isExistByFullNameOrEnCode(id, null, entity.getEnCode())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        boolean flag = dataInterfaceService.update(entity, id);
        if (!flag) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除接口
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除接口")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.dataInterface")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable String id) {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        if (entity != null) {
            dataInterfaceService.delete(entity);
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail(MsgCode.FA001.get());
    }

    /**
     * 更新接口状态
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "更新接口状态")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.dataInterface")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) throws DataException {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        if (entity != null) {
            if (entity.getEnabledMark() == 0) {
                entity.setEnabledMark(1);
            } else {
                entity.setEnabledMark(0);
            }
            dataInterfaceService.update(entity, id);
            return ActionResult.success("更新接口状态成功");
        }
        return ActionResult.fail(MsgCode.FA001.get());
    }

    /**
     * 获取接口分页数据
     *
     * @param id 主键
     * @param page 分页参数
     * @return
     */
    
    @Operation(summary = "获取接口分页数据")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "page", description = "分页参数", required = true)
    })
    @PostMapping("/{id}/Actions/List")
    public ActionResult infoToIdPageList(@PathVariable("id") String id, @RequestBody DataInterfacePage page) {
        ActionResult result = dataInterfaceService.infoToIdPageList(id, page);
        return result;
    }

//    /**
//     * 获取接口详情数据
//     *
//     * @param id 主键
//     * @param page 分页参数
//     * @return
//     */
//    @Operation(summary = "获取接口详情数据")
//    @Parameters({
//            @Parameter(name = "id", description = "主键", required = true)
//    })
//    @GetMapping("/{id}/Actions/Info")
//    public ActionResult info(@PathVariable("id") String id, DataInterfacePage page) {
//        Map<String, Object> data = dataInterfaceService.infoToInfo(id, page);
//        return ActionResult.success(data);
//    }

    /**
     * 获取接口详情数据
     *
     * @param id 主键
     * @param page 分页参数
     * @return
     */
    
    @Operation(summary = "获取接口详情数据")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "page", description = "分页参数", required = true)
    })
    @PostMapping("/{id}/Actions/InfoByIds")
    public ActionResult<List<Map<String, Object>>> infoByIds(@PathVariable("id") String id, @RequestBody DataInterfacePage page) {
        List<Map<String, Object>> data = dataInterfaceService.infoToInfo(id, page);
        return ActionResult.success(data);
    }

    
    @PostMapping("/getInterfaceList")
    public List<DataInterfaceEntity> getInterfaceList(@RequestBody List<String> id) {
        return dataInterfaceService.getList(id);
    }

//    /**
//     * 预览
//     *
//     * @param id 主键
//     * @return
//     */
//    @Operation(summary = "预览")
//    @Parameters({
//            @Parameter(name = "id", description = "主键", required = true)
//    })
////    @SaCheckPermission("systemData.dataInterface")
//    @GetMapping("/{id}/Preview")
//    public ActionResult Actions(@PathVariable("id") String id) {
//        String escape = XSSEscape.escape(id);
//        Object preview = dataInterfaceService.preview(escape);
//        if (preview instanceof JSONObject) {
//            JSONObject jsonObject = (JSONObject) preview;
//            if (Objects.nonNull(jsonObject) && "1".equals(jsonObject.get("errorCode"))) {
//                return ActionResult.fail(String.valueOf(jsonObject.get("errorMsg")));
//            }
//        }
//        return ActionResult.success(preview);
//    }

    /**
     * 测试接口
     *
     * @param id 主键
     * @param objectMap 参数、参数值对象
     * @return
     */
    @Operation(summary = "测试接口")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "objectMap", description = "参数、参数值对象")
    })
    @PostMapping("/{id}/Actions/Preview")
    @NoDataSourceBind
    public ActionResult callPreview(@PathVariable("id") String id, @RequestBody(required = false) Map<String, Object> objectMap) {
        DataInterfaceParamModel model = JsonUtil.getJsonToBean(objectMap, DataInterfaceParamModel.class);
        Map<String, String> map = null;
        if (model != null) {
            if (configValueUtil.isMultiTenancy()) {
                //切换成租户库
                try{
                    TenantDataSourceUtil.switchTenant(model.getTenantId());
                }catch (Exception e){
                    return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
                }
            }
            if (model.getParamList() != null && model.getParamList().size() > 0) {
                map = new HashMap<>(16);
                List<DataInterfaceModel> jsonToList = JsonUtil.getJsonToList(model.getParamList(), DataInterfaceModel.class);
                for (DataInterfaceModel dataInterfaceModel : jsonToList) {
                    map.put(dataInterfaceModel.getField(), dataInterfaceModel.getDefaultValue());
                }
            }
        }
        ActionResult actionResult = dataInterfaceService.infoToId(id, null, map);
        if (actionResult.getCode() == 200) {
            actionResult.setMsg("接口请求成功");
        }
        return actionResult;
    }

    /**
     * 访问接口GET
     *
     * @param id 主键
     * @param map 参数、参数值对象
     * @return
     */
    @Operation(summary = "访问接口GET")
    @GetMapping("/{id}/Actions/Response")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "map", description = "参数、参数值对象")
    })
    @NoDataSourceBind
    public ActionResult getResponse(@PathVariable("id") String id,@RequestParam(required = false) Map<String,String> map) {
        DataInterfaceActionModel entity;
        try{
            entity= dataInterfaceService.checkParams(map);
            entity.setInvokType("GET");
        }catch (Exception e){
            return ActionResult.fail(e.getMessage());
        }
        String name = null;
        if (configValueUtil.isMultiTenancy()) {
            //切换成租户库
            try{
                TenantDataSourceUtil.switchTenant(entity.getTenantId());
            }catch (Exception e){
                return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
            }
        }
        return dataInterfaceService.infoToIdNew(id, name, entity);
    }

    /**
     * 访问接口POST
     *
     * @param id 主键
     * @param map 参数、参数值对象
     * @return
     */
    @Operation(summary = "访问接口POST")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "map", description = "参数、参数值对象")
    })
    @PostMapping("/{id}/Actions/Response")
    @NoDataSourceBind
    public ActionResult postResponse(@PathVariable("id") String id, @RequestBody(required = false) Map<String,String> map) {
        DataInterfaceActionModel entity;
        try{
            entity= dataInterfaceService.checkParams(map);
            entity.setInvokType("POST");
        }catch (Exception e){
            return ActionResult.fail(e.getMessage());
        }
        String name = null;
        if (configValueUtil.isMultiTenancy()) {
            //切换成租户库
            try{
                TenantDataSourceUtil.switchTenant(entity.getTenantId());
            }catch (Exception e){
                return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
            }
        }
        return dataInterfaceService.infoToIdNew(id, name, entity);
    }

    /**
     * 外部接口获取authorization
     *
     * @param appId 应用id
     * @param intefaceId 接口id
     * @param map 参数、参数值对象
     * @return
     */
    @Operation(summary = "外部接口获取authorization")
    @Parameters({
            @Parameter(name = "appId", description = "应用id", required = true),
            @Parameter(name = "intefaceId", description = "接口id"),
            @Parameter(name = "map", description = "参数、参数值对象")
    })
    @PostMapping("/Actions/GetAuth")
    @NoDataSourceBind
    public ActionResult getAuthorization(@RequestParam("appId") String appId,@RequestParam("intefaceId") String intefaceId, @RequestBody(required = false) Map<String,String> map) {
        InterfaceOauthEntity infoByAppId = interfaceOauthService.getInfoByAppId(appId);
        if(infoByAppId==null){
            return ActionResult.fail("appId参数错误");
        }
        Map<String, String> authorization = InterfaceUtil.getAuthorization(intefaceId,appId,infoByAppId.getAppSecret(), map);
        return ActionResult.success(MsgCode.SU005.get(),authorization);
    }

    
    @GetMapping("/getDataInterfaceInfo")
    @NoDataSourceBind
    public DataInterfaceEntity getDataInterfaceInfo(@RequestParam("id") String id, @RequestParam("tenantId") String tenantId) {
        // 判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            //切换成租户库
            try{
                TenantDataSourceUtil.switchTenant(tenantId);
            }catch (Exception e){
                throw new RuntimeException("切换租户失败");
            }
        }
        return dataInterfaceService.getInfo(id);
    }

    
    @PostMapping("/infoToIdById/{id}")
    public ActionResult infoToIdById(@PathVariable("id") String id, @RequestBody Map<String, String> parameterMap) {
        return dataInterfaceService.infoToId(id,null, parameterMap);
    }

    
    @PostMapping("/infoToId/{id}")
    public ActionResult infoToId(@PathVariable("id") String id) {
        return dataInterfaceService.infoToId(id,null,null);
    }

    
    @NoDataSourceBind
    @PostMapping("/invokeById")
    public ActionResult invokeById(@RequestBody DataInterfaceInvokeModel dataInterfaceInvokeModel) {
        // 判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            //切换成租户库
            try{
                TenantDataSourceUtil.switchTenant(dataInterfaceInvokeModel.getTenantId());
            }catch (Exception e){
                return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
            }
        }
        return dataInterfaceService.infoToId(dataInterfaceInvokeModel.getId(),dataInterfaceInvokeModel.getTenantId(),
                dataInterfaceInvokeModel.getMap(), dataInterfaceInvokeModel.getToken(),
                null ,null, dataInterfaceInvokeModel.getPagination(), dataInterfaceInvokeModel.getShowMap());
    }

    /**
     * 数据接口导出功能
     *
     * @param id 接口id
     */
    @Operation(summary = "导出数据接口数据")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("systemData.dataInterface")
    @GetMapping("/{id}/Actions/Export")
    public ActionResult exportFile(@PathVariable("id") String id) {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.SYSTEM_DATAINTEFASE.getTableName());
        return ActionResult.success(downloadVO);
    }

    /**
     * 数据接口导入功能
     *
     * @param multipartFile
     * @return
     * @throws DataException
     */
    @Operation(summary = "数据接口导入功能")
    @SaCheckPermission("systemData.dataInterface")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importFile(@RequestPart("file") MultipartFile multipartFile,
                                   @RequestParam("type") Integer type) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.SYSTEM_DATAINTEFASE.getTableName())) {
            return ActionResult.fail("导入文件格式错误");
        }
        try {
            //读取文件内容
            String fileContent = FileUtil.getFileContent(multipartFile);
            DataInterfaceEntity entity = JsonUtil.getJsonToBean(fileContent, DataInterfaceEntity.class);
            // 验证数据是否正常
            if (dictionaryDataService.getInfo(entity.getCategory()) == null) {
                return ActionResult.fail(MsgCode.IMP004.get());
            }
            StringJoiner stringJoiner = new StringJoiner("、");
            QueryWrapper<DataInterfaceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DataInterfaceEntity::getId, entity.getId());
            if (dataInterfaceService.count(queryWrapper) > 0) {
                stringJoiner.add("ID");
            }
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DataInterfaceEntity::getEnCode, entity.getEnCode());
            if (dataInterfaceService.count(queryWrapper) > 0) {
                stringJoiner.add("编码");
            }
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DataInterfaceEntity::getFullName, entity.getFullName());
            if (dataInterfaceService.count(queryWrapper) > 0) {
                stringJoiner.add("名称");
            }
            if (stringJoiner.length() > 0 && ObjectUtil.equal(type, 1)) {
                String copyNum = UUID.randomUUID().toString().substring(0, 5);
                entity.setFullName(entity.getFullName() + ".副本" + copyNum);
                entity.setEnCode(entity.getEnCode() + copyNum);
            } else if (ObjectUtil.equal(type, 0) && stringJoiner.length() > 0) {
                return ActionResult.fail(stringJoiner.toString() + "重复");
            }
            entity.setCreatorTime(new Date());
            entity.setCreatorUserId(UserProvider.getLoginUserId());
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            entity.setId(RandomUtil.uuId());
            try {
                dataInterfaceService.setIgnoreLogicDelete().removeById(entity);
                dataInterfaceService.setIgnoreLogicDelete().saveOrUpdate(entity);
            } catch (Exception e) {
                throw new DataException(MsgCode.IMP003.get());
            }finally {
                dataInterfaceService.clearIgnoreLogicDelete();
            }
            return ActionResult.success(MsgCode.IMP001.get());
        } catch (Exception e) {
            return ActionResult.fail(MsgCode.IMP004.get());
        }
    }


    /**
     * 获取接口字段
     *
     * @param id 主键
     * @param objectMap 参数、参数值
     * @return
     */
    @Operation(summary = "获取接口字段")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "objectMap", description = "参数、参数值")
    })
    @PostMapping("/{id}/Actions/GetFields")
    public ActionResult getFields(@PathVariable("id") String id, @RequestBody(required = false) Map<String, Object> objectMap) {
        DataInterfacePage model = JsonUtil.getJsonToBean(objectMap, DataInterfacePage.class);
        ActionResult actionResult = dataInterfaceService.infoToIdPageList(id, model);
        if (actionResult.getCode() == 200) {
            try{
                Object data = actionResult.getData();
                if (data instanceof List) {
                    List<Map<String,Object>> list=(List)data;
                    List<String> listKey=new ArrayList();
                    for(String key:list.get(0).keySet()){
                        listKey.add(key);
                    }
                    actionResult.setData(listKey);
                }else{
                    Map<String,Object> map=JsonUtil.stringToMap(JSONObject.toJSONString(data, SerializerFeature.WriteMapNullValue));
                    List<Map<String,Object>> list=(List)map.get("list");
                    List<String> listKey=new ArrayList();
                    for(String key:list.get(0).keySet()){
                        listKey.add(key);
                    }
                    actionResult.setData(listKey);
                }
            }catch (Exception e){
                return ActionResult.fail("接口不符合规范！");
            }
        }
        return actionResult;
    }
    /**
     * 复制数据接口
     *
     * @param id 数据接口ID
     * @return 执行结构
     * @throws DataException ignore
     */
    @Operation(summary = "复制数据接口")
    @Parameters({
            @Parameter(name = "id", description = "数据接口ID", required = true)
    })
    @SaCheckPermission("systemData.dataInterface")
    @PostMapping("/{id}/Actions/Copy")
    public ActionResult<?> Copy(@PathVariable("id") String id) throws DataException {
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        entity.setFullName(entity.getFullName() + ".副本" + copyNum);
        if(entity.getFullName().length() > 50) return ActionResult.fail(MsgCode.COPY001.get());
        entity.setEnCode(entity.getEnCode() + "." + copyNum);
        entity.setEnabledMark(0);
        dataInterfaceService.create(entity);
        return ActionResult.success(MsgCode.SU007.get());
    }

    
    @GetMapping("/getEntity")
    public DataInterfaceEntity getEntity(@RequestParam("id") String id) {
        return dataInterfaceService.getInfo(id);
    }
}
