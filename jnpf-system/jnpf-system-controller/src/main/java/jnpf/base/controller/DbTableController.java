package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.Pagination;
import jnpf.base.entity.PrintDevEntity;
import jnpf.base.model.dbtable.dto.DbTableFieldDTO;
import jnpf.base.model.dbtable.vo.DbFieldVO;
import jnpf.base.model.dbtable.vo.DbTableInfoVO;
import jnpf.base.model.dbtable.vo.DbTableListVO;
import jnpf.base.service.DbTableService;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.datatype.model.DtModelDTO;
import jnpf.database.model.dbfield.DbFieldModel;
import jnpf.database.model.dbfield.base.DbFieldModelBase;
import jnpf.database.model.dbtable.DbTableFieldModel;
import jnpf.database.model.page.DbTableDataForm;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.DataException;
import jnpf.util.FileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据建模
 *
 * @author JNPF开发平台组 YANYU
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "数据建模", description = "DataModel")
@RestController
@RequestMapping("/api/system/DataModel")
public class DbTableController {

    @Autowired
    private DbTableService dbTableService;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserProvider userProvider;

    /**
     * 1:列表
     *
     * @param id         连接id
     * @param pagination 关键词
     * @return 数据库表列表
     * @throws DataException ignore
     */
    @Operation(summary = "获取数据库表列表")
    @Parameters({
            @Parameter(name = "id", description = "连接id", required = true)
    })
    @GetMapping("/{id}/Tables")
    public ActionResult<DbTableListVO<DbTableFieldModel>> getList(@PathVariable("id") String id, Pagination pagination) throws Exception {
        try {
            List<DbTableFieldModel> tableList = dbTableService.getListPage(XSSEscape.escape(id), pagination);
            return ActionResult.success(new DbTableListVO<>(tableList, JsonUtil.getJsonToBean(pagination, PaginationVO.class)));
        } catch (Exception e) {
            throw new DataException("数据库连接失败");
        }
    }

    /**
     * 1:列表
     *
     * @param id 连接id
     * @param page 关键字
     * @return 数据库表列表
     * @throws DataException ignore
     */
    @Operation(summary = "获取数据库表列表")
    @Parameters({
            @Parameter(name = "id", description = "连接id", required = true)
    })
    @GetMapping("/{id}/TableAll")
    public ActionResult<ListVO<DbTableFieldModel>> getList(@PathVariable("id") String id, Page page) throws Exception {
        List<DbTableFieldModel> tableList = dbTableService.getListPage(XSSEscape.escape(id), page);
        ListVO<DbTableFieldModel> list = new ListVO<>();
        list.setList(tableList);
        return ActionResult.success(list);
    }

    /**
     * 2:预览数据库表
     *
     * @param dbTableDataForm 查询条件
     * @param linkId          数据连接ID
     * @param tableName       表名
     * @return 数据库表
     * @throws Exception ignore
     */
    @Operation(summary = "预览数据库表")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "tableName", description = "表名", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @GetMapping("/{linkId}/Table/{tableName}/Preview")
    public ActionResult<PageListVO<Map<String, Object>>> data(DbTableDataForm dbTableDataForm, @PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws Exception {
        String escape = XSSEscape.escape(linkId);
        String escapeTableName = XSSEscape.escape(tableName);
        List<Map<String, Object>> data = dbTableService.getData(dbTableDataForm, escape, escapeTableName);
        PaginationVO paginationVO = JsonUtilEx.getJsonToBeanEx(dbTableDataForm, PaginationVO.class);
        return ActionResult.page(JsonUtil.getJsonToListMap(JsonUtil.getObjectToStringAsDate(data)), paginationVO);
    }

    /**
     * 3:列表
     *
     * @param linkId    数据连接ID
     * @param tableName 表名
     * @return 列表
     * @throws DataException ignore
     */
    @GetMapping("/{linkId}/Tables/{tableName}/Fields/Selector")
    @Operation(summary = "获取数据库表字段下拉框列表")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "tableName", description = "表名", required = true)
    })
    public ActionResult<ListVO<DbFieldVO>> selectorList(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws Exception {
        List<DbFieldModel> data = dbTableService.getFieldList(linkId, tableName);
        List<DbFieldVO> vos = JsonUtil.getJsonToList(data, DbFieldVO.class);
        ListVO<DbFieldVO> vo = new ListVO<>();
        vo.setList(vos);
        return ActionResult.success(vo);
    }

    /**
     * 4:字段列表
     *
     * @param linkId    连接Id
     * @param tableName 表名
     * @param type      类型
     * @return 段列表
     * @throws DataException ignore
     */
    @Operation(summary = "获取数据库表字段列表")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "tableName", description = "表名", required = true),
            @Parameter(name = "type", description = "类型")
    })
    @GetMapping("/{linkId}/Tables/{tableName}/Fields")
    public ActionResult<ListVO<DbFieldVO>> fieldList(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName, @RequestParam(value = "type", required = false) String type) throws Exception {
        List<DbFieldModel> data;
        try{
            data = dbTableService.getFieldList(linkId, tableName);
        }catch (Exception e){
            return ActionResult.fail(MsgCode.DB302.get());
        }
        if(CollectionUtils.isEmpty(data)){
            return ActionResult.fail("请在数据库中添加对应的数据表");
        }
        List<DbFieldVO> voList = data.stream().map(DbFieldVO::new).collect(Collectors.toList());
        for (DbFieldVO vo : voList) {
            String columnName = vo.getField();
            if ("1".equals(type)) {
                String name = vo.getField().toLowerCase();
                name = name.startsWith("f_")? name.substring(2) : name;
                vo.setField(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
            }
            vo.setColumnName(columnName);
        }
        ListVO<DbFieldVO> vo = new ListVO<>();
        vo.setList(voList);
        return ActionResult.success(vo);
    }

    /**
     * 5:编辑显示 - 表、字段信息
     *
     * @param dbLinkId  连接Id
     * @param tableName 表名
     * @return 表、字段信息
     * @throws DataException ignore
     */
    @Operation(summary = "获取表及表字段信息")
    @Parameters({
            @Parameter(name = "dbLinkId", description = "数据连接ID", required = true),
            @Parameter(name = "tableName", description = "表名", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @GetMapping("/{dbLinkId}/Table/{tableName}")
    public ActionResult<DbTableInfoVO> get(@PathVariable("dbLinkId") String dbLinkId, @PathVariable("tableName") String tableName) throws Exception {
        return ActionResult.success(new DbTableInfoVO(dbTableService.getTable(dbLinkId, tableName), dbTableService.getFieldList(dbLinkId, tableName)));
    }

    /**
     * 6:新建表
     *
     * @param linkId          连接Id
     * @param dbTableFieldDTO 新建模型
     * @return 执行结果
     * @throws DataException ignore
     */
    @Operation(summary = "新建")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "dbTableFieldDTO", description = "建表参数对象", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @PostMapping("{linkId}/Table")
    public ActionResult<String> create(@PathVariable("linkId") String linkId, @RequestBody @Valid DbTableFieldDTO dbTableFieldDTO) throws Exception {
        int status = dbTableService.createTable(dbTableFieldDTO.getCreDbTableModel(linkId));
        if (status == 1) {
            return ActionResult.success(MsgCode.SU001.get());
        } else if (status == 0) {
            return ActionResult.fail("表名称不能重复");
        } else {
            return ActionResult.fail("添加失败");
        }
    }

    /**
     * 7:更新
     *
     * @param linkId 连接Id
     * @return 执行结果
     * @throws DataException ignore
     */
    @Operation(summary = "更新")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "dbTableFieldDTO", description = "建表参数对象", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @PutMapping("/{linkId}/Table")
    public ActionResult<String> update(@PathVariable("linkId") String linkId, @RequestBody @Valid DbTableFieldDTO dbTableFieldDTO) throws Exception {
        DbTableFieldModel dbTableModel = dbTableFieldDTO.getUpDbTableModel(linkId);
        // 当修改表名时，验证是否与其他表名重名
        if (!dbTableModel.getUpdateNewTable().equals(dbTableModel.getUpdateOldTable())) {
            if (dbTableService.isExistTable(linkId, dbTableModel.getUpdateNewTable())) {
                return ActionResult.fail("表名称不能重复");
            }
        }
        dbTableService.update(dbTableModel);
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 8:更新
     *
     * @param linkId 连接Id
     * @return 执行结果
     * @throws DataException ignore
     */
    @Operation(summary = "添加字段")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "dbTableFieldDTO", description = "建表参数对象", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @PutMapping("/{linkId}/addFields")
    public ActionResult<String> addField(@PathVariable("linkId") String linkId, @RequestBody @Valid DbTableFieldDTO dbTableFieldDTO) throws Exception {
        DbTableFieldModel dbTableModel = dbTableFieldDTO.getUpDbTableModel(linkId);
        dbTableService.addField(dbTableModel);
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 9:删除
     *
     * @param linkId    连接Id
     * @param tableName 表名
     * @return 执行结果
     * @throws DataException ignore
     */
    @Operation(summary = "删除")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
            @Parameter(name = "tableName", description = "表名", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @DeleteMapping("/{linkId}/Table/{tableName}")
    public ActionResult<String> delete(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws Exception {
        dbTableService.delete(linkId, tableName);
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 10:导入
     *
     * @param linkId        连接id
     * @param multipartFile 文件
     * @return 执行结果
     * @throws DataException ignore
     */
    @Operation(summary = "导入")
    @Parameters({
            @Parameter(name = "linkId", description = "数据连接ID", required = true),
    })
    @SaCheckPermission("systemData.dataModel")
    @PostMapping(value = "/{linkId}/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult<PageListVO<PrintDevEntity>> importData(@PathVariable String linkId, @RequestPart("file") MultipartFile multipartFile) throws Exception {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.SYSTEM_DBTABLE.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        //读取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        DbTableFieldModel dbTableFieldModel = JSONObject.parseObject(fileContent, DbTableFieldModel.class);

        // 数据类型长度解析（enum枚举无法Json化）
        for (DbFieldModel dbFieldModel : dbTableFieldModel.getDbFieldModelList()) {
            String formatDataType = dbFieldModel.getLength();
            String dataType = "";
            String dtLength = "";
            if (formatDataType.contains("(")) {
                Matcher matcher = Pattern.compile("(.+)\\((.*)\\)").matcher(formatDataType);
                if (matcher.find()) {
                    dataType = matcher.group(1).trim();
                    dtLength = matcher.group(2).trim();
                }
            } else {
                dataType = formatDataType.trim();
            }
            DtModelDTO dtModelDTO = new DtModelDTO(dataType, dtLength, dbTableFieldModel.getDbEncode(), false);
            dtModelDTO.setConvertType(DtModelDTO.DB_VAL);
            dbFieldModel.setDtModelDTO(dtModelDTO);
        }

        dbTableFieldModel.setDbLinkId(linkId);
        int i = dbTableService.createTable(dbTableFieldModel);
        if (i == 1) {
            return ActionResult.success(MsgCode.IMP001.get());
        } else {
            return ActionResult.fail(MsgCode.DB007.get());
        }
    }

    /**
     * 导出
     *
     * @param tableName 表明
     * @param linkId    连接id
     * @return ignore
     */
    @Operation(summary = "导出")
    @Parameters({
            @Parameter(name = "tableName", description = "表明", required = true),
            @Parameter(name = "linkId", description = "连接id", required = true)
    })
    @SaCheckPermission("systemData.dataModel")
    @GetMapping("/{linkId}/Table/{tableName}/Actions/Export")
    public ActionResult<DownloadVO> export(@PathVariable String tableName, @PathVariable String linkId) throws Exception {
        DbTableFieldModel dbTable = dbTableService.getDbTableModel(linkId, tableName);
        dbTable.getDbFieldModelList().forEach(dbField -> {
            dbField.setLength(dbField.getDtModelDTO().convert().formatDataType());
            dbField.setDtModelDTO(null);
        });
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(dbTable, configValueUtil.getTemporaryFilePath(),
                dbTable.getTable() + "_", ModuleTypeEnum.SYSTEM_DBTABLE.getTableName());
        return ActionResult.success(downloadVO);
    }

    
    @PostMapping("/createTable")
    public void createTable(@RequestBody List<DbTableFieldModel> dbTableList) throws Exception {
        for (DbTableFieldModel dbTable : dbTableList) {
            dbTableService.createTable(dbTable);
        }
    }

    
    @PostMapping("/addField")
    public void addField(@RequestBody DbTableFieldModel dbTable) throws Exception {
        dbTableService.addField(dbTable);
    }

    
    @GetMapping("/getDbTableModel/{linkId}/{table}")
    public List<DbFieldModelBase> getDbTableModel(@PathVariable("linkId") String linkId, @PathVariable("table") String table) throws Exception {
        List<DbFieldModel> dbFieldModelList = dbTableService.getDbTableModel(linkId, table).getDbFieldModelList();
        List<DbFieldModelBase> list = JsonUtil.getJsonToList(dbFieldModelList, DbFieldModelBase.class);
        return list;
    }


    
    @GetMapping("/getPrimaryDbField/{linkId}/{table}")
    public Boolean getPrimaryDbField(@PathVariable("linkId") String linkId, @PathVariable("table") String table) throws Exception {
        DbTableFieldModel dbTableModel = dbTableService.getDbTableModel(linkId, table);
        List<DbFieldModel> data = dbTableModel.getDbFieldModelList();
        DbFieldModel dbFieldModel = data.stream().filter(DbFieldModel::getIsPrimaryKey).findFirst().orElse(null);
        if (dbFieldModel != null) {
            return dbFieldModel.getIsAutoIncrement() != null && dbFieldModel.getIsAutoIncrement();
        } else {
            return null;
        }
    }

}
