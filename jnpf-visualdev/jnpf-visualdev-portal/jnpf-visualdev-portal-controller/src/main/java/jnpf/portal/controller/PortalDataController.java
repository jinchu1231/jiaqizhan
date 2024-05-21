package jnpf.portal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.constant.MsgCode;
import jnpf.portal.constant.PortalConst;
import jnpf.portal.entity.PortalEntity;
import jnpf.portal.model.PortalCustomPrimary;
import jnpf.portal.model.PortalDataForm;
import jnpf.portal.model.PortalInfoAuthVO;
import jnpf.portal.model.PortalReleaseForm;
import jnpf.portal.model.PortalReleaseVO;
import jnpf.portal.model.ReleaseModel;
import jnpf.portal.service.PortalDataService;
import jnpf.portal.service.PortalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
@Tag(name = "门户展示界面" , description = "Portal" )
@RequestMapping("/api/visualdev/Portal" )
public class PortalDataController extends SuperController<PortalService, PortalEntity> {
    @Autowired
    private PortalDataService portalDataService;

    @Operation(summary = "设置默认门户" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @PutMapping("/{id}/Actions/SetDefault" )
    @SaCheckPermission("onlineDev.visualPortal" )
    @Transactional
    public ActionResult<String> SetDefault(@PathVariable("id") String id, String platform) {
        portalDataService.setCurrentDefault(platform, id);
        return ActionResult.success("设置成功" );
    }

    @Operation(summary = "门户自定义保存" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @PutMapping("/Custom/Save/{id}")
    public ActionResult<String> customSave(@PathVariable("id" ) String id, @RequestBody PortalDataForm portalDataForm) throws Exception {
        StpUtil.checkPermissionOr("onlineDev.visualPortal" , id);
        portalDataForm.setPortalId(id);
        portalDataService.createOrUpdate(
                new PortalCustomPrimary(portalDataForm.getPlatform(), portalDataForm.getPortalId()),
                portalDataForm.getFormData());
        return ActionResult.success(MsgCode.SU002.getMsg());
    }

    @Operation(summary = "门户发布(同步)" )
    @Parameters({
            @Parameter(name = "portalId" , description = "门户主键" ),
    })
    @PutMapping("/Actions/release/{portalId}" )
    @Transactional(rollbackFor = Exception.class)
    public ActionResult<PortalReleaseVO> release(@PathVariable("portalId") String portalId, @RequestBody @Valid PortalReleaseForm form) throws Exception {
        if (form.getPcPortal() == 1)
            portalDataService.release(PortalConst.WEB, portalId, form.getPcPortalSystemId(), PortalConst.WEB);
        if (form.getAppPortal() == 1)
            portalDataService.release(PortalConst.APP, portalId, form.getAppPortalSystemId(), PortalConst.APP);

        ReleaseModel releaseSystemModel = new ReleaseModel();
        releaseSystemModel.setPc(form.getPc());
        releaseSystemModel.setPcSystemId(form.getPcSystemId());
        releaseSystemModel.setPcModuleParentId(form.getPcModuleParentId());
        releaseSystemModel.setApp(form.getApp());
        releaseSystemModel.setAppSystemId(form.getAppSystemId());
        releaseSystemModel.setAppModuleParentId(form.getAppModuleParentId());
        portalDataService.releaseModule(releaseSystemModel,portalId);

        return ActionResult.success(MsgCode.SU011.get());
    }

    @Operation(summary = "个人门户详情" )
    @Parameters({
            @Parameter(name = "id" , description = "主键" ),
    })
    @GetMapping("/{id}/auth" )
    public ActionResult<PortalInfoAuthVO> infoAuth(@PathVariable("id" ) String id, String platform, String systemId) {
        platform = platform.equalsIgnoreCase("pc") || platform.equalsIgnoreCase(PortalConst.WEB) ? PortalConst.WEB : PortalConst.APP;
        try{
            return ActionResult.success(portalDataService.getDataFormView(id, platform));
        }catch (Exception e){
            return ActionResult.fail(e.getMessage());
        }
    }

}
