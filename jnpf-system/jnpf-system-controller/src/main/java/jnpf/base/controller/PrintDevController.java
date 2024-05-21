package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.base.entity.OperatorRecordEntity;
import jnpf.base.entity.PrintDevEntity;
import jnpf.base.model.printdev.PrintOption;
import jnpf.base.model.printdev.PrintPagination;
import jnpf.base.model.printdev.PrintTableTreeModel;
import jnpf.base.model.printdev.dto.PrintDevFormDTO;
import jnpf.base.model.printdev.query.PrintDevDataQuery;
import jnpf.base.model.printdev.query.PrintDevFieldsQuery;
import jnpf.base.model.printdev.vo.PrintDevListVO;
import jnpf.base.model.printdev.vo.PrintDevVO;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.service.IPrintDevService;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.DbSensitiveConstant;
import jnpf.constant.MsgCode;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.FileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.ParameterUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import jnpf.util.treeutil.SumTree;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * 打印模板-控制器
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Tag(name = "打印模板", description = "print")
@RestController
@RequestMapping("/api/system/printDev")
public class PrintDevController extends SuperController<IPrintDevService, PrintDevEntity> {

    @Autowired
    private IPrintDevService iPrintDevService;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private UserService userService;

    /**
     * 查询打印列表
     * @param ids 主键集合
     * @return
     */
    @Operation(summary = "查询打印列表")
    @Parameter(name = "ids", description = "主键集合", required = true)
    @SaCheckPermission("system.printDev")
    @PostMapping("/getListById")
    public List<PrintOption> getListById(@RequestBody List<String> ids) {
        QueryWrapper<PrintDevEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(PrintDevEntity::getId,ids);
        wrapper.lambda().eq(PrintDevEntity::getEnabledMark,1);
        List<PrintDevEntity> list = iPrintDevService.getBaseMapper().selectList(wrapper);
        List<PrintOption> options = JsonUtil.getJsonToList(list, PrintOption.class);
        return options;
    }

    @Operation(summary = "查询打印列表")
    @Parameter(name = "data", description = "打印模板-数查询对象", required = true)
    @SaCheckPermission("system.printDev")
    @PostMapping("getListOptions")
    public ActionResult getListOptions(@RequestBody PrintDevDataQuery data) {
        List<String> ids = data.getIds();
        QueryWrapper<PrintDevEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(PrintDevEntity::getId,ids);
        wrapper.lambda().eq(PrintDevEntity::getEnabledMark,1);
        List<PrintDevEntity> list = iPrintDevService.getBaseMapper().selectList(wrapper);
        List<PrintOption> options = JsonUtil.getJsonToList(list, PrintOption.class);
        return  ActionResult.success(options);
    }

    /*============增删改==============*/

    /**
     * 新增打印模板对象
     *
     * @param printDevForm 打印模板数据传输对象
     * @return 执行结果标识
     */
    @Operation(summary = "新增")
    @Parameter(name = "printDevForm", description = "打印模板数据传输对象", required = true)
    @SaCheckPermission("system.printDev")
    @PostMapping
    public ActionResult<PrintDevFormDTO> create(@RequestBody @Valid PrintDevFormDTO printDevForm) {
        PrintDevEntity printDevEntity = JsonUtil.getJsonToBean(printDevForm, PrintDevEntity.class);
        iPrintDevService.creUpdateCheck(printDevEntity, true, true); // 校验
        printDevEntity.setId(RandomUtil.uuId());
        iPrintDevService.save(printDevEntity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 删除打印模板
     *
     * @param id           打印模板id
     * @param printDevForm 打印模板数据传输对象
     * @return 执行结果标识
     */
    @Operation(summary = "编辑")
    @Parameters({
            @Parameter(name = "id", description = "打印模板id", required = true),
            @Parameter(name = "printDevForm", description = "打印模板数据传输对象", required = true)
    })
    @SaCheckPermission("system.printDev")
    @PutMapping("/{id}")
    public ActionResult<PrintDevFormDTO> update(@PathVariable String id, @RequestBody @Valid PrintDevFormDTO printDevForm) {
        PrintDevEntity printDevEntity = JsonUtil.getJsonToBean(printDevForm, PrintDevEntity.class);
        PrintDevEntity originEntity = iPrintDevService.getById(id);
        iPrintDevService.creUpdateCheck(printDevEntity,
                !originEntity.getFullName().equals(printDevForm.getFullName()),
                !originEntity.getEnCode().equals(printDevForm.getEnCode()));
        printDevEntity.setId(id);
        iPrintDevService.updateById(printDevEntity);
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 复制打印模板
     *
     * @param id 打印模板id
     * @return 执行结果标识
     */
    @Operation(summary = "复制")
    @Parameters({
            @Parameter(name = "id", description = "打印模板id", required = true)
    })
    @SaCheckPermission("system.printDev")
    @PostMapping("/{id}/Actions/Copy")
    public ActionResult<?> copy(@PathVariable String id) {
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        PrintDevEntity entity = iPrintDevService.getById(id);
        entity.setFullName(entity.getFullName() + ".副本" + copyNum);
        if(entity.getFullName().length() > 50){
            return ActionResult.fail(MsgCode.PRI006.get());
        }
        entity.setEnCode(entity.getEnCode() + "." + copyNum);
        entity.setId(RandomUtil.uuId());
        entity.setEnabledMark(0);
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(UserProvider.getLoginUserId());
        PrintDevEntity entityBean = JsonUtil.getJsonToBean(entity, PrintDevEntity.class);
        iPrintDevService.save(entityBean);
        return ActionResult.success(MsgCode.SU007.get());
    }

    /**
     * 删除打印模板
     *
     * @param id 打印模板id
     * @return ignore
     */
    @Operation(summary = "删除")
    @Parameters({
            @Parameter(name = "id", description = "打印模板id", required = true)
    })
    @SaCheckPermission("system.printDev")
    @DeleteMapping("/{id}")
    public ActionResult<PrintDevFormDTO> delete(@PathVariable String id) {
        //对象存在判断
        if (iPrintDevService.getById(id) != null) {
            iPrintDevService.removeById(id);
            return ActionResult.success(MsgCode.SU003.get());
        } else {
            return ActionResult.fail(MsgCode.FA003.get());
        }
    }

    /**
     * 修改打印模板可用状态
     *
     * @param id 打印模板id
     * @return 执行结果标识
     */
    @Operation(summary = "修改状态")
    @Parameters({
            @Parameter(name = "id", description = "打印模板id", required = true)
    })
    @SaCheckPermission("system.printDev")
    @PutMapping("/{id}/Actions/State")
    public ActionResult<PageListVO<PrintDevEntity>> state(@PathVariable String id) {
        PrintDevEntity entity = iPrintDevService.getById(id);
        if (entity != null) {
            if ("1".equals(entity.getEnabledMark().toString())) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            iPrintDevService.updateById(entity);
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail(MsgCode.FA002.get());
    }

    /*============查询==============*/

    /**
     * 查看单个模板详情
     *
     * @param id 打印模板id
     * @return 单个模板对象
     */
    @Operation(summary = "预览")
    @Parameters({
            @Parameter(name = "id", description = "打印模板id", required = true)
    })
    @SaCheckPermission("system.printDev")
    @GetMapping("/{id}")
    public ActionResult<PrintDevEntity> info(@PathVariable String id) {
        return ActionResult.success(iPrintDevService.getById(id));
    }

    /**
     * 查看所有模板
     *
     * @return 所有模板集合
     */
    @Operation(summary = "列表")
    @SaCheckPermission("system.printDev")
    @GetMapping
    public ActionResult<PageListVO<PrintDevListVO>> list(PrintPagination paginationPrint) {
        List<PrintDevEntity> list = iPrintDevService.getList(paginationPrint);
        DictionaryTypeEntity typeEntity = dictionaryTypeService.getInfoByEnCode("printDev");
        List<DictionaryDataEntity> typeList = dictionaryDataService.getList(typeEntity.getId());
        List<PrintDevListVO> voList = new ArrayList<>();
        for (PrintDevEntity entity : list) {
            PrintDevListVO vo = JsonUtil.getJsonToBean(entity, PrintDevListVO.class);
            DictionaryDataEntity dataEntity = typeList.stream().filter(t -> t.getEnCode().equals(entity.getCategory())).findFirst().orElse(null);
            if (dataEntity != null) {
                vo.setCategory(dataEntity.getFullName());
            } else {
                vo.setCategory("");
            }
            // 创建者
            UserEntity creatorUser = userService.getInfo(entity.getCreatorUserId());
            vo.setCreatorUser(creatorUser != null ? creatorUser.getRealName() + "/" + creatorUser.getAccount() : entity.getCreatorUserId());
            // 修改人
            UserEntity lastModifyUser = userService.getInfo(entity.getLastModifyUserId());
            vo.setLastModifyUser(lastModifyUser != null ? lastModifyUser.getRealName() + "/" + lastModifyUser.getAccount() : entity.getLastModifyUserId());
            voList.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationPrint, PaginationVO.class);
        return ActionResult.page(voList , paginationVO);
    }

    /**
     * 下拉列表
     *
     * @return 返回列表数据
     */
    @Operation(summary = "下拉列表")
    @GetMapping("/Selector")
    public ActionResult<ListVO<PrintDevVO>> selectorList(@RequestParam(value = "type", required = false) Integer type){
        ListVO<PrintDevVO> vo = new ListVO<>();
        vo.setList(iPrintDevService.getTreeModel(type));
        return ActionResult.success(vo);
    }

    /**
     * 根据sql获取内容
     * @param printDevSqlDataQuery 打印模板查询对象
     * @return 打印模板对应内容
     */
    @Operation(summary = "Sql数据获取")
    @SaCheckPermission("system.printDev")
    @GetMapping("/Data")
    public ActionResult<Map<String, Object>> getFieldData(PrintDevDataQuery printDevSqlDataQuery) throws Exception {
        String id = XSSEscape.escape(printDevSqlDataQuery.getId());
        String formId = XSSEscape.escape(printDevSqlDataQuery.getFormId());
        PrintDevEntity entity = iPrintDevService.getById(id);
        if(entity == null){
            return ActionResult.fail(MsgCode.PRI001.get());
        }
        Map<String, Object> printDataMap = iPrintDevService.getDataBySql(
                entity.getDbLinkId(),
                entity.getSqlTemplate().replaceAll("@formId", "'" + formId + "'"));
        List<Map<String, Object>> headTableList = (List<Map<String, Object>>) printDataMap.get("headTable");
        printDataMap.remove("headTable");
        for (Map map : headTableList) {
            printDataMap.putAll(map);
        }
        Map<String, Object> dataMap = new HashMap<>(16);
        dataMap.put("printData", JsonUtil.getJsonToBean(JsonUtil.getObjectToStringAsDate(printDataMap), Map.class));
        dataMap.put("printTemplate", entity.getPrintTemplate());
        List<OperatorRecordEntity> operatorRecordList = iPrintDevService.getFlowTaskOperatorRecordList(formId);
        dataMap.put("operatorRecordList", operatorRecordList);
        return ActionResult.success(dataMap);
    }

    /**
     * Sql数据获取
     *
     * @param printDevSqlDataQuery 打印模板-数查询对象
     * @return
     */
    @Operation(summary = "Sql数据获取")
    @GetMapping("/BatchData")
    public ActionResult getBatchData(PrintDevDataQuery printDevSqlDataQuery) {
        String id = XSSEscape.escape(printDevSqlDataQuery.getId());
        String formId = XSSEscape.escape(printDevSqlDataQuery.getFormId());
        PrintDevEntity entity = iPrintDevService.getById(id);
        if(entity == null){
            return ActionResult.fail(MsgCode.PRI001.get());
        }

        ArrayList<Object> list = new ArrayList<>();
        List<String> record = Arrays.asList(formId.split(","));
        record.forEach(rid->{
            list.add(iPrintDevService.getDataMap(entity,rid));
        });
        return ActionResult.success(list);
    }


    /**
     * 获取打印模块字段信息
     *
     * @param printDevFieldsQuery 打印模板查询对象
     * @return 字段信息数据
     */
    @Operation(summary = "Sql字段获取")
    @Parameters({
            @Parameter(name = "printDevFieldsQuery", description = "打印模板查询对象", required = true)
    })
    @SaCheckPermission("system.printDev")
    @PostMapping("/Fields")
    public ActionResult<List<SumTree<PrintTableTreeModel>>> getFields(@RequestBody PrintDevFieldsQuery printDevFieldsQuery) throws Exception {
        String containsSensitive = ParameterUtil.checkContainsSensitive(printDevFieldsQuery.getSqlTemplate(), DbSensitiveConstant.PRINT_SENSITIVE);
        if (StringUtil.isNotEmpty(containsSensitive)) {
            return ActionResult.fail("当前查询SQL包含敏感字：" + containsSensitive);
        }
        String dbLinkId = XSSEscape.escape(printDevFieldsQuery.getDbLinkId());
        List<SumTree<PrintTableTreeModel>> printTableFields = iPrintDevService.getPintTabFieldStruct(dbLinkId,
                printDevFieldsQuery.getSqlTemplate().replaceAll("@formId", " null "));
        return ActionResult.success(printTableFields);
    }

    /*==========行为============*/

    /**
     * 导出打印模板备份json
     *
     * @param id 打印模板id
     */
    @Operation(summary = "导出")
    @Parameters({
            @Parameter(name = "id", description = "打印模板id", required = true)
    })
    @SaCheckPermission("system.printDev")
    @GetMapping("/{id}/Actions/Export")
    public ActionResult<DownloadVO> export(@PathVariable String id) {
        PrintDevEntity entity = iPrintDevService.getById(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.SYSTEM_PRINT.getTableName());
        return ActionResult.success(downloadVO);
    }

    /**
     * 导入打印模板备份json
     *
     * @param multipartFile 备份json文件
     * @return 执行结果标识
     */
    @Operation(summary = "导入")
    @SaCheckPermission("system.printDev")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult<PageListVO<PrintDevEntity>> importData(@RequestPart("file") MultipartFile multipartFile,
                                                               @RequestParam("type") Integer type) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.SYSTEM_PRINT.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        try {
            //读取文件内容
            String fileContent = FileUtil.getFileContent(multipartFile);
            PrintDevEntity entity = JsonUtil.getJsonToBean(fileContent, PrintDevEntity.class);
            StringJoiner stringJoiner = new StringJoiner("、");
            //id为空切名称不存在时
            QueryWrapper<PrintDevEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(PrintDevEntity::getId, entity.getId());
            if (iPrintDevService.count(queryWrapper) > 0) {
                stringJoiner.add("ID");
            }
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(PrintDevEntity::getEnCode, entity.getEnCode());
            if (iPrintDevService.count(queryWrapper) > 0) {
                stringJoiner.add("编码");
            }
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(PrintDevEntity::getFullName, entity.getFullName());
            if (iPrintDevService.count(queryWrapper) > 0) {
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
            iPrintDevService.setIgnoreLogicDelete().removeById(entity);
            entity.setEnabledMark(0);
            iPrintDevService.setIgnoreLogicDelete().saveOrUpdate(entity);
            return ActionResult.success(MsgCode.IMP001.get());
        } catch (Exception e) {
            return ActionResult.fail(MsgCode.IMP004.get());
        } finally {
            iPrintDevService.clearIgnoreLogicDelete();
        }
    }

}
