package gas.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import gas.service.*;
import gas.entity.*;
import jnpf.util.*;
import gas.model.gasbaserroad.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;
import jnpf.annotation.JnpfField;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.ProvinceEntity;
import java.io.IOException;
import java.util.stream.Collectors;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.exception.WorkFlowException;
import jnpf.model.visualJson.UploaderTemplateModel;
import org.springframework.web.multipart.MultipartFile;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.File;
import jnpf.onlinedev.model.ExcelImFieldModel;
import jnpf.onlinedev.model.OnlineImport.ImportDataModel;
import jnpf.onlinedev.model.OnlineImport.ImportFormCheckUniqueModel;
import jnpf.onlinedev.model.OnlineImport.ExcelImportModel;
import jnpf.onlinedev.model.OnlineImport.VisualImportModel;
import cn.xuyanwu.spring.file.storage.FileInfo;
import lombok.Cleanup;
import jnpf.model.visualJson.config.HeaderModel;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.util.VisualUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 路段设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Slf4j
@RestController
@Tag(name = "路段设置" , description = "gas")
@RequestMapping("/api/gas/GasBaserRoad")
public class GasBaserRoadController {

    @Autowired
    private GeneraterSwapUtil generaterSwapUtil;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private GasBaserRoadService gasBaserRoadService;



    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
    * 列表
    *
    * @param gasBaserRoadPagination
    * @return
    */
    @Operation(summary = "获取列表")
    @PostMapping("/getList")
    public ActionResult list(@RequestBody GasBaserRoadPagination gasBaserRoadPagination)throws IOException{
        List<GasBaserRoadEntity> list= gasBaserRoadService.getList(gasBaserRoadPagination);
        List<Map<String, Object>> realList=new ArrayList<>();
        for (GasBaserRoadEntity entity : list) {
        Map<String, Object> gasBaserRoadMap=JsonUtil.entityToMap(entity);
        gasBaserRoadMap.put("id", gasBaserRoadMap.get("id"));
        //副表数据
        //子表数据
            realList.add(gasBaserRoadMap);
        }
        //数据转换
        realList = generaterSwapUtil.swapDataList(realList, GasBaserRoadConstant.getFormData(), GasBaserRoadConstant.getColumnData(), gasBaserRoadPagination.getModuleId(),false);

        //返回对象
        PageListVO vo = new PageListVO();
        vo.setList(realList);
        PaginationVO page = JsonUtil.getJsonToBean(gasBaserRoadPagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }
    /**
    * 创建
    *
    * @param gasBaserRoadForm
    * @return
    */
    @PostMapping()
    @Operation(summary = "创建")
    public ActionResult create(@RequestBody @Valid GasBaserRoadForm gasBaserRoadForm) {
        String b = gasBaserRoadService.checkForm(gasBaserRoadForm,0);
        if (StringUtil.isNotEmpty(b)){
            return ActionResult.fail(b );
        }
        try{
            gasBaserRoadService.saveOrUpdate(gasBaserRoadForm, null ,true);
        }catch(Exception e){
            return ActionResult.fail("新增数据失败");
        }
        return ActionResult.success("创建成功");
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

    /**
    * 模板下载
    *
    * @return
    */
    @Operation(summary = "模板下载")
    @GetMapping("/TemplateDownload")
    public ActionResult<DownloadVO>  TemplateDownload(){
        DownloadVO vo = DownloadVO.builder().build();
        UserInfo userInfo = userProvider.get();
        Map<String, Object> dataMap = new HashMap<>();
        //主表对象
        List<ExcelExportEntity> entitys = new ArrayList<>();
        //以下添加字段
                entitys.add(new ExcelExportEntity("路段名称(name)" ,"name"));
        dataMap.put("name", "");
                entitys.add(new ExcelExportEntity("路段编码(code)" ,"code"));
            dataMap.put("code", "系统自动生成");
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(dataMap);

        ExportParams exportParams = new ExportParams(null, "路段管理模板");
        exportParams.setType(ExcelType.XSSF);
        try{
        @Cleanup Workbook workbook = new HSSFWorkbook();
        if (entitys.size()>0){
        if (list.size()==0){
        list.add(new HashMap<>());
        }
        //复杂表头-表头和数据处理
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(GasBaserRoadConstant.getColumnData(), ColumnDataModel.class);
        List<HeaderModel> complexHeaderList = columnDataModel.getComplexHeaderList();
        if (!Objects.equals(columnDataModel.getType(), 3) && !Objects.equals(columnDataModel.getType(), 5)) {
            entitys = VisualUtils.complexHeaderHandel(entitys, complexHeaderList, false);
            list =  VisualUtils.complexHeaderDataHandel(list, complexHeaderList, false);
        }
        workbook = ExcelExportUtil.exportExcel(exportParams, entitys, list);
        }
        String fileName = "路段管理模板_" + DateUtil.dateNow("yyyyMMddHHmmss") + ".xlsx";
        MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, fileName);
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, temporaryFilePath, fileName);
        vo.setName(fileInfo.getFilename());
        vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + "Temporary") + "&name=" + fileName);
        } catch (Exception e) {
        log.error("模板信息导出Excel错误:{}", e.getMessage());
        e.printStackTrace();
        }
        return ActionResult.success(vo);
    }

    /**
    * 导入预览
    *
    * @return
    */
    @Operation(summary = "导入预览" )
    @GetMapping("/ImportPreview")
    public ActionResult<Map<String, Object>> ImportPreview(String fileName) throws Exception {
        Map<String, Object> headAndDataMap = new HashMap<>(2);
            String filePath = FileUploadUtils.getLocalBasePath() + configValueUtil.getTemporaryFilePath();
            FileUploadUtils.downLocal(configValueUtil.getTemporaryFilePath(), filePath, fileName);
        File temporary = new File(XSSEscape.escapePath(filePath + fileName));
            int headerRowIndex = 1;
        ImportParams params = new ImportParams();
        params.setTitleRows(0);
        params.setHeadRows(headerRowIndex);
        params.setNeedVerify(true);
        try {
            List<Map> excelDataList = ExcelUtil.importExcel(temporary, 0, headerRowIndex, Map.class);

            ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(GasBaserRoadConstant.getColumnData(), ColumnDataModel.class);
            UploaderTemplateModel uploaderTemplateModel = JsonUtil.getJsonToBean(columnDataModel.getUploaderTemplateJson(), UploaderTemplateModel.class);
            List<String> selectKey = uploaderTemplateModel.getSelectKey();
            //子表合并
            List<Map<String, Object>> results = FormExecelUtils.dataMergeChildTable(excelDataList,selectKey);
            // 导入字段
            List<ExcelImFieldModel> columns = new ArrayList<>();
            columns.add(new ExcelImFieldModel("name","路段名称","input"));
            columns.add(new ExcelImFieldModel("code","路段编码","billRule"));
            headAndDataMap.put("dataRow" , results);
            headAndDataMap.put("headerRow" , JsonUtil.getJsonToList(JsonUtil.getListToJsonArray(columns)));
            } catch (Exception e){
            e.printStackTrace();
            return ActionResult.fail("表头名称不可更改,表头行不能删除");
        }
        return ActionResult.success(headAndDataMap);
    }

    /**
    * 导入数据
    *
    * @return
    */
    @Operation(summary = "导入数据" )
    @PostMapping("/ImportData")
    public ActionResult<ExcelImportModel> ImportData(@RequestBody VisualImportModel visualImportModel) throws Exception {
        List<Map<String, Object>> listData = visualImportModel.getList();
        ImportFormCheckUniqueModel uniqueModel = new ImportFormCheckUniqueModel();
        uniqueModel.setDbLinkId(GasBaserRoadConstant.DBLINKID);
        uniqueModel.setUpdate(Objects.equals("1", "2"));

        Map<String,String> tablefieldkey = new HashMap<>();
        for(String key:GasBaserRoadConstant.TABLEFIELDKEY.keySet()){
        tablefieldkey.put(key,GasBaserRoadConstant.TABLERENAMES.get(GasBaserRoadConstant.TABLEFIELDKEY.get(key)));
        }
        ExcelImportModel excelImportModel = generaterSwapUtil.importData(GasBaserRoadConstant.getFormData(),listData,uniqueModel, tablefieldkey,GasBaserRoadConstant.getTableList());
        List<ImportDataModel> importDataModel = uniqueModel.getImportDataModel();
        for (ImportDataModel model : importDataModel) {
            String id = model.getId();
            Map<String, Object> result = model.getResultData();
            if(StringUtil.isNotEmpty(id)){
                update(id, JsonUtil.getJsonToBean(result,GasBaserRoadForm.class), true);
            }else {
                create( JsonUtil.getJsonToBean(result,GasBaserRoadForm.class));
            }
        }
        return ActionResult.success(excelImportModel);
    }

    /**
    * 导出异常报告
    *
    * @return
    */
    @Operation(summary = "导出异常报告")
    @PostMapping("/ImportExceptionData")
    public ActionResult<DownloadVO> ImportExceptionData(@RequestBody VisualImportModel visualImportModel) {
        DownloadVO vo=DownloadVO.builder().build();
        List<GasBaserRoadExcelErrorVO> gasBaserRoadVOList = JsonUtil.getJsonToList(visualImportModel.getList(), GasBaserRoadExcelErrorVO.class);
        UserInfo userInfo = userProvider.get();

        try{
        @Cleanup Workbook workbook = new HSSFWorkbook();
        ExportParams exportParams = new ExportParams(null, "错误报告");
        exportParams.setType(ExcelType.XSSF);
        workbook = ExcelExportUtil.exportExcel(exportParams,
        GasBaserRoadExcelErrorVO.class, gasBaserRoadVOList);

        String fileName = "路段管理错误报告_" + DateUtil.dateNow("yyyyMMddHHmmss") + ".xlsx";
        MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, fileName);
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, temporaryFilePath, fileName);
        vo.setName(fileInfo.getFilename());
        vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + "Temporary") + "&name=" + fileName);
        } catch (Exception e) {
        e.printStackTrace();
        }
        return ActionResult.success(vo);
    }
    /**
    * 编辑
    * @param id
    * @param gasBaserRoadForm
    * @return
    */
    @PutMapping("/{id}")
    @Operation(summary = "更新")
    public ActionResult update(@PathVariable("id") String id,@RequestBody @Valid GasBaserRoadForm gasBaserRoadForm,
                                @RequestParam(value = "isImport", required = false) boolean isImport){
        if (!isImport) {
            String b =  gasBaserRoadService.checkForm(gasBaserRoadForm,1);
            if (StringUtil.isNotEmpty(b)){
                return ActionResult.fail(b );
            }
        }
        GasBaserRoadEntity entity= gasBaserRoadService.getInfo(id);
        if(entity!=null){
            try{
                gasBaserRoadService.saveOrUpdate(gasBaserRoadForm,id,false);
            }catch(Exception e){
                return ActionResult.fail("修改数据失败");
            }
            return ActionResult.success("更新成功");
        }else{
            return ActionResult.fail("更新失败，数据不存在");
        }
    }
    /**
    * 删除
    * @param id
    * @return
    */
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id){
        GasBaserRoadEntity entity= gasBaserRoadService.getInfo(id);
        if(entity!=null){
            //假删除
            entity.setDeleteMark(1);
            entity.setDeleteUserId(userProvider.get().getUserId());
            entity.setDeleteTime(new Date());
            gasBaserRoadService.update(id,entity);
        }
        return ActionResult.success("删除成功");
    }
    /**
    * 表单信息(详情页)
    * 详情页面使用-转换数据
    * @param id
    * @return
    */
    @Operation(summary = "表单信息(详情页)")
    @GetMapping("/detail/{id}")
    public ActionResult detailInfo(@PathVariable("id") String id){
        GasBaserRoadEntity entity= gasBaserRoadService.getInfo(id);
        if(entity==null){
            return ActionResult.fail("表单数据不存在！");
        }
        Map<String, Object> gasBaserRoadMap=JsonUtil.entityToMap(entity);
        gasBaserRoadMap.put("id", gasBaserRoadMap.get("id"));
        //副表数据
        //子表数据
        gasBaserRoadMap = generaterSwapUtil.swapDataDetail(gasBaserRoadMap,GasBaserRoadConstant.getFormData(),"562561412187037573",false);
        return ActionResult.success(gasBaserRoadMap);
    }
    /**
    * 获取详情(编辑页)
    * 编辑页面使用-不转换数据
    * @param id
    * @return
    */
    @Operation(summary = "信息")
    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id){
        GasBaserRoadEntity entity= gasBaserRoadService.getInfo(id);
        if(entity==null){
            return ActionResult.fail("表单数据不存在！");
        }
        Map<String, Object> gasBaserRoadMap=JsonUtil.entityToMap(entity);
        gasBaserRoadMap.put("id", gasBaserRoadMap.get("id"));
        //副表数据
        //子表数据
        gasBaserRoadMap = generaterSwapUtil.swapDataForm(gasBaserRoadMap,GasBaserRoadConstant.getFormData(),GasBaserRoadConstant.TABLEFIELDKEY,GasBaserRoadConstant.TABLERENAMES);
        return ActionResult.success(gasBaserRoadMap);
    }

}
