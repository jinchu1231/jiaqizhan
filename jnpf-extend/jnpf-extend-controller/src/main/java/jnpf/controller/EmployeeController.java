package jnpf.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.xuyanwu.spring.file.storage.FileInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.entity.EmployeeEntity;
import jnpf.exception.DataException;
import jnpf.exception.ImportException;
import jnpf.model.EmployeeModel;
import jnpf.model.employee.EmployeeCrForm;
import jnpf.model.employee.EmployeeExportVO;
import jnpf.model.employee.EmployeeImportVO;
import jnpf.model.employee.EmployeeInfoVO;
import jnpf.model.employee.EmployeeListVO;
import jnpf.model.employee.EmployeeUpForm;
import jnpf.model.employee.PaginationEmployee;
import jnpf.service.EmployeeService;
import jnpf.util.DateUtil;
import jnpf.util.ExcelUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.RandomUtil;
import jnpf.util.UpUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import jnpf.util.WordUtil;
import jnpf.util.XSSEscape;
import jnpf.util.type.StringNumber;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职员信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Slf4j
@Tag(name = "职员信息", description = "Employee")
@RestController
@RequestMapping("/api/extend/Employee")
public class EmployeeController extends SuperController<EmployeeService, EmployeeEntity> {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserProvider userProvider;

    /**
     * 列表(忽略验证Token)
     *
     * @param paginationEmployee 分页模型
     * @return
     */
    @Operation(summary = "获取职员列表")
    @GetMapping
    public ActionResult<PageListVO<EmployeeListVO>> getList(PaginationEmployee paginationEmployee) {
        List<EmployeeEntity> data = employeeService.getList(paginationEmployee);
        List<EmployeeListVO> list = JsonUtil.getJsonToList(data, EmployeeListVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationEmployee, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取职员信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<EmployeeInfoVO> info(@PathVariable("id") String id) throws DataException {
        EmployeeEntity entity = employeeService.getInfo(id);
        EmployeeInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, EmployeeInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param employeeCrForm 职工模型
     * @return
     */
    @Operation(summary = "app添加职员信息")
    @PostMapping
    @Parameters({
            @Parameter(name = "employeeCrForm", description = "职工模型", required = true),
    })
    public ActionResult create(@RequestBody @Valid EmployeeCrForm employeeCrForm) {
        EmployeeEntity entity = JsonUtil.getJsonToBean(employeeCrForm, EmployeeEntity.class);
        employeeService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 更新
     *
     * @param id             主键
     * @param employeeUpForm 职工模型
     * @return
     */
    @Operation(summary = "app修改职员信息")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "employeeUpForm", description = "职工模型", required = true),
    })
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid EmployeeUpForm employeeUpForm) {
        EmployeeEntity entity = JsonUtil.getJsonToBean(employeeUpForm, EmployeeEntity.class);
        employeeService.update(id, entity);
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除职员信息")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult delete(@PathVariable("id") String id) {
        EmployeeEntity entity = employeeService.getInfo(id);
        if (entity != null) {
            employeeService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 模板下载
     *
     * @return
     */
    @Operation(summary = "模板下载")
    @GetMapping("/TemplateDownload")
    public ActionResult<DownloadVO> templateDownload() {
        DownloadVO vo = DownloadVO.builder().build();
        try {
            vo.setName("职员信息.xlsx");
            vo.setUrl(UploaderUtil.uploaderFile("/api/file/DownloadModel?encryption=", "职员信息" +
                    ".xlsx" + "#" + "Temporary"));
        } catch (Exception e) {
            log.error("信息导出Excel错误:{}", e.getMessage());
        }
        return ActionResult.success(vo);
    }

    /**
     * 导出Excel
     *
     * @return
     */
    @Operation(summary = "导出Excel")
    @GetMapping("/ExportExcel")
    public ActionResult<DownloadVO> exportExcel() {
        List<EmployeeEntity> entityList = employeeService.getList();
        List<EmployeeExportVO> list = JsonUtil.listToJsonField(JsonUtil.getJsonToList(JsonUtilEx.getObjectToStringDateFormat(entityList, "yyyy-MM-dd"), EmployeeExportVO.class));
        List<ExcelExportEntity> entitys = new ArrayList<>();
        entitys.add(new ExcelExportEntity("工号", "enCode"));
        entitys.add(new ExcelExportEntity("姓名", "fullName"));
        entitys.add(new ExcelExportEntity("性别", "gender"));
        entitys.add(new ExcelExportEntity("部门", "departmentName"));
        entitys.add(new ExcelExportEntity("职务", "positionName", 25));
        entitys.add(new ExcelExportEntity("用工性质", "workingNature"));
        entitys.add(new ExcelExportEntity("身份证号", "idNumber", 25));
        entitys.add(new ExcelExportEntity("联系电话", "telephone", 20));
        entitys.add(new ExcelExportEntity("出生年月", "birthday", 20));
        entitys.add(new ExcelExportEntity("参加工作", "attendWorkTime", 20));
        entitys.add(new ExcelExportEntity("最高学历", "education"));
        entitys.add(new ExcelExportEntity("所学专业", "major"));
        entitys.add(new ExcelExportEntity("毕业院校", "graduationAcademy"));
        entitys.add(new ExcelExportEntity("毕业时间", "graduationTime", 20));
        ExportParams exportParams = new ExportParams(null, "职员信息");
        exportParams.setType(ExcelType.XSSF);
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, entitys, list);
        String name = "职员信息" + DateUtil.dateNow("yyyyMMdd") + "_" + RandomUtil.uuId() + ".xlsx";
        String fileName = configValueUtil.getTemporaryFilePath() + name;
        DownloadVO vo = DownloadVO.builder().build();
        try {
            @Cleanup FileOutputStream output = new FileOutputStream(fileName);
            workbook.write(output);
            vo.setName(name);
            vo.setUrl(UploaderUtil.uploaderFile(name + "#" + "Temporary"));
        } catch (Exception e) {
            log.error("信息导出Excel错误:{}", e.getMessage());
        }
        return ActionResult.success(vo);
    }

    /**
     * 导出Word
     *
     * @return
     */
    @Operation(summary = "导出Word")
    @GetMapping("/ExportWord")
    public ActionResult<DownloadVO> exportWord() {
        List<EmployeeEntity> list = employeeService.getList();
        //模板文件地址
        String inputUrl = configValueUtil.getTemplateFilePath() + "employee_export_template.docx";
        //新生产的模板文件
        String name = "职员信息" + DateUtil.dateNow("yyyyMMdd") + "_" + RandomUtil.uuId() + ".docx";
        String outputUrl = configValueUtil.getTemporaryFilePath() + name;
        List<String[]> testList = new ArrayList<>();
        Map<String, String> testMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String[] employee = new String[13];
            EmployeeEntity entity = list.get(i);
            employee[0] = entity.getFullName();
            employee[1] = entity.getGender();
            employee[2] = entity.getDepartmentName();
            employee[3] = entity.getPositionName();
            employee[4] = entity.getWorkingNature();
            employee[5] = entity.getIdNumber();
            employee[6] = entity.getTelephone();
            employee[7] = entity.getBirthday() != null ? DateUtil.daFormat(entity.getBirthday()) : "";
            employee[8] = entity.getAttendWorkTime() != null ? DateUtil.daFormat(entity.getAttendWorkTime()) : "";
            employee[9] = entity.getEducation();
            employee[10] = entity.getMajor();
            employee[11] = entity.getGraduationAcademy();
            employee[12] = entity.getGraduationTime() != null ? DateUtil.daFormat(entity.getGraduationTime()) : "";
            testList.add(employee);
        }
        WordUtil.changWord(inputUrl, outputUrl, testMap, testList);
        if (FileUtil.fileIsFile(outputUrl)) {
            DownloadVO vo = DownloadVO.builder().name(name).url(UploaderUtil.uploaderFile(name + "#" + "Temporary")).build();
            return ActionResult.success(vo);
        }
        return ActionResult.success("文件导出失败");
    }

    /**
     * 导出pdf
     *
     * @return
     */
    @Operation(summary = "导出pdf")
    @GetMapping("/ExportPdf")
    public ActionResult<DownloadVO> exportPdf() {
        String name = "职员信息" + DateUtil.dateNow("yyyyMMdd") + "_" + RandomUtil.uuId() + ".pdf";
        String outputUrl = configValueUtil.getTemporaryFilePath() + name;
        employeeService.exportPdf(employeeService.getList(), outputUrl);
        if (FileUtil.fileIsFile(outputUrl)) {
            DownloadVO vo = DownloadVO.builder().name(name).url(UploaderUtil.uploaderFile(name + "#" + "Temporary")).build();
            return ActionResult.success(vo);
        }
        return ActionResult.success("文件导出失败");
    }

    /**
     * 导出Excel
     *
     * @return
     */
    @Operation(summary = "导出Excel(备用)")
    @GetMapping("/Excel")
    public void excel() {
        Map<String, Object> map = new HashMap<>();
        List<EmployeeEntity> list = employeeService.getList();
        TemplateExportParams param = new TemplateExportParams(configValueUtil.getTemplateFilePath() + "employee_import_template.xlsx", true);
        map.put("Employee", JSON.parse(JSONObject.toJSONStringWithDateFormat(list, "yyyy-MM-dd")));
        Workbook workbook = ExcelExportUtil.exportExcel(param, map);
        ExcelUtil.dowloadExcel(workbook, "职员信息.xlsx");
    }


    /**
     * 上传文件(excel)
     *
     * @return
     */
    @Operation(summary = "上传文件")
    @PostMapping("/Uploader")
    public ActionResult<DownloadVO> uploader() {
        List<MultipartFile> list = UpUtil.getFileAll();
        MultipartFile file = list.get(0);
        if (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls")) {
            String filePath = configValueUtil.getTemporaryFilePath();
            String fileName = RandomUtil.uuId() + "." + UpUtil.getFileType(file);
            fileName = XSSEscape.escape(fileName);
            //上传文件
            FileInfo fileInfo = FileUploadUtils.uploadFile(file, filePath, fileName);
//            FileUtil.upFile(file, filePath, fileName);
            DownloadVO vo = DownloadVO.builder().build();
            vo.setName(fileInfo.getFilename());
            return ActionResult.success(vo);
        } else {
            return ActionResult.fail("选择文件不符合导入");
        }

    }

    /**
     * 导入预览
     *
     * @param fileName 文件名称
     * @return
     */
    @Operation(summary = "导入预览")
    @GetMapping("/ImportPreview")
    @Parameters({
            @Parameter(name = "fileName", description = "文件名称"),
    })
    public ActionResult importPreview(@RequestParam("fileName") String fileName) throws ImportException {
        Map<String, Object> map = new HashMap<>();
        try {
            String filePath = configValueUtil.getTemporaryFilePath();
            @Cleanup InputStream inputStream = new ByteArrayInputStream(FileUploadUtils.downloadFileByte(filePath, fileName, false));
            // 得到数据
            List<EmployeeModel> personList = ExcelUtil.importExcelByInputStream(inputStream, 0, 1, EmployeeModel.class);
            //预览数据
            map = employeeService.importPreview(personList);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ImportException(e.getMessage());
        }
        return ActionResult.success(map);
    }

    /**
     * 导入数据
     *
     * @param data 职工模型
     * @return
     */
    @Operation(summary = "导入数据")
    @PostMapping("/ImportData")
    @Parameters({
            @Parameter(name = "data", description = "职工模型"),
    })
    public ActionResult<EmployeeImportVO> importData(@RequestBody EmployeeModel data) {
        List<EmployeeModel> dataList = JsonUtil.getJsonToList(data.getList(), EmployeeModel.class);
        //导入数据
        EmployeeImportVO result = employeeService.importData(dataList);
        return ActionResult.success(result);
    }

    /**
     * 导出Excel(可选字段)
     *
     * @param paginationEmployee 分页模型
     * @return
     */
    @Operation(summary = "导出Excel（可选字段）")
    @GetMapping("/ExportData")
    public ActionResult<DownloadVO> exportExcelData(PaginationEmployee paginationEmployee) {
        String dataType = paginationEmployee.getDataType();
        String selectKey = paginationEmployee.getSelectKey();
        List<EmployeeEntity> entityList = new ArrayList<>();
        if (StringNumber.ZERO.equals(dataType)) {
            entityList = employeeService.getList(paginationEmployee);
        } else if (StringNumber.ONE.equals(dataType)) {
            entityList = employeeService.getList();
        }
        List<EmployeeExportVO> list = JsonUtil.listToJsonField(JsonUtil.getJsonToList(JsonUtilEx.getObjectToStringDateFormat(entityList, "yyyy-MM-dd HH:mm:ss"), EmployeeExportVO.class));
        List<ExcelExportEntity> entitys = new ArrayList<>();
        String[] splitData = selectKey.split(",");
        if (splitData != null && splitData.length > 0) {
            for (int i = 0; i < splitData.length; i++) {
                if ("enCode".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("工号", "enCode"));
                }
                if ("fullName".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("姓名", "fullName"));
                }
                if ("gender".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("性别", "gender"));
                }
                if ("departmentName".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("部门", "departmentName"));
                }
                if ("positionName".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("职务", "positionName", 25));
                }
                if ("workingNature".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("用工性质", "workingNature"));
                }
                if ("idNumber".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("身份证号", "idNumber", 25));
                }
                if ("telephone".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("联系电话", "telephone", 20));
                }
                if ("birthday".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("出生年月", "birthday", 20));
                }
                if ("attendWorkTime".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("参加工作", "attendWorkTime", 20));
                }
                if ("education".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("最高学历", "education"));
                }
                if ("major".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("所学专业", "major"));
                }
                if ("graduationAcademy".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("毕业院校", "graduationAcademy"));
                }
                if ("graduationTime".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("毕业时间", "graduationTime", 20));
                }
                if ("creatorTime".equals(splitData[i])) {
                    entitys.add(new ExcelExportEntity("创建时间", "creatorTime"));
                }
            }
        }
        ExportParams exportParams = new ExportParams(null, "职员信息");
        exportParams.setType(ExcelType.XSSF);
        DownloadVO vo = DownloadVO.builder().build();
        try {
            @Cleanup Workbook workbook = new HSSFWorkbook();
            if (entitys.size() > 0) {
                workbook = ExcelExportUtil.exportExcel(exportParams, entitys, list);
            }
            String name = "职员信息" + DateUtil.dateNow("yyyyMMdd") + "_" + RandomUtil.uuId() + ".xlsx";
            //上传文件
            MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, name);
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, temporaryFilePath, name);
            vo.setName(fileInfo.getFilename());
            vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + "Temporary") + "&name=" + name);
        } catch (Exception e) {
            log.error("信息导出Excel错误:{}", e.getMessage());
        }
        return ActionResult.success(vo);
    }

}
