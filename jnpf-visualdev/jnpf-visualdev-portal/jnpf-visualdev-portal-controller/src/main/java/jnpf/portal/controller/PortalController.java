package jnpf.portal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.PortalManageEntity;
import jnpf.base.model.VisualFunctionModel;
import jnpf.base.service.ModuleService;
import jnpf.base.service.PortalManageService;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.emnus.ExportModelTypeEnum;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.portal.constant.PortalConst;
import jnpf.portal.entity.PortalEntity;
import jnpf.portal.model.PortalCrForm;
import jnpf.portal.model.PortalExportDataVo;
import jnpf.portal.model.PortalInfoVO;
import jnpf.portal.model.PortalModPrimary;
import jnpf.portal.model.PortalPagination;
import jnpf.portal.model.PortalSelectModel;
import jnpf.portal.model.PortalSelectVO;
import jnpf.portal.model.PortalUpForm;
import jnpf.portal.model.PortalViewPrimary;
import jnpf.portal.service.PortalDataService;
import jnpf.portal.service.PortalService;
import jnpf.util.FileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 可视化门户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@RestController
@Tag(name = "可视化门户" , description = "Portal" )
@RequestMapping("/api/visualdev/Portal" )
public class PortalController extends SuperController<PortalService, PortalEntity> {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private PortalService portalService;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private PortalDataService portalDataService;
    @Autowired
    private PortalManageService portalManageService;
    @Autowired
    private ModuleService moduleApi;

    @Operation(summary = "门户列表" )
    @GetMapping
    @SaCheckPermission("onlineDev.visualPortal" )
    public ActionResult list(PortalPagination portalPagination) {
        List<VisualFunctionModel> modelAll = portalService.getModelList(portalPagination);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(portalPagination, PaginationVO.class);
        return ActionResult.page(modelAll, paginationVO);
    }

    @Operation(summary = "门户树形列表" )
    @Parameters({
            @Parameter(name = "type" , description = "类型：0-门户设计,1-配置路径" ),
    })
    @GetMapping("/Selector" )
    public ActionResult<ListVO<PortalSelectVO>> listSelect(String platform,String type) {
        List<PortalSelectModel> modelList = new ArrayList<>();
        if(StringUtil.isNotEmpty(type)){
            modelList.addAll(portalService.getModList(new PortalViewPrimary(platform, null)));
        }else {
            modelList.addAll(portalService.getModSelectList());
        }
        List<SumTree<PortalSelectModel>> sumTrees = TreeDotUtils2.convertListToTreeDot(modelList);
        List<PortalSelectVO> jsonToList = JsonUtil.getJsonToList(sumTrees, PortalSelectVO.class);
        return ActionResult.success(new ListVO<>(jsonToList));
    }

    @Operation(summary = "门户详情" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @GetMapping("/{id}" )
    public ActionResult<PortalInfoVO> info(@PathVariable("id" ) String id, String platform) throws Exception {
        StpUtil.checkPermissionOr("onlineDev.visualPortal" , id);
        PortalEntity entity = portalService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail("数据不存在");
        }
        PortalInfoVO vo = JsonUtil.getJsonToBean(JsonUtilEx.getObjectToStringDateFormat(entity, "yyyy-MM-dd HH:mm:ss" ), PortalInfoVO.class);
        vo.setFormData(portalDataService.getModelDataForm(new PortalModPrimary(id)));
        List<PortalManageEntity> isReleaseList = portalManageService.list();
        List<ModuleEntity> moduleEntityList = moduleApi.getModuleByPortal(Collections.singletonList(id));
        vo.setPcPortalIsRelease(isReleaseList.stream().anyMatch(t-> t.getPortalId().equalsIgnoreCase(entity.getId())
                && PortalConst.WEB.equalsIgnoreCase(t.getPlatform())) ? 1 : 0);
        vo.setAppPortalIsRelease(isReleaseList.stream().anyMatch(t-> t.getPortalId().equalsIgnoreCase(entity.getId())
                && PortalConst.APP.equalsIgnoreCase(t.getPlatform())) ? 1 : 0);
        vo.setPcIsRelease(moduleEntityList.stream().anyMatch(moduleEntity -> moduleEntity.getModuleId().equals(entity.getId()) && PortalConst.WEB.equals(moduleEntity.getCategory())) ? 1 :0);
        vo.setAppIsRelease(moduleEntityList.stream().anyMatch(moduleEntity -> moduleEntity.getModuleId().equals(entity.getId()) && PortalConst.APP.equals(moduleEntity.getCategory())) ? 1 :0);
        return ActionResult.success(vo);
    }

    @Operation(summary = "删除门户" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @DeleteMapping("/{id}" )
    @SaCheckPermission("onlineDev.visualPortal" )
    @DSTransactional
    public ActionResult<String> delete(@PathVariable("id" ) String id) {
        PortalEntity entity = portalService.getInfo(id);
        if (entity != null) {
            try {
                portalService.delete(entity);
            } catch (Exception e) {
                return ActionResult.fail(e.getMessage());
            }
        }
        return ActionResult.success(MsgCode.SU003.get());
    }

    @Operation(summary = "创建门户" )
    @PostMapping()
    @SaCheckPermission("onlineDev.visualPortal" )
    @DSTransactional
    public ActionResult<String> create(@RequestBody @Valid PortalCrForm portalCrForm) throws Exception {
        PortalEntity entity = JsonUtil.getJsonToBean(portalCrForm, PortalEntity.class);
        entity.setId(RandomUtil.uuId());
        //判断名称是否重复
        if (portalService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail("门户" + MsgCode.EXIST001.get());
        }
        //判断编码是否重复
        if (portalService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("门户" + MsgCode.EXIST002.get());
        }
        // 修改模板排版数据
        if(Objects.equals(entity.getType(),1)){
            entity.setEnabledLock(null);
        }
        // 修改模板排版数据
        portalService.create(entity);
        portalDataService.createOrUpdate(new PortalModPrimary(entity.getId()), portalCrForm.getFormData());
        return ActionResult.success(MsgCode.SU001.get(), entity.getId());
    }

    @Operation(summary = "复制功能" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @PostMapping("/{id}/Actions/Copy" )
    @SaCheckPermission("onlineDev.visualPortal" )
    public ActionResult copyInfo(@PathVariable("id" ) String id) throws Exception {
        PortalEntity entity = portalService.getInfo(id);
        entity.setEnabledMark(0);
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        entity.setFullName(entity.getFullName() + ".副本" + copyNum);
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        entity.setId(RandomUtil.uuId());
        entity.setEnCode(entity.getEnCode() + copyNum);
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(userProvider.get().getUserId());
        PortalEntity entity1 = JsonUtil.getJsonToBean(entity, PortalEntity.class);
        if (entity1.getEnCode().length() > 50 || entity1.getFullName().length() > 50) {
            return ActionResult.fail("已到达该模板复制上限，请复制源模板" );
        }
        portalService.create(entity1);
        portalDataService.createOrUpdate(new PortalModPrimary(entity1.getId()),
                portalDataService.getModelDataForm(new PortalModPrimary(id)));
        return ActionResult.success(MsgCode.SU007.get());
    }

    @Operation(summary = "修改门户" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @PutMapping("/{id}" )
    @SaCheckPermission("onlineDev.visualPortal" )
    @DSTransactional
    public ActionResult<String> update(@PathVariable("id" ) String id, @RequestBody @Valid PortalUpForm portalUpForm) throws Exception {
        PortalEntity originEntity = portalService.getInfo(portalUpForm.getId());
        //判断名称是否重复
        if (!originEntity.getFullName().equals(portalUpForm.getFullName()) && StringUtil.isNotEmpty(portalUpForm.getFullName())) {
            if (portalService.isExistByFullName(portalUpForm.getFullName(), portalUpForm.getId())) {
                return ActionResult.fail("门户" + MsgCode.EXIST001.get());
            }
        }
        //判断编码是否重复
        if (!originEntity.getEnCode().equals(portalUpForm.getEnCode()) && StringUtil.isNotEmpty(portalUpForm.getEnCode())) {
            if (portalService.isExistByEnCode(portalUpForm.getEnCode(), portalUpForm.getId())) {
                return ActionResult.fail("门户" + MsgCode.EXIST002.get());
            }
        }
        // 修改排版数据
        if(Objects.equals(portalUpForm.getType(),1)){
            portalUpForm.setEnabledLock(null);
        }
        //修改状态
        if(Objects.equals(originEntity.getState(),1)){
            originEntity.setState(2);
            portalUpForm.setState(2);
        }
        // 修改排版数据
        portalDataService.createOrUpdate(new PortalModPrimary(portalUpForm.getId()), portalUpForm.getFormData());
        if (StringUtil.isNotEmpty(portalUpForm.getFullName()) && StringUtil.isNotEmpty(portalUpForm.getEnCode())) {
            portalService.update(id, JsonUtil.getJsonToBean(portalUpForm, PortalEntity.class));
        }else {
            portalService.update(id, originEntity);
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    @Operation(summary = "门户导出" )
    @Parameters({
            @Parameter(name = "modelId" , description = "模板id" ),
    })
    @PostMapping("/{modelId}/Actions/ExportData" )
    @SaCheckPermission("onlineDev.visualPortal" )
    public ActionResult exportFunction(@PathVariable("modelId" ) String modelId) throws Exception {
        PortalEntity entity = portalService.getInfo(modelId);
        if (entity != null) {
            PortalExportDataVo vo = new PortalExportDataVo();
            BeanUtils.copyProperties(entity, vo);
            vo.setId(null);
            vo.setModelType(ExportModelTypeEnum.Portal.getMessage());
            vo.setFormData(portalDataService.getModelDataForm(new PortalModPrimary(entity.getId())));
            DownloadVO downloadVO = fileExport.exportFile(vo, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.VISUAL_PORTAL.getTableName());
            return ActionResult.success(downloadVO);
        } else {
            return ActionResult.success("并无该条数据" );
        }
    }

    @SneakyThrows
    @Operation(summary = "门户导入" )
    @Parameters({
            @Parameter(name = "file" , description = "导入文件" ),
    })
    @PostMapping(value = "/Model/Actions/ImportData" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("onlineDev.visualPortal" )
    public ActionResult importFunction(@RequestPart("file" ) MultipartFile multipartFile) throws Exception {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.VISUAL_PORTAL.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        PortalExportDataVo vo = JsonUtil.getJsonToBean(fileContent, PortalExportDataVo.class);
        if (vo.getModelType() == null || !vo.getModelType().equals(ExportModelTypeEnum.Portal.getMessage())) {
            return ActionResult.fail("请导入对应功能的json文件" );
        }
        //判断名称是否重复
        if (portalService.isExistByFullName(vo.getFullName(), null)) {
            return ActionResult.fail("门户" + MsgCode.EXIST001.get());
        }
        //判断编码是否重复
        if (portalService.isExistByEnCode(vo.getEnCode(), null)) {
            return ActionResult.fail("门户" + MsgCode.EXIST002.get());
        }
        PortalEntity entity = JsonUtil.getJsonToBean(fileContent, PortalEntity.class);
        entity.setId(null);
        entity.setEnabledMark(0);
        entity.setSortCode(0l);
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        portalService.create(entity);
        portalDataService.createOrUpdate(new PortalModPrimary(entity.getId()), vo.getFormData());
        return ActionResult.success(MsgCode.IMP001.get());
    }

    @Operation(summary = "门户管理下拉列表" )
    @GetMapping("/manage/Selector/{systemId}" )
    public ActionResult<PageListVO<PortalSelectVO>> getManageSelectorList(@PathVariable String systemId, PortalPagination portalPagination) {
        portalPagination.setType(null); // 门户设计、配置路径。全选
        List<PortalSelectVO> voList = portalService.getManageSelectorPage(portalPagination, systemId);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(portalPagination, PaginationVO.class);
        return ActionResult.page(voList, paginationVO);
    }

}
