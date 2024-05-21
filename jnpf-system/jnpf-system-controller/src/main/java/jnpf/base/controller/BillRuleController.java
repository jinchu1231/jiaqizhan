package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.annotation.HandleLog;
import jnpf.base.ActionResult;
import jnpf.base.entity.BillRuleEntity;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.model.billrule.BillRuleCrForm;
import jnpf.base.model.billrule.BillRuleInfoVO;
import jnpf.base.model.billrule.BillRuleListVO;
import jnpf.base.model.billrule.BillRulePagination;
import jnpf.base.model.billrule.BillRuleUpForm;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DataFileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;

/**
 * 单据规则
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "单据规则", description = "BillRule")
@RestController
@RequestMapping("/api/system/BillRule")
public class BillRuleController extends SuperController<BillRuleService, BillRuleEntity> {

    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    /**
     * 列表
     *
     * @param pagination 分页模型
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据规则列表(带分页)")
    @SaCheckPermission("system.billRule")
    @GetMapping
    public ActionResult<PageListVO<BillRuleListVO>> list(BillRulePagination pagination) {
        List<BillRuleEntity> list = billRuleService.getList(pagination);
        List<BillRuleListVO> listVO = new ArrayList<>();
        list.forEach(entity->{
            BillRuleListVO vo = JsonUtil.getJsonToBean(entity, BillRuleListVO.class);
            if(StringUtil.isNotEmpty(entity.getCategory())){
                DictionaryDataEntity dataEntity = dictionaryDataService.getInfo(entity.getCategory());
                vo.setCategory(dataEntity != null ? dataEntity.getFullName() : null);
            }

            UserEntity userEntity = userService.getInfo(entity.getCreatorUserId());
            if(userEntity != null){
                vo.setCreatorUser(userEntity.getRealName() + "/" + userEntity.getAccount());
            }
            listVO.add(vo);
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 列表
     *
     * @param pagination 分页模型
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据规则下拉框")
    @GetMapping("/Selector")
    public ActionResult<PageListVO<BillRuleListVO>> selectList(BillRulePagination pagination) {
        List<BillRuleEntity> list = billRuleService.getListByCategory(pagination.getCategoryId(),pagination);
        List<BillRuleListVO> listVO = new ArrayList<>();
        list.forEach(entity->{
            BillRuleListVO vo = JsonUtil.getJsonToBean(entity, BillRuleListVO.class);
            if(StringUtil.isNotEmpty(entity.getCategory())){
                DictionaryDataEntity dataEntity = dictionaryDataService.getInfo(entity.getCategory());
                vo.setCategory(dataEntity != null ? dataEntity.getFullName() : null);
            }

            UserEntity userEntity = userService.getInfo(entity.getCreatorUserId());
            if(userEntity != null){
                vo.setCreatorUser(userEntity.getRealName() + "/" + userEntity.getAccount());
            }
            listVO.add(vo);
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }


    /**
     * 更新组织状态
     *
     * @param id 主键值
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "修改")
    @Operation(summary = "更新单据规则状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        BillRuleEntity entity = billRuleService.getInfo(id);
        if (entity != null) {
            if (entity.getEnabledMark() == 1) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            billRuleService.update(entity.getId(), entity);
            return ActionResult.success("更新成功");
        }
        return ActionResult.fail("更新失败，数据不存在");
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据规则信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id) throws DataException {
        BillRuleEntity entity = billRuleService.getInfo(id);
        BillRuleInfoVO vo = JsonUtil.getJsonToBeanEx(entity, BillRuleInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 获取单据流水号
     *
     * @param enCode 参数编码
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据流水号(工作流调用)")
    @Parameters({
            @Parameter(name = "enCode", description = "参数编码", required = true)
    })
    @GetMapping("/BillNumber/{enCode}")
    public ActionResult GetBillNumber(@PathVariable("enCode") String enCode) throws DataException {
        String data = billRuleService.getBillNumber(enCode, true);
        return ActionResult.success("获取成功", data);
    }

    /**
     * 新建
     *
     * @param billRuleCrForm 实体对象
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "新增")
    @Operation(summary = "添加单据规则")
    @Parameters({
            @Parameter(name = "billRuleCrForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.billRule")
    @PostMapping
    public ActionResult create(@RequestBody @Valid BillRuleCrForm billRuleCrForm) {
        BillRuleEntity entity = JsonUtil.getJsonToBean(billRuleCrForm, BillRuleEntity.class);
        if (billRuleService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail("名称不能重复");
        }
        if (billRuleService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("编码不能重复");
        }
        billRuleService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 更新
     *
     * @param billRuleUpForm 实体对象
     * @param id             主键值
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "修改")
    @Operation(summary = "修改单据规则")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "billRuleUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.billRule")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody BillRuleUpForm billRuleUpForm) {
        BillRuleEntity entity = JsonUtil.getJsonToBean(billRuleUpForm, BillRuleEntity.class);
        if (billRuleService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail("名称不能重复");
        }
        if (billRuleService.isExistByEnCode(entity.getEnCode(), id)) {
            return ActionResult.fail("编码不能重复");
        }
        boolean flag = billRuleService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "删除")
    @Operation(summary = "删除单据规则")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        BillRuleEntity entity = billRuleService.getInfo(id);
        if (entity != null) {
            if (!StringUtils.isEmpty(entity.getOutputNumber())) {
                return ActionResult.fail("单据已经被使用,不允许被删除");
            } else {
                billRuleService.delete(entity);
                return ActionResult.success("删除成功");
            }
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 获取单据缓存
     *
     * @param enCode 参数编码
     * @return
     */
    
    @GetMapping("/useBillNumber/{enCode}")
    public ActionResult useBillNumber(@PathVariable("enCode") String enCode) {
        billRuleService.useBillNumber(enCode);
        return ActionResult.success();
    }

    /**
     * 获取单据流水号
     *
     * @param enCode 参数编码
     * @return
     */
    
    @GetMapping("/getBillNumber/{enCode}")
    public ActionResult getBillNumber(@PathVariable("enCode") String enCode) throws DataException {
        Object data = billRuleService.getBillNumber(enCode, false);
        return ActionResult.success(data);
    }

    /**
     * 导出单据规则
     * @param id 打印模板id
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "导出")
    @Operation(summary = "导出")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @GetMapping("/{id}/Actions/Export")
    public ActionResult<DownloadVO> export(@PathVariable String id){
        BillRuleEntity entity = billRuleService.getInfo(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.SYSTEM_BILLRULE.getTableName());
        return ActionResult.success(downloadVO);
    }

    /**
     * 导入单据规则
     * @param multipartFile 备份json文件
     * @return 执行结果标识
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "导入")
    @Operation(summary = "导入")
    @SaCheckPermission("system.billRule")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importData(@RequestPart("file") MultipartFile multipartFile,
                                   @RequestParam("type") Integer type) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.SYSTEM_BILLRULE.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        try {
            String fileContent = FileUtil.getFileContent(multipartFile);
            BillRuleEntity entity = JsonUtil.getJsonToBean(fileContent, BillRuleEntity.class);
            return billRuleService.ImportData(entity, type);
        } catch (Exception e) {
            throw new DataException(MsgCode.IMP004.get());
        }

    }

}
